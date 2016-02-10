package com.cisco.dse.global.migration.web;

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
import org.apache.log4j.Logger;
import org.apache.sling.commons.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.cisco.dse.global.migration.config.BaseAction;
import com.cisco.dse.global.migration.config.Constants;
import com.cisco.dse.global.migration.config.FrameworkUtils;

public class WebVariation2 extends BaseAction {
	Document doc;

	StringBuilder sb = new StringBuilder(1024);

	static Logger log = Logger.getLogger(WebVariation2.class);

	public String translate(String host, String loc, String prod, String type,
			String catType, String locale, Session session,
			Map<String, String> urlMap) throws IOException,
			ValueFormatException, VersionException, LockException,
			ConstraintViolationException, RepositoryException {

		log.debug("In the translate method of WebVariation2");
		log.debug("In the translate method, catType is :" + catType);

		// Repo node paths
		String pagePropertiesPath = "/content/<locale>/" + catType
				+ "/<prod>/overview/jcr:content";
		String pageUrl = host + "/content/<locale>/" + catType
				+ "/<prod>/overview.html";
		pagePropertiesPath = pagePropertiesPath.replace("<locale>", locale)
				.replace("<prod>", prod);
		pageUrl = pageUrl.replace("<locale>", locale).replace("<prod>", prod);

		sb.append("<td>" + "<a href=" + pageUrl + ">" + pageUrl + "</a>"
				+ "</td>");
		sb.append("<td>" + "<a href=" + loc + ">" + loc + "</a>" + "</td>");
		sb.append("<td><ul>");

		String heroParentNodePath = "/content/<locale>/"
				+ catType
				+ "/<prod>/overview/jcr:content/content_parsys/overview/layout-overview/gd12v2/gd12v2-left";
		heroParentNodePath = heroParentNodePath.replace("<locale>", locale)
				.replace("<prod>", prod);
		String primaryCtaParentNodePath = "/content/<locale>/"
				+ catType
				+ "/<prod>/overview/jcr:content/content_parsys/overview/layout-overview/gd12v2/gd12v2-right";
		primaryCtaParentNodePath = primaryCtaParentNodePath.replace("<locale>",
				locale).replace("<prod>", prod);
		String leftHtmlblobNodePath = "/content/<locale>/"
				+ catType
				+ "/<prod>/overview/jcr:content/content_parsys/overview/layout-overview/gd12v2_0/gd12v2-left";
		leftHtmlblobNodePath = leftHtmlblobNodePath.replace("<locale>", locale)
				.replace("<prod>", prod);
		String rightHtmlblobNodePath = "/content/<locale>/"
				+ catType
				+ "/<prod>/overview/jcr:content/content_parsys/overview/layout-overview/gd12v2_0/gd12v2-right";
		rightHtmlblobNodePath = rightHtmlblobNodePath.replace("<locale>",
				locale).replace("<prod>", prod);

		javax.jcr.Node heroParentNode = null;
		javax.jcr.Node primaryCtaParentNode = null;
		javax.jcr.Node leftHtmlblobNode = null;
		javax.jcr.Node rightHtmlblobNode = null;
		javax.jcr.Node pageJcrNode = null;
		try {
			heroParentNode = session.getNode(heroParentNodePath);
			primaryCtaParentNode = session.getNode(primaryCtaParentNodePath);
			leftHtmlblobNode = session.getNode(leftHtmlblobNodePath);
			rightHtmlblobNode = session.getNode(rightHtmlblobNodePath);
			pageJcrNode = session.getNode(pagePropertiesPath);

			try {
				doc = getConnection(loc);
			} catch (Exception e) {
				log.error("Exception ", e);
			}
			if (doc != null) {
				// start set page properties.
				log.debug("Started setting page properties");
				FrameworkUtils.setPageProperties(pageJcrNode, doc, session, sb);
				log.debug("Done with the setting page properties");
				// end set page properties.
				// -------------------------------------------------------------------------------------------------------------------
				// start of hero component
				try {
					Node heroLargeNode = null;
					Value[] panelPropertiest = null;
					String heroTitle = "";
					String heroDescription = "";
					String heroLinkText = "";
					String herolinkUrl = "";
					heroLargeNode = heroParentNode.hasNode("hero_large") ? heroParentNode
							.getNode("hero_large") : null;
							if (heroLargeNode != null) {
								Property panelNodesProperty = heroLargeNode
										.hasProperty("panelNodes") ? heroLargeNode
												.getProperty("panelNodes") : null;
												if (panelNodesProperty.isMultiple()) {
													panelPropertiest = panelNodesProperty.getValues();
												}
							} else {
								sb.append(Constants.HERO_NODE_NOT_AVAILABLE);
								log.debug("Node with name 'hero_lage' doesn't exist under");
							}

							Element heroLagreElement = doc.select("div.c50-pilot")
									.first();
							if (heroLagreElement != null) {
								Elements heroLargeFrameElements = heroLagreElement
										.select("div.frame");
								Node heroPanelNode = null;
								if (heroLargeFrameElements != null) {
									if (heroLargeFrameElements.size() != heroLargeNode
											.getNodes("heropanel*").getSize()) {
										sb.append(Constants.MISMATCH_IN_HERO_SLIDES);
									}
									int i = 0;
									for (Element ele : heroLargeFrameElements) {
										Element heroTitleElement = ele
												.getElementsByTag("h2").first();
										if (heroTitleElement != null) {
											heroTitle = heroTitleElement.text();
										} else {
											sb.append(Constants.HERO_CONTENT_HEADING_ELEMENT_DOESNOT_EXISTS);
											log.debug("No h2 first element found with in the class 'frame' of div.");
										}
										Element heroDescriptionElement = ele
												.getElementsByTag("p").first();
										if (heroDescriptionElement != null) {
											heroDescription = heroDescriptionElement
													.text();
										} else {
											sb.append(Constants.HERO_CONTENT_DESCRIPTION_ELEMENT_DOESNOT_EXISTS);
											log.debug("No p frist element found with in the class 'frame' of div.");
										}
										Element heroPanelLinkTextElement = ele
												.getElementsByTag("b").first();
										if (heroPanelLinkTextElement != null) {
											heroLinkText = heroPanelLinkTextElement
													.text();
										} else {
											sb.append(Constants.HERO_CONTENT_ANCHOR_TEXT_IS_BLANK);
											log.debug("No b tags first elemtn found with in the class 'frame' of div.");
										}
										Element heroPanelLinkUrlElement = ele
												.getElementsByTag("a").first();
										if (heroPanelLinkUrlElement != null) {
											herolinkUrl = heroPanelLinkUrlElement
													.absUrl("href");
											if(StringUtils.isBlank(herolinkUrl)){
												herolinkUrl = heroPanelLinkUrlElement.attr("href");
											}
											// Start extracting valid href
											log.debug("heroPanellinkUrl before migration : "
													+ herolinkUrl);
											herolinkUrl = FrameworkUtils
													.getLocaleReference(herolinkUrl,
															urlMap,locale, sb, catType, type);
											log.debug("heroPanellinkUrl after migration : "
													+ herolinkUrl);
											// End extracting valid href
										} else {
											sb.append("<li>Hero Panel element not having any linkurl in it </li>");
											log.debug("No anchor first element found with in the class 'frame' of div.");
										}
										String heroImage = FrameworkUtils
												.extractImagePath(ele, sb);
										log.debug("heroImage path : " + heroImage);
										if (panelPropertiest != null
												&& i <= panelPropertiest.length) {
											String propertyVal = panelPropertiest[i]
													.getString();
											if (StringUtils.isNotBlank(propertyVal)) {
												JSONObject jsonObj = new JSONObject(
														propertyVal);
												if (jsonObj.has("panelnode")) {
													String panelNodeProperty = jsonObj
															.get("panelnode")
															.toString();
													heroPanelNode = heroLargeNode
															.hasNode(panelNodeProperty) ? heroLargeNode
																	.getNode(panelNodeProperty)
																	: null;
												}
											}
											i++;
										}
										if (heroPanelNode != null) {
											Node heroPanelPopUpNode = null;
											Elements lightBoxElements = ele.select(
													"p.cta-link").select(
															"a.c26v4-lightbox");
											if (StringUtils.isNotBlank(heroTitle)) {
												heroPanelNode.setProperty("title",
														heroTitle);
												heroPanelPopUpNode = FrameworkUtils
														.getHeroPopUpNode(heroPanelNode);
												if (heroPanelPopUpNode != null) {
													if (lightBoxElements != null
															&& !lightBoxElements
															.isEmpty()) {
														heroPanelPopUpNode.setProperty(
																"popupHeader",
																heroTitle);
													} else {
														sb.append("<li>Hero content video pop up elements not found in locale page.</li>");
														log.debug("No pop-up node found for the hero panel node "
																+ heroPanelNode
																.getPath());
													}
												} else {
													if (lightBoxElements != null
															&& !lightBoxElements
															.isEmpty()) {
														sb.append("<li>Hero content video pop up node not found.</li>");
													}
												}
											}
											if (StringUtils.isNotBlank(heroDescription)) {
												heroPanelNode.setProperty(
														"description", heroDescription);
											}
											if (StringUtils.isNotBlank(heroLinkText)) {
												heroPanelNode.setProperty("linktext",
														heroLinkText);
											}
											if (StringUtils.isNotBlank(herolinkUrl)) {
												heroPanelNode.setProperty("linkurl",
														herolinkUrl);
											}
											if (heroPanelNode.hasNode("image")) {
												Node imageNode = heroPanelNode
														.getNode("image");
												String fileReference = imageNode
														.hasProperty("fileReference") ? imageNode
																.getProperty("fileReference")
																.getString() : "";
																heroImage = FrameworkUtils
																		.migrateDAMContent(heroImage,
																				fileReference, locale,
																				sb, catType, type);
																log.debug("heroImage : " + heroImage);
																if (StringUtils.isNotBlank(heroImage)) {
																	imageNode.setProperty(
																			"fileReference", heroImage);
																} else {
																	sb.append(Constants.IMAGE_NOT_FOUND_IN_LOCALE_PAGE);
																}
											} else {
												sb.append(Constants.HERO_IMAGE_NODE_NOT_FOUND);
												log.debug("'image' node doesn't exists in "
														+ heroPanelNode.getPath());
											}
										}
									}
								} else {
									log.debug(Constants.HERO_CONTENT_PANEL_ELEMENT_NOT_FOUND);
									log.debug("No div found with class 'frame'");
								}
							} else {
								;
								sb.append(Constants.HERO_LARGE_COMPONENT_NOT_FOUND);
								log.debug("No element found with class 'c50-pilot'");
							}
				} catch (Exception e) {
					sb.append(Constants.EXCEPTION_IN_HERO_MIGRATION);
					log.debug("Exception : ", e);
				}

				// end of hero component
				// ----------------------------------------------------------------------------------------------------------------
				// start of primary cta component
				try {
					String h3Text = "";
					String pText = "";
					String aText = "";
					String aHref = "";
					// getting data
					Element primaryCtaElements = doc.select("div.c47-pilot")
							.first();
					if (primaryCtaElements != null) {
						Elements h3TagText = primaryCtaElements
								.getElementsByTag("h3");
						if (h3TagText != null) {
							h3Text = h3TagText.html();
						} else {
							sb.append(Constants.PRIMARY_CTA_TITLE_ELEMENT_NOT_FOUND);
						}

						Elements descriptionText = primaryCtaElements
								.getElementsByTag("p");
						if (descriptionText != null) {
							pText = descriptionText.html();
						} else {
							sb.append(Constants.PRIMARY_CTA_DESCRIPTION_ELEMENT_NOT_FOUND);
						}

						Element anchorText = primaryCtaElements
								.getElementsByTag("a").first();
						if (anchorText != null) {
							aText = anchorText.text();
							aHref = anchorText.absUrl("href");
							if(StringUtils.isBlank(aHref)){
								aHref = anchorText.attr("href");
							}
							// Start extracting valid href
							log.debug("Before primaryCTALinkUrl" + aHref + "\n");
							aHref = FrameworkUtils.getLocaleReference(aHref,
									urlMap,locale, sb, catType, type);
							log.debug("after primaryCTALinkUrl" + aHref + "\n");
							// End extracting valid href
						} else {
							sb.append(Constants.PRIMARY_CTA_ANCHOR_ELEMENT_NOT_FOUND);
						}
					} else {
						sb.append(Constants.PRIMARY_CTA_COMPONENT_NOT_FOUND);
					}
					// setting data
					NodeIterator primaryCtaNodeItr = primaryCtaParentNode
							.hasNodes() ? primaryCtaParentNode
									.getNodes("primary_cta*") : null;
									Node primaryCtaNode = primaryCtaNodeItr.hasNext()?(Node)primaryCtaNodeItr.next():null;
									if (primaryCtaNode != null) {
										if (StringUtils.isNotBlank("h3Text")) {
											primaryCtaNode.setProperty("title", h3Text);
										}
										if (StringUtils.isNotBlank("pText")) {
											primaryCtaNode.setProperty("description", pText);
										}
										if (StringUtils.isNotBlank("aText")) {
											primaryCtaNode.setProperty("linktext", aText);
										}
										if (StringUtils.isNotBlank("aHref")) {
											primaryCtaNode.setProperty("linkurl", aHref);
										}
									} else {
										sb.append("<li>Primary cta node not found.</li>");

									}

				} catch (Exception e) {
					sb.append(Constants.PRIMARY_CTA_COMPONENT_NOT_UPDATED);
				}

				// end of primary cta component
				// --------------------------------------------------------------------------------------------------------------
				// Start of left html blob
				try {
					String htmlContent = "";
					int count = 1;
					// getting data
					Elements htmlEle = doc.select("div.gd-left");
					if(htmlEle.size() == 1 && htmlEle != null){
						htmlEle.first().select("div.c50-pilot").remove();
						htmlContent = FrameworkUtils
								.extractHtmlBlobContent(htmlEle.first(), "",
										locale, sb, urlMap, catType, type);
					}else{
						for (Element ele : htmlEle) {
							if (count == 2) {

								if (ele != null) {
									htmlContent = FrameworkUtils
											.extractHtmlBlobContent(ele, "",
													locale, sb, urlMap, catType, type);
								} else {
									sb.append(Constants.HTMLBLOB_ELEMENT_NOT_FOUND);
								}
							}
							count++;
						}
					}
					// setting data
					Node htmlNode = leftHtmlblobNode.hasNode("htmlblob") ? leftHtmlblobNode
							.getNode("htmlblob") : null;
							if (htmlNode != null) {
								if (StringUtils.isNotEmpty(htmlContent)
										&& htmlContent != null) {
									htmlNode.setProperty("html", htmlContent);
								}
							} else {
								sb.append(Constants.HTMLBLOB_NODE_NOT_FOUND);
							}

				} catch (Exception e) {
					sb.append(Constants.EXCEPTION_IN_HTMLBLOB);
					log.error("Exception ", e);
				}
				// end of left html blob
				// -------------------------------------------------------------------------------------------------
				// Start of right html blob
				try {
					String htmlContent = "";
					// getting data
					Element htmlEle = doc.select("div.gd-right").last();
					if (htmlEle != null) {
						htmlEle.select("div.c47-pilot").remove();
						Elements s12Ele = htmlEle.select("div.s12-pilot");
						if(s12Ele != null && !s12Ele.isEmpty()){
							sb.append("<li>Extra Component found with  class s12-pilot and that can't be migrated.</li>");
							htmlEle.select("div.s12-pilot").remove();
						}
						log.debug("gd-right " + htmlEle);
						//Fix By Aziz
						Element popUp = htmlEle.getElementsByClass("c23-pilot").first().getElementsByTag("a").first();
						if(popUp != null){
							if(popUp.hasAttr("data-config-targetlightbox")){
								String id = popUp.attr("data-config-targetlightbox");
								Element popUpHeading = doc.getElementById(id);
								if(popUpHeading != null){
									Element popUpHeader = popUpHeading.getElementsByTag("h2").first();
									if(popUpHeader != null){
										Node c26v4_popup_cqNode = rightHtmlblobNode.hasNode("c26v4_popup_cq") ? rightHtmlblobNode.getNode("c26v4_popup_cq") : null;
										if(c26v4_popup_cqNode != null){
											c26v4_popup_cqNode.setProperty("popupHeader", popUpHeader.text());
										}else{
											sb.append("<li>PopUpNode not found.</li>");
										}
									}else{
										sb.append("<li>PopUp heading element not found in web page.</li>");
									}
								}else{
									sb.append("<li>PopUp anchor element not found in web page.</li>");
								}
							}else{
								sb.append("<li>PopUp anchor element not found in web page.</li>");
							}
						}else{
							sb.append("<li>PopUp anchor element not found in web page.</li>");
						}
						//End of Fix
						htmlContent = FrameworkUtils.extractHtmlBlobContent(
								htmlEle, "", locale, sb, urlMap, catType, type);
					} else {
						sb.append(Constants.HTMLBLOB_ELEMENT_NOT_FOUND);
					}

					// setting data
					Node htmlNode = rightHtmlblobNode.hasNode("htmlblob") ? rightHtmlblobNode
							.getNode("htmlblob") : null;
							if (htmlNode != null) {
								if (StringUtils.isNotEmpty(htmlContent)
										&& htmlContent != null) {
									htmlNode.setProperty("html", htmlContent.replaceAll("<br>", ""));
								}
							} else {
								sb.append(Constants.HTMLBLOB_NODE_NOT_FOUND);
							}

				} catch (Exception e) {
					sb.append(Constants.EXCEPTION_IN_HTMLBLOB);
					log.error("Exception ", e);
				}
				// end of right html blob
			} else {
				sb.append(Constants.URL_CONNECTION_EXCEPTION);
			}
		} catch (Exception e) {
			log.error("Exception ", e);
			sb.append(Constants.UNABLE_TO_MIGRATE_PAGE);
		}
		sb.append("</ul></td>");
		session.save();
		return sb.toString();
	}

}
