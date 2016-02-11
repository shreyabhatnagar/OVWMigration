/* 
 * S.No		Name	Date		Description of change
 * 1		Vidya	1-feb-16	Added the Java file to handle the migration of collaboration solution listing page.
 * 
 * */
package com.cisco.dse.global.migration.solutionlisting;

import java.io.IOException;
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

public class SolutionListingVariation6 extends BaseAction {

	Document doc;

	StringBuilder sb = new StringBuilder(1024);

	static Logger log = Logger.getLogger(SolutionListingVariation6.class);

	public String translate(String host, String loc, String prod, String type,
			String catType, String locale, Session session,
			Map<String, String> urlMap) throws IOException,
			ValueFormatException, VersionException, LockException,
			ConstraintViolationException, RepositoryException {

		log.debug("In the translate method of SolutionListingVariation6");
		log.debug("In the translate method, catType is :" + catType);

		// Repo node paths
		String pagePropertiesPath = "/content/<locale>/" + catType
				+ "/<prod>/solution-listing/jcr:content";
		String pageUrl = host + "/content/<locale>/" + catType
				+ "/<prod>/solution-listing.html";
		pagePropertiesPath = pagePropertiesPath.replace("<locale>", locale)
				.replace("<prod>", prod);
		pageUrl = pageUrl.replace("<locale>", locale).replace("<prod>", prod);

		sb.append("<td>" + "<a href=" + pageUrl + ">" + pageUrl + "</a>"
				+ "</td>");
		sb.append("<td>" + "<a href=" + loc + ">" + loc + "</a>" + "</td>");
		sb.append("<td><ul>");

		String solutionListTopNodePath = "/content/<locale>/"
				+ catType
				+ "/<prod>/solution-listing/jcr:content/content_parsys/solutions/layout-solutions/gd21v1/gd21v1-mid";
		solutionListTopNodePath = solutionListTopNodePath.replace("<locale>",
				locale).replace("<prod>", prod);
		String solutionListFirstMidNodePath = "/content/<locale>/"
				+ catType
				+ "/<prod>/solution-listing/jcr:content/content_parsys/solutions/layout-solutions/gd23v1";
		solutionListFirstMidNodePath = solutionListFirstMidNodePath.replace(
				"<locale>", locale).replace("<prod>", prod);
		String solutionListSecondMidNodePath = "/content/<locale>/"
				+ catType
				+ "/<prod>/solution-listing/jcr:content/content_parsys/solutions/layout-solutions/gd23v1_0";
		solutionListSecondMidNodePath = solutionListSecondMidNodePath.replace(
				"<locale>", locale).replace("<prod>", prod);

		javax.jcr.Node solutionListTopNode = null;
		javax.jcr.Node solutionListFirstMidNode = null;
		javax.jcr.Node solutionListSecondMidNode = null;
		javax.jcr.Node pageJcrNode = null;

		try {
			solutionListTopNode = session.getNode(solutionListTopNodePath);
			solutionListFirstMidNode = session
					.getNode(solutionListFirstMidNodePath);
			solutionListSecondMidNode = session
					.getNode(solutionListSecondMidNodePath);
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
					Node heroLargeNode = null;
					Value[] panelPropertiest = null;
					String heroTitle = "";
					String heroDescription = "";
					String heroLinkText = "";
					String herolinkUrl = "";
					heroLargeNode = solutionListTopNode.hasNode("hero_large") ? solutionListTopNode
							.getNode("hero_large") : null;
					if (heroLargeNode != null) {
						Property panelNodesProperty = heroLargeNode
								.hasProperty("panelNodes") ? heroLargeNode
								.getProperty("panelNodes") : null;
						if (panelNodesProperty.isMultiple()) {
							panelPropertiest = panelNodesProperty.getValues();
						} else {
							panelPropertiest = new Value[1];
							panelPropertiest[0] = panelNodesProperty.getValue();
						}
					} else {
						sb.append(Constants.HERO_NODE_NOT_AVAILABLE);
						log.debug("Node with name 'hero_large' doesn't exist under");
					}

					Element heroLargeElement = doc.select("div.c50-pilot")
							.first();
					if (heroLargeElement != null) {
						Elements heroLargeTextElements = heroLargeElement
								.select("div.c50-text");
						Node heroPanelNode = null;
						if (heroLargeTextElements != null) {
							if (heroLargeTextElements.size() != heroLargeNode
									.getNodes("heropanel*").getSize()) {
								sb.append(Constants.MISMATCH_IN_HERO_SLIDES);
							}
							int i = 0;
							for (Element ele : heroLargeTextElements) {
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
									if (StringUtil.isBlank(herolinkUrl)) {
										herolinkUrl = heroPanelLinkUrlElement
												.attr("href");
									}
									// Start extracting valid href
									log.debug("heroPanellinkUrl before migration : "
											+ herolinkUrl);
									herolinkUrl = FrameworkUtils
											.getLocaleReference(herolinkUrl,
													urlMap, locale, sb);
									log.debug("heroPanellinkUrl after migration : "
											+ herolinkUrl);
									// End extracting valid href
								} else {
									sb.append(Constants.HERO_CONTENT_ANCHOR_ELEMENT_DOESNOT_EXISTS);
									log.debug("No anchor first element found with in the class 'frame' of div.");
								}
								String heroImage = FrameworkUtils
										.extractImagePath(ele.parent(), sb);
								log.debug("heroImage path : " + heroImage);
								if (panelPropertiest != null
										&& i <= panelPropertiest.length) {
									log.debug("loop" + i);
									String propertyVal = panelPropertiest[i]
											.getString();
									log.debug("propertyVal" + propertyVal);
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
								int imageSrcEmptyCount = 0;
								if (heroPanelNode != null) {
									// start of hero pop up
									Node heroPanelPopUpNode = null;
									Element lightBoxElement = null;
									Elements lightBoxElements = ele.select(
											"div.c50-image").select(
											"a.c26v4-lightbox");
									if (lightBoxElements != null
											&& !lightBoxElements.isEmpty()) {
										lightBoxElement = lightBoxElements
												.first();
									}
									heroPanelPopUpNode = FrameworkUtils
											.getHeroPopUpNode(heroPanelNode);
									if (heroPanelPopUpNode == null
											&& lightBoxElement != null) {
										sb.append("<li>video pop up is present in WEB page but it is not present in WEM page.</li>");
									}
									if (heroPanelPopUpNode != null
											&& lightBoxElement == null) {
										sb.append("<li>video pop up is present in WEM page but it is not present in WEB page.</li>");
									}
									if (heroPanelPopUpNode != null
											&& lightBoxElement != null
											&& StringUtils
													.isNotBlank(heroTitle)) {
										heroPanelPopUpNode.setProperty(
												"popupHeader", heroTitle);
									}
									// end of hero pop up
									if (StringUtils.isNotBlank(heroTitle)) {
										heroPanelNode.setProperty("title",
												heroTitle);
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
							log.debug("No div found with class 'c50-text'");
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
				// -------------------------------------------------------------------------------------------------------
				// start of migrating text component
				try {
					String text = "";
					StringBuilder textBuilder = new StringBuilder();
					// getting data
					Elements textEle = doc.select("div.c00-pilot");
					if (textEle != null) {
						for (Element ele : textEle) {
							text = FrameworkUtils.extractHtmlBlobContent(ele,
									"", locale, sb, urlMap);
							textBuilder.append(text);
						}
					} else {
						sb.append(Constants.TEXT_ELEMENT_NOT_FOUND);
					}
					// setting data
					Node textNode = solutionListTopNode.hasNode("text_0") ? solutionListTopNode
							.getNode("text_0") : null;
					if (textNode != null) {
						if (StringUtils.isNotEmpty(textBuilder.toString())
								&& textBuilder.toString() != null) {
							textNode.setProperty("text", textBuilder.toString());
						}
					} else {
						sb.append(Constants.TEXT_NODE_NOT_FOUND);
					}
				} catch (Exception e) {
					sb.append(Constants.UNABLE_TO_MIGRATE_TEXT);
					log.error("Exception ", e);
				}
				// end of migrating text component
				// ---------------------------------------------------------------------------------------------------
				// start of migrating tile_slp_small component
				int count = 1;
				Node tileSlpNode = null;
				Elements tileSlpEle = doc.select("div.nn12-pilot");
				if (tileSlpEle != null) {
					for (Element ele : tileSlpEle) {
						if (count == 1) {
							tileSlpNode = solutionListFirstMidNode;
						} else if (count == 2) {
							tileSlpNode = solutionListSecondMidNode;
						}
						HandleTileSlpSection(ele, tileSlpNode, urlMap, locale);
						count++;
					}
				} else {
					sb.append(Constants.MID_GRID_ELEMENT_NOT_FOUND);
				}
				// end of migrating tile_slp_small component
				// --------------------------------------------------------------------------------------------------------
				// start of migrating tile_bordered elements
				if (solutionListFirstMidNode.getParent().hasNode("gd23v1_1")) {
					sb.append(Constants.TILE_BORDERED_ELEMENTS_NOT_FOUND);
				}
				// end of migrating tile_bordered elements
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

	public void HandleTileSlpSection(Element ele, Node tileSlpNode,
			Map<String, String> urlMap, String locale) {
		try {
			int count = 1;
			Elements tileSlpLiElements = ele.getElementsByTag("li");
			for (Element tileSlpLiEle : tileSlpLiElements) {
				if (count == 1) {
					Node leftSlpNode = tileSlpNode
							.hasNode("gd23v1-left/tile_slp_small") ? tileSlpNode
							.getNode("gd23v1-left/tile_slp_small") : null;
					setContentToTileSlp(tileSlpLiEle, leftSlpNode, urlMap,
							locale);
				} else if (count == 2) {
					Node midSlpNode = tileSlpNode
							.hasNode("gd23v1-mid/tile_slp_small") ? tileSlpNode
							.getNode("gd23v1-mid/tile_slp_small") : null;
					setContentToTileSlp(tileSlpLiEle, midSlpNode, urlMap,
							locale);
				} else if (count == 3) {
					Node rightSlpNode = tileSlpNode
							.hasNode("gd23v1-right/tile_slp_small") ? tileSlpNode
							.getNode("gd23v1-right/tile_slp_small") : null;
					setContentToTileSlp(tileSlpLiEle, rightSlpNode, urlMap,
							locale);
				}
				count++;
			}
		} catch (Exception e) {
			sb.append("<li>Unable to migrate tile_slp_small component</li>");
		}
	}

	public void setContentToTileSlp(Element tileSlpEle, Node tileSlpNode,
			Map<String, String> urlMap, String locale) {
		try {
			String tileSlpTitle = "";
			String tileSlpDesc = "";
			String titleLink = "";
			// getting data
			if (tileSlpEle != null) {
				Element tileSlpTitleEle = tileSlpEle.getElementsByTag("h2")
						.first();
				if (tileSlpTitleEle != null) {
					tileSlpTitle = tileSlpTitleEle.text();
				} else {
					sb.append("<li>Tile_slp_small title element not found.</li>");
				}
				Element tileSlpTitleLinkEle = tileSlpTitleEle.getElementsByTag(
						"a").first();
				if (tileSlpTitleLinkEle != null) {
					titleLink = tileSlpTitleLinkEle.absUrl("href");
					if (StringUtil.isBlank(titleLink)) {
						titleLink = tileSlpTitleEle.attr("href");
					}
				} else {
					sb.append("<li>Tile_slp_small " + tileSlpTitle
							+ " title link not found.</li>");
				}
				tileSlpDesc = tileSlpEle.ownText();
				if (tileSlpDesc == null || StringUtils.isBlank(tileSlpDesc)) {
					Element tileSlpDescEle = tileSlpEle.getElementsByTag("p")
							.first();
					log.debug("tile desc:" + tileSlpDescEle);
					if (tileSlpDescEle != null) {
						tileSlpDesc = tileSlpDescEle.text();
					} else {
						sb.append("<li>Tile_slp_small decsription elements not found.</li>");
					}
				}
				// Start extracting valid href
				log.debug("Before titleLink" + titleLink + "\n");
				titleLink = FrameworkUtils
						.getLocaleReference(titleLink, urlMap, locale, sb);
				log.debug("after titleLink" + titleLink + "\n");
				// End extracting valid href

				// setting data
				if (tileSlpNode != null) {
					if (StringUtils.isNotEmpty(tileSlpTitle)) {
						tileSlpNode.setProperty("title", tileSlpTitle);
					}
					if (StringUtils.isNotEmpty(tileSlpDesc)) {
						tileSlpNode.setProperty("description", tileSlpDesc);
					}
					if (StringUtils.isNotEmpty(titleLink)) {
						tileSlpNode.setProperty("linktext", titleLink);
					}
					// start image
					String tileSlpImage = FrameworkUtils.extractImagePath(
							tileSlpEle, sb);
					log.debug("tileSlpImage " + tileSlpImage + "\n");
					// end image
					if (tileSlpNode.hasNode("image")) {
						Node imageNode = tileSlpNode.getNode("image");
						String fileReference = imageNode
								.hasProperty("fileReference") ? imageNode
								.getProperty("fileReference").getString() : "";
						tileSlpImage = FrameworkUtils.migrateDAMContent(
								tileSlpImage, fileReference, locale, sb);
						log.debug("tileSlpImage " + tileSlpImage + "\n");
						if (StringUtils.isNotBlank(tileSlpImage)) {
							imageNode
									.setProperty("fileReference", tileSlpImage);
						} else {
							sb.append(Constants.IMAGE_NOT_FOUND_IN_LOCALE_PAGE);
						}
					} else {
						sb.append("<li>Tile_slp_small image node not found.</li>");
					}
					// end image
				} else {
					sb.append("<li>Tile_slp_small node not found.</li>");
				}

			} else {
				sb.append("<li>Tile_slp_elements not found on WEB page.</li>");
			}
		} catch (Exception e) {
			sb.append("<li>Unable to migrate tile_slp_small component.</li>");
			log.error("Exception", e);
		}

	}
}
