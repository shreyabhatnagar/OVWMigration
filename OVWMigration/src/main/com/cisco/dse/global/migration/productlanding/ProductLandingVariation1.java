package com.cisco.dse.global.migration.productlanding;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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

public class ProductLandingVariation1 extends BaseAction {

	Document doc;
	String title = null;
	StringBuilder sb = new StringBuilder(1024);
	static Logger log = Logger.getLogger(ProductLandingVariation1.class);

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

		log.debug("In the translate method to migarate " + loc + " to " + pageUrl);

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
					log.debug("Started translating CTA content using class 'c47-pilot'.");
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
									log.debug("h3 element first is blank with in the class 'c47-pilot'");
								}
							} else {
								sb.append("<li>Primary CTA Heading element section not found </li>");
								log.debug("No h3 tag found with in the class 'c47-pilot'");
							}
							Elements paraElements = primaryCTAElement.getElementsByTag("p");
							if (paraElements != null) {
								Element paraElement = paraElements.first();
								if (paraElement != null) {
									primaryCTADescription = paraElement.text();
								} else {
									sb.append("<li>Primary CTA Para element not having title</li>");
									log.debug("p tags first element is blank with in the class 'c47-pilot'");
								}
							} else {
								sb.append("<li>Primary CTA Para element section not found </li>");
								log.debug("p tag not found with in the class 'c47-pilot'");
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
												primaryCTALinkUrl = anchorElement.absUrl("href");
												if (StringUtils.isBlank(primaryCTALinkUrl)) {
													primaryCTALinkUrl = anchorElement.attr("href");
												}
												log.debug("primaryCTALinkUrl before migration : " + primaryCTALinkUrl);
												primaryCTALinkUrl = FrameworkUtils.getLocaleReference(primaryCTALinkUrl, urlMap, locale, sb, catType, type);
												log.debug("primaryCTALinkUrl after migration : " + primaryCTALinkUrl);
											} else {
												sb.append("<li>Primary CTA Link anchor tag not found </li>");
												log.debug("anchor element first if blank with in the class 'cta' of 'cta-links'");
											}
										} else {
											sb.append("<li>Primary CTA Link anchor tag section not found </li>");
											log.debug("No anchor element found with the class 'cta' of 'cta-links'");
										}
									} else {
										sb.append("<li>Primary CTA Link not found </li>");
										log.debug("element with class 'cta' of li tags first element is blank.");
									}
								} else {
									sb.append("<li>Primary CTA Links not found </li>");
									log.debug("No element found with in the class 'cta' of li tag.");
								}
							} else {
								sb.append("<li>Primary CTA Links section not found </li>");
								log.debug("No element found with in the class 'cta-links' of ul tag.");
							}
							log.debug("primaryCTATitle : " + primaryCTATitle);
							log.debug("primaryCTADescription : " + primaryCTADescription);
							log.debug("primaryCTALinkText : " + primaryCTALinkText);
							log.debug("primaryCTALinkUrl : " + primaryCTALinkUrl);
							if (indexUpperLeftNode.hasNode("primary_cta_v2")) {
								Node primartCTANode = indexUpperLeftNode.getNode("primary_cta_v2");
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
									log.debug("linktext property is not set at " + primartCTANode.getPath());
								}

								if (primartCTANode.hasNode("linkurl")) {
									Node primartCTALinkUrlNode = primartCTANode.getNode("linkurl");
									if (StringUtils.isNotBlank(primaryCTALinkUrl)) {
										primartCTALinkUrlNode.setProperty("url", primaryCTALinkUrl);
									} else {
										log.debug("url property is not set at " + primartCTALinkUrlNode.getPath());
									}
								} else {
									sb.append("<li>primary_cta_v2 node is not having linkurl node</li>");
									log.debug("primary_cta_v2 node is not having linkurl node.");
								}
							} else {
								sb.append("<li>primary_cta_v2 node doesn't exists</li>");
								log.debug("primary_cta_v2 node doesn't exists");
							}
						} else {
							sb.append("<li>CTA element not found.</li>");
							log.debug("CTA element not found.");
						}
					} else {
						sb.append("<li>Primary CTA component is not there in web publisher page.</li>");
						log.debug("Primary CTA component doesn't exists in web publisher page.");
					}
				} catch (Exception e) {
					log.error("Exception : ", e);
				}
				// end set primary CTA content.
				// ------------------------------------------------------------------------------------------------------------------------------------------
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
						log.debug("Node with name 'hero_large' doesn't exist under " + indexUpperRightNode.getPath());
					}

					Elements heroLargeElements = doc.select("div.c50-pilot");
					if (heroLargeElements != null) {
						Element heroLargeElement = heroLargeElements.first();
						if (heroLargeElement != null) {
							Elements heroLargeFrameElements = heroLargeElement.select("div.frame");
							Node heroPanelNode = null;
							if (heroLargeFrameElements != null) {
								if (heroLargeFrameElements.size() != heroLargeNode.getNodes("heropanel*").getSize()) {
									sb.append("<li>Mismatch in the count of slides in the hero component </li>");
								}
								int i = 0;
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
											log.debug("No h2 first element found with in the class 'frame' of div.");
										}
									} else {
										sb.append("<li>Hero Panel title element is not found</li>");
										log.debug("No h2 found with in the class 'frame' of div.");
									}
									Elements heroDescriptionElements = ele.getElementsByTag("p");
									if (heroDescriptionElements != null) {
										Element heroDescriptionElement = heroDescriptionElements.first();
										if (heroDescriptionElement != null) {
											heroPanelDescription = heroDescriptionElement.text();
										} else {
											sb.append("<li>Hero Panel element not having any description in it </li>");
											log.debug("No p frist element found with in the class 'frame' of div.");
										}
									} else {
										sb.append("<li>Hero Panel para element not found </li>");
										log.debug("No p elemtn found with in the class 'frame' of div.");
									}
									Elements heroPanelLinkTextElements = ele.getElementsByTag("b");
									if (heroPanelLinkTextElements != null) {
										Element heroPanelLinkTextElement = heroPanelLinkTextElements.first();
										if (heroPanelLinkTextElement != null) {
											heroPanelLinkText = heroPanelLinkTextElement.text();
										} else {
											sb.append("<li>Hero Panel element not having any linktext in it </li>");
											log.debug("No b tags first elemtn found with in the class 'frame' of div.");
										}
									} else {
										sb.append("<li>Hero Panel linktext element not found  </li>");
										log.debug("No b tag found with the class 'frame' of div.");
									}
									Elements heroPanelLinkUrlElements = ele.getElementsByTag("a");
									if (heroPanelLinkUrlElements != null) {
										Element heroPanelLinkUrlElement = heroPanelLinkUrlElements.first();
										if (heroPanelLinkUrlElement != null) {
											heroPanellinkUrl = heroPanelLinkUrlElement.attr("href");
											// Start extracting valid href
											log.debug("heroPanellinkUrl before migration : " + heroPanellinkUrl);
											heroPanellinkUrl = FrameworkUtils.getLocaleReference(heroPanellinkUrl, urlMap, locale, sb, catType, type);
											log.debug("heroPanellinkUrl after migration : " + heroPanellinkUrl);
											// End extracting valid href
										} else {
											sb.append("<li>Hero Panel element not having any linkurl in it </li>");
											log.debug("No anchor first element found with in the class 'frame' of div.");
										}
									} else {
										sb.append("<li>Hero Panel link url element not found </li>");
										log.debug("No anchor element found with in the class 'frame' of div.");
									}
									// start image
									String heroImage = FrameworkUtils.extractImagePath(ele, sb);
									log.debug("heroImage path : " + heroImage);
									// end image
									log.debug("heroPanelTitle : " + heroPanelTitle);
									log.debug("heroPanelDescription : " + heroPanelDescription);
									log.debug("heroPanelLinkText : " + heroPanelLinkText);
									log.debug("heroPanellinkUrl : " + heroPanellinkUrl);

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
										log.debug("No list panelProperties found for the hero compoent order.");
									}
									
									int imageSrcEmptyCount = 0;
									if (heroPanelNode != null) {
										//start of hero pop up 
										Node heroPanelPopUpNode = null;
										Element lightBoxElement = null;
										Elements lightBoxElements = ele.select("div.c50-image").select("a.c26v4-lightbox");
										if (lightBoxElements != null && !lightBoxElements.isEmpty()) {
											  lightBoxElement = lightBoxElements.first();
										}
										heroPanelPopUpNode = FrameworkUtils.getHeroPopUpNode(heroPanelNode);
										if (heroPanelPopUpNode == null && lightBoxElement != null) {
											sb.append("<li>video pop up is present in WEB page but it is not present in WEM page.</li>");
										}
										if (heroPanelPopUpNode != null && lightBoxElement == null) {
											sb.append("<li>video pop up is present in WEM page but it is not present in WEB page.</li>");
										}
										if (heroPanelPopUpNode != null && lightBoxElement != null && StringUtils.isNotBlank(heroPanelTitle))  {
												heroPanelPopUpNode.setProperty("popupHeader", heroPanelTitle);
										} 
										//end of hero pop up
										if (StringUtils.isNotBlank(heroPanelTitle)) {
											heroPanelNode.setProperty("title", heroPanelTitle);
										} else {
											sb.append("<li>title of hero slide doesn't exist</li>");
											log.debug("Title is blank with in the 'frame' class of div.");
										}
										if (StringUtils.isNotBlank(heroPanelDescription)) {
											heroPanelNode.setProperty("description", heroPanelDescription);
										} else {
											sb.append("<li>description of hero slide doesn't exist</li>");
											log.debug("Description is blank with in the 'frame' class of the div.");
										}
										if (StringUtils.isNotBlank(heroPanelLinkText)) {
											heroPanelNode.setProperty("linktext", heroPanelLinkText);
										} else {
											sb.append("<li>link text of hero slide doesn't exist</li>");
											log.debug("Link Text doesn't exists with in the class 'frame' of the div.");
										}
										if (StringUtils.isNotBlank(heroPanellinkUrl)) {
											heroPanelNode.setProperty("linkurl", heroPanellinkUrl);
										} else {
											sb.append("<li>link url of hero slide doesn't exist / found video as link url for the slide on web publisher page </li>");
											log.debug("Link url doesn't exists with in the class 'frame' of the div.");
										}
										
										if (heroPanelNode.hasNode("image")) {
											Node imageNode = heroPanelNode.getNode("image");
											String fileReference = imageNode.hasProperty("fileReference") ? imageNode.getProperty("fileReference").getString() : "";
											heroImage = FrameworkUtils.migrateDAMContent(heroImage, fileReference, locale, sb, catType, type);
											log.debug("heroImage : " + heroImage);
											if (StringUtils.isNotBlank(heroImage)) {
												imageNode.setProperty("fileReference", heroImage);
											}else{
												imageSrcEmptyCount++;
												
											}
										} else {
											sb.append("<li>hero image node doesn't exist</li>");
											log.debug("'image' node doesn't exists in " + heroPanelNode.getPath());
										}
										if(imageSrcEmptyCount > 0){
											sb.append("<li> "+imageSrcEmptyCount+" image(s) are not found on locale page for hero panel </li>");
										}
								}else{
									sb.append("<li>hero node doesn't exist</li>");
								}
							}
							} else {
								log.debug("<li>Hero Large Frames/Panel Elements is not found</li>");
								log.debug("No div found with class 'frame'");
							}
						} else {
							sb.append("<li>Hero Large Element is not found</li>");
							log.debug("No first element found with class 'c50-pilot'");
						}
					} else {
						sb.append("<li>Hero Large component is not found on web publisher page</li>");
						log.debug("No element found with class 'c50-pilot'");
					}
						
				} catch (Exception e) {
					sb.append("<li>Unable to update hero_large component.</li>");
					log.debug("Exception : ", e);
				}
				// end set Hero Large content.
				// ------------------------------------------------------------------------------------------------------------------------------------------
				// start set drawers_container component content.
				try {
					Node drawersContainerNode = null;
					Elements drawerComponentHeaderElements = doc.select("div.c00-pilot,div.c100-pilot");
					if (drawerComponentHeaderElements != null) {
						Element drawerComponentHeader = drawerComponentHeaderElements.first();
						if (drawerComponentHeader != null) {
							String drawerComponentHeaderTitle = "";
							Elements drawerComponentHeaderTitleElements = drawerComponentHeader.getElementsByTag("h2");
							if (drawerComponentHeaderTitleElements != null) {
								Element drawerComponentHeaderTitleElement = drawerComponentHeaderTitleElements.first();
								if (drawerComponentHeaderTitleElement != null) {
									drawerComponentHeaderTitle = drawerComponentHeaderTitleElement.text();
								} else {
									sb.append("<li>drawerComponent Header Title Element not found</li>");
									log.debug("h2 element first not found with in the class 'c00-pilot or c100-pilot' of div.");
								}
							} else {
								sb.append("<li>drawerComponent Header Title Element Section not found</li>");
								log.debug("h2 element not found with in the class 'c00-pilot or c100-pilot' of div.");
							}
							if (indexLowerLeftNode.hasNodes()) {
								NodeIterator drawersContainerIterator = indexLowerLeftNode.getNodes("drawers_container*");

								if (drawersContainerIterator.hasNext()) {
									drawersContainerNode = drawersContainerIterator.nextNode();
									if (StringUtils.isNotBlank(drawerComponentHeaderTitle)) {
										drawersContainerNode.setProperty("title", drawerComponentHeaderTitle);
									} else {
										sb.append("<li>title of drawer container doesn't exist for "+drawerComponentHeaderTitle+"</li>");
										log.debug("Drawer component title is blank.");
									}
									Elements hTextElements = doc.getElementsByAttribute("data-config-hidetext");
									if (hTextElements != null && hTextElements.size() > 0) {
										Element hText = hTextElements.first();
										if (hText != null) {
											drawersContainerNode.setProperty("closetext", hText.attr("data-config-hidetext"));
											drawersContainerNode.setProperty("opentext", hText.attr("data-config-showtext"));
										} else {
											sb.append("<li>data-config-hidetext not found for "+drawerComponentHeaderTitle+"</li>");
											log.debug("No first attribute found with name 'data-config-hidetext' in the doc.");
										}
									} else {
										log.debug("No attribute found with name 'data-config-hidetext' in the doc So show Text and hide Text canot be migrated.");
										sb.append("No attribute found with name 'data-config-hidetext' in the doc So show Text and hide Text canot be migrated.");
									}
									Element drawerHeaderLinksElement = drawerComponentHeader.select("div.clearfix").first();
									JSONObject jsonObj = new JSONObject();
									String anchorText = "";
									String anchorHref = "";
									if (drawerHeaderLinksElement != null) {
										Elements anchorElements = drawerHeaderLinksElement.getElementsByTag("a");
										if (anchorElements != null) {
											Element anchorElement = anchorElements.first();
											if (anchorElement != null) {
												anchorText = anchorElement.text();
												anchorHref = anchorElement.absUrl("href");
												// Start extracting valid href
												log.debug("anchorHref before migration : " + anchorHref);
												anchorHref = FrameworkUtils.getLocaleReference(anchorHref, urlMap, locale, sb, catType, type);
												log.debug("anchorHref after migration : " + anchorHref);
												// End extracting valid href
											} else {
												log.debug("anchor first element not found within the class 'clearfix' of div.");
											}
										} else {
											log.debug("anchor element not found within the class 'clearfix' of div.");
										}
										if (anchorElements.size() > 1) {
											sb.append("<li>extra link found in drawer container header for "+drawerComponentHeaderTitle+"</li>");
										}
										jsonObj.put("linktext", anchorText);
										jsonObj.put("linkurl", anchorHref);
										log.debug("anchorText : " + anchorText);
										log.debug("anchorHref : " + anchorHref);
										if (jsonObj != null && jsonObj.length() > 0) {
											if (drawersContainerNode.hasProperty("headerlinks")) {
												Property p = drawersContainerNode.getProperty("headerlinks");
												p.remove();
												session.save();
											}
											drawersContainerNode.setProperty("headerlinks", jsonObj.toString());
										} 
									}
								}
								if (drawersContainerNode != null && drawersContainerNode.hasNodes()) {
									NodeIterator drawerPanelsIterator = drawersContainerNode.getNodes("drawerspanel*");
									javax.jcr.Node drawersPanelNode = null;
									Elements drawersPanelElements = doc.select("div.n21,ul.n21");

									if (drawersPanelElements != null) {
										// Element drawersPanelElement = drawersPanelElements.first();
										// start new code
										int count = 0;
										for (Element drawersPanelElement : drawersPanelElements) {
											
											Elements drawerPanelLiElements = drawersPanelElement.getElementsByTag("li");
											if (drawerPanelLiElements != null) {
												log.debug("li elements size" + drawerPanelLiElements.size());
												for (Element drawerPanelLiElement : drawerPanelLiElements) {
													boolean infoLinksMisMatchFlag = false;
													boolean linkUrlNotFoundFlag = false;
													boolean imageSrcNotFoundFlag = false;
													boolean subdrawerTitleNotFoundFlag = false;
													boolean subdrawerHighlightsNotFoundFlag = false;
													boolean misMatchFlag = true;
													String panelTitle = "";
													Elements iconBlock = drawerPanelLiElement.select("div.series");
													if (iconBlock.size() == 0) {
														log.debug("icon div with class 'series' not found.");
														continue;
													}
													count = count + 1;
													if (drawerPanelsIterator.hasNext()) {
														drawersPanelNode = drawerPanelsIterator.nextNode();
													}
													Elements seriesElements = drawerPanelLiElement.select("div.series");
													if (seriesElements != null) {
														Element seriesElement = seriesElements.first();
														String linkUrl = "";
														String panelDescription = "";
														if (seriesElement != null) {
															Elements panelTitleElements = seriesElement.getElementsByTag("h3");
															if (panelTitleElements != null) {
																Element panelTitleElement = panelTitleElements.first();
																if (panelTitleElement != null) {
																	Elements anchorTag = panelTitleElement.getElementsByTag("a");
																	if (anchorTag.size() > 0) {
																		panelTitle = anchorTag.first().text();
																		linkUrl = anchorTag.first().attr("href");
																		// Start extracting valid href
																		log.debug("Before linkUrl" + linkUrl);
																		linkUrl = FrameworkUtils.getLocaleReference(linkUrl, urlMap, locale, sb, catType, type);
																		log.debug("after linkUrl" + linkUrl);
																		// End extracting valid href
																	}
																	if (StringUtils.isBlank(panelTitle)) {
																		panelTitle = panelTitleElement.text();
																	}
																	log.debug("panel title : " + panelTitle);
																} else {
																	log.debug("h3 first element not found with in the class 'series' of div.");
																}
															} else {
																log.debug("h3 element not found with in the class 'series' of div.");
															}
															Elements panelParaElements = seriesElement.getElementsByTag("p");
															if (panelParaElements != null) {
																Element panelDescriptionElement = panelParaElements.first();
																panelDescription = panelDescriptionElement.text();
															} else {
																log.debug("p tag not found with in the 'series' class of the div.");
															}
															// start image
															String drawerImage = FrameworkUtils.extractImagePath(seriesElement, sb);
															log.debug("drawerImage : " + drawerImage);
															if (drawersPanelNode != null) {
																if (drawersPanelNode.hasNode("drawers-image")) {
																	Node drawersImageNode = drawersPanelNode.getNode("drawers-image");
																	String fileReference = drawersImageNode.hasProperty("fileReference") ? drawersImageNode.getProperty("fileReference").getString() : "";
																	drawerImage = FrameworkUtils.migrateDAMContent(drawerImage, fileReference, locale, sb, catType, type);
																	log.debug("drawerImage " + drawerImage + "\n");
																	if (StringUtils.isNotBlank(drawerImage)) {
																		drawersImageNode.setProperty("fileReference", drawerImage);
																	}
																} else {
																	sb.append("<li>drawer image node doesn't exist for "+drawerComponentHeaderTitle+"</li>");
																	log.debug("'drawers-image' node not found with in the node : " + drawersPanelNode.getPath());
																}
															}
															// end image
															if (drawersPanelNode != null) {
																log.debug("panelTitle : " + panelTitle);
																log.debug("linkUrl : " + linkUrl);
																log.debug("panelDescription : " + panelDescription);
																if (StringUtils.isNotBlank(panelTitle)) {
																	drawersPanelNode.setProperty("title", panelTitle);
																} else {
																	sb.append("<li>title of drawer panel doesn't exist </li>");
																	log.debug("Title is blank for one of the panel.");
																}
																if (StringUtils.isNotBlank(linkUrl)) {
																	drawersPanelNode.setProperty("linkurl", linkUrl);
																} else {
																	//fix for generating comment in the report
																	if(drawersPanelNode.hasProperty("linkurl")){
																	sb.append("<li>Title Link of drawer panel doesn't exist for "+ panelTitle+" </li>");
																	log.debug("link url is blank for thr one of the panel.");
																	}
																}
																if (StringUtils.isNotBlank(panelDescription)) {
																	drawersPanelNode.setProperty("description", panelDescription);
																} else {
																	sb.append("<li>Description of drawer panel doesn't exist </li>");
																	log.debug("description is blank for one of the panel.");
																}
															}
														}
													}
													// selecting sub drawer elements from document
													NodeIterator subDrawerIterator = drawersPanelNode.getNode("parsys-drawers").getNodes("subdrawer_product*");
													javax.jcr.Node subdrawerpanel = null;
													Elements subDrawerColl = drawerPanelLiElement.select("ul.items");
													Elements clearfixdivs = drawerPanelLiElement.select("li.clearfix");

													for (Element ss : subDrawerColl) {
														String title = "";
														String linkTitleUrl = "";
														Elements subItems = ss.select("div.prodinfo");
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
																					log.debug("Sub Series Title : " + siATitle.text());
																					log.debug("siATitle.text() : " + siATitle.text());
																					log.debug("siATitle.text() " + siATitle.attr("href"));
																					title = siATitle.text();
																					linkTitleUrl = siATitle.attr("href");
																					// Start extracting  valid href
																					log.debug("Before linkTitleUrl" + linkTitleUrl);
																					linkTitleUrl = FrameworkUtils.getLocaleReference(linkTitleUrl, urlMap, locale, sb, catType, type);
																					log.debug("after linkTitleUrl" + linkTitleUrl);
																					// End extracting valid href
																				} else {
																					log.debug("h4 tag first element not found with in the class 'prodinfo' of div.");
																				}
																			} else {
																				log.debug("h4 tag element not found with in the class 'prodinfo' of div.");
																			}
																		} else {
																			log.debug("h4 tag element not found with in the class 'prodinfo' of div.");
																		}

																	} else {
																		log.debug("h4 tag not found with in the class 'prodinfo' of div");
																	}
																	// start image
																	String subDrawerImage = FrameworkUtils.extractImagePath(subItem, sb);
																	log.debug("subDrawerImage before migration : " + subDrawerImage);
																	if (subdrawerpanel != null) {
																		if (subdrawerpanel.hasNode("subdrawers-image")) {
																			Node subDrawersImageNode = subdrawerpanel.getNode("subdrawers-image");
																			String fileReference = subDrawersImageNode.hasProperty("fileReference") ? subDrawersImageNode.getProperty("fileReference").getString() : "";
																			subDrawerImage = FrameworkUtils.migrateDAMContent(subDrawerImage, fileReference, locale, sb, catType, type);
																			log.debug("subDrawerImage after migration : " + subDrawerImage);
																			if (StringUtils.isNotBlank(subDrawerImage)) {
																				subDrawersImageNode.setProperty("fileReference", subDrawerImage);
																			}else{
																				imageSrcNotFoundFlag = true;
																			}
																		} else {
																			
																			log.debug("'subdrawers-image' node not found with the node : " + subdrawerpanel.getPath());
																		}
																	}
																	// end image
																	Elements indDetailsElements = subItem.select("ul.details");
																	if (indDetailsElements != null) {
																		Element indDetailsElement = indDetailsElements.first();
																		if (indDetailsElement != null) {
																			Elements indItems = indDetailsElement.getElementsByTag("li");
																			if (indItems != null) {
																				for (Element indItem : indItems) {
																					JSONObject jsonObj = new JSONObject();
																					jsonObj.put("linktext", indItem.html());
																					list1.add(jsonObj.toString());
																				}
																			} else {
																				log.debug("No li element found with in the class 'details' of ul tag.");
																			}
																		} else {
																			log.debug("first element not found with in the class 'subdrawerpanel' of ul tag.");
																		}
																	} else {
																		log.debug("element not found with in the class 'subdrawerpanel' of ul tag.");
																	}

																	Element subItemUlInfoLink = subItem.siblingElements().first(); // subItemUlInfoLinks.first();
																	log.debug("Info Links Elements : " + subItemUlInfoLink);
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
																					// Start extracting valid href
																					log.debug("Before linkTextUrl" + linkTextUrl);
																					linkTextUrl = FrameworkUtils.getLocaleReference(linkTextUrl, urlMap, locale, sb, catType, type);
																					log.debug("after linkTextUrl" + linkTextUrl);
																					// End extracting valid href
																				} else {
																					log.debug("No first anchor tag found with in the 'li' of 'details' class of ul tag.");
																				}
																			} else {
																				log.debug("No anchor tag found with in the 'li' of 'details' class of ul tag.");
																			}
																			if (StringUtils.isNotBlank(linkText)) {
																				jsonObj.put("linktext", linkText);
																			}
																			if (StringUtils.isNotBlank(linkTextUrl)) {
																				jsonObj.put("linkurl", linkTextUrl);
																			}
																			list2.add(jsonObj.toString());
																		}
																		log.debug("list size : " + list2.size());
																	}
																}
																if (subdrawerpanel != null) {
																	if (StringUtils.isNotBlank(title)) {
																		subdrawerpanel.setProperty("title", title);
																	} else {
																		subdrawerTitleNotFoundFlag = true;
																		log.debug("Title of the sub drawer doesn't exists.");
																	}
																	if (StringUtils.isNotBlank(linkTitleUrl)) {
																		subdrawerpanel.setProperty("linkurl", linkTitleUrl);
																	} else {
																		linkUrlNotFoundFlag = true;
																		log.debug("linkurl property is not set at : " + subdrawerpanel.getPath());
																	}
																	if (list1.size() > 0) {
																		if (subdrawerpanel.hasProperty("highlights")) {
																			Property p = subdrawerpanel.getProperty("highlights");
																			p.remove();
																			session.save();
																		}
																		subdrawerpanel.setProperty("highlights", list1.toArray(new String[list1.size()]));
																	} else {
																		subdrawerHighlightsNotFoundFlag = true;
																		log.debug("hightlights of sub drawer doesn't exists.");
																	}
																	if (list2.size() > 0) {
																		if (subdrawerpanel.hasProperty("infolinks")) {
																			Property p = subdrawerpanel.getProperty("infolinks");
																			p.remove();
																			session.save();
																		}
																		subdrawerpanel.setProperty("infolinks", list2.toArray(new String[list2.size()]));
																	} else {
																		infoLinksMisMatchFlag = true;
																		log.debug("infolinks of sub drawer doesn't exists.");
																	}
																} else {
																	misMatchFlag = false;
																}
															}
														}
														
													}
													if (!misMatchFlag) {
														sb.append("<li>Mis Match of subdrawers count in drawer panel " + panelTitle + "</li>");
													}
													
													if(infoLinksMisMatchFlag){
														sb.append(Constants.MISMATCH_IN_INFOLINKS+" "+panelTitle);
													}
													if(linkUrlNotFoundFlag){
														sb.append(Constants.LINK_URL_OF_SUB_DRAWER_NOT_FOUND+" "+panelTitle);
													}
													if(imageSrcNotFoundFlag){
														sb.append(Constants.IMAGE_NOT_FOUND_IN_LOCALE_PAGE+" "+panelTitle);
													}
													if(subdrawerHighlightsNotFoundFlag){
														sb.append(Constants.IMAGE_NOT_FOUND_IN_LOCALE_PAGE+" "+panelTitle);
													}
													if(subdrawerTitleNotFoundFlag){
														sb.append(Constants.IMAGE_NOT_FOUND_IN_LOCALE_PAGE+" "+panelTitle);
													}
												}
											}
										}
										if (count != drawerPanelsIterator.getSize())
											sb.append("<li>Mis-Match in drawer panels count</li>");
										// end new code
									} else {
										sb.append("<li>drawer panel elements section not found</li>");
									}
								} else {
									sb.append("<li>drawers_container node is not found</li>");
								}
							}
						} else {
							sb.append("<li>DrawerComponent HeaderElement not found</li>");
						}
					} else {
						sb.append("<li>DrawerComponent HeaderElements not found</li>");
					}
				} catch (Exception e) {
					sb.append("<li>Unable to update drawers_container component.</li>");
					log.debug("Exception : ", e);
				}
				// end set drawers_container component content.
				// ---------------------------------------------------------------------------------------------------------------------------------------
				// start of html blob components content.
				try {
					log.debug("Started Migrating html blob.");
					String html = "";
					Elements iconBlockElements = doc.select("div.icon-block");
					// boolean test =
					// doc.select("div.c23-pilot").contains("div.icon-block");
					if (iconBlockElements.isEmpty()) {
						log.debug("html blob content not found with class 'icon-block'");
						iconBlockElements = doc.select("div.poly");
						if (iconBlockElements != null) {
							// Element htmlblobElement =
							// iconBlockElements.first();
							for (Element htmlblobElement : iconBlockElements) {
								if (htmlblobElement.hasClass("c47-pilot")) {
									continue;
								}
								if (htmlblobElement != null) {
									Elements ulElements = htmlblobElement.getElementsByTag("ul");
									if (ulElements.size() > 0) {
										html = FrameworkUtils.extractHtmlBlobContent(htmlblobElement, "", locale, sb, urlMap, catType, type);
									}
								}
							}
						} else {
							sb.append("<li>htmlblob component not found on publisher page </li>");
						}
					} else {
						log.debug("html blob content found with class 'icon-block'");
						if (iconBlockElements != null) {
							// Element htmlblobElement =
							// iconBlockElements.first();
							for (Element htmlblobElement : iconBlockElements) {
								if (htmlblobElement != null) {
									// html = htmlblobElement.outerHtml();
									Element htmlElement = htmlblobElement.parent();
									html = FrameworkUtils.extractHtmlBlobContent(htmlElement, "", locale, sb, urlMap, catType, type);
									/*if (htmlblobElement.getElementsByTag("ul").size() > 0) {
									} else {
										Element iconBlockParent = htmlblobElement.parent();
										if (iconBlockParent != null) {
											Elements anchorTagEle = iconBlockParent.getElementsByTag("a");
											if (anchorTagEle != null) {
												for (Element aTagElement : anchorTagEle) {
													html = html + aTagElement.outerHtml();
													html = html + "<br>";
												}
											}
										}
									}*/
								} else {
									sb.append("<li>htmlblob/icon-block Element section not found</li>");
									log.debug("htmlblob/icon-block Element section not found");
								}
							}
						}
					}
					// Elements iconBlockElements =
					// doc.select("div.icon-block, div.poly");

					if (indexLowerRightNode.hasNode("htmlblob")) {
						javax.jcr.Node htmlBlobNode = indexLowerRightNode.getNode("htmlblob");
						log.debug("htmlblobElement.outerHtml() " + html);
						if (StringUtils.isNotBlank(html)) {
							htmlBlobNode.setProperty("html", html);
						} else {
							sb.append("<li>htmlblob content doesn't exist</li>");
						}
					} else {
						sb.append("<li>Htmlblob component not found.</li>");
						log.debug("'htmlblob' node not found at : " + indexLowerRightNode.getPath());
					}

				} catch (Exception e) {
					log.debug("Excepiton : ", e);
				}
				// end set html blob component content.
				// --------------------------------------------------------------------------------------------------------------------------
				// start of tile bordered components.
				try {
					Elements rightRail = doc
							.select("div.c23-pilot,div.cc23-pilot");
					if (rightRail != null) {
						log.debug("rightRail size : " + rightRail.size());
						if (rightRail.size() > 0) {
							NodeIterator titleBorderNodes = indexLowerRightNode.getNodes("tile_bordered*");
							int count = 0;
							int countOfTileBorderedElements = 0;
							boolean flag = false;
							for (Element ele : rightRail) {
								Elements iconBlock = ele.select("div.icon-block");
								if (iconBlock != null && iconBlock.size() > 0) {
									continue;
								}
								Elements ulElements = ele.getElementsByTag("ul");
								if (ulElements.size() > 0) {
									log.debug("found ul elements in c23-pilot so skipping.");
									continue;
								}
								count = count + 1;
								Node rightRailNode = null;
								String title = ele.getElementsByTag("h2") != null ? ele.getElementsByTag("h2").text() : "";
								if (StringUtils.isBlank(title)) {
									title = ele.getElementsByTag("h3") != null ? ele.getElementsByTag("h3").text() : "";
								}
								String desc = ele.getElementsByTag("p") != null ? ele.getElementsByTag("p").text() : "";
								Elements anchor = ele.getElementsByTag("a");

								String textAfterAnchorTag = ele.ownText();
								if (StringUtils.isNotBlank(textAfterAnchorTag)) {
									flag = true;
									countOfTileBorderedElements++;
								}
								String anchorText = anchor != null ? anchor.text() : "";
								String anchorHref = anchor != null ? anchor.attr("href") : "";
								// Start extracting valid href
								log.debug("Before anchorHref" + anchorHref);
								anchorHref = FrameworkUtils.getLocaleReference(anchorHref, urlMap, locale, sb, catType, type);
								log.debug("after anchorHref" + anchorHref);
								// End extracting valid href
								if (titleBorderNodes.hasNext()) {
									rightRailNode = (Node) titleBorderNodes.next();
								} else {
									log.debug("<li>all tile_boredered components are migrated</li>");
								}
								if (rightRailNode != null) {
									if (title != null && title != "" && desc != null && desc != "" && anchorText != null && anchorText != "") {
										rightRailNode.setProperty("title", title);
										rightRailNode.setProperty("description", desc);
										rightRailNode.setProperty("linktext", anchorText);
										rightRailNode.setProperty("linkurl", anchorHref);
										log.debug("title, description, linktext and linkurl are created at " + rightRailNode.getPath());
									} else {
										log.debug("<li>Content miss match for " + "</li>");
									}
								} else {
									log.debug("<li>one of title_bordered node doesn't exist in node structure.</li>");
								}
							}
							if (flag) {
								sb.append("<li>Extra Text found after link on locale page for " + countOfTileBorderedElements + " TileBordered Component(s) , hence the text cannot be migrated.</li>");
							}
							if (count != indexLowerRightNode.getNodes("tile_bordered*").getSize()) {
								sb.append("<li>Mis-Match in tilebordered Panels count." + count + " is not equal " + indexLowerRightNode.getNodes( "tile_bordered*").getSize() + "</li>");
							}
						} else {
							log.debug("<li>No Content with class 'c23-pilot or cc23-pilot' found</li>");
						}
					} else {
						sb.append("<li>tile bordered component not present in the web publisher page</li>");
					}
				} catch (Exception e) {
					log.debug("Exception ", e);
					log.debug("<li>Unable to update tile_bordered component.\n</li>");
				}
				// End of tile bordered components.
				// -----------------------------------------------------------------------------------------------------
				session.save();
			} else {
				sb.append(Constants.URL_CONNECTION_EXCEPTION);
			}
		} catch (Exception e) {
			log.error("Exception ", e);
		}
		sb.append("</ul></td>");
		return sb.toString();
	}
}
