package com.cisco.dse.global.migration.productlanding;

import java.io.IOException;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ProductLandingVariation3 {

	Document doc;

	String title = null;

	StringBuilder sb = new StringBuilder(1024);
	
	static Logger log = Logger.getLogger(ProductLandingVariation3.class);

	public String translate(String loc, String prod, String type, String catType,
			String locale, Session session) throws IOException,
			ValueFormatException, VersionException, LockException,
			ConstraintViolationException, RepositoryException {
		BasicConfigurator.configure();
		log.debug("In the translate method");
		log.debug("In the translate method, catType is :"+ catType);

		// Repo node paths
		String indexUpperLeft = "/content/<locale>/"+catType+"/<prod>/index/jcr:content/content_parsys/overview/layout-overview/gd12v1/gd12v1-left";
		String indexUpperRight = "/content/<locale>/"+catType+"/<prod>/index/jcr:content/content_parsys/overview/layout-overview/gd12v1/gd12v1-right";
		String indexLowerLeft = "/content/<locale>/"+catType+"/<prod>/index/jcr:content/content_parsys/overview/layout-overview/gd12v2/gd12v2-left";
		String indexLowerRight = "/content/<locale>/"+catType+"/<prod>/index/jcr:content/content_parsys/overview/layout-overview/gd12v2/gd12v2-right";
		String pageUrl = "http://chard.cisco.com:4502/content/<locale>/"+catType+"/<prod>/index.html";
		
		pageUrl = pageUrl.replace("<locale>", locale).replace("<prod>", prod);
		
		sb.append("<td>" + "<a href="+pageUrl+">"+pageUrl+"</a>"+"</td>");
		sb.append("<td>" + "<a href="+loc+">"+loc +"</a>"+ "</td>");
		sb.append("<td><ul>");


		indexUpperLeft = indexUpperLeft.replace("<locale>", locale).replace("<prod>", prod);
		indexUpperRight = indexUpperRight.replace("<locale>", locale).replace("<prod>", prod);
		indexLowerLeft = indexLowerLeft.replace("<locale>", locale).replace("<prod>", prod);
		indexLowerRight = indexLowerRight.replace("<locale>", locale).replace("<prod>", prod);

		javax.jcr.Node indexUpperLeftNode = null;
		javax.jcr.Node indexUpperRightNode = null;
		javax.jcr.Node indexLowerLeftNode = null;
		javax.jcr.Node indexLowerRightNode = null;
		try {
			indexUpperLeftNode = session.getNode(indexUpperLeft);
			indexUpperRightNode = session.getNode(indexUpperRight);
			indexLowerLeftNode = session.getNode(indexLowerLeft);
			indexLowerRightNode = session.getNode(indexLowerRight);

			try {
				doc = Jsoup.connect(loc).get();
			} catch (Exception e) {
				sb.append("<li>Cannot Connect to given URL. \n"+loc+"</li>");
			}

			title = doc.title();
			// ------------------------------------------------------------------------------------------------------------------------------------------
			// start set primary CTA content.
			String primaryCTATitle = "";
			String primaryCTADescription = "";
			String primaryCTALinkText = "";
			String primaryCTALinkUrl = "";
			try {
				Elements primaryCTAElements = doc.select("div.c47-pilot");
				if (primaryCTAElements != null) {
					Element primaryCTAElement = doc.select("div.c47-pilot").first();
					if (primaryCTAElement != null) {
						Elements titleElements = primaryCTAElement.getElementsByTag("h3");
						if (titleElements != null) {
							Element titleElement = titleElements.first();
							if (titleElement != null) {
								primaryCTATitle = titleElement.text();
							} else {
								sb.append("<li>Primary CTA Heding element not having title</li>");
							}
						} else {
							 sb.append("<li>Primary CTA Heading element section not found </li>");
						}
						Elements paraElements = primaryCTAElement.getElementsByTag("p");
						if (paraElements != null) {
							Element paraElement = paraElements.first();
							if (paraElement != null) {
								primaryCTADescription = paraElement.text();
							} else {
								sb.append("<li>Primary CTA Para element not having title</li>");
							}
						} else {
							 sb.append("<li>Primary CTA Para element section not found </li>");
						}
						Elements ctaLinksElements = primaryCTAElement.select("ul.cta-links");
						if (ctaLinksElements != null) {
							Elements ctaLiElements = ctaLinksElements.select("li.cta");
							if (ctaLiElements != null) {
								Element ctaLiElement = ctaLiElements.first();
								if (ctaLiElement != null) {
									Elements anchorElements = ctaLiElement.getElementsByTag("a");
									if (anchorElements != null) {
										Element anchorElement = anchorElements.first();
										if (anchorElement != null) {
											primaryCTALinkText = anchorElement.text();
											primaryCTALinkUrl = anchorElement.attr("href");
										} else {
											sb.append("<li>Primary CTA Link anchor tag not found </li>");
										}
									} else {
										sb.append("<li>Primary CTA Link anchor tag section not found </li>");
									}
								} else {
									sb.append("<li>Primary CTA Link not found </li>");
								}
							} else {
								sb.append("<li>Primary CTA Links not found </li>");
							}
						} else {
							sb.append("<li>Primary CTA Links section not found </li>");
						}
						log.debug("primaryCTATitle" + primaryCTATitle + "\n");
						log.debug("primaryCTADescription" + primaryCTADescription + "\n");
						log.debug("primaryCTALinkText" + primaryCTALinkText + "\n");
						log.debug("primaryCTALinkUrl" + primaryCTALinkUrl + "\n");
						if (indexUpperLeftNode.hasNode("primary_cta")) {
							javax.jcr.Node primartCTANode = indexUpperLeftNode.getNode("primary_cta");
							 if(StringUtils.isNotBlank(primaryCTATitle)){
								 primartCTANode.setProperty("title", primaryCTATitle);
			                  } else{
                                 sb.append("title property is not set at " + primartCTANode.getPath());
			                  }
							 if(StringUtils.isNotBlank(primaryCTADescription)){
								 primartCTANode.setProperty("description", primaryCTADescription);
			                  } else{
                                 sb.append("description property is not set at " + primartCTANode.getPath());
			                  }
							 if(StringUtils.isNotBlank(primaryCTALinkText)){
								 primartCTANode.setProperty("linktext", primaryCTALinkText);
			                  } else{
                                 sb.append("linktext property is not set at " + primartCTANode.getPath());
			                  }
							
							if (primartCTANode.hasNode("linkurl")) {
								javax.jcr.Node primartCTALinkUrlNode = primartCTANode.getNode("linkurl");
								if(StringUtils.isNotBlank(primaryCTALinkUrl)){
									primartCTALinkUrlNode.setProperty("url", primaryCTALinkUrl);
				                  } else{
	                                 sb.append("url property is not set at " + primartCTALinkUrlNode.getPath());
				                 }
							} else {
								sb.append("<li>primary_cta_v2 node is not having linkurl node</li>");
							}
						} else {								
							sb.append("<li>primary_cta_v2 node doesn't exists</li>");
						}
				  } else {								
						sb.append("<li>CTA element not found.</li>");
					}
				} else {
					sb.append("<li> CTA element section not found.</li>");
				}
				} catch (Exception e) {
				sb.append("<li>Unable to update primary_cta_v2 component."+e+"</li>");
			}

			// end set primary CTA content.
			// ---------------------------------------------------------------------------------------------------------------------------------------
			// ------------------------------------------------------------------------------------------------------------------------------------------
			// start set Hero Large component content.
			try {
				javax.jcr.Node heroLargeNode = null;
				NodeIterator heroPanelIterator = null;
				if (indexUpperRightNode.hasNode("hero_large")) {
				heroLargeNode = indexUpperRightNode.getNode("hero_large");
					if (heroLargeNode.hasNodes()) {
						heroPanelIterator = heroLargeNode.getNodes("heropanel*");
					}
				} else{
                    sb.append("<li>Node with name 'hero_large' doesn't exist under "+indexUpperRightNode.getPath()+"</li>");
				}
				
				Elements heroLargeElements = doc.select("div.c50-pilot");
				if (heroLargeElements != null) {
					Element heroLargeElement = heroLargeElements.first();
					if (heroLargeElement != null) {
						Elements heroLargeFrameElements = heroLargeElement.select("div.frame");
						javax.jcr.Node heroPanelNode = null;
				
						if (heroLargeFrameElements != null) {
							if (heroLargeFrameElements.size() != heroLargeNode.getNodes("heropanel*").getSize()) {
								sb.append("Mismatch in Hero Panels.\n");
							}
							for (Element ele : heroLargeFrameElements) {
								String heroPanelTitle = "";
								String heroPanelDescription = "";
								String heroPanelLinkText = "";
								String heroPanellinkUrl = "";
								Elements heroTitleElements = ele.getElementsByTag("h2");
								if (heroTitleElements != null) {
									Element heroTitleElement = heroTitleElements.first();
									if (heroTitleElement != null) {
										heroPanelTitle = heroTitleElement.text();
									} else {
										 sb.append("<li>Hero Panel element not having any title in it </li>");
									}
								} else {
									sb.append("<li>Hero Panel title element is not found</li>");
								}
								Elements heroDescriptionElements = ele.getElementsByTag("p");
								if (heroDescriptionElements != null) {
									Element heroDescriptionElement = heroDescriptionElements.first();
									if (heroDescriptionElement != null) {
										heroPanelDescription = heroDescriptionElement.text();
									} else {
										sb.append("<li>Hero Panel element not having any description in it </li>");
									}
								} else {
									sb.append("<li>Hero Panel para element not found </li>");
								}
								Elements heroPanelLinkTextElements = ele.getElementsByTag("b");
								if (heroPanelLinkTextElements != null) {
									Element heroPanelLinkTextElement = heroPanelLinkTextElements.first();
									if (heroPanelLinkTextElement != null) {
										heroPanelLinkText = heroPanelLinkTextElement.text();
									} else {
										sb.append("<li>Hero Panel element not having any linktext in it </li>");
									}
								} else {
									sb.append("<li>Hero Panel linktext element not found  </li>");
								}
								Elements heroPanelLinkUrlElements = ele.getElementsByTag("a");
								if (heroPanelLinkUrlElements != null) {
									Element heroPanelLinkUrlElement = heroPanelLinkUrlElements.first();
									if (heroPanelLinkUrlElement != null) {
										heroPanellinkUrl =heroPanelLinkUrlElement.attr("href");
									} else {
										sb.append("<li>Hero Panel element not having any linkurl in it </li>");
									}
								} else {
									sb.append("<li>Hero Panel link url element not found </li>");
								}
								log.debug("heroPanelTitle " + heroPanelTitle + "\n");
								log.debug("heroPanelDescription " + heroPanelDescription + "\n");
								log.debug("heroPanelLinkText " + heroPanelLinkText + "\n");
								log.debug("heroPanellinkUrl " + heroPanellinkUrl + "\n");
								if (heroPanelIterator != null && heroPanelIterator.hasNext())
									heroPanelNode = heroPanelIterator.nextNode();
									if (heroPanelNode != null) {
										if (StringUtils.isNotBlank(heroPanelTitle)) {
											heroPanelNode.setProperty("title", heroPanelTitle);
										} else {
											sb.append("title property is not set at " + heroPanelNode.getPath());
										}
										if (StringUtils.isNotBlank(heroPanelDescription)) {
											heroPanelNode.setProperty("description", heroPanelDescription);
										} else {
											sb.append("description property is not set at " + heroPanelNode.getPath());
										}
										if (StringUtils.isNotBlank(heroPanelLinkText)) {
											heroPanelNode.setProperty("linktext", heroPanelLinkText);
										} else {
											sb.append("linktext property is not set at " + heroPanelNode.getPath());
										}
										if (StringUtils.isNotBlank(heroPanellinkUrl)) {
											heroPanelNode.setProperty("linkurl", heroPanellinkUrl);
										} else {
											sb.append("linkurl property is not set at " + heroPanelNode.getPath());
										}
										
									}
							}
						} else {
							sb.append("<li>Hero Large Frames/Panel Elements is not found</li>");
						}
					} else {
						sb.append("<li>Hero Large Element is not found</li>");
					}
				 } else {
						sb.append("<li>Hero Large Element section is not found</li>");
				 }
				} catch (Exception e) {
				sb.append("<li>Unable to update hero_large component."+e+"</li>");
			}

			// end set Hero Large content.
			// ---------------------------------------------------------------------------------------------------------------------------------------
			// ------------------------------------------------------------------------------------------------------------------------------------------
			// start set text component content.
			try {
				Elements textElements = doc.select("div.c00-pilot");
				String text = "";
				if (textElements != null) {
					Element textElement = textElements.first();
					if (textElement != null) {
						text = textElement.outerHtml();
					} else {
						sb.append("<li>Text Element not found</li>");
					}
				} else {
					sb.append("<li>Text Element section not found</li>");
				}
				if (indexLowerLeftNode.hasNode("text")) {
					javax.jcr.Node textNode = indexLowerLeftNode.getNode("text");
					if (StringUtils.isNotBlank(text)) {
						textNode.setProperty("text", text);
					} else {
						sb.append("text property is not set at " + textNode.getPath());
					}
				}
				
			} catch (Exception e) {
				sb.append("<li>Unable to update drawers_container component."+e+"</li>");
			}

			// end set text component content.
			// --------------------------------------------------------------------------------------------------------------------------
			
			// ---------------------------------------------------------------------------------------------------------------------------------------
			// start of html blob components content.
			try {
				Elements htmlblobElements = doc.select("ul.n21");
				String html = "";
				if (htmlblobElements != null) {
					Element htmlblobElement = htmlblobElements.first();
					if (htmlblobElement != null) {
						html = htmlblobElement.outerHtml();
					} else {
						sb.append("<li>html blob Element not found</li>");
					}
				} else {
					sb.append("<li>html blob Element Section not found</li>");
				}
				if (indexLowerLeftNode.hasNode("htmlblob")) {
					javax.jcr.Node htmlBlobNode = indexLowerLeftNode.getNode("htmlblob");
					log.debug("htmlblobElement.html() " + html + "\n");
					if (StringUtils.isNotBlank(html)) {
						htmlBlobNode.setProperty("html", html);
					} else {
						sb.append("html property is not set at " + htmlBlobNode.getPath());
					}
				}
			} catch (Exception e) {
				sb.append("<li>Unable to update html blob component."+e+"</li>");
			}
			// end set html blob component content.
			// --------------------------------------------------------------------------------------------------------------------------
			// --------------------------------------------------------------------------------------------------------------------------
			// start of tile bordered components.

			try {

				Elements rightRail = doc.select("div.c23-pilot,div.cc23-pilot");
				int rightcount = 0;
				boolean entry = true;
				if(rightRail != null){
					log.debug("rightRail size" + rightRail.size());
				if (rightRail.size() != indexLowerRightNode.getNodes("tile_bordered*").getSize()) {
                    sb.append("<li>Mis-Match in tilebordered Panels count/content."+rightRail.size()+" is not equal "+indexLowerRightNode.getNodes("tile_bordered*").getSize()+"</li>");
				}
				if(rightRail.size()>0){
					
				NodeIterator titleBorderNodes = indexLowerRightNode.getNodes("tile_bordered*");
					
				for (Element ele : rightRail) {
					Elements iconBlock = ele.select("div.icon-block");
					if (iconBlock != null && iconBlock.size() > 0) {
						continue;
					}
					javax.jcr.Node rightRailNode = null;
					String title = ele.getElementsByTag("h2")!=null?ele.getElementsByTag("h2").text():"";
					if(StringUtils.isBlank(title)){
						title = ele.getElementsByTag("h3")!=null?ele.getElementsByTag("h3").text():"";
					}
					String desc = ele.getElementsByTag("p")!=null?ele.getElementsByTag("p").text():"";
					Elements anchor = ele.getElementsByTag("a");

					String anchorText = anchor!=null?anchor.text():"";
					String anchorHref = anchor.attr("href");
					if (titleBorderNodes.hasNext()) {
						rightRailNode = (Node)titleBorderNodes.next();
					} else {
							sb.append("<li>all tile_boredered components are migrated</li>");
					}		
					
					
					if (rightRailNode != null) {
						if(title != null && title != "" && desc != null && desc != "" && anchorText != null && anchorText != ""){
						rightRailNode.setProperty("title", title);
						rightRailNode.setProperty("description", desc);
						rightRailNode.setProperty("linktext", anchorText);
						rightRailNode.setProperty("linkurl", anchorHref);
						log.debug("title, description, linktext and linkurl are created at "+rightRailNode.getPath());
						}else{
							sb.append("<li>Content miss match for "+ele.className()+"</li>");
						}
					}else{
						sb.append("<li>one of title_bordered node doesn't exist in node structure.</li>");
					}
				}
				}else{
					sb.append("<li>No Content with class 'c23-pilot or cc23-pilot' found</li>");
				}
				}else{
					sb.append("<li>div class c23-pilot not found in dom</li>");
				}
			} catch (Exception e) {
				sb.append("<li>Unable to update tile_bordered component.\n</li>");
			}
			// End of tile bordered components.
			// -----------------------------------------------------------------------------------------------------

			session.save();

		} catch (Exception e) {
			sb.append("<li>Exception "+e+"</li>");
		}
		
		sb.append("</ul></td>");
		
		return sb.toString();
	}
}