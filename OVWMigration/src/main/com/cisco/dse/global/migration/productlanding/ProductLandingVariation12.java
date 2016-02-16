package com.cisco.dse.global.migration.productlanding;

/* 
 * S.No     	Name                 Date                    Description of change
 *  #1         Saroja            15-Dec-15           Added the Java file to handle the migration of product landing variation 12 page(s).
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

public class ProductLandingVariation12 extends BaseAction {

	Document doc;
	StringBuilder sb = new StringBuilder(1024);

	String footerLinks = "{\"linktext\":\"<aaa>\",\"linkurl\":\"<bbb>\"}";

	static Logger log = Logger.getLogger(ProductLandingVariation12.class);

	public String translate(String host, String loc, String prod, String type,
			String catType, String locale, Session session,Map<String,String> urlMap) throws IOException,
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
					Node heroNode = indexLeftNode.hasNode("hero_large") ? indexLeftNode.getNode("hero_large") : null;
					Value[] panelPropertiest = null;
					Property panelNodesProperty = heroNode.hasProperty("panelNodes")?heroNode.getProperty("panelNodes"):null;
					if(panelNodesProperty.isMultiple()){
						panelPropertiest = panelNodesProperty.getValues();
					}else{
						panelPropertiest = new Value[1];
						panelPropertiest[0] = panelNodesProperty.getValue();
					}

					if (heroNode != null) {
						log.debug("heronode found: " + heroNode.getPath());
						if (heroElements != null) {
							int eleSize = heroElements.size();

							NodeIterator heroPanelNodeIterator = heroNode.getNodes("heropanel*");
							
							int nodeSize = (int) heroPanelNodeIterator.getSize();
							int i = 0;
							int imageSrcEmptyCount = 0;
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
								if (heroPanelNodeIterator.hasNext()) {
									if(panelPropertiest != null && i<panelPropertiest.length){
										String propertyVal = panelPropertiest[i].getString();
										if(StringUtils.isNotBlank(propertyVal)){
											JSONObject jsonObj = new JSONObject(propertyVal);
											if(jsonObj.has("panelnode")){
												String panelNodeProperty = jsonObj.get("panelnode").toString();
												heroPanelNode = heroNode.hasNode(panelNodeProperty)?heroNode.getNode(panelNodeProperty):null;
											}
										}
										i++;
									}/*else{
										sb.append("<li>No heropanel Node found.</li>");
									}*/
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
															locale,sb);
											log.debug("heroImage " + heroImage
													+ "\n");
											if (StringUtils
													.isNotBlank(heroImage)) {
												imageNode.setProperty(
														"fileReference",
														heroImage);
											}else{
												imageSrcEmptyCount++;
											}
										} else {
											sb.append(Constants.HERO_IMAGE_NODE_NOT_FOUND);
										}
										//start of hero pop up
										Node heroPanelPopUpNode = null;
										Element lightBoxElement = null;
										Elements lightBoxElements = ele.select("div.c50-text").select("a.c26v4-lightbox");
										if(lightBoxElements != null && !lightBoxElements.isEmpty()){
											lightBoxElement = lightBoxElements.first();
										}
										heroPanelPopUpNode = FrameworkUtils.getHeroPopUpNode(heroPanelNode);
										if (heroPanelPopUpNode == null && lightBoxElement != null) {
											sb.append("<li>video pop up is present in WEB page but it is not present in WEM page.</li>");
										}
										if (heroPanelPopUpNode != null && lightBoxElement == null) {
											sb.append("<li>video pop up is present in WEM page but it is not present in WEB page.</li>");
										}
										if (heroPanelPopUpNode != null && lightBoxElement != null && StringUtils.isNotBlank(h2Text)) {
											heroPanelPopUpNode.setProperty("popupHeader", h2Text);
										}
										//end of hero pop up
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
						sb.append(Constants.HERO_CONTENT_NODE_NOT_FOUND);

					}

				} catch (Exception e) {
					sb.append(Constants.EXCEPTOIN_IN_UPDATING_HERO_CONTENT);
					log.debug(" Hero Component Exception: ", e);
				}

				// ---------------------------------------------------------------------------------------------------------------------------------------
				// start set selectorbar large component properties.

				try {
					/*Elements flagDivElements = doc.select("body.cdc-fw").select("div.ciscoFlagDiv");
					int falgDivSize = flagDivElements.size();
					log.debug("falgDivSize:***************** "+ falgDivSize);
					if(!flagDivElements.isEmpty()){
						sb.append("<li> Flag symbol upon on hover of selector bar links cannot be migrated as there is no corresponding proeporty. </li>");
					}*/
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
						Node selectorBarPanelNode = null;
						int nodeSize = (int) selectorBarPanel.getSize();
						for (Element ele : selectorBarLargeElements) {
							String h2Text = "";
							String titleURL = "";
							String aText = "";
							String aHref = "";
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
									titleURL = titleUrl.attr("href");
								}
								// Start extracting valid href
								log.debug("Before selectorbartitleLinkUrl" + titleURL + "\n");
								titleURL = FrameworkUtils.getLocaleReference(titleURL, urlMap, locale, sb);
								log.debug("after selectorbartitleLinkUrl" + titleURL + "\n");
								// End extracting valid href
							} else {
								sb.append(Constants.SELECTOR_BAR_TITLE_URL_NOT_AVAILABLE);
							}
							Element menuelement = null;
							if(ele.parent().hasClass("c58v1-pilot")){
								 menuelement = ele.child(0).child(1);
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
								aHref = allLinkTag.absUrl("href");
								if(StringUtil.isBlank(aHref)){
									aHref = allLinkTag.attr("href");
								}
								// Start extracting valid href
								log.debug("Before selectorBarMenuLinkUrl" + aHref + "\n");
								aHref = FrameworkUtils.getLocaleReference(aHref, urlMap, locale, sb);
								log.debug("after selectorBarMenuLinkUrl" + aHref + "\n");
								// End extracting valid href
							} else {
								sb.append(Constants.SELECTOR_BAR_ALL_LINK_NOT_AVAILABLE);
							}
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

							Elements menuUlList = menuelement
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
												anchorHref = listItemAnchor.attr("href");
											}
											// Start extracting valid href
											log.debug("Before listItemLinkUrl" + anchorHref + "\n");
											anchorHref = FrameworkUtils.getLocaleReference(anchorHref, urlMap, locale, sb);
											log.debug("after listItemLinkUrl" + anchorHref + "\n");
											// End extracting valid href
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
													locale, sb,urlMap);
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
							Node spotLightComponentNode = null;
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

							Element anchorText = ele.getElementsByTag("a").first();
							if (anchorText != null) {
								aText = anchorText.text();
								aHref = anchorText.absUrl("href");
								if(StringUtil.isBlank(aHref)){
									aHref = anchorText.attr("href");
								}
								// Start extracting valid href
								log.debug("Before tileBorderedLinkUrl" + aHref + "\n");
								aHref = FrameworkUtils.getLocaleReference(aHref, urlMap, locale, sb);
								log.debug("after tileBorderedLinkUrl" + aHref + "\n");
								// End extracting valid href
							} else {
								sb.append(Constants.TILE_BORDERED_ANCHOR_NOT_FOUND);
							}
							if(tileBorderedNodeIterator.hasNext()){
								spotLightComponentNode = (Node) tileBorderedNodeIterator
										.next();
								
							spotLightComponentNode.setProperty("title", h2Text);
							spotLightComponentNode.setProperty("description",
									pText);
							spotLightComponentNode.setProperty("linktext",
									aText);
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
						sb.append(Constants.EXTRA_LIST_COMPONENT_FOUND);
						
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
							int imageSrcEmptyCount = 0;
							for (Element ele : spotLightElements) {
								Node spotLightComponentNode = null;
								String h2Text = "";
								String pText = "";
								String aText = "";
								String aHref = "";
								Elements h2TagText = ele.getElementsByTag("h2");
								if (h2TagText != null) {
									h2Text = h2TagText.text();
								} else {
									sb.append(Constants.SPOTLIGHT_HEADING_TEXT_NOT_FOUND);
								}
								
								
								Elements descriptionText = ele
										.getElementsByTag("p");
								
								if (descriptionText != null) {
									pText = descriptionText.first().html();
								} else {
									sb.append(Constants.SPOTLIGHT_DESCRIPTION_ELEMENT_NOT_FOUND);
								}

								String ownText = "";
								Element anchorText = ele.getElementsByTag("a").first();
								ownText = ele.ownText();
								if (anchorText != null) {
									aText = anchorText.text()+ownText;
									aHref = anchorText.absUrl("href");
									if(StringUtil.isBlank(aHref)){
										aHref = anchorText.attr("href");
									}
									// Start extracting valid href
									log.debug("Before spotlightLinkUrl" + aHref + "\n");
									aHref = FrameworkUtils.getLocaleReference(aHref, urlMap, locale, sb);
									log.debug("after spotlightLinkUrl" + aHref + "\n");
									// End extracting valid href
								} else {
									sb.append(Constants.SPOTLIGHT_ANCHOR_ELEMENT_NOT_FOUND);
								}
								// start image
								String spotLightImage = FrameworkUtils.extractImagePath(ele, sb);
								log.debug("spotLightImage " + spotLightImage + "\n");
								if(spoLightNodeIterator.hasNext()){
									spotLightComponentNode = (Node) spoLightNodeIterator
										.next();
								
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
							}
							if(imageSrcEmptyCount > 0){
								sb.append("<li> "+imageSrcEmptyCount+" image(s) are not found on spot light component of locale page. </li>");
														}
							if (nodeSize != eleSize) {
								sb.append("<li>Could not migrate  SpotLight node. Count mis match as Element Count is "
										+ eleSize
										+ " and node count is "
										+ nodeSize + " </li>");

							}
						}

					} else {
						sb.append(Constants.EXCEPTION_SPOTLIGHT_COMPONENT);

					}
				} catch (Exception e) {
					sb.append(Constants.EXCEPTION_SPOTLIGHT_COMPONENT);
				}
				// end set spotlight nodes

				// start set ENTERPRISE NETWORK INDEX list.
				if(!primaryCtaExists){
					try {
					Elements indexlistElem = doc.select("div.n13-pilot");
					Element rightRailPilotElement = indexlistElem.first();
					if (rightRailPilotElement != null) {
							String indexTitle = rightRailPilotElement.getElementsByTag(
									"h2").text();

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
									Element listItemAnchor = li
											.getElementsByTag("a").first();
									Elements listItemSpan = li
											.getElementsByTag("span");

									String anchorText = listItemAnchor != null ? listItemAnchor
											.text() : "";
									String anchorHref = listItemAnchor != null ? listItemAnchor
											.absUrl("href"): "";
									if( listItemAnchor != null){
										if(StringUtil.isBlank(anchorHref)){
									
										anchorHref = listItemAnchor.attr("href");
									}
									// Start extracting valid href
									log.debug("Before rightPilotLinkUrl" + anchorHref + "\n");
									anchorHref = FrameworkUtils.getLocaleReference(anchorHref, urlMap, locale, sb);
									log.debug("after rightPilotLinkUrl" + anchorHref + "\n");
									// End extracting valid href
									String anchorTarget = listItemAnchor
											.attr("target");
									String listIcon = listItemSpan
											.attr("class");
									// String icon = li.ownText();
									if (StringUtils.isBlank(anchorText)) {
										anchorText = li.text();
									}
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
								
							}
								//Node listNode = null;
								NodeIterator elementList = null;
								Node listNode = indexRightNode.hasNode("list")?
										indexRightNode.getNode("list"):null;
									if (listNode != null) {
										log.debug("path of list node in right index node: "
												+ listNode.getPath());
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
													sb.append(Constants.LIST_NODE_NOT_FOUND);
												}
														
											}
										} 

									}else{
										sb.append(Constants.LIST_NODE_NOT_FOUND);
									}
					} else {
						sb.append(Constants.LIST_ELEMENT_NOT_FOUND);
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
										htmlblobElement, "", locale, sb,urlMap);
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
				
				
				// start set Service provider right rail list list.
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
									boolean openNewWindow = false;
									
									String pdfIcon = null;
									String pdf = li.ownText().trim();
									log.debug("pdf text is: "+ pdf);
									try{
										
										log.debug(pdf);
										if (pdf.length() > 0) {
											if (pdf.toLowerCase().contains("pdf"))
												pdfIcon = "pdf";
											log.debug("pdfIcon text is: "+ pdfIcon);
											int i = 0;
											for (; i < pdf.length(); i++) {
												char character = pdf.charAt(i);
												boolean isDigit = Character.isDigit(character);
												if (isDigit) {
													break;
												}
											}
											pdf = pdf.substring(i, pdf.length() - 1);
										}
										pdf = pdf.trim();
										log.debug("final pdf text is: "+ pdf);
										// end pdf
									}catch(Exception e){
										sb.append(Constants.Exception_BY_SPECIAL_CHARACTER);
										log.error("Exception : ",e);
									}
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
									log.debug("Before indexListLinkUrl" + anchorHref + "\n");
									anchorHref = FrameworkUtils.getLocaleReference(anchorHref, urlMap, locale, sb);
									log.debug("after indexListLinkUrl" + anchorHref + "\n");
									// End extracting valid href
									String anchorTarget = listItemAnchor
											.attr("target");
									String listIcon = listItemSpan
											.attr("class");
									// String icon = li.ownText();

									jsonObj.put("linktext", anchorText);
									jsonObj.put("linkurl", anchorHref);
									jsonObj.put("icon", pdfIcon);
									jsonObj.put("size", pdf);// Need to get the
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
											sb.append(Constants.LIST_NODE_NOT_FOUND);
										}

									}
								}
							}
							eleSize = childElements.size();

						}
							

						}
						if (eleSize > 1 && eleSize != listNodeSize) {

							sb.append("<li>Could not migrate List node. Count mis match as Element Count is "
									+ eleSize
									+ " and node count is "
									+ listNodeSize + " </li>");
							log.debug("Could not migrate  tilebordered node. Count mis match");

						}
						} else {
						sb.append(Constants.LIST_NODE_NOT_FOUND);
					}
				} catch (Exception e) {
					sb.append(Constants.EXCEPTION_IN_UPDATING_LIST_COMPONENT);
					log.error("Exception : ", e);
				}
				// end set benefit list.

				
				try{
					Elements extraTextComponent = doc.select("div.c00v0-pilot");
					if(!extraTextComponent.isEmpty()){
						sb.append(Constants.EXTRA_TEXT_ELEMENT_FOUND);
					}
				}catch(Exception e){
					sb.append(Constants.EXCEPTION_TEXT_COMPONENT);
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
									String href = aElement.absUrl("href");
									if(StringUtil.isBlank(href)){
										href = aElement.attr("href");
									}
									// Start extracting valid href
									log.debug("Before followusLinkUrl" + href + "\n");
									href = FrameworkUtils.getLocaleReference(href, urlMap, locale, sb);
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
