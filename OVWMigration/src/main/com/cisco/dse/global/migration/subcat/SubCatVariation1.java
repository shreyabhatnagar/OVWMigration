package com.cisco.dse.global.migration.subcat;

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
import org.apache.log4j.Logger;
import org.apache.sling.commons.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.cisco.dse.global.migration.config.BaseAction;
import com.cisco.dse.global.migration.config.Constants;
import com.cisco.dse.global.migration.config.FrameworkUtils;

public class SubCatVariation1 extends BaseAction {
	Document doc;

	StringBuilder sb = new StringBuilder(1024);

	static Logger log = Logger.getLogger(SubCatVariation1.class);

	public String translate(String host, String loc, String prod, String type,
			String catType, String locale, Session session,
			Map<String, String> urlMap) throws IOException,
			ValueFormatException, VersionException, LockException,
			ConstraintViolationException, RepositoryException {

		log.debug("In the translate method of SubCategoryVariation1");
		log.debug("In the translate method, catType is :" + catType);

		// Repo node paths
		String pagePropertiesPath = "/content/<locale>/" + catType
				+ "/<prod>/mobile-internet/index/jcr:content";
		String pageUrl = host + "/content/<locale>/" + catType
				+ "/<prod>/mobile-internet/index.html";
		pagePropertiesPath = pagePropertiesPath.replace("<locale>", locale)
				.replace("<prod>", prod);
		pageUrl = pageUrl.replace("<locale>", locale).replace("<prod>", prod);

		sb.append("<td>" + "<a href=" + pageUrl + ">" + pageUrl + "</a>"
				+ "</td>");
		sb.append("<td>" + "<a href=" + loc + ">" + loc + "</a>" + "</td>");
		sb.append("<td><ul>");

		String subCatTopNodePath = "/content/<locale>/"
				+ catType
				+ "/<prod>/mobile-internet/index/jcr:content/content_parsys/solutions/layout-solutions/gd22v2";
		String subCatFirstMidNodePath = "/content/<locale>/"
				+ catType
				+ "/<prod>/mobile-internet/index/jcr:content/content_parsys/solutions/layout-solutions/gd21v1/gd21v1-mid";
		String subCatSecondMidNodePath = "/content/<locale>/"
				+ catType
				+ "/<prod>/mobile-internet/index/jcr:content/content_parsys/solutions/layout-solutions/gd22v2_0";
		String subCatBottomNodePath = "/content/<locale>/"
				+ catType
				+ "/<prod>/mobile-internet/index/jcr:content/content_parsys/solutions/layout-solutions/gd21v1_0/gd21v1-mid";

		subCatTopNodePath = subCatTopNodePath.replace("<locale>", locale)
				.replace("<prod>", prod);

		subCatFirstMidNodePath = subCatFirstMidNodePath.replace("<locale>",
				locale).replace("<prod>", prod);

		subCatSecondMidNodePath = subCatSecondMidNodePath.replace("<locale>",
				locale).replace("<prod>", prod);

		subCatBottomNodePath = subCatBottomNodePath.replace("<locale>", locale)
				.replace("<prod>", prod);

		javax.jcr.Node subCatTopNode = null;
		javax.jcr.Node subCatFirstMidNode = null;
		javax.jcr.Node subCatSecondMidNode = null;
		javax.jcr.Node subCatBottomNode = null;
		javax.jcr.Node pageJcrNode = null;
		try {
			subCatTopNode = session.getNode(subCatTopNodePath);
			subCatFirstMidNode = session.getNode(subCatFirstMidNodePath);
			subCatSecondMidNode = session.getNode(subCatSecondMidNodePath);
			subCatBottomNode = session.getNode(subCatBottomNodePath);
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
					Node heroMediumNode = null;
					Value[] panelPropertiest = null;
					String heroTitle = "";
					String heroDescription = "";
					String heroLinkText = "";
					String herolinkUrl = "";
					heroMediumNode = subCatTopNode
							.hasNode("gd22v2-left/hero_medium") ? subCatTopNode
							.getNode("gd22v2-left/hero_medium") : null;
					if (heroMediumNode != null) {
						Property panelNodesProperty = heroMediumNode
								.hasProperty("panelNodes") ? heroMediumNode
								.getProperty("panelNodes") : null;
						if (panelNodesProperty.isMultiple()) {
							panelPropertiest = panelNodesProperty.getValues();
						}
					} else {
						sb.append(Constants.HERO_NODE_NOT_AVAILABLE);
						log.debug("Node with name 'hero_medium' doesn't exist under");
					}

					Element heroMediumElement = doc.select("div.c50-pilot")
							.first();
					if (heroMediumElement != null) {
						Elements heroMediumFrameElements = heroMediumElement
								.select("div.frame");
						Node heroPanelNode = null;
						if (heroMediumFrameElements != null) {
							if (heroMediumFrameElements.size() != heroMediumNode
									.getNodes("heropanel*").getSize()) {
								sb.append(Constants.MISMATCH_IN_HERO_SLIDES);
							}
							int i = 0;
							for (Element ele : heroMediumFrameElements) {
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
									// Start extracting valid href
									log.debug("heroPanellinkUrl before migration : "
											+ herolinkUrl);
									herolinkUrl = FrameworkUtils
											.getLocaleReference(herolinkUrl,
													urlMap);
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
											heroPanelNode = heroMediumNode
													.hasNode(panelNodeProperty) ? heroMediumNode
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
										if (lightBoxElements != null
												&& !lightBoxElements.isEmpty()) {
											heroPanelPopUpNode = FrameworkUtils
													.getHeroPopUpNode(heroPanelNode);
											if (heroPanelPopUpNode != null) {
												heroPanelPopUpNode.setProperty(
														"popupHeader",
														heroTitle);
											} else {
												sb.append("<li>Hero content video pop up node not found.</li>");
												log.debug("No pop-up node found for the hero panel node "
														+ heroPanelNode
																.getPath());
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
														sb);
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
				// --------------------------------------------------------------------------------------------------------
				// start of letus help component
				try {

					String titeText = "";
					String callText = "";
					Element contactUsElement = doc.select("div.f-holder")
							.first();
					if (contactUsElement != null) {
						Elements titleElem = contactUsElement
								.getElementsByTag("h3");
						Elements liElements = contactUsElement
								.getElementsByTag("li");
						Elements imgElements = contactUsElement
								.getElementsByTag("img");
						if (!titleElem.isEmpty()) {
							titeText = titleElem.text();
						} else {
							sb.append("<li> Contact Us Element title not found on locale page.</li>");
						}
						log.debug("call text is : " + callText);
						if (!liElements.isEmpty()) {
							callText = liElements.first().html();
							log.debug("call text is : " + callText);
						} else {
							sb.append("<li> Contact Us Element Call text not found on locale page.</li>");
						}

					} else {
						sb.append("<li> Contact Us Element Not Found on locale page. </li>");
					}
					Node letUsHelpNode = subCatTopNode
							.hasNode("gd22v2-right/letushelp") ? subCatTopNode
							.getNode("gd22v2-right/letushelp") : null;
					if (letUsHelpNode != null) {
						letUsHelpNode.setProperty("title", titeText);
						letUsHelpNode.setProperty("calltext", callText);
						if (letUsHelpNode.hasProperty("timetext")) {
							sb.append("<li> Extra text(Time Text) in Contact Us element found on WEM page. </li>");
						}
					}

				} catch (Exception e) {
					sb.append("<li>Unable to migrate let us help component</li>");
					log.error("Exception", e);
				}

				// end of letus help component
				// ----------------------------------------------------------------------------------------------------------
				// start of follow us component
				try {
					String followUsTitle = "";
					List<String> list = new ArrayList<String>();
					// getting data
					Element followUsEle = doc.select("div.s14-pilot").first();
					if (followUsEle != null) {
						Element followUsTitleEle = followUsEle
								.getElementsByTag("h2").first();
						if (followUsTitleEle != null) {
							followUsTitle = followUsTitleEle.text();
						} else {
							sb.append(Constants.FOLLOWUS_TITLE_NOT_FOUND);
						}
						Elements liElements = followUsEle
								.getElementsByTag("li");
						for (Element ele : liElements) {
							JSONObject obj = new JSONObject();
							String icon = ele.attr("class");
							obj.put("icon", icon);
							Elements aElements = ele.getElementsByTag("a");
							if (aElements != null) {
								Element aElement = aElements.first();
								String title = aElement.attr("title");
								String href = aElement.attr("href");
								// Start extracting valid href
								log.debug("Before pilotLinkUrl" + href + "\n");
								href = FrameworkUtils.getLocaleReference(href,
										urlMap);
								log.debug("after pilotLinkUrl" + href + "\n");
								// End extracting valid href
								obj.put("linktext", title);
								obj.put("linkurl", href);
							} else {
								sb.append(Constants.FOLLOW_US_ANCHOR_ELEMENT_NOT_FOUND);
							}
							list.add(obj.toString());
						}
					} else {
						sb.append(Constants.FOLLOWUS_ELEMENT_NOT_FOUND);
					}
					// setting data
					Node followUsNode = subCatTopNode
							.hasNode("gd22v2-right/followus") ? subCatTopNode
							.getNode("gd22v2-right/followus") : null;
					if (followUsNode != null) {
						if (StringUtils.isNotEmpty(followUsTitle)
								&& followUsTitle != null) {
							followUsNode.setProperty("title", followUsTitle);
						}
						if (list.size() > 1) {
							followUsNode.setProperty("links",
									list.toArray(new String[list.size()]));
						}
					} else {
						sb.append(Constants.FOLLOWUS_NODE_NOT_FOUND);
					}
				} catch (Exception e) {
					sb.append(Constants.UNABLE_TO_UPDATE_FOLLOWUS);
					log.error("Exception ", e);
				}

				// end of follow us component
				// --------------------------------------------------------------------------------------------------------
				// start of middle text component
				try {
					String text = "";
					// getting data
					Element textEle = doc
							.select("div.gd21-pilot,div.gd41-pilot")
							.select("div.gd-mid").select("div.c00-pilot")
							.first();
					if (textEle != null) {
						text = FrameworkUtils.extractHtmlBlobContent(textEle,
								"", locale, sb, urlMap);
						;
					} else {
						sb.append(Constants.TEXT_ELEMENT_NOT_FOUND);
					}
					// setting data
					Node textNode = subCatFirstMidNode.hasNode("text_0") ? subCatFirstMidNode
							.getNode("text_0") : null;
					if (textNode != null) {
						if (StringUtils.isNotEmpty(text) && text != null) {
							textNode.setProperty("text", text);
						}
					} else {
						sb.append(Constants.TEXT_NODE_NOT_FOUND);
					}

				} catch (Exception e) {
					sb.append(Constants.UNABLE_TO_MIGRATE_TEXT);
					log.error("Exception ", e);
				}

				// end of middle text component
				// ------------------------------------------------------------------------------------------------
				// start of left text component
				try {
					String text = "";
					// getting data
					Element textEle = doc.select("div.gd22v2")
							.select("div.gd-left").select("div.c00-pilot")
							.first();
					if (textEle != null) {
						text = FrameworkUtils.extractHtmlBlobContent(textEle,
								"", locale, sb, urlMap);
						;
					} else {
						sb.append(Constants.TEXT_ELEMENT_NOT_FOUND);
					}
					// setting data
					Node textNode = subCatSecondMidNode
							.hasNode("gd22v2-left/text") ? subCatSecondMidNode
							.getNode("gd22v2-left/text") : null;
					if (textNode != null) {
						if (StringUtils.isNotEmpty(text) && text != null) {
							textNode.setProperty("text", text);
						}
					} else {
						sb.append(Constants.TEXT_NODE_NOT_FOUND);
					}

				} catch (Exception e) {
					sb.append(Constants.UNABLE_TO_MIGRATE_TEXT);
					log.error("Exception ", e);
				}

				// end of left text component
				// ---------------------------------------------------------------------------------
				// start of right htmlblob component
				try {
					String htmlContent = "";
					// getting data
					Element htmlEle = doc.select("div.gd22v2")
							.select("div.gd-right").select("div.htmlblob")
							.first();
					if (htmlEle != null) {
						htmlContent = FrameworkUtils.extractHtmlBlobContent(
								htmlEle, "", locale, sb, urlMap);
						;
					} else {
						sb.append(Constants.HTMLBLOB_ELEMENT_NOT_FOUND);
					}
					// setting data
					Node htmlNode = subCatSecondMidNode
							.hasNode("gd22v2-right/htmlblob") ? subCatSecondMidNode
							.getNode("gd22v2-right/htmlblob") : null;
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
				// end of right htmlblob component
				// ---------------------------------------------------------------------------------------------------------
				// start of text component in bottom part
				try {
					String text = "";
					Elements textEle = doc.select("h2.bdr-1");
					log.debug("h2Ele:" + textEle);
					NodeIterator textNodeIterator = subCatBottomNode
							.hasNode("text_0") ? subCatBottomNode
							.getNodes("text*") : null;
					if (textEle != null) {
						if (textNodeIterator != null) {
							for (Element ele : textEle) {
								text = FrameworkUtils.extractHtmlBlobContent(
										ele, "", locale, sb, urlMap);
								if (textNodeIterator.hasNext()) {
									Node textNode = (Node) textNodeIterator
											.next();
									if (StringUtils.isNotEmpty(text)
											&& text != null) {
										textNode.setProperty("text", text);
									}
								}
							}
						} else {
							sb.append(Constants.TEXT_NODE_NOT_FOUND);
						}
					} else {
						sb.append(Constants.TEXT_ELEMENT_NOT_FOUND);
					}
				} catch (Exception e) {
					sb.append(Constants.UNABLE_TO_MIGRATE_TEXT);
					log.error("Exception ", e);
				}

				// end of text component in bottom part
				// ---------------------------------------------------------------------------------------------------
				// start of spotlight component
				try {
					String spotLightTitle = "";
					String spotLightDesc = "";
					String ctaText = "";
					String ctaLink = "";
					Node spotLightPopUpNode = null;
					// getting data
					Element spotLightEle = doc.select("div.c11-pilot").first();
					if (spotLightEle != null) {
						Element spotLightTitleEle = spotLightEle
								.getElementsByTag("h2").first();
						if (spotLightTitleEle != null) {
							spotLightTitle = spotLightTitleEle.text();
						} else {
							sb.append(Constants.SPOTLIGHT_HEADING_ELEMENT_NOT_FOUND);
						}
						Element spotLightDescEle = spotLightEle
								.getElementsByTag("p").first();
						if (spotLightDescEle != null) {
							spotLightDesc = spotLightDescEle.text();
						} else {
							sb.append(Constants.SPOTLIGHT_DESCRIPTION_ELEMENT_NOT_FOUND);
						}
						Element spotLightCta = spotLightEle.getElementsByTag(
								"a").first();
						if (spotLightCta != null) {
							ctaText = spotLightCta.text();
							ctaLink = spotLightCta.absUrl("href");
						} else {
							sb.append(Constants.SPOTLIGHT_ANCHOR_ELEMENT_NOT_FOUND);
						}
						// Start extracting valid href
						log.debug("Before ctaLink" + ctaLink + "\n");
						ctaLink = FrameworkUtils.getLocaleReference(ctaLink,
								urlMap);
						log.debug("after ctaLink" + ctaLink + "\n");
						// End extracting valid href

						// setting data
						Node spotLightNode = subCatBottomNode
								.hasNode("spotlight_medium_v2") ? subCatBottomNode
								.getNode("spotlight_medium_v2") : null;
						if (spotLightNode != null) {
							if (StringUtils.isNotEmpty(spotLightTitle)) {
								spotLightNode.setProperty("title",
										spotLightTitle);
							}
							if (StringUtils.isNotEmpty(spotLightDesc)) {
								spotLightNode.setProperty("description",
										spotLightDesc);
							}
							if (StringUtils.isNotEmpty(ctaText)) {
								spotLightNode.setProperty("linktext", ctaText);
							}
							Node ctaNode = spotLightNode.hasNode("cta") ? spotLightNode
									.getNode("cta") : null;
							if (ctaNode != null) {
								Elements lightBoxElements = spotLightCta
										.select("a.c26v4-lightbox");
								spotLightPopUpNode = FrameworkUtils
										.getHeroPopUpNode(ctaNode);
								if (spotLightPopUpNode != null) {
									if (lightBoxElements != null
											&& !lightBoxElements.isEmpty()) {
										spotLightPopUpNode.setProperty(
												"popupHeader", spotLightTitle);
									} else {
										sb.append("<li>Pop up element not found in locale page for spotlight link</li>");
									}
								}
							} else {
								sb.append(Constants.SPOTLIGHT_CTA_NODE_NOT_FOUND);
							}
							// start image
							String spotLightImage = FrameworkUtils
									.extractImagePath(spotLightEle, sb);
							log.debug("spotLightImage " + spotLightImage + "\n");
							// end image
							if (spotLightNode.hasNode("image")) {
								Node imageNode = spotLightNode.getNode("image");
								String fileReference = imageNode
										.hasProperty("fileReference") ? imageNode
										.getProperty("fileReference")
										.getString() : "";
								spotLightImage = FrameworkUtils
										.migrateDAMContent(spotLightImage,
												fileReference, locale, sb);
								log.debug("spotLightImage " + spotLightImage
										+ "\n");
								if (StringUtils.isNotBlank(spotLightImage)) {
									imageNode.setProperty("fileReference",
											spotLightImage);
								} else {
									sb.append(Constants.IMAGE_NOT_FOUND_IN_LOCALE_PAGE);
								}
							} else {
								sb.append(Constants.SPOTLIGHT_IMAGE_NODE_NOT_AVAILABLE);
							}
							// end image
						} else {
							sb.append(Constants.SPOTLIGHT_NODE_NOT_FOUND);
						}

					} else {
						sb.append(Constants.SPOTLIGHT_ELEMENT_NOT_FOUND);
					}
				} catch (Exception e) {
					sb.append(Constants.UNABLE_TO_UPDATE_SPOTLIGHT);
					log.error("Exception", e);
				}

				// end of spotlight component
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
