package com.cisco.dse.global.migration.productlanding;

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

	public String translate(String host, String loc, String prod, String type, String catType,
			String locale, Session session) throws IOException,
			ValueFormatException, VersionException, LockException,
			ConstraintViolationException, RepositoryException {
		BasicConfigurator.configure();
		log.debug("In the translate method");
		log.debug("In the translate method, catType is :"+ catType);

		// Repo node paths
		String pagePropertiesPath = "/content/<locale>/"+catType+"/<prod>/index/jcr:content";
		String indexUpperLeft = "/content/<locale>/"+catType+"/<prod>/index/jcr:content/content_parsys/overview/layout-overview/gd12v1/gd12v1-left";
		String indexUpperRight = "/content/<locale>/"+catType+"/<prod>/index/jcr:content/content_parsys/overview/layout-overview/gd12v1/gd12v1-right";
		String indexLowerLeft = "/content/<locale>/"+catType+"/<prod>/index/jcr:content/content_parsys/overview/layout-overview/gd12v2/gd12v2-left";
		String indexLowerRight = "/content/<locale>/"+catType+"/<prod>/index/jcr:content/content_parsys/overview/layout-overview/gd12v2/gd12v2-right";
		String pageUrl = host + "/content/<locale>/"+catType+"/<prod>/index.html";

		pageUrl = pageUrl.replace("<locale>", locale).replace("<prod>", prod);
		pagePropertiesPath = pagePropertiesPath.replace("<locale>", locale).replace("<prod>", prod);

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
		javax.jcr.Node pageJcrNode = null;
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
			if(doc != null){
				title = doc.title();

				// ------------------------------------------------------------------------------------------------------------------------------------------
				// start set page properties.

				FrameworkUtils.setPageProperties(pageJcrNode, doc, session, sb);

				// end set page properties.
				// ------------------------------------------------------------------------------------------------------------------------------------------


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
									log.debug("<li>Primary CTA Heding element not having title</li>");
								}
							} else {
								log.debug("<li>Primary CTA Heading element section not found </li>");
							}
							Elements paraElements = primaryCTAElement.getElementsByTag("p");
							if (paraElements != null) {
								Element paraElement = paraElements.first();
								if (paraElement != null) {
									primaryCTADescription = paraElement.text();
								} else {
									log.debug("<li>Primary CTA Para element not having title</li>");
								}
							} else {
								log.debug("<li>Primary CTA Para element section not found </li>");
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
												log.debug("<li>Primary CTA Link anchor tag not found </li>");
											}
										} else {
											log.debug("<li>Primary CTA Link anchor tag section not found </li>");
										}
									} else {
										log.debug("<li>Primary CTA Link not found </li>");
									}
								} else {
									log.debug("<li>Primary CTA Links not found </li>");
								}
							} else {
								log.debug("<li>Primary CTA Links section not found </li>");
							}
							log.debug("primaryCTATitle" + primaryCTATitle + "\n");
							log.debug("primaryCTADescription" + primaryCTADescription + "\n");
							log.debug("primaryCTALinkText" + primaryCTALinkText + "\n");
							log.debug("primaryCTALinkUrl" + primaryCTALinkUrl + "\n");
							if (indexUpperLeftNode.hasNode("primary_cta_v2")) {
								javax.jcr.Node primartCTANode = indexUpperLeftNode.getNode("primary_cta_v2");
								if(StringUtils.isNotBlank(primaryCTATitle)){
									primartCTANode.setProperty("title", primaryCTATitle);
								} else{
									sb.append("<li>title of primary CTA doesn't exist</li>");
									log.debug("title property is not set at " + primartCTANode.getPath());
								}
								if(StringUtils.isNotBlank(primaryCTADescription)){
									primartCTANode.setProperty("description", primaryCTADescription);
								} else{
									sb.append("<li>description of primary CTA doesn't exist</li>");
									log.debug("description property is not set at " + primartCTANode.getPath());
								}
								if(StringUtils.isNotBlank(primaryCTALinkText)){
									primartCTANode.setProperty("linktext", primaryCTALinkText);
								} else{
									sb.append("<li>link text of primary CTA doesn't exist</li>");
									log.debug("linktext property is not set at " + primartCTANode.getPath());
								}

								if (primartCTANode.hasNode("linkurl")) {
									javax.jcr.Node primartCTALinkUrlNode = primartCTANode.getNode("linkurl");
									if(StringUtils.isNotBlank(primaryCTALinkUrl)){
										primartCTALinkUrlNode.setProperty("url", primaryCTALinkUrl);
									} else{
										sb.append("<li>link url of primary CTA doesn't exist</li>");
										log.debug("url property is not set at " + primartCTALinkUrlNode.getPath());
									}
								} else {
									log.debug("<li>primary_cta_v2 node is not having linkurl node</li>");
								}
							} else {								
								log.debug("<li>primary_cta_v2 node doesn't exists</li>");
							}
						} else {								
							log.debug("<li>CTA element not found.</li>");
						}
					} else {
						sb.append("<li> Primary CTA component is not there in web publisher page.</li>");
					}
				} catch (Exception e) {
					log.debug("<li>Unable to update primary_cta_v2 component."+e+"</li>");
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
						log.debug("<li>Node with name 'hero_large' doesn't exist under "+indexUpperRightNode.getPath()+"</li>");
					}

					Elements heroLargeElements = doc.select("div.c50-pilot");
					if (heroLargeElements != null) {
						Element heroLargeElement = heroLargeElements.first();
						if (heroLargeElement != null) {
							Elements heroLargeFrameElements = heroLargeElement.select("div.frame");
							javax.jcr.Node heroPanelNode = null;

							if (heroLargeFrameElements != null) {
								if (heroLargeFrameElements.size() != heroLargeNode.getNodes("heropanel*").getSize()) {
									sb.append("<li>Mismatch in the count of slides in the hero component </li>");
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
											log.debug("<li>Hero Panel element not having any description in it </li>");
										}
									} else {
										log.debug("<li>Hero Panel para element not found </li>");
									}
									Elements heroPanelLinkTextElements = ele.getElementsByTag("b");
									if (heroPanelLinkTextElements != null) {
										Element heroPanelLinkTextElement = heroPanelLinkTextElements.first();
										if (heroPanelLinkTextElement != null) {
											heroPanelLinkText = heroPanelLinkTextElement.text();
										} else {
											log.debug("<li>Hero Panel element not having any linktext in it </li>");
										}
									} else {
										log.debug("<li>Hero Panel linktext element not found  </li>");
									}
									Elements heroPanelLinkUrlElements = ele.getElementsByTag("a");
									if (heroPanelLinkUrlElements != null) {
										Element heroPanelLinkUrlElement = heroPanelLinkUrlElements.first();
										if (heroPanelLinkUrlElement != null) {
											heroPanellinkUrl =heroPanelLinkUrlElement.attr("href");
										} else {
											log.debug("<li>Hero Panel element not having any linkurl in it </li>");
										}
									} else {
										log.debug("<li>Hero Panel link url element not found </li>");
									}
									// start image
									String heroImage = FrameworkUtils.extractImagePath(ele, sb);
									log.debug("heroImage " + heroImage + "\n");
									heroImage = FrameworkUtils.migrateDAMContent(heroImage, locale);
									log.debug("heroImage " + heroImage + "\n");
									if (heroPanelNode != null) {
										if (heroPanelNode.hasNode("image")) {
											Node imageNode = heroPanelNode.getNode("image");
											if (StringUtils.isNotBlank(heroImage)) {
												imageNode.setProperty("fileReference" , heroImage);
											} else {
												sb.append("<li>hero image doesn't exist</li>");
											}
										} else {
											sb.append("<li>hero image node doesn't exist</li>");
										}
									}
									// end image
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
											sb.append("<li>title of hero slide doesn't exist</li>");
										}
										if (StringUtils.isNotBlank(heroPanelDescription)) {
											heroPanelNode.setProperty("description", heroPanelDescription);
										} else {
											sb.append("<li>description of hero slide doesn't exist</li>");
										}
										if (StringUtils.isNotBlank(heroPanelLinkText)) {
											heroPanelNode.setProperty("linktext", heroPanelLinkText);
										} else {
											sb.append("<li>link text of hero slide doesn't exist</li>");
										}
										if (StringUtils.isNotBlank(heroPanellinkUrl)) {
											heroPanelNode.setProperty("linkurl", heroPanellinkUrl);
										} else {
											sb.append("<li>link url of hero slide doesn't exist / found video as link url for the slide on web publisher page </li>");
										}										
									}
								}
							} else {
								log.debug("<li>Hero Large Frames/Panel Elements is not found</li>");
							}
						} else {
							log.debug("<li>Hero Large Element is not found</li>");
						}
					} else {
						sb.append("<li>Hero Large component is not found on web publisher page</li>");
					}
				} catch (Exception e) {
					sb.append("<li>Unable to update hero_large component."+e+"</li>");
				}

				// end set Hero Large content.
				// ---------------------------------------------------------------------------------------------------------------------------------------
				// ------------------------------------------------------------------------------------------------------------------------------------------
				// start set drawers_container component content.
				try {
					javax.jcr.Node drawersContainerNode = null;
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
									log.debug("<li>drawerComponent Header Title Element not found</li>");
								}
							} else {
								log.debug("<li>drawerComponent Header Title Element Section not found</li>");
							}
							if (indexLowerLeftNode.hasNodes()) {
								NodeIterator drawersContainerIterator = indexLowerLeftNode.getNodes("drawers_container*");

								if (drawersContainerIterator.hasNext()) {
									drawersContainerNode = drawersContainerIterator.nextNode();
									log.debug("drawerComponentHeaderTitle " + drawerComponentHeaderTitle + "\n");
									if (StringUtils.isNotBlank(drawerComponentHeaderTitle)) {
										drawersContainerNode.setProperty("title", drawerComponentHeaderTitle);
									} else {
										sb.append("<li>title of drawer container doesn't exist</li>");
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
										sb.append("<li>showtext and hidetext links doesn't exist</li>");
									}

									Element drawerHeaderLinksElement = drawerComponentHeader.select("div.clearfix").first();
									JSONObject jsonObj = new JSONObject();
									String anchorText = "";
									String anchorHref = "";
									if (drawerHeaderLinksElement != null) {
										Elements anchorElements = drawerHeaderLinksElement.getElementsByTag("a");
										if (anchorElements != null) {
											Element anchorElement =anchorElements.first();
											if (anchorElement != null) {
												anchorText = anchorElement.text();
												anchorHref = anchorElement.attr("href");
											} else {
												log.debug("<li>drawerComponent link Element not found</li>");
											}
										} else {
											log.debug("<li>drawerComponent link Element section not found</li>");
										}
										if (anchorElements.size() > 1) {
											sb.append("<li>extra link found in drawer container header</li>");
										}
										jsonObj.put("linktext", anchorText);
										jsonObj.put("linkurl", anchorHref);
										log.debug("anchorText " + anchorText + "\n");
										log.debug("anchorHref " + anchorHref + "\n");
										if (jsonObj != null && jsonObj.length() > 0) {
											if (drawersContainerNode.hasProperty("headerlinks")) {
												Property p = drawersContainerNode.getProperty("headerlinks");
												p.remove();
												session.save();
											}
											drawersContainerNode.setProperty("headerlinks",jsonObj.toString());
										} else {
											sb.append("<li>link of drawer container doesn't exist</li>");
										}
									}
								}
								if (drawersContainerNode != null && drawersContainerNode.hasNodes()) {
									NodeIterator drawerPanelsIterator = drawersContainerNode.getNodes("drawerspanel*");
									javax.jcr.Node drawersPanelNode = null;
									Elements drawersPanelElements = doc.select("div.n21,ul.n21");

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
																	log.debug("panel title" + panelTitle);
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
															// start image																													
															String drawerImage = FrameworkUtils.extractImagePath(seriesElement, sb);
															log.debug("drawerImage " + drawerImage + "\n");
															drawerImage = FrameworkUtils.migrateDAMContent(drawerImage, locale);
															log.debug("drawerImage " + drawerImage + "\n");
															if (drawersPanelNode != null) {
																if (drawersPanelNode.hasNode("drawers-image")) {
																	Node drawersImageNode = drawersPanelNode.getNode("drawers-image");
																	if (StringUtils.isNotBlank(drawerImage)) {
																		drawersImageNode.setProperty("fileReference" , drawerImage);
																	} else {
																		sb.append("<li>drawer image doesn't exist</li>");
																	}
																} else {
																	sb.append("<li>drawer image node doesn't exist</li>");
																}
															}
															// end image
															if (drawersPanelNode != null) {
																log.debug("panelTitle " + panelTitle + "\n");
																log.debug("linkUrl " + linkUrl + "\n");
																log.debug("panelDescription " + panelDescription + "\n");
																if (StringUtils.isNotBlank(panelTitle)) {
																	drawersPanelNode.setProperty("title", panelTitle);
																} else {
																	sb.append("<li>title of drawer panel doesn't exist</li>");
																}
																if (StringUtils.isNotBlank(linkUrl)) {
																	drawersPanelNode.setProperty("linkurl", linkUrl);
																} else {
																	sb.append("<li>link of the title of drawer panel doesn't exist</li>");
																}
																if (StringUtils.isNotBlank(panelDescription)) {
																	drawersPanelNode.setProperty("description", panelDescription);
																} else {
																	sb.append("<li>description of drawer panel doesn't exist</li>");
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
																					log.debug("<li>sub series title Element anchor not found</li>");
																				}
																			} else {
																				log.debug("<li>sub series title Element anchor Section not found</li>");
																			}
																		} else {
																			log.debug("<li>sub series title Element not found</li>");
																		}

																	} else {
																		log.debug("<li>sub series title Elements section not found</li>");
																	}
																	// start image
																	String subDrawerImage = FrameworkUtils.extractImagePath(subItem, sb);
																	log.debug("subDrawerImage " + subDrawerImage + "\n");
																	subDrawerImage = FrameworkUtils.migrateDAMContent(subDrawerImage, locale);
																	log.debug("subDrawerImage " + subDrawerImage + "\n");
																	if (subdrawerpanel != null) {
																		if (subdrawerpanel.hasNode("subdrawers-image")) {
																			Node subDrawersImageNode = subdrawerpanel.getNode("subdrawers-image");
																			if (StringUtils.isNotBlank(subDrawerImage)) {
																				subDrawersImageNode.setProperty("fileReference" , subDrawerImage);
																			} else {
																				sb.append("<li>subdrawer image doesn't exist</li>");
																			}
																		} else {
																			sb.append("<li>subdrawer image node doesn't exist</li>");
																		}
																	}
																	// end image
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
																					log.debug("<li>info links anchor element not found</li>");
																				}
																			} else {
																				log.debug("<li>info links anchor element section not found</li>");
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
																	if (StringUtils.isNotBlank(title)) {
																		subdrawerpanel
																		.setProperty("title", title);
																	} else {
																		sb.append("<li>title of sub drawer doesn't exist</li>");
																	}
																	if (StringUtils.isNotBlank(linkTitleUrl)) {
																		subdrawerpanel.setProperty("linkurl", linkTitleUrl);
																	} else {
																		sb.append("<li>link url of sub drawer doesn't exist</li>");
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
																		sb.append("<li>highlights of sub drawer doesn't exist</li>");
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
																		sb.append("<li>infolinks of sub drawer doesn't exist</li>");
																	}
																}else{
																	misMatchFlag = false;
																}
															}

														}


													}
													if (!misMatchFlag) {
														sb.append("<li>Mis Match of subdrawers count in drawer panel</li>");
													}
												}


											}
										}
										if (count != drawerPanelsIterator.getSize())
											sb.append("<li>Mis-Match in drawer panels count</li>");

										//end new code

									} else {
										log.debug("<li>drawer panel elements section not found</li>");
									}
								}else {
									log.debug("<li>drawers_container node is not found</li>");
								}
							}
						} else {
							log.debug("<li>DrawerComponent HeaderElement not found</li>");
						}
					} else {
						log.debug("<li>DrawerComponent HeaderElements not found</li>");
					}


				} catch (Exception e) {
					log.debug("<li>Unable to update drawers_container component."+e+"</li>");
				}

				// end set drawers_container component content.
				// --------------------------------------------------------------------------------------------------------------------------

				// ---------------------------------------------------------------------------------------------------------------------------------------
				// start of html blob components content.
				try {
					String html = "";
					Elements iconBlockElements = doc.select("div.icon-block");
					//				boolean test = doc.select("div.c23-pilot").contains("div.icon-block");
					if (iconBlockElements.isEmpty()) {
						iconBlockElements = doc.select("div.poly");
						if (iconBlockElements != null) {
							//Element htmlblobElement = iconBlockElements.first();
							for (Element htmlblobElement : iconBlockElements) {
								if (htmlblobElement.hasClass("c47-pilot")) {
									continue;
								}
								if (htmlblobElement != null) {
									Elements ulElements = htmlblobElement.getElementsByTag("ul");
									if (ulElements.size() > 0) {
										html = htmlblobElement.outerHtml();
									}
								} 
							}
						} else {
							sb.append("<li>htmlblob component not found on publisher page </li>");
						}
					}
					else {
						if (iconBlockElements != null) {
							//Element htmlblobElement = iconBlockElements.first();
							for (Element htmlblobElement : iconBlockElements) {
								if (htmlblobElement != null) {
									html = htmlblobElement.outerHtml();
									if (htmlblobElement.getElementsByTag("ul").size() > 0) {
									}
									else {
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
									}

								} else {
									log.debug("<li>htmlblob/icon-block Element section not found</li>");
								}
							}
						} 
					}
					//				Elements iconBlockElements = doc.select("div.icon-block, div.poly");

					if (indexLowerRightNode.hasNode("htmlblob")) {
						javax.jcr.Node htmlBlobNode = indexLowerRightNode.getNode("htmlblob");
						log.debug("htmlblobElement.outerHtml() " + html + "\n");
						if (StringUtils.isNotBlank(html)) {
							htmlBlobNode.setProperty("html", html);
						} else {
							sb.append("<li>htmlblob content doesn't exist</li>");
						}

					} else {
						log.debug("htmlblob component not present at " + indexLowerRightNode.getPath());
					}

				} catch (Exception e) {
					log.debug("<li>Unable to update html blob component."+e+"</li>");
				}
				// end set html blob component content.
				// --------------------------------------------------------------------------------------------------------------------------
				// --------------------------------------------------------------------------------------------------------------------------
				// start of tile bordered components.

				try {

					Elements rightRail = doc.select("div.c23-pilot,div.cc23-pilot");
					if(rightRail != null){
						log.debug("rightRail size" + rightRail.size());

						if(rightRail.size()>0){

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
									log.debug("found ul elements in c23-pilot so skipping");
									continue;
								}
								count = count + 1;
								javax.jcr.Node rightRailNode = null;
								String title = ele.getElementsByTag("h2")!=null?ele.getElementsByTag("h2").text():"";
								if(StringUtils.isBlank(title)){
									title = ele.getElementsByTag("h3")!=null?ele.getElementsByTag("h3").text():"";
								}
								String desc = ele.getElementsByTag("p")!=null?ele.getElementsByTag("p").text():"";
								Elements anchor = ele.getElementsByTag("a");

								String textAfterAnchorTag = ele.ownText();
								if(StringUtils.isNotBlank(textAfterAnchorTag)){
									flag = true;
									countOfTileBorderedElements++;
								}
								String anchorText = anchor!=null?anchor.text():"";
								String anchorHref = anchor!=null?anchor.attr("href"):"";

								if (titleBorderNodes.hasNext()) {
									rightRailNode = (Node)titleBorderNodes.next();
								} else {
									log.debug("<li>all tile_boredered components are migrated</li>");
								}

								if (rightRailNode != null) {
									if(title != null && title != "" && desc != null && desc != "" && anchorText != null && anchorText != ""){
										rightRailNode.setProperty("title", title);
										rightRailNode.setProperty("description", desc);
										rightRailNode.setProperty("linktext", anchorText);
										rightRailNode.setProperty("linkurl", anchorHref);
										log.debug("title, description, linktext and linkurl are created at "+rightRailNode.getPath());
									}else{
										log.debug("<li>Content miss match for "+"</li>");
									}
								}else{
									log.debug("<li>one of title_bordered node doesn't exist in node structure.</li>");
								}
							}
							if(flag){
								sb.append("<li>Extra Text found after link on locale page for "+ countOfTileBorderedElements +" TileBordered Component(s) , hence the text cannot be migrated.</li>");
							}
							if (count != indexLowerRightNode.getNodes("tile_bordered*").getSize()) {
								sb.append("<li>Mis-Match in tilebordered Panels count."+count+" is not equal "+indexLowerRightNode.getNodes("tile_bordered*").getSize()+"</li>");
							}
						}else{
							log.debug("<li>No Content with class 'c23-pilot or cc23-pilot' found</li>");
						}
					}else{
						sb.append("<li>tile bordered component not present in the web publisher page</li>");
					}
				} catch (Exception e) {
					log.debug("Exception ",e);
					log.debug("<li>Unable to update tile_bordered component.\n</li>");
				}
				// End of tile bordered components.
				// -----------------------------------------------------------------------------------------------------

				session.save();
			}
			else {
				sb.append(Constants.URL_CONNECTION_EXCEPTION);
			}

		} catch (Exception e) {
			log.debug("Exception ", e);
		}

		sb.append("</ul></td>");

		return sb.toString();
	}
}
