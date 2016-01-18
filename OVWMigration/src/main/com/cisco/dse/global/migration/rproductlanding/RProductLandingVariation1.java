package com.cisco.dse.global.migration.rproductlanding;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

public class RProductLandingVariation1 extends BaseAction {

	Document doc;

	String title = null;

	StringBuilder sb = new StringBuilder(1024);

	static Logger log = Logger.getLogger(RProductLandingVariation1.class);

	public String translate(String host, String loc, String prod, String type,
			String catType, String locale, Session session,Map<String,String> urlMap) throws IOException,
			ValueFormatException, VersionException, LockException,
			ConstraintViolationException, RepositoryException {
		BasicConfigurator.configure();
		log.debug("In the translate method");
		log.debug("In the translate method, catType is :" + catType);

		// Repo node paths
		String pagePropertiesPath = "/content/<locale>/" + catType
				+ "/<prod>/index/jcr:content";
		String indexUpperLeft = "/content/<locale>/"
				+ catType
				+ "/<prod>/index/jcr:content/Grid/category/layout-category/widenarrow/WN-Narrow-2";
		String indexUpperRight = "/content/<locale>/"
				+ catType
				+ "/<prod>/index/jcr:content/Grid/category/layout-category/widenarrow/WN-Wide-1/carousel";
		String indexLowerLeft = "/content/<locale>/"
				+ catType
				+ "/<prod>/index/jcr:content/Grid/category/layout-category/widenarrow/WN-Wide-1";
		String indexLowerRight = "/content/<locale>/"
				+ catType
				+ "/<prod>/index/jcr:content/Grid/category/layout-category/widenarrow/WN-Narrow-2";

		String indexUpperLft = "/content/<locale>/"
				+ catType
				+ "/<prod>/index/jcr:content/Grid/category/layout-category/widenarrow_0/WN-Narrow-2";
		String indexUpperRght = "/content/<locale>/"
				+ catType
				+ "/<prod>/index/jcr:content/Grid/category/layout-category/widenarrow_0/WN-Wide-1/carousel";
		String indexLowerLft = "/content/<locale>/"
				+ catType
				+ "/<prod>/index/jcr:content/Grid/category/layout-category/widenarrow_0/WN-Wide-1";
		String indexLowerRght = "/content/<locale>/"
				+ catType
				+ "/<prod>/index/jcr:content/Grid/category/layout-category/widenarrow_0/WN-Narrow-2";

		String pageUrl = host + "/content/<locale>/" + catType
				+ "/<prod>/index.html";

		pageUrl = pageUrl.replace("<locale>", locale).replace("<prod>", prod);
		pagePropertiesPath = pagePropertiesPath.replace("<locale>", locale)
				.replace("<prod>", prod);

		sb.append("<td>" + "<a href=" + pageUrl + ">" + pageUrl + "</a>"
				+ "</td>");
		sb.append("<td>" + "<a href=" + loc + ">" + loc + "</a>" + "</td>");
		sb.append("<td><ul>");

		indexUpperLeft = indexUpperLeft.replace("<locale>", locale).replace(
				"<prod>", prod);
		indexUpperRight = indexUpperRight.replace("<locale>", locale).replace(
				"<prod>", prod);
		indexLowerLeft = indexLowerLeft.replace("<locale>", locale).replace(
				"<prod>", prod);
		indexLowerRight = indexLowerRight.replace("<locale>", locale).replace(
				"<prod>", prod);

		indexUpperLft = indexUpperLft.replace("<locale>", locale).replace(
				"<prod>", prod);
		indexUpperRght = indexUpperRght.replace("<locale>", locale).replace(
				"<prod>", prod);
		indexLowerLft = indexLowerLft.replace("<locale>", locale).replace(
				"<prod>", prod);
		indexLowerRght = indexLowerRght.replace("<locale>", locale).replace(
				"<prod>", prod);

		Node indexUpperLeftNode = null;
		Node indexUpperRightNode = null;
		Node indexLowerLeftNode = null;
		Node indexLowerRightNode = null;
		Node pageJcrNode = null;
		try {
			indexUpperLeftNode = session.nodeExists(indexUpperLeft) ? session
					.getNode(indexUpperLeft) : session.getNode(indexUpperLft);
			indexUpperRightNode = session.nodeExists(indexUpperRight) ? session
					.getNode(indexUpperRight) : session.getNode(indexUpperRght);
			indexLowerLeftNode = session.nodeExists(indexLowerLeft) ? session
					.getNode(indexLowerLeft) : session.getNode(indexLowerLft);
			indexLowerRightNode = session.nodeExists(indexLowerRight) ? session
					.getNode(indexLowerRight) : session.getNode(indexLowerRght);
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

				// ------------------------------------------------------------------------------------------------------------------------------------------
				// start set primary CTA content.
				String primaryCTATitle = "";
				String primaryCTADescription = "";
				String primaryCTALinkText = "";
				String primaryCTALinkUrl = "";
				try {
					Elements primaryCTAElements = doc.select("div.c47-pilot,div.ovt-in");
					if (primaryCTAElements != null) {
						Element primaryCTAElement = doc.select("div.c47-pilot,div.ovt-in")
								.first();
						if (primaryCTAElement != null) {
							Elements titleElements = primaryCTAElement
									.getElementsByTag("h3");
							if(titleElements.isEmpty()){
								titleElements = primaryCTAElement
										.getElementsByTag("h2");
							}
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
							Elements paraElements = primaryCTAElement
									.getElementsByTag("p");
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
							Elements ctaLinksElements = primaryCTAElement
									.select("ul.cta-links");
							Elements ctaOvtLinkElements = doc.select("div.ovt-in");
							if (!ctaLinksElements.isEmpty()) {
								Elements ctaLiElements = ctaLinksElements
										.select("li.cta");
								if (ctaLiElements != null) {
									Element ctaLiElement = ctaLiElements
											.first();
									if (ctaLiElement != null) {
										Elements anchorElements = ctaLiElement
												.getElementsByTag("a");
										if (anchorElements != null) {
											Element anchorElement = anchorElements
													.first();
											if (anchorElement != null) {
												primaryCTALinkText = anchorElement
														.text();
												primaryCTALinkUrl = anchorElement
														.absUrl("href");
												log.debug("primaryCTALinkUrl" + primaryCTALinkUrl + "\n");
												// Start extracting valid href
												log.debug("Before primaryCTALinkUrl" + primaryCTALinkUrl + "\n");
												primaryCTALinkUrl = FrameworkUtils.getLocaleReference(primaryCTALinkUrl, urlMap);
												log.debug("after primaryCTALinkUrl" + primaryCTALinkUrl + "\n");
												// End extracting valid href
									
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
							} else if(!ctaOvtLinkElements.isEmpty()){
								Element ctaOvtLinkElement = ctaOvtLinkElements.last();
								if(ctaOvtLinkElement != null){
								Element anchorOvtElement = ctaOvtLinkElement.getElementsByTag("a").last();
								if(anchorOvtElement != null){
									primaryCTALinkText = anchorOvtElement
											.text();
									primaryCTALinkUrl = anchorOvtElement
											.absUrl("href");
									log.debug("primaryCTALinkUrl" + primaryCTALinkText + "\n");
									// Start extracting valid href
									log.debug("Before primaryCTALinkUrl" + primaryCTALinkUrl + "\n");
									primaryCTALinkUrl = FrameworkUtils.getLocaleReference(primaryCTALinkUrl, urlMap);
									log.debug("after primaryCTALinkUrl" + primaryCTALinkUrl + "\n");
									// End extracting valid href
								}else {
									log.debug("<li>Primary CTA Link anchor tag not found </li>");
								}
								}else {
									log.debug("<li>Primary CTA Link anchor tag not found </li>");
								}
							}else {
								log.debug("<li>Primary CTA Links section not found </li>");
							}
							log.debug("primaryCTATitle" + primaryCTATitle
									+ "\n");
							log.debug("primaryCTADescription"
									+ primaryCTADescription + "\n");
							log.debug("primaryCTALinkText" + primaryCTALinkText
									+ "\n");
							log.debug("primaryCTALinkUrl" + primaryCTALinkUrl
									+ "\n");
							if (indexUpperLeftNode.hasNode("hero_tile")) {
								javax.jcr.Node primartCTANode = indexUpperLeftNode
										.getNode("hero_tile");
								if (StringUtils.isNotBlank(primaryCTATitle)) {
									primartCTANode.setProperty("title",
											primaryCTATitle);
								} else {
									sb.append(Constants.PRIMARY_CTA_TITLE_NOT_FOUND);
									log.debug("title property is not set at "
											+ primartCTANode.getPath());
								}
								if (StringUtils
										.isNotBlank(primaryCTADescription)) {
									primartCTANode.setProperty("description",
											primaryCTADescription);
								} else {
									sb.append(Constants.PRIMARY_CTA_DESCRIPTION_NOT_FOUND);
									log.debug("description property is not set at "
											+ primartCTANode.getPath());
								}
								if (StringUtils.isNotBlank(primaryCTALinkText)) {
									primartCTANode.setProperty("linktext",
											primaryCTALinkText);
								} else {
									sb.append(Constants.PRIMARY_CTA_LINKTEXT_NOT_FOUND);
									log.debug("linktext property is not set at "
											+ primartCTANode.getPath());
								}

								if (primartCTANode.hasNode("cta")) {
									javax.jcr.Node primartCTALinkUrlNode = primartCTANode
											.getNode("cta");
									if (StringUtils
											.isNotBlank(primaryCTALinkUrl)) {
										primartCTALinkUrlNode.setProperty(
												"url", primaryCTALinkUrl);
									} else {
										sb.append(Constants.PRIMARY_CTA_LINKURL_NOT_FOUND);
										log.debug("url property is not set at "
												+ primartCTALinkUrlNode
														.getPath());
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
						sb.append(Constants.PRIMARY_CTA_COMPONENT_INWEB_NOT_FOUND);
					}
				} catch (Exception e) {
					log.debug("<li>Unable to update primary_cta_v2 component."
							+ e + "</li>");
				}

				// end set primary CTA content.
				// ---------------------------------------------------------------------------------------------------------------------------------------
				// ------------------------------------------------------------------------------------------------------------------------------------------
				// start set Hero Large component content.
				try {
					javax.jcr.Node heroLargeNode = null;
					NodeIterator heroPanelIterator = null;
					if (indexUpperRightNode.hasNode("carouselContents")) {
						heroLargeNode = indexUpperRightNode
								.getNode("carouselContents");
						if (heroLargeNode.hasNodes()) {
							heroPanelIterator = heroLargeNode
									.getNodes("hero_panel*");
						}
					} else {
						log.debug("<li>Node with name 'carouselContents' doesn't exist under "
								+ indexUpperRightNode.getPath() + "</li>");
					}

					Elements heroLargeElements = doc.select("div.c50-pilot");
					if (heroLargeElements != null) {
						Element heroLargeElement = heroLargeElements.first();
						if (heroLargeElement != null) {
							Elements heroLargeFrameElements = heroLargeElement
									.select("div.frame");
							javax.jcr.Node heroPanelNode = null;

							if (heroLargeFrameElements != null) {
								if (heroLargeFrameElements.size() != heroLargeNode
										.getNodes("hero_panel*").getSize()) {
									sb.append(Constants.MISMATCH_IN_HERO_SLIDES);
								}
								for (Element ele : heroLargeFrameElements) {
									String heroPanelTitle = "";
									String heroPanelDescription = "";
									String heroPanelLinkText = "";
									String heroPanellinkUrl = "";
									Elements heroTitleElements = ele
											.getElementsByTag("h2");
									if (heroTitleElements != null) {
										Element heroTitleElement = heroTitleElements
												.first();
										if (heroTitleElement != null) {
											heroPanelTitle = heroTitleElement
													.text();
										} else {
											log.debug("<li>Hero Panel element not having any title in it </li>");
										}
									} else {
										log.debug("<li>Hero Panel title element is not found</li>");
									}
									Elements heroDescriptionElements = ele
											.getElementsByTag("p");
									if (heroDescriptionElements != null) {
										Element heroDescriptionElement = heroDescriptionElements
												.first();
										if (heroDescriptionElement != null) {
											heroPanelDescription = heroDescriptionElement
													.text();
										} else {
											log.debug("<li>Hero Panel element not having any description in it </li>");
										}
									} else {
										log.debug("<li>Hero Panel para element not found </li>");
									}
									Elements heroPanelLinkTextElements = ele
											.getElementsByTag("b");
									if (heroPanelLinkTextElements != null) {
										Element heroPanelLinkTextElement = heroPanelLinkTextElements
												.first();
										if (heroPanelLinkTextElement != null) {
											heroPanelLinkText = heroPanelLinkTextElement
													.text();
										} else {
											log.debug("<li>Hero Panel element not having any linktext in it </li>");
										}
									} else {
										log.debug("<li>Hero Panel linktext element not found  </li>");
									}
									Elements heroPanelLinkUrlElements = ele
											.getElementsByTag("a");
									if (heroPanelLinkUrlElements != null) {
										Element heroPanelLinkUrlElement = heroPanelLinkUrlElements
												.first();
										if (heroPanelLinkUrlElement != null) {
											heroPanellinkUrl = heroPanelLinkUrlElement
													.absUrl("href");
											if (StringUtils.isBlank(heroPanellinkUrl)) {
												heroPanellinkUrl = heroPanelLinkUrlElement
														.attr("href");
											}
											// Start extracting valid href
											log.debug("Before heroPanellinkUrl" + heroPanellinkUrl + "\n");
											heroPanellinkUrl = FrameworkUtils.getLocaleReference(heroPanellinkUrl, urlMap);
											log.debug("after heroPanellinkUrl" + heroPanellinkUrl + "\n");
											// End extracting valid href
									
										} else {
											log.debug("<li>Hero Panel element not having any linkurl in it </li>");
										}
										Element ctaLinkElement = ele.select("p.cta-link").first(); 
										Elements ctaLinkAnchorElements = ctaLinkElement
												.getElementsByTag("a");
										log.debug("ctaLinkAnchorElements.size() :::::: " + ctaLinkAnchorElements.size());
										if (ctaLinkAnchorElements.size() > 1) {
											sb.append("<li>Some extra text found in hero panel </li>");
										}
									} else {
										log.debug("<li>Hero Panel link url element not found </li>");
									}
									// start image
									String heroImage = FrameworkUtils
											.extractImagePath(ele, sb);
									log.debug("heroImage " + heroImage + "\n");
									// end image
									log.debug("heroPanelTitle "
											+ heroPanelTitle + "\n");
									log.debug("heroPanelDescription "
											+ heroPanelDescription + "\n");
									log.debug("heroPanelLinkText "
											+ heroPanelLinkText + "\n");
									log.debug("heroPanellinkUrl "
											+ heroPanellinkUrl + "\n");
									if (heroPanelIterator != null
											&& heroPanelIterator.hasNext())
										heroPanelNode = heroPanelIterator
												.nextNode();
									if (heroPanelNode != null) {
										if (StringUtils
												.isNotBlank(heroPanelTitle)) {
											heroPanelNode.setProperty("title",
													heroPanelTitle);
										} else {
											sb.append(Constants.HERO_SLIDE_TITLE_NOT_FOUND);
										}
										if (StringUtils
												.isNotBlank(heroPanelDescription)) {
											heroPanelNode.setProperty(
													"description",
													heroPanelDescription);
										} else {
											sb.append(Constants.HERO_SLIDE_DESCRIPTION_NOT_FOUND);
										}
										if (StringUtils
												.isNotBlank(heroPanelLinkText)) {
											heroPanelNode.setProperty(
													"linktext",
													heroPanelLinkText);
										} else {
											sb.append(Constants.HERO_SLIDE_LINKTEXT_NOT_FOUND);
										}
										if (StringUtils
												.isNotBlank(heroPanellinkUrl)) {
											heroPanelNode
													.setProperty("linkurl",
															heroPanellinkUrl);
										} else {
											sb.append(Constants.HERO_SLIDE_LINKURL_NOT_FOUND);
										}
										if (heroPanelNode.hasNode("image")) {
											Node imageNode = heroPanelNode
													.getNode("image");
											String fileReference = imageNode
													.hasProperty("fileReference") ? imageNode
													.getProperty(
															"fileReference")
													.getString() : "";
											heroImage = FrameworkUtils
													.migrateDAMContent(
															heroImage,
															fileReference,
															locale, sb);
											log.debug("heroImage " + heroImage
													+ "\n");
											if (StringUtils
													.isNotBlank(heroImage)) {
												imageNode.setProperty(
														"fileReference",
														heroImage);
											}
										} else {
											sb.append(Constants.HERO_SLIDE_IMAGE_NODE_NOT_FOUND);
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
						sb.append(Constants.HERO_LARGE_COMPONENT_NOT_FOUND);
					}
				} catch (Exception e) {
					sb.append("<li>Unable to update hero_large component." + e
							+ "</li>");
				}

				// end set Hero Large content.
				// ---------------------------------------------------------------------------------------------------------------------------------------
				// ------------------------------------------------------------------------------------------------------------------------------------------
				// start set drawers_container component content.
				try {
					javax.jcr.Node drawersContainerNode = null;
					Elements drawerComponentHeaderElements = doc
							.select("div.c00-pilot,div.c100-pilot");
					if (drawerComponentHeaderElements != null
							&& !drawerComponentHeaderElements.isEmpty()) {
						Element drawerComponentHeader = drawerComponentHeaderElements
								.first();
						if (drawerComponentHeader != null) {
							String drawerComponentHeaderTitle = "";
							Elements drawerComponentHeaderTitleElements = drawerComponentHeader
									.getElementsByTag("h2");

							if (drawerComponentHeaderTitleElements != null) {
								Element drawerComponentHeaderTitleElement = drawerComponentHeaderTitleElements
										.first();
								if (drawerComponentHeaderTitleElement != null) {
									drawerComponentHeaderTitle = drawerComponentHeaderTitleElement
											.text();
								} else {
									log.debug("<li>drawerComponent Header Title Element not found</li>");
								}
							} else {
								log.debug("<li>drawerComponent Header Title Element Section not found</li>");
							}
							if (indexLowerLeftNode.hasNodes()) {
								NodeIterator drawersContainerIterator = indexLowerLeftNode
										.getNodes("header*");

								if (drawersContainerIterator.hasNext()) {
									drawersContainerNode = drawersContainerIterator
											.nextNode();
									log.debug("drawerComponentHeaderTitle "
											+ drawerComponentHeaderTitle + "\n");
									if (StringUtils
											.isNotBlank(drawerComponentHeaderTitle)) {
										drawersContainerNode.setProperty(
												"title",
												drawerComponentHeaderTitle);
									} else {
										sb.append(Constants.DRAWERCONTAINER_TITLE_NOT_FOUND);
									}
									Elements hTextElements = doc
											.getElementsByAttribute("data-config-hidetext");
									if (hTextElements != null
											&& hTextElements.size() > 0) {
										Element hText = hTextElements.first();
										if (hText != null) {
											System.out
													.println("Hide TEXT:::::::::"
															+ hText == null ? "NULL"
															: "NOT NULL"
																	+ hText.attr("data-config-hidetext"));

											System.out
													.println("Show TEXT:::::::::"
															+ hText == null ? "NULL"
															: "NOT NULL"
																	+ hText.attr("data-config-showtext"));
											drawersContainerNode
													.setProperty(
															"closetext",
															hText.attr("data-config-hidetext"));
											drawersContainerNode
													.setProperty(
															"opentext",
															hText.attr("data-config-showtext"));
										} else {
											log.debug("<li>data-config-hidetext not found</li>");
										}

									} else {
										sb.append(Constants.SHOW_HIDE_LINKS_NOT_FOUND);
									}
									Element drawerHeaderLinksElement = drawerComponentHeader
											.select("div.clearfix").first();
									JSONObject jsonObj = new JSONObject();
									String anchorText = "";
									String anchorHref = "";
									NodeIterator linkItemIterator = null;
									if (drawerHeaderLinksElement != null) {
										Elements anchorElements = drawerHeaderLinksElement
												.getElementsByTag("a");
										Node linkNode = drawersContainerNode
												.hasNode("links") ? drawersContainerNode
												.getNode("links") : null;
										if (linkNode != null) {
											linkItemIterator = linkNode
													.hasNode("item_1") ? linkNode
													.getNodes("item*") : null;
										}
										int linkElementSize = anchorElements.size();
										int linkNodeSize = (int)linkItemIterator.getSize();
										if(linkElementSize!=linkNodeSize){
											sb.append("<li>Mis-match of links above drawerpanels. Elements are ("+linkElementSize+") and nodes are ("+linkNodeSize+").</li>");
										}
										Node itemNode = null;
										Node linkItemNode = null;
										for (Element aElement : anchorElements) {
											if (aElement != null) {
												anchorText = aElement.text();
												anchorHref = aElement
														.absUrl("href");
												// Start extracting valid href
												log.debug("Before anchorHref " + anchorHref + "\n");
												anchorHref = FrameworkUtils.getLocaleReference(anchorHref, urlMap);
												log.debug("after anchorHref" + anchorHref + "\n");
												// End extracting valid href
												if (linkItemIterator.hasNext()) {
													itemNode = linkItemIterator
															.nextNode();
													linkItemNode = itemNode
															.hasNode("link") ? itemNode
															.getNode("link")
															: null;
													linkItemNode.setProperty(
															"linktext",
															anchorText);
													linkItemNode.setProperty(
															"url", anchorHref);
												} else {
//													sb.append(Constants.DRAWERCONTAINER_LINK_NOT_FOUND);
												}
											} else {
												log.debug("<li>drawerComponent link Element section not found</li>");
											}

										}

									}
								}

								if (indexLowerLeftNode.hasNodes()) {
									NodeIterator drawerPanelsIterator = indexLowerLeftNode
											.getNodes("container*");
									
									javax.jcr.Node drawersPanelNode = null;
									Elements drawersPanelElements = doc
											.select("div.n21,ul.n21");

									if (drawersPanelElements != null) {
										// Element drawersPanelElement =
										// drawersPanelElements.first();
										// start new code
										int count = 0;
										for (Element drawersPanelElement : drawersPanelElements) {
											Elements drawerPanelLiElements = drawersPanelElement
													.getElementsByTag("li");
											boolean infoLinksMisMatchFlag = false;
											boolean linkUrlNotFoundFlag = false;
											boolean imageSrcNotFoundFlag = false;
											boolean subdrawerTitleNotFoundFlag = false;
											boolean subdrawerHighlightsNotFoundFlag = false;
											if (drawerPanelLiElements != null) {
												for (Element drawerPanelLiElement : drawerPanelLiElements) {
													String panelTitle = "";
													boolean misMatchFlag = true;
													Elements iconBlock = drawerPanelLiElement
															.select("div.series");
													if (iconBlock.size() == 0) {
														log.debug("SERIES SIZE0");
														continue;
													}
													count = count + 1;
													if (drawerPanelsIterator
															.hasNext()) {

														drawersPanelNode = drawerPanelsIterator
																.nextNode();
													}
													Elements seriesElements = drawerPanelLiElement
															.select("div.series");
													if (seriesElements != null) {
														Element seriesElement = seriesElements
																.first();
														
														String linkUrl = "";
														String panelDescription = "";
														if (seriesElement != null) {
															Elements panelTitleElements = seriesElement
																	.getElementsByTag("h3");
															if (panelTitleElements != null) {
																Element panelTitleElement = panelTitleElements
																		.first();

																if (panelTitleElement != null) {
																	Elements anchorTag = panelTitleElement
																			.getElementsByTag("a");

																	if (anchorTag
																			.size() > 0) {
																		panelTitle = anchorTag
																				.first()
																				.text();
																		linkUrl = anchorTag
																				.first()
																				.absUrl("href");
																		// Start extracting valid href
																		log.debug("Before linkUrl" + linkUrl + "\n");
																		linkUrl = FrameworkUtils.getLocaleReference(linkUrl, urlMap);
																		log.debug("after linkUrl" + linkUrl + "\n");
																		// End extracting valid href
																	}
																	if (StringUtils
																			.isBlank(panelTitle)) {
																		panelTitle = panelTitleElement
																				.text();
																	}
																	log.debug("panel title"
																			+ panelTitle);
																} else {
																	log.debug("<li>drawer panel anchor element not found</li>");
																}
															} else {
																log.debug("<li>drawer panel title element not found</li>");
															}
															Elements panelParaElements = seriesElement
																	.getElementsByTag("p");
															if (panelParaElements != null) {
																Element panelDescriptionElement = panelParaElements
																		.first();
																panelDescription = panelDescriptionElement
																		.text();
															} else {
																log.debug("<li>drawer panel para element not found</li>");
															}
															// start image
															String drawerImage = FrameworkUtils.extractImagePath(seriesElement, sb);
															log.debug("drawerImage " + drawerImage);
															if (drawersPanelNode != null) {
																Node drawersImageNode = drawersPanelNode.getNode("image");
																String fileReference = drawersImageNode.hasProperty("fileReference") ? drawersImageNode.getProperty("fileReference").getString() : "";
																drawerImage = FrameworkUtils.migrateDAMContent(drawerImage, fileReference, locale, sb);
																log.debug("drawerImage " + drawerImage);
																if (StringUtils.isNotBlank(drawerImage)) {
																	drawersImageNode.setProperty("fileReference", drawerImage);
																}else{
																	imageSrcNotFoundFlag = true;
																}
															}
															// end image
															if (drawersPanelNode != null) {
																Node drawersOverviewNode = drawersPanelNode.hasNode("overview")? drawersPanelNode.getNode("overview"):null;
																if (StringUtils.isNotBlank(panelTitle)) {
																	drawersPanelNode.setProperty("title", panelTitle);
																	drawersOverviewNode.setProperty("linktext", panelTitle);
																} else {
																	sb.append(Constants.DRAWER_PANEL_TITLE_NOT_FOUND);
																}
																if (StringUtils.isNotBlank(linkUrl)) {
																	drawersPanelNode.setProperty("linkUrl", linkUrl);
																	drawersOverviewNode.setProperty("url", linkUrl);
																} 
																if (StringUtils.isNotBlank(panelDescription)) {
																	drawersPanelNode.setProperty("description", panelDescription);
																} else {
																	sb.append(Constants.DRAWER_PANEL_DESC_NOT_FOUND);
																}
															}
														}
													}

													// selecting sub drawer
													// elements from document
													NodeIterator subDrawerIterator = drawersPanelNode.getNode("subdrawer_parsys").getNodes("content_product*");
													javax.jcr.Node subdrawerpanel = null;
													Elements subDrawerColl = drawerPanelLiElement.select("ul.items");
													Elements clearfixdivs = drawerPanelLiElement.select("li.clearfix");
													for(Element cFix : clearfixdivs){
														Element div = cFix.getElementsByTag("div").first();
														if(div.hasAttr("align")){
															sb.append(Constants.EXTRA_LINK_IN_SUBDRAWER);
														}
													}

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
																List<String> list3 = new ArrayList<String>();
																if (subDrawerIterator.hasNext()) {
																	subdrawerpanel = subDrawerIterator.nextNode();
																}
																if (subItem != null) {

																	Elements siTitles = subItem.getElementsByTag("h4");
																	if (siTitles != null) {
																		Element siTitle = siTitles.first();
																		if (siTitle != null) {
																			Elements siATitles = siTitle.getElementsByTag("a");
																			Element span =siTitle.getElementsByTag("span").first();
																			if (siATitles != null) {
																				Element siATitle = siATitles
																						.first();
																				if (siATitle != null) {
																					log.debug("Sub Series Title:::::::::::::"
																							+ siATitle
																									.text());
																					log.debug("siATitle.text() "
																							+ siATitle
																									.text()
																							+ "\n");
																					log.debug("siATitle.text() "
																							+ siATitle
																									.attr("href")
																							+ "\n");
																					title = siATitle
																							.text();
																					if(span!=null){
																						title = title+" "+span.outerHtml();
																					}
																					linkTitleUrl = siATitle
																							.absUrl("href");
																					// Start extracting valid href
																					log.debug("Before linkTitleUrl" + linkTitleUrl + "\n");
																					linkTitleUrl = FrameworkUtils.getLocaleReference(linkTitleUrl, urlMap);
																					log.debug("after linkTitleUrl" + linkTitleUrl + "\n");
																					// End extracting valid href
																				} else {
																					title = siTitle
																							.text();
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
																	// start
																	// image
																	String subDrawerImage = FrameworkUtils
																			.extractImagePath(
																					subItem,
																					sb);
																	log.debug("subDrawerImage before migration : "
																			+ subDrawerImage
																			+ "\n");
																	if (subdrawerpanel != null) {
																		Node subDrawersImageNode = subdrawerpanel
																				.getNode("image");
																		String fileReference = subDrawersImageNode
																				.hasProperty("fileReference") ? subDrawersImageNode
																				.getProperty(
																						"fileReference")
																				.getString()
																				: "";
																		subDrawerImage = FrameworkUtils
																				.migrateDAMContent(
																						subDrawerImage,
																						fileReference,
																						locale,
																						sb);
																		log.debug("subDrawerImage after migration : "
																				+ subDrawerImage
																				+ "\n");
																		if (StringUtils
																				.isNotBlank(subDrawerImage)) {
																			subDrawersImageNode
																					.setProperty(
																							"fileReference",
																							subDrawerImage);
																		}else{
																			imageSrcNotFoundFlag = true;
																		}
																	}
																	// end image

																	Elements indDetailsElements = subItem
																			.select("ul.details");
																	if (indDetailsElements != null) {
																		Element indDetailsElement = indDetailsElements
																				.first();
																		if (indDetailsElement != null) {
																			Elements indItems = indDetailsElement
																					.getElementsByTag("li");
																			if (indItems != null) {
																				for (Element indItem : indItems) {
																					list1.add(indItem
																							.html());
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

																	Element subItemUlInfoLink = subItem
																			.siblingElements()
																			.first(); // subItemUlInfoLinks.first();
																	log.debug("Info Links Elements -----------"
																			+ subItemUlInfoLink);
																	log.debug("--------------------------------");
																	if (subItemUlInfoLink != null) {
																		Elements subItemInfoLinks = subItemUlInfoLink
																				.getElementsByTag("li");

																		for (Element si : subItemInfoLinks) {
																			JSONObject jsonObj = new JSONObject();
																			System.out
																					.println("\t\t FeatureSubInfoLinks Text :::::::::::::::"
																							+ si.text());
																			log.debug("\t\t FeatureSubInfoLinks Text :::::::::::::::"
																					+ si.text());

																			String linkText = "";
																			String linkTextUrl = "";
																			boolean ownTextCheck = false;
																											Elements linkTextElements = si
																													.getElementsByTag("a");
																											System.out
																													.println("ownnn text" + si.ownText());
																											if (linkTextElements != null) {
																												Element linkTextElement = linkTextElements
																														.first();
																												if (linkTextElement != null) {
																													linkText = linkTextElement
																															.text();
																													linkTextUrl = linkTextElement
																															.absUrl("href");
																													// Start extracting valid href
																													log.debug("Before linkTextUrl" + linkTextUrl + "\n");
																													linkTextUrl = FrameworkUtils.getLocaleReference(linkTextUrl, urlMap);
																													log.debug("after linkTextUrl" + linkTextUrl + "\n");
																													// End extracting valid href
																													ownTextCheck = false;
																													if(StringUtils.isNotBlank(si.ownText())) {
																														linkText = linkText + si.ownText();
																													}
																												} else if(!si.ownText().isEmpty()){
																													System.out
																															.println("intoooo ownnn text");
																													linkText = si.ownText();
																													linkTextUrl = "";
																													ownTextCheck = true;
																												}else {
																													log.debug("<li>info links anchor element not found</li>");
																												}
																											
																			} else {
																				log.debug("<li>info links anchor element section not found</li>");
																			}
																			if (StringUtils
																					.isNotBlank(linkText)) {
																				jsonObj.put(
																						"linktext",
																						linkText);
																				list2.add(linkText);
																			}
																			if (StringUtils
																					.isNotBlank(linkTextUrl)) {
																				list3.add(linkTextUrl);

																			}else if(ownTextCheck){
																												list3.add(linkTextUrl);
																												linkUrlNotFoundFlag = true;
																											}
																		}
																		log.debug("list2.size()"
																				+ list2.size());

																	}

																}
																if (subdrawerpanel != null) {
																	if (StringUtils
																			.isNotBlank(title)) {
																		subdrawerpanel
																				.setProperty(
																						"title",
																						title);
																	} else {
																		subdrawerTitleNotFoundFlag = true;
																	}
																	if (StringUtils
																			.isNotBlank(linkTitleUrl)) {
																		subdrawerpanel
																				.setProperty(
																						"linkTitleUrl",
																						linkTitleUrl);
																	} else {
																		linkUrlNotFoundFlag = true;
																		log.debug("linkurl property is not set at "
																				+ subdrawerpanel
																						.getPath());
																	}
																	if (list1
																			.size() > 0) {

																		if (subdrawerpanel
																				.hasProperty("description")) {
																			Property p = subdrawerpanel
																					.getProperty("description");
																			p.remove();
																			session.save();
																		}
																		subdrawerpanel
																				.setProperty(
																						"description",
																						list1.toArray(new String[list1
																								.size()]));

																	} else {
																		subdrawerHighlightsNotFoundFlag = true;
																	}
																	NodeIterator subdraweritems = null;
																	if (list2
																			.size() > 0) {
																		
																		Node subdrawerlinks = subdrawerpanel
																				.hasNode("links") ? subdrawerpanel
																				.getNode("links")
																				: null;

																		if (subdrawerlinks != null) {
																			subdraweritems = subdrawerlinks
																					.hasNodes() ? subdrawerlinks
																					.getNodes("item*")
																					: null;
																			for (int loop = 0; loop < list2
																					.size(); loop++) {
																				log.debug("Loop "
																						+ loop);
																				if (subdraweritems
																						.hasNext()) {
																					Node subDrawerinfo = subdraweritems
																							.nextNode();
																					Node subDrawerItemLink = subDrawerinfo
																							.hasNode("link") ? subDrawerinfo
																							.getNode("link")
																							: null;
																					if (subDrawerItemLink
																							.hasProperty("linktext")) {
																						Property p = subDrawerItemLink
																								.getProperty("linktext");
																						p.remove();
																						session.save();
																					}
																					if (subDrawerItemLink
																							.hasProperty("url")) {
																						Property url = subDrawerItemLink
																								.getProperty("url");
																						url.remove();
																						session.save();
																					}
																					subDrawerItemLink
																							.setProperty(
																									"linktext",
																									list2.get(loop));
																					subDrawerItemLink
																							.setProperty(
																									"url",
																									list3.get(loop));

																				}

																			}

																		}
																	} 
																	if (list2
																			.size() != subdraweritems
																			.getSize()) {
																		infoLinksMisMatchFlag = true;
																	}
																	
																} else {
																	misMatchFlag = false;
																}
															}

														}

													}
													if (!misMatchFlag) {
														sb.append(Constants.MIS_MATCH_IN_SUB_DRAWER_PANEL_COUNT);
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
										if (count != drawerPanelsIterator
												.getSize())
											sb.append(Constants.MIS_MATCH_IN_DRAWER_PANEL_COUNT);

										// end new code

									} else {
										log.debug("<li>drawer panel elements section not found</li>");
									}
								} else {
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
					log.debug("<li>Unable to update drawers_container component."
							+ e + "</li>");
					log.error("Exception", e);
				}

				// end set drawers_container component content.
				// --------------------------------------------------------------------------------------------------------------------------

				// ---------------------------------------------------------------------------------------------------------------------------------------

				// --------------------------------------------------------------------------------------------------------------------------
				// --------------------------------------------------------------------------------------------------------------------------
				// start of tile bordered components.

				try {

					Elements rightRail = doc.select("div.c23-pilot,div.cc23-pilot");
					if (rightRail != null) {
						log.debug("rightRail size" + rightRail.size());

						if (rightRail.size() > 0) {

							NodeIterator titleBorderNodes = indexLowerRightNode
									.getNodes("tile*");

							int count = 0;
							int countOfTileBorderedElements = 0;
							boolean flag = false;
							for (Element ele : rightRail) {
								count = count + 1;
								javax.jcr.Node rightRailNode = null;

								String title = ele.getElementsByTag("h2") != null ? ele
										.getElementsByTag("h2").text() : "";
										
								if(title.equals("")){
									title = ele.getElementsByTag("h3") != null ? ele
											.getElementsByTag("h3").text() : "";
								}
								Elements imgElements = ele.getElementsByTag("img");
								if (imgElements != null && imgElements.size() > 0) {
									sb.append("<li> extra image found in right rail tile bordered component </li>");
								}
								String desc = ele.getElementsByTag("p") != null ? ele
										.getElementsByTag("p").text() : "";
								Elements anchor = ele.getElementsByTag("a");

								String textAfterAnchorTag = ele.ownText();
								if (StringUtils.isNotBlank(textAfterAnchorTag)) {
									flag = true;
									countOfTileBorderedElements++;
								}
								String anchorText = anchor != null ? anchor
										.first().text() : "";
								String anchorHref = anchor != null ? anchor
										.first().absUrl("href") : "";
										// Start extracting valid href
										log.debug("Before anchorHref" + anchorHref + "\n");
										anchorHref = FrameworkUtils.getLocaleReference(anchorHref, urlMap);
										log.debug("after anchorHref" + anchorHref + "\n");
										// End extracting valid href
								if (anchor != null && anchor.size() > 1) {
									sb.append("<li> extra anchor (url) is found in right rail tile bordered component </li>");
								}
								if (titleBorderNodes.hasNext()) {
									rightRailNode = (Node) titleBorderNodes
											.next();
								} else {
									log.debug("<li>all tile_boredered components are migrated</li>");
								}

								if (rightRailNode != null) {
									if (title != null && title != ""
											&& desc != null && desc != ""
											&& anchorText != null
											&& anchorText != "") {
										rightRailNode.setProperty("title",
												title);
										if(rightRailNode.hasProperty("filesize")){
											rightRailNode.setProperty("filesize","");
										}
										rightRailNode.setProperty(
												"description", desc);
										Node ctaNode = rightRailNode
												.hasNode("cta") ? rightRailNode
												.getNode("cta") : null;
										ctaNode.setProperty("linktext",
												anchorText);
										ctaNode.setProperty("url", anchorHref);
										log.debug("title, description, linktext and linkurl are created at "
												+ rightRailNode.getPath());
									} else {
										log.debug("<li>Content miss match for "
												+ "</li>");
									}
								} else {
									log.debug("<li>one of title_bordered node doesn't exist in node structure.</li>");
								}
							}
							if (flag) {
								sb.append("<li>Extra Text found after link on locale page for "
										+ countOfTileBorderedElements
										+ " TileBordered Component(s) , hence the text cannot be migrated.</li>");
							}
							if (count != indexLowerRightNode.getNodes("tile*")
									.getSize()) {
								sb.append("<li>Mis-Match in tilebordered Panels count."
										+ count
										+ " is not equal "
										+ indexLowerRightNode.getNodes("tile*")
												.getSize() + "</li>");
							}
						} else {
							log.debug("<li>No Content with class 'c23-pilot or cc23-pilot' found</li>");
						}
					} else {
						sb.append(Constants.TILE_BORDERED_COMPONENT_NOT_FOUND);
					}
				} catch (Exception e) {
					log.debug("Exception ", e);
					log.debug("<li>Unable to update tile_bordered component.\n</li>");
				}
				// End of tile bordered components.
				// -----------------------------------------------------------------------------------------------------
				// start set physical-security INDEX list.
				try {
					Node listContainerNode = null;
					Node list_item_parsys = null;
					Node list_content = null;
					Node listitems = null;
					Node itemNode = null;
					Node linkdata = null;
					NodeIterator item = null;
					Element indexlist = doc.select("div.n13-pilot").first();
					listContainerNode = indexLowerRightNode
							.hasNode("list_container") ? indexLowerRightNode
							.getNode("list_container") : null;
					if (indexlist != null) {
						String indexlistTitleText = indexlist.getElementsByTag(
								"h2").first() != null ? indexlist
								.getElementsByTag("h2").first().text() : "";
						Elements indexUlList = indexlist.getElementsByTag("ul");
						if (listContainerNode != null) {
							listContainerNode.setProperty("title",
									indexlistTitleText);
							log.debug("");

							list_item_parsys = listContainerNode
									.hasNode("list_item_parsys") ? listContainerNode
									.getNode("list_item_parsys") : null;
							if (list_item_parsys != null) {
								list_content = list_item_parsys
										.hasNode("list_content") ? list_item_parsys
										.getNode("list_content") : null;
								if (list_content != null) {
									listitems = list_content
											.hasNode("listitems") ? list_content
											.getNode("listitems") : null;
									if (listitems != null) {
										log.debug("list items path is : "
												+ listitems.getPath());
										item = listitems.getNodes("item*");
										int itemSize = (int)item.getSize();
										
												for (Element ele : indexUlList) {
													Elements indexUlLiList = ele
															.getElementsByTag("li");
													int liSize = indexUlLiList.size();
													if(itemSize!=liSize){
														sb.append(Constants.MISMATCH_RIGHT_RAIL_LIST);
													}

													for (Element li : indexUlLiList) {
														Elements listItemAnchor = li
																.getElementsByTag("a");
														String anchorText = listItemAnchor != null ? listItemAnchor
																.text() : "";
														String anchorHref = listItemAnchor
																.first().absUrl("href");
														// Start extracting valid href
														log.debug("Before anchorHref" + anchorHref + "\n");
														anchorHref = FrameworkUtils.getLocaleReference(anchorHref, urlMap);
														log.debug("after anchorHref" + anchorHref + "\n");
														// End extracting valid href
														if (item.hasNext()) {
															itemNode = (Node) item.next();
															log.debug("items path is : "
																	+ itemNode.getPath());
															linkdata = itemNode
																	.hasNode("linkdata") ? itemNode
																	.getNode("linkdata") : null;
															if (linkdata != null) {
														linkdata.setProperty(
																"linktext",
																anchorText);
														linkdata.setProperty(
																"url",
																anchorHref);
													}else {
														sb.append(Constants.LIST_ITEM_NODE_NOT_FOUND);
													}
												}
											} 

										}

									}

								}

							}
						}

					} else {
						if(listContainerNode!=null){
						sb.append(Constants.EXTRA_LIST_NODE_RIGHT_RAIL);
						}
					}
				} catch (Exception e) {
					sb.append("<li>Unable to update index list component.\n</li>");
					log.error("Exception : ", e);
				}
				// end set benefit list.
				
				//start of html blob
				int htmlBlobSize = 0;
				NodeIterator htmlBlob = indexUpperLeftNode.getNodes("htmlblob*");
				NodeIterator letUsHelp = indexUpperLeftNode.getNodes("letushelp_*");
				if(htmlBlob != null && htmlBlob.getSize() > 0){
					htmlBlobSize = (int)htmlBlob.getSize();
					if(letUsHelp != null && letUsHelp.getSize() > 0){
						htmlBlobSize=htmlBlobSize+(int)letUsHelp.getSize();
					}
					
				}
				if (doc.select("div.c42-pilot").size() > 0) {
					Element htmlBlobElement =  doc.select("div.c42-pilot").first();
					String html = FrameworkUtils.extractHtmlBlobContent(htmlBlobElement, "", locale, sb, urlMap);
					if (indexUpperLeftNode.hasNode("htmlblob_0")) {
						Node htmlblobNode = indexUpperLeftNode.getNode("htmlblob_0");
						if (StringUtils.isNotBlank(html)) {
							htmlblobNode.setProperty("html", html);
						}
					}
					sb.append("<li>Mis-match of html blob components "+"web page has (1) and nodes are ("+htmlBlobSize+") </li>");
				} else {
					sb.append("<li>Mis-match of html blob components "+"web page has (0) and nodes are ("+htmlBlobSize+") </li>");
				}
				//end of html blob
				
				
				// start of follow us component

				try {
					Elements rightRailPilotElements = doc.select("div.s14-pilot");
					if (!rightRailPilotElements.isEmpty()) {
						sb.append(Constants.FOLLOWUS_NODE_NOT_FOUND);
					}
				} catch (Exception e) {

					sb.append("<li>Unable to update followus component. Exception found is:  "
							+ e + "</li>");
				}
				// end of follow us component

				session.save();
			} else {
				sb.append(Constants.URL_CONNECTION_EXCEPTION);
			}

		} catch (Exception e) {
			log.debug("Exception ", e);
		}

		sb.append("</ul></td>");

		return sb.toString();
	}
}
