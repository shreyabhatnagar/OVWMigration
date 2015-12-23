package com.cisco.dse.global.migration.rservicelisting;

import java.io.IOException;

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
import org.jsoup.Jsoup;

import com.cisco.dse.global.migration.config.Constants;
import com.cisco.dse.global.migration.config.FrameworkUtils;

public class RServiceListingVariation1 {
	Document doc;
	StringBuilder sb = new StringBuilder(1024);
	static Logger log = Logger.getLogger(RServiceListingVariation1.class);

	public String translate(String host, String loc, String prod, String type,
			String catType, String locale, Session session) throws IOException,
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

			doc = Jsoup.connect(loc).get();

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
							sb.append("<li>Heading elements not found in web publisher page.</li>");
						}
					} else {
						headingElements = doc.select("div.gd-right")
								.select("div.cc00-pilot").first();
						if(headingElements != null){
							h1Text = headingElements.html();
						}else{
							sb.append("<li>Heading elements not found in web publisher page.</li>");
						}
					}
					// setting data
					Node headerNode = serviceListingNode.hasNode("header") ? serviceListingNode
							.getNode("header") : null;
					if (headerNode != null) {
						log.debug("header node path:" + headerNode.getPath());
						log.debug("h1Text:" + h1Text);
						log.debug("title property:"
								+ headerNode.hasProperty("title"));
						headerNode.setProperty("title", h1Text);
					} else {
						sb.append("<li>Header node not found.</li>");
					}

				} catch (Exception e) {
					sb.append("<li>Heading cannot be migrated.</li>");
				}

				// end of heading component
				// ------------------------------------------------------------------------------------------------------------------------------
				// start of spotlight components
				try {
					String h2Text = "";
					String h3Text = "";
					String pText = "";
					int count = 1;
					Elements spotlightElements = doc.select("div.gd-right")
							.select("div.c11-pilot");
					NodeIterator spotlightNodeIterator = serviceListingNode
							.hasNode("spotlight") ? serviceListingNode
							.getNodes("spotlight*") : null;
					if (!spotlightElements.isEmpty()) {
						for (Element ele : spotlightElements) {
							log.debug("Loop run:" + count);
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
								descText.append(pElement.outerHtml());
							} else {
								sb.append(Constants.SPOTLIGHT_DESCRIPTION_ELEMENT_NOT_FOUND);
							}
							Element ulElement = ele.getElementsByTag("ul")
									.first();
							if (ulElement != null) {
								/*//log.debug("old ul Elements:" + ulElement);
								String newElements = FrameworkUtils
										.extractHtmlBlobContent(ulElement, "",
												locale, sb);
								//log.debug("new ul elements:" + newElements);
*/								descText.append(ulElement.outerHtml());
							} else {
								sb.append("<li>SPOTLIGHT_LIST_ELEMENTS_NOT_FOUND</li>");
							}
							if (spotlightNodeIterator.hasNext()) {
								Node spotlightNode = (Node) spotlightNodeIterator
										.next();

								// start image

								String spotLightImage = FrameworkUtils
										.extractImagePath(ele, sb);
								log.debug("spotLightImage befor migration : "
										+ spotLightImage + "\n");
								if (spotlightNode.hasNode("image")) {
									Node spotLightImageNode = spotlightNode
											.getNode("image");
									String fileReference = spotLightImageNode
											.hasProperty("fileReference") ? spotLightImageNode
											.getProperty("fileReference")
											.getString() : "";
									spotLightImage = FrameworkUtils
											.migrateDAMContent(spotLightImage,
													fileReference, locale, sb);
									log.debug("spotLightImage after migration : "
											+ spotLightImage + "\n");
									if (StringUtils.isNotBlank(spotLightImage)) {
										spotLightImageNode
												.setProperty("fileReference",
														spotLightImage);
									}
								} else {
									sb.append("<li>spotlight image node doesn't exist</li>");
								}
								// end image

								if (h2Text != null) {
									log.debug("h2Text:" + h2Text);
									log.debug("spotlight node path:"
											+ spotlightNode.getPath());
									spotlightNode.setProperty("title", h2Text);
								}
								if (descText != null) {
									log.debug("descText:" + descText);
									spotlightNode.setProperty("description",
											descText.toString());
								}
							} else {
								sb.append(Constants.SPOTLIGHT_NODE_NOT_FOUND);
							}
							count++;
						}
					} else {
						int sCount = 1;
						spotlightElements = doc.select("div.gd-right")
								.select("div.cc00-pilot");
						if(!spotlightElements.isEmpty()){
						for(Element ele:spotlightElements){
							log.debug("sCount:"+sCount);
							if(sCount!=1 &&sCount != 5){
								log.debug("sCount:"+sCount);
								log.debug("Loop run:" + count);
								Element h3Element = ele.getElementsByTag("h3")
										.first();
								if (h3Element != null) {
									h3Text = h3Element.text();
								} else {
									sb.append(Constants.SPOTLIGHT_HEADING_ELEMENT_NOT_FOUND);
								}
								Elements pElements = ele.getElementsByTag("p");
								if (pElements != null) {
									pText = pElements.html();
								} else {
									sb.append(Constants.SPOTLIGHT_DESCRIPTION_ELEMENT_NOT_FOUND);
								}
								if (spotlightNodeIterator.hasNext()) {
									Node spotlightNode = (Node) spotlightNodeIterator
											.next();

									// start image

									String spotLightImage = FrameworkUtils
											.extractImagePath(ele, sb);
									log.debug("spotLightImage befor migration : "
											+ spotLightImage + "\n");
									if (spotlightNode.hasNode("image")) {
										Node spotLightImageNode = spotlightNode
												.getNode("image");
										String fileReference = spotLightImageNode
												.hasProperty("fileReference") ? spotLightImageNode
												.getProperty("fileReference")
												.getString() : "";
										spotLightImage = FrameworkUtils
												.migrateDAMContent(spotLightImage,
														fileReference, locale, sb);
										log.debug("spotLightImage after migration : "
												+ spotLightImage + "\n");
										if (StringUtils.isNotBlank(spotLightImage)) {
											spotLightImageNode
													.setProperty("fileReference",
															spotLightImage);
										}
									} else {
										sb.append("<li>spotlight image node doesn't exist</li>");
									}
									// end image

									if (h3Text != null) {
										spotlightNode.setProperty("title", h3Text);
									}
									if (pText != null) {
										spotlightNode.setProperty("description",
												pText);
									}
								} else {
									sb.append(Constants.SPOTLIGHT_NODE_NOT_FOUND);
								}
							}
							else if(sCount == 5){
								pText = spotlightElements.select("p").first().ownText();
								sb.append("<li>paragarph "+pText+"extra in web page</li>");
							}
							sCount++;
						}
						}else{
							sb.append(Constants.SPOTLIGHT_ELEMENT_NOT_FOUND);
						}
					}

				} catch (Exception e) {
					sb.append(Constants.UNABLE_TO_UPDATE_SPOTLIGHT);
					log.debug("Exception:" + e);
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
					Node leftListContainerNode = null;
					Node midListContainerNode = null;
					Node rightListContainerNode = null;
					Node leftListItemsNode = null;
					Node midListItemsNode = null;
					Node rightListItemsNode = null;
					leftListContainerNode = serviceListingNode
							.getNode("thirds/Th-Third-1/list_container");
					midListContainerNode = serviceListingNode
							.getNode("thirds/Th-Third-2/list_container");
					rightListContainerNode = serviceListingNode
							.getNode("thirds/Th-Third-3/list_container");

					leftListItemsNode = leftListContainerNode
							.getNode("list_item_parsys/list_content/listitems");
					midListItemsNode = midListContainerNode
							.getNode("list_item_parsys/list_content/listitems");
					rightListItemsNode = rightListContainerNode
							.getNode("list_item_parsys/list_content/listitems");
					log.debug("Left list node path:"
							+ leftListContainerNode.getPath());
					Elements listElements = doc.select("div.gd23-pilot")
							.select("div.c00-pilot");

					if (!listElements.isEmpty()) {
						for (Element ele : listElements) {
							log.debug("List loop run:"+count);
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
								sb.append("<li>List anchor elements not found.</li>");
							}
							if (count == 1) {
								if (leftListContainerNode != null) {
									if (h2Text != null) {
										log.debug("List heading:"+h2Text);
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
												listItemNode = listItemNode
														.getNode("linkdata");
												if (pElements.isEmpty()) {
												aText = aEle.text();
												}
												aHref = aEle.attr("href");
												if (aText != null) {
													log.debug("aText:" + aText);
													listItemNode.setProperty(
															"linktext", aText);
												} else {
													sb.append("<li>Link text not found.</li>");
												}
												if (aHref != null) {
													log.debug("aHref:" + aHref);
													listItemNode.setProperty(
															"ulr", aHref);
												} else {
													sb.append("<li>Link href not found.</li>");
												}
											}
										}
									} else {
										sb.append("<li>List items node not found</li>");
									}
								} else {
									sb.append("<li>Left list_container node not found.</li>");
								}
							} else if (count == 2) {
								if (midListContainerNode != null) {
									if (h2Text != null) {
										log.debug("List heading:"+h2Text);
										midListContainerNode.setProperty(
												"title", h2Text);
									}
									if (introText != null) {
										log.debug("List para:"+introText);
										midListContainerNode.setProperty(
												"intropara", introText);
									} else {
										sb.append("<li>List intropara canot be migrated</li>");
									}
									Node listItemsNode = midListItemsNode
											.getNode("item_1/linkdata");
									if (listItemsNode != null) {
										if (pElements.isEmpty()) {
										aText = aElements.first().text();
										}
										aHref = aElements.first().attr("href");
										if (aText != null) {
											log.debug("aText:" + aText);
											listItemsNode.setProperty(
													"linktext", aText);
										} else {
											sb.append("<li>Link text not found.</li>");
										}
										if (aHref != null) {
											log.debug("aHref:" + aHref);
											listItemsNode.setProperty("ulr",
													aHref);
										} else {
											sb.append("<li>Link href not found.</li>");
										}

									} else {
										sb.append("<li>List items node not found</li>");
									}

								} else {
									sb.append("<li>Mid list_container node not found.</li>");
								}
							} else if (count == 3) {
								if (rightListContainerNode != null) {
									if (h2Text != null) {
										log.debug("List heading:"+h2Text);
										rightListContainerNode.setProperty(
												"title", h2Text);
									}
									if (introText != null) {
										log.debug("List para:"+introText);
										rightListContainerNode.setProperty(
												"intropara", introText);
									} else {
										sb.append("<li>List intropara canot be migrated</li>");
									}
									Node listItemsNode = rightListItemsNode
											.getNode("item_1/linkdata");
									if (listItemsNode != null) {
										if (pElements.isEmpty()) {
										aText = aElements.first().text();
										}
										aHref = aElements.first().attr("href");
										if (aText != null) {
											log.debug("aText:" + aText);
											listItemsNode.setProperty(
													"linktext", aText);
										} else {
											sb.append("<li>Link text not found.</li>");
										}
										if (aHref != null) {
											log.debug("aHref:" + aHref);
											listItemsNode.setProperty("ulr",
													aHref);
										} else {
											sb.append("<li>Link href not found.</li>");
										}

									} else {
										sb.append("<li>List items node not found</li>");
									}

								} else {
									sb.append("<li>right list_container node not found.</li>");
								}
							}
							count++;
						}

					} else {
						sb.append(Constants.LIST_ELEMENT_NOT_FOUND);
					}

				} catch (Exception e) {
					sb.append(Constants.UNABLE_TO_MIGRATE_LIST_COMPONENT);
					e.printStackTrace();
				}
				// end of list components
				session.save();
			} else {
				sb.append(Constants.URL_CONNECTION_EXCEPTION);
			}
		} catch (Exception e) {
			sb.append(Constants.URL_CONNECTION_EXCEPTION);
			log.debug("Exception as url cannot be connected: " + e);
		}

		sb.append("</ul></td>");

		return sb.toString();
	}
}
