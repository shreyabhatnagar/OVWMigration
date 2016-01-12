package com.cisco.dse.global.migration.productlanding;

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

public class ProductLandingVariation11 extends BaseAction {

	Document doc;

	String title = null;

	StringBuilder sb = new StringBuilder(1024);

	String footerLinks = "{\"linktext\":\"<aaa>\",\"linkurl\":\"<bbb>\"}";

	static Logger log = Logger.getLogger(ProductLandingVariation11.class);

	public String translate(String host, String loc, String prod, String type,
			String catType, String locale, Session session,Map<String,String> urlMap) throws IOException,
			ValueFormatException, VersionException, LockException,
			ConstraintViolationException, RepositoryException {

		BasicConfigurator.configure();
		log.debug("In the translate method");
		log.debug("In the translate method, catType is :" + catType);
		String pagePropertiesPath = "/content/<locale>/"+catType+"/<prod>/index/jcr:content";
		String indexLeft = "/content/<locale>/"
				+ catType
				+ "/<prod>/index/jcr:content/content_parsys/overview/layout-overview/gd12v2/gd12v2-left";

		String indexRightRail = "/content/<locale>/"
				+ catType
				+ "/<prod>/index/jcr:content/content_parsys/overview/layout-overview/gd12v2/gd12v2-right";

		String pageUrl = host + "/content/<locale>/"
				+ catType + "/<prod>/index.html";

		pageUrl = pageUrl.replace("<locale>", locale).replace("<prod>", prod);
		pagePropertiesPath = pagePropertiesPath.replace("<locale>", locale).replace("<prod>", prod);
		indexLeft = indexLeft.replace("<locale>", locale).replace("<prod>",
				prod);
		indexRightRail = indexRightRail.replace("<locale>", locale).replace(
				"<prod>", prod);
		sb.append("<td>" + "<a href=" + pageUrl + ">" + pageUrl + "</a>"
				+ "</td>");
		sb.append("<td>" + "<a href=" + loc + ">" + loc + "</a>" + "</td>");
		sb.append("<td><ul>");

		javax.jcr.Node indexLeftNode = null;
		javax.jcr.Node indexRightRailNode = null;
		javax.jcr.Node pageJcrNode = null;
		try {

			indexLeftNode = session.getNode(indexLeft);
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
				// start set hero large component properties.

				try {
					String h2Text = "";
					String pText = "";
					String aText = "";
					String aHref = "";
					Value[] panelPropertiest = null;
					Elements heroElements = doc.select("div.c50-pilot");
					heroElements = heroElements.select("div.frame");
					Node heroNode = indexLeftNode.hasNode("hero_large") ? indexLeftNode.getNode("hero_large") : null;
					Property panelNodesProperty = heroNode.hasProperty("panelNodes")?heroNode.getProperty("panelNodes"):null;
					if(panelNodesProperty.isMultiple()){
						panelPropertiest = panelNodesProperty.getValues();
					}

							if (heroNode != null) {
								log.debug("heronode found: " + heroNode.getPath());
								if (heroElements != null) {
									int eleSize = heroElements.size();

									NodeIterator heroPanelNodeIterator = heroNode
											.getNodes("heropanel*");
									int nodeSize = (int) heroPanelNodeIterator.getSize();
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

										Elements descriptionText = ele
												.getElementsByTag("p");
										if (descriptionText != null) {
											pText = descriptionText.first().text();
										} else {
											sb.append("<li>Hero Component description element not having any title in it ('p' is blank)</li>");
										}

										Elements anchorText = ele.getElementsByTag("a");
										if (anchorText != null) {
											aText = anchorText.text();
											aHref = anchorText.attr("href");
											// Start extracting valid href
											log.debug("Before heropanelLinkUrl" + aHref + "\n");
											aHref = FrameworkUtils.getLocaleReference(aHref, urlMap);
											log.debug("after heropanelLinkUrl" + aHref + "\n");
											// End extracting valid href
										} else {
											sb.append("<li>Hero Component anchor tag not having any content in it ('<a>' is blank)</li>");
										}
										// start image
										String heroImage = FrameworkUtils.extractImagePath(ele, sb);
										log.debug("heroImage " + heroImage + "\n");
										if (heroPanelNode != null) {
											if (heroPanelNode.hasNode("image")) {
												Node imageNode = heroPanelNode.getNode("image");
												String fileReference = imageNode.hasProperty("fileReference")?imageNode.getProperty("fileReference").getString():"";
												heroImage = FrameworkUtils.migrateDAMContent(heroImage, fileReference, locale,sb);
												log.debug("heroImage " + heroImage + "\n");
												if (StringUtils.isNotBlank(heroImage)) {
													imageNode.setProperty("fileReference" , heroImage);
												}else{
													imageSrcEmptyCount++;
												}
											} else {
												sb.append("<li>hero image node doesn't exist</li>");
											}
											
											Node heroPanelPopUpNode = null;
											Elements lightBoxElements = ele.select("div.c50-image").select("a.c26v4-lightbox");
											if(lightBoxElements != null && !lightBoxElements.isEmpty()){
												Element lightBoxElement = lightBoxElements.first();
												heroPanelPopUpNode = FrameworkUtils.getHeroPopUpNode(heroPanelNode);
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
										
									}
									if(imageSrcEmptyCount > 0){
										sb.append("<li> " +imageSrcEmptyCount+ "image(s) are not found on locale page's hero element. </li>");
									}
									if (nodeSize != eleSize) {
										sb.append("<li>Unable to Migrate Hero component. Element Count is "
												+ eleSize
												+ " and Node count is "
												+ nodeSize + ". Count mismatch.</li>");

									}
								}

							} else {
								sb.append("<li>Hero Component not found on page. </li>");

							}

				} catch (Exception e) {
					sb.append("<li>Unable to update hero large component." + e
							+ "</li>");
				}


				try{
					Elements selectorBarElements = doc.select("div.c58v3-pilot");
					Element selectorBarElem = selectorBarElements.first();
					if(selectorBarElem != null){
						sb.append("<li>Extra selectorbar element found on locale page. Cannot be migrated.</li>");
					}

				}catch(Exception e){

				}

				// end set Hero Large component's title, description, link
				// text,linkurl.
				// ---------------------------------------------------------------------------------------------------------------------------------------
				// start set text component.
				try {
					Elements textElements = doc.select("div.c00-pilot");
					if (textElements != null) {
						Node textNode = indexLeftNode.hasNode("text") ? indexLeftNode
								.getNode("text") : null;
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
					if (spotLightElements != null) {
						if (indexLeftNode != null) {
							int eleSize = spotLightElements.size();
							NodeIterator spoLightNodeIterator = indexLeftNode
									.getNodes("spotlight_large*");
							// NodeIterator spoLightNodeIterator =
							// spotLightNode.getNodes();
							int nodeSize = (int) spoLightNodeIterator.getSize();
							int imageSrcEmptyCount = 0;
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
								log.debug("spotLightImage " + spotLightImage + "\n");
								if (spotLightComponentNode != null) {
									if (spotLightComponentNode.hasNode("image")) {
										Node spotLightImageNode = spotLightComponentNode.getNode("image");
										String fileReference = spotLightImageNode.hasProperty("fileReference")?spotLightImageNode.getProperty("fileReference").getString():"";
										spotLightImage = FrameworkUtils.migrateDAMContent(spotLightImage, fileReference, locale,sb);
										log.debug("spotLightImage " + spotLightImage + "\n");
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
								spotLightComponentNode.setProperty("title", h2Text);
								spotLightComponentNode.setProperty("description",
										pText);
								spotLightComponentNode.setProperty("linktext",
										aText);
							}
							if(imageSrcEmptyCount > 0){
								sb.append("<li> "+imageSrcEmptyCount+" image(s) are not found on spot light component of locale page. </li>");
							}

							if (nodeSize != eleSize) {
								sb.append("<li>Unable to Migrate spotlight component. Element Count is "
										+ eleSize
										+ " and Node count "
										+ nodeSize
										+ ". Count mismatch.</li>");

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

					String h2Text = "";
					String pText = "";
					String aText = "";
					String aHref = "";

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

							Elements descriptionText = ele.getElementsByTag("p");
							if (descriptionText != null) {
								pText = descriptionText.html();
							} else {
								sb.append("<li>TileBordered Component description element not having any title in it ('p' is blank)</li>");
							}

							Elements anchorText = ele.getElementsByTag("a");
							if (anchorText != null) {
								aText = anchorText.text();
								aHref = anchorText.attr("href");
								// Start extracting valid href
								log.debug("Before tileborderedLinkUrl" + aHref + "\n");
								aHref = FrameworkUtils.getLocaleReference(aHref, urlMap);
								log.debug("after tileborderedLinkUrl" + aHref + "\n");
								// End extracting valid href
							} else {
								sb.append("<li>TileBordered Component anchor tag not having any content in it ('<a>' is blank)</li>");
							}

							spotLightComponentNode.setProperty("title", h2Text);
							spotLightComponentNode
							.setProperty("description", pText);
							spotLightComponentNode.setProperty("linktext", aText);
							spotLightComponentNode.setProperty("linkurl", aHref);

						}
						if (eleSize != nodeSize) {

							sb.append("<li>Could not migrate  tilebordered node. Count mis match as Element Count is "
									+ eleSize
									+ " and node count is "
									+ nodeSize
									+ " </li>");
							log.debug("Could not migrate  tilebordered node. Count mis match");

						}
					}

				} catch (Exception e) {
					sb.append("<li>Unable to update spotlight component." + e
							+ "</li>");
				}

				// start set ENTERPRISE NETWORK INDEX list.
				try {
					NodeIterator listNodeIterator = indexRightRailNode.getNodes("list*");
					Elements indexlistElem = doc.select("div.n13-pilot");
					Element rightRailPilotElement = indexlistElem
							.first();
					if (rightRailPilotElement != null) {
						for (Element indexListItem : indexlistElem) {
							String indexTitle = indexListItem
									.getElementsByTag("h2").text();

							Elements indexUlList = indexListItem
									.getElementsByTag("ul");

							Elements listDescription = indexListItem.select(
									"div.intro").select("p");

							for (Element ele : indexUlList) {
								java.util.List<String> list = new ArrayList<String>();
								Elements indexLiList = ele.getElementsByTag("li");

								for (Element li : indexLiList) {
									JSONObject jsonObj = new JSONObject();
									Elements listItemAnchor = li
											.getElementsByTag("a");
									Elements listItemSpan = li
											.getElementsByTag("span");

									String anchorText = listItemAnchor != null ? listItemAnchor
											.text() : "";
											String anchorHref = listItemAnchor.attr("href");
											// Start extracting valid href
											log.debug("Before listItemLinkUrl" + anchorHref + "\n");
											anchorHref = FrameworkUtils.getLocaleReference(anchorHref, urlMap);
											log.debug("after listItemLinkUrl" + anchorHref + "\n");
											// End extracting valid href
											
											String anchorTarget = listItemAnchor
													.attr("target");
											String listIcon = listItemSpan.attr("class");
											//String icon = li.ownText();

											jsonObj.put("linktext", anchorText);
											jsonObj.put("linkurl", anchorHref);
											jsonObj.put("icon", listIcon);
											jsonObj.put("size", "");// Need to get the size
											// from the list element
											// text.
											jsonObj.put("description", "");// Need to get
											// the
											// description
											// from the list
											// element text.
											if (StringUtils.isNotBlank(anchorTarget)) {
												jsonObj.put("openInNewWindow", true);
											}
											list.add(jsonObj.toString());

								}
								log.debug("div class 'div.n13-pilot' not found in dom  list ::"
										+ list.toString());

									Node listNode = null;
									NodeIterator elementList = null;

									if(listNodeIterator.hasNext()){
									listNode = (Node) listNodeIterator.next();
									log.debug("path of list node: "+ listNode.getPath());
								
								if (listNode != null) {
									listNode.setProperty("title", indexTitle);
									if (listNode.hasNode("intro")) {
										Node introNode = listNode.getNode("intro");
										String descProperty = "";
										if (listDescription != null) {
											descProperty = listDescription.html();
											introNode.setProperty("paragraph_rte",
													descProperty.trim());
											log.debug("Updated descriptions at "
													+ introNode.getPath());

										} else {
											sb.append("<li>Paragraph description is not migrated as it has no content.</li>");
										}
									}
								
								elementList = listNode.getNodes("element_list*");
								if(elementList !=null && elementList.hasNext()){
									Node eleNode = (Node) elementList.next();
									if (eleNode != null) {

										if(eleNode.hasProperty("listitems")){
											Property listitems = eleNode
													.getProperty("listitems");
											if (!listitems.isMultiple()) {
												listitems.remove();
												session.save();
											}
										}
										eleNode.setProperty("listitems", list
												.toArray(new String[list.size()]));
										log.debug("Updated listitems at "
												+ eleNode.getPath());
									}
									} else {
										sb.append("<li>element_list node doesn't exists</li>");
									}
								
								} if (elementList !=null && elementList.getSize() != indexUlList.size()) {
									sb.append("<li>Mis-Match in Resource list Panels count/content."
											+ indexUlList.size()
											+ "not equal to "
											+ elementList.getSize() + "</li>");
								
								}
									}
							}

						}
						
					}else {
						sb.append("<li>Mismatch in the right rail. List element is not found.</li>");
					}
				} catch (Exception e) {
					sb.append("<li>Unable to update index list component.\n</li>");
					log.error("Exception : ", e);
				}
				// end set benefit list.
				// --------------------------------------------------------------------------------------------------------------------------
				// start of follow us component

				try {
					String h2Content = "";
					List<String> list = new ArrayList<String>();
					Elements rightRailPilotElements = doc.select("div.s14-pilot");
					if (rightRailPilotElements != null) {
						Element rightRailPilotElement = rightRailPilotElements
								.first();
						if (rightRailPilotElement != null) {
							Elements h2Elements = rightRailPilotElement
									.getElementsByTag("h2");
							if (h2Elements != null) {
								Element h2Element = h2Elements.first();
								h2Content = h2Element.text();
							} else {
								sb.append("<li>h2 of right rail with class 'div.s14-pilot' is blank.</li>");
							}
							Elements liElements = rightRailPilotElement
									.getElementsByTag("li");
							for (Element ele : liElements) {
								JSONObject obj = new JSONObject();
								String icon = ele.attr("class");
								obj.put("icon", icon);
								Elements aElements = ele.getElementsByTag("a");
								if (aElements != null) {
									Element aElement = aElements.first();
									String title = aElement.attr("title");
									String href = aElement.attr("href");
									// Start extracting valid href
									log.debug("Before followusLinkUrl" + href + "\n");
									href = FrameworkUtils.getLocaleReference(href, urlMap);
									log.debug("after followusLinkUrl" + href + "\n");
									// End extracting valid href
									obj.put("linktext", title);
									obj.put("linkurl", href);
								} else {
									sb.append("<li>No anchor tag found in the right rail social links</li>");
								}
								list.add(obj.toString());
							}
						} else {
							sb.append("<li>right rail does not have followus element.</li>");
						}
					} else {
						sb.append("<li>No pilot found on right rail with class 'div.s14-pilot'</li>");
					}

					if (indexRightRailNode.hasNode("followus")) {
						Node followus = indexRightRailNode.getNode("followus");
						if (StringUtils.isNotBlank(h2Content)) {
							followus.setProperty("title", h2Content);
						} else {
							sb.append("<li>No title found at right rail social media piolot.</li>");
						}

						if (list.size() > 1) {
							followus.setProperty("links",
									list.toArray(new String[list.size()]));
						}

					} else {
						sb.append("<li>No 'followus' node found under "
								+ indexRightRailNode.getPath() + "</li>");
					}
				} catch (Exception e) {

					sb.append("<li>Unable to update followus component. Exception found is:  "
							+ e + "</li>");
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
