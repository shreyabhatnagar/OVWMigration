package com.cisco.dse.global.migration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
import org.apache.sling.commons.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ProductLandingVariation2 {

	Document doc;

	String title = null;
	String fProductsTitle = null;

	Element fProduct = null;
	Elements fProdTitle = null;
	Elements fFooterLinks = null;
	Elements fSeries = null;
	Elements fProdSeries = null;
	Element fSubSeries = null;

	StringBuilder sb = new StringBuilder(1024);

	javax.jcr.Node hNode = null;
	javax.jcr.Node drawer = null;
	javax.jcr.Node subdrawer = null;
	javax.jcr.Node subdrawerpanel = null;

	String footerLinks = "{\"linktext\":\"<aaa>\",\"linkurl\":\"<bbb>\"}";

	
	static Logger log = Logger.getLogger(ProductLandingVariation2.class);

	public String translate(String loc, String prod, String type, String catType,
			String locale, Session session) throws IOException,
			ValueFormatException, VersionException, LockException,
			ConstraintViolationException, RepositoryException {
		BasicConfigurator.configure();
		log.debug("In the translate method");
		log.debug("In the translate method, catType is :"+ catType);

		// Repo node paths

		String layoutOverView = "/content/<locale>/"+catType+"/<prod>/index/jcr:content/content_parsys/overview_alt1/layout-overview-alt1";

		String pageUrl = "http://chard.cisco.com:4502/content/<locale>/"+catType+"/<prod>/index.html";
		
		pageUrl = pageUrl.replace("<locale>", locale).replace("<prod>", prod);
		
		sb.append("<td>" + "<a href="+pageUrl+">"+pageUrl+"</a>"+"</td>");
		sb.append("<td>" + "<a href="+loc+">"+loc +"</a>"+ "</td>");
		sb.append("<td><ul>");


		layoutOverView = layoutOverView.replace("<locale>", locale).replace("<prod>", prod);
		layoutOverView = layoutOverView.replace("<locale>", locale).replace("<prod>", prod);
		log.debug("layoutOverView : "+layoutOverView);
		javax.jcr.Node layoutOverViewNode = null;
		try {
			layoutOverViewNode = session.getNode(layoutOverView);
			try {
				doc = Jsoup.connect(loc).get();
			} catch (Exception e) {
				sb.append("<li>Cannot Connect to given URL. \n"+loc+"</li>");
			}

			title = doc.title();
			
			// ------------------------------------------------------------------------------------------------------------------------------------------
			// start hero panel.
			try {
				String h2Text = "";
				String pText = "";
				String aText = "";
				String aHref = "";
				String parbaseContent = "";
				Elements gridLeftElements = doc.select("div.gd-left");
				if(gridLeftElements != null){
					Elements textElements = gridLeftElements.select("div.c50-text");
					if(textElements != null){
						Element textElement = textElements.first();
						if(textElement != null){
							Elements h2TagElements = textElement.getElementsByTag("h2");
							if(h2TagElements != null){
								Element h2TagElement = h2TagElements.first();
								if(h2TagElement != null){
									h2Text = h2TagElement.text();
								}else{
									sb.append("<li>Hero Panel Heding element not having any title in it ('h2' is blank)</li>");
								}
							}else{
								sb.append("<li>Hero Panel Heading element not found ('h2' tag not found in 'div.c50-text' div element)</li>");
							}
							Elements pTagElements = textElement.getElementsByTag("p");
							if(pTagElements != null){
								Element pTagElement = pTagElements.first();
								if(pTagElement != null){
									pText = pTagElement.text();
								}else{
									sb.append("<li>Hero Panel Paragraph element is not having any paragraph in it ('p' is blank)</li>");
								}
							}else{
								sb.append("<li>Hero Panel Paragraph element not found ('p' tag not found in 'div.c50-text' div element)</li>");
							}
							Elements aTagElements = textElement.getElementsByTag("a");
							if(aTagElements != null){
								Element aTagElement = aTagElements.first();
								if(aTagElement != null){
									aText = aTagElement.text();
									aHref = aTagElement.attr("href");
								}else{
									sb.append("<li>No anchor tag found in 'div.c50-text' div element</li>");
								}
							}else{
								sb.append("<li>No anchor tags found in 'div.c50-text' div element</li>");
							}
							
						}else{
							sb.append("<li>Hero Panel text element not found ('div.c50-text' elements exists but size of the elements is zero)</li>");
						}
					}else{
						sb.append("<li>Hero Panel text elements not found ('div.c50-text' class not found in the document)</li>");
					}
					Elements parbaseTextElements = gridLeftElements.select("div.parbase");
					if(parbaseTextElements != null){
						parbaseTextElements = parbaseTextElements.select("div.c100-pilot");
						if(parbaseTextElements != null){
							Element parbaseTextElement = parbaseTextElements.first();
							if(parbaseTextElement != null){
								parbaseContent = parbaseTextElement.html();
							}else{
								sb.append("<li>No parbse text found.('div.parbase' element not found in 'div.gd-left')</li>");
							}
						}else{
							sb.append("<li>No parbse text found.('div.text parbase section' element not found in 'div.gd-left')</li>");
						}
					}else{
						sb.append("<li>No parbse text found.('div.text parbase section' element not found in 'div.gd-left')</li>");
					}
					
					
				}else{
					sb.append("<li>Hero panel not found. ('div.gd-left' class not found in the document)</li>");
				}
				
				
				
				
				
				
				
				Node gd12v2 = null;
				Node gd12v2_left = null;
				Node hero_large = null;
				Node heroPanelNode = null;
				if(layoutOverViewNode != null){
					if(layoutOverViewNode.hasNode("gd12v2")){
						gd12v2 = layoutOverViewNode.getNode("gd12v2");
						if(gd12v2.hasNode("gd12v2-left")){
							gd12v2_left = gd12v2.getNode("gd12v2-left");
							if(gd12v2_left.hasNode("hero_large")){
								hero_large = gd12v2_left.getNode("hero_large");
								NodeIterator heroPanelIterator = hero_large.getNodes("heropanel*");
								if(heroPanelIterator.hasNext()){
									heroPanelNode = (Node)heroPanelIterator.next();
									if(StringUtils.isNotBlank(h2Text)){
										heroPanelNode.setProperty("title", h2Text);
									}else{
										sb.append("<li>h2 Text is blank in hero panel.</li>");
									}
									if(StringUtils.isNotBlank(pText)){
										heroPanelNode.setProperty("description", pText);
									}else{
										sb.append("<li>p Text is blank in hero panel.</li>");
									}
									if(StringUtils.isNotBlank(aText)){
										heroPanelNode.setProperty("linktext", aText);
									}else{
										sb.append("<li>p a href text is blank in hero panel.</li>");
									}
									if(StringUtils.isNotBlank(aHref)){
										heroPanelNode.setProperty("linkurl", aHref);
									}else{
										sb.append("<li>p a href ulr is blank in hero panel.</li>");
									}
								}else{
									sb.append("<li>Node with name 'heropanel*' doesn't exist under "+hero_large.getPath()+"</li>");
								}
							}else{
								sb.append("<li>Node with name 'hero_large' doesn't exist under "+gd12v2_left.getPath()+"</li>");
							}
							if(gd12v2_left.hasNode("text")){
								Node textNode = gd12v2_left.getNode("text");
								if(StringUtils.isNotBlank(parbaseContent)){
								textNode.setProperty("text", parbaseContent);
								}else{
									sb.append("<li>parbase content in left rail is blank.</li>");
								}
							}else{
								sb.append("<li>text node doesn't exist under : "+gd12v2_left.getPath()+"</li>");
							}
						}else{
							sb.append("<li>Node with name 'gd12v2-left' doesn't exist under "+gd12v2.getPath()+"</li>");
						}
					}else{
						sb.append("<li>Node with name 'gd12v2' doesn't exist under "+layoutOverView+"</li>");
					}
				}else{
					sb.append("<li>Node doesn't exist with path : "+layoutOverView+"</li>");
				}
				
			} catch (Exception e) {
				log.error("Exception : ",e);
				sb.append("<li>Unable to update benefits text component."+e+"</li>");
			}

			// end of hero panel.
			// ---------------------------------------------------------------------------------------------------------------------------------------
			// start of primary CTA section.
			try {
				
				
				
				
				
				//-----------------------------------------------------------
				
				
				String h3Text = "";
				String pText = "";
				String aText = "";
				String aHref = "";
				String h2Content = "";
				String pilotAText = "";
				String pilotALink = "";
				List<String> list = new ArrayList<String>();
				Elements gridRightElements = doc.select("div.gd-right");
				if(gridRightElements != null){
					Elements primaryCTAElements = gridRightElements.select("div.c47-pilot");
					if(primaryCTAElements != null){
						Element primaryCTAElement = primaryCTAElements.first();
						if(primaryCTAElement != null){
							Elements h3TagElements = primaryCTAElement.getElementsByTag("h3");
							if(h3TagElements != null){
								Element h3TagElement = h3TagElements.first();
								if(h3TagElement != null){
									h3Text = h3TagElement.text();
								}else{
									sb.append("<li>Primary CTA Heding element not having any title in it ('h2' is blank)</li>");
								}
							}else{
								sb.append("<li>Primary CTA Heading element not found ('h2' tag not found in 'div.c50-text' div element)</li>");
							}
							Elements pTagElements = primaryCTAElement.getElementsByTag("p");
							if(pTagElements != null){
								Element pTagElement = pTagElements.first();
								if(pTagElement != null){
									pText = pTagElement.text();
								}else{
									sb.append("<li>Primary CTA Paragraph element is not having any paragraph in it ('p' is blank)</li>");
								}
							}else{
								sb.append("<li>Primary CTA Paragraph element not found ('p' tag not found in 'div.c50-text' div element)</li>");
							}
							Elements aTagElements = primaryCTAElement.getElementsByTag("a");
							if(aTagElements != null){
								Element aTagElement = aTagElements.first();
								if(aTagElement != null){
									aText = aTagElement.text();
									aHref = aTagElement.attr("href");
								}else{
									sb.append("<li>No anchor tag found in 'div. c47-pilot' div element</li>");
								}
							}else{
								sb.append("<li>No anchor tags found in 'div. c47-pilot' div element</li>");
							}
							
						}else{
							sb.append("<li>Hero Panel text element not found ('div.c50-text' elements exists but size of the elements is zero)</li>");
						}
					}else{
						sb.append("<li>Hero Panel text elements not found ('div.c50-text' class not found in the document)</li>");
					}
					Elements rightRailPilotElements = gridRightElements.select("div.s14-pilot");
					if(rightRailPilotElements != null){
						Element rightRailPilotElement = rightRailPilotElements.first();
						if(rightRailPilotElement != null){
							Elements h2Elements = rightRailPilotElement.getElementsByTag("h2");
							if(h2Elements != null){
								Element h2Element = h2Elements.first();
								h2Content = h2Element.text();
							}else{
								sb.append("<li>h2 of right rail with class 'div.s14-pilot' is blank.</li>");
							}
							Elements liElements = rightRailPilotElement.getElementsByTag("li");
							for(Element ele : liElements){
								JSONObject obj = new JSONObject();
								String icon = ele.attr("class");
								obj.put("icon", icon);
								Elements aElements = ele.getElementsByTag("a");
								if(aElements != null){
									Element aElement = aElements.first();
									String title = aElement.attr("title");
									String href = aElement.attr("href");
									obj.put("linktext", title);
									obj.put("linkurl", href);
								}else{
									sb.append("<li>No anchor tag found in the right rail social links</li>");
								}
								list.add(obj.toString());
							}
						}else{
							sb.append("<li>right rail with class 'div.s14-pilot' is blank.</li>");
						}
					}else{
						sb.append("<li>No pilot found on right rail with class 'div.s14-pilot'</li>");
					}
					
					
				}else{
					sb.append("<li>Hero panel not found. ('div.gd-left' class not found in the document)</li>");
				}
				
				
				
				
				
				
				Node gd12v2 = null;
				Node gd12v2_right = null;
				Node primary_cta_v2 = null;
				Node heroPanelNode = null;
				if(layoutOverViewNode != null){
					if(layoutOverViewNode.hasNode("gd12v2")){
						gd12v2 = layoutOverViewNode.getNode("gd12v2");
						if(gd12v2.hasNode("gd12v2-right")){
							gd12v2_right = gd12v2.getNode("gd12v2-right");
							if(gd12v2_right.hasNode("primary_cta_v2")){
								primary_cta_v2 = gd12v2_right.getNode("primary_cta_v2");
								if(StringUtils.isNotBlank(h3Text)){
									primary_cta_v2.setProperty("title", h3Text);
									log.debug(h3Text+"is set to the property title at : "+primary_cta_v2.getPath());
								}else{
									sb.append("<li>h3 text is blank in primary cta.</li>");
								}
								if(StringUtils.isNotBlank(pText)){
									primary_cta_v2.setProperty("description", pText);
									log.debug(pText+"is set to the property title at : "+primary_cta_v2.getPath());
								}else{
									sb.append("<li>p text is blank in primary cta.</li>");
								}
								if(StringUtils.isNotBlank(aText)){
									primary_cta_v2.setProperty("linktext", aText);
									log.debug(aText+"is set to the property title at : "+primary_cta_v2.getPath());
								}else{
									sb.append("<li>anchor text is blank in primary cta.</li>");
								}
								if(StringUtils.isNotBlank(aHref)){
									if(primary_cta_v2.hasNode("linkurl")){
										Node linkurlNode = primary_cta_v2.getNode("linkurl");
										linkurlNode.setProperty("url", aHref);
										log.debug(aHref+"is set to the property title at : "+primary_cta_v2.getPath());
									}else{
										sb.append("<li>linkurl node doesn't exists under : "+primary_cta_v2.getPath()+"</li>");
									}
								}else{
									sb.append("<li>anchor href is blank for primary cta.</li>");
								}
							}else{
								sb.append("<li>Node with name 'hero_large' doesn't exist under "+gd12v2_right.getPath()+"</li>");
							}
							if(gd12v2_right.hasNode("followus")){
								Node followus = gd12v2_right.getNode("followus");
								if(StringUtils.isNotBlank(h2Content)){
									followus.setProperty("title", h2Content);
								}else{
									sb.append("<li>No title found at right rail social media piolot.</li>");
								}
								
								if(list.size()>1){
									followus.setProperty("links",list.toArray(new String[list.size()]));
								}
								
								
							}else{
								sb.append("<li>No 'followus' node found under "+gd12v2_right.getPath()+"</li>");
							}
							
							
							
							
							
						}else{
							sb.append("<li>Node with name 'gd12v2-left' doesn't exist under "+gd12v2.getPath()+"</li>");
						}
					}else{
						sb.append("<li>Node with name 'gd12v2' doesn't exist under "+layoutOverView+"</li>");
					}
				}else{
					sb.append("<li>Node doesn't exist with path : "+layoutOverView+"</li>");
				}
				
				
				
				
				
				
				
				
				//-------------------------------------------------------------
				
				
			} catch (Exception e) {
				sb.append("<li>Unable to update benefits list component.\n</li>");
				log.error("Exceptoin : ",e);
			}
			// end of primary CTA Section.
			// --------------------------------------------------------------------------------------------------------------------------
			// start of benefit list right rail.

			try {

				String title = "";
				Elements gridElements = null;
				Elements titleGrids = doc.select("div.c100-pilot");
				Elements titleElements = null;
				if(titleGrids != null){
					titleElements = titleGrids.select("h2.bdr-1");
					
				}else{
					sb.append("<li>No title found center Grid with class 'bdr-1' </li>");
				}
				
				
				
				if(layoutOverViewNode != null){
					if(layoutOverViewNode.hasNode("gd11v1_0")){
						Node gd11v1_0 = layoutOverViewNode.getNode("gd11v1_0");
						if(gd11v1_0.hasNode("gd11v1-mid")){
							Node gd11v1_mid = gd11v1_0.getNode("gd11v1-mid");
							if(gd11v1_mid.hasNode("text")){
								Node textNode = gd11v1_mid.getNode("text");
								textNode.setProperty("text", titleElements.get(0).outerHtml());
							}else{
								sb.append("<li>gd11v1-mid Node doesn't exist with path : "+gd11v1_mid.getPath()+"</li>");
							}
						}else{
							sb.append("<li>gd11v1-mid Node doesn't exist with path : "+gd11v1_0.getPath()+"</li>");
						}
					}else{
						sb.append("<li>gd11v1_0 Node doesn't exist with path : "+layoutOverViewNode.getPath()+"</li>");
					}
					
					
				}else{
					sb.append("<li>Node doesn't exist with path : "+layoutOverView+"</li>");
				}
				
				
				
				if(layoutOverViewNode != null){
					if(layoutOverViewNode.hasNode("gd11v1")){
						Node gd11v1 = layoutOverViewNode.getNode("gd11v1");
						if(gd11v1.hasNode("gd11v1-mid")){
							Node gd11v1_mid = gd11v1.getNode("gd11v1-mid");
							if(gd11v1_mid.hasNode("text")){
								Node textNode = gd11v1_mid.getNode("text");
								textNode.setProperty("text", titleElements.get(1).outerHtml());
							}else{
								sb.append("<li>gd11v1-mid Node doesn't exist with path : "+gd11v1_mid.getPath()+"</li>");
							}
						}else{
							sb.append("<li>gd11v1-mid Node doesn't exist with path : "+gd11v1.getPath()+"</li>");
						}
					}else{
						sb.append("<li>gd11v1_0 Node doesn't exist with path : "+layoutOverViewNode.getPath()+"</li>");
					}
					
					
				}else{
					sb.append("<li>Node doesn't exist with path : "+layoutOverView+"</li>");
				}
				
				

				
				
				
				Elements grids = doc.select("div.gd14v1");
				if(grids != null){
					Element grid = grids.first();
					if(grid != null){
						gridElements = grid.select("div.c100-pilot");
						
					}else{
						sb.append("<li>Grids are emplty with class 'div.gd14v1' </li>");
					}
				}else{
					sb.append("<li>No grids found with class 'div.gd14v1' </li>");
				}
				

				
				
				
				int count = 0;
				if(layoutOverViewNode != null){
					if(layoutOverViewNode.hasNode("gd14v1")){
						Node gd14v1 = layoutOverViewNode.getNode("gd14v1");
						NodeIterator gd14v1Iterator = gd14v1.getNodes("gd14v1-*");
						if(gd14v1Iterator.getSize() == gridElements.size()){
							while(gd14v1Iterator.hasNext()){
								Node gridNode = (Node)gd14v1Iterator.next();
								if(gridNode.hasNode("text")){
									Node textNode = gridNode.getNode("text");
									textNode.setProperty("text", gridElements.get(count).html());
									count++;
								}else{
									sb.append("<li>text node doesn't exists under : "+gridNode.getPath()+"</li>");
								}
							}
						}else{
							sb.append("<li>Number of grid elements("+gridElements.size()+") and nodes("+gd14v1Iterator.getSize()+") are not equals</li>");
						}
												
						
						
					}else{
						sb.append("<li>gd14v1 Node doesn't exist with path : "+layoutOverViewNode.getPath()+"</li>");
					}
					
					
				}else{
					sb.append("<li>Node doesn't exist with path : "+layoutOverView+"</li>");
				}
				
				
				
				
				
				/*
				Elements grids1 = doc.select("div.gd13v2");
				if(grids1 != null){
					Element grid = grids1.first();
					if(grid != null){
						gridElements = grid.select("div.c100-pilot");
						
					}else{
						sb.append("<li>Grids are emplty with class 'div.gd14v1' </li>");
					}
				}else{
					sb.append("<li>No grids found with class 'div.gd14v1' </li>");
				}
				

				
				
				
				int count = 0;
				if(layoutOverViewNode != null){
					if(layoutOverViewNode.hasNode("gd14v1")){
						Node gd14v1 = layoutOverViewNode.getNode("gd14v1");
						NodeIterator gd14v1Iterator = gd14v1.getNodes("gd14v1-*");
						if(gd14v1Iterator.getSize() == gridElements.size()){
							while(gd14v1Iterator.hasNext()){
								Node gridNode = (Node)gd14v1Iterator.next();
								if(gridNode.hasNode("text")){
									Node textNode = gridNode.getNode("text");
									textNode.setProperty("text", gridElements.get(count).html());
									count++;
								}else{
									sb.append("<li>text node doesn't exists under : "+gridNode.getPath()+"</li>");
								}
							}
						}else{
							sb.append("<li>Number of grid elements("+gridElements.size()+") and nodes("+gd14v1Iterator.getSize()+") are not equals</li>");
						}
												
						
						
					}else{
						sb.append("<li>gd14v1 Node doesn't exist with path : "+layoutOverViewNode.getPath()+"</li>");
					}
					
					
				}else{
					sb.append("<li>Node doesn't exist with path : "+layoutOverView+"</li>");
				}
				
				
				*/
				
				
				
				
				
				
				
				
				
				
							} catch (Exception e) {
				sb.append("<li>Unable to update benefits tile_bordered component.\n</li>");
			}
			// End of benefit list right rail.
			// -----------------------------------------------------------------------------------------------------
			
			session.save();

		} catch (Exception e) {
			log.error("Exception : ",e);
			sb.append("<li>Exception "+e+"</li>");
		}
		
		sb.append("</ul></td>");
		
		return sb.toString();
	}
}
