/* 
 * S.No		Name	Date		Description of change
 * 1		kiran	11-jan-16	Added the Java file to handle the migration of web featured-case-studies pages.
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
public class WebVariation9 extends BaseAction{
	Document doc;
	StringBuilder sb = new StringBuilder(1024);
	static Logger log = Logger.getLogger(WebVariation9.class);
	
	int noImageCount = 0;
	
	public String translate(String host, String loc, String prod, String type,
			String catType, String locale, Session session, Map<String, String> urlMap) throws IOException,
			ValueFormatException, VersionException, LockException,
			ConstraintViolationException, RepositoryException {
		log.debug("In the translate method of WebVariation9");
		log.debug("In the translate method, catType is :" + catType);

		String pagePropertiesPath = "/content/<locale>/" + catType
				+ "/<prod>/featured-case-studies/jcr:content";
		String pageUrl = host + "/content/<locale>/" + catType
				+ "/<prod>/featured-case-studies.html";
		pagePropertiesPath = pagePropertiesPath.replace("<locale>", locale)
				.replace("<prod>", prod);
		pageUrl = pageUrl.replace("<locale>", locale).replace("<prod>", prod);
		
		sb.append("<td>" + "<a href=" + pageUrl + ">" + pageUrl + "</a>"
				+ "</td>");
		sb.append("<td>" + "<a href=" + loc + ">" + loc + "</a>" + "</td>");
		sb.append("<td><ul>");
		
		String webNodePath = "/content/<locale>/"
				+ catType
				+ "/<prod>/featured-case-studies/jcr:content/content_parsys/generic/layout-generic/gd12v2_0";
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
					Node heroLargeNode = null;
					Value[] panelPropertiest = null;
					String heroTitle = "";
					String heroDescription = "";
					String heroLinkText = "";
					String herolinkUrl = "";
					heroLargeNode = webNode
							.hasNode("gd12v2-left/hero_large") ? webNode
							.getNode("gd12v2-left/hero_large") : null;
					if (heroLargeNode != null) {
						Property panelNodesProperty = heroLargeNode
								.hasProperty("panelNodes") ? heroLargeNode
								.getProperty("panelNodes") : null;
						if (panelNodesProperty.isMultiple()) {
							panelPropertiest = panelNodesProperty.getValues();
						}
					} else {
						sb.append(Constants.HERO_NODE_NOT_AVAILABLE);
						log.debug("Node with name 'hero_Large' doesn't exist under");
					}

					Element heroLargeElement = doc.select("div.c50-pilot")
							.first();
					if (heroLargeElement != null) {
						Elements heroLargeFrameElements = heroLargeElement
								.select("div.frame");
						if(heroLargeFrameElements.isEmpty()){
							heroLargeFrameElements = doc.select("div.c50-pilot");
						}
						Node heroPanelNode = null;
						if (heroLargeFrameElements != null) {
							if (heroLargeFrameElements.size() != heroLargeNode
									.getNodes("heropanel*").getSize()) {
								sb.append(Constants.MISMATCH_IN_HERO_SLIDES);
							}
							int i = 0;
							for (Element ele : heroLargeFrameElements) {
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
													urlMap, locale, sb);
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
											heroPanelNode = heroLargeNode
													.hasNode(panelNodeProperty) ? heroLargeNode
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
					StringBuilder oldImage = new StringBuilder();
					
					log.debug("Started migrating HtmlBlob content.");
					// Start get content.
					Elements htmlBlobElements = doc.select("div.gd-left").select("div.c00-pilot,div.c11-pilot,div.n12-pilot");
					if (htmlBlobElements != null) {
						/*Element h2Ele = htmlBlobElements.select("h2").first();
						oldImage.append(h2Ele);*/
						
						for(Element ele:htmlBlobElements)
						{
							htmlBlobContent = FrameworkUtils.extractHtmlBlobContent(ele, "", locale, sb, urlMap);
							oldImage.append(htmlBlobContent);
						}
						
					}
				 else {
					sb.append(Constants.HTMLBLOB_ELEMENT_NOT_FOUND);
				}
					//End of getContent
					//Start of set content
					if (webNode.hasNode("gd12v2-left/htmlblob")) {
						Node htmlBlobNode = webNode.getNode("gd12v2-left/htmlblob");
						if (!StringUtils.isEmpty(htmlBlobContent)) {
							htmlBlobNode.setProperty("html",oldImage.toString());
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
					StringBuilder oldImage = new StringBuilder();
					
					log.debug("Started migrating HtmlBlob content.");
					// Start get content.
					Elements htmlBlobElements = doc.select("div.gd-right").select("div.n13-pilot");
					if (htmlBlobElements != null) {
						for(Element ele : htmlBlobElements)
						{
							htmlBlobContent = FrameworkUtils.extractHtmlBlobContent(ele, "", locale, sb, urlMap);
							oldImage.append(htmlBlobContent);
							}
					}
					//End of getContent
					//Start of set content
					if (webNode.hasNode("gd12v2-right/htmlblob")) {
						Node htmlBlobNode = webNode.getNode("gd12v2-right/htmlblob");
						if (!StringUtils.isEmpty(htmlBlobContent)) {
							htmlBlobNode.setProperty("html",oldImage.toString());
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

					
						
					



