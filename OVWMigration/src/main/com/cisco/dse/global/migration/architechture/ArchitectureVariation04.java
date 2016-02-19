/* 
 * S.No		Name	Date		Description of change
 * 1		Vidya	17-dec-15			Added the Java file to handle the migration of architecture variation 4 page.
 * 
 * */
package com.cisco.dse.global.migration.architechture;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
import org.apache.sling.commons.json.JSONObject;
import org.jsoup.helper.StringUtil;
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
			String catType, String locale, Session session, Map<String, String> urlMap) throws IOException,
			ValueFormatException, VersionException, LockException,
			ConstraintViolationException, RepositoryException {
		log.debug("In the translate method of ArchitectureVariation04");
		log.debug("In the translate method, catType is :" + catType);

		// Repo node paths
		String pagePropertiesPath = "/content/<locale>/"+catType+"/<prod>/architecture/jcr:content";
		String pageUrl = host + "/content/<locale>/" + catType
				+ "/<prod>/architecture.html";
		pagePropertiesPath = pagePropertiesPath.replace("<locale>", locale).replace("<prod>", prod);
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
		javax.jcr.Node pageJcrNode = null;
		try {

			architectureLeftNode = session.getNode(architectureLeftPath);
			architectureRightNode = session.getNode(architectureRightPath);
			pageJcrNode = session.getNode(pagePropertiesPath);
			try {
				doc = getConnection(loc);
			} catch (Exception e) {
				log.error("Exception " , e);
			}

			if (doc != null) {
				// ------------------------------------------------------------------------------------------------------------------------------------------
				// start set page properties.

				FrameworkUtils.setPageProperties(pageJcrNode, doc, session, sb);

				// end set page properties.
				// ------------------------------------------------------------------------------------------------------------------------------------------
				Elements allListElements = doc.select("div.gd-left").select(
						"div.n13-pilot,div.n13-dm");
				if (allListElements.size() == 1) {
					// start of text component and first list properties setting
					try {
						String textProp = "";
						String listIntroText = "";
						StringBuilder textContent = new StringBuilder();
						List<String> list = new ArrayList<String>();
						Elements divElements = doc.select("div.gd22v2-left")
								.select("div.c00-pilot,div.c100-dm,div.c100-pilot");
						if(divElements.size()==1){
							sb.append(Constants.TEXT_ELEMENT_NOT_FOUND);
						}
						if (divElements != null) {
							Element h2Element = divElements.select("h2")
									.first();
							Element pElement = divElements.select("p").first();
							if (h2Element != null || pElement != null) {
								textContent.append(h2Element);
								textContent.append(pElement);
								textProp = textContent.toString();
							} else {
								sb.append(Constants.TEXT_ELEMENT_NOT_FOUND);
							}
							Element listInroElement = divElements.select("p")
									.last();
							if (listInroElement != null) {
								listIntroText = listInroElement.html();
							} else {
								sb.append(Constants.LIST_PARAGRAPH_ELEMENT_NOT_FOUND);
							}
							Elements aElements = divElements.select("li");
							for (Element ele : aElements) {
								JSONObject obj = new JSONObject();
								obj.put("linktext", ele.html());
								obj.put("linkurl", "");
								obj.put("icon", "none");
								obj.put("size", "");
								obj.put("description", "");
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
										sb.append(Constants.LIST_INTRO_NODE_NOT_FOUND);
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
										sb.append(Constants.LIST_ELEMENT_LIST_NODE_NOT_FOUND);
									}
								} else {
									sb.append(Constants.LIST_NODE_NOT_FOUND);
								}

					} catch (Exception e) {
						sb.append(Constants.EXCEPTION_TEXT_COMPONENT);
						log.error("Exception :",e);
					}
					// end of text component and first list properties setting
					// -------------------------------------------------------------------------------------------------------------------
					// start of middle content
					try {
						// getting middle content
						Element listElements = doc.select("div.parbase")
								.select("div.c00-pilot,div.c100-dm,div.c100-pilot").last();
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
										h2Text = headerList.get(loop);
										pText = paraList.get(loop);
										if (h2Text != null) {
											listNode.setProperty("title",
													h2Text);
										} else {
											sb.append(Constants.TITLE_OF_LIST_ELEMENT_NOT_FOUND);
										}
										if (listNode.hasNode("intro")) {
											Node introNode = listNode
													.getNode("intro");
											if (pText != null) {
												introNode.setProperty(
														"paragraph_rte", pText);
											} else {
												sb.append(Constants.LIST_INTRO_PARAGRAPH_ELEMENT_NOT_FOUND);
											}
										} else {
											sb.append(Constants.LIST_INTRO_NODE_NOT_FOUND);
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
											sb.append(Constants.LIST_ELEMENT_LIST_NODE_NOT_FOUND);
										}
									} else {
										sb.append(Constants.LIST_NODE_NOT_FOUND);
									}
								}
							}
						} else {
							sb.append(Constants.LIST_ELEMENT_NOT_FOUND);
						}
					} catch (Exception e) {
						sb.append(Constants.UNABLE_TO_UPDATE_LIST);
						log.error("Exception :",e);
					}
					// end of middle content migration
					// -----------------------------------------------------------------------------------------------------------------------------
					// start html blob
					handleHtmlBolb(architectureLeftNode, locale, urlMap);
					// end html blob
					// -----------------------------------------------------------------------------------------------------------------------------
					// start of last list in left section
					handleResourcesSection(architectureLeftNode, urlMap, locale);
					// end of last list in left section
					// --------------------------------------------------------------------------------------------------------------------------------------
					// start of right side list component migration
					handleRightListSection(architectureRightNode, urlMap, locale);
					// end of right side list component migration
					//---------------------------------------------------------------------------------------------------------------------------------------
					// start tile section
					handleTileSection(architectureRightNode, urlMap, locale);
					// end of tile section
				} else {

					// start of text component
					try {
						String textProp = null;
						Elements textElements = doc
								.select("div.c00-pilot,div.c100-dm,div.c100-pilot");
						if (textElements != null) {
							NodeIterator textNodeIterator = architectureLeftNode
									.getNodes("text*");
							for (Element ele : textElements) {
								if (architectureLeftNode.hasNodes()) {
									if (textNodeIterator.hasNext()) {
										Node textNode = (Node) textNodeIterator
												.next();
										textProp = ele.html();
										textNode.setProperty("text", textProp);
									}
								} else {
									sb.append(Constants.TEXT_NODE_NOT_FOUND);
								}
							}
						} else {
							sb.append(Constants.TEXT_ELEMENT_NOT_FOUND);
						}
					} catch (Exception e) {
						sb.append(Constants.EXCEPTION_TEXT_COMPONENT);
						log.error("Exception :",e);
					}
					// end of text component
					// --------------------------------------------------------------------------------------------------------------------------------------
					// start of migrating list components
					try {
						int count = 1;
						String aText = "";
						Node listNode = null;
						NodeIterator insideListIterator = architectureLeftNode
								.getNode("gd22v2").getNode("gd22v2-left")
								.getNodes("list*");
						NodeIterator outsideListIterator = architectureLeftNode
								.getNodes("list*");
						for (Element ele : allListElements) {
							if(count == 6) break;
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
									if(StringUtils.isEmpty(h2Text)){
										h2Text = " ";
									}
									listNode.setProperty("title", h2Text);
								} else {
									sb.append(Constants.TITLE_OF_LIST_ELEMENT_NOT_FOUND);
								}
								Element pElements = ele.select("p").first();
								if (pElements != null) {
									pText = FrameworkUtils.extractHtmlBlobContent(pElements, "", locale, sb, urlMap);
									log.debug("pText:"+pText);
									if (listNode.hasNode("intro")) {
										Node introNode = listNode
												.getNode("intro");
										if (pText != null) {
											introNode.setProperty(
													"paragraph_rte", pText);
										}
									} else {
										sb.append(Constants.LIST_INTRO_PARAGRAPH_ELEMENT_NOT_FOUND);
									}
								} else {
									sb.append(Constants.LIST_INTRO_NODE_NOT_FOUND);
								}
								Elements liElements = ele.select("li");
								for (Element liEle : liElements) {
									String linkurl = "";
									JSONObject obj = new JSONObject();
									Element aElements = liEle.select("a").first();
									if (aElements != null) {
										aText = aElements.text();
										linkurl = aElements.absUrl("href");
										if(StringUtil.isBlank(linkurl)){
											linkurl = aElements.attr("href");	
										}
										linkurl = FrameworkUtils.getLocaleReference(linkurl, urlMap, locale, sb);
									} else {
										aText = liEle.html();
									}
									obj.put("linktext", aText);
									obj.put("linkurl", linkurl);
									obj.put("icon", "");
									obj.put("size","");
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
										element_list_0.setProperty("listitems",
												list.toArray(new String[list
												                        .size()]));
									} else {
										element_list_0.setProperty("listitems",
												list.toString());
									}
								} else {
									sb.append(Constants.LIST_ELEMENT_LIST_NODE_NOT_FOUND);
								}
							}
							count++;
						}
					} catch (Exception e) {
						log.error("Exception :",e);
						sb.append(Constants.UNABLE_TO_UPDATE_LIST);
					}
					// end of migrating list components
					// -------------------------------------------------------------------------------------------------------------------------
					// start of last list in left section
					handleResourcesSection(architectureLeftNode, urlMap, locale);
					// end of last list in left section
					//-------------------------------------------------------------------------------------------------------------------------
					// start tile section
					handleTileSection(architectureRightNode, urlMap, locale);

					// end tile section
					// --------------------------------------------------------------------------------------------------------------------
					// start html blob
					handleHtmlBolb(architectureLeftNode, locale, urlMap);
					// end html blob
					// -----------------------------------------------------------------------------------------------------------------------------
					// start of right side list component migration
					handleRightListSection(architectureRightNode, urlMap, locale);
					// end of right side list component migration
				}
				session.save();
			} else {
				sb.append(Constants.URL_CONNECTION_EXCEPTION);
			}
		} catch (Exception e) {
			sb.append(Constants.UNABLE_TO_MIGRATE_PAGE);
			log.debug("Exception as url cannot be connected: " + e);
			log.error("Exception :",e);
		}

		sb.append("</ul></td>");

		return sb.toString();

	}

	public void handleHtmlBolb(Node architectureLeftNode, String locale, Map<String, String> urlMap) {
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
									ele, "", locale, sb, urlMap);
							htmlNode.setProperty("html", textProp);
						} 
					}
				}else{
					sb.append(Constants.HTMLBLOB_NODE_NOT_FOUND);
				}
			} else {
				sb.append(Constants.HTMLBLOB_ELEMENT_NOT_FOUND);
			}

		} catch (Exception e) {
			sb.append(Constants.EXCEPTION_IN_HTMLBLOB);
			log.error("Exception :",e);
		}

		// end of htmlblob content

	}

	public void handleTileSection(Node architectureRightNode, Map<String, String> urlMap, String locale) {
		// start of tile bordered section
		try {
			String h2Text = "";
			String pText = "";
			String aText = "";
			String aHref = "";
			Elements tileBorderedElements = doc.select("div.gd-right").select(
					"div.c23-pilot");
			if (tileBorderedElements != null) {
				int eleSize = tileBorderedElements.size();
				if (architectureRightNode.hasNodes()) {
					NodeIterator tileBorderedNodeIterator = architectureRightNode
							.getNodes("tile_bordered*");
					int nodeSize = (int) tileBorderedNodeIterator.getSize();
					if (eleSize != nodeSize) {
							String message = Constants.TILE_BORDERED_ELEMENT_COUNT_MISMATCH;
							message = message.replace("(<ele>)",
									Integer.toString(eleSize));
							message = message.replace("(<node>)",
									Integer.toString(nodeSize));
							sb.append(message);
						}
					for (Element ele : tileBorderedElements) {
						if (tileBorderedNodeIterator.hasNext()) {
							Node tileNode = (Node) tileBorderedNodeIterator
									.next();
							Elements h2TagText = ele.getElementsByTag("h2");
							if (h2TagText != null) {
								h2Text = h2TagText.html();
							} else {
								sb.append(Constants.TILE_BORDERED_TITLE_ELEMENT_NOT_FOUND);
							}
							//start of tile pop up 
							Node tilePopUpNode = null;
							Element lightBoxElement = null;
							Elements lightBoxElements = ele.select("a.c26v4-lightbox");
							log.debug("Light box ele:"+lightBoxElements);
							if (lightBoxElements != null && !lightBoxElements.isEmpty()) {
								  lightBoxElement = lightBoxElements.first();
							}
							tilePopUpNode = FrameworkUtils.getHeroPopUpNode(tileNode);
							if (tilePopUpNode == null && lightBoxElement != null) {
								sb.append("<li>video pop up is present in WEB page but it is not present in WEM page.</li>");
							}
							if (tilePopUpNode != null && lightBoxElement == null) {
								sb.append("<li>video pop up is present in WEM page but it is not present in WEB page.</li>");
							}
							if (tilePopUpNode != null && lightBoxElement != null && StringUtils.isNotBlank(h2Text))  {
								tilePopUpNode.setProperty("popupHeader", h2Text);
							} 
							//end of tile pop up
							
							Elements descriptionText = ele
									.getElementsByTag("p");
							if (descriptionText != null) {
								pText = descriptionText.html();
							} else {
								sb.append(Constants.TILE_BORDERED_DESCRIPTION_NOT_FOUND);
							}

							Element anchorText = ele.getElementsByTag("a").first();
							if (anchorText != null) {
								aText = anchorText.text();
								aHref = anchorText.absUrl("href");
								if(StringUtil.isBlank(aHref)){
									aHref = anchorText.attr("href");	
								}
								aHref = FrameworkUtils.getLocaleReference(aHref, urlMap, locale, sb);
							} else {
								sb.append(Constants.TILE_BORDERED_ANCHOR_ELEMENTS_NOT_FOUND);
							}

							tileNode.setProperty("title", h2Text);
							tileNode.setProperty("description", pText);
							tileNode.setProperty("linktext", aText);
							tileNode.setProperty("linkurl", aHref);
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
			log.error("Exception :" , e);
		}
		// end of tile bordered section
	}
	public void handleRightListSection(Node architectureRightNode, Map<String, String> urlMap, String locale) {
		try {
			String h2Text = "";
			boolean anchor = true;
			List<String> list = new ArrayList<String>();
			Element listElements = doc.select("div.gd-right")
					.select("div.n13-dm,div.n13-pilot").first();
			if (listElements != null) {
				Elements h2Elements = listElements
						.select("h2.bdr-1");
				if (h2Elements != null && !h2Elements.isEmpty()) {
					Element h2Element = h2Elements.first();
					if (h2Element != null) {
						h2Text = h2Element.text();
					} else {
						sb.append(Constants.TITLE_OF_LIST_ELEMENT_NOT_FOUND);
					}
					Elements aElements = listElements.select("li");
					for (Element ele : aElements) {
						JSONObject obj = new JSONObject();
						Element aEle = ele.getElementsByTag("a").first();
						String aText = aEle.text();
						String aHref = aEle.absUrl("href");
						if(StringUtil.isBlank(aHref)){
							aHref = aEle.attr("href");	
						}
						aHref = FrameworkUtils.getLocaleReference(aHref, urlMap, locale, sb);
						obj.put("linktext", aText);
						obj.put("linkurl", aHref);
						obj.put("icon", "");
						obj.put("size", "");
						obj.put("description", "");
						obj.put("openInNewWindow", false);
						list.add(obj.toString());
					}
				} else {
					sb.append(Constants.TITLE_OF_LIST_ELEMENT_NOT_FOUND);
				}
			} else {
				anchor = false;
				sb.append(Constants.LIST_ELEMENT_NOT_FOUND);
			}
			// End get content.
			// Start set content.
			if (architectureRightNode.hasNode("list")) {
				Node listNode = architectureRightNode
						.getNode("list");
				if (StringUtils.isNotBlank(h2Text)) {
					listNode.setProperty("title", h2Text);
				}
				if(anchor){
					if (listNode.hasNode("element_list_0")) {
						Node element_list_0 = listNode
								.getNode("element_list_0");
						int size = list.size();
						if (size > 1) {
							element_list_0
							.setProperty("listitems", list
									.toArray(new String[list
									                    .size()]));
						} else if(size >0){
							element_list_0.setProperty("listitems",
									list.get(0));
						}
					} else {
						sb.append(Constants.LIST_ELEMENT_LIST_NODE_NOT_FOUND);
					}
				}
			} else {
				sb.append(Constants.LIST_NODE_NOT_FOUND);
			}
			// End set content.
		} catch (Exception e) {
			log.error("Exception : ", e);
			sb.append(Constants.UNABLE_TO_UPDATE_LIST);
		}

	}
	public void handleResourcesSection(Node architectureLeftNode, Map<String, String> urlMap, String locale){
		try {
			String h2Text = "";
			String text = "";
			// boolean anchor = true;
			List<String> list = new ArrayList<String>();
			Element listElements = doc.select("div.gd-left")
					.select("div.n13-dm,div.n13-pilot").last();
			if (listElements != null) {
				Elements h2Elements = listElements
						.select("h2.bdr-1");
				if (h2Elements != null && !h2Elements.isEmpty()) {
					Element h2Element = h2Elements.first();
					if (h2Element != null) {
						h2Text = h2Element.text();
					} else {
						sb.append(Constants.TITLE_OF_LIST_ELEMENT_NOT_FOUND);
					}
					Elements aElements = listElements.select("li");
					for (Element ele : aElements) {
						JSONObject obj = new JSONObject();
						Element aEle = ele.getElementsByTag("a").first();
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
						String aHref = aEle.absUrl("href");
						if(StringUtil.isBlank(aHref)){
							aHref = aEle.attr("href");
						}
						aHref = FrameworkUtils.getLocaleReference(aHref, urlMap, locale, sb);
						obj.put("linktext", aText);
						obj.put("linkurl", aHref);
						obj.put("icon", pdfIcon);
						obj.put("size", pdf);
						obj.put("description", text);
						obj.put("openInNewWindow", false);
						list.add(obj.toString());
					}
				} else {
					sb.append(Constants.TITLE_OF_LIST_ELEMENT_NOT_FOUND);
				}
			} else {
				sb.append(Constants.LIST_ELEMENT_LIST_NODE_NOT_FOUND);
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
					sb.append(Constants.LIST_ELEMENT_LIST_NODE_NOT_FOUND);
				}
			} else {
				sb.append(Constants.LIST_NODE_NOT_FOUND);
			}
			// End set content.
		} catch (Exception e) {
			log.error("Exception : ", e);
			sb.append(Constants.UNABLE_TO_UPDATE_LIST);
		}
	}
}
