package com.cisco.dse.global.migration.rroot;

import java.io.IOException;
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
import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.cisco.dse.global.migration.config.BaseAction;
import com.cisco.dse.global.migration.config.Constants;
import com.cisco.dse.global.migration.config.FrameworkUtils;

public class RSolutionIndex extends BaseAction {

	Document doc;
	StringBuilder sb = new StringBuilder(1024);
	static Logger log = Logger.getLogger(RSolutionIndex.class);
	public String translate(String host, String loc, String prod, String type,
			String catType, String locale, Session session,
			Map<String, String> urlMap) throws IOException,
			ValueFormatException, VersionException, LockException,
			ConstraintViolationException, RepositoryException {
		BasicConfigurator.configure();

		String pagePropertiesPath = "/content/<locale>/" + catType + "/index/jcr:content";
		String pageUrl = host + "/content/<locale>/" + catType + "/index.html";
		pageUrl = pageUrl.replace("<locale>", locale);
		pagePropertiesPath = pagePropertiesPath.replace("<locale>", locale);

		String indexLeft = pagePropertiesPath + "/Grid/solutions/layout-solutions/widenarrow/WN-Wide-1";
		String mainGrid = pagePropertiesPath + "/Grid/solutions/layout-solutions";

		log.debug("In the translate to migate "+loc+" to "+pageUrl);
		
		log.debug("Path is " + indexLeft);
		log.debug("main grid path is : " + mainGrid);

		sb.append("<td>" + "<a href=" + pageUrl + ">" + pageUrl + "</a>" + "</td>");
		sb.append("<td>" + "<a href=" + loc + ">" + loc + "</a>" + "</td>");
		sb.append("<td><ul>");

		Node indexLeftNode = null;
		Node mainNode = null;
		Node pageJcrNode = null;
		try {
			indexLeftNode = session.getNode(indexLeft);
			mainNode = session.getNode(mainGrid);
			pageJcrNode = session.getNode(pagePropertiesPath);
			try {
				doc = getConnection(loc);
				log.debug("Connected to the provided URL");
			} catch (Exception e) {
				log.error("Exception : ", e);
			}

			if (doc != null) {
				// start set page properties.
				FrameworkUtils.setPageProperties(pageJcrNode, doc, session, sb);
				// end set page properties.
				// start Hero Migration
				try {
					log.debug("Start Hero Element Migration");
					Elements heroElements = doc.select("div.c50-pilot");

					String h2Text = null;
					String pText = null;
					String aText = null;
					String aLink = null;
					String imagePath = null;

					// Start of get content logic.
					if (heroElements != null && !heroElements.isEmpty()) {
						Element heroElement = heroElements.first();
						Elements textElements = heroElement.select("div.c50-text");
						if (textElements != null && !textElements.isEmpty()) {
							Element textElement = textElements.first();
							Elements h2Elements = textElement.getElementsByTag("h2");
							if (h2Elements != null && !h2Elements.isEmpty()) {
								Element h2Element = h2Elements.first();
								h2Text = h2Element.text();
							} else {
								sb.append(Constants.HERO_CONTENT_HEADING_ELEMENT_DOESNOT_EXISTS);
								log.debug("No h2 elements found with in the div class 'c50-text' with in div class 'c50-pilot'");
							}
							Elements pElements = textElement.getElementsByTag("p");
							if (pElements != null && !pElements.isEmpty()) {
								Element pElement = pElements.first();
								pText = pElement.text();
							} else {
								sb.append(Constants.HERO_CONTENT_DESCRIPTION_ELEMENT_DOESNOT_EXISTS);
								log.debug("No p elements found with in the div class 'c50-text' with in div class 'c50-pilot'");
							}
							Elements aElements = textElement.getElementsByTag("a");
							if (aElements != null && !aElements.isEmpty()) {
								Element aElement = aElements.first();
								aText = aElement.text();
								aLink = aElement.absUrl("href");
								if(StringUtil.isBlank(aLink)){
									aLink = aElement.attr("href");
								}
							} else {
								sb.append(Constants.HERO_CONTENT_DESCRIPTION_ELEMENT_DOESNOT_EXISTS);
								log.debug("No p elements found with in the div class 'c50-text' with in div class 'c50-pilot'");
							}
						} else {
							sb.append(Constants.HERO_CONTENT_PANEL_TEXT_ELEMENT_NOT_FOUND);
							log.debug("No element found with div class 'c50-text'");
						}
						Elements imgElements = heroElement.select("div.c50-image");
						if (imgElements != null && !imgElements.isEmpty()) {
							Element imgElement = imgElements.first();
							Elements imageElements = imgElement.getElementsByTag("img");
							if (imageElements != null && !imageElements.isEmpty()) {
								Element imageElement = imageElements.first();
								imagePath = imageElement.attr("src");
							} else {
								sb.append(Constants.HERO_CONTENT_PANEL_IMAGE_ELEMENT_NOT_FOUND);
							}
						} else {
							sb.append(Constants.HERO_CONTENT_PANEL_IMAGE_ELEMENT_NOT_FOUND);
						}
					} else {
						sb.append(Constants.HERO_CONTENT_PANEL_ELEMENT_NOT_FOUND);
						log.debug("No element found with div class 'c50-pilot'");
					}
					// end of get content logic.
					// start of set content logic.

					if (indexLeftNode.hasNode("hero_panel")) {
						Node hero_panel_Node = indexLeftNode.getNode("hero_panel");

						if (StringUtils.isNotBlank(h2Text)) {
							hero_panel_Node.setProperty("title", h2Text);
						} else {
							sb.append(Constants.HERO_CONTENT_HEADING_IS_BLANK);
						}

						if (StringUtils.isNotBlank(pText)) {
							hero_panel_Node.setProperty("description", pText);
						} else {
							sb.append(Constants.HERO_CONTENT_DESCRIPTION_IS_BLANK);
						}

						if (StringUtils.isNotBlank(aText)) {
							hero_panel_Node.setProperty("linktext", aText);
						} else {
							sb.append(Constants.HERO_CONTENT_ANCHOR_TEXT_IS_BLANK);
						}
						if (hero_panel_Node.hasNode("cta")) {
							Node cta_Node = hero_panel_Node.getNode("cta");
							if (StringUtils.isNotBlank(aLink)) {
								cta_Node.setProperty("url", aLink);
							} else {
								sb.append(Constants.HERO_CONTENT_ANCHOR_LINK_IS_BLANK);
							}
						} else {
							sb.append(Constants.HERO_CONTENT_ANCHOR_NODE_NOT_FOUND);
						}
						if (hero_panel_Node.hasNode("image")) {
							Node imageNode = hero_panel_Node.getNode("image");
							if (StringUtils.isNotBlank(imagePath)) {
								String fileReference = imageNode.hasProperty("fileReference") ? imageNode.getProperty("fileReference").getString() : "";
								log.debug("imagePath before migration : " + imagePath);
								imagePath = FrameworkUtils.migrateDAMContent(imagePath, fileReference, locale, sb);
								log.debug("imagePath after migration : " + imagePath);
								imageNode.setProperty("fileReference", imagePath);
							} else {
								sb.append(Constants.HERO_CONTENT_IMAGE_LINK_IS_BLANK);
							}
						} else {
							sb.append(Constants.HERO_CONTENT_IMAGE_NODE_NOT_FOUND);
						}
					} else {
						sb.append(Constants.HERO_CONTENT_NODE_NOT_FOUND);
					}
					// end of set content logic.
				} catch (Exception e) {
					sb.append(Constants.EXCEPTION_IN_HERO_MIGRATION);
					log.debug("Exception in Hero Element Migration" + e);
				}
				// End Hero Migration

				// Start of logic for business links migration.
				try {
					// start of get logic.
					Elements grid_elements = doc.select("div.gd22v1-pilot");
					if (grid_elements != null && !grid_elements.isEmpty()) {
						Element grid_element = grid_elements.first();
						Elements gd_left_Elements = grid_element.select("div.gd22v1-left");
						if (gd_left_Elements != null
								&& !gd_left_Elements.isEmpty()) {
							Element gd_left_Element = gd_left_Elements.first();
							Elements liElements = gd_left_Element.getElementsByTag("li");
							Node thirds_0 = null;
							if (mainNode.hasNode("thirds_0")) {
								thirds_0 = mainNode.getNode("thirds_0");
							}
							int count = 0;
							for (Element ele : liElements) {
								Elements aElements = ele.getElementsByTag("a");
								if (aElements != null && !aElements.isEmpty()) {
									if (count == 0) {
										if (thirds_0 != null && thirds_0.hasNode("Th-Third-1")) {
											Node Th_Third_1 = thirds_0.getNode("Th-Third-1");
											setHtmlContent(Th_Third_1, aElements, locale, urlMap);
										}
									}
									if (count == 1) {
										if (thirds_0 != null && thirds_0.hasNode("Th-Third-2")) {
											Node Th_Third_2 = thirds_0.getNode("Th-Third-2");
											setHtmlContent(Th_Third_2, aElements, locale, urlMap);
										}
									}
									if (count == 2) {
										if (thirds_0 != null && thirds_0.hasNode("Th-Third-3")) {
											Node Th_Third_3 = thirds_0.getNode("Th-Third-3");
											setHtmlContent(Th_Third_3, aElements, locale, urlMap);
										}
									}
									if (count > 2) {
										sb.append(Constants.EXTRA_ANCHOR_LINK_FOUND_IN_WEB_HTML_CONTENT + "'" + aElements.first().html() + "'");
									}
									count++;
								} else {
									log.debug("No anchor element found with in the li : " + ele.html());
									sb.append(Constants.NO_ANCHOS_FOUND_IN_WEB_HTML_CONTENT);
								}
							}
						} else {
							sb.append(Constants.LEFT_GRID_ELEMENT_NOT_FOUND);
						}
						Elements gd_right_Elements = grid_element.select("div.gd-right");
					} else {
						sb.append(Constants.LEFT_GRID_ELEMENT_NOT_FOUND);
					}
				} catch (Exception e) {
					sb.append(Constants.GRID_ELEMENT_NOT_FOUND);
					log.debug("Exception : ", e);
				}
				// End of logic for business links migration.

				// Start of logic for industries links migration.
				try {
					Elements grid_elements = doc.select("div.gd22v1-pilot");
					if (grid_elements != null && !grid_elements.isEmpty()) {
						Element grid_element = grid_elements.first();
						Elements gd_left_Elements = grid_element.select("div.gd22v1-right");
						if (gd_left_Elements != null && !gd_left_Elements.isEmpty()) {
							Element gd_left_Element = gd_left_Elements.first();
							Elements liElements = gd_left_Element.getElementsByTag("li");
							Node thirds_1 = null;
							if (mainNode.hasNode("thirds_1")) {
								thirds_1 = mainNode.getNode("thirds_1");
							}
							Elements element = new Elements();
							int count = 1;
							int index = 0;
							for (Element ele : liElements) {
								Elements aElements = ele.getElementsByTag("a");
								if (aElements != null && !aElements.isEmpty()) {
									Element aElement = aElements.first();
									element.add(aElement);
									if (count % 3 == 0) {
										if (index == 0) {
											if (thirds_1 != null && thirds_1.hasNode("Th-Third-1")) {
												Node Th_Third_1 = thirds_1.getNode("Th-Third-1");
												setHtmlContent(Th_Third_1, element, locale, urlMap);
											} else {
												sb.append(Constants.HTMLBLOB_NODE_DOES_NOT_EXIST + " : 'Th-Third-1'");
												log.debug("Node doesn't exist with name : 'Th-Third-1'");
											}
											element = new Elements();
										}
										if (index == 1) {
											if (thirds_1 != null && thirds_1.hasNode("Th-Third-2")) {
												Node Th_Third_2 = thirds_1.getNode("Th-Third-2");
												setHtmlContent(Th_Third_2, element, locale, urlMap);
											} else {
												sb.append(Constants.HTMLBLOB_NODE_DOES_NOT_EXIST + " : 'Th-Third-2'");
												log.debug("Node doesn't exist with name : 'Th-Third-2'");
											}
											element = new Elements();
										}

										if (index == 2) {
											if (thirds_1 != null && thirds_1.hasNode("Th-Third-3")) {
												Node Th_Third_3 = thirds_1.getNode("Th-Third-3");
												setHtmlContent(Th_Third_3, element, locale, urlMap);
											} else {
												sb.append(Constants.HTMLBLOB_NODE_DOES_NOT_EXIST + " : 'Th-Third-3'");
												log.debug("Node doesn't exist with name : 'Th-Third-3'");
											}
											element = new Elements();
										}
										index++;
									}
									if (index > 2) {
										sb.append(Constants.EXTRA_ANCHOR_LINK_FOUND_IN_WEB_HTML_CONTENT + "'" +aElement.text()+"'");
									}
								}
								count++;
							}
						} else {
							sb.append(Constants.NO_ANCHOS_FOUND_IN_WEB_HTML_CONTENT);
							log.debug("div element not found with class 'gd22v1-left'");
						}
					} else {
						sb.append(Constants.NO_ANCHOS_FOUND_IN_WEB_HTML_CONTENT);
						log.debug("div element not found with class 'gd22v1-pilot'");
					}
				} catch (Exception e) {
					log.debug("Excepiton ", e);
				}
				// End of logic for industries links migration.
			} else {
				sb.append(Constants.URL_CONNECTION_EXCEPTION);
			}
		} catch (Exception e) {
			sb.append(Constants.UNABLE_TO_MIGRATE_PAGE);
			log.error("Exception : ", e);
		}
		sb.append("</ul></td>");
		session.save();
		log.debug("Msg returned is " + sb.toString());
		return sb.toString();
	}

	public void setHtmlContent(Node node, Elements aElements, String locale,
			Map<String, String> urlMap) {
		try {
			Elements product_contents = null;
			Document document1 = null;
			Node htmlblob = null;
			if (node.hasNode("htmlblob")) {
				htmlblob = node.getNode("htmlblob");
				String wemhtml = htmlblob.hasProperty("html") ? htmlblob.getProperty("html").getString() : "";
				document1 = Jsoup.parse(wemhtml);
				product_contents = document1.select("div.product-content");
			} else {
				log.debug("'htmlblob' node not found with in the node : " + node.getPath());
			}
			if (aElements != null && !aElements.isEmpty()) {
				int count = 0;
				for (Element aElement : aElements) {
					String aText = aElement.ownText();
					String aLink = aElement.absUrl("href");
					if(StringUtil.isBlank(aLink)){
						aLink = aElement.attr("href");
					}
					if (node.hasNode("htmlblob")) {
						if (product_contents != null && !product_contents.isEmpty()) {
							if (count < product_contents.size()) {
								Element product_content = product_contents.get(count);
								Elements wemAElements = product_content.getElementsByTag("a");
								if (wemAElements != null && !wemAElements.isEmpty()) {
									Element wemAElement = wemAElements.first();
									wemAElement.text(aText);
									wemAElement.attr("href", aLink);
								} else {
									sb.append(Constants.NO_ANCHORS_FOUND_IN_WEM_HTML_CONTENT);
									log.debug("No anchor elements found in wem html content in : " + product_content.html());
								}
							} else {
								log.debug("No link found in wem to migrate the web link.");
							}
						} else {
							sb.append(Constants.NO_ANCHORS_FOUND_IN_WEM_HTML_CONTENT);
							log.debug("No element found in the wem html content with div class 'product-content'");
						}
					}
					count++;
				}
			} else {
				sb.append(Constants.NO_ANCHOS_FOUND_IN_WEB_HTML_CONTENT);
				log.debug("No anchro elements found in the web html content.");
			}
			if (document1 != null) {
				Element bodyContentElement = document1.getElementsByTag("body").first();
				String html = FrameworkUtils.extractHtmlBlobContent(bodyContentElement, "", locale, sb, urlMap);
				if (htmlblob != null) {
					htmlblob.setProperty("html", html);
				}
			} else {
				sb.append(Constants.NO_HTML_CONTET_FOUND_IN_WEM);
				log.debug("No html content found in the node.");
			}
		} catch (Exception e) {
			log.error("Exception : ", e);
		}
	}
}