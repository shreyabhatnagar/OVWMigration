package com.cisco.dse.global.migration.rproductlisting;

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

public class RProductListingVariation2 extends BaseAction{
	
	Document doc;


	StringBuilder sb = new StringBuilder(1024);

	String footerLinks = "{\"linktext\":\"<aaa>\",\"linkurl\":\"<bbb>\"}";

	static Logger log = Logger.getLogger(RProductListingVariation2.class);

	public String translate(String host,String loc, String prod, String type,
			String catType, String locale, Session session, Map<String, String> urlMap) throws IOException,
			ValueFormatException, VersionException, LockException,
			ConstraintViolationException, RepositoryException {
		BasicConfigurator.configure();
		log.debug("In the translate method");
		log.debug("In the translate method, catType is :" + catType);

		// Repo node paths
		String pagePropertiesPath = "/content/<locale>/"+catType+"/<prod>/product-listing/jcr:content";
		String gridFullNodePath = "/content/<locale>/"
				+ catType
				+ "/<prod>/product-listing/jcr:content/Grid/category/layout-category/full/Full";
		
		String gridNarrowWideNodePath = "/content/<locale>/"
				+ catType
				+ "/<prod>/product-listing/jcr:content/Grid/category/layout-category/narrowwide/NW-Wide-2";


		String pageUrl = host + "/content/<locale>/"
				+ catType + "/<prod>/product-listing.html";

		pageUrl = pageUrl.replace("<locale>", locale).replace("<prod>", prod);
		pagePropertiesPath = pagePropertiesPath.replace("<locale>", locale).replace("<prod>", prod);
		sb.append("<td>" + "<a href=" + pageUrl + ">" + pageUrl + "</a>"
				+ "</td>");
		sb.append("<td>" + "<a href=" + loc + ">" + loc + "</a>" + "</td>");
		sb.append("<td><ul>");

		gridFullNodePath = gridFullNodePath.replace("<locale>", locale).replace(
				"<prod>", prod);
		
		gridNarrowWideNodePath = gridNarrowWideNodePath.replace("<locale>", locale).replace(
				"<prod>", prod);
		Node gridNarrowWideNode = null;
		Node pageJcrNode = null;
		try {
			gridNarrowWideNode = session.getNode(gridNarrowWideNodePath);
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
				// start set text component.
				try {
					String h2TagVal = "";
					String pTagVal = "";
					Node textNodeOne = null;
					Node textNodeTwo = null;

					if(gridNarrowWideNode.hasNode("text")){
						textNodeOne = gridNarrowWideNode.getNode("text");
					}else{
						sb.append(Constants.TEXT_NODE_NOT_FOUND);

					}

					if(gridNarrowWideNode.hasNode("text_0")){
						textNodeTwo = gridNarrowWideNode.getNode("text_0");
					}else{
						sb.append(Constants.TEXT_NODE_NOT_FOUND);
					}
					
					Element firstTextElement = doc.select("div.c00v1-pilot").first();
					if(firstTextElement == null){
						firstTextElement = doc.select("div.cc00v1-pilot").first();
					}
					
					if (firstTextElement != null) {
						//Element eleh1 = 
						Element ele = firstTextElement.getElementsByTag("h1").first()!=null?firstTextElement.getElementsByTag("h1").first():firstTextElement.getElementsByTag("h2").first();
					//	Element ele = hElements.first();
						if (ele != null) {
							log.debug("text property element is !: " + ele);
								//fix for class header-1
								h2TagVal = ele.outerHtml();
								if(textNodeOne != null){
									textNodeOne.setProperty("text", h2TagVal);
								log.debug("h2TagVal property!: " + h2TagVal);
							} else {
								sb.append(Constants.CHILD_TEXT_ELEMENT_NOT_FOUND);
							}
						}
					} else {
						sb.append(Constants.TEXT_ELEMENT_NOT_FOUND);
					}

					Element pTagElements = doc.select("div.c00v1-pilot").last();
					
					if(pTagElements == null){
						pTagElements = doc.select("div.cc00v1-pilot").first();
					}
					
					if(pTagElements == null){
						pTagElements = doc.select("div.c00v0-pilot").first();
					}
					
					StringBuilder  paragraphBuilder = new StringBuilder();
					if(pTagElements != null){

						Elements pElements = pTagElements.select("p");
						
						
						if(!pElements.isEmpty()){
							for(Element ptagElement : pElements){
								log.debug("pTagText property!: " + ptagElement.text());
								pTagVal = FrameworkUtils.extractHtmlBlobContent(ptagElement, "", locale, sb, urlMap, catType, type);
								paragraphBuilder.append(pTagVal);
							}
							log.debug("paragraphBuilder.toString() is: "+paragraphBuilder.toString());
						
							if(textNodeTwo != null){
								textNodeTwo.setProperty("text", paragraphBuilder.toString());
							}

						}else{
							sb.append(Constants.CHILD_TEXT_ELEMENT_NOT_FOUND);
						}

						}else{

							sb.append(Constants.CHILD_TEXT_ELEMENT_NOT_FOUND);
						}
					
				} catch (Exception e) {
					sb.append(Constants.EXCEPTION_TEXT_COMPONENT
							);
					log.error("Exception in updating text component: ", e);
				}
				
				
				
				// ------------------------------------------------------------------------------------------------------------------------------------------
				// start set Hero Large component content.
			try {
				
				
				Elements drawerComponentHeaderElements = doc
						.select("div.c00v0-pilot");
				if (drawerComponentHeaderElements != null
						&& !drawerComponentHeaderElements.isEmpty()) {
					Element drawerComponentHeader = drawerComponentHeaderElements
							.first();
					if (drawerComponentHeader != null) {
						String drawerComponentHeaderTitle = "";
						Elements drawerComponentHeaderTitleElements = drawerComponentHeader
								.getElementsByTag("h2");

						if (!drawerComponentHeaderTitleElements.isEmpty()) {
							Element drawerComponentHeaderTitleElement = drawerComponentHeaderTitleElements
									.first();
							if (drawerComponentHeaderTitleElement != null) {
								drawerComponentHeaderTitle = drawerComponentHeaderTitleElement
										.text();
							} else {
								log.debug("<li>drawerComponent Header Title Element not found</li>");
							}
						} else {
							log.debug("<li>drawerComponent Header Title Element Section not found</li>");
						}
						if (gridNarrowWideNode.hasNode("header")) {
								Node drawersContainerHeaderNode = gridNarrowWideNode.getNode("header");
								log.debug("drawerComponentHeaderTitle "+ drawerComponentHeaderTitle + "\n");
								if (StringUtils.isNotBlank(drawerComponentHeaderTitle)) {
									drawersContainerHeaderNode.setProperty("title",	drawerComponentHeaderTitle);
								} else {
									sb.append(Constants.DRAWERCONTAINER_TITLE_NOT_FOUND);
								}
						}else{
							sb.append(Constants.HEADER_NODE_NOT_FOUND);
						}
					}
				}	
							if (gridNarrowWideNode.hasNodes()) {
								NodeIterator drawerPanelsIterator = gridNarrowWideNode
										.getNodes("container*");
								
								boolean htmlBlobNodeExists = false;
								Node htmlBlobNode = gridNarrowWideNode.hasNode("htmlblob")?gridNarrowWideNode.getNode("htmlblob"):null;
								NodeIterator htmlblobIterator = null;
								if(htmlBlobNode != null){
									htmlblobIterator = gridNarrowWideNode.getNodes("htmlblob*");
									htmlBlobNodeExists = true;
									//sb.append("<li> Extra english content found on WEM page. </li>");
								}
								Node drawersPanelNode = null;
								Elements drawersPanelElements = doc.select("div.n21,ul.n21");

								if (!drawersPanelElements.isEmpty()) {
									int count = 0;
									for (Element drawersPanelElement : drawersPanelElements) {
										
										
										String panelTitle = "";
										String panelDescription = "";
										Elements drawerPanelLiElements = drawersPanelElement
												.getElementsByTag("li");
										if (drawerPanelLiElements != null) {
											for (Element drawerPanelLiElement : drawerPanelLiElements) {
												boolean infoLinksMisMatchFlag = false;
												boolean linkUrlNotFoundFlag = false;
												boolean imageSrcNotFoundFlag = false;
												boolean isHtmlBlob = false;
												boolean infoLinksNotFound = false;
												Elements iconBlock = drawerPanelLiElement
														.select("div.series");
												if (iconBlock.isEmpty()) {
													log.debug("SERIES SIZE0");
													continue;
												}
												
												Elements seriesElements = drawerPanelLiElement
														.select("div.series");
												boolean misMatchFlag = true;
												Element seriesElement = seriesElements.first();
												if	(seriesElements != null)	{
												count = count + 1;
												
												if((count == 2 || count == 3) && htmlBlobNodeExists){
													isHtmlBlob = true;
													String htmContent = FrameworkUtils.extractHtmlBlobContent(drawerPanelLiElement, "", locale, sb, urlMap, catType, type);
													 log.debug("htmlblobl exists for : "+ gridNarrowWideNodePath);
													if(htmlblobIterator.hasNext()){
														Node htmlblobNode = (Node) htmlblobIterator.next();
														if (htmlblobNode != null) {
															log.debug("node path is : "+ htmlblobNode.getPath());
															String htmlContentPropertyValue = htmlblobNode.hasProperty("html")?htmlblobNode.getProperty("html").getString():"";
															String modifiedHtml = FrameworkUtils.updateHtmlBlobContent(htmContent, htmlContentPropertyValue, loc, sb);
															
															log.debug("modified html is "+ modifiedHtml);
															if (StringUtils
																	.isNotBlank(htmContent)) {
																htmlblobNode
																		.setProperty(
																				"html",
																				modifiedHtml);
															} else {
																sb.append(Constants.DRAWER_PANEL_TITLE_NOT_FOUND);
															}
														}
													}
													
													
												} 
												else{
													log.debug("count in else block" +  count);
												if (drawerPanelsIterator.hasNext()) {

													drawersPanelNode = (Node) drawerPanelsIterator
															.next();
												
													if (seriesElement != null) {
														Elements panelTitleElements = seriesElement
																.getElementsByTag("h3");
														if (!panelTitleElements.isEmpty()) {
															Element panelTitleElement = panelTitleElements.first();
															if (panelTitleElement != null) {
																/*Elements anchorTag = panelTitleElement.getElementsByTag("a");
																if (anchorTag.size() > 0) {
																	panelTitle = anchorTag.first().text();
																	linkUrl = anchorTag
																			.first()
																			.absUrl("href");
																	// Start extracting valid href
																	log.debug("Before linkUrl" + linkUrl + "\n");
																	linkUrl = FrameworkUtils.getLocaleReference(linkUrl, urlMap);
																	log.debug("after linkUrl" + linkUrl + "\n");
																	// End extracting valid href
																}*/
																
																	panelTitle = panelTitleElement.text();
																log.debug("panel title" + panelTitle);
															}else {
																log.debug("<li>drawer panel title element not found</li>");
															}
														} 
														Elements panelParaElements = seriesElement
																.getElementsByTag("p");
														if (!panelParaElements.isEmpty()) {
															Element panelDescriptionElement = panelParaElements
																	.first();
															panelDescription = panelDescriptionElement
																	.text();
														} else {
															log.debug("<li>drawer panel para element not found</li>");
														}
														
														// start image
														String drawerImage = FrameworkUtils
																.extractImagePath(
																		seriesElement,
																		sb);
														log.debug("drawerImage "
																+ drawerImage
																+ "\n");
														if (drawersPanelNode != null) {
															Node drawersImageNode = drawersPanelNode
																	.getNode("image");
															Node drawersOverviewNode = drawersPanelNode.hasNode("overview")?
																	drawersPanelNode.getNode("overview"):null;
																	if(drawersOverviewNode!=null){
																		if(drawersOverviewNode.hasProperty("linktext")){
																			sb.append(Constants.EXTRA_OVERVIEW_NODE_FOUND+" for "+ panelTitle+" Drawer");
																		}
																	}
															String fileReference = drawersImageNode
																	.hasProperty("fileReference") ? drawersImageNode
																	.getProperty(
																			"fileReference")
																	.getString()
																	: "";
															drawerImage = FrameworkUtils
																	.migrateDAMContent(
																			drawerImage,
																			fileReference,
																			locale,
																			sb, catType, type);
															log.debug("drawerImage "
																	+ drawerImage
																	+ "\n");
															
															if (StringUtils
																	.isNotBlank(drawerImage)) {
																drawersImageNode
																		.setProperty(
																				"fileReference",
																				drawerImage);
															}
															else{
																imageSrcNotFoundFlag = true;
															}
														}
														// end image
														if (drawersPanelNode != null) {
															log.debug("node path is : "+ drawersPanelNode.getPath());
															if (StringUtils
																	.isNotBlank(panelTitle)) {
																drawersPanelNode
																		.setProperty(
																				"title",
																				panelTitle);
															} else {
																sb.append(Constants.DRAWER_PANEL_TITLE_NOT_FOUND);
															}
															
															if (StringUtils
																	.isNotBlank(panelDescription)) {
																drawersPanelNode
																		.setProperty(
																				"description",
																				panelDescription);
															} else {
																sb.append(Constants.DRAWER_PANEL_DESC_NOT_FOUND+" for "+ panelTitle+" Drawer");
															}
														}
													}
												}} }

												// selecting sub drawer
												// elements from document
												if(!isHtmlBlob){
												NodeIterator subDrawerIterator = drawersPanelNode
														.getNode(
																"subdrawer_parsys")
														.getNodes(
																"content_product*");
												Node subdrawerpanel = null;
												Elements subDrawerColl = drawerPanelLiElement
														.select("ul.items");
												Elements clearfixdivs = drawerPanelLiElement
														.select("li.clearfix");
												for(Element cFix : clearfixdivs){
													Element div = cFix.getElementsByTag("div").first();
													if(div.hasAttr("align")){
														sb.append(Constants.EXTRA_LINK_IN_SUBDRAWER+" of"+panelTitle+" Drawer");
													}
												}

												for (Element ss : subDrawerColl) {

													String title = "";
													String linkTitleUrl = "";

													Elements subItems = ss
															.select("div.prodinfo");

													if (!subItems.isEmpty()) {

														for (Element subItem : subItems) {
															if ((clearfixdivs
																	.size() != subDrawerIterator
																	.getSize())) {
																misMatchFlag = false;
															}
															boolean isMultiple = false;
															List<String> list1 = new ArrayList<String>();
															List<String> list2 = new ArrayList<String>();
															List<String> list3 = new ArrayList<String>();
															if (subDrawerIterator
																	.hasNext()) {
																subdrawerpanel = subDrawerIterator
																		.nextNode();
															}
															if (subItem != null) {

																Elements siTitles = subItem
																		.getElementsByTag("h4");
																
																if (siTitles != null) {
																	Element siTitle = siTitles
																			.first();
																	if (siTitle != null) {
																		Elements siATitles = siTitle
																				.getElementsByTag("a");
																		Element span =siTitle.getElementsByTag("span").first();
																		if (siATitles != null) {
																			Element siATitle = siATitles
																					.first();
																			if (siATitle != null) {
																				log.debug("Sub Series Title:::::::::::::"
																						+ siTitle
																								.outerHtml());
																				log.debug("siATitle.text() "
																						+ siATitle
																								.text()
																						+ "\n");
																				log.debug("siATitle.text() "
																						+ siATitle
																								.attr("href")
																						+ "\n");
																				if(siATitles.size() > 1){
																					isMultiple = true;
																					title = siTitle.outerHtml();
																				}else{
																				title = siATitle.text();
																				linkTitleUrl = siATitle
																						.absUrl("href");
																				if(StringUtil.isBlank(linkTitleUrl)){
																					linkTitleUrl = siATitle.absUrl("href");
																				}
																				log.debug("linkTitleUrl: "+linkTitleUrl);
																				}
																				if(span!=null){
																					title = title+" "+span.outerHtml();
																					
																				}
																				
																				// Start extracting valid href
																				log.debug("Before linkTitleUrl" + linkTitleUrl + "\n");
																				linkTitleUrl = FrameworkUtils.getLocaleReference(linkTitleUrl, urlMap, locale, sb, catType, type);
																				log.debug("after linkTitleUrl" + linkTitleUrl + "\n");
																				// End extracting valid href
																			} else {
																				title = siTitle
																						.outerHtml();
																				log.debug("<li>sub series title Element anchor not found</li>" + title);
																			}
																		} else {
																			log.debug("<li>sub series title Element anchor Section not found</li>");
																		}
																	} else {
																		log.debug("<li>sub series title Element not found</li>");
																	}

																} else {
																	log.debug("<li>sub series title Elements section not found</li>");
																}
																// start
																// image
																String subDrawerImage = FrameworkUtils
																		.extractImagePath(
																				subItem,
																				sb);
																log.debug("subDrawerImage before migration : "
																		+ subDrawerImage
																		+ "\n");
																if (subdrawerpanel != null) {
																	Node subDrawersImageNode = subdrawerpanel
																			.getNode("image");
																	String fileReference = subDrawersImageNode
																			.hasProperty("fileReference") ? subDrawersImageNode
																			.getProperty(
																					"fileReference")
																			.getString()
																			: "";
																	subDrawerImage = FrameworkUtils
																			.migrateDAMContent(
																					subDrawerImage,
																					fileReference,
																					locale,
																					sb, catType, type);
																	log.debug("subDrawerImage after migration : "
																			+ subDrawerImage
																			+ "\n");
																	if (StringUtils
																			.isNotBlank(subDrawerImage)) {
																		subDrawersImageNode
																				.setProperty(
																						"fileReference",
																						subDrawerImage);
																	}else{
																		imageSrcNotFoundFlag = true;
																	}
																}
																// end image

																Elements indDetailsElements = subItem
																		.select("ul.details");
																if (indDetailsElements != null) {
																	Element indDetailsElement = indDetailsElements
																			.first();
																	if (indDetailsElement != null) {
																		Elements indItems = indDetailsElement
																				.getElementsByTag("li");
																		if (indItems != null) {
																			for (Element indItem : indItems) {
																				list1.add(indItem
																						.html());
																			}
																		} else {
																			log.debug("<li>li elements in details Element not found</li>");
																		}
																	} else {
																		log.debug("<li>details Element not found</li>");
																	}
																} else {
																	log.debug("<li>details Element section not found</li>");
																}

																Element subItemUlInfoLink = subItem.parent().children().last();
																		//.siblingElements()
																		//.first(); // subItemUlInfoLinks.first();
																log.debug("Info Links Elements -----------"
																		+ subItemUlInfoLink);
																log.debug("--------------------------------");
																if (subItemUlInfoLink != null) {
																	Elements subItemInfoLinks = subItemUlInfoLink
																			.getElementsByTag("li");
																	if(subItemInfoLinks != null){
																	for (Element si : subItemInfoLinks) {
																		JSONObject jsonObj = new JSONObject();
																		log.debug("\t\t FeatureSubInfoLinks Text :::::::::::::::"
																						+ si.text());
																		log.debug("\t\t FeatureSubInfoLinks Text :::::::::::::::"
																				+ si.text());

																		String linkText = "";
																		String linkTextUrl = "";
																		Elements linkTextElements = si
																				.getElementsByTag("a");
																		if (linkTextElements != null) {
																			Element linkTextElement = linkTextElements
																					.first();
																			if (linkTextElement != null) {
																				linkText = linkTextElement
																						.text();
																				linkTextUrl = linkTextElement
																						.absUrl("href");
																				if(StringUtil.isBlank(linkTextUrl)){
																					linkTextUrl = linkTextElement.absUrl("href");
																				}
																				// Start extracting valid href
																				log.debug("Before linkTextUrl" + linkTextUrl + "\n");
																				linkTextUrl = FrameworkUtils.getLocaleReference(linkTextUrl, urlMap, locale, sb, catType, type);
																				log.debug("after linkTextUrl" + linkTextUrl + "\n");
																				// End extracting valid href
																			
																			} else {
																				log.debug("<li>info links anchor element not found</li>");
																			}
																		} else {
																			log.debug("<li>info links anchor element section not found</li>");
																		}
																		if (StringUtils
																				.isNotBlank(linkText)) {
																			jsonObj.put(
																					"linktext",
																					linkText);
																			list2.add(linkText);
																		}
																		if (StringUtils
																				.isNotBlank(linkTextUrl)) {
																			list3.add(linkTextUrl);

																		}
																	}
																	}
																	log.debug("list2.size()"
																			+ list2.size());

																}
															}
															if (subdrawerpanel != null) {
																Node titleLinkNode = subdrawerpanel.hasNode("titlelink")?subdrawerpanel.getNode("titlelink"):null;
																if (StringUtils
																		.isNotBlank(title)) {
																	subdrawerpanel
																			.setProperty(
																					"title",
																					title);
																	if(isMultiple){
																		log.debug("anchor tags are multiplee ************************");
																		titleLinkNode.setProperty("url", "");
																	}
																} else {
																	sb.append(Constants.TITLE_SUB_DRAWER_NOT_FOUND+" For "+ panelTitle.trim());
																}
																if (StringUtils
																		.isNotBlank(linkTitleUrl) && titleLinkNode!=null) {
																	if(!isMultiple){
																	titleLinkNode
																			.setProperty(
																					"url",
																					linkTitleUrl);
																	titleLinkNode.setProperty("linktype", "Url");
																	log.debug("anchor tags are NOTTTTT multiplee ************************");
																	}
																} else {
																	if(!isMultiple && titleLinkNode.hasProperty("url")){
																		linkUrlNotFoundFlag = true;
																		log.debug("linkurl property is not set at "
																				+ subdrawerpanel
																						.getPath());
																		log.debug("anchor tags are/not multiplee while settingg inside ifff");
																	}
																	log.debug("anchor tags are/not multiplee while settingg");
																}
																if (list1
																		.size() > 0) {

																	if (subdrawerpanel
																			.hasProperty("description")) {
																		Property p = subdrawerpanel
																				.getProperty("description");
																		p.remove();
																		session.save();
																	}
																	subdrawerpanel
																			.setProperty(
																					"description",
																					list1.toArray(new String[list1
																							.size()]));

																} else {
																	sb.append(Constants.HIGHLIGHTS_OF_SUB_DRAWER_NOT_FOUND+" For "+title.trim());
																}
																NodeIterator subdraweritems = null;
																Node subDrawerItemLink = null;
																
																	Node subdrawerlinks = subdrawerpanel
																			.hasNode("links") ? subdrawerpanel
																			.getNode("links")
																			: null;

																	if (subdrawerlinks != null) {
																		subdraweritems = subdrawerlinks
																				.hasNodes() ? subdrawerlinks
																				.getNodes("item*")
																				: null;
																	if (list2.size() > 0) {		
																		for (int loop = 0; loop < list2
																				.size(); loop++) {
																			log.debug("Loop "
																					+ loop);
																			if (subdraweritems
																					.hasNext()) {
																				Node subDrawerinfo = subdraweritems
																						.nextNode();
																					subDrawerItemLink = subDrawerinfo
																						.hasNode("link") ? subDrawerinfo
																						.getNode("link")
																						: null;
																				if (subDrawerItemLink
																							.hasProperty("linktext")) {
																						Property p = subDrawerItemLink
																								.getProperty("linktext");
																						p.remove();
																						session.save();
																					}
																				if (subDrawerItemLink
																						.hasProperty("url")) {
																					Property url = subDrawerItemLink
																							.getProperty("url");
																					url.remove();
																					session.save();
																				}
																				subDrawerItemLink
																						.setProperty(
																								"linktext",
																								list2.get(loop));
																				subDrawerItemLink
																						.setProperty(
																								"url",
																								list3.get(loop));

																			}

																		}
																		if(subdraweritems != null && list2 != null){
																			if (list2
																				.size() != subdraweritems
																				.getSize()) {
																			infoLinksMisMatchFlag =  true;
																		}
																		
																		}
																		
																		}
																	else if(subdraweritems != null && subdraweritems.getSize() > 0){
																		Node subDrawerItem = (Node) subdraweritems.next();
																		subDrawerItemLink = subDrawerItem.getNode("link");
																		if(subDrawerItemLink.hasProperty("url") && subDrawerItemLink.hasProperty("linktext")){
																			infoLinksNotFound = true;
																		}
																	}
																	
																} 
																
															} else {
																misMatchFlag = false;
															}
														}

													}

												}
												
												if (!misMatchFlag) {
													sb.append(Constants.MIS_MATCH_IN_SUB_DRAWER_PANEL_COUNT+" "+panelTitle);
												}
												
												if(infoLinksMisMatchFlag){
													sb.append(Constants.MISMATCH_IN_INFOLINKS+" "+panelTitle);
												}
												if(linkUrlNotFoundFlag){
													sb.append(Constants.LINK_URL_OF_SUB_DRAWER_NOT_FOUND+" "+panelTitle);
												}
												if(imageSrcNotFoundFlag){
													sb.append(Constants.IMAGE_NOT_FOUND_IN_LOCALE_PAGE+" "+panelTitle);
												}
												if(infoLinksNotFound){
													sb.append(Constants.INFO_LINKS_OF_SUB_DRAWER_NOT_FOUND+" "+panelTitle);
												}
												
												}

										}
									}
									if (htmlblobIterator != null && count != drawerPanelsIterator
											.getSize()+htmlblobIterator.getSize())
										sb.append(Constants.MIS_MATCH_IN_DRAWER_PANEL_COUNT);

									// end new code

								} 
									}else {
									log.debug("<li>drawer panel elements section not found</li>");
									sb.append(Constants.DRAWER_PANEL_ELEMENTS_NOT_FOUND);
								}
							} else {
								log.debug("<li>drawers_container node is not found</li>");
							}
				
			} catch (Exception e) {
				log.debug("<li>Unable to update drawers_container component."
						+ e + "</li>");
				log.error("Exception", e);
			}

			// end set drawers_container component content.
			// --------------------------------------------------------------------------------------------------------------------------

				
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
}
