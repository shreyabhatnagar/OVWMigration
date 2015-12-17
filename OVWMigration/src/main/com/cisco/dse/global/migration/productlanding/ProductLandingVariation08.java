package com.cisco.dse.global.migration.productlanding;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.cisco.dse.global.migration.config.BaseAction;
import com.cisco.dse.global.migration.config.Constants;
import com.cisco.dse.global.migration.config.FrameworkUtils;

public class ProductLandingVariation08 extends BaseAction {

	Document doc;

	String title = null;

	StringBuilder sb = new StringBuilder(1024);

	String footerLinks = "{\"linktext\":\"<aaa>\",\"linkurl\":\"<bbb>\"}";

	static Logger log = Logger.getLogger(ProductLandingVariation08.class);

	public String translate(String host, String loc, String prod, String type,
			String catType, String locale, Session session) throws IOException,
			ValueFormatException, VersionException, LockException,
			ConstraintViolationException, RepositoryException {

		BasicConfigurator.configure();
		log.debug("In the translate method");
		log.debug("In the translate method, catType is :" + catType);
		String pagePropertiesPath = "/content/<locale>/" + catType
				+ "/<prod>/index/jcr:content";
		String indexLeft = "/content/<locale>/"
				+ catType
				+ "/<prod>/index/jcr:content/content_parsys/overview/layout-overview/gd12v1/gd12v1-left";

		String indexMidLeft = "/content/<locale>/"
				+ catType
				+ "/<prod>/index/jcr:content/content_parsys/overview/layout-overview/gd12v2/gd12v2-left";

		String indexRight = "/content/<locale>/"
				+ catType
				+ "/<prod>/index/jcr:content/content_parsys/overview/layout-overview/gd12v1/gd12v1-right";

		String indexRightRail = "/content/<locale>/"
				+ catType
				+ "/<prod>/index/jcr:content/content_parsys/overview/layout-overview/gd12v2/gd12v2-right";

		String pageUrl = host + "/content/<locale>/" + catType
				+ "/<prod>/index.html";

		pageUrl = pageUrl.replace("<locale>", locale).replace("<prod>", prod);
		pagePropertiesPath = pagePropertiesPath.replace("<locale>", locale)
				.replace("<prod>", prod);
		indexLeft = indexLeft.replace("<locale>", locale).replace("<prod>",
				prod);
		indexRight = indexRight.replace("<locale>", locale).replace("<prod>",
				prod);
		indexMidLeft = indexMidLeft.replace("<locale>", locale).replace(
				"<prod>", prod);
		indexRightRail = indexRightRail.replace("<locale>", locale).replace(
				"<prod>", prod);
		sb.append("<td>" + "<a href=" + pageUrl + ">" + pageUrl + "</a>"
				+ "</td>");
		sb.append("<td>" + "<a href=" + loc + ">" + loc + "</a>" + "</td>");
		sb.append("<td><ul>");

		Node indexLeftNode = null;
		Node indexRightNode = null;
		Node indexMidLeftNode = null;
		Node indexRightRailNode = null;
		Node pageJcrNode = null;
		try {

			indexLeftNode = session.getNode(indexLeft);
			indexMidLeftNode = session.getNode(indexMidLeft);
			indexRightRailNode = session.getNode(indexRightRail);
			indexRightNode = session.getNode(indexRight);
			pageJcrNode = session.getNode(pagePropertiesPath);
			try {
				doc = Jsoup.connect(loc).get();
				log.debug("Connected to the provided URL");
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

				// ------------------------------------------------------------------------------------------------------------------------------------------
				// start set benefit text content.
				try {
					String h3Text = "";
					String pText = "";
					String aText = "";
					String aHref = "";

					Elements primaryCtaElements = doc.select("div.c47-pilot");
					if (primaryCtaElements != null) {
						Node primaryCtaNode = indexLeftNode
								.hasNode("primary_cta_v2") ? indexLeftNode
								.getNode("primary_cta_v2") : null;
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

								Elements anchorText = ele.getElementsByTag("a");
								if (anchorText != null) {
									aText = anchorText.text();
									aHref = anchorText.attr("href");
								} else {
									sb.append("<li>Primary CTA anchor tag not having any content in it ('<a>' is blank)</li>");
								}
								primaryCtaNode.setProperty("title", h3Text);
								primaryCtaNode
										.setProperty("description", pText);
								primaryCtaNode.setProperty("linktext", aText);
								Node linkUrlNode = primaryCtaNode
										.hasNode("linkurl") ? primaryCtaNode
										.getNode("linkurl") : null;
								if (linkUrlNode != null) {
									linkUrlNode.setProperty("url", aHref);
								} else {
									sb.append("<li>Primary CTA Node does not have link url node. </li>");
								}
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

				// ------------------------------------------------------------------------------------------------------------------------------------------
				// start set hero large component properties.

				try {
					String h2Text = "";
					String pText = "";
					String aText = "";
					String aHref = "";

					Elements heroElements = doc.select("div.c50-pilot");
					heroElements = heroElements.select("div.frame");
					Node heroNode = indexRightNode.hasNode("hero_large") ? indexRightNode
							.getNode("hero_large") : null;

					if (heroNode != null) {
						log.debug("heronode found: " + heroNode.getPath());
						if (heroElements != null) {
							int eleSize = heroElements.size();

							NodeIterator heroPanelNodeIterator = heroNode
									.getNodes("heropanel*");
							int nodeSize = (int) heroPanelNodeIterator
									.getSize();
							for (Element ele : heroElements) {
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
								} else {
									sb.append("<li>Hero Component anchor tag not having any content in it ('<a>' is blank)</li>");
								}
								if (heroPanelNodeIterator.hasNext()) {
									Node heroPanelNode = (Node) heroPanelNodeIterator
											.next();
									// start image
									String heroImage = FrameworkUtils
											.extractImagePath(ele, sb);
									log.debug("heroImage " + heroImage + "\n");
									if (heroPanelNode != null) {
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
															locale);
											log.debug("heroImage " + heroImage
													+ "\n");
											if (StringUtils
													.isNotBlank(heroImage)) {
												imageNode.setProperty(
														"fileReference",
														heroImage);
											}
										} else {
											sb.append("<li>hero image node doesn't exist</li>");
										}
									}
									// end image
									heroPanelNode.setProperty("title", h2Text);
									heroPanelNode.setProperty("description",
											pText);
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
						}

					} else {
						sb.append("<li>Hero Component not found on page. </li>");

					}

				} catch (Exception e) {
					sb.append("<li>Unable to update hero large component." + e
							+ "</li>");
					log.debug("Exception: ", e);
				}

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
						log.debug("selector component found ele size: "
								+ eleSize);
						NodeIterator selectorBarPanel = selectorBarNode
								.getNodes("selectorbarpanel*");
						int nodeSize = (int) selectorBarPanel.getSize();
						log.debug("selector component found nodeSize : "
								+ nodeSize);

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

							Elements titleUrl = ele.getElementsByTag("h2")
									.select("a");
							if (titleUrl != null) {
								titleURL = titleUrl.attr("href");
							} else {
								sb.append("<li>Selector Bar Component Title URL element not having any content in it ('a href' is blank)</li>");
							}
							Element menuEle = ele.child(1);
							// Element anchor =
							Element allLinkTag = menuEle.getElementsByTag("a")
									.last();
							if (allLinkTag != null) {
								aText = allLinkTag.text();
								aHref = allLinkTag.attr("href");
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
								List<String> list = new ArrayList<String>();
								Elements menuLiList = element
										.getElementsByTag("li");

								if (menuLiList != null) {
									for (Element li : menuLiList) {
										JSONObject jsonObj = new JSONObject();
										Elements listItemAnchor = li
												.getElementsByTag("a");

										if (listItemAnchor != null) {
											String anchorText = listItemAnchor
													.text();
											String anchorHref = listItemAnchor
													.attr("href");
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

						if (eleSize != nodeSize) {
							sb.append("<li>Selector Bar Component element size ( "
									+ eleSize
									+ " ) and node size ( "
									+ nodeSize + " ) mismatch</li>");
						}
					}

				} catch (Exception e) {
					sb.append("<li>Unable to update Selector bar large component."
							+ e + "</li>");
				}

				// end set Selector bar.
				// ----------------------------------------------------------------------------------
				// ---------------------------------------------------------------------------------------------------------------------------------------
				// start of html blob components content.
				try {
					String html = "";
					StringBuilder htmlBlobContent = new StringBuilder();
					Elements htmlBlobElements = doc.select("div.c11v5-pilot");
					if (htmlBlobElements != null) {
						for (Element htmlblobElement : htmlBlobElements) {
							if (htmlblobElement != null) {
								html = htmlblobElement.outerHtml();
								html = FrameworkUtils.extractHtmlBlobContent(
										htmlblobElement, "", locale, sb);
								htmlBlobContent.append(html);
							} else {
								log.debug("<li>htmlblob/icon-block Element section not found</li>");
							}
						}
					}

					if (indexMidLeftNode.hasNode("htmlblob")) {
						javax.jcr.Node htmlBlobNode = indexMidLeftNode
								.getNode("htmlblob");
						log.debug("htmlblobElement.outerHtml() " + html + "\n");
						if (StringUtils.isNotBlank(html)) {
							htmlBlobNode.setProperty("html",
									htmlBlobContent.toString());
						} else {
							sb.append("<li>htmlblob content doesn't exist</li>");
						}

					} else {
						log.debug("htmlblob component not present at "
								+ indexMidLeftNode.getPath());
					}

				} catch (Exception e) {
					log.debug("<li>Unable to update html blob component." + e
							+ "</li>");
				}
				// end set html blob component content.
				// --------------------------------------------------------------------------------------------------------------------------

				// text,linkurl.
				// ---------------------------------------------------------------------------------------------------------------------------------------
				// start set text component.
				try {
					Elements textElements = doc.select("div.c00-pilot");
					if (textElements != null) {
						Node textNode = indexMidLeftNode.hasNode("text") ? indexMidLeftNode
								.getNode("text") : null;
						if (textNode != null) {
							for (Element ele : textElements) {
								if (ele != null) {
									String textProp = ele.html();
									textProp = FrameworkUtils
											.extractHtmlBlobContent(ele, "",
													locale, sb);
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
					sb.append("<li>Unable to update index text component component."
							+ e + "</li>");
				}

				// end set text
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

							
							
							Elements h2TagText = null;
							boolean h3TagExists = false;
							h2TagText = ele.getElementsByTag("h2");
							if(ele.getElementsByTag("h2").isEmpty()){
								h3TagExists = true;
								h2TagText = ele.getElementsByTag("h3");
							}
							log.debug("h2TagText: "+ h2TagText);
							if (!h2TagText.isEmpty()) {
								h2Text = h2TagText.html();
							} else {
								sb.append("<li>TileBordered Component Heading element not having any title in it ('h2' is blank)</li>");
							}

							Elements descriptionText = ele
									.getElementsByTag("p");
							if (!descriptionText.isEmpty()) {
								pText = descriptionText.html();
							} else {
								sb.append("<li>TileBordered Component description element not having any title in it ('p' is blank)</li>");
							}
							
							Elements anchorText = ele.getElementsByTag("a");
							if(h3TagExists){
								Element anchor = anchorText.first();
									aText = anchor.text();
									aHref = anchor.attr("href");
									sb.append("<li>TileBordered Component has extra link, which cannot be migrated.</li>");
							}
							else{
							if (!anchorText.isEmpty()) {
								aText = anchorText.text();
								aHref = anchorText.attr("href");
							} else {
								sb.append("<li>TileBordered Component anchor tag not having any content in it ('<a>' is blank)</li>");
							}
							}

							spotLightComponentNode.setProperty("title", h2Text);
							spotLightComponentNode.setProperty("description",
									pText);
							spotLightComponentNode.setProperty("linktext",
									aText);
							spotLightComponentNode
									.setProperty("linkurl", aHref);

						}
						if (eleSize != nodeSize) {

							sb.append("<li>Could not migrate  tilebordered node. Count mis match as Element Count is "
									+ eleSize
									+ " and node count is "
									+ nodeSize + " </li>");
							log.debug("Could not migrate  tilebordered node. Count mis match");

						}
					}

				} catch (Exception e) {
					sb.append("<li>Unable to update spotlight component." + e
							+ "</li>");
				}

				// start set ENTERPRISE NETWORK INDEX list.
				try {
					
					boolean listItemFound = false;
					NodeIterator listNodeIterator = indexRightRailNode
							.getNodes("list*");
					Elements indexlistElem = doc.select("div.n13-pilot");
					if(indexlistElem.isEmpty()){
						//After text and 1st list section
						
						Element listElements = doc.select("div.c00v0-pilot").last();
						if (listElements != null) {
							listItemFound =  true;
							int childrenSize = listElements.children().size(); 						
							log.debug("Children size is "+childrenSize);
							List<String> headerList = new ArrayList<String>();
							List<String> paraList = new ArrayList<String>();
							List<Element> ulList = new ArrayList<Element>();
							String paraContent = "";
							String previousHeader = "";
							
							for (int count =0; count < childrenSize; count++) {
								System.out.println("Element at "+count);
								System.out.println("------------------");
								System.out.println(listElements.child(count));
								
								Element child = listElements.child(count);
								if (child != null) {
									System.out.println(child.tagName());
									if ("h2".equalsIgnoreCase(child.tagName())) {
										headerList.add(child.text());
										if (!paraContent.isEmpty()) {
											//Report content comes here
											System.out.println("The last paragraph element under heading '"+ previousHeader +"' is not migrated from locale page.");
											paraContent = "";
										}
										previousHeader = child.html();
									}
									else if ("p".equalsIgnoreCase(child.tagName())) {
										paraContent = paraContent + child.outerHtml();
									}
									else if ("ul".equalsIgnoreCase(child.tagName())) {
										paraList.add(paraContent);
										paraContent = "";
										ulList.add(child);
									}
								}
							}
							
							for (int loop = 0; loop < headerList.size(); loop++) {
								System.out.println("Header content for loop "+loop);
								System.out.println(headerList.get(loop));
								Node listNode = null;
								NodeIterator elementList = null;

								if (listNodeIterator.hasNext()) {
									listNode = (Node) listNodeIterator.next();
									log.debug("path of list node: "
											+ listNode.getPath());
									if(listNode != null){
										listNode.setProperty("title", headerList.get(loop));
									}
								}
								java.util.List<String> list = new ArrayList<String>();
								System.out.println(paraList.get(loop));
								System.out.println(ulList.get(loop));
								Elements ulItems = ulList.get(loop).select("li");
								for(Element li : ulItems){

									
										JSONObject jsonObj = new JSONObject();
										Elements listItemAnchor = li
												.getElementsByTag("a");
										Elements listItemSpan = li
												.getElementsByTag("span");

										String anchorText = listItemAnchor != null ? listItemAnchor
												.text() : "";
										String anchorHref = listItemAnchor
												.attr("href");
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

								
									log.debug("div class 'div.n13-pilot' ul content ::"
											+ list.toString());
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
									sb.append("<li>element_list node doesn't exists</li>");
								}
								System.out.println("-------------------------------------------------------------------------------------");
							}


					}	}
					Element rightRailPilotElement = indexlistElem.first();
					if(listItemFound){
						if (rightRailPilotElement != null ) {
					
						for (Element indexListItem : indexlistElem) {
							String indexTitle = indexListItem.getElementsByTag(
									"h2").text();

							Elements indexUlList = indexListItem
									.getElementsByTag("ul");

							Elements listDescription = indexListItem.select(
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

								Node listNode = null;
								NodeIterator elementList = null;

								if (listNodeIterator.hasNext()) {
									listNode = (Node) listNodeIterator.next();
									log.debug("path of list node: "
											+ listNode.getPath());

									if (listNode != null) {
										listNode.setProperty("title",
												indexTitle);
										if (listNode.hasNode("intro")) {
											Node introNode = listNode
													.getNode("intro");
											String descProperty = "";
											if (listDescription != null) {
												descProperty = listDescription
														.html();
												introNode.setProperty(
														"paragraph_rte",
														descProperty.trim());
												log.debug("Updated descriptions at "
														+ introNode.getPath());

											} else {
												sb.append("<li>Paragraph description is not migrated as it has no content.</li>");
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
											sb.append("<li>element_list node doesn't exists</li>");
										}

									}
									if (elementList != null
											&& elementList.getSize() != indexUlList
													.size()) {
										sb.append("<li>Mis-Match in Resource list Panels count/content."
												+ indexUlList.size()
												+ "not equal to "
												+ elementList.getSize()
												+ "</li>");

									}
								}
							}

						}

					}else {
						sb.append("<li>Mismatch in the right rail. List element is not found.</li>");
					}
						}
				} catch (Exception e) {
					sb.append("<li>Unable to update index list component.\n</li>");
					log.error("Exception : ", e);
				}
				// end set benefit list.

				session.save();
			} else {
				sb.append(Constants.URL_CONNECTION_EXCEPTION);
			}

		} catch (Exception e) {
			sb.append("<li>Exception as URL cannot be connected! </li>");
			log.debug("Exception as url cannot be connected: " + e);
		}

		sb.append("</ul></td>");

		return sb.toString();
	}

}
