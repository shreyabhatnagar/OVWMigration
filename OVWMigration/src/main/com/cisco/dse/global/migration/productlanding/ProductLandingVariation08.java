package com.cisco.dse.global.migration.productlanding;

/* 
 * S.No     	Name                 Date                    Description of change
 *  #1         Saroja            15-Dec-15           Added the Java file to handle the migration of product landing variation 08 page(s).
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
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.cisco.dse.global.migration.config.BaseAction;
import com.cisco.dse.global.migration.config.Constants;
import com.cisco.dse.global.migration.config.FrameworkUtils;

public class ProductLandingVariation08 extends BaseAction {

	Document doc;

	StringBuilder sb = new StringBuilder(1024);

	String footerLinks = "{\"linktext\":\"<aaa>\",\"linkurl\":\"<bbb>\"}";

	static Logger log = Logger.getLogger(ProductLandingVariation08.class);

	public String translate(String host, String loc, String prod, String type,
			String catType, String locale, Session session, Map<String, String> urlMap) throws IOException,
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

				// ------------------------------------------------------------------------------------------------------------------------------------------
				// start set page properties.

				FrameworkUtils.setPageProperties(pageJcrNode, doc, session, sb);

				// end set page properties.
				// ------------------------------------------------------------------------------------------------------------------------------------------

				// ------------------------------------------------------------------------------------------------------------------------------------------
				// start set benefit text content.
				try {
					Elements primaryCtaElements = doc.select("div.c47-pilot");
					if (primaryCtaElements != null) {
						Node primaryCtaNode = indexLeftNode
								.hasNode("primary_cta_v2") ? indexLeftNode
								.getNode("primary_cta_v2") : null;
						if (primaryCtaNode != null) {
							for (Element ele : primaryCtaElements) {
								String h3Text = "";
								String pText = "";
								String aText = "";
								String aHref = "";

								Elements h3TagText = ele.getElementsByTag("h3");
								if (h3TagText != null) {
									h3Text = h3TagText.html();
								} else {
									sb.append(Constants.PRIMARY_CTA_TITLE_ELEMENT_NOT_FOUND);
								}

								Elements descriptionText = ele
										.getElementsByTag("p");
								if (descriptionText != null) {
									pText = descriptionText.html();
								} else {
									sb.append(Constants.PRIMARY_CTA_DESCRIPTION_ELEMENT_NOT_FOUND);
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
									sb.append(Constants.PRIMARY_CTA_ANCHOR_ELEMENT_NOT_FOUND);
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
									sb.append(Constants.PRIMARY_CTA_LINK_URL_NODE_NOT_FOUND);
								}
							}
						}

					} else {
						sb.append(Constants.PRIMARY_CTA_COMPONENT_NOT_FOUND);

					}

				} catch (Exception e) {
					sb.append(Constants.PRIMARY_CTA_COMPONENT_NOT_UPDATED);
				}

				// end set primay cta title, description, link text, linkurl.

				// ------------------------------------------------------------------------------------------------------------------------------------------
				// start set hero large component properties.

				try {
					Elements heroElements = doc.select("div.c50-pilot");
					heroElements = heroElements.select("div.frame");
					Node heroNode = indexRightNode.hasNode("hero_large") ? indexRightNode
							.getNode("hero_large") : null;

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
											Element lightBoxElement = lightBoxElements.first();
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
					sb.append(Constants.EXCEPTOIN_IN_UPDATING_HERO_CONTENT);
					log.debug("Exception in updating hero content: ", e);
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
							Node selectorBarPanelNode = null;
							
							Elements h2TagText = ele.getElementsByTag("h2");
							if (h2TagText != null) {
								h2Text = h2TagText.html();
							} else {
								sb.append(Constants.SELECTOR_BAR_TITLE_NOT_AVAILABLE);
							}

							Element titleUrl = ele.getElementsByTag("h2")
									.select("a").first();
							if (titleUrl != null) {
								titleURL = titleUrl.absUrl("href");
								if(StringUtil.isBlank(titleURL)){
									titleURL =  titleUrl.attr("href");
								}
								// Start extracting valid href
								log.debug("Before selector bar title Url" + titleURL + "\n");
								titleURL = FrameworkUtils.getLocaleReference(titleURL, urlMap, locale, sb);
								log.debug("after selector bar title Url" + titleURL + "\n");
								// End extracting valid href
							} else {
								sb.append(Constants.SELECTOR_BAR_TITLE_URL_NOT_AVAILABLE);
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
								log.debug("Before AllLinkUrl" + aHref + "\n");
								aHref = FrameworkUtils.getLocaleReference(aHref, urlMap, locale, sb);
								log.debug("after AllLinkUrl" + aHref + "\n");
								// End extracting valid href
							} else {
								sb.append(Constants.SELECTOR_BAR_ALL_LINK_NOT_AVAILABLE);
							}

							log.debug("selector component titleUrl: "
									+ titleURL);
							if(selectorBarPanel.hasNext()){
								selectorBarPanelNode = (Node) selectorBarPanel
									.next();
							
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
										Element listItemAnchor = li
												.getElementsByTag("a").first();

										if (listItemAnchor != null) {
											String anchorText = listItemAnchor
													.text();
											String anchorHref = listItemAnchor
													.absUrl("href");
											if(StringUtil.isBlank(anchorHref)){
												anchorHref = listItemAnchor
														.attr("href");
											}
											// Start extracting valid href
											log.debug("Before selector bar li url" + anchorHref + "\n");
											anchorHref = FrameworkUtils.getLocaleReference(anchorHref, urlMap, locale, sb);
											log.debug("after selector bar li url" + anchorHref + "\n");
											// End extracting valid href
											jsonObj.put("linktext", anchorText);
											jsonObj.put("linkurl", anchorHref);
											list.add(jsonObj.toString());
										} else {
											sb.append(Constants.SELECTOR_BAR_ALL_LINK_NOT_AVAILABLE);
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
					}

						if (eleSize != nodeSize) {
							sb.append("<li>Selector Bar Component element size ( "
									+ eleSize
									+ " ) and node size ( "
									+ nodeSize + " ) mismatch</li>");
						}
					}

				} catch (Exception e) {
					sb.append(Constants.SELECTOR_BAR_COMPONENT_NOT_UPDATED);
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
										htmlblobElement, "", locale, sb,urlMap);
								htmlBlobContent.append(html);
							} else {
								log.debug(Constants.HTMLBLOB_ELEMENT_NOT_FOUND);
							}
						}
					}

					if (indexMidLeftNode.hasNode("htmlblob")) {
							Node htmlBlobNode = indexMidLeftNode.hasNode("htmlblob")?
									indexMidLeftNode.getNode("htmlblob"):null;
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
				// end set html blob component content.
				// --------------------------------------------------------------------------------------------------------------------------

				// text,linkurl.
				// ---------------------------------------------------------------------------------------------------------------------------------------
				// start set text component.
				try {
					Elements textElements = doc.select("div.c00-pilot");
					if (!textElements.isEmpty()) {
						Node textNode = indexMidLeftNode.hasNode("text") ? indexMidLeftNode
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
									sb.append(Constants.TEXT_DOES_NOT_EXIST);
								}

							
						}else {
							sb.append(Constants.TEXT_NODE_NOT_FOUND);
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

					Elements tileBorderedElements = doc.select("div.c23-pilot");
					if (indexRightRailNode != null) {
						log.debug("indexRightRailNode found: "
								+ indexRightRailNode.getPath());
						int eleSize = tileBorderedElements.size();
						NodeIterator tileBorderedNodeIterator = null;
							tileBorderedNodeIterator = indexRightRailNode
								.getNodes("tile_bordered*");
						int nodeSize = (int) tileBorderedNodeIterator.getSize();
						for (Element ele : tileBorderedElements) {
							Node spotLightComponentNode = null ;
							String h2Text = "";
							String pText = "";
							String aText = "";
							String aHref = "";
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
								sb.append(Constants.TILE_BORDERED_TITLE_ELEMENT_NOT_FOUND);
							}

							Elements descriptionText = ele
									.getElementsByTag("p");
							if (!descriptionText.isEmpty()) {
								pText = descriptionText.html();
							} else {
								if (!h3TagExists)
								sb.append(Constants.TILE_BORDERED_DESCRIPTION_NOT_FOUND);
							}
							
							/*Elements imgSrc = ele.getElementsByTag("img");
							boolean extraImageTagExists = false;
							if(!imgSrc.isEmpty()){
								extraImageTagExists = true;
							}
							
							if(extraImageTagExists){
								sb.append(Constants.EXTRA_IMAGE_TAG_FOUND);
							}*/
							Element anchorText = ele.getElementsByTag("a").first();
							String ownText = ele.ownText();
							log.debug("owntext:::" + ownText);
							if(h3TagExists){
								Element anchor = anchorText;
									aText = anchor.text() + ownText;
									aHref = anchor.absUrl("href");
									if(StringUtil.isBlank(aHref)){
										aHref = anchor.attr("href");
									}
									// Start extracting valid href
									log.debug("Before tile bordered url" + aHref + "\n");
									aHref = FrameworkUtils.getLocaleReference(aHref, urlMap, locale, sb);
									log.debug("after tile bordered url" + aHref + "\n");
									// End extracting valid href
									sb.append(Constants.EXTRA_URLS_FOUND_ON_TILE_BORDRED_COMPONENT);
							}
							else{
							if (anchorText!=null) {
								aText = anchorText.text() + ownText;
								aHref = anchorText.absUrl("href");
								if(StringUtil.isBlank(aHref)){
									aHref = anchorText.attr("href");
								}
								// Start extracting valid href
								log.debug("Before tile bordered url" + aHref + "\n");
								aHref = FrameworkUtils.getLocaleReference(aHref, urlMap, locale, sb);
								log.debug("after tile bordered url" + aHref + "\n");
								// End extracting valid href
							} else {
								sb.append(Constants.TILE_BORDERED_ANCHOR_ELEMENTS_NOT_FOUND);
							}
							}
							if(tileBorderedNodeIterator.hasNext()){
								 spotLightComponentNode = (Node) tileBorderedNodeIterator
										.next();
								

							if(StringUtils.isNotBlank(h2Text)){
								spotLightComponentNode.setProperty("title", h2Text);
							}
							if(StringUtils.isNotBlank(pText)){
								spotLightComponentNode.setProperty("description",
									pText);
							}
							if(StringUtils.isNotBlank(aText)){
								spotLightComponentNode.setProperty("linktext",
									aText);
							}
							if(StringUtils.isNotBlank(aHref)){
								spotLightComponentNode
									.setProperty("linkurl", aHref);
							}

							}	
						
						if (eleSize != nodeSize) {

							sb.append("<li>Could not migrate  tilebordered node. Count mis match as Element Count is "
									+ eleSize
									+ " and node count is "
									+ nodeSize + " </li>");
							log.debug("Could not migrate  tilebordered node. Count mis match");

						}
						}

				} 
					}catch (Exception e) {
					sb.append(Constants.TILE_BORDERED_COMPONENT_NOT_UPDATED);
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
							for (int count =0; count < childrenSize; count++) {
								Element child = listElements.child(count);
								if (child != null) {
									if ("h2".equalsIgnoreCase(child.tagName())) {
										headerList.add(child.text());
										if (!paraContent.isEmpty()) {
											//Report content comes here
											paraContent = "";
										}
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
								Elements ulItems = ulList.get(loop).select("li");
								for(Element li : ulItems){

									
										JSONObject jsonObj = new JSONObject();
										Element listItemAnchor = li
												.getElementsByTag("a").first();
										Elements listItemSpan = li
												.getElementsByTag("span");

										String anchorText = listItemAnchor != null ? listItemAnchor
												.text() : "";
										String anchorHref = listItemAnchor
												.absUrl("href");
										if(StringUtil.isBlank(anchorHref)){
											anchorHref = listItemAnchor.attr("href");
										}
										// Start extracting valid href
										log.debug("Before ListLinkUrl" + anchorHref + "\n");
										anchorHref = FrameworkUtils.getLocaleReference(anchorHref, urlMap, locale, sb);
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
									sb.append(Constants.LIST_ELEMENT_LIST_NODE_NOT_FOUND);
								}
							}


					}	}
					Element rightRailPilotElement = indexlistElem.first();
					if(!listItemFound){
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
									Element listItemAnchor = li
											.getElementsByTag("a").first();
									Elements listItemSpan = li
											.getElementsByTag("span");

									String anchorText = listItemAnchor != null ? listItemAnchor
											.text() : "";
									String anchorHref = listItemAnchor
											.absUrl("href");
									if(StringUtil.isBlank(anchorHref)){
										anchorHref = listItemAnchor.attr("href");
									}
									// Start extracting valid href
									log.debug("Before ListLinkUrl" + anchorHref + "\n");
									anchorHref = FrameworkUtils.getLocaleReference(anchorHref, urlMap, locale, sb);
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

						}

					}else {
						sb.append(Constants.LIST_ELEMENT_NOT_FOUND);
					}
						}
				} catch (Exception e) {
					sb.append(Constants.EXCEPTION_IN_UPDATING_LIST_COMPONENT);
					log.error("Exception : ", e);
				}
				// end set benefit list.

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
