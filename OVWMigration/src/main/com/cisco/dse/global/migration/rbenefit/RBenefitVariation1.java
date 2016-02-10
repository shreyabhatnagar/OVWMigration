package com.cisco.dse.global.migration.rbenefit;

import java.io.IOException;
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
import org.apache.log4j.Logger;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.cisco.dse.global.migration.config.BaseAction;
import com.cisco.dse.global.migration.config.Constants;
import com.cisco.dse.global.migration.config.FrameworkUtils;

public class RBenefitVariation1 extends BaseAction {
	Document doc;

	StringBuilder sb = new StringBuilder(1024);

	static Logger log = Logger.getLogger(RBenefitVariation1.class);

	public String translate(String host, String loc, String prod, String type,
			String catType, String locale, Session session,
			Map<String, String> urlMap) throws IOException,
			ValueFormatException, VersionException, LockException,
			ConstraintViolationException, RepositoryException {

		log.debug("In the translate method of RBenefitVariation1");
		log.debug("In the translate method, catType is :" + catType);

		// Repo node paths
		String pagePropertiesPath = "/content/<locale>/" + catType
				+ "/<prod>/benefit/jcr:content";
		String pageUrl = host + "/content/<locale>/" + catType
				+ "/<prod>/benefit.html";
		pagePropertiesPath = pagePropertiesPath.replace("<locale>", locale)
				.replace("<prod>", prod);
		pageUrl = pageUrl.replace("<locale>", locale).replace("<prod>", prod);

		sb.append("<td>" + "<a href=" + pageUrl + ">" + pageUrl + "</a>"
				+ "</td>");
		sb.append("<td>" + "<a href=" + loc + ">" + loc + "</a>" + "</td>");
		sb.append("<td><ul>");

		String benefitLeftNodePath = "/content/<locale>/"
				+ catType
				+ "/<prod>/benefit/jcr:content/Grid/category/layout-category/widenarrow/WN-Wide-1";
		String benefitRightNodePath = "/content/<locale>/"
				+ catType
				+ "/<prod>/benefit/jcr:content/Grid/category/layout-category/widenarrow/WN-Narrow-2";

		benefitLeftNodePath = benefitLeftNodePath.replace("<locale>", locale)
				.replace("<prod>", prod);

		benefitRightNodePath = benefitRightNodePath.replace("<locale>", locale)
				.replace("<prod>", prod);
		javax.jcr.Node benefitLeftNode = null;
		javax.jcr.Node benefitRightNode = null;
		javax.jcr.Node pageJcrNode = null;
		try {
			benefitLeftNode = session.getNode(benefitLeftNodePath);
			benefitRightNode = session.getNode(benefitRightNodePath);
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
				// start set page title text component
				try {
					String text = "";
					// getting data
					Element textEle = doc.select("div.gd-left")
							.select("div.c00-pilot").first();
					if (textEle != null) {
						text = textEle.html();
					} else {
						sb.append(Constants.TEXT_ELEMENT_NOT_FOUND);
					}
					// setting data
					Node textNode = benefitLeftNode.hasNode("text_1") ? benefitLeftNode
							.getNode("text_1") : null;
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

				// end set page title text component
				// --------------------------------------------------------------------------------------------------------------------
				// start set hero component
				try {
					String heroTitle = "";
					String heroDesc = "";
					String ctaText = "";
					String ctaLink = "";
					// getting data
					Element heroEle = doc.select("div.gd-left")
							.select("div.heropanel").first();
					if (heroEle != null) {
						Element heroTitleEle = heroEle.getElementsByTag("h2")
								.first();
						if (heroTitleEle != null) {
							heroTitle = heroTitleEle.text();
						} else {
							sb.append(Constants.HERO_CONTENT_HEADING_ELEMENT_DOESNOT_EXISTS);
						}
						Element heroDescEle = heroEle.getElementsByTag("p")
								.first();
						if (heroDescEle != null) {
							heroDesc = heroDescEle.text();
						} else {
							sb.append(Constants.HERO_CONTENT_DESCRIPTION_ELEMENT_DOESNOT_EXISTS);
						}
						Element heroCta = heroEle.getElementsByTag("a").first();
						if (heroCta != null) {
							ctaText = heroCta.text();
							ctaLink = heroCta.absUrl("href");
							if(StringUtil.isBlank(ctaLink)){
								ctaLink = heroCta.attr("href");
							}
						} else {
							sb.append(Constants.HERO_CONTENT_ANCHOR_ELEMENT_DOESNOT_EXISTS);
						}
						// Start extracting valid href
						log.debug("Before ctaLink" + ctaLink + "\n");
						ctaLink = FrameworkUtils.getLocaleReference(ctaLink,
								urlMap, locale, sb, catType, type);
						log.debug("after ctaLink" + ctaLink + "\n");
						// End extracting valid href

						// setting data
						Node heroNode = benefitLeftNode.hasNode("hero_panel") ? benefitLeftNode
								.getNode("hero_panel") : null;
						if (heroNode != null) {
							if (StringUtils.isNotEmpty(heroTitle)) {
								heroNode.setProperty("title", heroTitle);
							}
							if (StringUtils.isNotEmpty(heroDesc)) {
								heroNode.setProperty("description", heroDesc);
							}
							if (StringUtils.isNotEmpty(ctaText)) {
								heroNode.setProperty("linktext", ctaText);
							}
							Node ctaNode = heroNode.hasNode("cta") ? heroNode
									.getNode("cta") : null;
							if (ctaNode != null) {
								if (StringUtils.isNotEmpty(ctaLink)) {
									ctaNode.setProperty("url", ctaLink);
								}
							} else {
								sb.append(Constants.HERO_COMPONENT_CTA_NODE_NOT_FOUND);
							}
							// start image
							String heroImage = FrameworkUtils.extractImagePath(
									heroEle, sb);
							log.debug("heroImage " + heroImage + "\n");
							// end image
							if (heroNode.hasNode("image")) {
								Node imageNode = heroNode.getNode("image");
								String fileReference = imageNode
										.hasProperty("fileReference") ? imageNode
										.getProperty("fileReference")
										.getString() : "";
								heroImage = FrameworkUtils.migrateDAMContent(
										heroImage, fileReference, locale, sb, catType, type);
								log.debug("heroImage " + heroImage + "\n");
								if (StringUtils.isNotBlank(heroImage)) {
									imageNode.setProperty("fileReference",
											heroImage);
								}
							} else {
								sb.append(Constants.HERO_SLIDE_IMAGE_NODE_NOT_FOUND);
							}
							// end image
						} else {
							sb.append(Constants.HERO_CONTENT_NODE_NOT_FOUND);
						}

					} else {
						sb.append(Constants.HERO_CONTENT_PANEL_ELEMENT_NOT_FOUND);
					}
				} catch (Exception e) {
					sb.append(Constants.EXCEPTION_IN_HERO_MIGRATION);
					log.error("Exception", e);
				}

				// end of set hero component
				// ---------------------------------------------------------------------------------------------------
				// start of middle text components
				try {
					String text = "";
					int loopCount = 1;
					int h3Count = 0;
					// List<Element> firstText = new ArrayList<Element>();
					// List<Element> lastText = new ArrayList<Element>();
					StringBuilder firstText = new StringBuilder();
					StringBuilder lastText = new StringBuilder();
					// getting data
					Elements textEle = doc.select("div.gd-left").select(
							"div.c00-pilot");
					if (textEle != null) {
						for (Element ele : textEle) {
							if (loopCount == 2) {
								log.debug("Text elements:" + ele);
								int childrenSize = ele.children().size();
								for (int count = 0; count < childrenSize; count++) {
									Element child = ele.child(count);
									text = FrameworkUtils
											.extractHtmlBlobContent(child, "",
													locale, sb, urlMap, catType, type);
									if (child != null) {
										if ("h3".equalsIgnoreCase(child
												.tagName())) {
											h3Count++;
										}
										if (h3Count < 2) {
											firstText.append(text);
										} else {
											lastText.append(text);
										}
									}
								}

							}
							loopCount++;
						}
						log.debug("first text:" + firstText);
						log.debug("last text:" + lastText);
						// setting data
						Node firstTextNode = benefitLeftNode.hasNode("text") ? benefitLeftNode
								.getNode("text") : null;
						if (firstTextNode != null) {
							if (firstText.toString() != null) {
								firstTextNode.setProperty("text",
										firstText.toString());
							}
						} else {
							sb.append(Constants.TEXT_NODE_NOT_FOUND);
						}
						Node lastTextNode = benefitLeftNode.hasNode("text_2") ? benefitLeftNode
								.getNode("text_2") : null;
						if (lastTextNode != null) {
							if (lastText.toString() != null) {
								lastTextNode.setProperty("text",
										lastText.toString());
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

				// end of middle text components
				// ---------------------------------------------------------------------------------------------------
				// start of spotlight component
				try {
					String spotLightTitle = "";
					String spotLightDesc = "";
					String ctaText = "";
					String ctaLink = "";
					// getting data
					Element spotLightEle = doc.select("div.gd-left")
							.select("div.c11-pilot").first();
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
								urlMap, locale, sb, catType, type);
						log.debug("after ctaLink" + ctaLink + "\n");
						// End extracting valid href

						// setting data
						Node spotLightNode = benefitLeftNode
								.hasNode("spotlight") ? benefitLeftNode
								.getNode("spotlight") : null;
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
								spotLightNode.setProperty("ctaText", ctaText);
							}
							Node ctaNode = spotLightNode.hasNode("cta") ? spotLightNode
									.getNode("cta") : null;
							if (ctaNode != null) {
								if (StringUtils.isNotEmpty(ctaLink)) {
									ctaNode.setProperty("url", ctaLink);
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
												fileReference, locale, sb, catType, type);
								log.debug("spotLightImage " + spotLightImage
										+ "\n");
								if (StringUtils.isNotBlank(spotLightImage)) {
									imageNode.setProperty("fileReference",
											spotLightImage);
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
				// ------------------------------------------------------------------------------------------------------
				// start of list component
				try {
					String h3Text = "";
					String aText = "";
					String aHref = "";
					Node listContainerNode = benefitLeftNode
							.hasNode("list_container") ? benefitLeftNode
							.getNode("list_container") : null;
					Element listEle = doc.select("div.gd-left")
							.select("div.c00-pilot").last();
					if (listEle != null) {
						Element h3Ele = listEle.getElementsByTag("h3").first();
						Elements aEle = listEle.getElementsByTag("a");
						if (h3Ele != null) {
							h3Text = h3Ele.text();
						} else {
							sb.append(Constants.TITLE_OF_LIST_ELEMENT_NOT_FOUND);
						}
						if (listContainerNode != null) {
							if (h3Text != null) {
								listContainerNode.setProperty("title", h3Text);
							}
							Node listItemsNode = listContainerNode
									.hasNode("list_item_parsys/list_content/listitems") ? listContainerNode
									.getNode("list_item_parsys/list_content/listitems")
									: null;
							if (listItemsNode != null) {
								NodeIterator itemIterator = listItemsNode
										.hasNodes() ? listItemsNode
										.getNodes("item*") : null;
								int nodeCount = (int) itemIterator.getSize();
								int eleCount = aEle.size();
								if (nodeCount != eleCount) {
									sb.append(Constants.MIS_MATCH_IN_LINKS_OF_LIST
											+ nodeCount
											+ " and "
											+ eleCount
											+ "</li>");
								}
								for (Element ele : aEle) {
									if (itemIterator.hasNext()) {
										Node itemNode = (Node) itemIterator
												.next();
										itemNode = itemNode.hasNode("linkdata") ? itemNode
												.getNode("linkdata") : null;
										if (itemNode != null) {
											aText = ele.text();
											aHref = ele.absUrl("href");
											if(StringUtil.isBlank(aHref)){
												aHref = ele.attr("href");
											}
											// Start extracting valid href
											log.debug("Before aHref" + aHref
													+ "\n");
											aHref = FrameworkUtils
													.getLocaleReference(aHref,
															urlMap, locale, sb, catType, type);
											log.debug("after aHref" + aHref
													+ "\n");
											// End extracting valid href
											if (StringUtils.isNotEmpty(aText)) {
												itemNode.setProperty(
														"linktext", aText);
											} else {
												sb.append(Constants.LINK_TEXT_NOT_FOUND_IN_LIST);
											}
											if (StringUtils.isNotEmpty(aHref)) {
												itemNode.setProperty("url",
														aHref);
											} else {
												sb.append(Constants.LINK_URL_NOT_FOUND_IN_LIST);
											}
										} else {
											sb.append(Constants.LINK_DATA_NODE_FOR_LIST_NOT_FOUND);
										}

									}
								}
							} else {
								sb.append(Constants.LIST_ITEM_NODE_NOT_FOUND);
							}
						} else {
							sb.append(Constants.LIST_HEADING_COMPONENT_NOT_FOUND);
						}
					} else {
						sb.append(Constants.LIST_ELEMENT_NOT_FOUND);
					}
				} catch (Exception e) {
					sb.append(Constants.UNABLE_TO_MIGRATE_LIST_COMPONENT);
					log.error("Exception ", e);
				}
				// end of list component
				// ------------------------------------------------------------------------------------------------------
				// start of tile bordered components
				try {
					String tileTitle = "";
					String tileDesc = "";
					String ctaText = "";
					String ctaLink = "";
					Node tilePopUpNode = null;
					Elements tileEle = doc.select("div.gd-right").select(
							"div.c23-pilot");
					NodeIterator tileIterator = benefitRightNode.hasNodes() ? benefitRightNode
							.getNodes("tile*") : null;
					if (tileEle != null) {
						if (tileIterator != null) {
							int nodeCount = (int) tileIterator.getSize();
							int eleCount = tileEle.size();
							if (nodeCount != eleCount) {
								String message = Constants.TILE_BORDERED_ELEMENT_COUNT_MISMATCH;
								message = message.replace("(<ele>)",
										Integer.toString(eleCount));
								message = message.replace("(<node>)",
										Integer.toString(nodeCount));
								sb.append(message);
							}
							for (Element ele : tileEle) {
								Element tileTitleEle = ele.select("h2,h3")
										.first();
								if (tileTitleEle != null) {
									tileTitle = tileTitleEle.text();
								} else {
									sb.append(Constants.TILE_BORDERED_TITLE_NOT_FOUND);
								}
								Element tileDescEle = ele.getElementsByTag("p")
										.first();
								if (tileDescEle != null) {
									tileDesc = tileDescEle.text();
								} else {
									sb.append(Constants.TILE_BORDERED_DESCRIPTION_NOT_FOUND);
								}
								Element tileCta = ele.getElementsByTag("a")
										.first();
								if (tileCta != null) {
									ctaText = tileCta.text();
									ctaLink = tileCta.absUrl("href");
									if(StringUtil.isBlank(ctaLink)){
										ctaLink = tileCta.attr("href");
									}
								} else {
									sb.append(Constants.TILE_BORDERED_ANCHOR_ELEMENTS_NOT_FOUND);
								}
								// Start extracting valid href
								log.debug("Before ctaLink" + ctaLink + "\n");
								ctaLink = FrameworkUtils.getLocaleReference(
										ctaLink, urlMap, locale, sb, catType, type);
								log.debug("after ctaLink" + ctaLink + "\n");
								// End extracting valid href
								if (tileIterator.hasNext()) {
									Node tileNode = (Node) tileIterator.next();
									if (StringUtils.isNotEmpty(tileTitle)) {
										tileNode.setProperty("title", tileTitle);
									}
									if (StringUtils.isNotEmpty(tileDesc)) {
										tileNode.setProperty("description",
												tileDesc);
									}
									Property fileSize = tileNode
											.hasProperty("filesize") ? tileNode
											.getProperty("filesize") : null;
									if (fileSize != null) {
										log.debug(fileSize.getValue().toString());
										if (!fileSize.getValue().toString()
												.isEmpty()) {
											tileNode.setProperty("filesize", "");
										}
									}
									Node ctaNode = tileNode.hasNode("cta") ? tileNode
											.getNode("cta") : null;
									if (ctaNode != null) {
										Elements lightBoxElements = ele
												.select("a.c26v4-lightbox");
										tilePopUpNode = FrameworkUtils
												.getHeroPopUpNode(ctaNode);
										if(tilePopUpNode != null){
										if (lightBoxElements != null
												&& !lightBoxElements.isEmpty()) {
												tilePopUpNode.setProperty(
														"header", tileTitle);
											}else {
												if (StringUtils.isNotEmpty(ctaText)) {
													ctaNode.setProperty("linktype",
															"Url");
												}
											}
										}
										if (StringUtils.isNotEmpty(ctaText)) {
											ctaNode.setProperty("linktext",
													ctaText);
										}
										if (StringUtils.isNotEmpty(ctaLink)) {
											ctaNode.setProperty("url", ctaLink);
										}
									} else {
										sb.append(Constants.TILE_BORDERED_ANCHOR_ELEMENTS_NOT_FOUND);
									}
								}
							}
						} else {
							sb.append(Constants.TILE_BORDERED_NODES_NOT_FOUND);
						}
					} else {
						sb.append(Constants.TILE_BORDERED_ELEMENTS_NOT_FOUND);
					}
				} catch (Exception e) {
					sb.append(Constants.UNABLE_TO_MIGRATE_TILE_BORDERED_COMPONENTS);
					log.error("Exception ", e);
				}
				// end of tile bordered components
				// ---------------------------------------------------------------------------------------------------------
				// start of checking image
				if (benefitLeftNode.hasNode("image")) {
					sb.append(Constants.IMAGE_NOT_FOUND_IN_LOCALE_PAGE);
				}
				// end of checking image
				// ---------------------------------------------------------------------------------------------------------
				// start of checking text component
				if (benefitLeftNode.hasNode("text_0")) {
					sb.append(Constants.TEXT_ELEMENT_NOT_FOUND);
				}
				// end of checking text component
				// --------------------------------------------------------------------------------------------------------
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
