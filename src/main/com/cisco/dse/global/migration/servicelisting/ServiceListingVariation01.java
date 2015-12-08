package com.cisco.dse.global.migration.servicelisting;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
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
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.cisco.dse.global.migration.config.BaseAction;
import com.cisco.dse.global.migration.config.Constants;

public class ServiceListingVariation01 extends BaseAction {

	Document doc;
	StringBuilder sb = new StringBuilder(1024);
	static Logger log = Logger.getLogger(ServiceListingVariation01.class);

	public String translate(String host, String loc, String prod, String type,
			String catType, String locale, Session session) throws IOException,
			ValueFormatException, VersionException, LockException,
			ConstraintViolationException, RepositoryException {
		BasicConfigurator.configure();
		log.debug("In the translate method, catType is :" + catType);

		// Repo node paths
		String midNodeTopPath = "/content/<locale>/"
				+ catType
				+ "/<prod>/service-listing/jcr:content/content_parsys/services/layout-services/gd21v1/gd21v1-mid";
		String midNodeBottomPath = "/content/<locale>/"
				+ catType
				+ "/<prod>/service-listing/jcr:content/content_parsys/services/layout-services";
		String pageUrl = host + "/content/<locale>/" + catType
				+ "/<prod>/service-listing.html";
		pageUrl = pageUrl.replace("<locale>", locale).replace("<prod>", prod);

		sb.append("<td>" + "<a href=" + pageUrl + ">" + pageUrl + "</a>"
				+ "</td>");
		sb.append("<td>" + "<a href=" + loc + ">" + loc + "</a>" + "</td>");
		sb.append("<td><ul>");

		midNodeTopPath = midNodeTopPath.replace("<locale>", locale).replace(
				"<prod>", prod);
		midNodeBottomPath = midNodeBottomPath.replace("<locale>", locale)
				.replace("<prod>", prod);
		Node midTopNode = null;
		Node midBottomNode = null;

		try {
			midTopNode = session.getNode(midNodeTopPath);
			midBottomNode = session.getNode(midNodeBottomPath);
			if (midTopNode.hasNode("gd23v1")) {
				midBottomNode = midTopNode.getNode("gd23v1");
			} else {
				midBottomNode = midBottomNode.hasNode("gd23v1") ? midBottomNode
						.getNode("gd23v1") : null;
			}
			doc = getConnection(loc);

			if (doc != null) {

				// -----------------------------------------------------------------------------------------------------------------------------------------
				// start Title text component.
				try {
					log.debug("Started migrating title content.");
					// Start Get Content.
					Elements titleElements = doc
							.select("div.c00v0-alt1-pilot,div.c00v1-pilot");
					String h1Text = "";
					if (titleElements != null && !titleElements.isEmpty()) {
						Element titleElement = titleElements.first();
						if (titleElement != null) {
							Elements h1Elements = titleElement
									.getElementsByTag("h1");
							if (h1Elements != null && !h1Elements.isEmpty()) {
								Element h1Element = h1Elements.first();
								if (h1Element != null) {
									h1Text = h1Element.outerHtml();
								} else {
									sb.append(Constants.TEXT_DOES_NOT_EXIST);
								}
							} else {
								sb.append(Constants.TEXT_DOES_NOT_EXIST);
							}
						} else {
							sb.append(Constants.TEXT_ELEMENT_NOT_FOUND);
						}

					} else {
						sb.append(Constants.TEXT_ELEMENT_NOT_FOUND);
					}
					// End Get Content.
					// Start Set Content.
					if (midTopNode != null) {
						if (midTopNode.hasNode("text")) {
							Node textNode = midTopNode.getNode("text");
							if (StringUtils.isNotBlank(h1Text)) {
								textNode.setProperty("text", h1Text);
								log.debug("Title migration is done.");
							} else {
								sb.append(Constants.TEXT_DOES_NOT_EXIST);
							}
						} else {
							sb.append(Constants.TEXT_NODE_NOT_FOUND);
						}
						// End Set Content.
					}

					// end Title text component.
					// ---------------------------------------------------------------------------------------------------------------------------------------
					// start htmlblob component.
					try {
						log.debug("Started migrating HtmlBlob content.");
						// Start get content.
						StringBuilder htmlBlobContent = new StringBuilder();
						Elements htmlBlobElements = doc
								.select("div.c11v5-pilot");
						if (htmlBlobElements != null
								&& !htmlBlobElements.isEmpty()) {
							for (Element ele : htmlBlobElements) {
								htmlBlobContent.append(ele.outerHtml());
							}
						} else {
							sb.append(Constants.HTMLBLOB_ELEMENT_NOT_FOUND);
						}
						// End get content.
						// Start set content.
						if (midTopNode.hasNode("htmlblob")) {
							Node htmlBlobNode = midTopNode.getNode("htmlblob");
							if (StringUtils.isNotBlank(htmlBlobContent
									.toString())) {
								htmlBlobNode.setProperty("html",
										htmlBlobContent.toString());
								log.debug("HtmlBlob Content migrated is done.");
							} else {
								sb.append(Constants.HTMLBLOB_CONTENT_DOES_NOT_EXIST);
							}
						} else {
							sb.append(Constants.HTMLBLOB_NODE_NOT_FOUND);
						}
						// End get content.
					} catch (Exception e) {
						log.error("Exception : ", e);
						sb.append(Constants.EXCEPTION_IN_HTMLBLOB);
					}
					// end htmlblob component.
					// ------------------------------------------------------------------------------------------------------------------------------------------
					// start gd-left component.
					try {
						// Start get content.
						String h2Text = "";
						List<String> list = new ArrayList<String>();
						Elements gd_left_Elements = doc.select("div.gd-left");
						if (gd_left_Elements != null
								&& !gd_left_Elements.isEmpty()) {
							Elements h2Elements = gd_left_Elements
									.select("h2.bdr-1");
							if (h2Elements != null && !h2Elements.isEmpty()) {
								Element h2Element = h2Elements.first();
								if (h2Element != null) {
									h2Text = h2Element.text();
									Element gd_left_Element = h2Element
											.parent();
									Elements liElements = gd_left_Element
											.getElementsByTag("li");
									for (Element element : liElements) {
										JSONObject obj = new JSONObject();
										Elements aElements = element.getElementsByTag("a");
										String aText = aElements.text();
										String aHref = aElements.attr("href");
										String pdf = element.ownText();									
										String pdfIcon =null;
										if(pdf.length()>0){											
											if(pdf.toLowerCase().contains("pdf"))
												pdfIcon = "pdf";
											int i=0;
											for(;i<pdf.length();i++){
												char c = pdf.charAt(i);												
												boolean b = Character.isDigit(c);
												if(b){
													break;
												} 
										}										
										pdf = pdf.substring(i, pdf.length()-1);
										}
										pdf = pdf.replace(")", "");
										pdf = pdf.trim();										
										obj.put("linktext", aText);
										obj.put("linkurl", aHref);
										obj.put("icon", pdfIcon);
										obj.put("size", pdf);
										obj.put("description", "");
										obj.put("openInNewWindow", false);
										list.add(obj.toString());
									}
								} else {
									sb.append("<li>LEFT_GRID_HEADING_NOT_FOUND</li>");
								}
							} else {
								sb.append("<li>LEFT_GRID_HEADING_NOT_FOUND</li>");
							}
						} else {
							sb.append("<li>LEFT_GRID_ELEMENT_NOT_FOUND</li>");
						}
						// End get content.
						// Start set content.
						if (midBottomNode.hasNode("gd23v1-left")) {
							Node gd23v1_left_Node = midBottomNode
									.getNode("gd23v1-left");
							if (gd23v1_left_Node.hasNode("list")) {
								Node listNode = gd23v1_left_Node
										.getNode("list");
								if (StringUtils.isNotBlank(h2Text)) {
									listNode.setProperty("title", h2Text);
								} else {
									sb.append("<li>LEFT_GRID_HEADING_NOT_FOUND</li>");
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
									sb.append("<li>LEFT_GRID_ELEMENT_LIST_NODE_NOT_FOUND</li>");
								}
							} else {
								sb.append("<li>LEFT_GRID_LIST_NODE_NOT_FOUND</li>");
							}
						} else {
							sb.append("<li>LEFT_GRID_NODE_NOT_FOUND</li>");
						}
						// End set content.
					} catch (Exception e) {
						log.error("Exception : ", e);
						sb.append("<li>UNABLE_TO_MIGRATE_LEFT_GRID</li>");
					}
					// end gd-left component.
					// ----------------------------------------------------------------------------------------------------------------------------------------
					// start gd-mid component
					try {
						String h2Text = "";
						String pText = "";
						List<String> list = new ArrayList<String>();
						Elements gd_mid_Elements = doc.select("div.gd23-pilot")
								.select("div.gd-mid");
						if (gd_mid_Elements != null
								&& !gd_mid_Elements.isEmpty()) {
							Elements h2Elements = gd_mid_Elements
									.select("h2.bdr-1");
							if (h2Elements != null && !h2Elements.isEmpty()) {
								Element h2Element = h2Elements.first();
								if (h2Element != null) {
									h2Text = h2Element.text();
								} else {
									sb.append("<li>RIGHT_GRID_HEADING_NOT_FOUND</li>");
								}
								Elements pElements = gd_mid_Elements
										.select("p");
								Elements aElements = gd_mid_Elements
										.select("a");
								log.debug("P elements of mid grid are:"
										+ pElements);
								log.debug("a elements of mid grid are:"
										+ aElements);
								int count = 0;
								for (Element pElement : pElements) {
									if (count == 0) {
										pText = pElement.outerHtml();
										count = count + 1;
									}
									else {
										pText = pText + "<br>";
										pText = pText + pElement.ownText();
										break;
									}
									
								}								
								if (aElements != null && !aElements.isEmpty()) {
									JSONObject obj = new JSONObject();
									String aText = aElements.text();
									String aHref = aElements.attr("href");
									obj.put("linktext", aText);
									obj.put("linkurl", aHref);
									obj.put("icon", "none");
									obj.put("size", "");
									obj.put("description", "");
									obj.put("openInNewWindow", false);
									list.add(obj.toString());
									log.debug(list);
								} else {
									sb.append("<li>MID_GRID_ANCHOR_ELEMENTS_NOT_FOUND</li>");
								}
							} else {
								sb.append("<li>MID_GRID_HEADING_NOT_FOUND</li>");
							}
						} else {
							sb.append("<li>MID_GRID_ELEMENT_NOT_FOUND</li>");
						}
						// End get content.
						// Start set content.
						if (midBottomNode.hasNode("gd23v1-mid")) {
							Node gd23v1_mid_Node = midBottomNode
									.getNode("gd23v1-mid");
							if (gd23v1_mid_Node.hasNode("list")) {
								Node listNode = gd23v1_mid_Node.getNode("list");
								if (StringUtils.isNotBlank(h2Text)) {
									listNode.setProperty("title", h2Text);
								} else {
									sb.append("<li>MID_GRID_HEADING_NOT_FOUND</li>");
								}
								if (listNode.hasNode("intro")) {
									Node introNode = listNode.getNode("intro");
									introNode.setProperty("paragraph_rte",
											pText);
								} else {
									sb.append("<li>MID_GRID_INTRO_NODE_NOT_FOUND</li>");
								}
								if (listNode.hasNode("element_list_0")) {
									Node element_list_0 = listNode
											.getNode("element_list_0");
									int size = list.size();
									log.debug("Size of list is"+size);
									if(size>1) {
										element_list_0.setProperty("listitems",
												list.toArray(new String[list
														.size()]));
									} else {
										element_list_0.setProperty("listitems",list.get(0));
									}
								} else {
									sb.append("<li>MID_GRID_ELEMENT_LIST_NODE_NOT_FOUND</li>");
								}
							} else {
								sb.append("<li>MID_GRID_LIST_NODE_NOT_FOUND</li>");
							}
						} else {
							sb.append("<li>MID_GRID_NODE_NOT_FOUND</li>");
						}
						// End set content.
					} catch (Exception e) {
						log.error("Exception : ", e);
						sb.append("<li>UNABLE_TO_MIGRATE_MID_GRID</li>");
					}

					// end gd-mid component.
					// ----------------------------------------------------------------------------------------------------------------------------------------
					// start gd-right component.

					try {
						String h2Text = "";
						String pText = "";
						List<String> list = new ArrayList<String>();
						Elements gd_mid_Elements = doc.select("div.gd23-pilot")
								.select("div.gd-right");
						if (gd_mid_Elements != null
								&& !gd_mid_Elements.isEmpty()) {
							Elements h2Elements = gd_mid_Elements
									.select("h2.bdr-1");
							if (h2Elements != null && !h2Elements.isEmpty()) {
								Element h2Element = h2Elements.first();
								if (h2Element != null) {
									h2Text = h2Element.text();
								} else {
									sb.append("<li>RIGHT_GRID_HEADING_NOT_FOUND</li>");
								}
								Elements pElements = gd_mid_Elements
										.select("p");
								Elements aElements = gd_mid_Elements
										.select("a");
								log.debug("P elements of right grid are:"
										+ pElements);
								log.debug("a elements of right grid are:"
										+ aElements);
								//pText = pElements.first().text();
								int count = 0;
								for (Element pElement : pElements) {
									if (count == 0) {
										pText = pElement.outerHtml();
										count = count + 1;
									}
									else {
										pText = pText + "<br>";
										pText = pText + pElement.ownText();
										break;
									}
								}	
								if (aElements != null && !aElements.isEmpty()) {
									JSONObject obj = new JSONObject();
									String aText = aElements.text();
									String aHref = aElements.attr("href");
									obj.put("linktext", aText);
									obj.put("linkurl", aHref);
									obj.put("icon", "none");
									obj.put("size", "");
									obj.put("description", "");
									obj.put("openInNewWindow", false);
									list.add(obj.toString());
									log.debug(list);
								} else {
									sb.append("<li>RIGHT_GRID_ANCHOR_ELEMENTS_NOT_FOUND</li>");
								}
							} else {
								sb.append("<li>RIGHT_GRID_HEADING_NOT_FOUND</li>");
							}
						} else {
							sb.append("<li>RIGHT_GRID_ELEMENT_NOT_FOUND</li>");
						}
						// End get content.
						// Start set content.
						if (midBottomNode.hasNode("gd23v1-right")) {
							Node gd23v1_mid_Node = midBottomNode
									.getNode("gd23v1-right");
							if (gd23v1_mid_Node.hasNode("list")) {
								Node listNode = gd23v1_mid_Node.getNode("list");
								if (StringUtils.isNotBlank(h2Text)) {
									listNode.setProperty("title", h2Text);
								} else {
									sb.append("<li>RIGHT_GRID_HEADING_NOT_FOUND</li>");
								}
								if (listNode.hasNode("intro")) {
									Node introNode = listNode.getNode("intro");
									introNode.setProperty("paragraph_rte",
											pText);
								} else {
									sb.append("<li>RIGHT_GRID_INTRO_NODE_NOT_FOUND</li>");
								}
								if (listNode.hasNode("element_list_0")) {
									Node element_list_0 = listNode
											.getNode("element_list_0");
									int size = list.size();
									 if(size>1){
										element_list_0.setProperty("listitems",
												list.toArray(new String[list
														.size()]));
									} else {
										element_list_0.setProperty(
												"listitems",
												list.get(0));
									}
								} else {
									sb.append("<li>RIGHT_GRID_ELEMENT_LIST_NODE_NOT_FOUND</li>");
								}
							} else {
								try {
									if (gd23v1_mid_Node.hasNode("htmlblob")) {
										Node htmlBlob = gd23v1_mid_Node
												.getNode("htmlblob");
										String text = gd_mid_Elements.html();
										log.debug("html blob content"+text);
										htmlBlob.setProperty(
												"html",text);
									} else {
										sb.append("<li>HTMLBLOB_NODE_DOES_NOT_EXIST</li>");
									}
								} catch (Exception e) {
									log.error("Exception : ", e);
									sb.append("<li>UNABLE_TO_MIGRATE_RIGHT_GRID</li>");
								}
							}
						} else {
							sb.append("<li>RIGHT_GRID_NODE_NOT_FOUND</li>");
						}
						// End set content.
					} catch (Exception e) {
						log.error("Exception : ", e);
						sb.append("<li>UNABLE_TO_MIGRATE_RIGHT_GRID</li>");
					}

					// end gd-right component.
					// ------------------------------------------------------------------------------------------------------------------------------------------
				} catch (Exception e) {
					sb.append(Constants.URL_CONNECTION_EXCEPTION);
				}
			}
			session.save();
		} catch (Exception e) {
			sb.append(Constants.URL_CONNECTION_EXCEPTION);
			log.debug("Exception as url cannot be connected: " + e);
		}

		sb.append("</ul></td>");

		return sb.toString();
	}
}