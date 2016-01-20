package com.cisco.dse.global.migration.subcat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
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
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.cisco.dse.global.migration.config.BaseAction;
import com.cisco.dse.global.migration.config.Constants;
import com.cisco.dse.global.migration.config.FrameworkUtils;

public class SubCatVariation4 extends BaseAction {
	Document doc;

	StringBuilder sb = new StringBuilder(1024);

	static Logger log = Logger.getLogger(SubCatVariation4.class);

	public String translate(String host, String loc, String prod, String type,
			String catType, String locale, Session session,
			Map<String, String> urlMap) throws IOException,
			ValueFormatException, VersionException, LockException,
			ConstraintViolationException, RepositoryException {

		log.debug("In the translate method of SubCategoryVariation4");
		log.debug("In the translate method, catType is :" + catType);

		// Repo node paths
		String pagePropertiesPath = "/content/<locale>/" + catType
				+ "/<prod>/service-provider-video-solutions/index/jcr:content";
		String pageUrl = host + "/content/<locale>/" + catType
				+ "/<prod>/service-provider-video-solutions/index.html";
		pagePropertiesPath = pagePropertiesPath.replace("<locale>", locale)
				.replace("<prod>", prod);
		pageUrl = pageUrl.replace("<locale>", locale).replace("<prod>", prod);

		sb.append("<td>" + "<a href=" + pageUrl + ">" + pageUrl + "</a>"
				+ "</td>");
		sb.append("<td>" + "<a href=" + loc + ">" + loc + "</a>" + "</td>");
		sb.append("<td><ul>");

		String subCatTopLeftNodePath = "/content/<locale>/"
				+ catType
				+ "/<prod>/service-provider-video-solutions/index/jcr:content/content_parsys/solutions/layout-solutions/gd22v2/gd22v2-left";
		String subCatTopRightNodePath = "/content/<locale>/"
				+ catType
				+ "/<prod>/service-provider-video-solutions/index/jcr:content/content_parsys/solutions/layout-solutions/gd22v2/gd22v2-right";
		String subCatBottomNodePath = "/content/<locale>/"
				+ catType
				+ "/<prod>/service-provider-video-solutions/index/jcr:content/content_parsys/solutions/layout-solutions/gd21v1/gd21v1-mid";

		subCatTopLeftNodePath = subCatTopLeftNodePath.replace("<locale>",
				locale).replace("<prod>", prod);
		subCatTopRightNodePath = subCatTopRightNodePath.replace("<locale>",
				locale).replace("<prod>", prod);
		subCatBottomNodePath = subCatBottomNodePath.replace("<locale>", locale)
				.replace("<prod>", prod);

		javax.jcr.Node subCatTopLeftNode = null;
		javax.jcr.Node subCatTopRightNode = null;
		javax.jcr.Node subCatBottomNode = null;
		javax.jcr.Node pageJcrNode = null;

		try {

			subCatTopLeftNode = session.getNode(subCatTopLeftNodePath);
			subCatTopRightNode = session.getNode(subCatTopRightNodePath);
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
				// ----------------------------------------------------------------------------------------------------------------
				// start of hero component
				try {
					Node heroMediumNode = null;
					Value[] panelPropertiest = null;
					String heroTitle = "";
					String heroDescription = "";
					String heroLinkText = "";
					String herolinkUrl = "";
					heroMediumNode = subCatTopLeftNode.hasNode("hero_medium") ? subCatTopLeftNode
							.getNode("hero_medium") : null;
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
									if(StringUtil.isBlank(herolinkUrl)){
										herolinkUrl = heroPanelLinkUrlElement.attr("href");
									}
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
								int imageSrcEmptyCount = 0;
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
											imageSrcEmptyCount++;
										}
									} else {
										sb.append(Constants.HERO_IMAGE_NODE_NOT_FOUND);
										log.debug("'image' node doesn't exists in "
												+ heroPanelNode.getPath());
									}
									if (imageSrcEmptyCount > 0) {
										sb.append(Constants.IMAGE_NOT_FOUND_IN_LOCALE_PAGE);
									}
								}
							}
						} else {
							log.debug(Constants.HERO_CONTENT_PANEL_ELEMENT_NOT_FOUND);
							log.debug("No div found with class 'frame'");
						}
					} else {
						sb.append(Constants.HERO_LARGE_COMPONENT_NOT_FOUND);
						log.debug("No element found with class 'c50-pilot'");
					}
				} catch (Exception e) {
					sb.append(Constants.EXCEPTION_IN_HERO_MIGRATION);
					log.debug("Exception : ", e);
				}

				// end of hero component
				// ----------------------------------------------------------------------------------------------------------
				// start Let Us Help migration
				try {
					Element contactUsElement = doc.select("div.f-holder")
							.first();
					if (contactUsElement != null) {
						sb.append(Constants.CONTACTUS_NODE_NOT_FOUND);
					}
				} catch (Exception e) {
					sb.append(Constants.UNABLE_TO_UPDATE_CONTACTUS);
					log.debug("Exception in Let Us Help Element Migration" + e);
				}
				// end Let Us Help migration
				// ----------------------------------------------------------------------------------------------------------
				// start of right part list component
				try {
					String title = "";
					String linkUrl = "";
					List<String> list = new ArrayList<String>();
					// getting data
					Element listEle = doc.select("div.gd-right").select("div.n13-pilot").first();
					if(listEle == null){
						listEle = doc.select("div.n13-pilot").last();
					}
					if (listEle != null) {
						Element titleEle = listEle.getElementsByTag("h2")
								.first();
						if (titleEle != null) {
							title = titleEle.text();
						} else {
							sb.append(Constants.TITLE_OF_LIST_ELEMENT_NOT_FOUND);
						}
						Elements links = listEle.getElementsByTag("a");
						if (links != null) {
							for (Element anchor : links) {
								linkUrl = anchor.absUrl("href");
								if(StringUtil.isBlank(linkUrl)){
									linkUrl = anchor.attr("href");
								}
								linkUrl = FrameworkUtils.getLocaleReference(
										linkUrl, urlMap);
								JSONObject obj = new JSONObject();
								obj.put("linktext", anchor.text());
								obj.put("linkurl", linkUrl);
								obj.put("icon", "");
								obj.put("size", "");
								obj.put("description", "");
								obj.put("openInNewWindow", false);
								list.add(obj.toString());
							}

						} else {
							sb.append(Constants.RIGHT_GRID_ANCHOR_ELEMENTS_NOT_FOUND);
						}
					} else {
						sb.append(Constants.RIGHT_RAIL_LIST_NOT_FOUND);
					}
					// setting data
					Node listNode = subCatTopRightNode.hasNode("list") ? subCatTopRightNode
							.getNode("list") : null;
					if (listNode != null) {
						if (StringUtils.isNotBlank(title)) {
							listNode.setProperty("title", title);
						}
						Node elementNode = listNode.hasNode("element_list_0") ? listNode
								.getNode("element_list_0") : null;
						if (elementNode != null) {
							boolean multiple = elementNode.getProperty(
									"listitems").isMultiple();
							if (multiple) {
								elementNode.setProperty("listitems",
										list.toArray(new String[list.size()]));
							} else {
								elementNode.setProperty("listitems",
										list.toString());
							}
						} else {
							sb.append(Constants.RIGHT_LIST_ITEMS_NODE_NOT_FOUND);
						}
					} else {
						sb.append(Constants.LIST_NODE_NOT_FOUND);
					}
				} catch (Exception e) {
					sb.append(Constants.EXCEPTION_IN_UPDATING_LIST_COMPONENT);
					log.debug("Exception in Top Right Element Migration" + e);
				}
				// end of right part list component
				// -----------------------------------------------------------------------------------------------------------
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
								String href = aElement.absUrl("href");
								if(StringUtil.isBlank(href)){
									href = aElement.attr("href");
								}
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
					Node followUsNode = subCatTopRightNode.hasNode("followus") ? subCatTopRightNode
							.getNode("followus") : null;
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
				// ------------------------------------------------------------------------------------------------------
				// start of migrating text components
				try {
					String text = "";
					String listText = "";
					StringBuilder firstText = new StringBuilder();
					StringBuilder lastText = new StringBuilder();
					int count = 1;
					// getting data
					Element listEle = doc.select("div.gd21v1-mid")
							.select("div.n13-pilot").first();
					if (listEle != null) {
						listText = FrameworkUtils.extractHtmlBlobContent(
								listEle, "", locale, sb, urlMap);
						lastText.append(listText);
					}
					Elements textEle = doc.select("div.c00-pilot");
					if (textEle != null) {
						for (Element ele : textEle) {
							text = FrameworkUtils.extractHtmlBlobContent(ele,
									"", locale, sb, urlMap);
							if (count < 4) {
								firstText.append(text);
							} else {
								lastText.append(text);
							}
							count++;
						}
					} else {
						sb.append(Constants.TEXT_ELEMENT_NOT_FOUND);
					}
					// setting data
					Node textFirstNode = subCatBottomNode.hasNode("text") ? subCatBottomNode
							.getNode("text") : null;
					if (textFirstNode != null) {
						if (StringUtils.isNotEmpty(firstText.toString())
								&& firstText.toString() != null) {
							textFirstNode.setProperty("text",
									firstText.toString());
						}
					} else {
						sb.append(Constants.TEXT_NODE_NOT_FOUND);
					}
					Node textLastNode = subCatBottomNode.hasNode("text_0") ? subCatBottomNode
							.getNode("text_0") : null;
					if (textLastNode != null) {
						if (StringUtils.isNotEmpty(lastText.toString())
								&& lastText.toString() != null) {
							textLastNode.setProperty("text",
									lastText.toString());
						}
					} else {
						sb.append(Constants.TEXT_NODE_NOT_FOUND);
					}

				} catch (Exception e) {
					sb.append(Constants.UNABLE_TO_MIGRATE_TEXT);
					log.error("Exception ", e);
				}
				// end of migrating text components
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
							if(StringUtil.isBlank(ctaLink)){
								ctaLink = spotLightCta.attr("href");
							}
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
								log.debug("Spotlight popup node:"+spotLightPopUpNode.getPath());
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
