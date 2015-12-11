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
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.sling.commons.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.cisco.dse.global.migration.config.Constants;
import com.cisco.dse.global.migration.config.BaseAction;

public class ProductListingVariation6 extends BaseAction {

	Document doc;

	String title = null;

	StringBuilder sb = new StringBuilder(1024);

	static Logger log = Logger.getLogger(ProductListingVariation6.class);

	public String translate(String host, String loc, String prod, String type, String catType,
			String locale, Session session) throws IOException,
			ValueFormatException, VersionException, LockException,
			ConstraintViolationException, RepositoryException {
		BasicConfigurator.configure();
		// Repo node paths
		String pagePropertiesPath = "/content/<locale>/"+catType+"/<prod>/product-listing/jcr:content/";
		String pageUrl = host + "/content/<locale>/"+catType+"/<prod>/product-listing.html";

		pageUrl = pageUrl.replace("<locale>", locale).replace("<prod>", prod);
		pagePropertiesPath = pagePropertiesPath.replace("<locale>", locale).replace("<prod>", prod);
		String productListMid=pagePropertiesPath+"content_parsys/products/layout-products/gd21v1/gd21v1-mid";

		log.debug("Path is "+productListMid);
		sb.append("<td>" + "<a href="+pageUrl+">"+pageUrl+"</a>"+"</td>");
		sb.append("<td>" + "<a href="+loc+">"+loc +"</a>"+ "</td>");
		sb.append("<td><ul>");

		productListMid = productListMid.replace("<locale>", locale).replace("<prod>", prod);
		javax.jcr.Node productListMidNode = null;

		try {
			productListMidNode = session.getNode(productListMid);
			try {
				doc = getConnection(loc);
			} catch (Exception e) {
				sb.append(Constants.URL_CONNECTION_EXCEPTION);
			}

			//start of title text
			try{
				Element titleEle = doc.select("div.c00-pilot").first();
				if(titleEle != null){
					Element title = titleEle.getElementsByTag("h2").first();
					if(title==null){
						title = titleEle.getElementsByTag("h1").first();
					}
					Node titleNode = productListMidNode.hasNode("text")?productListMidNode.getNode("text"):null;
					if(titleNode==null){
						titleNode = productListMidNode.getNode("text_0");
					}
					if(titleNode != null){
						titleNode.setProperty("text", title.outerHtml());
					}
					else{
						sb.append(Constants.TEXT_NODE_NOT_FOUND);
					}
				}	
				else{
					sb.append(Constants.TEXT_HAEDING_NOT_FOUND);
				}
			}catch(Exception e){
				log.debug("Exception in title" +e);
			}
			//End of title text	
			// start set drawers_container component content.

			try{
				if (productListMidNode.hasNodes()) {
					NodeIterator drawersContainerIterator = productListMidNode.hasNode("drawers_container")?productListMidNode.getNodes("drawers_container"):productListMidNode.getNodes("drawers_container_0");
					if (drawersContainerIterator.hasNext()) {
						Node drawersContainerNode = drawersContainerIterator.nextNode();
						try{ 
						String includeheaderValue = drawersContainerNode.getProperty("includeheader").getValue().getString();
						log.debug("Include Header Value : "+includeheaderValue);
							if(includeheaderValue.equals("true")){
								sb.append(Constants.TITLE_ABOVE_DRAWERS_NOT_FOUND);
							}
						}catch(Exception e){
							log.debug("No INCLUDEHEADER PROPERTY found.");
						}
						Elements hTextElements = doc.getElementsByAttribute(
								"data-config-hidetext");
						if (hTextElements != null && hTextElements.size() > 0) {
							Element hText = hTextElements.first();
							if (hText != null) {
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
							Elements drawersPanelElements = doc.select("div.n21,ul.n21");
							if (drawersPanelElements != null && !drawersPanelElements.isEmpty()) {
								//Element drawersPanelElement = drawersPanelElements.first();
								// start new code
								int count = 0;
								for (Element drawersPanelElement : drawersPanelElements) {
									Elements drawerPanelLiElements = drawersPanelElement.getElementsByTag("li");
									if (drawerPanelLiElements != null) {
										for (Element drawerPanelLiElement : drawerPanelLiElements) {
											boolean misMatchFlag = true;
											Elements iconBlock = drawerPanelLiElement.select("div.series");
											if (iconBlock.size() == 0) {
												log.debug("SERIES SIZE0");
												continue;
											}
											count = count + 1;
											if (drawerPanelsIterator.hasNext()) {
												drawersPanelNode = drawerPanelsIterator.nextNode();
											}
											Elements seriesElements = drawerPanelLiElement.select("div.series");
											if (seriesElements != null) {
												Element seriesElement = seriesElements.first();
												String panelTitle = "";
												String linkUrl = "";
												String panelDescription = "";
												if (seriesElement != null) {
													Elements panelTitleElements = seriesElement.getElementsByTag("h3");
													if (panelTitleElements != null) {
														Element panelTitleElement = panelTitleElements.first(); 
														if (panelTitleElement != null){
															Elements anchorTag = panelTitleElement.getElementsByTag("a");
															if (anchorTag.size() > 0) {
																panelTitle = anchorTag.first().text();
																linkUrl = anchorTag.first().attr("href");
															} 
															if (StringUtils.isBlank(panelTitle)) {
																panelTitle = panelTitleElement.text();
															}
														} else {
															log.debug("<li>drawer panel anchor element not found</li>");
														}
													} else {
														log.debug("<li>drawer panel title element not found</li>");
													}
													Elements panelParaElements = seriesElement.getElementsByTag("p");
													if (panelParaElements != null) {
														Element panelDescriptionElement = panelParaElements.first();
														panelDescription = panelDescriptionElement.text();
													} else {
														log.debug("<li>drawer panel para element not found</li>");
													}
													if (drawersPanelNode != null) {
														if (StringUtils.isNotBlank(panelTitle)) {
															drawersPanelNode.setProperty("title", panelTitle);
														} else {
															sb.append(Constants.DRAWER_PANEL_TITLE_NOT_FOUND);
														}
														if (StringUtils.isNotBlank(linkUrl)) {
															drawersPanelNode.setProperty("linkurl", linkUrl);
														} else {
															sb.append(Constants.DRAWER_PANEL_LINK_TITLE_NOT_FOUND);
														}
														if (StringUtils.isNotBlank(panelDescription)) {
															drawersPanelNode.setProperty("description", panelDescription);
														} else {
															sb.append(Constants.DRAWER_PANEL_DESC_NOT_FOUND);
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

												/*	if (subDrawerIterator.hasNext()) {
																subdrawerpanel = subDrawerIterator.nextNode();
															} */
												Elements subItems = ss.select("div.prodinfo");
												Elements subItemUlInfoLinks = ss.select("ul.infolinks");

												if (subItems != null) {
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
																			title = siATitle.text();
																			linkTitleUrl = siATitle.attr("href");
																		} else {
																			log.debug("<li>sub series title Element anchor not found</li>");
																		}
																	} else {
																		log.debug("<li>sub series title Element anchor Section not found</li>");
																	}
																	Element spanTag = siTitle.getElementsByTag("span").first();
																	if(spanTag != null){
																		String spanTagText = spanTag.outerHtml();
																		title = title+" "+spanTagText;
																	}
																} else {
																	log.debug("<li>sub series title Element not found</li>");
																}

															} else {
																log.debug("<li>sub series title Elements section not found</li>");
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
																		log.debug("<li>li elements in details Element not found</li>");
																	}
																} else {
																	log.debug("<li>details Element not found</li>");
																}
															} else {
																log.debug("<li>details Element section not found</li>");
															}

															Element subItemUlInfoLink = subItem.siblingElements().first(); //subItemUlInfoLinks.first();
															if (subItemUlInfoLink != null) {
																Elements subItemInfoLinks = subItemUlInfoLink.getElementsByTag("li");
																for (Element si : subItemInfoLinks) {
																	JSONObject jsonObj = new JSONObject();

																	String linkText = "";
																	String linkTextUrl = "";
																	Elements linkTextElements = si.getElementsByTag("a");
																	if (linkTextElements != null) {
																		Element linkTextElement = linkTextElements.first();
																		if (linkTextElement != null) {
																			linkText = linkTextElement.text();
																			linkTextUrl = linkTextElement.attr("href");
																		} else {
																			log.debug("<li>info links anchor element not found</li>");
																		}
																	} else {
																		log.debug("<li>info links anchor element section not found</li>");
																	}
																	String liOwn = si.ownText();
																	if(liOwn!=null && !liOwn.equals("")){
																		linkText = linkText+" "+liOwn;
																	}
																	if (StringUtils.isNotBlank(linkText)) {
																		jsonObj.put("linktext", linkText);
																	}
																	if (StringUtils.isNotBlank(linkTextUrl)) {
																		jsonObj.put("linkurl", linkTextUrl);
																	}
																	list2.add(jsonObj.toString());
																}

															}


														}
														if (subdrawerpanel != null) {
															if (StringUtils.isNotBlank(title)) {
																subdrawerpanel
																.setProperty("title", title);
															} else {
																sb.append(Constants.TITLE_SUB_DRAWER_NOT_FOUND);
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
								if (count != drawerPanelsIterator.getSize())
									sb.append(Constants.MIS_MATCH_IN_DRAWER_PANEL_COUNT);
								//end new code

							} else {
								log.debug("<li>drawer panel elements section not found</li>");
							}
						}else {
							log.debug("<li>drawers_container node is not found</li>");
						}
					}
				} 
			} catch (Exception e) {
				log.debug("<li>Unable to update drawers_container component."+e+"</li>");
			}
			// End  set drawers_container component content.
			session.save();
		} catch (Exception e) {
			log.debug("Exception ", e);
		}
		sb.append("</ul></td>");
		return sb.toString();
	}	
}