/* 
 * S.No		Name	Date		Description of change
 * 1		Vidya	22-dec-15	Added the Java file to handle the migration of responsive service listing pages.
 * 
 * */
package com.cisco.dse.global.migration.rservicelisting;

import java.io.IOException;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.cisco.dse.global.migration.config.BaseAction;
import com.cisco.dse.global.migration.config.Constants;
import com.cisco.dse.global.migration.config.FrameworkUtils;

public class RServiceListingVariation1 extends BaseAction {
	Document doc;
	StringBuilder sb = new StringBuilder(1024);
	static Logger log = Logger.getLogger(RServiceListingVariation1.class);

	public String translate(String host, String loc, String prod, String type,
			String catType, String locale, Session session, Map<String, String> urlMap) throws IOException,
			ValueFormatException, VersionException, LockException,
			ConstraintViolationException, RepositoryException {
		log.debug("In the translate method of RServiceListingVariation1");
		log.debug("In the translate method, catType is :" + catType);

		String pagePropertiesPath = "/content/<locale>/" + catType
				+ "/<prod>/service-listing/jcr:content";
		String pageUrl = host + "/content/<locale>/" + catType
				+ "/<prod>/service-listing.html";
		pagePropertiesPath = pagePropertiesPath.replace("<locale>", locale)
				.replace("<prod>", prod);
		pageUrl = pageUrl.replace("<locale>", locale).replace("<prod>", prod);

		sb.append("<td>" + "<a href=" + pageUrl + ">" + pageUrl + "</a>"
				+ "</td>");
		sb.append("<td>" + "<a href=" + loc + ">" + loc + "</a>" + "</td>");
		sb.append("<td><ul>");

		String serviceListingNodePath = "/content/<locale>/"
				+ catType
				+ "/<prod>/service-listing/jcr:content/Grid/category/layout-category/narrowwide/NW-Wide-2";

		serviceListingNodePath = serviceListingNodePath.replace("<locale>",
				locale).replace("<prod>", prod);

		javax.jcr.Node serviceListingNode = null;
		javax.jcr.Node pageJcrNode = null;
		try {

			serviceListingNode = session.getNode(serviceListingNodePath);
			pageJcrNode = session.getNode(pagePropertiesPath);

			try {
				doc = getConnection(loc);
			} catch (Exception e) {
				log.error("Exception ", e);
			}

			if (doc != null) {
				// ------------------------------------------------------------------------------------------------------------------------------------------
				// start set page properties.

				FrameworkUtils.setPageProperties(pageJcrNode, doc, session, sb);

				// end set page properties.
				// ------------------------------------------------------------------------------------------------------------------------------------------
				// start of heading component
				try {
					String h1Text = "";
					// getting data
					Element headingElements = doc.select("div.gd-right")
							.select("div.c00-pilot").first();
					if (headingElements != null) {
						Element h1Elements = headingElements.getElementsByTag(
								"h1").first();
						if (h1Elements != null) {
							h1Text = h1Elements.text();
						} else {
							sb.append(Constants.HEADER_ELEMENT_NOT_FOUND);
						}
					} else {
						headingElements = doc.select("div.gd-right")
								.select("div.cc00-pilot").first();
						if (headingElements != null) {
							h1Text = headingElements.html();
						} else {
							sb.append(Constants.HEADER_ELEMENT_NOT_FOUND);
						}
					}
					// setting data
					Node headerNode = serviceListingNode.hasNode("header") ? serviceListingNode
							.getNode("header") : null;
					if (headerNode != null) {
						headerNode.setProperty("title", h1Text);
					} else {
						sb.append(Constants.HEADER_NODE_NOT_FOUND);
					}

				} catch (Exception e) {
					sb.append(Constants.UNABLE_TO_MIGRATE_HEADER);
					log.error("Exception ", e);
				}

				// end of heading component
				// ------------------------------------------------------------------------------------------------------------------------------
				// start of spotlight components
				try {
					String h2Text = "";
					String h3Text = "";
					String pText = "";
					String aHref = "";
					Elements spotlightElements = doc.select("div.gd-right")
							.select("div.c11-pilot");
					NodeIterator spotlightNodeIterator = serviceListingNode
							.hasNode("spotlight") ? serviceListingNode
							.getNodes("spotlight*") : null;
					if (!spotlightElements.isEmpty()) {
						for (Element ele : spotlightElements) {
							Element h2Element = ele.getElementsByTag("h2")
									.first();
							if (h2Element != null) {
								h2Text = h2Element.text();
							} else {
								sb.append(Constants.SPOTLIGHT_HEADING_ELEMENT_NOT_FOUND);
							}
							StringBuilder descText = new StringBuilder();
							Element pElement = ele.getElementsByTag("p")
									.first();
							if (pElement != null) {
								descText.append(FrameworkUtils.extractHtmlBlobContent(pElement, "",locale, sb, urlMap));
							} else {
								sb.append(Constants.SPOTLIGHT_DESCRIPTION_ELEMENT_NOT_FOUND);
							}
							Element ulElement = ele.getElementsByTag("ul")
									.first();
							if (ulElement != null) {
								descText.append(FrameworkUtils.extractHtmlBlobContent(ulElement, "",locale, sb, urlMap));
							}
							if (spotlightNodeIterator.hasNext()) {
								Node spotlightNode = (Node) spotlightNodeIterator
										.next();
								pText = descText.toString();
								handleSpotlightMigration(spotlightNode, ele,
										locale, h2Text, pText, aHref);
							} else {
								sb.append(Constants.SPOTLIGHT_NODE_NOT_FOUND);
							}
						}
					} else {
						int sCount = 1;
						spotlightElements = doc.select("div.gd-right").select(
								"div.cc00-pilot");
						if (!spotlightElements.isEmpty()) {
							for (Element ele : spotlightElements) {
								if (sCount != 1 && sCount != 5) {
									Element h3Element = ele.getElementsByTag(
											"h3").first();
									if (h3Element != null) {
										Element aEle = h3Element.getElementsByTag("a").first();
										if(aEle != null){
											h3Text = aEle.text()+h3Element.ownText();
											aHref = aEle.absUrl("href");
											// Start extracting valid href
											log.debug("Before linkTitleUrl" + aHref + "\n");
											aHref = FrameworkUtils.getLocaleReference(aHref, urlMap);
											log.debug("after linkTitleUrl" + aHref + "\n");
											// End extracting valid href
										}else{
											h3Text = h3Element.text();
											sb.append(Constants.SPOTLIGHT_TITLELINK_NODE_NOT_FOUND);
										}
										
									} else {
										sb.append(Constants.SPOTLIGHT_HEADING_ELEMENT_NOT_FOUND);
									}
									Element pElements = ele
											.getElementsByTag("p").first();
									if (pElements != null) {
										pText = FrameworkUtils.extractHtmlBlobContent(pElements, "",locale, sb, urlMap);
									} else {
										sb.append(Constants.SPOTLIGHT_DESCRIPTION_ELEMENT_NOT_FOUND);
									}
									if (spotlightNodeIterator.hasNext()) {
										Node spotlightNode = (Node) spotlightNodeIterator
												.next();

										handleSpotlightMigration(spotlightNode,
												ele, locale, h3Text, pText, aHref);
									} else {
										sb.append(Constants.SPOTLIGHT_NODE_NOT_FOUND);
									}
								} else if (sCount == 5) {
									pText = spotlightElements.select("p")
											.first().ownText();
									sb.append("<li>paragarph " + pText
											+ "extra in web page</li>");
								}
								sCount++;
							}
						} else {
							sb.append(Constants.SPOTLIGHT_ELEMENT_NOT_FOUND);
						}
					}

				} catch (Exception e) {
					sb.append(Constants.UNABLE_TO_UPDATE_SPOTLIGHT);
					log.error("Exception ", e);
				}
				// end of spotlight components
				// ----------------------------------------------------------------------------------------------------------------------------
				// start of list components
				try {
					String h2Text = "";
					String introText = "";
					int count = 1;
					String aText = "";
					String aHref = "";
					String ownPdfText = "";
					String ownText = "";
					String pdfIcon = "";
					String pdfSize = "";
					Node leftListContainerNode = null;
					Node midListContainerNode = null;
					Node rightListContainerNode = null;
					Node leftListItemsNode = null;
					Node midListItemsNode = null;
					Node rightListItemsNode = null;
					Node listContainerNode = null;
					Node listNode = null;
					Node listItemsNode = null;
					if (serviceListingNode
							.hasNode("thirds/Th-Third-1/list_container")) {
						leftListContainerNode = serviceListingNode
								.getNode("thirds/Th-Third-1/list_container");
					}
					if (serviceListingNode
							.hasNode("thirds/Th-Third-2/list_container")) {
						midListContainerNode = serviceListingNode
								.getNode("thirds/Th-Third-2/list_container");
					}
					if (serviceListingNode
							.hasNode("thirds/Th-Third-3/list_container")) {
						rightListContainerNode = serviceListingNode
								.getNode("thirds/Th-Third-3/list_container");
					}
					if (leftListContainerNode
							.hasNode("list_item_parsys/list_content/listitems")) {
						leftListItemsNode = leftListContainerNode
								.getNode("list_item_parsys/list_content/listitems");
					}
					if (midListContainerNode
							.hasNode("list_item_parsys/list_content/listitems")) {
						midListItemsNode = midListContainerNode
								.getNode("list_item_parsys/list_content/listitems");
					}
					if (rightListContainerNode
							.hasNode("list_item_parsys/list_content/listitems")) {
						rightListItemsNode = rightListContainerNode
								.getNode("list_item_parsys/list_content/listitems");
					}
					Elements listElements = doc.select("div.gd23-pilot")
							.select("div.c00-pilot");

					if (!listElements.isEmpty()) {
						for (Element ele : listElements) {
							Element h2Elements = ele.getElementsByTag("h2")
									.first();
							if (h2Elements != null) {
								h2Text = h2Elements.text();
							} else {
								sb.append(Constants.LIST_HEADING_COMPONENT_NOT_FOUND);
							}
							Elements pElements = ele.getElementsByTag("p");
							if (!pElements.isEmpty()) {
								introText = pElements.first().text();
								aText = pElements.last().text();
							}
							Elements aElements = ele.getElementsByTag("a");
							if (aElements == null) {
								sb.append(Constants.LIST_ANCHOR_ELEMENTS_NOT_FOUND);
							}
							if (count == 1) {
								if (leftListContainerNode != null) {
									if (h2Text != null) {
										leftListContainerNode.setProperty(
												"title", h2Text);
									}
									if (leftListItemsNode != null
											&& leftListItemsNode.hasNodes()) {
										NodeIterator listItemsIterator = leftListItemsNode
												.getNodes("item*");
										for (Element aEle : aElements) {
											if (listItemsIterator.hasNext()) {
												Node listItemNode = (Node) listItemsIterator
														.next();
												//start handle pdf in wireless
												Element liElement = aEle.parent();
												ownPdfText = liElement.ownText();
												if(StringUtils.isNotEmpty(ownPdfText)){
													log.debug("OWn text is:"+ownPdfText);
													if(ownPdfText.toLowerCase().contains("pdf")){
														pdfIcon = "pdf";
														int i=0;
														for(;i<ownPdfText.length();i++){
															char character = ownPdfText.charAt(i);												
															boolean isDigit = Character.isDigit(character);
															if(isDigit){
																break;
															} 
													}	
													pdfSize = ownPdfText.substring(i, ownPdfText.length()-1);
													pdfSize = pdfSize.replace(")", "");
													pdfSize = pdfSize.trim();
													log.debug("Pdf size set to the node:"+listItemNode.getPath());
													listItemNode.setProperty("icon", pdfIcon);
													listItemNode.setProperty("size", pdfSize);
												}
												else{
													ownText = ownPdfText;
													log.debug("ownText:"+ownText);
												}
												}
												log.debug("Pdf size:"+pdfSize);
												log.debug("Pdf icon:"+pdfIcon);
												//end handle pdf in wireless
												listItemNode = listItemNode
														.getNode("linkdata");
												if (pElements.isEmpty()) {
													aText = aEle.text()+ownText;
												}
												aHref = aEle.absUrl("href");
												// Start extracting valid href
												log.debug("Before linkUrl" + aHref + "\n");
												aHref = FrameworkUtils.getLocaleReference(aHref, urlMap);
												log.debug("after linkUrl" + aHref + "\n");
												// End extracting valid href
												if (aText != null) {
													listItemNode.setProperty(
															"linktext", aText);
												} else {
													sb.append(Constants.LINK_TEXT_NOT_FOUND_IN_LIST);
												}
												if (aHref != null) {
													listItemNode.setProperty(
															"url", aHref);
												} else {
													sb.append(Constants.LINK_URL_NOT_FOUND_IN_LIST);
												}
											}
										}
									} else {
										sb.append(Constants.LEFT_LIST_ITEMS_NODE_NOT_FOUND);
									}
								} else {
									sb.append(Constants.LEFT_LIST_HEDAING_NODE_NOT_FOUND);
								}
							} else {
								if (count == 2) {
									listContainerNode = midListContainerNode;
									listNode = midListItemsNode;
								} else if (count == 3) {
									listContainerNode = rightListContainerNode;
									listNode = rightListItemsNode;
								}
								if (listContainerNode != null) {
									if (h2Text != null) {
										listContainerNode.setProperty("title",
												h2Text);
									}
									if (introText != null) {
										listContainerNode.setProperty(
												"intropara", introText);
									} else {
										sb.append(Constants.LIST_INTRO_PARAGRAPH_ELEMENT_NOT_FOUND);
									}
									if (listNode.hasNode("item_1/linkdata")) {
										listItemsNode = listNode
												.getNode("item_1/linkdata");
									}
									if (listItemsNode != null) {
										if (pElements.isEmpty()) {
											aText = aElements.first().text();
										}
										aHref = aElements.first().absUrl("href");
										// Start extracting valid href
										log.debug("Before linkUrl" + aHref + "\n");
										aHref = FrameworkUtils.getLocaleReference(aHref, urlMap);
										log.debug("after linkUrl" + aHref + "\n");
										// End extracting valid href
										if (aText != null) {
											listItemsNode.setProperty(
													"linktext", aText);
										} else {
											sb.append(Constants.LINK_TEXT_NOT_FOUND_IN_LIST);
										}
										if (aHref != null) {
											listItemsNode.setProperty("ulr",
													aHref);
										} else {
											sb.append(Constants.LINK_URL_NOT_FOUND_IN_LIST);
										}

									} else {
										if (count == 2)
											sb.append(Constants.MID_LIST_ITEMS_NODE_NOT_FOUND);
										else if (count == 3)
											sb.append(Constants.RIGHT_LIST_ITEMS_NODE_NOT_FOUND);
									}

								} else {
									if (count == 2)
										sb.append(Constants.MID_LIST_HEDAING_NODE_NOT_FOUND);
									else if (count == 3)
										sb.append(Constants.RIGHT_LIST_HEDAING_NODE_NOT_FOUND);
								}
							}
							count++;
						}

					} else {
						sb.append(Constants.LIST_ELEMENT_NOT_FOUND);
					}

				} catch (Exception e) {
					sb.append(Constants.UNABLE_TO_MIGRATE_LIST_COMPONENT);
					log.error("Exception ", e);
				}
				// end of list components
				session.save();
			} else {
				sb.append(Constants.URL_CONNECTION_EXCEPTION);
			}
		} catch (Exception e) {
			sb.append(Constants.UNABLE_TO_MIGRATE_PAGE);
			log.debug("Exception as url cannot be connected: " + e);
			log.error("Exception ", e);
		}

		sb.append("</ul></td>");

		return sb.toString();
	}

	public void handleSpotlightMigration(Node spotlightNode, Element ele,
			String locale, String hText, String pText, String aHref) {
		try {
			// start image
			String spotLightImage = FrameworkUtils.extractImagePath(ele, sb);
			log.debug("spotLightImage befor migration : " + spotLightImage
					+ "\n");
			if (spotlightNode.hasNode("image")) {
				Node spotLightImageNode = spotlightNode.getNode("image");
				String fileReference = spotLightImageNode
						.hasProperty("fileReference") ? spotLightImageNode
						.getProperty("fileReference").getString() : "";
				spotLightImage = FrameworkUtils.migrateDAMContent(
						spotLightImage, fileReference, locale, sb);
				log.debug("spotLightImage after migration : " + spotLightImage
						+ "\n");
				if (StringUtils.isNotBlank(spotLightImage)) {
					spotLightImageNode.setProperty("fileReference",
							spotLightImage);
				}
			} else {
				sb.append(Constants.SPOTLIGHT_IMAGE_NODE_NOT_AVAILABLE);
			}
			// end image

			if (hText != null) {
				spotlightNode.setProperty("title", hText);
			}
			if (aHref != null && StringUtils.isNotEmpty("aHref")) {
				if (spotlightNode.hasNode("titleLink")) {
					Node spotLightTitleLinkNode = spotlightNode.getNode("titleLink");
					spotLightTitleLinkNode.setProperty("linktype", "Url");
					spotLightTitleLinkNode.setProperty("url", aHref);
				}
			}
			if (pText != null) {
				spotlightNode.setProperty("description", pText);
			}
		} catch (Exception e) {
			log.error("Exception:", e);
		}
	}
}
