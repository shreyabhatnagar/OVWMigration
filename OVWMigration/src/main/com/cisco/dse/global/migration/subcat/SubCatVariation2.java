package com.cisco.dse.global.migration.subcat;

/* 
 * S.No     	Name                 Date                    Description of change
 *  #1         Saroja            21-Dec-15           Added the Java file to handle the migration of product listing responsive page(s).
 * 
 * */

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

public class SubCatVariation2 extends BaseAction{
	
	Document doc;


	StringBuilder sb = new StringBuilder(1024);

	String footerLinks = "{\"linktext\":\"<aaa>\",\"linkurl\":\"<bbb>\"}";

	static Logger log = Logger.getLogger(SubCatVariation2.class);

	public String translate(String host,String loc, String prod, String type,
			String catType, String locale, Session session, Map<String, String> urlMap) throws IOException,
			ValueFormatException, VersionException, LockException,
			ConstraintViolationException, RepositoryException {
		BasicConfigurator.configure();
		log.debug("In the translate method");
		log.debug("In the translate method, catType is :" + catType);

		// Repo node paths
		String pagePropertiesPath = "/content/<locale>/"+catType+"/<prod>/network-infrastructure/index/jcr:content";
		String topLeftGridNodePath = "/content/<locale>/"
				+ catType
				+ "/<prod>/network-infrastructure/index/jcr:content/content_parsys/solutions/layout-solutions/gd22v2/gd22v2-left";
		String topRightGridNodePath = "/content/<locale>/"
				+ catType
				+ "/<prod>/network-infrastructure/index/jcr:content/content_parsys/solutions/layout-solutions/gd22v2/gd22v2-right";
		String midLeftGridNodePath = "/content/<locale>/"
				+ catType
				+ "/<prod>/network-infrastructure/index/jcr:content/content_parsys/solutions/layout-solutions/gd21v1/gd21v1-mid";
		String bottomLeftGridNodePath = "/content/<locale>/"
				+ catType
				+ "/<prod>/network-infrastructure/index/jcr:content/content_parsys/solutions/layout-solutions/gd22v2_0/gd22v2-left";
		
		String bottomRightGridNodePath = "/content/<locale>/"
				+ catType
				+ "/<prod>/network-infrastructure/index/jcr:content/content_parsys/solutions/layout-solutions/gd22v2_0/gd22v2-right";
		
		String bottomMostLeftGridNodePath = "/content/<locale>/"
				+ catType
				+ "/<prod>/network-infrastructure/index/jcr:content/content_parsys/solutions/layout-solutions/gd21v1_1/gd21v1-mid";
		
		String pageUrl = host + "/content/<locale>/"
				+ catType + "/<prod>/network-infrastructure/index.html";

		pageUrl = pageUrl.replace("<locale>", locale).replace("<prod>", prod);
		pagePropertiesPath = pagePropertiesPath.replace("<locale>", locale).replace("<prod>", prod);
		sb.append("<td>" + "<a href=" + pageUrl + ">" + pageUrl + "</a>"
				+ "</td>");
		sb.append("<td>" + "<a href=" + loc + ">" + loc + "</a>" + "</td>");
		sb.append("<td><ul>");

		topLeftGridNodePath = topLeftGridNodePath.replace("<locale>", locale).replace(
				"<prod>", prod);
		topRightGridNodePath = topRightGridNodePath.replace("<locale>", locale).replace(
				"<prod>", prod);
		midLeftGridNodePath = midLeftGridNodePath.replace("<locale>", locale).replace(
				"<prod>", prod);
		bottomLeftGridNodePath =bottomLeftGridNodePath.replace("<locale>", locale).replace(
				"<prod>", prod);
		bottomRightGridNodePath =bottomRightGridNodePath.replace("<locale>", locale).replace(
				"<prod>", prod);
		
		bottomMostLeftGridNodePath =bottomMostLeftGridNodePath.replace("<locale>", locale).replace(
				"<prod>", prod);
		Node topLeftGridNode = null;
		Node topRightGridNode = null;
		Node midLeftGridNode = null;
		Node bottomLeftGridNode = null;
		Node bottomRightGridNode = null;
		Node bottomMostLeftGridNode = null;
		Node pageJcrNode = null;
		try {
			topLeftGridNode = session.getNode(topLeftGridNodePath);
			topRightGridNode = session.getNode(topRightGridNodePath);
			midLeftGridNode = session.getNode(midLeftGridNodePath);
			bottomLeftGridNode = session.getNode(bottomLeftGridNodePath);
			bottomRightGridNode = session.getNode(bottomRightGridNodePath);
			bottomMostLeftGridNode = session.getNode(bottomMostLeftGridNodePath);
			pageJcrNode = session.getNode(pagePropertiesPath);
			try {
				doc = Jsoup.connect(loc).get();
				log.debug("Connected to the provided URL");
			} catch (Exception e) {
				doc = getConnection(loc);
			}

			if(doc != null){

				// ------------------------------------------------------------------------------------------------------------------------------------------
				// start set page properties.

				FrameworkUtils.setPageProperties(pageJcrNode, doc, session, sb);

				// end set page properties.
				// ------------------------------------------------------------------------------------------------------------------------------------------
				// start set hero medium component.
				try {
					

					Elements heroElements = doc.select("div.c50-pilot");
					heroElements = heroElements.select("div.frame");
					Node heroNode = topLeftGridNode.hasNode("hero_medium") ? topLeftGridNode
							.getNode("hero_medium") : null;

					if (heroNode != null) {
						log.debug("heronode found: " + heroNode.getPath());
						if (heroElements != null) {
							int eleSize = heroElements.size();
							Value[] panelPropertiest = null;
							NodeIterator heroPanelNodeIterator = heroNode.getNodes("heropanel*");
							Property panelNodesProperty = heroNode.hasProperty("panelNodes")?heroNode.getProperty("panelNodes"):null;
							if(panelNodesProperty.isMultiple()){
								panelPropertiest = panelNodesProperty.getValues();
							}
							int nodeSize = (int) heroPanelNodeIterator.getSize();
							int i=0;
							for (Element ele : heroElements) {
								String h2Text = "";
								String pText = "";
								String aText = "";
								String aHref = "";
								Elements h2TagText = ele.getElementsByTag("h2");
								if (h2TagText != null) {
									h2Text = h2TagText.html();
								} else {
									sb.append(Constants.HERO_CONTENT_HEADING_ELEMENT_DOESNOT_EXISTS);
								}

								Elements descriptionText = ele
										.getElementsByTag("p");
								if (descriptionText != null) {
									pText = descriptionText.first().text();
								} else {
									sb.append(Constants.HERO_CONTENT_DESCRIPTION_ELEMENT_DOESNOT_EXISTS);
								}

								Elements anchorText = ele.getElementsByTag("a");
								if (anchorText != null) {
									aText = anchorText.text();
									aHref = anchorText.attr("href");
									// Start extracting valid href
									log.debug("Before heroPanelLinkUrl" + aHref + "\n");
									aHref = FrameworkUtils.getLocaleReference(aHref, urlMap);
									log.debug("after heroPanelLinkUrl" + aHref + "\n");
									// End extracting valid href
								} else {
									sb.append(Constants.HERO_CONTENT_ANCHOR_ELEMENT_DOESNOT_EXISTS);
								}
								Node heroPanelNode = null;
								if (heroNode != null) {
									
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
								
								// start image
									String heroImage = FrameworkUtils
											.extractImagePath(ele, sb);
									log.debug("heroImage " + heroImage + "\n");
									if (heroPanelNode != null) {
										Node heroPanelPopUpNode = null;
										Elements lightBoxElements = ele.select("div.c50-image").select("a.c26v4-lightbox");
										if(lightBoxElements != null && !lightBoxElements.isEmpty()){
											//Element lightBoxElement = lightBoxElements.first();
											heroPanelPopUpNode = FrameworkUtils.getHeroPopUpNode(heroPanelNode);
										}
										if(heroPanelPopUpNode != null){
											heroPanelPopUpNode.setProperty("popupHeader", h2Text);
										}else{
											sb.append("<li>Hero content video pop up node not found.</li>");
										}
										if (heroPanelNode.hasNode("image")) {
											Node imageNode = heroPanelNode
													.getNode("image");
											String fileReference = imageNode
													.hasProperty("fileReference") ? imageNode
													.getProperty(
															"fileReference")
													.getString() : "";
											heroImage = FrameworkUtils
													.migrateDAMContent(
															heroImage,
															fileReference,
															locale,sb);
											log.debug("heroImage " + heroImage
													+ "\n");
											if (StringUtils
													.isNotBlank(heroImage)) {
												imageNode.setProperty(
														"fileReference",
														heroImage);
											}
										} else {
											sb.append(Constants.HERO_IMAGE_NOT_AVAILABLE);
										}
									}
									// end image
									heroPanelNode.setProperty("title", h2Text);
									heroPanelNode.setProperty("description", pText);
									heroPanelNode
											.setProperty("linktext", aText);
									heroPanelNode.setProperty("linkurl", aHref);
								}
						}
							if (nodeSize != eleSize) {
								sb.append("<li>Unable to Migrate Hero component. Element Count is "
										+ eleSize
										+ " and Node count is "
										+ nodeSize + ". Count mismatch.</li>");
							}
							}else {
							sb.append(Constants.HERO_CONTENT_PANEL_ELEMENT_NOT_FOUND);
						}
					} else {
						sb.append(Constants.HERO_CONTENT_NODE_NOT_FOUND);
					}
				} catch (Exception e) {
					sb.append(Constants.EXCEPTION_IN_HERO_MIGRATION
							);
					log.error("Exception in updating text component: ", e);
				}
				
				try{
					
					String titeText = "";
					String callText = "";
					Element contactUsElements = doc.select("div.f-holder").first();
					Node letUsHelpNode = topRightGridNode.hasNode("letushelp")?topRightGridNode.getNode("letushelp"):null;
					
					if(contactUsElements != null){
						Elements titleElem = contactUsElements.getElementsByTag("h3");
						Elements liElements = contactUsElements.getElementsByTag("li");
						if(!titleElem.isEmpty()){
							titeText = titleElem.text();
						}
						if(!liElements.isEmpty()){
							callText = liElements.first().outerHtml();
							
						}
						else{
							sb.append("<li> Contact Us Element title not found. </li>");
						}
						
					}else{
						sb.append("<li> Contact Us Element Not Found on locale page. </li>");
					} 
					if(letUsHelpNode != null){
						letUsHelpNode.setProperty("title",titeText);
						letUsHelpNode.setProperty("calltext",callText);
					}
					
					
					
				}catch(Exception e){
					
				}
				
				try{
					
					Node listNode = topRightGridNode
							.getNode("list");
					Elements topRightListElement = doc.select("div.n13v1-pilot");
					if(!topRightListElement.isEmpty()){
						//After text and 1st list section
						
						Element listElement = topRightListElement.first();
						if (listElement != null) {
									String indexTitle = listElement.getElementsByTag(
											"h2").text();

									Elements indexUlList = listElement
											.getElementsByTag("ul");

									Elements listDescription = listElement.select(
											"div.intro").select("p");

									for (Element ele : indexUlList) {
										java.util.List<String> list = new ArrayList<String>();
										Elements indexLiList = ele
												.getElementsByTag("li");

										for (Element li : indexLiList) {
											JSONObject jsonObj = new JSONObject();
											Elements listItemAnchor = li
													.getElementsByTag("a");
											Elements listItemSpan = li
													.getElementsByTag("span");

											String anchorText = listItemAnchor != null ? listItemAnchor
													.text() : "";
											String anchorHref = listItemAnchor
													.attr("href");
											// Start extracting valid href
											log.debug("Before ListLinkUrl" + anchorHref + "\n");
											anchorHref = FrameworkUtils.getLocaleReference(anchorHref, urlMap);
											log.debug("after ListLinkUrl" + anchorHref + "\n");
											// End extracting valid href
											String anchorTarget = listItemAnchor
													.attr("target");
											String listIcon = listItemSpan
													.attr("class");
											// String icon = li.ownText();

											jsonObj.put("linktext", anchorText);
											jsonObj.put("linkurl", anchorHref);
											jsonObj.put("icon", listIcon);
											jsonObj.put("size", "");// Need to get the
																	// size
											// from the list element
											// text.
											jsonObj.put("description", "");// Need to
																			// get
											// the
											// description
											// from the list
											// element text.
											if (StringUtils.isNotBlank(anchorTarget)) {
												jsonObj.put("openInNewWindow", true);
											}
											list.add(jsonObj.toString());

										}
										log.debug("div class 'div.n13-pilot' ul content ::"
												+ list.toString());

										NodeIterator elementList = null;
											if (listNode != null) {
												log.debug("path of list node: "
														+ listNode.getPath());
												listNode.setProperty("title",
														indexTitle);
												if (listNode.hasNode("intro")) {
													Node introNode = listNode.hasNode("intro")?listNode
															.getNode("intro"):null;
													String descProperty = "";
													if (listDescription != null && introNode != null) {
														descProperty = listDescription
																.html();
														introNode.setProperty(
																"paragraph_rte",
																descProperty.trim());
														log.debug("Updated descriptions at "
																+ introNode.getPath());

													} else {
														sb.append(Constants.LIST_INTRO_PARAGRAPH_ELEMENT_NOT_FOUND);
													}
												}

												elementList = listNode
														.getNodes("element_list*");
												if (elementList != null
														&& elementList.hasNext()) {
													Node eleNode = (Node) elementList
															.next();
													if (eleNode != null) {

														if (eleNode
																.hasProperty("listitems")) {
															Property listitems = eleNode
																	.getProperty("listitems");
															if (!listitems.isMultiple()) {
																listitems.remove();
																session.save();
															}
														}
														eleNode.setProperty(
																"listitems",
																list.toArray(new String[list
																		.size()]));
														log.debug("Updated listitems at "
																+ eleNode.getPath());
													}
												} else {
													sb.append(Constants.LIST_ELEMENT_LIST_NODE_NOT_FOUND);
												}

											}
											if (elementList != null
													&& elementList.getSize() != indexUlList
															.size()) {
												sb.append("<li>Could not migrate List node. Count mis match as Element Count is "
														+ indexUlList.size()
														+ " and node count is "
														+ (int) elementList.getSize() + " </li>");
											}
										}
									}
							else {
								sb.append(Constants.LIST_ELEMENT_NOT_FOUND);
							}
									
					}else {
						sb.append(Constants.LIST_ELEMENT_NOT_FOUND);
					}
						
				}catch(Exception e){
					sb.append(Constants.EXCEPTION_IN_UPDATING_LIST_COMPONENT);
					log.debug(Constants.EXCEPTION_IN_UPDATING_LIST_COMPONENT);
				}
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
								Element h2Element = h2Elements.first();
								h2Content = h2Element.text();
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
									sb.append(Constants.FOLLOW_US_ANCHOR_ELEMENT_NOT_FOUND);
								}
								list.add(obj.toString());
							}
						} else {
							sb.append(Constants.FOLLOWUS_ELEMENT_NOT_FOUND);
						}
					} 
					if (topRightGridNode.hasNode("followus")) {
						Node followus = topRightGridNode.getNode("followus");
						if (StringUtils.isNotBlank(h2Content)) {
							followus.setProperty("title", h2Content);
						} else {
							sb.append(Constants.FOLLOWUS_TITLE_NOT_FOUND);
						}

						if (list.size() > 1) {
							followus.setProperty("links",
									list.toArray(new String[list.size()]));
						}

					} else {
						sb.append(Constants.FOLLOWUS_NODE_NOT_FOUND);
					}
				} catch (Exception e) {

					sb.append(Constants.EXCEPTION_IN_FOLLOW_US_COMPONENT);
				}

				// ---------------------------------------------------------------------------------------------------------------------------------------
				// start of html blob components content.
				try {
					String html = "";
					StringBuilder htmlBlobContent = new StringBuilder();
					Elements htmlBlobElements = doc.select("div.gd22v2-right").select("div.htmlblob");
					if (htmlBlobElements != null) {
						for (Element htmlblobElement : htmlBlobElements) {
							if (htmlblobElement != null) {
								html = htmlblobElement.outerHtml();
								html = FrameworkUtils.extractHtmlBlobContent(
										htmlblobElement, "", locale, sb,urlMap);
								htmlBlobContent.append(html);
							} else {
								log.debug(Constants.HTMLBLOB_ELEMENT_NOT_FOUND);
							}
						}
					}

					if(bottomRightGridNode.hasNode("htmlblob")) {
							Node htmlBlobNode = bottomRightGridNode.hasNode("htmlblob")?
									bottomRightGridNode.getNode("htmlblob"):null;
						log.debug("htmlblobElement.outerHtml() " + html + "\n");
						if (StringUtils.isNotBlank(html)) {
							htmlBlobNode.setProperty("html",
									htmlBlobContent.toString());
						} else {
							sb.append(Constants.HTMLBLOB_CONTENT_DOES_NOT_EXIST);
						}

					} else {
						log.debug("htmlblob component not present at "
								+ bottomRightGridNode.getPath());
					}

				} catch (Exception e) {
					log.debug(Constants.EXCEPTION_IN_HTMLBLOB);
				}
				// end set html blob component content.
				// --------------------------------------------------------------------------------------------------------------------------

				// ---------------------------------------------------------------------------------------------------------------------------------------
				// start set text component.
				try {
					Elements textElements = doc.select("div.c00v0-pilot");
					if (!textElements.isEmpty()) {
						Node textNode = midLeftGridNode.hasNode("text") ? midLeftGridNode
								.getNode("text") : null;
						if (textNode != null) {
							Element ele = textElements.first();
								if (ele != null) {
									String textProp = ele.html();
									textProp = FrameworkUtils
											.extractHtmlBlobContent(ele, "",
													locale, sb,urlMap);
									log.debug("text property for first c00v0-pilottttt*******!: " + textProp);
									textNode.setProperty("text", textProp);
								} else {
									sb.append(Constants.TEXT_ELEMENT_NOT_FOUND);
								}

							}
					} else {
						sb.append(Constants.TEXT_ELEMENT_NOT_FOUND);
					}

				} catch (Exception e) {
					sb.append(Constants.EXCEPTION_TEXT_COMPONENT);
				}

				// end set text
				
				// ---------------------------------------------------------------------------------------------------------------------------------------
				// start set text component.
				try {
					Elements textElements = doc.select("div.gd22v2-left").select("div.c00v0-pilot");
					if (textElements != null) {
						Node textNode = bottomLeftGridNode.hasNode("text") ? bottomLeftGridNode
								.getNode("text") : null;
						if (textNode != null) {
							Element ele = textElements.first();
								if (ele != null) {
									String textProp = ele.html();
									textProp = FrameworkUtils
											.extractHtmlBlobContent(ele, "",
													locale, sb,urlMap);
									log.debug("text property!: " + textProp);
									textNode.setProperty("text", textProp);
								} else {
									sb.append(Constants.TEXT_ELEMENT_NOT_FOUND);
								}

							}
					} else {
						sb.append(Constants.TEXT_ELEMENT_NOT_FOUND);
					}

				} catch (Exception e) {
					sb.append(Constants.EXCEPTION_TEXT_COMPONENT);
				}

				// end set text

				// start set text component.
				try {
					Elements textElements = doc.select("div.gd22v2-left").select("div.c00v0-pilot");
					if (textElements != null) {
						Node textNode = bottomLeftGridNode.hasNode("text") ? bottomLeftGridNode
								.getNode("text") : null;
						if (textNode != null) {
							Element ele = textElements.first();
								if (ele != null) {
									String textProp = ele.html();
									textProp = FrameworkUtils
											.extractHtmlBlobContent(ele, "",
													locale, sb,urlMap);
									log.debug("text property!: " + textProp);
									textNode.setProperty("text", textProp);
								} else {
									sb.append(Constants.TEXT_ELEMENT_NOT_FOUND);
								}

							}
					} else {
						sb.append(Constants.TEXT_ELEMENT_NOT_FOUND);
					}

				} catch (Exception e) {
					sb.append(Constants.EXCEPTION_TEXT_COMPONENT);
				}

				
				try{
					log.debug("Start of spotlight..");
					Elements spElem = doc.select("div.c11-pilot");
					Node spNode = bottomMostLeftGridNode.hasNode("spotlight_medium_0")?bottomMostLeftGridNode.getNode("spotlight_medium_0"):null;
					if(!spElem.isEmpty()){
						for(Element sp : spElem){
							Element h2Ele = sp.getElementsByTag("h2").first();
							Element pEle = sp.getElementsByTag("p").first();
							Element aEle = sp.select("a.cta").first();
								if(spNode != null){
								spNode.setProperty("title",h2Ele.text());
								spNode.setProperty("description",pEle.text());
								// start image
								String spotLightImage = FrameworkUtils.extractImagePath(sp, sb);
								log.debug("spotLightImage befor migration : " + spotLightImage + "\n");
								Node imageNode = spNode.hasNode("image")?spNode.getNode("image"):null;
								if(imageNode!=null){
									String fileReference = imageNode.hasProperty("fileReference")?imageNode.getProperty("fileReference").getString():"";
									spotLightImage = FrameworkUtils.migrateDAMContent(spotLightImage, fileReference, locale,sb);
									log.debug("spotLightImage after migration : " + spotLightImage + "\n");
									if (StringUtils.isNotBlank(spotLightImage)) {
										imageNode.setProperty("fileReference" , spotLightImage);
									}
								}else{
									sb.append(Constants.SPOTLIGHT_IMAGE_NODE_NOT_AVAILABLE);
								}
								// end image
								if(aEle!=null){
									spNode.setProperty("linktext",aEle.text());
									Node spCta = spNode.hasNode("cta")?spNode.getNode("cta"):null;
									if(spCta!=null){
										spCta.setProperty("url",aEle.attr("href"));
									}else{
										sb.append(Constants.CTA_NOT_AVAILABLE);
									}
								}
							}
						}
					}else{
						sb.append(Constants.SPOTLIGHT_ELEMENT_NOT_FOUND);
					}
					log.debug("Spotlight component(s) updated..");
				}catch(Exception e){
					sb.append(Constants.EXCEPTION_SPOTLIGHT_COMPONENT);
					log.error("unable to update spotlight component due to : ",e);
				}
				//end of spotlight


				
				// ---------------------------------------------------------------------------------------------------------------------------------------
				// start of html blob components content.
				try {
					String headerOfHtmlBlob = "";
					Elements headingElements = doc.select("h2.bdr-1");
					if(!headingElements.isEmpty()){
						for(Element heading : headingElements){
							if(heading.parent().hasClass("no-padding")){
								log.debug("heading for last htmlblob********* "+heading.text());
								headerOfHtmlBlob = heading.outerHtml();
						}
							
						}
					}
					String html = "";
					//StringBuilder htmlBlobContent = new StringBuilder();
					Elements htmlBlobElements = doc.select("div.gd21v1-mid").select("div.htmlblob");
					NodeIterator htmlblobNodes = bottomMostLeftGridNode.getNodes("htmlblob*");
					if (!htmlBlobElements.isEmpty()) {
						int count = 0;
						for (Element htmlblobElement : htmlBlobElements) {
							if (htmlblobElement != null) {
								count++;
								html = htmlblobElement.outerHtml();
								if(count == htmlBlobElements.size()){
								html = FrameworkUtils.extractHtmlBlobContent(
										htmlblobElement, "", locale, sb,urlMap);
								html = headerOfHtmlBlob+html;
								//htmlBlobContent.append(headerOfHtmlBlob+html);
								}else{
									html = FrameworkUtils.extractHtmlBlobContent(
											htmlblobElement, "", locale, sb,urlMap);
								}
							} else {
								log.debug(Constants.HTMLBLOB_ELEMENT_NOT_FOUND);
							}
							if(htmlblobNodes.hasNext()){
							Node htmlBlobNode = (Node) htmlblobNodes.next();
							if(htmlBlobNode != null) {
							log.debug("htmlblobElement.outerHtml() " + headerOfHtmlBlob + html +" heading" + "\n");
							if (StringUtils.isNotBlank(html)) {
								htmlBlobNode.setProperty("html",
										html);
							} else {
								sb.append(Constants.HTMLBLOB_CONTENT_DOES_NOT_EXIST);
							}
							}

						} else {
							log.debug("htmlblob component not present at "
									+ bottomRightGridNode.getPath());
						}

						}
						
						
					}

					
				} catch (Exception e) {
					log.debug(Constants.EXCEPTION_IN_HTMLBLOB);
				}
				// end set html blob component content.
				// --------------------------------------------------------------------------------------------------------------------------

				try{
					
					Node listNode = bottomMostLeftGridNode
							.getNode("list_0");
					Elements topRightListElement = doc.select("div.n13v12-pilot");
					if(!topRightListElement.isEmpty()){
						//After text and 1st list section
						
						Element listElement = topRightListElement.first();
						if (listElement != null) {
									String indexTitle = listElement.getElementsByTag(
											"h2").text();

									Elements indexUlList = listElement
											.getElementsByTag("ul");

									Elements listDescription = listElement.select(
											"div.intro").select("p");

									for (Element ele : indexUlList) {
										java.util.List<String> list = new ArrayList<String>();
										Elements indexLiList = ele
												.getElementsByTag("li");

										for (Element li : indexLiList) {
											JSONObject jsonObj = new JSONObject();
											Elements listItemAnchor = li
													.getElementsByTag("a");
											Elements listItemSpan = li
													.getElementsByTag("span");

											String anchorText = listItemAnchor != null ? listItemAnchor
													.text() : "";
											String anchorHref = listItemAnchor
													.attr("href");
											// Start extracting valid href
											log.debug("Before ListLinkUrl" + anchorHref + "\n");
											anchorHref = FrameworkUtils.getLocaleReference(anchorHref, urlMap);
											log.debug("after ListLinkUrl" + anchorHref + "\n");
											// End extracting valid href
											String anchorTarget = listItemAnchor
													.attr("target");
											String listIcon = listItemSpan
													.attr("class");
											// String icon = li.ownText();

											jsonObj.put("linktext", anchorText);
											jsonObj.put("linkurl", anchorHref);
											jsonObj.put("icon", listIcon);
											jsonObj.put("size", "");// Need to get the
																	// size
											// from the list element
											// text.
											jsonObj.put("description", "");// Need to
																			// get
											// the
											// description
											// from the list
											// element text.
											if (StringUtils.isNotBlank(anchorTarget)) {
												jsonObj.put("openInNewWindow", true);
											}
											list.add(jsonObj.toString());

										}
										log.debug("div class 'div.n13-pilot' ul content ::"
												+ list.toString());

										NodeIterator elementList = null;
											if (listNode != null) {
												log.debug("path of list node: "
														+ listNode.getPath());
												listNode.setProperty("title",
														indexTitle);
												if (listNode.hasNode("intro")) {
													Node introNode = listNode.hasNode("intro")?listNode
															.getNode("intro"):null;
													String descProperty = "";
													if (listDescription != null && introNode != null) {
														descProperty = listDescription
																.html();
														introNode.setProperty(
																"paragraph_rte",
																descProperty.trim());
														log.debug("Updated descriptions at "
																+ introNode.getPath());

													} else {
														sb.append(Constants.LIST_INTRO_PARAGRAPH_ELEMENT_NOT_FOUND);
													}
												}

												elementList = listNode
														.getNodes("element_list*");
												if (elementList != null
														&& elementList.hasNext()) {
													Node eleNode = (Node) elementList
															.next();
													if (eleNode != null) {

														if (eleNode
																.hasProperty("listitems")) {
															Property listitems = eleNode
																	.getProperty("listitems");
															if (!listitems.isMultiple()) {
																listitems.remove();
																session.save();
															}
														}
														eleNode.setProperty(
																"listitems",
																list.toArray(new String[list
																		.size()]));
														log.debug("Updated listitems at "
																+ eleNode.getPath());
													}
												} else {
													sb.append(Constants.LIST_ELEMENT_LIST_NODE_NOT_FOUND);
												}

											}
											if (elementList != null
													&& elementList.getSize() != indexUlList
															.size()) {
												sb.append("<li>Could not migrate List node. Count mis match as Element Count is "
														+ indexUlList.size()
														+ " and node count is "
														+ (int) elementList.getSize() + " </li>");
											}
										}
									}
							else {
								sb.append(Constants.LIST_ELEMENT_NOT_FOUND);
							}
									
					}else {
						sb.append(Constants.LIST_ELEMENT_NOT_FOUND);
					}
						
				}catch(Exception e){
					sb.append(Constants.EXCEPTION_IN_UPDATING_LIST_COMPONENT);
					log.debug(Constants.EXCEPTION_IN_UPDATING_LIST_COMPONENT);
				}

				
				
				session.save();
			}
			else {
				sb.append(Constants.URL_CONNECTION_EXCEPTION);
			}
		} catch (Exception e) {
			sb.append(Constants.URL_CONNECTION_EXCEPTION);
			log.debug("Exception as url cannot be connected: ", e);
		}
		sb.append("</ul></td>");

		return sb.toString();
	}
	
	//start setting of spotlight
		public void spotLightTranslate (Node slNode, Element spElement, String locale,Map<String,String> urlMap) {
			try {
				Element spotLightTitle = spElement.getElementsByTag("h2").first();
				Element spotLightDescription = spElement.getElementsByTag("p").first();
				Elements spotLightAnchorElements = spElement.getElementsByTag("a");
				Element spotLightAnchor = spotLightAnchorElements.last();
				
				// start image
				String spotLightImage = FrameworkUtils.extractImagePath(spElement, sb);
				log.debug("spotLightImage " + spotLightImage + "\n");
				if (slNode != null) {
					if (slNode.hasNode("image")) {
						Node spotLightImageNode = slNode.getNode("image");
						String fileReference = spotLightImageNode.hasProperty("fileReference")?spotLightImageNode.getProperty("fileReference").getString():"";
						spotLightImage = FrameworkUtils.migrateDAMContent(spotLightImage, fileReference, locale,sb);
						log.debug("spotLightImage " + spotLightImage + "\n");
						if (StringUtils.isNotBlank(spotLightImage)) {
							spotLightImageNode.setProperty("fileReference" , spotLightImage);
						}else{
							sb.append(Constants.IMAGE_NOT_FOUND_IN_LOCALE_PAGE);
						}
					} else {
						sb.append("<li>spotlight image node doesn't exist</li>");
					}
				}
				// end image
				
				if(spotLightDescription.getElementsByTag("a")!=null && !spotLightDescription.getElementsByTag("a").isEmpty()){
					slNode.setProperty("description", spotLightDescription.html());
					// start
					if (spotLightAnchorElements.size() > 1) {
						spotLightAnchor = spotLightAnchorElements.get(1);
					}
					else {
						spotLightAnchor = null;
						sb.append("<li>Link is not found on locale page for the spotlight component. This needs to be deleted manually.</li>");
					}
					//end
				}
				else {
					slNode.setProperty("description", spotLightDescription.text());
				}
				
				if (spotLightAnchor != null) {
					String linkText = spotLightAnchor.text();
					String linkUrl = spotLightAnchor.attr("href");
					// Start extracting valid href
					log.debug("Before spotlight LinkUrl" + linkUrl + "\n");
					linkUrl = FrameworkUtils.getLocaleReference(linkUrl, urlMap);
					log.debug("after spotlight LinkUrl" + linkUrl + "\n");
					// End extracting valid href
					slNode.setProperty("linktext", linkText);
					javax.jcr.Node ctaNode = slNode.getNode("cta");
					if (ctaNode != null) {
						if (linkUrl != null) {
//							ctaNode.setProperty("linktype", "Url");
							ctaNode.setProperty("url", linkUrl);
						}
					}
				}
				
				if (spotLightTitle != null) {
					Elements spotLightLink = spotLightTitle.getElementsByTag("a");
					if (spotLightLink.isEmpty()) {
						slNode.setProperty("title", spotLightTitle.text());
					}
					else {
						Element spotLightLinkEle = spotLightLink.first();
						String slLinkText = spotLightLinkEle.text();
						String slLinkUrl = spotLightLinkEle.attr("href");
						// Start extracting valid href
						log.debug("Before spotlight LinkUrl" + slLinkUrl + "\n");
						slLinkUrl = FrameworkUtils.getLocaleReference(slLinkUrl, urlMap);
						log.debug("after spotlight LinkUrl" + slLinkUrl + "\n");
						// End extracting valid href
						slNode.setProperty("title", slLinkText);
						javax.jcr.Node titleLinkNode = slNode.getNode("titlelink");
						if (titleLinkNode != null) {
							if (slLinkUrl != null) {
								titleLinkNode.setProperty("linktype", "Url");
								titleLinkNode.setProperty("url", slLinkUrl);
							}
						}
					}
				}
				log.debug("Updated title, descriptoin and linktext at "+slNode.getPath());
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		//end setting of spotlight

}
