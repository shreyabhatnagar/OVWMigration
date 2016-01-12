/* 
 * S.No		Name	Date		Description of change
 * 1		kiran   11-jan-16	Added the Java file to handle the migration of web about pages.
 * 
 * */

package com.cisco.dse.global.migration.web;

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
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.cisco.dse.global.migration.config.BaseAction;
import com.cisco.dse.global.migration.config.Constants;
import com.cisco.dse.global.migration.config.FrameworkUtils;



public class WebVariation1 extends BaseAction{
	Document doc;
	StringBuilder sb = new StringBuilder(1024);
	static Logger log = Logger.getLogger(WebVariation1.class);
	
	int noImageCount = 0;
	
	public String translate(String host, String loc, String prod, String type,
			String catType, String locale, Session session, Map<String, String> urlMap) throws IOException,
			ValueFormatException, VersionException, LockException,
			ConstraintViolationException, RepositoryException {
		log.debug("In the translate method of WebVariation1");
		log.debug("In the translate method, catType is :" + catType);

		String pagePropertiesPath = "/content/<locale>/" + catType
				+ "/jcr:content";
		String pageUrl = host + "/content/<locale>/" + catType
				+ ".html";
		pagePropertiesPath = pagePropertiesPath.replace("<locale>", locale)
				.replace("<prod>", prod);
		pageUrl = pageUrl.replace("<locale>", locale).replace("<prod>", prod);
		
		sb.append("<td>" + "<a href=" + pageUrl + ">" + pageUrl + "</a>"
				+ "</td>");
		sb.append("<td>" + "<a href=" + loc + ">" + loc + "</a>" + "</td>");
		sb.append("<td><ul>");
		
		String webNodePath = "/content/<locale>/"
				+ catType
				+ "/jcr:content/content_parsys/generic/layout-generic/gd12v2";
		//String webBottomNodePath = "/content/<locale>/"
				//+ catType
			//	+ "/<prod>//content/en/us/solutions/test-wp-on-wem/about/jcr:content/content_parsys/generic/layout-generic/gd12v2/gd12v2-right";

		webNodePath = webNodePath.replace("<locale>",
				locale).replace("<prod>", prod);
		/*webBottomNodePath = webBottomNodePath.replace("<locale>",
				locale).replace("<prod>", prod);*/
		javax.jcr.Node webNode = null;
	//	javax.jcr.Node webBottomNode = null;
		javax.jcr.Node pageJcrNode = null;
		try {

			webNode = session.getNode(webNodePath);
		//	webBottomNode = session.getNode(webBottomNodePath);
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
				// start of hero component
				try {
					Node heroMediumNode = null;
					Value[] panelPropertiest = null;
					String heroTitle = "";
					String heroDescription = "";
					String heroLinkText = "";
					String herolinkUrl = "";
					heroMediumNode = webNode
							.hasNode("gd12v2-left/hero_medium") ? webNode
							.getNode("gd12v2-left/hero_medium") : null;
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
											sb.append(Constants.IMAGE_NOT_FOUND_IN_LOCALE_PAGE);
										}
									} else {
										sb.append(Constants.HERO_IMAGE_NODE_NOT_FOUND);
										log.debug("'image' node doesn't exists in "
												+ heroPanelNode.getPath());
									}
								}
							}
						} else {
							log.debug(Constants.HERO_CONTENT_PANEL_ELEMENT_NOT_FOUND);
							log.debug("No div found with class 'frame'");
						}
					} else {
						;
						sb.append(Constants.HERO_LARGE_COMPONENT_NOT_FOUND);
						log.debug("No element found with class 'c50-pilot'");
					}
				} catch (Exception e) {
					sb.append(Constants.EXCEPTION_IN_HERO_MIGRATION);
					log.debug("Exception : ", e);
				}

				// end of hero component
				// --------------------------------------------------------------------------------------------------------
				// start of htmlblob component
				try {
					String htmlBlobContent = "";
					
					log.debug("Started migrating HtmlBlob content.");
					// Start get content.
					Elements htmlBlobElements = doc.select("div.gd-left").select("div.gd23-pilot,div.c00-pilot");
					if (htmlBlobElements != null) {
						htmlBlobContent = htmlBlobElements.outerHtml();
					}
					//End of getContent
					//Start of set content
					if (webNode.hasNode("gd12v2-left/htmlblob")) {
						Node htmlBlobNode = webNode.getNode("gd12v2-left/htmlblob");
						if (!StringUtils.isEmpty(htmlBlobContent)) {
							htmlBlobNode.setProperty("html",htmlBlobContent);
							log.debug("HtmlBlob Content migrated is done.");
						}
					} else {
						sb.append(Constants.HTMLBLOB_NODE_NOT_FOUND);
					}
				}	
				catch (Exception e) {
					sb.append(Constants.EXCEPTION_IN_HTMLBLOB);
					log.error("Exception " , e);
				}
				

				// End get content.
			//End of htmlblob Component
			//-------------------------------------------------------------------------------------
			//start of right-grid Component
			//start of htmlblob Component
				try {
					String htmlBlobContent = "";
					
					log.debug("Started migrating HtmlBlob content.");
					// Start get content.
					Element htmlBlobElements = doc.select("div.gd-right").last();
					if (htmlBlobElements != null) {
						htmlBlobContent = htmlBlobElements.outerHtml();
					}
					//End of getContent
					//Start of set content
					if (webNode.hasNode("gd12v2-right/htmlblob")) {
						Node htmlBlobNode = webNode.getNode("gd12v2-right/htmlblob");
						if (!StringUtils.isEmpty(htmlBlobContent)) {
							htmlBlobNode.setProperty("html",htmlBlobContent);
							log.debug("HtmlBlob Content migrated is done.");
						}
					} else {
						sb.append(Constants.HTMLBLOB_NODE_NOT_FOUND);
					}
				}

				
				
				
				
				catch (Exception e) {
					sb.append(Constants.EXCEPTION_IN_HTMLBLOB);
					log.error("Exception " , e);
				}
				// end htmlblob component.
				
				session.save();
			} else {
				sb.append(Constants.URL_CONNECTION_EXCEPTION);
			}
		} catch (Exception e) {
			sb.append(Constants.UNABLE_TO_MIGRATE_PAGE);
			log.debug("Exception as url cannot be connected: " + e);
			log.error("Exception " , e);
		}

		sb.append("</ul></td>");

		return sb.toString();

	}

}

					
						
					
