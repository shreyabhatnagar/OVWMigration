package com.cisco.dse.global.migration.architecture;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
import org.apache.sling.commons.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.cisco.dse.global.migration.config.BaseAction;
import com.cisco.dse.global.migration.config.Constants;
import com.cisco.dse.global.migration.config.FrameworkUtils;

public class ArchitectureVariation04 extends BaseAction {
	Document doc;
	StringBuilder sb = new StringBuilder(1024);
	static Logger log = Logger.getLogger(ArchitectureVariation04.class);

	public String translate(String host, String loc, String prod, String type,
			String catType, String locale, Session session) throws IOException,
			ValueFormatException, VersionException, LockException,
			ConstraintViolationException, RepositoryException {
		log.debug("In the translate method of ArchitectureVariation04");
		log.debug("In the translate method, catType is :" + catType);

		// Repo node paths

		String pageUrl = host + "/content/<locale>/" + catType
				+ "/<prod>/architecture.html";
		pageUrl = pageUrl.replace("<locale>", locale).replace("<prod>", prod);

		sb.append("<td>" + "<a href=" + pageUrl + ">" + pageUrl + "</a>"
				+ "</td>");
		sb.append("<td>" + "<a href=" + loc + ">" + loc + "</a>" + "</td>");
		sb.append("<td><ul>");

		String architectureLeftPath = "/content/<locale>/"
				+ catType
				+ "/<prod>/architecture/jcr:content/content_parsys/architecture/layout-architecture/gd12v2/gd12v2-left";
		String architectureRightPath = "/content/<locale>/"
				+ catType
				+ "/<prod>/architecture/jcr:content/content_parsys/architecture/layout-architecture/gd12v2/gd12v2-right";
		architectureLeftPath = architectureLeftPath.replace("<locale>", locale)
				.replace("<prod>", prod);
		architectureRightPath = architectureRightPath.replace("<locale>",
				locale).replace("<prod>", prod);
		javax.jcr.Node architectureLeftNode = null;
		javax.jcr.Node architectureRightNode = null;
		try {

			architectureLeftNode = session.getNode(architectureLeftPath);
			architectureRightNode = session.getNode(architectureRightPath);
			doc = getConnection(loc);

			if (doc != null) {
				Elements allListElements = doc.select("div.gd-left").select(
						"n13-pilot");
				log.debug(allListElements.size());
				if (allListElements.size() == 1) {
					// start of text component and first list properties setting
					try {
						String textProp = "";
						String listIntroText = "";
						String aText = "";
						String linkurl = "";
						String description = "";
						StringBuilder textContent = new StringBuilder();
						List<String> list = new ArrayList<String>();
						Elements divElements = doc.select("div.gd22v2-left")
								.select("div.c00-pilot,div.c100-dm");
						if (divElements != null) {
							Element h2Element = divElements.select("h2")
									.first();
							Element pElement = divElements.select("p").first();
							if (h2Element != null || pElement != null) {
								textContent.append(h2Element);
								textContent.append(pElement);
								textProp = textContent.toString();
							} else {
								sb.append("<li>Text Elements not exists</li>");
							}
							log.debug("text Prop" + textProp);
							Element listInroElement = divElements.select("p")
									.last();
							if (listInroElement != null) {
								listIntroText = listInroElement.html();
							} else {
								sb.append("<li>List Paragraph not found</li>");
							}
							Elements aElements = divElements.select("li");
							log.debug("a elements:" + aElements);
							for (Element ele : aElements) {
								JSONObject obj = new JSONObject();
								Elements aEle = ele.getElementsByTag("a");
								if (aEle == null) {
									aText = ele.text();
								} else {
									aText = aEle.text();
									linkurl = aEle.attr("href");
									description = ele.ownText();
								}
								// String aHref = ele.attr("href");
								obj.put("linktext", aText);
								obj.put("linkurl", linkurl);
								obj.put("icon", "none");
								obj.put("size", "");
								obj.put("description", description);
								obj.put("openInNewWindow", false);
								list.add(obj.toString());
							}
						}
						Node textNode = architectureLeftNode.hasNode("text") ? architectureLeftNode
								.getNode("text") : null;
						if (textNode != null) {
							if (textProp != null) {
								textNode.setProperty("text", textProp);
							}
						} else {
							sb.append(Constants.TEXT_NODE_NOT_FOUND);
						}
						if (architectureLeftNode.hasNode("list_0")) {
							Node listNode = architectureLeftNode
									.getNode("list_0");
							if (listNode.hasNode("intro")) {
								Node introNode = listNode.getNode("intro");
								introNode.setProperty("paragraph_rte",
										listIntroText);
							} else {
								sb.append("<li>List Intro node not found</li>");
							}
							if (listNode.hasNode("element_list_0")) {
								Node element_list_0 = listNode
										.getNode("element_list_0");
								int size = list.size();
								if (size > 1) {
									element_list_0
											.setProperty("listitems", list
													.toArray(new String[list
															.size()]));
								} else {
									element_list_0.setProperty("listitems",
											list.get(0));
								}
							} else {
								sb.append("<li>Element_list node not found for list node</li>");
							}
						} else {
							sb.append("<li>List Node not found</li>");
						}

					} catch (Exception e) {
						sb.append(Constants.EXCEPTION_TEXT_COMPONENT);
					}
					// end of text component and first list properties setting
					// -------------------------------------------------------------------------------------------------------------------
					// start of middle content
					try {
						// getting middle content
						Element listElements = doc.select("div.parbase")
								.select("div.c00-pilot,div.c100-dm").last();
						if (listElements != null) {
							int childrenSize = listElements.children().size();
							List<String> headerList = new ArrayList<String>();
							List<String> paraList = new ArrayList<String>();
							List<Element> ulList = new ArrayList<Element>();
							String paraContent = "";
							String previousHeader = "";

							for (int count = 0; count < childrenSize; count++) {
								Element child = listElements.child(count);
								if (child != null) {
									if ("h3".equalsIgnoreCase(child.tagName())) {
										headerList.add(child.outerHtml());
										if (!paraContent.isEmpty()) {
											// Report content comes here
											sb.append("<li>The last paragraph element under heading '"
													+ previousHeader
													+ "' is not migrated from locale page.</li>");
											paraContent = "";
										}
										previousHeader = child.html();
									} else if ("p".equalsIgnoreCase(child
											.tagName())) {
										paraContent = paraContent
												+ child.outerHtml();
									} else if ("ul".equalsIgnoreCase(child
											.tagName())) {
										paraList.add(paraContent);
										paraContent = "";
										ulList.add(child);
									}
								}
							}
							// setting middle content
							if (architectureLeftNode.hasNode("gd22v2")) {
								Node gdListNode = architectureLeftNode.getNode(
										"gd22v2").getNode("gd22v2-left");
								NodeIterator listNodeIterator = gdListNode
										.getNodes("list*");
								Node listNode = null;
								for (int loop = 0; loop < headerList.size(); loop++) {
									log.debug("for loop run:" + loop);
									if (loop < 2) {
										listNode = (Node) listNodeIterator
												.next();
									} else if (loop == 2) {
										listNode = architectureLeftNode
												.hasNode("list_2") ? architectureLeftNode
												.getNode("list_2") : null;
									} else if (loop == 3) {
										listNode = architectureLeftNode
												.hasNode("list_1") ? architectureLeftNode
												.getNode("list_1") : null;
									}
									if (listNode != null) {
										String h2Text = "";
										String pText = "";
										List<String> list = new ArrayList<String>();
										log.debug(listNode);
										h2Text = headerList.get(loop);
										pText = paraList.get(loop);
										if (h2Text != null) {
											listNode.setProperty("title",
													h2Text);
										} else {
											sb.append("<li>Title element of list component not found</li>");
										}
										if (listNode.hasNode("intro")) {
											Node introNode = listNode
													.getNode("intro");
											if (pText != null) {
												introNode.setProperty(
														"paragraph_rte", pText);
											} else {
												sb.append("<li>Intro paragraph element of list component not found</li>");
											}
										} else {
											sb.append("<li>List Intro node not found</li>");
										}
										Elements liElements = ulList.get(loop)
												.select("li");
										for (Element ele : liElements) {
											JSONObject obj = new JSONObject();
											String aText = ele.html();
											obj.put("linktext", aText);
											obj.put("linkurl", "");
											obj.put("icon", "none");
											obj.put("size", "");
											obj.put("description", "");
											obj.put("openInNewWindow", false);
											list.add(obj.toString());
										}
										if (listNode.hasNode("element_list_0")) {
											Node element_list_0 = listNode
													.getNode("element_list_0");
											boolean multiple = element_list_0
													.getProperty("listitems")
													.isMultiple();
											if (multiple) {
												element_list_0
														.setProperty(
																"listitems",
																list.toArray(new String[list
																		.size()]));
											} else {
												element_list_0.setProperty(
														"listitems",
														list.toString());
											}
										} else {
											sb.append("<li>Element_list node not found for list node</li>");
										}
									} else {
										sb.append("<li>List Node is not exists</li>");
									}
								}
							} else {
								sb.append("<li>gd22v2 node not exists</li>");
							}
						} else {
							sb.append("<li>Middle section list elements not found</li>");
						}
					} catch (Exception e) {
						sb.append("<li>Unable to migrate middle list conetent</li>");
					}
					// end of middle content migration
					// -----------------------------------------------------------------------------------------------------------------------------
					// start html blob
					handleHtmlBolb(architectureLeftNode, locale);
					// end html blob
					// -----------------------------------------------------------------------------------------------------------------------------
					// start of last list in left section
					try {
						String h2Text = "";
						String text = "";
						// boolean anchor = true;
						List<String> list = new ArrayList<String>();
						Element listElements = doc.select("div.gd-left")
								.select("div.n13-dm,div.n13-pilot").last();
						log.debug("list elements:" + listElements);
						if (listElements != null) {
							Elements h2Elements = listElements
									.select("h2.bdr-1");
							if (h2Elements != null && !h2Elements.isEmpty()) {
								Element h2Element = h2Elements.first();
								if (h2Element != null) {
									h2Text = h2Element.text();
								} else {
									sb.append("<li>No heading element for last list section in left part</li>");
								}
								Elements aElements = listElements.select("li");
								for (Element ele : aElements) {
									JSONObject obj = new JSONObject();
									Elements aEle = ele.getElementsByTag("a");
									String pdf = ele.ownText();
									String pdfIcon = null;
									if (pdf.length() > 0) {
										if (pdf.toLowerCase().contains("pdf"))
											pdfIcon = "pdf";
										int i = 0;
										for (; i < pdf.length(); i++) {
											char character = pdf.charAt(i);
											boolean isDigit = Character
													.isDigit(character);
											if (isDigit) {
												break;
											}
										}
										pdf = pdf
												.substring(i, pdf.length() - 1);
									}
									// pdf = pdf.replace(")", "");
									pdf = pdf.trim();
									String aText = aEle.text();
									String aHref = aEle.attr("href");
									obj.put("linktext", aText);
									obj.put("linkurl", aHref);
									obj.put("icon", pdfIcon);
									obj.put("size", pdf);
									obj.put("description", text);
									obj.put("openInNewWindow", false);
									list.add(obj.toString());
								}
							} else {
								sb.append("<li>No heading element for last list section in left part</li>");
							}
						} else {
							sb.append("<li>Last list section in left part doesn't found</li>");
						}
						// End get content.
						// Start set content.
						if (architectureLeftNode.hasNode("list")) {
							Node listNode = architectureLeftNode
									.getNode("list");
							if (StringUtils.isNotBlank(h2Text)) {
								listNode.setProperty("title", h2Text);
							}
							if (listNode.hasNode("element_list_0")) {
								Node element_list_0 = listNode
										.getNode("element_list_0");
								int size = list.size();
								if (size > 1) {
									element_list_0
											.setProperty("listitems", list
													.toArray(new String[list
															.size()]));
								} else {
									element_list_0.setProperty("listitems",
											list.get(0));
								}
							} else {
								sb.append("<li>Element_list node of list node not found</li>");
							}
						} else {
							sb.append("<li>List node not found</li>");
						}
						// End set content.
					} catch (Exception e) {
						log.error("Exception : ", e);
						sb.append("<li>Unable to migrate last list</li>");
					}

					// end of last list in left section
					// --------------------------------------------------------------------------------------------------------------------------------------
					// start tile section
					handleTileSection(architectureRightNode);
					// end of tile section
				} else {

					// start of text component
					try {
						String textProp = null;
						Elements textElements = doc
								.select("div.c00-pilot,div.c100-dm");
						if (textElements != null) {
							for (Element ele : textElements) {
								if (architectureLeftNode.hasNode("text")) {
									NodeIterator textNodeIterator = architectureLeftNode
											.getNodes("text*");
									if (textNodeIterator.hasNext()) {
										Node textNode = (Node) textNodeIterator
												.next();
										textProp = ele.text();
										textNode.setProperty("text", textProp);
									}
								} else {
									sb.append("<li>Text Node not found</li>");
								}
							}
						} else {
							sb.append("<li>Text Elements not found</li>");
						}
					} catch (Exception e) {
						sb.append(Constants.EXCEPTION_TEXT_COMPONENT);
					}
					// end of text component
					// --------------------------------------------------------------------------------------------------------------------------------------
					// start of migrating list components
					try {
						int count = 1;
						String aText = "";
						String pdf = "";
						String pdfIcon = "";
						String linkurl = "";
						String description = "";
						Node listNode = null;
						for (Element ele : allListElements) {
							NodeIterator insideListIterator = architectureLeftNode
									.getNode("gd22v2").getNode("gd22v2-left")
									.getNodes("list*");
							NodeIterator outsideListIterator = architectureLeftNode
									.getNodes("list*");
							if (count == 2 || count == 3) {
								listNode = (Node) insideListIterator.next();
							} else {
								listNode = (Node) outsideListIterator.next();
							}
							if (listNode != null) {
								String h2Text = "";
								String pText = "";
								List<String> list = new ArrayList<String>();
								Elements h2Elements = ele.select("h3,h2");
								if (h2Elements != null) {
									h2Text = h2Elements.text();
									listNode.setProperty("title", h2Text);
								} else {
									sb.append("<li>Title element of list component not found</li>");
								}
								Elements pElements = ele.select("p");
								if (pElements != null) {
									pText = pElements.text();
									if (listNode.hasNode("intro")) {
										Node introNode = listNode
												.getNode("intro");
										if (pText != null) {
											introNode.setProperty(
													"paragraph_rte", pText);
										}
									} else {
										sb.append("<li>List Intro node not found</li>");
									}
								} else {
									sb.append("<li>Intro paragraph element of list component not found</li>");
								}
								Elements liElements = ele.select("li");
								for (Element liEle : liElements) {
									if (liEle.ownText() != null) {
										description = liEle.ownText();
									}
									JSONObject obj = new JSONObject();
									Element aElements = liElements.select("a")
											.first();
									if (aElements != null) {
										aText = aElements.html();
										linkurl = aElements.attr("href");
										pdf = aElements.ownText();
										if (pdf.length() > 0) {
											if (pdf.toLowerCase().contains(
													"pdf"))
												pdfIcon = "pdf";
											int i = 0;
											for (; i < pdf.length(); i++) {
												char character = pdf.charAt(i);
												boolean isDigit = Character
														.isDigit(character);
												if (isDigit) {
													break;
												}
											}
											pdf = pdf.substring(i,
													pdf.length() - 1);
										}
										pdf = pdf.trim();
									} else {
										aText = liEle.html();
									}
									obj.put("linktext", aText);
									obj.put("linkurl", linkurl);
									obj.put("icon", pdfIcon);
									obj.put("size", pdf);
									obj.put("description", description);
									obj.put("openInNewWindow", false);
									list.add(obj.toString());
								}
								if (listNode.hasNode("element_list_0")) {
									Node element_list_0 = listNode
											.getNode("element_list_0");
									boolean multiple = element_list_0
											.getProperty("listitems")
											.isMultiple();
									if (multiple) {
										element_list_0.setProperty("listitems",
												list.toArray(new String[list
														.size()]));
									} else {
										element_list_0.setProperty("listitems",
												list.toString());
									}
								} else {
									sb.append("<li>Element_list node not found for list node</li>");
								}
							} else {
								sb.append("<li>List Node is not exists</li>");
							}
							count++;
						}
					} catch (Exception e) {
						sb.append("<li>Cannot migrate list components.</li>");
					}
					// end of migrating list components
					// -------------------------------------------------------------------------------------------------------------------------
					// start tile section
					handleTileSection(architectureRightNode);

					// end tile section
					// --------------------------------------------------------------------------------------------------------------------
					// start html blob
					handleHtmlBolb(architectureLeftNode, locale);
					// end html blob
					// -----------------------------------------------------------------------------------------------------------------------------
					// start of right side list component migration
					try {
						String h2Text = "";
						List<String> list = new ArrayList<String>();
						Element listElements = doc.select("div.gd-right")
								.select("div.n13-dm,div.n13-pilot").first();
						log.debug("list elements:" + listElements);
						if (listElements != null) {
							Elements h2Elements = listElements
									.select("h2.bdr-1");
							if (h2Elements != null && !h2Elements.isEmpty()) {
								Element h2Element = h2Elements.first();
								if (h2Element != null) {
									h2Text = h2Element.text();
								} else {
									sb.append("<li>No heading element for last list section in left part</li>");
								}
								Elements aElements = listElements.select("li");
								for (Element ele : aElements) {
									JSONObject obj = new JSONObject();
									Elements aEle = ele.getElementsByTag("a");
									String aText = aEle.text();
									String aHref = aEle.attr("href");
									obj.put("linktext", aText);
									obj.put("linkurl", aHref);
									obj.put("icon", "");
									obj.put("size", "");
									obj.put("description", "");
									obj.put("openInNewWindow", false);
									list.add(obj.toString());
								}
							} else {
								sb.append("<li>No heading element for last list section in left part</li>");
							}
						} else {
							sb.append("<li>Last list section in left part doesn't found</li>");
						}
						// End get content.
						// Start set content.
						if (architectureRightNode.hasNode("list")) {
							Node listNode = architectureRightNode
									.getNode("list");
							if (StringUtils.isNotBlank(h2Text)) {
								listNode.setProperty("title", h2Text);
							}
							if (listNode.hasNode("element_list_0")) {
								Node element_list_0 = listNode
										.getNode("element_list_0");
								int size = list.size();
								if (size > 1) {
									element_list_0
											.setProperty("listitems", list
													.toArray(new String[list
															.size()]));
								} else {
									element_list_0.setProperty("listitems",
											list.get(0));
								}
							} else {
								sb.append("<li>Element_list node of list node not found</li>");
							}
						} else {
							sb.append("<li>List node not found</li>");
						}
						// End set content.
					} catch (Exception e) {
						log.error("Exception : ", e);
						sb.append("<li>Unable to migrate last list</li>");
					}
					// end of right side list component migration
				}
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

	public void handleHtmlBolb(Node architectureLeftNode, String locale) {
		// start of htmlblob content
		try {
			Elements htmlElements = doc.select("div.gd22v2-right");
			if (htmlElements != null) {
				Node htmlNode = architectureLeftNode.getNode("gd22v2")
						.getNode("gd22v2-right").getNode("htmlblob");
				if (htmlNode != null) {
					for (Element ele : htmlElements) {
						if (ele != null) {
							String textProp = ele.html();
							textProp = FrameworkUtils.extractHtmlBlobContent(
									ele, "", locale, sb);
							log.debug("html property!: " + textProp);
							htmlNode.setProperty("html", textProp);
						} else {
							sb.append("<li>Unable to update htmlblob as there are no elements</li>");
						}

					}
				}
			} else {
				sb.append("<li>Unable to update htmlblob as its respective div is missing</li>");
			}

		} catch (Exception e) {
			sb.append("<li>Unable to update htmlblob.</li>");
		}

		// end of htmlblob content

	}

	public void handleTileSection(Node architectureRightNode) {
		// start of tile bordered section
		try {
			boolean flag = false;
			String h2Text = "";
			String pText = "";
			String aText = "";
			String aHref = "";
			int count = 0;
			Elements tileBorderedElements = doc.select("div.gd-right").select(
					"div.c23-pilot");
			if (tileBorderedElements != null) {
				int eleSize = tileBorderedElements.size();
				if (architectureRightNode.hasNodes()) {
					NodeIterator tileBorderedNodeIterator = architectureRightNode
							.getNodes("tile_bordered*");
					int nodeSize = (int) tileBorderedNodeIterator.getSize();
					log.debug("Ele size:" + eleSize);
					log.debug("Node size:" + nodeSize);
					if (eleSize != nodeSize) {
						sb.append("<li>Could not migrate  tilebordered node. Count mis match</li>");
						log.debug("Could not migrate  tilebordered node. Count mis match");
					}
					for (Element ele : tileBorderedElements) {
						if (tileBorderedNodeIterator.hasNext()) {
							Node tileNode = (Node) tileBorderedNodeIterator
									.next();
							Elements h2TagText = ele.getElementsByTag("h2");
							if (h2TagText != null) {
								h2Text = h2TagText.html();
							} else {
								sb.append("<li>TileBordered Component Heading element not having any title in it ('h2' is blank)</li>");
							}

							Elements descriptionText = ele
									.getElementsByTag("p");
							if (descriptionText != null) {
								pText = descriptionText.html();
							} else {
								sb.append("<li>TileBordered Component description element not having any title in it ('p' is blank)</li>");
							}

							Elements anchorText = ele.getElementsByTag("a");
							if (anchorText != null) {
								aText = anchorText.text();
								aHref = anchorText.attr("href");
							} else {
								sb.append("<li>TileBordered Component anchor tag not having any content in it ('<a>' is blank)</li>");
							}
							String textAppended = ele.ownText();
							if (StringUtils.isNotBlank(textAppended)) {
								flag = true;
								count++;
							}
							if (flag) {
								sb.append("<li>Extra Text found after link on locale page for "
										+ count
										+ " TileBordered Component(s) , hence the text cannot be migrated.</li>");
							}
							tileNode.setProperty("title", h2Text);
							tileNode.setProperty("description", pText);
							tileNode.setProperty("linktext", aText);
							tileNode.setProperty("linkurl", aHref);
						}
					}

				} else {
					sb.append("<li>Tile bordered nodes are not found.</li>");
				}
			} else {
				sb.append("<li>Tile bordered elements are not found.</li>");
			}

		} catch (Exception e) {
			sb.append("<li>Unable to update tile bordered component.</li>");
			log.debug("Exception :" + e);
		}
		// end of tile bordered section
	}
}
