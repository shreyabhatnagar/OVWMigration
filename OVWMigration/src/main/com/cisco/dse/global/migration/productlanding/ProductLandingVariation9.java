package com.cisco.dse.global.migration.productlanding;

import java.io.IOException;
import java.util.ArrayList;
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
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.cisco.dse.global.migration.config.BaseAction;
import com.cisco.dse.global.migration.config.Constants;
import com.cisco.dse.global.migration.config.FrameworkUtils;

public class ProductLandingVariation9 extends BaseAction{

	Document doc;

	String title = null;

	StringBuilder sb = new StringBuilder(1024);

	String footerLinks = "{\"linktext\":\"<aaa>\",\"linkurl\":\"<bbb>\"}";

	static Logger log = Logger.getLogger(ProductLandingVariation9.class);

	public String translate(String host, String loc, String prod, String type,
			String catType, String locale, Session session,Map<String,String> urlMap) throws IOException,
			ValueFormatException, VersionException, LockException,
			ConstraintViolationException, RepositoryException {
		BasicConfigurator.configure();
		log.debug("In the translate method");
		log.debug("In the translate method, catType is :" + catType);

		// Repo node paths
		String pagePropertiesPath = "/content/<locale>/"+catType+"/<prod>/index/jcr:content";
		String indexLeft = "/content/<locale>/"
				+ catType
				+ "/<prod>/index/jcr:content/content_parsys/overview/layout-overview/gd12v1/gd12v1-left";
		String indexRight = "/content/<locale>/"
				+ catType
				+ "/<prod>/index/jcr:content/content_parsys/overview/layout-overview/gd12v1/gd12v1-right";
		String indexMidLeft = "/content/<locale>/"
				+ catType
				+ "/<prod>/index/jcr:content/content_parsys/overview/layout-overview/gd12v2_0/gd12v2-left";
		String indexRightRail = "/content/<locale>/"
				+ catType
				+ "/<prod>/index/jcr:content/content_parsys/overview/layout-overview/gd12v2_0/gd12v2-right";

		String pageUrl = host + "/content/<locale>/"
				+ catType + "/<prod>/index.html";

		pageUrl = pageUrl.replace("<locale>", locale).replace("<prod>", prod);
		pagePropertiesPath = pagePropertiesPath.replace("<locale>", locale).replace("<prod>", prod);
		sb.append("<td>" + "<a href=" + pageUrl + ">" + pageUrl + "</a>"
				+ "</td>");
		sb.append("<td>" + "<a href=" + loc + ">" + loc + "</a>" + "</td>");
		sb.append("<td><ul>");

		indexLeft = indexLeft.replace("<locale>", locale).replace("<prod>",
				prod);
		indexRight = indexRight.replace("<locale>", locale).replace("<prod>",
				prod);
		indexMidLeft = indexMidLeft.replace("<locale>", locale).replace(
				"<prod>", prod);
		indexRightRail = indexRightRail.replace("<locale>", locale).replace(
				"<prod>", prod);

		javax.jcr.Node indexLeftNode = null;
		javax.jcr.Node indexRightNode = null;
		javax.jcr.Node indexMidLeftNode = null;
		javax.jcr.Node indexRightRailNode = null;
		javax.jcr.Node pageJcrNode = null;
		try {
			indexLeftNode = session.getNode(indexLeft);
			indexRightNode = session.getNode(indexRight);
			indexMidLeftNode = session.getNode(indexMidLeft);
			indexRightRailNode = session.getNode(indexRightRail);
			pageJcrNode = session.getNode(pagePropertiesPath);
			try {
				doc = Jsoup.connect(loc).get();
				log.debug("Connected to the provided URL");
			} catch (Exception e) {
				doc = getConnection(loc);
			}

			if(doc != null){

				title = doc.title();

				// ------------------------------------------------------------------------------------------------------------------------------------------
				// start set page properties.

				FrameworkUtils.setPageProperties(pageJcrNode, doc, session, sb);

				// end set page properties.
				// ------------------------------------------------------------------------------------------------------------------------------------------


				// ------------------------------------------------------------------------------------------------------------------------------------------
				// start set benefit text content.
				try {
					String h3Text = "";
					String pText = "";
					String aText = "";
					String aHref = "";

					Elements primaryCtaElements = doc.select("div.c47-pilot");
					if (primaryCtaElements != null) {
						Node primaryCtaNode = indexLeftNode.hasNode("primary_cta") ? indexLeftNode
								.getNode("primary_cta") : null;
								if (primaryCtaNode != null) {
									for (Element ele : primaryCtaElements) {

										Elements h3TagText = ele.getElementsByTag("h3");
										if (h3TagText != null) {
											h3Text = h3TagText.html();
										} else {
											sb.append("<li>Primary CTA Heading element not having any title in it ('h3' is blank)</li>");
										}

										Elements descriptionText = ele
												.getElementsByTag("p");
										if (descriptionText != null) {
											pText = descriptionText.html();
										} else {
											sb.append("<li>Primary CTA description element not having any title in it ('p' is blank)</li>");
										}

										Element anchorText = ele.getElementsByTag("a").first();
										if (anchorText != null) {
											aText = anchorText.text();
											aHref = anchorText.absUrl("href");
											if(StringUtil.isBlank(aHref)){
												aHref = anchorText.attr("href");
											}
											// Start extracting valid href
											log.debug("Before primaryCTALinkUrl" + aHref + "\n");
											aHref = FrameworkUtils.getLocaleReference(aHref, urlMap, locale, sb);
											log.debug("after primaryCTALinkUrl" + aHref + "\n");
											// End extracting valid href
										} else {
											sb.append("<li>Primary CTA anchor tag not having any content in it ('<a>' is blank)</li>");
										}
										primaryCtaNode.setProperty("title", h3Text);
										primaryCtaNode.setProperty("description", pText);
										primaryCtaNode.setProperty("linktext", aText);
										primaryCtaNode.setProperty("linkurl", aHref);
									}
								}

					} else {
						sb.append("<li>Primary CTA Component not found on page. ('div.gd-left' class not found in the document)</li>");

					}

				} catch (Exception e) {
					sb.append("<li>Unable to update index primary cta component component."
							+ e + "</li>");
				}

				// end set primay cta title, description, link text, linkurl.
				// ---------------------------------------------------------------------------------------------------------------------------------------
				// start set hero large component properties.

				try {
					String h2Text = "";
					String pText = "";
					String aText = "";
					String aHref = "";
					Value[] panelPropertiest = null;
					Elements heroElements = doc.select("div.c50-pilot");
					heroElements = heroElements.select("div.frame");
					Node heroNode = indexRightNode.hasNode("hero_large") ? indexRightNode.getNode("hero_large") : null;
					Property panelNodesProperty = heroNode.hasProperty("panelNodes")?heroNode.getProperty("panelNodes"):null;
					if(panelNodesProperty.isMultiple()){
						panelPropertiest = panelNodesProperty.getValues();
						
					}
							if(heroElements != null){
								if (heroNode != null) {

									int eleSize = heroElements.size();
									NodeIterator heroPanelNodeIterator = heroNode.getNodes("heropanel*");
									int nodeSize = (int) heroPanelNodeIterator.getSize();
									if(eleSize != nodeSize){
										log.debug("Hero component node count mismatch!");
										sb.append("<li>Hero Component count mis match. Elements on page are: "+eleSize+" Node Count is: "+nodeSize+"</li>");
									}
									int i = 0;
									int imageSrcEmptyCount = 0;
									for (Element ele : heroElements) {
										Node heroPanelNode = null;
										if(panelPropertiest != null && i<=panelPropertiest.length){
											String propertyVal = panelPropertiest[i].getString();
											if(StringUtils.isNotBlank(propertyVal)){
												JSONObject jsonObj = new JSONObject(propertyVal);
												if(jsonObj.has("panelnode")){
													String panelNodeProperty = jsonObj.get("panelnode").toString();
													heroPanelNode = heroNode.hasNode(panelNodeProperty)?heroNode.getNode(panelNodeProperty):null;
												}
											}
											i++;
										}else{
											sb.append("<li>No heropanel Node found.</li>");
										}
										
										Elements h2TagText = ele.getElementsByTag("h2");
										if (h2TagText != null) {
											h2Text = h2TagText.html();
										} else {
											sb.append("<li>Hero Component Heading element not having any title in it ('h2' is blank)</li>");
										}

										Elements descriptionText = ele.getElementsByTag("p");
										if (descriptionText != null) {
											pText = descriptionText.first().text();
										} else {
											sb.append("<li>Hero Component description element not having any title in it ('p' is blank)</li>");
										}

										Element anchorText = ele.getElementsByTag("a").first();
										if (anchorText != null) {
											aText = anchorText.text();
											aHref = anchorText.absUrl("href");
											if(StringUtil.isBlank(aHref)){
												aHref = anchorText.attr("href");
											}
											// Start extracting valid href
											log.debug("Before heroPanelLinkUrl" + aHref + "\n");
											aHref = FrameworkUtils.getLocaleReference(aHref, urlMap, locale, sb);
											log.debug("after heroPanelLinkUrl" + aHref + "\n");
											// End extracting valid href
										} else {
											sb.append("<li>Hero Component anchor tag not having any content in it ('<a>' is blank)</li>");
										}

										// start image
										String heroImage = FrameworkUtils.extractImagePath(ele, sb);
										log.debug("heroImage before migration : " + heroImage + "\n");
										if (heroPanelNode != null) {
											Node heroPanelPopUpNode = null;
											Elements lightBoxElements = ele.select("div.c50-image").select("a.c26v4-lightbox");
											if(lightBoxElements != null && !lightBoxElements.isEmpty()){
												Element lightBoxElement = lightBoxElements.first();
												heroPanelPopUpNode = FrameworkUtils.getHeroPopUpNode(heroPanelNode);
											}
											
											if (heroPanelNode.hasNode("image")) {
												Node imageNode = heroPanelNode.getNode("image");
												String fileReference = imageNode.hasProperty("fileReference") ? imageNode.getProperty("fileReference").getString():"";
													heroImage = FrameworkUtils.migrateDAMContent(heroImage, fileReference, locale,sb);
													log.debug("heroImage after migration : " + heroImage + "\n");
													if (StringUtils.isNotBlank(heroImage)) {
														imageNode.setProperty("fileReference", heroImage);
													}else{
														imageSrcEmptyCount++;
													}
											} else {
												sb.append("<li>hero image node doesn't exist</li>");
											}
											
											if(heroPanelPopUpNode != null){
												heroPanelPopUpNode.setProperty("popupHeader", h2Text);
											}else{
												sb.append("<li>Hero content video pop up node not found.</li>");
											}
											
											heroPanelNode.setProperty("title", h2Text);
											heroPanelNode.setProperty("description", pText);
											heroPanelNode.setProperty("linktext", aText);
											heroPanelNode.setProperty("linkurl", aHref);
										}
										// end image
										
										

									}if(imageSrcEmptyCount > 0){
										sb.append("<li> " +imageSrcEmptyCount+ "image(s) are not found on locale page's hero element. </li>");
									}
	

								}

							}
							else{
								sb.append("<li>Hero Component elements are Not found</li>");

							}
				} catch (Exception e) {
					sb.append("<li>Unable to update hero large component." + e
							+ "</li>");
				}

				// end set Hero Large component's title, description, link text,
				// linkurl.
				// ---------------------------------------------------------------------------------------------------------------------------------------
				// start set selectorbar large component properties.

				try {
					String h2Text = "";
					String titleURL = "";
					String aText = "";
					String aHref = "";
					Elements selectorBarLargeElements = doc
							.select("div.selectorbarpanel");

					// selectorBarLargeElements =
					// selectorBarLargeElements.select("div.title");
					// Elements subElements =
					// selectorBarLargeElements.select("div.menu");
					log.debug("selector component found indexMidLeftNode: "
							+ indexMidLeftNode.getPath());
					Node selectorBarNode = indexMidLeftNode
							.hasNode("selectorbarlarge") ? indexMidLeftNode
									.getNode("selectorbarlarge") : null;

									if (selectorBarNode != null) {
										int eleSize = selectorBarLargeElements.size();
										log.debug("selector component found ele size: " + eleSize);
										NodeIterator selectorBarPanel = selectorBarNode
												.getNodes("selectorbarpanel*");
										int nodeSize = (int) selectorBarPanel.getSize();
										log.debug("selector component found nodeSize : " + nodeSize);

										if (eleSize == nodeSize) {

											for (Element ele : selectorBarLargeElements) {
												selectorBarPanel.hasNext();
												Node selectorBarPanelNode = (Node) selectorBarPanel
														.next();
												// Element titleEle =
												// selectorBarLargeElements.first();
												Elements h2TagText = ele.getElementsByTag("h2");
												if (h2TagText != null) {
													h2Text = h2TagText.html();
												} else {
													sb.append("<li>Selector Bar Large  Component title element not having any title in it ('h2' is blank)</li>");
												}

												Element titleUrl = ele.getElementsByTag("h2")
														.select("a").first();
												if (titleUrl != null) {
													titleURL = titleUrl.absUrl("href");
													if(StringUtil.isBlank(titleURL)){
														titleURL = titleUrl.attr("href");
													}
													// Start extracting valid href
													log.debug("Before selectorbartitleLinkUrl" + titleURL + "\n");
													titleURL = FrameworkUtils.getLocaleReference(titleURL, urlMap, locale, sb);
													log.debug("after selectorbartitleLinkUrl" + titleURL + "\n");
													// End extracting valid href
												} else {
													sb.append("<li>Selector Bar Component Title URL element not having any content in it ('a href' is blank)</li>");
												}
												Element menuEle = ele.child(1);
												// Element anchor =
												Element allLinkTag = menuEle.getElementsByTag("a")
														.last();
												if (allLinkTag != null) {
													aText = allLinkTag.text();
													aHref = allLinkTag.absUrl("href");
													if(StringUtil.isBlank(aHref)){
														aHref = allLinkTag.attr("href");
													}
													// Start extracting valid href
													log.debug("Before selectorbarmenuLinkUrl" + aHref + "\n");
													aHref = FrameworkUtils.getLocaleReference(aHref, urlMap, locale, sb);
													log.debug("after selectorbarmenuLinkUrl" + aHref + "\n");
													// End extracting valid href
												} else {
													sb.append("<li>Selector Bar Component all link url href tag not having any content in it ('<a>' is blank)</li>");
												}

												log.debug("selector component titleUrl: "
														+ titleURL);
												selectorBarPanelNode.setProperty("title", h2Text);
												selectorBarPanelNode.setProperty("titleurl",
														titleURL);
												selectorBarPanelNode.setProperty("alllinktext",
														aText);
												selectorBarPanelNode.setProperty("alllinkurl",
														aHref);

												Elements menuUlList = menuEle
														.getElementsByTag("ul");
												for (Element element : menuUlList) {
													java.util.List<String> list = new ArrayList<String>();
													Elements menuLiList = element
															.getElementsByTag("li");

													if (menuLiList != null) {
														for (Element li : menuLiList) {
															JSONObject jsonObj = new JSONObject();
															Element listItemAnchor = li
																	.getElementsByTag("a").first();

															if (listItemAnchor != null) {
																String anchorText = listItemAnchor
																		.text();
																String anchorHref = listItemAnchor
																		.absUrl("href");
																if(StringUtil.isBlank(anchorHref)){
																	anchorHref = listItemAnchor.absUrl("href");
																}
																// Start extracting valid href
																log.debug("Before selectorbarLinkUrl" + anchorHref + "\n");
																anchorHref = FrameworkUtils.getLocaleReference(anchorHref, urlMap, locale, sb);
																log.debug("after selectorbarLinkUrl" + anchorHref + "\n");
																// End extracting valid href
																jsonObj.put("linktext", anchorText);
																jsonObj.put("linkurl", anchorHref);
																list.add(jsonObj.toString());
															} else {
																sb.append("<li>Selector Bar Component drop down not having links and corresponding urls in it ('<li>' s are blank)</li>");
															}

														}
														selectorBarPanelNode.setProperty(
																"panelitems", list
																.toArray(new String[list
																                    .size()]));
													} else {
														sb.append("<li>Selector Bar Component dropdown doesnt have any content in it ('<ul>' is blank)</li>");
													}
												}
											}

										}
									}

				} catch (Exception e) {
					sb.append("<li>Unable to update Selector bar large component."
							+ e + "</li>");
				}

				// end set Selectot bar.
				// ----------------------------------------------------------------------------------
				// start set text component.
				try {
					Elements textElements = doc.select("div.c00-pilot");
					if (textElements != null) {
						Node textNode = indexMidLeftNode.hasNode("text") ? indexMidLeftNode

								.getNode("text")
								: null;
								if (textNode != null) {
									for (Element ele : textElements) {
										if (ele != null) {
											String textProp = ele.html();
											log.debug("text property!: " + textProp);
											textNode.setProperty("text", textProp);
										} else {
											sb.append("<li>Unable to update text component as there are no elements in the class c00-pilot.</li>");
										}

									}
								}
					} else {
						sb.append("<li>Unable to update text component as its respective div is missing. c00-pilot class is missing.</li>");
					}

				} catch (Exception e) {
					sb.append("<li>Unable to update index primary cta component component."
							+ e + "</li>");
				}

				// end set text
				// ---------------------------------------------------------------------------------------------------------------------------------------
				// start set spotlight component.
				try {
					String h2Text = "";
					String pText = "";
					String aText = "";
					Elements spotLightElements = doc.select("div.c11-pilot");
					// Node spotLightNode =
					// indexMidLeftNode.hasNode("spotlight_large*") ?
					// indexMidLeftNode
					// .getNode("spotlight_large*") : null;
					if (spotLightElements != null) {
						if (indexMidLeftNode != null) {
							int eleSize = spotLightElements.size();
							NodeIterator spoLightNodeIterator = indexMidLeftNode
									.getNodes("spotlight_large*");
							// NodeIterator spoLightNodeIterator =
							// spotLightNode.getNodes();
							int nodeSize = (int) spoLightNodeIterator.getSize();
							int imageSrcEmptyCount = 0;
							if (eleSize == nodeSize) {
								for (Element ele : spotLightElements) {
									spoLightNodeIterator.hasNext();
									Node spotLightComponentNode = (Node) spoLightNodeIterator
											.next();

									Elements h2TagText = ele.getElementsByTag("h2");
									if (h2TagText != null) {
										h2Text = h2TagText.html();
									} else {
										sb.append("<li>Spotlight Component Heading element not having any title in it ('h2' is blank)</li>");
									}

									Elements descriptionText = ele
											.getElementsByTag("p");
									if (descriptionText != null) {
										pText = descriptionText.html();
									} else {
										sb.append("<li>Spotlight Component description element not having any title in it ('p' is blank)</li>");
									}

									Elements anchorText = ele.getElementsByTag("a");
									if (anchorText != null) {
										aText = anchorText.text();
									} else {
										sb.append("<li>Spotlight Component anchor tag not having any content in it ('<a>' is blank)</li>");
									}
									
									// start image
									String spotLightImage = FrameworkUtils.extractImagePath(ele, sb);
									log.debug("spotLightImage befor migration : " + spotLightImage + "\n");
									if (spotLightComponentNode != null) {
										if (spotLightComponentNode.hasNode("image")) {
											Node spotLightImageNode = spotLightComponentNode.getNode("image");
											String fileReference = spotLightImageNode.hasProperty("fileReference")?spotLightImageNode.getProperty("fileReference").getString():"";
											spotLightImage = FrameworkUtils.migrateDAMContent(spotLightImage, fileReference, locale,sb);
											log.debug("spotLightImage after migration : " + spotLightImage + "\n");
											if (StringUtils.isNotBlank(spotLightImage)) {
												spotLightImageNode.setProperty("fileReference" , spotLightImage);
											}else{
												imageSrcEmptyCount++;
											}
										} else {
											sb.append("<li>spotlight image node doesn't exist</li>");
										}
									}
									// end image
									
									spotLightComponentNode.setProperty("title",
											h2Text);
									spotLightComponentNode.setProperty(
											"description", pText);
									spotLightComponentNode.setProperty("linktext",
											aText);

								}
								if(imageSrcEmptyCount > 0){
									sb.append("<li> "+imageSrcEmptyCount+" image(s) are not found on spot light component of locale page. </li>");
								}
							}

							if (nodeSize < eleSize) {
								int nodeCount = 1;

								for (Element ele : spotLightElements) {
									if (nodeCount <= nodeSize) {
										spoLightNodeIterator.hasNext();
										Node spotLightComponentNode = (Node) spoLightNodeIterator
												.next();
										Elements h2TagText = ele
												.getElementsByTag("h2");
										if (h2TagText != null) {
											h2Text = h2TagText.html();
										} else {
											sb.append("<li>Spotlight Component Heading element not having any title in it ('h2' is blank)</li>");
										}

										Elements descriptionText = ele
												.getElementsByTag("p");
										if (descriptionText != null) {
											pText = descriptionText.html();
										} else {
											sb.append("<li>Spotlight Component description element not having any title in it ('p' is blank)</li>");
										}

										Elements anchorText = ele
												.getElementsByTag("a");
										if (anchorText != null) {
											aText = anchorText.text();
										} else {
											sb.append("<li>Spotlight Component anchor tag not having any content in it ('<a>' is blank)</li>");
										}
										
										
										// start image
										String spotLightImage = FrameworkUtils.extractImagePath(ele, sb);
										log.debug("spotLightImage befor migration : " + spotLightImage + "\n");
										if (spotLightComponentNode != null) {
											if (spotLightComponentNode.hasNode("image")) {
												Node spotLightImageNode = spotLightComponentNode.getNode("image");
												String fileReference = spotLightImageNode.hasProperty("fileReference")?spotLightImageNode.getProperty("fileReference").getString():"";
												spotLightImage = FrameworkUtils.migrateDAMContent(spotLightImage, fileReference, locale,sb);
												log.debug("spotLightImage after migration : " + spotLightImage + "\n");
												if (StringUtils.isNotBlank(spotLightImage)) {
													spotLightImageNode.setProperty("fileReference" , spotLightImage);
												}else{
													imageSrcEmptyCount++;
												}
											} else {
												sb.append("<li>spotlight image node doesn't exist</li>");
											}
										}
										// end image
										
										spotLightComponentNode.setProperty("title",
												h2Text);
										spotLightComponentNode.setProperty(
												"description", pText);
										spotLightComponentNode.setProperty(
												"linktext", aText);
										nodeCount++;

									} else {
										sb.append("<li>Unable to migrate one spotlight component. Count MisMatch.</li>");
										log.debug("Could not migrate one spotlight large node.");
									}
								}if(imageSrcEmptyCount > 0){
									sb.append("<li> "+imageSrcEmptyCount+" image(s) are not found on spot light component of locale page. </li>");
								}

							}
						}

					} else {
						sb.append("<li>Unable to update spotlight component as its respective div is not available.</li>");

					}
				} catch (Exception e) {
					sb.append("<li>Unable to update spotlight component." + e
							+ "</li>");
				}
				// end set spotlight nodes
				// ---------------------------------------------------------------------------------------------------------------------------------------
				// start set tilebordered component.
				try {

					boolean flag = false;
					String h2Text = "";
					String pText = "";
					String aText = "";
					String aHref = "";
					int count = 0;
					Elements tileBorderedElements = doc.select("div.c23-pilot");
					// Node spotLightNode =
					// indexMidLeftNode.hasNode("spotlight_large*") ?
					// indexMidLeftNode
					// .getNode("spotlight_large*") : null;

					if (indexRightRailNode != null) {
						log.debug("indexRightRailNode found: "
								+ indexRightRailNode.getPath());
						int eleSize = tileBorderedElements.size();
						NodeIterator tileBorderedNodeIterator = indexRightRailNode
								.getNodes("tile_bordered*");
						// NodeIterator spoLightNodeIterator =
						// spotLightNode.getNodes();
						int nodeSize = (int) tileBorderedNodeIterator.getSize();
						if (eleSize == nodeSize) {
							for (Element ele : tileBorderedElements) {
								tileBorderedNodeIterator.hasNext();
								Node spotLightComponentNode = (Node) tileBorderedNodeIterator
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

								Element anchorText = ele.getElementsByTag("a").first();
								if (anchorText != null) {
									aText = anchorText.text();
									aHref = anchorText.absUrl("href");
									if(StringUtil.isBlank(aHref)){
										aHref = anchorText.attr("href");
									}
									// Start extracting valid href
									log.debug("Before tileborderedLinkUrl" + aHref + "\n");
									aHref = FrameworkUtils.getLocaleReference(aHref, urlMap, locale, sb);
									log.debug("after tileborderedLinkUrl" + aHref + "\n");
									// End extracting valid href
								} else {
									sb.append("<li>TileBordered Component anchor tag not having any content in it ('<a>' is blank)</li>");
								}

								spotLightComponentNode.setProperty("title", h2Text);
								spotLightComponentNode.setProperty("description",
										pText);
								spotLightComponentNode.setProperty("linktext",
										aText);
								spotLightComponentNode
								.setProperty("linkurl", aHref);
								String textAppended = ele.ownText();
								if(StringUtils.isNotBlank(textAppended)){
									flag = true;
									count++;
								}
							}
						} else {

							sb.append("<li>Could not migrate  tilebordered node. Count mis match</li>");
							log.debug("Could not migrate  tilebordered node. Count mis match");
						}

					}
					if(flag){
						sb.append("<li>Extra Text found after link on locale page for "+ count +" TileBordered Component(s) , hence the text cannot be migrated.</li>");
					}

				} catch (Exception e) {
					sb.append("<li>Unable to update spotlight component." + e
							+ "</li>");
				}

				session.save();
			}
			else {
				sb.append(Constants.URL_CONNECTION_EXCEPTION);
			}
		} catch (Exception e) {
			sb.append("<li>Exception as URL cannot be connected! </li>");
			log.debug("Exception as url cannot be connected: "+ e);
		}

		sb.append("</ul></td>");

		return sb.toString();
	}
}
