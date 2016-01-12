package com.cisco.dse.global.migration.productlanding;

import java.io.IOException;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.sling.commons.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.cisco.dse.global.migration.config.BaseAction;
import com.cisco.dse.global.migration.config.Constants;
import com.cisco.dse.global.migration.config.FrameworkUtils;

public class ProductLandingVariation3 extends BaseAction {

	Document doc;
	String title = null;
	StringBuilder sb = new StringBuilder(1024);
	static Logger log = Logger.getLogger(ProductLandingVariation3.class);

	public String translate(String host, String loc, String prod, String type,
			String catType, String locale, Session session,
			Map<String, String> urlMap) throws IOException,
			ValueFormatException, VersionException, LockException,
			ConstraintViolationException, RepositoryException {
		BasicConfigurator.configure();
		// Repo node paths
		String pagePropertiesPath = "/content/<locale>/" + catType + "/<prod>/index/jcr:content";
		String indexUpperLeft = "/content/<locale>/" + catType + "/<prod>/index/jcr:content/content_parsys/overview/layout-overview/gd12v1/gd12v1-left";
		String indexUpperRight = "/content/<locale>/" + catType + "/<prod>/index/jcr:content/content_parsys/overview/layout-overview/gd12v1/gd12v1-right";
		String indexLowerLeft = "/content/<locale>/" + catType + "/<prod>/index/jcr:content/content_parsys/overview/layout-overview/gd12v2/gd12v2-left";
		String indexLowerRight = "/content/<locale>/" + catType + "/<prod>/index/jcr:content/content_parsys/overview/layout-overview/gd12v2/gd12v2-right";
		String pageUrl = host + "/content/<locale>/" + catType + "/<prod>/index.html";

		pageUrl = pageUrl.replace("<locale>", locale).replace("<prod>", prod);
		pagePropertiesPath = pagePropertiesPath.replace("<locale>", locale).replace("<prod>", prod);
		sb.append("<td>" + "<a href=" + pageUrl + ">" + pageUrl + "</a>" + "</td>");
		sb.append("<td>" + "<a href=" + loc + ">" + loc + "</a>" + "</td>");
		sb.append("<td><ul>");

		log.debug("In the translate method to mgrate "+loc+" to "+pageUrl);
		
		indexUpperLeft = indexUpperLeft.replace("<locale>", locale).replace("<prod>", prod);
		indexUpperRight = indexUpperRight.replace("<locale>", locale).replace("<prod>", prod);
		indexLowerLeft = indexLowerLeft.replace("<locale>", locale).replace("<prod>", prod);
		indexLowerRight = indexLowerRight.replace("<locale>", locale).replace("<prod>", prod);

		Node indexUpperLeftNode = null;
		Node indexUpperRightNode = null;
		Node indexLowerLeftNode = null;
		Node indexLowerRightNode = null;
		Node pageJcrNode = null;
		try {
			indexUpperLeftNode = session.getNode(indexUpperLeft);
			indexUpperRightNode = session.getNode(indexUpperRight);
			indexLowerLeftNode = session.getNode(indexLowerLeft);
			indexLowerRightNode = session.getNode(indexLowerRight);
			pageJcrNode = session.getNode(pagePropertiesPath);
			try {
				doc = Jsoup.connect(loc).get();
				log.debug("Connected to the provided URL");
			} catch (Exception e) {
				doc = getConnection(loc);
			}
			if (doc != null) {
				title = doc.title();
				// ------------------------------------------------------------------------------------------------------------------------------------------
				// start set page properties.
				FrameworkUtils.setPageProperties(pageJcrNode, doc, session, sb);
				// end set page properties.
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
												// Start extracting valid href
												log.debug("primaryCTALinkUrl before migration : " + primaryCTALinkUrl);
												primaryCTALinkUrl = FrameworkUtils.getLocaleReference(primaryCTALinkUrl, urlMap);
												log.debug("primaryCTALinkUrl after migration : " + primaryCTALinkUrl);
												// End extracting valid href
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
							log.debug("primaryCTATitle : " + primaryCTATitle);
							log.debug("primaryCTADescription : " + primaryCTADescription);
							log.debug("primaryCTALinkText : " + primaryCTALinkText);
							log.debug("primaryCTALinkUrl : " + primaryCTALinkUrl);
							if (indexUpperLeftNode.hasNode("primary_cta")) {
								Node primartCTANode = indexUpperLeftNode.getNode("primary_cta");
								if (StringUtils.isNotBlank(primaryCTATitle)) {
									primartCTANode.setProperty("title", primaryCTATitle);
								} else {
									sb.append("<li>title of primary CTA doesn't exist</li>");
									log.debug("title property is not set at " + primartCTANode.getPath());
								}
								if (StringUtils.isNotBlank(primaryCTADescription)) {
									primartCTANode.setProperty("description", primaryCTADescription);
								} else {
									sb.append("<li>description of primary CTA doesn't exist</li>");
									log.debug("description property is not set at " + primartCTANode.getPath());
								}
								if (StringUtils.isNotBlank(primaryCTALinkText)) {
									primartCTANode.setProperty("linktext", primaryCTALinkText);
								} else {
									sb.append("<li>link text of primary CTA doesn't exist</li>");
									log.debug("linktext property is not set at " + primartCTANode.getPath());
								}

								if (primartCTANode.hasNode("linkurl")) {
									Node primartCTALinkUrlNode = primartCTANode.getNode("linkurl");
									if (StringUtils.isNotBlank(primaryCTALinkUrl)) {
										primartCTALinkUrlNode.setProperty("url", primaryCTALinkUrl);
									} else {
										sb.append("<li>link url of primary CTA doesn't exist</li>");
										log.debug("url property is not set at " + primartCTALinkUrlNode	.getPath());
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
						sb.append("<li> Primary CTA component not found on web publisher page.</li>");
					}
				} catch (Exception e) {
					sb.append("<li>Unable to update primary_cta_v2 component.</li>");
					log.error("Exception : ",e);
				}

				// end set primary CTA content.
				// ---------------------------------------------------------------------------------------------------------------------------------------
				// start set Hero Large component content.
				try {
					Node heroLargeNode = null;
					Value[] panelPropertiest = null;
					if (indexUpperRightNode.hasNode("hero_large")) {
						heroLargeNode = indexUpperRightNode.getNode("hero_large");
						Property panelNodesProperty = heroLargeNode.hasProperty("panelNodes") ? heroLargeNode.getProperty("panelNodes") : null;
						if (panelNodesProperty.isMultiple()) {
							panelPropertiest = panelNodesProperty.getValues();
						}
					} else {
						sb.append("<li>Node with name 'hero_large' doesn't exist under " + indexUpperRightNode.getPath() + "</li>");
					}

					Elements heroLargeElements = doc.select("div.c50-pilot");
					if (heroLargeElements != null) {
						Element heroLargeElement = heroLargeElements.first();
						if (heroLargeElement != null) {
							Elements heroLargeFrameElements = heroLargeElement.select("div.frame");
							Node heroPanelNode = null;
							if (heroLargeFrameElements != null) {
								if (heroLargeFrameElements.size() != heroLargeNode.getNodes("heropanel*").getSize()) {
									sb.append("<li>Mismatch in Hero Panels count.</li>");
								}
								int i = 0;
								int imageSrcEmptyCount = 0;
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
											log.debug("<li>Hero Panel element not having any title in it </li>");
										}
									} else {
										log.debug("<li>Hero Panel title element is not found</li>");
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
											heroPanellinkUrl = heroPanelLinkUrlElement.attr("href");
											// Start extracting valid href
											log.debug("Before heroPanellinkUrl" + heroPanellinkUrl);
											heroPanellinkUrl = FrameworkUtils.getLocaleReference(heroPanellinkUrl, urlMap);
											log.debug("after heroPanellinkUrl" + heroPanellinkUrl);
											// End extracting valid href
										} else {
											sb.append("<li>Hero Panel element not having any linkurl in it </li>");
										}
									} else {
										sb.append("<li>Hero Panel link url element not found </li>");
									}

									if (panelPropertiest != null && i <= panelPropertiest.length) {
										String propertyVal = panelPropertiest[i].getString();
										if (StringUtils.isNotBlank(propertyVal)) {
											JSONObject jsonObj = new JSONObject(propertyVal);
											if (jsonObj.has("panelnode")) {
												String panelNodeProperty = jsonObj.get("panelnode").toString();
												heroPanelNode = heroLargeNode.hasNode(panelNodeProperty) ? heroLargeNode.getNode(panelNodeProperty) : null;
											}
										}
										i++;
									} else {
										sb.append("<li>No heropanel Node found.</li>");
									}
									// start image
									String heroImage = FrameworkUtils.extractImagePath(ele, sb);
									log.debug("heroImage before migration : " + heroImage);
									if (heroPanelNode != null) {
										if (heroPanelNode.hasNode("image")) {
											Node imageNode = heroPanelNode.getNode("image");
											String fileReference = imageNode.hasProperty("fileReference") ? imageNode.getProperty("fileReference").getString() : "";
											heroImage = FrameworkUtils.migrateDAMContent(heroImage, fileReference, locale, sb);
											log.debug("heroImage after migration : " + heroImage);
											if (StringUtils.isNotBlank(heroImage)) {
												imageNode.setProperty("fileReference", heroImage);
											}else{
												imageSrcEmptyCount++;
											}
										} else {
											sb.append("<li>hero image node doesn't exist</li>");
										}
									}
									// end image
									log.debug("heroPanelTitle : " + heroPanelTitle);
									log.debug("heroPanelDescription : " + heroPanelDescription);
									log.debug("heroPanelLinkText : " + heroPanelLinkText);
									log.debug("heroPanellinkUrl : " + heroPanellinkUrl);

									if (heroPanelNode != null) {
										Node heroPanelPopUpNode = null;
										Elements lightBoxElements = ele.select("div.c50-image").select("a.c26v4-lightbox");
										if (lightBoxElements != null && !lightBoxElements.isEmpty()) {
											Element lightBoxElement = lightBoxElements.first();
											heroPanelPopUpNode = FrameworkUtils.getHeroPopUpNode(heroPanelNode);
										}
										if (StringUtils.isNotBlank(heroPanelTitle)) {
											heroPanelNode.setProperty("title", heroPanelTitle);
											if (heroPanelPopUpNode != null) {
												heroPanelPopUpNode.setProperty("popupHeader", heroPanelTitle);
											} else {
												sb.append("<li>Hero content video pop up node not found.</li>");
											}
										} else {
											sb.append("<li>title of hero slide doesn't exist</li>");
											log.debug("title property is not set at " + heroPanelNode.getPath());
										}
										if (StringUtils.isNotBlank(heroPanelDescription)) {
											heroPanelNode.setProperty("description", heroPanelDescription);
										} else {
											sb.append("<li>description of hero slide doesn't exist</li>");
											log.debug("description property is not set at " + heroPanelNode.getPath());
										}
										if (StringUtils.isNotBlank(heroPanelLinkText)) {
											heroPanelNode.setProperty("linktext", heroPanelLinkText);
										} else {
											sb.append("<li>link text of hero slide doesn't exist</li>");
											log.debug("linktext property is not set at " + heroPanelNode.getPath());
										}
										if (StringUtils.isNotBlank(heroPanellinkUrl)) {
											heroPanelNode.setProperty("linkurl", heroPanellinkUrl);
										} else {
											sb.append("<li>link url of hero slide doesn't exist / found video as link url for the slide on web publisher page</li> ");
											log.debug("linkurl property is not set at " + heroPanelNode.getPath());
										}
									}
								}if(imageSrcEmptyCount > 0){
									sb.append("<li> " +imageSrcEmptyCount+ "image(s) are not found on locale page's hero element. </li>");
								}
							} else {
								sb.append("<li>Hero Large Frames/Panel Elements is not found</li>");
							}
						} else {
							sb.append("<li>Hero Large Element is not found</li>");
						}
					} else {
						sb.append("<li>Hero Large component not found on web publisher page</li>");
					}
				} catch (Exception e) {
					sb.append("<li>Unable to update hero_large component.</li>");
					log.debug("Exception : ",e);
				}

				// end set Hero Large content.
				// ---------------------------------------------------------------------------------------------------------------------------------------
				// start set text component content.
				try {
					Elements textElements = doc.select("div.c00-pilot,div.c100-pilot");
					String text = "";
					if (textElements != null) {
						Element textElement = textElements.first();
						if (textElement != null) {
							text = textElement.outerHtml();
						} else {
							log.debug("<li>Text Element not found</li>");
						}
					} else {
						sb.append("<li>Text component not found on web publisher page</li>");
					}
					if (indexLowerLeftNode.hasNode("text")) {
						Node textNode = indexLowerLeftNode.getNode("text");
						if (StringUtils.isNotBlank(text)) {
							textNode.setProperty("text", text);
						} else {
							sb.append("<li>text doesn't exist</li>");
						}
					}
				} catch (Exception e) {
					sb.append("<li>Unable to update drawers_container component.</li>");
					log.error("Exception : ",e);
				}
				// end set text component content.
				// --------------------------------------------------------------------------------------------------------------------------
				// start of html blob components content.
				try {
					Elements htmlblobElements = doc.select("ul.n21");
					String html = "";
					if (htmlblobElements != null) {
						// Element htmlblobElement = htmlblobElements.first();
						for (Element htmlblobElement : htmlblobElements) {
							html = html + FrameworkUtils.extractHtmlBlobContent(htmlblobElement, "", locale, sb, urlMap);
						}
					} else {
						sb.append("<li>htmlblob component not found on web publisher page</li>");
					}
					if (indexLowerLeftNode.hasNode("htmlblob")) {
						Node htmlBlobNode = indexLowerLeftNode.getNode("htmlblob");
						log.debug("htmlblobElement.html() " + html);
						if (StringUtils.isNotBlank(html)) {
							htmlBlobNode.setProperty("html", html);
						} else {
							sb.append("<li>htmlblob content doesn't exist</li>");
						}
					}
					sb.append("<li>drawer component content is extracted from htmlblob element. So showtext and hidetext of drawer component should be migrated manually </li>");
				} catch (Exception e) {
					sb.append("<li>Unable to update html blob component.</li>");
					log.error("Exception : ",e);
				}
				// end set html blob component content.
				// --------------------------------------------------------------------------------------------------------------------------
				// start of tile bordered components.
				try {
					Elements rightRail = doc.select("div.c23-pilot,div.cc23-pilot");
					int rightcount = 0;
					boolean entry = true;
					if (rightRail != null) {
						log.debug("rightRail size" + rightRail.size());
						if (rightRail.size() != indexLowerRightNode.getNodes("tile_bordered*").getSize()) {
							sb.append("<li>Mis-Match in tilebordered Panels count." + rightRail.size() + " is not equal " + indexLowerRightNode.getNodes("tile_bordered*").getSize() + "</li>");
						}
						if (rightRail.size() > 0) {
							NodeIterator titleBorderNodes = indexLowerRightNode.getNodes("tile_bordered*");
							for (Element ele : rightRail) {
								Elements iconBlock = ele.select("div.icon-block");
								if (iconBlock != null && iconBlock.size() > 0) {
									continue;
								}
								Node rightRailNode = null;
								String title = ele.getElementsByTag("h2") != null ? ele.getElementsByTag("h2").text() : "";
								if (StringUtils.isBlank(title)) {
									title = ele.getElementsByTag("h3") != null ? ele.getElementsByTag("h3").text() : "";
								}
								String desc = ele.getElementsByTag("p") != null ? ele.getElementsByTag("p").text() : "";
								Elements anchor = ele.getElementsByTag("a");
								String anchorText = anchor != null ? anchor.text() : "";
								String anchorHref = anchor.attr("href");
								// Start extracting valid href
								log.debug("Before tileborderedLinkUrl" + anchorHref);
								anchorHref = FrameworkUtils.getLocaleReference(anchorHref, urlMap);
								log.debug("after tileborderedLinkUrl" + anchorHref);
								// End extracting valid href
								if (titleBorderNodes.hasNext()) {
									rightRailNode = (Node) titleBorderNodes.next();
								} else {
									sb.append("<li>all tile_boredered components are migrated</li>");
								}

								if (rightRailNode != null) {
									if (title != null && title != "" && desc != null && desc != "" && anchorText != null && anchorText != "") {
										rightRailNode.setProperty("title", title);
										rightRailNode.setProperty("description", desc);
										rightRailNode.setProperty("linktext", anchorText);
										rightRailNode.setProperty("linkurl", anchorHref);
										log.debug("title, description, linktext and linkurl are created at " + rightRailNode.getPath());
									} else {
										sb.append("<li>Content miss match for " 	+ ele.className() + "</li>");
									}
								} else {
									sb.append("<li>one of title_bordered node doesn't exist in node structure.</li>");
								}
							}
						} else {
							sb.append("<li>No Content with class 'c23-pilot or cc23-pilot' found</li>");
						}
					} else {
						sb.append("<li>tile bordered component not present in the web publisher page</li>");
					}
				} catch (Exception e) {
					log.debug("<li>Unable to update tile_bordered component.</li>");
				}
				// End of tile bordered components.
				// -----------------------------------------------------------------------------------------------------
				session.save();
			} else {
				sb.append(Constants.URL_CONNECTION_EXCEPTION);
			}
		} catch (Exception e) {
			log.debug("Exception : ", e);
		}
		sb.append("</ul></td>");
		return sb.toString();
	}
}
