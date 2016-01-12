package com.cisco.dse.global.migration.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.sling.commons.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.cisco.dse.global.migration.config.BaseAction;
import com.cisco.dse.global.migration.config.Constants;
import com.cisco.dse.global.migration.config.FrameworkUtils;
import com.cisco.dse.global.migration.productlanding.ProductLandingVariation1;

public class WebVariation3 extends BaseAction{


	Document doc;
	String title = null;
	StringBuilder sb = new StringBuilder(1024);
	static Logger log = Logger.getLogger(WebVariation3.class);

	public String translate(String host, String loc, String prod, String type,
			String catType, String locale, Session session,
			Map<String, String> urlMap) throws IOException,
			ValueFormatException, VersionException, LockException,
			ConstraintViolationException, RepositoryException {
		BasicConfigurator.configure();

		// Repo node paths
		String pagePropertiesPath = "/content/<locale>/" + catType + "/<prod>/overview/jcr:content";
		String midSizeUpperLeft = "/content/<locale>/" + catType + "/<prod>/overview/jcr:content/content_parsys/overview/layout-overview/gd12v2/gd12v2-left";
		String midSizeUpperRight = "/content/<locale>/" + catType + "/<prod>/overview/jcr:content/content_parsys/overview/layout-overview/gd12v2/gd12v2-right";
		String midSizeLowerLeft = "/content/<locale>/" + catType + "/<prod>/overview/jcr:content/content_parsys/overview/layout-overview/gd12v2_0/gd12v2-left";
		String midSizeLowerRight = "/content/<locale>/" + catType + "/<prod>/overview/jcr:content/content_parsys/overview/layout-overview/gd12v2_0/gd12v2-right";
		String midSizeMiddle = "/content/<locale>/" + catType + "/<prod>/overview/jcr:content/content_parsys/overview/layout-overview/gd11v1/gd11v1-mid";
		String pageUrl = host + "/content/<locale>/" + catType + "/<prod>/overview.html";

		pageUrl = pageUrl.replace("<locale>", locale).replace("<prod>", prod);
		pagePropertiesPath = pagePropertiesPath.replace("<locale>", locale).replace("<prod>", prod);

		sb.append("<td>" + "<a href=" + pageUrl + ">" + pageUrl + "</a>" + "</td>");
		sb.append("<td>" + "<a href=" + loc + ">" + loc + "</a>" + "</td>");
		sb.append("<td><ul>");

		log.debug("In the translate method to migarate " + loc + " to " + pageUrl);

		midSizeUpperLeft = midSizeUpperLeft.replace("<locale>", locale).replace("<prod>", prod);
		midSizeUpperRight = midSizeUpperRight.replace("<locale>", locale).replace("<prod>", prod);
		midSizeLowerLeft = midSizeLowerLeft.replace("<locale>", locale).replace("<prod>", prod);
		midSizeLowerRight = midSizeLowerRight.replace("<locale>", locale).replace("<prod>", prod);
		midSizeMiddle = midSizeMiddle.replace("<locale>", locale).replace("<prod>", prod);

		Node midSizeUpperLeftNode = null;
		Node midSizeUpperRightNode = null;
		Node midSizeLowerLeftNode = null;
		Node midSizeLowerRightNode = null;
		Node midSizeMiddleNode = null;
		Node pageJcrNode = null;
		try {
			midSizeUpperLeftNode = session.getNode(midSizeUpperLeft);
			midSizeUpperRightNode = session.getNode(midSizeUpperRight);
			midSizeLowerLeftNode = session.getNode(midSizeLowerLeft);
			midSizeLowerRightNode = session.getNode(midSizeLowerRight);
			midSizeMiddleNode = session.getNode(midSizeMiddle);
			pageJcrNode = session.getNode(pagePropertiesPath);
			try {
				doc = Jsoup.connect(loc).get();
			} catch (Exception e) {
				doc = getConnection(loc);
			}
			if (doc != null) {
				title = doc.title();
				// ------------------------------------------------------------------------------------------------------------------------------------------
				// start set page properties.
				FrameworkUtils.setPageProperties(pageJcrNode, doc, session, sb);
				// end set page properties.
				// ------------------------------------------------------------------------------------------------------------------------------------------
				// start set Hero Large component content.
				try {
					Node heroLargeNode = null;
					Value[] panelPropertiest = null;
					if (midSizeUpperLeftNode.hasNode("hero_large")) {
						heroLargeNode = midSizeUpperLeftNode.getNode("hero_large");
						Property panelNodesProperty = heroLargeNode.hasProperty("panelNodes") ? heroLargeNode.getProperty("panelNodes") : null;
						if (panelNodesProperty.isMultiple()) {
							panelPropertiest = panelNodesProperty.getValues();
						}
					} else {
						log.debug("<li>Node with name 'hero_large' doesn't exist under " + midSizeUpperLeftNode.getPath() + "</li>");
						log.debug("Node with name 'hero_large' doesn't exist under " + midSizeUpperLeftNode.getPath());
					}

					Elements heroLargeElements = doc.select("div.c50-pilot");
					if (heroLargeElements != null) {
						Element heroLargeElement = heroLargeElements.first();
						if (heroLargeElement != null) {
							Elements heroLargeFrameElements = heroLargeElement.select("div.frame");
							Node heroPanelNode = null;
							if (heroLargeFrameElements != null) {
								if (heroLargeFrameElements.size() != heroLargeNode.getNodes("heropanel*").getSize()) {
									sb.append("<li>Mismatch in the count of slides in the hero component </li>");
								}
								int i = 0;
								for (Element ele : heroLargeFrameElements) {
									String heroPanelTitle = "";
									String heroPanelDescription = "";
									String heroPanelLinkText = "";
									String heroPanellinkUrl = "";
									Elements heroTitleElements = ele.getElementsByTag("h2");
									if (heroTitleElements != null) {
										Element heroTitleElement = heroTitleElements.first();
										if (heroTitleElement != null) {
											heroPanelTitle = heroTitleElement.text();
										} else {
											sb.append("<li>Hero Panel element not having any title in it </li>");
											log.debug("No h2 first element found with in the class 'frame' of div.");
										}
									} else {
										sb.append("<li>Hero Panel title element is not found</li>");
										log.debug("No h2 found with in the class 'frame' of div.");
									}
									Elements heroDescriptionElements = ele.getElementsByTag("p");
									if (heroDescriptionElements != null) {
										Element heroDescriptionElement = heroDescriptionElements.first();
										if (heroDescriptionElement != null) {
											heroPanelDescription = heroDescriptionElement.text();
										} else {
											sb.append("<li>Hero Panel element not having any description in it </li>");
											log.debug("No p frist element found with in the class 'frame' of div.");
										}
									} else {
										sb.append("<li>Hero Panel para element not found </li>");
										log.debug("No p elemtn found with in the class 'frame' of div.");
									}
									Elements heroPanelLinkTextElements = ele.getElementsByTag("b");
									if (heroPanelLinkTextElements != null) {
										Element heroPanelLinkTextElement = heroPanelLinkTextElements.first();
										if (heroPanelLinkTextElement != null) {
											heroPanelLinkText = heroPanelLinkTextElement.text();
										} else {
											sb.append("<li>Hero Panel element not having any linktext in it </li>");
											log.debug("No b tags first elemtn found with in the class 'frame' of div.");
										}
									} else {
										sb.append("<li>Hero Panel linktext element not found  </li>");
										log.debug("No b tag found with the class 'frame' of div.");
									}
									Elements heroPanelLinkUrlElements = ele.getElementsByTag("a");
									if (heroPanelLinkUrlElements != null) {
										Element heroPanelLinkUrlElement = heroPanelLinkUrlElements.first();
										if (heroPanelLinkUrlElement != null) {
											heroPanellinkUrl = heroPanelLinkUrlElement.attr("href");
											// Start extracting valid href
											log.debug("heroPanellinkUrl before migration : " + heroPanellinkUrl);
											heroPanellinkUrl = FrameworkUtils.getLocaleReference(heroPanellinkUrl, urlMap);
											log.debug("heroPanellinkUrl after migration : " + heroPanellinkUrl);
											// End extracting valid href
										} else {
											sb.append("<li>Hero Panel element not having any linkurl in it </li>");
											log.debug("No anchor first element found with in the class 'frame' of div.");
										}
									} else {
										sb.append("<li>Hero Panel link url element not found </li>");
										log.debug("No anchor element found with in the class 'frame' of div.");
									}
									// start image
									String heroImage = FrameworkUtils.extractImagePath(ele, sb);
									log.debug("heroImage path : " + heroImage);
									// end image
									log.debug("heroPanelTitle : " + heroPanelTitle);
									log.debug("heroPanelDescription : " + heroPanelDescription);
									log.debug("heroPanelLinkText : " + heroPanelLinkText);
									log.debug("heroPanellinkUrl : " + heroPanellinkUrl);

									if (panelPropertiest != null && i <= panelPropertiest.length) {
										String propertyVal = panelPropertiest[i].getString();
										if (StringUtils.isNotBlank(propertyVal)) {
											JSONObject jsonObj = new JSONObject(propertyVal);
											if (jsonObj.has("panelnode")) {
												String panelNodeProperty = jsonObj.get("panelnode").toString();
												heroPanelNode = heroLargeNode.hasNode(panelNodeProperty) ? heroLargeNode.getNode(panelNodeProperty) : null;
											}
										}
										i++;
									} else {
										sb.append("<li>No heropanel Node found.</li>");
										log.debug("No list panelProperties found for the hero compoent order.");
									}

									if (heroPanelNode != null) {
										Node heroPanelPopUpNode = null;
										Elements lightBoxElements = ele.select("div.c50-image").select("a.c26v4-lightbox");
										if (lightBoxElements != null && !lightBoxElements.isEmpty()) {
											Element lightBoxElement = lightBoxElements.first();
											heroPanelPopUpNode = FrameworkUtils.getHeroPopUpNode(heroPanelNode);
										}
										if (StringUtils.isNotBlank(heroPanelTitle)) {
											heroPanelNode.setProperty("title", heroPanelTitle);
											if (heroPanelPopUpNode != null) {
												heroPanelPopUpNode.setProperty("popupHeader", heroPanelTitle);
											} else {
												sb.append("<li>Hero content video pop up node not found.</li>");
												log.debug("No pop-up node found for the hero panel node " + heroPanelNode.getPath());
											}
										} else {
											sb.append("<li>title of hero slide doesn't exist</li>");
											log.debug("Title is blank with in the 'frame' class of div.");
										}
										if (StringUtils.isNotBlank(heroPanelDescription)) {
											heroPanelNode.setProperty("description", heroPanelDescription);
										} else {
											sb.append("<li>description of hero slide doesn't exist</li>");
											log.debug("Description is blank with in the 'frame' class of the div.");
										}
										if (StringUtils.isNotBlank(heroPanelLinkText)) {
											heroPanelNode.setProperty("linktext", heroPanelLinkText);
										} else {
											sb.append("<li>link text of hero slide doesn't exist</li>");
											log.debug("Link Text doesn't exists with in the class 'frame' of the div.");
										}
										if (StringUtils.isNotBlank(heroPanellinkUrl)) {
											heroPanelNode.setProperty("linkurl", heroPanellinkUrl);
										} else {
											sb.append("<li>link url of hero slide doesn't exist / found video as link url for the slide on web publisher page </li>");
											log.debug("Link url doesn't exists with in the class 'frame' of the div.");
										}
										if (heroPanelNode.hasNode("image")) {
											Node imageNode = heroPanelNode.getNode("image");
											String fileReference = imageNode.hasProperty("fileReference") ? imageNode.getProperty("fileReference").getString() : "";
											heroImage = FrameworkUtils.migrateDAMContent(heroImage, fileReference, locale, sb);
											log.debug("heroImage : " + heroImage);
											if (StringUtils.isNotBlank(heroImage)) {
												imageNode.setProperty("fileReference", heroImage);
											}
										} else {
											sb.append("<li>hero image node doesn't exist</li>");
											log.debug("'image' node doesn't exists in " + heroPanelNode.getPath());
										}
									}
								}
							} else {
								log.debug("<li>Hero Large Frames/Panel Elements is not found</li>");
								log.debug("No div found with class 'frame'");
							}
						} else {
							sb.append("<li>Hero Large Element is not found</li>");
							log.debug("No first element found with class 'c50-pilot'");
						}
					} else {
						sb.append("<li>Hero Large component is not found on web publisher page</li>");
						log.debug("No element found with class 'c50-pilot'");
					}
				} catch (Exception e) {
					log.debug("<li>Unable to update hero_large component.</li>");
					log.debug("Exception : ", e);
				}
				// end set Hero Large content.
				// ------------------------------------------------------------------------------------------------------------------------------------------

				// ---------------------------------------------------------------------------------------------------------------------------------------
				// start of gd-right html blob components content.
				try {
					log.debug("Started Migrating gd-right html blob.");
					Elements gdRightElements = doc.select("div.gd-right");
					if (!gdRightElements.isEmpty() && gdRightElements.size() > 0) {
						int count = 1;
						for (Element gdRightElement : gdRightElements) {
							
							if (gdRightElement != null) {
								String html = FrameworkUtils.extractHtmlBlobContent(gdRightElement, "", locale, sb, urlMap);
								if (count == 1) {
									log.debug("Started Migrating upper gd-right html blob.");
									if (midSizeUpperRightNode.hasNode("htmlblob")) {
										Node htmlblobNode = midSizeUpperRightNode.getNode("htmlblob");
										htmlblobNode.setProperty("html", html);
									}
								}
								if (count == 2) {
									log.debug("Started Migrating upper gd-right html blob.");
									if (midSizeLowerRightNode.hasNode("htmlblob")) {
										Node htmlblobNode = midSizeLowerRightNode.getNode("htmlblob");
										htmlblobNode.setProperty("html", html);
									}
								}
							} else {
								sb.append("<li>gd-right htmlblob component not found on publisher page </li>");
							}
							count = count + 1 ;
						}
					}

					

				} catch (Exception e) {
					log.debug("Excepiton : ", e);
				}
				// end set gd-right html blob component content.
				// ---------------------------------------------------------------------------------------------------------------------------------------
				// start of gd-mid html blob components content.
				try {
					log.debug("Started Migrating gd-mid html blob.");
					String html = "";
					Elements gdMidElements = doc.select("div.gd-mid");
					if (!gdMidElements.isEmpty() && gdMidElements.size() > 0) {
						Element gdMidElement = gdMidElements.first();
						if (gdMidElement != null) {
							html = FrameworkUtils.extractHtmlBlobContent(gdMidElement, "", locale, sb, urlMap);
							if (midSizeMiddleNode.hasNode("htmlblob")) {
								Node htmlblobNode = midSizeMiddleNode.getNode("htmlblob");
								htmlblobNode.setProperty("html", html);
							}
						} else {
							sb.append("<li>gd-mid htmlblob component not found on publisher page </li>");
						}
					}

				} catch (Exception e) {
					log.debug("Excepiton : ", e);
				}
				// end set gd-mid html blob component content.
				// ---------------------------------------------------------------------------------------------------------------------------------------
				// start of gd-left html blob components content.
				try {
					log.debug("Started Migrating gd-left html blob.");
					Elements gdLeftElements = doc.select("div.gd-left");
					if (!gdLeftElements.isEmpty() && gdLeftElements.size() > 0) {
						int count = 0;
						for (Element gdLeftElement : gdLeftElements) {
							if (gdLeftElement != null) {
								String html = FrameworkUtils.extractHtmlBlobContent(gdLeftElement, "", locale, sb, urlMap);
								count = count + 1 ;
								if (count == 1) {
									log.debug("upper gd-left is found so skipping");
									continue;
								}
								if (count == 2) {
									log.debug("Started lower gd-left html blob.");
									if (midSizeLowerLeftNode.hasNode("htmlblob")) {
										Node htmlblobNode = midSizeLowerLeftNode.getNode("htmlblob");
										htmlblobNode.setProperty("html", html);
									}
								}
							} else {
								sb.append("<li>gd-left htmlblob component not found on publisher page </li>");
							}
							
						}
					}

				} catch (Exception e) {
					log.debug("Excepiton : ", e);
				}
				// end set gd-left html blob component content.
				// ---------------------------------------------------------------------------------------------------------------------------------------
				session.save();
			} else {
				sb.append(Constants.URL_CONNECTION_EXCEPTION);
			}
		} catch (Exception e) {
			log.error("Exception ", e);
		}
		sb.append("</ul></td>");
		return sb.toString();
	}

}
