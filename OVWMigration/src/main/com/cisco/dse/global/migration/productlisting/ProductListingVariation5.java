/* 
 * S.No		Name			Description of change
 * 1		vidya				Added the Java file to handle the migration of product listing variation 5 page.
 * 
 * */
package com.cisco.dse.global.migration.productlisting;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.sling.commons.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.cisco.dse.global.migration.config.Constants;
import com.cisco.dse.global.migration.config.FrameworkUtils;

public class ProductListingVariation5 {
	
	Document doc;

	String title = null;

	StringBuilder sb = new StringBuilder(1024);
	
	static Logger log = Logger.getLogger(ProductListingVariation5.class);

	public String translate(String host, String loc, String prod, String type, String catType,
			String locale, Session session) throws IOException,
			ValueFormatException, VersionException, LockException,
			ConstraintViolationException, RepositoryException {
		log.debug("In the translate method");
		log.debug("In the translate method, catType is :"+ catType);
		
		//Repo node paths
		String pagePropertiesPath = "/content/<locale>/"+catType+"/<prod>/product-listing/jcr:content";
		String topNodePath = "/content/<locale>/"+catType+"/<prod>/product-listing/jcr:content/content_parsys/products/layout-products/gd21v1/gd21v1-mid";
		String bottomNodePath = "/content/<locale>/"+catType+"/<prod>/product-listing/jcr:content/content_parsys/products/layout-products/gd21v1_0/gd21v1-mid";
        String pageUrl = host + "/content/<locale>/"+catType+"/<prod>/product-listing.html";
		
		pageUrl = pageUrl.replace("<locale>", locale).replace("<prod>", prod);
		pagePropertiesPath = pagePropertiesPath.replace("<locale>", locale).replace("<prod>", prod);
		
		sb.append("<td>" + "<a href="+pageUrl+">"+pageUrl+"</a>"+"</td>");
		sb.append("<td>" + "<a href="+loc+">"+loc +"</a>"+ "</td>");
		sb.append("<td><ul>");

		topNodePath = topNodePath.replace("<locale>", locale).replace("<prod>", prod);
		bottomNodePath = bottomNodePath.replace("<locale>", locale).replace("<prod>", prod);
		

		javax.jcr.Node topNode = null;
		javax.jcr.Node bottomNode = null;
		javax.jcr.Node pageJcrNode = null;
		
		try {
			topNode = session.getNode(topNodePath);
			bottomNode = session.getNode(bottomNodePath);
			pageJcrNode = session.getNode(pagePropertiesPath);
			try {
				doc = Jsoup.connect(loc).get();
			} catch (Exception e) {
				sb.append("<li>Cannot Connect to given URL. \n"+loc+"</li>");
			}

			title = doc.title();
			// ------------------------------------------------------------------------------------------------------------------------------------------
			// start set page properties.

			FrameworkUtils.setPageProperties(pageJcrNode, doc, session, sb);

			// end set page properties.
			// ------------------------------------------------------------------------------------------------------------------------------------------
			// start set text component.
						try {
							String h2Text="";
							String pText="";
							//get content 
							Elements textElements = doc.select("div.c00-pilot");
							if (textElements != null) {
									Element h2Elements=textElements.select("h2").first();
									if(h2Elements!=null){
										h2Text=h2Elements.outerHtml();
									}
									else{
										sb.append(Constants.TEXT_HAEDING_NOT_FOUND);
									}
									Elements pElements=textElements.select("p");
									if(pElements!=null){
										pText=pElements.outerHtml();
									}
									else{
										sb.append(Constants.TEXT_DESCRIPTION_NOT_FOUND);
									}
							}else{
								sb.append(Constants.TEXT_ELEMENT_NOT_FOUND);
								}
							
				          //set content
							Node h2TextNode = topNode.hasNode("text") ? topNode.getNode("text"): null;
							Node pTextNode = topNode.hasNode("text_0") ? topNode.getNode("text_0"): null;
							if(h2TextNode!=null){
								h2TextNode.setProperty("text", h2Text);
							}else{
								sb.append(Constants.TEXT_NODE_NOT_FOUND);
							}
							if(pTextNode!=null){
								pTextNode.setProperty("text", pText);
							}else{
								sb.append(Constants.TEXT_NODE_NOT_FOUND);
							}
							// end set text
						} catch (Exception e) {
							sb.append(Constants.EXCEPTION_TEXT_COMPONENT);
						}

						// end of text component
						//---------------------------------------------------------------------------------------------------------------------------
						// start set drawers_container component content.
						try {
							javax.jcr.Node drawersContainerNode = null;
							Elements drawerComponentHeaderElements = doc.select("div.c00-pilot");
							if (drawerComponentHeaderElements != null) {
								Element drawerComponentHeader = drawerComponentHeaderElements.last();
									if (drawerComponentHeader != null) {
										String drawerComponentHeaderTitle = "";
										Elements drawerComponentHeaderTitleElements = drawerComponentHeader.getElementsByTag("h2");
										if (drawerComponentHeaderTitleElements != null) {
											Element drawerComponentHeaderTitleElement = drawerComponentHeaderTitleElements.first();
											if (drawerComponentHeaderTitleElement != null) {
												drawerComponentHeaderTitle = drawerComponentHeaderTitleElement.text();
											} else {
												sb.append(Constants.DRAWER_CONTAINER_TITLE_ELEMENT_NOT_FOUND);
											}
										} else {
											sb.append(Constants.DRAWER_CONTAINER_TITLE_ELEMENT_NOT_FOUND);
										}
										if (bottomNode.hasNodes()) {
											NodeIterator drawersContainerIterator = bottomNode.getNodes("drawers_container*");
											
											if (drawersContainerIterator.hasNext()) {
												drawersContainerNode = drawersContainerIterator.nextNode();
												log.debug("drawerComponentHeaderTitle " + drawerComponentHeaderTitle + "\n");
												if (StringUtils.isNotBlank(drawerComponentHeaderTitle)) {
													drawersContainerNode.setProperty("title", drawerComponentHeaderTitle);
												} else {
													sb.append(Constants.DRAWER_CONTAINER_TITLE_ELEMENT_NOT_FOUND);
												}
												Elements hTextElements = doc.getElementsByAttribute(
														"data-config-hidetext");
												if (hTextElements != null && hTextElements.size() > 0) {
													Element hText = hTextElements.first();
													if (hText != null) {
														System.out
														.println("Hide TEXT:::::::::" + hText == null ? "NULL"
																: "NOT NULL"
																		+ hText.attr("data-config-hidetext"));
					
														System.out
																.println("Show TEXT:::::::::" + hText == null ? "NULL"
																		: "NOT NULL"
																				+ hText.attr("data-config-showtext"));
														drawersContainerNode.setProperty("closetext",
																hText.attr("data-config-hidetext"));
														drawersContainerNode.setProperty("opentext",
																hText.attr("data-config-showtext"));
													} else {
														log.debug("<li>data-config-hidetext not found</li>");
													}
													
												} else {
													sb.append(Constants.SHOW_HIDE_LINKS_NOT_FOUND);
												}
												if (drawersContainerNode != null && drawersContainerNode.hasNodes()) {
													NodeIterator drawerPanelsIterator = drawersContainerNode.getNodes("drawerspanel*");
													javax.jcr.Node drawersPanelNode = null;
													Elements drawersPanelElements = doc.select("div.n21");
													
													if (drawersPanelElements != null) {
														//Element drawersPanelElement = drawersPanelElements.first();
														// start new code
														int count = 0;
														for (Element drawersPanelElement : drawersPanelElements) {
															Elements drawerPanelLiElements = drawersPanelElement.getElementsByTag("li");
															if (drawerPanelLiElements != null) {
																
																log.debug("li elements size" + drawerPanelLiElements.size());
																
																
																for (Element drawerPanelLiElement : drawerPanelLiElements) {
																	boolean misMatchFlag = true;
																	
																	Elements iconBlock = drawerPanelLiElement.select("div.series");
																	if (iconBlock.size() == 0) {
																		log.debug("SERIES SIZE0");
																		continue;
																	}
																	log.debug("SERIES SIZE NOTTTTTTTTTTTTT 0");
																	count = count + 1;
																	if (drawerPanelsIterator.hasNext()) {
																		drawersPanelNode = drawerPanelsIterator.nextNode();
																	}
																	Elements seriesElements = drawerPanelLiElement.select("div.series");
																	if (seriesElements != null) {
																		Element seriesElement = seriesElements.first();
																		String panelTitle = "";
																		String panelDescription = "";
																		if (seriesElement != null) {
																			Elements panelTitleElements = seriesElement.getElementsByTag("h3");
																			if (panelTitleElements != null) {
																						panelTitle = panelTitleElements.first().text();
																						log.debug("panel title" + panelTitle);
																			} else {
																				sb.append(Constants.DRAWER_PANEL_TITLE_ELEMENT_NOT_FOUND);
																			}
																			Elements panelParaElements = seriesElement.getElementsByTag("p");
																			if (panelParaElements != null) {
																				Element panelDescriptionElement = panelParaElements.first();
																				panelDescription = panelDescriptionElement.text();
																			} else {
																				sb.append(Constants.DRAWER_PANEL_DESCRIPTION_ELEMENT_NOT_FOUND);
																			}
																			if (drawersPanelNode != null) {
																				log.debug("panelTitle " + panelTitle + "\n");
																				//log.debug("linkUrl " + linkUrl + "\n");
																				log.debug("panelDescription " + panelDescription + "\n");
																				if (StringUtils.isNotBlank(panelTitle)) {
																					drawersPanelNode.setProperty("title", panelTitle);
																				} else {
																					sb.append(Constants.DRAWER_PANEL_TITLE_ELEMENT_NOT_FOUND);
																				}
				
																				if (StringUtils.isNotBlank(panelDescription)) {
																					drawersPanelNode.setProperty("description", panelDescription);
																				} else {
																					sb.append(Constants.DRAWER_PANEL_DESCRIPTION_ELEMENT_NOT_FOUND);
																				}
																			}
																		}
																	}
																	
																	// selecting sub drawer elements from document
																	NodeIterator subDrawerIterator = drawersPanelNode.getNode("parsys-drawers")
																			.getNodes("subdrawer_product*");
																	javax.jcr.Node subdrawerpanel = null;
																	Elements subDrawerColl = drawerPanelLiElement.select("ul.items");
																	Elements clearfixdivs = drawerPanelLiElement.select("li.clearfix");
																	
																	for (Element ss : subDrawerColl) {
																		
																		
																		String title = "";
																		String linkTitleUrl = "";
																		Elements subItems = ss.select("div.prodinfo");
																		
																		if (subItems != null) {
																			
//																			Element subItem = subItems.first();
																			for (Element subItem : subItems) {
																				if ((clearfixdivs.size() != subDrawerIterator.getSize())) {
																					misMatchFlag = false;
																				}
																				List<String> list1 = new ArrayList<String>();
																				List<String> list2 = new ArrayList<String>();
																				if (subDrawerIterator.hasNext()) {
																					subdrawerpanel = subDrawerIterator.nextNode();
																				}
																				if (subItem != null) {
																					
																					Elements siTitles = subItem.getElementsByTag("h4");
																					if (siTitles != null) {
																						Element siTitle = siTitles.first();
																						if (siTitle != null) {
																							Elements siATitles = siTitle.getElementsByTag("a");
																							if (siATitles != null) {
																								Element siATitle = siATitles.first();
																								if (siATitle != null) {
																									log.debug("Sub Series Title:::::::::::::"
																											+ siATitle.text());
																									log.debug("siATitle.text() " + siATitle.text() + "\n");
																									log.debug("siATitle.text() " + siATitle.attr("href") + "\n");
																									title = siATitle.text();
																									linkTitleUrl = siATitle.attr("href");
																								} else {
																									sb.append(Constants.SUB_DRAWER_TITLE_ELEMENT_LINK_NOT_FOUND);
																								}
																							} else {
																								sb.append(Constants.SUB_DRAWER_TITLE_ELEMENT_LINK_NOT_FOUND);
																							}
																						} else {
																							sb.append(Constants.SUB_DRAWER_TITLE_ELEMENT_NOT_FOUND);
																						}
																						
																					} else {
																						sb.append(Constants.SUB_DRAWER_TITLE_ELEMENT_NOT_FOUND);
																					}
																					
																					Elements indDetailsElements = subItem.select("ul.details");
																					
																					if (indDetailsElements != null) {
																						Element indDetailsElement = indDetailsElements.first();
																						if (indDetailsElement != null) {
																							Elements indItems = indDetailsElement
																									.getElementsByTag("li");
																							if (indItems != null) {
																								for (Element indItem : indItems) {
																									JSONObject jsonObj = new JSONObject();
																									jsonObj.put("linktext", indItem.html());
																									list1.add( jsonObj.toString());
																								}
																							} else {
																								sb.append(Constants.LI_ELEMENTS_IN_DETAILS_ELEMENT_NOT_FOUND);
																							}
																						} else {
																							sb.append(Constants.DETAILS_ELEMEMT_NOT_FOUND);
																						}
																					} else {
																						sb.append(Constants.DETAILS_ELEMEMT_NOT_FOUND);
																					}

																					Element subItemUlInfoLink = subItem.siblingElements().first(); //subItemUlInfoLinks.first();
																					log.debug("Info Links Elements -----------"+subItemUlInfoLink);	
																					log.debug("--------------------------------");
																					if (subItemUlInfoLink != null) {
																							Elements subItemInfoLinks = subItemUlInfoLink.getElementsByTag("li");
																							
																							for (Element si : subItemInfoLinks) {
																								JSONObject jsonObj = new JSONObject();
																								System.out
																										.println("\t\t FeatureSubInfoLinks Text :::::::::::::::"
																												+ si.text());
																								
																								String linkText = "";
																								String linkTextUrl = "";
																								Elements linkTextElements = si.getElementsByTag("a");
																								if (linkTextElements != null) {
																									Element linkTextElement = linkTextElements.first();
																									if (linkTextElement != null) {
																										linkText = linkTextElement.text();
																										linkTextUrl = linkTextElement.attr("href");
																									} else {
																										sb.append(Constants.INFO_LINK_ANCHOR_ELEMENT_NOT_FOUND);
																									}
																								} else {
																									sb.append(Constants.INFO_LINK_ANCHOR_ELEMENT_NOT_FOUND);
																								}
																								if (StringUtils.isNotBlank(linkText)) {
																									jsonObj.put("linktext", linkText);
																								}
																								if (StringUtils.isNotBlank(linkTextUrl)) {
																									jsonObj.put("linkurl", linkTextUrl);
																								}
																								list2.add(jsonObj.toString());
																								System.out
																								.println("\t\t FeatureSubInfoLinks json Text :::::::::::::::"
																										+ jsonObj.toString());
																							}
																							log.debug("list2.size()" + list2.size());
																							
																						}
																					
																				
																				}
																				if (subdrawerpanel != null) {
																					log.debug("updating sub drawer*****" + subdrawerpanel.getPath() + "at" + drawersPanelNode.getPath());
																					if (StringUtils.isNotBlank(title)) {
																						subdrawerpanel
																						.setProperty("title", title);
																					} else {
																						sb.append(Constants.SUB_DRAWER_TITLE_ELEMENT_NOT_FOUND);
																					}
																					if (StringUtils.isNotBlank(linkTitleUrl)) {
																						subdrawerpanel.setProperty("linkurl", linkTitleUrl);
																					} else {
																						sb.append(Constants.LINK_URL_OF_SUB_DRAWER_NOT_FOUND);
																						log.debug("linkurl property is not set at " + subdrawerpanel.getPath());
																					}
																					if (list1.size() > 0) {
																						
																						if (subdrawerpanel.hasProperty("highlights")) {
																							Property p = subdrawerpanel.getProperty("highlights");
																							p.remove();
																							session.save();
																						}
																						subdrawerpanel.setProperty("highlights",
																								list1.toArray(new String[list1.size()]));
																					} else {
																						sb.append(Constants.HIGHLIGHTS_OF_SUB_DRAWER_NOT_FOUND);
																					}
																					if (list2.size() > 0) {
																						if (subdrawerpanel.hasProperty("infolinks")) {
																							Property p = subdrawerpanel.getProperty("infolinks");
																							p.remove();
																							session.save();
																						}
																						subdrawerpanel.setProperty("infolinks",
																								list2.toArray(new String[list2.size()]));
																					} else {
																						sb.append(Constants.INFO_LINKS_OF_SUB_DRAWER_NOT_FOUND);
																					}
																				}else{
																					misMatchFlag = false;
																				}
																			}
																			
																		}
																		
																		
																	}
																	if (!misMatchFlag) {
																		sb.append(Constants.MIS_MATCH_IN_SUB_DRAWER_PANEL_COUNT);
																	}
																}
																
																
															}
														}
														log.debug("countttttttt" + count);
														log.debug("iterator size" + drawerPanelsIterator.getSize());
														if (count != drawerPanelsIterator.getSize())
															sb.append(Constants.MIS_MATCH_IN_DRAWER_PANEL_COUNT);
														
														//end new code
													
												} else {
													sb.append(Constants.DRAWER_PANEL_ELEMENTS_NOT_FOUND);
												}
												}else {
													sb.append(Constants.DRAWER_CONTAINER_NODE_NOT_FOUND);
												}
											}
										} else {
											sb.append(Constants.DRAWER_CONTAINER_NODE_NOT_FOUND);
										}
								} else {
									sb.append(Constants.DRAWER_CONTAINER_TITLE_ELEMENT_NOT_FOUND);
								}
							}
						} catch (Exception e) {
							sb.append(Constants.UNABLE_TO_MIGRATE_DRAWER_COMPONENT);
						}

						// end set drawers_container component content.
						// --------------------------------------------------------------------------------------------------------------------------

					session.save();
					} catch (Exception e) {
						sb.append(Constants.URL_CONNECTION_EXCEPTION);
						log.debug("Exception as url cannot be connected: "+ e);
					}

					sb.append("</ul></td>");

					return sb.toString();
				}
			}