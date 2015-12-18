package com.cisco.dse.global.migration.productlanding;

/* 
 * S.No     	Name                 Date                    Description of change
 *  #1         Saroja            15-Dec-15           Added the Java file to handle the migration of product landing variation 12 page(s).
 * 
 * */

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

public class ProductLandingVariation12 extends BaseAction {

	Document doc;
	StringBuilder sb = new StringBuilder(1024);

	String footerLinks = "{\"linktext\":\"<aaa>\",\"linkurl\":\"<bbb>\"}";

	static Logger log = Logger.getLogger(ProductLandingVariation12.class);

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
				+ "/<prod>/index/jcr:content/content_parsys/overview/layout-overview/gd12v2_0/gd12v2-left";

		String indexMidLeft = "/content/<locale>/"
				+ catType
				+ "/<prod>/index/jcr:content/content_parsys/overview/layout-overview/gd11v1/gd11v1-mid";
		
		String indexBottomLeft = "/content/<locale>/"
				+ catType
				+ "/<prod>/index/jcr:content/content_parsys/overview/layout-overview/gd12v2/gd12v2-left";

		String indexRight = "/content/<locale>/"
				+ catType
				+ "/<prod>/index/jcr:content/content_parsys/overview/layout-overview/gd12v2_0/gd12v2-right";

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
		indexBottomLeft = indexBottomLeft.replace("<locale>", locale).replace(
				"<prod>", prod);
		sb.append("<td>" + "<a href=" + pageUrl + ">" + pageUrl + "</a>"
				+ "</td>");
		sb.append("<td>" + "<a href=" + loc + ">" + loc + "</a>" + "</td>");
		sb.append("<td><ul>");

		Node indexLeftNode = null;
		Node indexRightNode = null;
		Node indexMidLeftNode = null;
		Node indexRightRailNode = null;
		Node indexBottomLeftNode = null;
		Node pageJcrNode = null;
		try {
			
			indexLeftNode = session.getNode(indexLeft);
			indexBottomLeftNode = session.getNode(indexBottomLeft);
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

				// ------------------------------------------------------------------------------------------------------------------------------------------
				// start set page properties.

				FrameworkUtils.setPageProperties(pageJcrNode, doc, session, sb);

				// end set page properties.
				// ------------------------------------------------------------------------------------------------------------------------------------------

				// ------------------------------------------------------------------------------------------------------------------------------------------
				// start set hero large component properties.

				try {
										Elements heroElements = doc.select("div.c50-pilot");
					heroElements = heroElements.select("div.frame");
					Node heroNode = indexLeftNode.hasNode("hero_large") ? indexLeftNode
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
								} else {
									sb.append(Constants.HERO_CONTENT_ANCHOR_ELEMENT_DOESNOT_EXISTS);
								}
								if (heroPanelNodeIterator.hasNext()) {
									Node heroPanelNode = (Node) heroPanelNodeIterator
											.next();
									// start image
									String heroImage = FrameworkUtils.extractImagePath(ele, sb);
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
											heroImage = FrameworkUtils.migrateDAMContent(heroImage,
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
											sb.append(Constants.HERO_IMAGE_NOT_AVAILABLE);
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
								sb.append(Constants.HERO_CONTENT_COUNT_MISMATCH
										+ eleSize
										+ " "
										+ nodeSize);

							}
						}

					} else {
						sb.append(Constants.HERO_CONTENT_NODE_NOT_FOUND);

					}

				} catch (Exception e) {
					sb.append(Constants.EXCEPTOIN_IN_UPDATING_HERO_CONTENT);
					log.debug(" Hero Component Exception: ", e);
				}

				// ---------------------------------------------------------------------------------------------------------------------------------------
				// start set selectorbar large component properties.

				try {
					
					Elements selectorBarLargeElements = doc
							.select("div.panel");
					log.debug("selector component found indexMidLeftNode: "
							+ indexMidLeftNode.getPath());
					Node selectorBarNode = indexMidLeftNode
							.hasNode("selectorbarfull") ? indexMidLeftNode
							.getNode("selectorbarfull") : null;

					if (selectorBarNode != null) {
						int eleSize = selectorBarLargeElements.size();
						NodeIterator selectorBarPanel = selectorBarNode
								.getNodes("selectorbarpanel*");
						int nodeSize = (int) selectorBarPanel.getSize();
						for (Element ele : selectorBarLargeElements) {
							String h2Text = "";
							String titleURL = "";
							String aText = "";
							String aHref = "";
							selectorBarPanel.hasNext();
							Node selectorBarPanelNode = (Node) selectorBarPanel
									.next();
							// Element titleEle =
							// selectorBarLargeElements.first();
							Elements h2TagText = ele.getElementsByTag("h2");
							if (h2TagText != null) {
								h2Text = h2TagText.html();
							} else {
								sb.append(Constants.SELECTOR_BAR_TITLE_NOT_AVAILABLE);
							}

							Elements titleUrl = ele.getElementsByTag("h2")
									.select("a");
							if (titleUrl != null) {
								titleURL = titleUrl.attr("href");
							} else {
								sb.append(Constants.SELECTOR_BAR_TITLE_URL_NOT_AVAILABLE);
							}
							Element menuelement = null;
							if(ele.parent().hasClass("c58v1-pilot")){
								 menuelement = ele.child(0).child(1);
								 log.debug("menuelement in ifff" + menuelement);
								//menuEle = ele.child(1);
							}
							else{
								menuelement = ele.child(1);
							}
							
							int childrenSize = menuelement.children().size();
							if(childrenSize >1){
								Element allLinkTag = menuelement.getElementsByTag("a")						
									.last();
							if (allLinkTag != null) {
								aText = allLinkTag.text();
								aHref = allLinkTag.attr("href");
							} else {
								sb.append(Constants.SELECTOR_BAR_ALL_LINK_NOT_AVAILABLE);
							}
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

							Elements menuUlList = menuelement
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
											log.debug("list is: "+ list.toString());
										} else {
											sb.append(Constants.SELECTOR_BAR_DROPDOWN_URLS_NOT_AVAILABLE);
										}

									}
									selectorBarPanelNode.setProperty(
											"panelitems", list
													.toArray(new String[list
															.size()]));
								} else {
									sb.append(Constants.SELECTOR_BAR_DROPDOWN_URLS_NOT_AVAILABLE);
								}
							}
						}

						if (eleSize != nodeSize) {
							sb.append(Constants.SELECTOR_BAR_ELEMENTS_COUNT_MISMATCH
									+ eleSize
									+ " "
									+ nodeSize);
						}
					}

				} catch (Exception e) {
					sb.append(Constants.SELECTOR_BAR_COMPONENT_NOT_UPDATED);
					log.debug("exception : ",e);
				}

				// end set Selector bar.
				// ----------------------------------------------------------------------------------
				// ---------------------------------------------------------------------------------------------------------------------------------------
				// start of html blob components content.
				
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
									sb.append(Constants.TEXT_ELEMENT_NOT_FOUND);
								}

							}
						}
					} else {
						sb.append(Constants.TEXT_ELEMENT_NOT_FOUND);
					}

				} catch (Exception e) {
					sb.append(Constants.EXCEPTION_TEXT_COMPONENT);
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

							Elements h2TagText = ele.getElementsByTag("h2");
							if (h2TagText != null) {
								h2Text = h2TagText.html();
							} else {
								sb.append(Constants.TILE_BORDERED_TITLE_NOT_FOUND);
							}

							Elements descriptionText = ele
									.getElementsByTag("p");
							if (descriptionText != null) {
								pText = descriptionText.html();
							} else {
								sb.append(Constants.TILE_BORDERED_DESCRIPTION_NOT_FOUND);
							}

							Elements anchorText = ele.getElementsByTag("a");
							if (anchorText != null) {
								aText = anchorText.text();
								aHref = anchorText.attr("href");
							} else {
								sb.append(Constants.TILE_BORDERED_ANCHOR_NOT_FOUND);
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

							sb.append(Constants.TILE_BORDERED_ELEMENT_COUNT_MISMATCH
									+ eleSize
									+ " "
									+ nodeSize);
							log.debug("Could not migrate  tilebordered node. Count mis match");

						}
					}

				} catch (Exception e) {
					sb.append(Constants.TILE_BORDERED_COMPONENT_NOT_UPDATED);
					log.debug(Constants.TILE_BORDERED_COMPONENT_NOT_UPDATED, e);
				}
				
				boolean primaryCtaExists = false;
				// start set benefit text content.
				try {
					
					Elements primaryCtaElements = doc.select("div.c47-pilot");
					if (!primaryCtaElements.isEmpty()) {
						primaryCtaExists = true;
						sb.append(Constants.PRIMARY_CTA_COMPONENT_NOT_FOUND);
						
					} 

				} catch (Exception e) {
					sb.append(Constants.PRIMARY_CTA_COMPONENT_NOT_UPDATED);
					log.debug(Constants.PRIMARY_CTA_COMPONENT_NOT_UPDATED,e);
				}

				
				// end set primary cta title, description, link text, linkurl.
				
				
				// start set spotlight component.
				try {
					Elements spotLightElements = doc.select("div.c11-pilot");
					
					if (spotLightElements != null) {
						if (indexLeftNode != null) {
							int eleSize = spotLightElements.size();
							NodeIterator spoLightNodeIterator = indexBottomLeftNode
									.getNodes("spotlight_large*");
							int nodeSize = (int) spoLightNodeIterator.getSize();
							for (Element ele : spotLightElements) {
								String h2Text = "";
								String pText = "";
								String aText = "";
								String aHref = "";
								spoLightNodeIterator.hasNext();
								Node spotLightComponentNode = (Node) spoLightNodeIterator
										.next();

								Elements h2TagText = ele.getElementsByTag("h2");
								if (h2TagText != null) {
									h2Text = h2TagText.html();
								} else {
									sb.append(Constants.SPOTLIGHT_HEADING_TEXT_NOT_FOUND);
								}

								Elements descriptionText = ele
										.getElementsByTag("p");
								if (descriptionText != null) {
									pText = descriptionText.html();
								} else {
									sb.append(Constants.SPOTLIGHT_DESCRIPTION_ELEMENT_NOT_FOUND);
								}

								Elements anchorText = ele.getElementsByTag("a");
								if (anchorText != null) {
									aText = anchorText.text();
									aHref = anchorText.attr("href");
								} else {
									sb.append(Constants.SPOTLIGHT_ANCHOR_ELEMENT_NOT_FOUND);
								}
								// start image
								String spotLightImage = FrameworkUtils.extractImagePath(ele, sb);
								log.debug("spotLightImage " + spotLightImage + "\n");
								if (spotLightComponentNode != null) {
									if (spotLightComponentNode.hasNode("image")) {
										Node spotLightImageNode = spotLightComponentNode.getNode("image");
										String fileReference = spotLightImageNode.hasProperty("fileReference")?spotLightImageNode.getProperty("fileReference").getString():"";
										spotLightImage = FrameworkUtils.migrateDAMContent(spotLightImage, fileReference, locale);
										log.debug("spotLightImage " + spotLightImage + "\n");
										if (StringUtils.isNotBlank(spotLightImage)) {
											spotLightImageNode.setProperty("fileReference" , spotLightImage);
										}
									} else {
										sb.append(Constants.SPOTLIGHT_IMAGE_NOT_AVAILABLE);
									}
								}
								// end image
								Node ctaNode = spotLightComponentNode.hasNode("cta")?spotLightComponentNode.getNode("cta"): null;
								spotLightComponentNode.setProperty("title", h2Text);
								spotLightComponentNode.setProperty("description",
										pText);
								spotLightComponentNode.setProperty("linktext",
										aText);
								if(ctaNode != null){
									ctaNode.setProperty("url", aHref);
								}
							}

							if (nodeSize != eleSize) {
								sb.append(Constants.SPOTLIGHT_NODE_COUNT
										+ eleSize
										+ " "
										+ nodeSize);

							}
						}

					} else {
						sb.append(Constants.EXCEPTION_SPOTLIGHT_COMPONENT);

					}
				} catch (Exception e) {
					sb.append("<li>Unable to update spotlight component." + e
							+ "</li>");
				}
				// end set spotlight nodes

				// start set ENTERPRISE NETWORK INDEX list.
				if(!primaryCtaExists){
					try {
					Elements indexlistElem = doc.select("div.n13-pilot");
					Element rightRailPilotElement = indexlistElem.first();
					if (rightRailPilotElement != null) {
							String indexTitle = rightRailPilotElement.getElementsByTag(
									"h2").outerHtml();

							Elements indexUlList = rightRailPilotElement
									.getElementsByTag("ul");

							Elements listDescription = rightRailPilotElement.select(
									"div.intro").select("p");
							List<String> list = new ArrayList<String>();
							for (Element ele : indexUlList) {
								
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
									log.debug("div class 'div.n13-pilot' ul content ::"
											+ list.toString());
								}
							}
								

								//Node listNode = null;
								NodeIterator elementList = null;
								Node listNode = indexRightNode.hasNode("list")?
										indexRightNode.getNode("list"):null;
									log.debug("path of list node in right index node: "
											+ listNode.getPath());

									if (listNode != null) {
										if(StringUtils.isNotBlank(indexTitle)){
											listNode.setProperty("title",								
												indexTitle);
										}else{
											sb.append(Constants.RIGHT_LIST_COMPONENT_TITLE_NOT_FOUND);
										}
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
												sb.append(Constants.RIGHT_LIST_COMPONENT_DESCRIPTION_NOT_FOUND);
											}
										}
										//As Ja_jp has commented content in its right node list, this check has been done.
										if(!list.isEmpty()){
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
												log.debug("Updated listitems at "+ eleNode.getPath());
												}else{
													sb.append(Constants.LEFT_GRID_ELEMENT_LIST_NODE_NOT_FOUND);
												}
														
											}
										} 

									}
					} else {
						sb.append(Constants.LIST_ELEMENTS_COUNT_MISMATCH);
					}
					
				} catch (Exception e) {
					sb.append(Constants.EXCEPTION_IN_UPDATING_LIST_COMPONENT);
					log.error("Exception : ", e);
				}
				}
				
				if(!primaryCtaExists){
					try {
				
					String html = "";
					StringBuilder htmlBlobContent = new StringBuilder();
					Elements htmlBlobElements = doc.select("div.htmlblob");
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
					
					
					if (indexRightNode.hasNode("htmlblob")) {
						javax.jcr.Node htmlBlobNode = indexRightNode
								.getNode("htmlblob");
						log.debug("htmlblobElement.outerHtml() " + html + "\n");
						if (StringUtils.isNotBlank(html)) {
							htmlBlobNode.setProperty("html",
									htmlBlobContent.toString());
						} else {
							sb.append(Constants.HTMLBLOB_CONTENT_DOES_NOT_EXIST);
						}

					} else {
						log.debug("htmlblob component not present at "
								+ indexMidLeftNode.getPath());
					}
					

				} catch (Exception e) {
					log.debug(Constants.EXCEPTION_IN_HTMLBLOB);
				}
				}
				// end set html blob component content.
				// --------------------------------------------------------------------------------------------------------------------------

				// end set benefit list.
				
				
				// start set ENTERPRISE NETWORK INDEX list.
				try {
					NodeIterator listNodeIterator = indexRightRailNode
							.getNodes("list*");
					Elements indexlistElem = doc.select("div.n13-pilot");
					int eleSize = 0;
				    int listNodeSize = (int) listNodeIterator.getSize();
				    Elements childElements = null;
				    
					//Element rightRailPilotElement = indexlistElem.first();
					
					if (!indexlistElem.isEmpty()) {
						for (Element indexListItem : indexlistElem) {
							if(!indexListItem.hasClass("compact")){
								childElements = indexListItem.getElementsByTag(
										"h2");
								
							String indexTitle = indexListItem.getElementsByTag(
									"h2").first().html();

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
												sb.append(Constants.RIGHT_LIST_COMPONENT_DESCRIPTION_NOT_FOUND);
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
											sb.append(Constants.LEFT_GRID_ELEMENT_LIST_NODE_NOT_FOUND);
										}

									}
								}
							}
							eleSize = childElements.size();

						}
							

						}
						if (eleSize > 1 && eleSize != listNodeSize) {

							sb.append(Constants.LIST_ELEMENTS_COUNT_MISMATCH
									+ eleSize
									+ listNodeSize);
							log.debug("Could not migrate  tilebordered node. Count mis match");

						}
						} else {
						sb.append(Constants.LIST_NOT_FOUND_IN_RIGHT_RAIL);
					}
				} catch (Exception e) {
					sb.append(Constants.EXCEPTION_IN_UPDATING_LIST_COMPONENT);
					log.error("Exception : ", e);
				}
				// end set benefit list.

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
								sb.append(Constants.FOLLOWUS_TITLE_NOT_FOUND);
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
					if (indexRightRailNode.hasNode("followus")) {
						Node followus = indexRightRailNode.getNode("followus");
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


				
				session.save();
			} else {
				sb.append(Constants.URL_CONNECTION_EXCEPTION);
			}

		} catch (Exception e) {
			
			sb.append(Constants.URL_CONNECTION_EXCEPTION);
			log.debug("Exception as url cannot be connected: " + e);
		}

		sb.append("</ul></td>");

		return sb.toString();
	}

}
