/* 
 * S.No		Name			Description of change
 * 1		Vidya			Added the Java file to handle the migration of service listing variation 1 page(s).
 * 
 * */
package com.cisco.dse.global.migration.servicelisting;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.cisco.dse.global.migration.config.BaseAction;
import com.cisco.dse.global.migration.config.Constants;
import com.cisco.dse.global.migration.config.FrameworkUtils;

public class ServiceListingVariation01 extends BaseAction {

	Document doc;
	StringBuilder sb = new StringBuilder(1024);
	static Logger log = Logger.getLogger(ServiceListingVariation01.class);

	public String translate(String host, String loc, String prod, String type,
			String catType, String locale, Session session, Map<String, String> urlMap) throws IOException,
			ValueFormatException, VersionException, LockException,
			ConstraintViolationException, RepositoryException {
		log.debug("In the translate method, catType is :" + catType);

		// Repo node paths
		String pagePropertiesPath = "/content/<locale>/"+catType+"/<prod>/service-listing/jcr:content";
		String midNodeTopPath = "/content/<locale>/"
				+ catType
				+ "/<prod>/service-listing/jcr:content/content_parsys/services/layout-services/gd21v1/gd21v1-mid";
		String midNodeBottomPath = "/content/<locale>/"
				+ catType
				+ "/<prod>/service-listing/jcr:content/content_parsys/services/layout-services";
		String pageUrl = host + "/content/<locale>/" + catType
				+ "/<prod>/service-listing.html";
		pageUrl = pageUrl.replace("<locale>", locale).replace("<prod>", prod);
		pagePropertiesPath = pagePropertiesPath.replace("<locale>", locale).replace("<prod>", prod);
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
		javax.jcr.Node pageJcrNode = null;
		try {
			midTopNode = session.getNode(midNodeTopPath);
			midBottomNode = session.getNode(midNodeBottomPath);
			pageJcrNode = session.getNode(pagePropertiesPath);
			if (midTopNode.hasNode("gd23v1")) {
				midBottomNode = midTopNode.getNode("gd23v1");
			} else {
				midBottomNode = midBottomNode.hasNode("gd23v1") ? midBottomNode
						.getNode("gd23v1") : null;
			}
			doc = getConnection(loc);

			if (doc != null) {

				// ------------------------------------------------------------------------------------------------------------------------------------------
				// start set page properties.
				
				FrameworkUtils.setPageProperties(pageJcrNode, doc, session, sb);
				
				// end set page properties.
				// ------------------------------------------------------------------------------------------------------------------------------------------
				
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
						String outerHtmlText = "";
						log.debug("Started migrating HtmlBlob content.");
						// Start get content.
						StringBuilder htmlBlobContent = new StringBuilder();
						Elements htmlBlobElements = doc
								.select("div.c11v5-pilot");
						if (htmlBlobElements != null
								&& !htmlBlobElements.isEmpty()) {
							for (Element ele : htmlBlobElements) {
								outerHtmlText = FrameworkUtils.extractHtmlBlobContent(ele, "", locale, sb, urlMap, catType, type);
								htmlBlobContent.append(outerHtmlText);
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
										String aHref = aElements.first().absUrl("href");
										if(StringUtil.isBlank(aHref)){
											aHref = aElements.first().attr("href");
										}
										// Start extracting valid href
										log.debug("Before gd-left" + aHref + "\n");
										aHref = FrameworkUtils.getLocaleReference(aHref, urlMap, locale, sb, catType, type);
										log.debug("after gd-left" + aHref + "\n");
										// End extracting valid href
										String pdf = element.ownText();
										if(StringUtils.isEmpty(pdf)){ 
											Element pdfElement = element.select("nobr").first();
											if(pdfElement != null){
											pdf = pdfElement.ownText();
											}
										}
										String pdfIcon ="none";
										if(pdf.length()>0){											
											if(pdf.toLowerCase().contains("pdf"))
												pdfIcon = "pdf";
											int i=0;
											for(;i<pdf.length();i++){
												char character = pdf.charAt(i);												
												boolean isDigit = Character.isDigit(character);
												if(isDigit){
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
									sb.append(Constants.LEFT_GRID_HEADING_NOT_FOUND);
								}
							} else {
								sb.append(Constants.LEFT_GRID_HEADING_NOT_FOUND);
							}
						} else {
							sb.append(Constants.LEFT_GRID_ELEMENT_NOT_FOUND);
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
								}
								if (listNode.hasNode("intro")) {
									Node introNode = listNode.getNode("intro");
									if (introNode.hasProperty("paragraph_rte") && StringUtils.isNotBlank(introNode.getProperty("paragraph_rte").getValue().getString()))
										sb.append("paragraph text in list not found in web publisher page but found in WEM page.");
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
									sb.append(Constants.LEFT_GRID_ELEMENT_LIST_NODE_NOT_FOUND);
								}
							} else {
								sb.append(Constants.LEFT_GRID_LIST_NODE_NOT_FOUND);
							}
						} else {
							sb.append(Constants.LEFT_GRID_NODE_NOT_FOUND);
						}
						// End set content.
					} catch (Exception e) {
						log.error("Exception : ", e);
						sb.append(Constants.UNABLE_TO_MIGRATE_LEFT_GRID);
					}
					// end gd-left component.
					// ----------------------------------------------------------------------------------------------------------------------------------------
					// start gd-mid component
					try {
						String h2Text = "";
						String pText = "";
						String text = "";
						boolean anchor = true;
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
									sb.append(Constants.MID_GRID_HEADING_NOT_FOUND);
								}
								Elements pElements = gd_mid_Elements
										.select("p");
								Element aElements = gd_mid_Elements
										.select("a").first();
								String linkTextTobeAdded = "";
								int count = 0;
								for (Element pElement : pElements) {
									if (count == 0) {
										pText = FrameworkUtils.extractHtmlBlobContent(pElement, "", locale, sb, urlMap, catType, type);
										count = count + 1;
									}
									else {
										if("ko_kr".equals(locale) && ("servers-unified-computing".equals(prod))){
											text  = pElement.ownText();
										}
										else{
										//pText = pText + "<br>";
										linkTextTobeAdded = pElement.ownText();
										break;
										}
									}
									
								}								
								if (aElements != null) {
									JSONObject obj = new JSONObject();
									String aText = aElements.text();
									if (StringUtils.isNotBlank(linkTextTobeAdded)) {
										aText =  linkTextTobeAdded + " " + aText ;
										if (aText.indexOf(".") != -1) {
											aText = aText.replace(".", "");
											aText = aText + ".";											
										}
									}
									String aHref = aElements.absUrl("href");
									if(StringUtil.isBlank(aHref)){
										aHref = aElements.attr("href");
									}
									// Start extracting valid href
									log.debug("Before gd-mid" + aHref + "\n");
									aHref = FrameworkUtils.getLocaleReference(aHref, urlMap, locale, sb, catType, type);
									log.debug("after gd-mid" + aHref + "\n");
									// End extracting valid href
									obj.put("linktext", aText);
									obj.put("linkurl", aHref);
									obj.put("icon", "none");
									obj.put("size", "");
									obj.put("description", text);
									obj.put("openInNewWindow", false);
									list.add(obj.toString());
								} else {
									anchor = false;
									sb.append(Constants.MID_GRID_ANCHOR_ELEMENTS_NOT_FOUND);
								}
							} else {
								sb.append(Constants.MID_GRID_HEADING_NOT_FOUND);
							}
						} else {
							sb.append(Constants.MID_GRID_ELEMENT_NOT_FOUND);
						}
						// End get content.
						// Start set content.
						if (midBottomNode.hasNode("gd23v1-mid")) {
							Node gd23v1_mid_Node = midBottomNode
									.getNode("gd23v1-mid");
							if(anchor){
							if (gd23v1_mid_Node.hasNode("list")) {
								Node listNode = gd23v1_mid_Node.getNode("list");
								if (StringUtils.isNotBlank(h2Text)) {
									listNode.setProperty("title", h2Text);
								} else {
									sb.append(Constants.MID_GRID_HEADING_NOT_FOUND);
								}
								if (listNode.hasNode("intro")) {
									Node introNode = listNode.getNode("intro");
									introNode.setProperty("paragraph_rte",
											pText);
								} else {
									sb.append(Constants.MID_GRID_INTRO_NODE_NOT_FOUND);
								}
								if (listNode.hasNode("element_list_0")) {
									Node element_list_0 = listNode
											.getNode("element_list_0");
									int size = list.size();
									if(size>1) {
										element_list_0.setProperty("listitems",
												list.toArray(new String[list
														.size()]));
									} else {
										element_list_0.setProperty("listitems",list.get(0));
									}
								} else {
									sb.append(Constants.MID_GRID_ELEMENT_LIST_NODE_NOT_FOUND);
								}
							} else {
								sb.append(Constants.MID_GRID_LIST_NODE_NOT_FOUND);
							}
							}
						} else {
							sb.append(Constants.MID_GRID_NODE_NOT_FOUND);
						}
						// End set content.
					} catch (Exception e) {
						log.error("Exception : ", e);
						sb.append(Constants.UNABLE_TO_MIGRATE_MID_GRID);
					}

					// end gd-mid component.
					// ----------------------------------------------------------------------------------------------------------------------------------------
					// start gd-right component.

					try {
						String h2Text = "";
						String pText = "";
						String text = "";
						boolean anchor = true;
						boolean flag = true;
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
									sb.append(Constants.RIGHT_GRID_HEADING_NOT_FOUND);
								}
								Elements pElements = gd_mid_Elements
										.select("p");
								Element aElements = gd_mid_Elements
										.select("a").first();
								pText = pElements.first().text();
								String linkTextTobeAdded = "";
								int count = 0;
								for (Element pElement : pElements) {
									if (count == 0) {
										pText = FrameworkUtils.extractHtmlBlobContent(pElement, "", locale, sb, urlMap, catType, type);
										count = count + 1;
									}
									else {
										//pText = pText + "<br>";
										//pText = pText + pElement.ownText();
										linkTextTobeAdded = pElement.ownText();
										break;
									}
								}	
								if (aElements != null) {
									JSONObject obj = new JSONObject();
									String aText = aElements.text();
									if (StringUtils.isNotBlank(linkTextTobeAdded)) {
										aText =  linkTextTobeAdded + " " + aText ;
										if (aText.indexOf(".") != -1) {
											aText = aText.replace(".", "");
											aText = aText + ".";											
										}
									}
									String aHref = aElements.absUrl("href");
									if(StringUtil.isBlank(aHref)){
										aHref = aElements.attr("href");
									}
									// Start extracting valid href
									log.debug("Before gd-right" + aHref + "\n");
									aHref = FrameworkUtils.getLocaleReference(aHref, urlMap, locale, sb, catType, type);
									log.debug("after gd-right" + aHref + "\n");
									// End extracting valid href
									obj.put("linktext", aText);
									obj.put("linkurl", aHref);
									obj.put("icon", "none");
									obj.put("size", "");
									obj.put("description", "");
									obj.put("openInNewWindow", false);
									list.add(obj.toString());
								} else {
									anchor = false;
									sb.append(Constants.RIGHT_GRID_ANCHOR_ELEMENTS_NOT_FOUND);
								}
							} else {
								sb.append(Constants.RIGHT_GRID_HEADING_NOT_FOUND);
							}
						} else {
							Elements gd_left_Elements = doc.select("div.gd23-pilot").select("div.gd-left");
							if(gd_left_Elements.size() == 2){
								Element gd_left_Element = gd_left_Elements.last();
								text = FrameworkUtils.extractHtmlBlobContent(gd_left_Element, "", locale, sb, urlMap, catType, type);
								flag = false;
							}else{
							sb.append(Constants.RIGHT_GRID_ELEMENT_NOT_FOUND);
						}
						}
						// End get content.
						// Start set content.
						if (midBottomNode.hasNode("gd23v1-right")) {
							Node gd23v1_mid_Node = midBottomNode
									.getNode("gd23v1-right");
							if(anchor){
							if (gd23v1_mid_Node.hasNode("list")) {
								Node listNode = gd23v1_mid_Node.getNode("list");
								if (StringUtils.isNotBlank(h2Text)) {
									listNode.setProperty("title", h2Text);
								}
								if (listNode.hasNode("intro")) {
									Node introNode = listNode.getNode("intro");
									introNode.setProperty("paragraph_rte",
											pText);
								} else {
									sb.append(Constants.RIGHT_GRID_INTRO_NODE_NOT_FOUND);
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
									sb.append(Constants.RIGHT_GRID_ELEMENT_LIST_NODE_NOT_FOUND);
								}
							} else {
								try {
									if (gd23v1_mid_Node.hasNode("htmlblob")) {
										Node htmlBlob = gd23v1_mid_Node
												.getNode("htmlblob");
										if(flag){
											for(Element gd_mid_Element: gd_mid_Elements){
											text = FrameworkUtils.extractHtmlBlobContent(gd_mid_Element, "", locale, sb, urlMap, catType, type);
											}
										}
										htmlBlob.setProperty(
												"html",text);
									} else {
										sb.append(Constants.HTMLBLOB_NODE_DOES_NOT_EXIST);
									}
								} catch (Exception e) {
									log.error("Exception : ", e);
									sb.append(Constants.UNABLE_TO_MIGRATE_RIGHT_GRID);
								}
							}
							}
						} else {
							sb.append(Constants.RIGHT_GRID_NODE_NOT_FOUND);
						}
						// End set content.
					} catch (Exception e) {
						log.error("Exception : ", e);
						sb.append(Constants.UNABLE_TO_MIGRATE_RIGHT_GRID);
					}

					// end gd-right component.
					// ------------------------------------------------------------------------------------------------------------------------------------------
				} catch (Exception e) {
					sb.append(Constants.URL_CONNECTION_EXCEPTION);
				}
			}
			else{
				sb.append(Constants.URL_CONNECTION_EXCEPTION);
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