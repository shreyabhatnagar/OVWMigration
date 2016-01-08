package com.cisco.dse.global.migration.rproductlanding;

import java.io.IOException;
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
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.cisco.dse.global.migration.config.BaseAction;
import com.cisco.dse.global.migration.config.Constants;
import com.cisco.dse.global.migration.config.FrameworkUtils;

public class RProductLandingVariation2 extends BaseAction {

	Document doc;

	StringBuilder sb = new StringBuilder(1024);
	static Logger log = Logger.getLogger(RProductLandingVariation2.class);

	public String translate(String host, String loc, String prod, String type, String catType,
			String locale, Session session, Map<String, String> urlMap) throws IOException,
			ValueFormatException, VersionException, LockException,
			ConstraintViolationException, RepositoryException {
		BasicConfigurator.configure();
		log.debug("in translare method of RProductLandingVariation2");
		// Repo node paths
		try {
			String pagePropertiesPath = "/content/<locale>/"+catType+"/<prod>/index/jcr:content/";
			String pageUrl = host + "/content/<locale>/"+catType+"/<prod>/index.html";

			pageUrl = pageUrl.replace("<locale>", locale).replace("<prod>", prod);
			pagePropertiesPath = pagePropertiesPath.replace("<locale>", locale).replace("<prod>", prod);

			String indexLeft = pagePropertiesPath+"Grid/category/layout-category/widenarrow/WN-Wide-1";
			//			/content/en/us/products/storage-networking/index/jcr:content/Grid/category/layout-category/widenarrow/WN-Wide-1
			String indexRight = pagePropertiesPath+"Grid/category/layout-category/widenarrow/WN-Narrow-2";
			//			Grid/category/layout-category/widenarrow/WN-Narrow-2

			String indexUpperRight = "/content/<locale>/"
					+ catType
					+ "/<prod>/index/jcr:content/Grid/category/layout-category/widenarrow/WN-Wide-1/carousel";

			log.debug("Path is "+indexLeft);
			log.debug("Path is "+indexRight);

			sb.append("<td>" + "<a href="+pageUrl+">"+pageUrl+"</a>"+"</td>");
			sb.append("<td>" + "<a href="+loc+">"+loc +"</a>"+ "</td>");
			sb.append("<td><ul>");

			indexLeft = indexLeft.replace("<locale>", locale).replace("<prod>", prod);
			indexRight = indexRight.replace("<locale>", locale).replace("<prod>", prod);
			indexUpperRight = indexUpperRight.replace("<locale>", locale).replace("<prod>", prod);

			javax.jcr.Node indexLeftNode = null;
			javax.jcr.Node indexRightNode = null;
			javax.jcr.Node pageJcrNode = null;
			javax.jcr.Node indexUpperRightNode = null;

			indexLeftNode = session.getNode(indexLeft);
			indexRightNode = session.getNode(indexRight);
			indexUpperRightNode = session.getNode(indexUpperRight);
			pageJcrNode = session.getNode(pagePropertiesPath);

			try {
				doc = getConnection(loc);
				if(doc!=null){

					// ------------------------------------------------------------------------------------------------------------------------------------------
					// start set page properties.

					FrameworkUtils.setPageProperties(pageJcrNode, doc, session, sb);

					// end set page properties.
					// ------------------------------------------------------------------------------------------------------------------------------------------

					//-------------------------------- start gd-left -------------------// 

					// start set Hero Large component content.
					try {
						javax.jcr.Node heroLargeNode = null;
						NodeIterator heroPanelIterator = null;
						if (indexUpperRightNode.hasNode("carouselContents")) {
							heroLargeNode = indexUpperRightNode
									.getNode("carouselContents");
							if (heroLargeNode.hasNodes()) {
								heroPanelIterator = heroLargeNode
										.getNodes("hero_panel*");
							}
						} else {
							log.debug("<li>Node with name 'carouselContents' doesn't exist under "
									+ indexUpperRightNode.getPath() + "</li>");
						}

						Elements heroLargeElements = doc.select("div.c50-pilot");
						if (heroLargeElements != null) {
							Element heroLargeElement = heroLargeElements.first();
							if (heroLargeElement != null) {
								Elements heroLargeFrameElements = heroLargeElement
										.select("div.frame");
								javax.jcr.Node heroPanelNode = null;

								if (heroLargeFrameElements != null) {
									if (heroLargeFrameElements.size() != heroLargeNode
											.getNodes("hero_panel*").getSize()) {
										sb.append(Constants.MISMATCH_IN_HERO_SLIDES);
									}
									int noImageCount = 0; 
									for (Element ele : heroLargeFrameElements) {
										String heroPanelTitle = "";
										String heroPanelDescription = "";
										String heroPanelLinkText = "";
										String heroPanellinkUrl = "";
										Elements heroTitleElements = ele
												.getElementsByTag("h2");
										if (heroTitleElements != null) {
											Element heroTitleElement = heroTitleElements
													.first();
											if (heroTitleElement != null) {
												heroPanelTitle = heroTitleElement
														.text();
											} else {
												log.debug("<li>Hero Panel element not having any title in it </li>");
											}
										} else {
											log.debug("<li>Hero Panel title element is not found</li>");
										}
										Elements heroDescriptionElements = ele
												.getElementsByTag("p");
										if (heroDescriptionElements != null) {
											Element heroDescriptionElement = heroDescriptionElements
													.first();
											if (heroDescriptionElement != null) {
												heroPanelDescription = heroDescriptionElement
														.text();
											} else {
												log.debug("<li>Hero Panel element not having any description in it </li>");
											}
										} else {
											log.debug("<li>Hero Panel para element not found </li>");
										}
										Elements heroPanelLinkTextElements = ele
												.getElementsByTag("b");
										if (heroPanelLinkTextElements != null) {
											Element heroPanelLinkTextElement = heroPanelLinkTextElements
													.first();
											if (heroPanelLinkTextElement != null) {
												heroPanelLinkText = heroPanelLinkTextElement
														.text();
											} else {
												log.debug("<li>Hero Panel element not having any linktext in it </li>");
											}
										} else {
											log.debug("<li>Hero Panel linktext element not found  </li>");
										}
										Elements heroPanelLinkUrlElements = ele
												.getElementsByTag("a");
										if (heroPanelLinkUrlElements != null) {
											Element heroPanelLinkUrlElement = heroPanelLinkUrlElements
													.first();
											if (heroPanelLinkUrlElement != null) {
												heroPanellinkUrl = heroPanelLinkUrlElement
														.absUrl("href");
												// Start extracting valid href
												log.debug("Before heroPanellinkUrl" + heroPanellinkUrl + "\n");
												heroPanellinkUrl = FrameworkUtils.getLocaleReference(heroPanellinkUrl, urlMap);
												log.debug("after heroPanellinkUrl" + heroPanellinkUrl + "\n");
												// End extracting valid href

											} else {
												log.debug("<li>Hero Panel element not having any linkurl in it </li>");
											}
										} else {
											log.debug("<li>Hero Panel link url element not found </li>");
										}
										// start image
										String heroImage = FrameworkUtils
												.extractImagePath(ele, sb);
										log.debug("heroImage " + heroImage + "\n");
										// end image
										log.debug("heroPanelTitle "
												+ heroPanelTitle + "\n");
										log.debug("heroPanelDescription "
												+ heroPanelDescription + "\n");
										log.debug("heroPanelLinkText "
												+ heroPanelLinkText + "\n");
										log.debug("heroPanellinkUrl "
												+ heroPanellinkUrl + "\n");
										if (heroPanelIterator != null
												&& heroPanelIterator.hasNext())
											heroPanelNode = heroPanelIterator
											.nextNode();
										if (heroPanelNode != null) {
											if (StringUtils
													.isNotBlank(heroPanelTitle)) {
												heroPanelNode.setProperty("title",
														heroPanelTitle);
											} else {
												sb.append(Constants.HERO_SLIDE_TITLE_NOT_FOUND);
											}
											if (StringUtils
													.isNotBlank(heroPanelDescription)) {
												heroPanelNode.setProperty(
														"description",
														heroPanelDescription);
											} else {
												sb.append(Constants.HERO_SLIDE_DESCRIPTION_NOT_FOUND);
											}
											if (StringUtils
													.isNotBlank(heroPanelLinkText)) {
												heroPanelNode.setProperty(
														"linktext",
														heroPanelLinkText);
											} else {
												sb.append(Constants.HERO_SLIDE_LINKTEXT_NOT_FOUND);
											}
											if (StringUtils
													.isNotBlank(heroPanellinkUrl)) {
												heroPanelNode
												.setProperty("linkurl",
														heroPanellinkUrl);
											} else {
												sb.append(Constants.HERO_SLIDE_LINKURL_NOT_FOUND);
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
																						locale, sb);
																		log.debug("heroImage " + heroImage
																				+ "\n");
																		if (StringUtils
																				.isNotBlank(heroImage)) {
																			imageNode.setProperty(
																					"fileReference",
																					heroImage);
																		}else{
																			noImageCount++;
																			/*sb.append(Constants.HERO_IMAGE_NOT_AVAILABLE);
																			log.debug("image path is blank.");*/
																		}
											} else {
												sb.append(Constants.HERO_SLIDE_IMAGE_NODE_NOT_FOUND);
											}
										}
									}
									if(noImageCount>=1){
										sb.append(noImageCount+" "+Constants.HERO_IMAGE_NOT_AVAILABLE);
										log.debug("image path is blank.");
									}
								} else {
									log.debug("<li>Hero Large Frames/Panel Elements is not found</li>");
								}
							} else {
								log.debug("<li>Hero Large Element is not found</li>");
							}
						} else {
							sb.append(Constants.HERO_LARGE_COMPONENT_NOT_FOUND);
						}
					} catch (Exception e) {
						sb.append("<li>Unable to update hero_large component." + e
								+ "</li>");
					}

					// end set Hero Large content.

					// start set selectorbar large component properties.				
					try {
						Elements selectorBarLargeElements = doc.select("div.selectorbarpanel");
						if (selectorBarLargeElements.size() == 0) {
							selectorBarLargeElements = doc.select("div.c58-pilot").select("div.left,div.mid,div.right"); //("div.selectorbarpanel");
						}

						Node selectorBarNode = indexLeftNode.hasNode("selectorbar_containe") ? indexLeftNode.getNode("selectorbar_containe") : null;

						if (selectorBarNode != null) {
							if (selectorBarLargeElements.isEmpty()) {
								log.debug("No selector bar element found with div class name selectorbarpanel.");
								sb.append("<li>Selector bar component with class name 'selectorbarpanel' does not exist on locale page.</li>");
							}
							else {
								log.debug("selector bar node component found at : "+ indexLeftNode.getPath());
								int eleSize = selectorBarLargeElements.size();
								log.debug("selector component element size: "+ eleSize);
								Node panel =selectorBarNode.getNode("panel1");
								NodeIterator selectorBarPanel = panel.getNodes("selectorbar_content*");
								int nodeSize = (int)selectorBarPanel.getSize();
								log.debug("selector component nodeSize : "+ nodeSize);
								if(eleSize == nodeSize){
									forSelectorBar(selectorBarLargeElements,selectorBarPanel,urlMap);
								}
								else if(eleSize > nodeSize){
									forSelectorBar(selectorBarLargeElements,selectorBarPanel,urlMap);
									sb.append("<li>Mismatch in the count of selector bar panel. Additional panel(s) found on locale page. Locale page has "+ eleSize +" panels and there are "+ nodeSize +" nodes.</li>");

								}
								else if(eleSize < nodeSize){
									forSelectorBar(selectorBarLargeElements,selectorBarPanel,urlMap);
									sb.append("<li>Mismatch in the count of selector bar panels. Additional node(s) found. Locale page has "+ eleSize +" panels and there are "+ nodeSize +" nodes.</li>");						
								}
							}
						}
						else {
							log.debug("No selector bar node found at "+indexLeftNode);
							sb.append("<li>Node for selector bar component does not exist.</li>");
						}
					} catch (Exception e) {
						sb.append("<li>Unable to update Selecotr bar large component." + e
								+ "</li>");
					}		

					// end set Selector bar.

					//Start of Spotlight
					try{
						Elements spotLightEle = doc.select("div.gd-left").select("div.c11-pilot");
						int spEleSize = spotLightEle.size();
						NodeIterator spNodeItr = indexLeftNode.hasNode("spotlight")?indexLeftNode.getNodes("spotlight*"):null;
						int spNodeSize = (int)spNodeItr.getSize();
						if(spotLightEle!=null&&!spotLightEle.isEmpty()){
							if(spNodeSize==spEleSize){
								int noImageCount = 0;
								for(Element spEle : spotLightEle){
									Element h2Ele = spEle.getElementsByTag("h2").first();
									Element pEle = spEle.getElementsByTag("p").first();
									if(spNodeItr.hasNext()){
										Node spNode = (Node)spNodeItr.next();
										// start image
										String spotLightImage = FrameworkUtils.extractImagePath(spEle, sb);
										log.debug("spotLightImage befor migration : " + spotLightImage + "\n");
										Node imageNode = spNode.hasNode("image")?spNode.getNode("image"):null;
										if(imageNode!=null){
											String fileReference = imageNode.hasProperty("fileReference")?imageNode.getProperty("fileReference").getString():"";
											spotLightImage = FrameworkUtils.migrateDAMContent(spotLightImage, fileReference, locale,sb);
											log.debug("spotLightImage after migration : " + spotLightImage + "\n");
											if (StringUtils.isNotBlank(spotLightImage)) {
												imageNode.setProperty("fileReference" , spotLightImage);
											}else{
												noImageCount++;
												/*sb.append(Constants.SPOTLIGHT_IMAGE_NOT_AVAILABLE);
												log.debug("image path is blank.");*/
											}
										}else{
											sb.append(Constants.SPOTLIGHT_IMAGE_NODE_NOT_AVAILABLE);
										}
										// end image
										spNode.setProperty("title", h2Ele.text());
										String pText = FrameworkUtils.extractHtmlBlobContent(pEle, "", locale, sb, urlMap);
										spNode.setProperty("description", pText);
										Element prevEle = h2Ele.previousElementSibling();
										if(prevEle.hasAttr("href")){
											prevEle.remove();
										}
										h2Ele.remove();
										pEle.remove();
										Element aEle = spEle.getElementsByTag("a").last();

										if(aEle!=null){
											String aText = aEle.text();
											String ownText  = spEle.ownText();
											if(!ownText.equals("")){
												aText = aText+" "+ownText;
											}
											String aHref = aEle.absUrl("href");
											// Start extracting valid href
											log.debug("Before spotlight" + aHref + "\n");
											aHref = FrameworkUtils.getLocaleReference(aHref, urlMap);
											log.debug("after spotlight" + aHref + "\n");
											// End extracting valid href
											spNode.setProperty("ctaText", aText);
											Node ctaNode = spNode.hasNode("cta")?spNode.getNode("cta"):null;
											if(ctaNode!=null){
												ctaNode.setProperty("url",aHref);
											}
										}else{
											sb.append("<li>no cta link found in spotlight</li>");
										}
									}
								}
								if(noImageCount>=1){
									sb.append(noImageCount+" "+Constants.SPOTLIGHT_IMAGE_NOT_AVAILABLE);
									log.debug("image path is blank.");
								}
							}else{
								int noImageCount=0;
								for(Element spEle : spotLightEle){
									Element h2Ele = spEle.getElementsByTag("h2").first();
									Element pEle = spEle.getElementsByTag("p").first();
									if(spNodeItr.hasNext()){
										Node spNode = (Node)spNodeItr.next();
										// start image
										String spotLightImage = FrameworkUtils.extractImagePath(spEle, sb);
										log.debug("spotLightImage befor migration : " + spotLightImage + "\n");
										Node imageNode = spNode.hasNode("image")?spNode.getNode("image"):null;
										if(imageNode!=null){
											String fileReference = imageNode.hasProperty("fileReference")?imageNode.getProperty("fileReference").getString():"";
											spotLightImage = FrameworkUtils.migrateDAMContent(spotLightImage, fileReference, locale,sb);
											log.debug("spotLightImage after migration : " + spotLightImage + "\n");
											if (StringUtils.isNotBlank(spotLightImage)) {
												imageNode.setProperty("fileReference" , spotLightImage);
											}else{
												/*sb.append(Constants.SPOTLIGHT_IMAGE_NOT_AVAILABLE);
												log.debug("image path is blank.");*/
												noImageCount++;
											}
										}else{
											sb.append(Constants.SPOTLIGHT_IMAGE_NODE_NOT_AVAILABLE);
										}
										// end image
										spNode.setProperty("title", h2Ele.text());
										String pText = FrameworkUtils.extractHtmlBlobContent(pEle, "", locale, sb, urlMap);
										spNode.setProperty("description", pText);
										Element prevEle = h2Ele.previousElementSibling();
										if(prevEle.hasAttr("href")){
											prevEle.remove();
										}
										h2Ele.remove();
										pEle.remove();
										Element aEle = spEle.getElementsByTag("a").last();
										if(aEle!=null){
											String aText = aEle.text();
											String ownText  = spEle.ownText();
											if(!ownText.equals("")){
												aText = aText+" "+ownText;
											}
											String aHref = aEle.absUrl("href");
											// Start extracting valid href
											log.debug("Before spotlight" + aHref + "\n");
											aHref = FrameworkUtils.getLocaleReference(aHref, urlMap);
											log.debug("after spotlight" + aHref + "\n");
											// End extracting valid href
											spNode.setProperty("ctaText", aText);
											Node ctaNode = spNode.hasNode("cta")?spNode.getNode("cta"):null;
											if(ctaNode!=null){
												ctaNode.setProperty("url",aHref);
											}
										}else{
											sb.append("<li>no cta link found in spotlight</li>");
										}
									}
								}
								if(noImageCount>=1){
									sb.append(noImageCount+" "+Constants.SPOTLIGHT_IMAGE_NOT_AVAILABLE);
									log.debug("image path is blank.");
								}
								sb.append("<li>Mis-match in spotlight elements. Elements are ("+spEleSize+") and nodes are ("+spNodeSize+").</li>");
							}
						}else{
							sb.append(Constants.SPOTLIGHT_ELEMENT_NOT_FOUND);
						}
					}catch(Exception e){
						log.error("Exception in spotlight : "+e);
					}
					//End of Spotlight

					//start of text
					try{
						Elements textEle = doc.select("div.gd-left").select("div.c00-pilot,div.cc00-pilot");
						NodeIterator textNodeIterator = indexLeftNode.hasNodes()?indexLeftNode.getNodes("text*"):null;
						Node textNode = null;
						if(textEle != null){
							for(Element text : textEle){
								if(textNodeIterator.hasNext()){
									textNode = (Node)textNodeIterator.next();
									String textHtml = FrameworkUtils.extractHtmlBlobContent(text, "",locale, sb,urlMap);
									textNode.setProperty("text",textHtml);
								}
								else{
									sb.append(Constants.TEXT_NODE_NOT_FOUND);
								}
							}	
						}	
						else{
							sb.append(Constants.TEXT_ELEMENT_NOT_FOUND);
						}
					}catch(Exception e){
						log.debug("Exception in title" +e);
					}
					//end of text

					//-------------------------------- end gd-left -------------------//

					//-------------------------------- start gd-right -------------------// 

					//Start of Rightrail

					// start set primary CTA content.

					try {
						String primaryCTATitle = "";
						String primaryCTADescription = "";
						String primaryCTALinkText = "";
						String primaryCTALinkUrl = "";

						Elements primaryCTAElements = doc.select("div.c47-pilot,div.ovt");
						if (primaryCTAElements != null) {
							Element primaryCTAElement = doc.select("div.c47-pilot")
									.first();
							if (primaryCTAElement != null) {
								Elements titleElements = primaryCTAElement
										.getElementsByTag("h3");
								if (titleElements != null) {
									Element titleElement = titleElements.first();
									if (titleElement != null) {
										primaryCTATitle = titleElement.text();
									} else {
										log.debug("<li>Primary CTA Heding element not having title</li>");
									}
								} else {
									log.debug("<li>Primary CTA Heading element section not found </li>");
								}
								Elements paraElements = primaryCTAElement
										.getElementsByTag("p");
								if (paraElements != null) {
									Element paraElement = paraElements.first();
									if (paraElement != null) {
										primaryCTADescription = paraElement.text();
									} else {
										log.debug("<li>Primary CTA Para element not having title</li>");
									}
								} else {
									log.debug("<li>Primary CTA Para element section not found </li>");
								}
								Elements ctaLinksElements = primaryCTAElement
										.select("ul.cta-links");
								if (ctaLinksElements != null) {
									Elements ctaLiElements = ctaLinksElements
											.select("li.cta");
									if (ctaLiElements != null) {
										Element ctaLiElement = ctaLiElements
												.first();
										if (ctaLiElement != null) {
											Elements anchorElements = ctaLiElement
													.getElementsByTag("a");
											if (anchorElements != null) {
												Element anchorElement = anchorElements
														.first();
												if (anchorElement != null) {
													primaryCTALinkText = anchorElement
															.text();
													primaryCTALinkUrl = anchorElement
															.absUrl("href");
													log.debug("primaryCTALinkUrl" + primaryCTALinkUrl + "\n");
													// Start extracting valid href
													log.debug("Before primaryCTALinkUrl" + primaryCTALinkUrl + "\n");
													primaryCTALinkUrl = FrameworkUtils.getLocaleReference(primaryCTALinkUrl, urlMap);
													log.debug("after primaryCTALinkUrl" + primaryCTALinkUrl + "\n");
													// End extracting valid href

												} else {
													log.debug("<li>Primary CTA Link anchor tag not found </li>");
												}
											} else {
												log.debug("<li>Primary CTA Link anchor tag section not found </li>");
											}
										} else {
											log.debug("<li>Primary CTA Link not found </li>");
										}
									} else {
										log.debug("<li>Primary CTA Links not found </li>");
									}
								} else {
									log.debug("<li>Primary CTA Links section not found </li>");
								}
								log.debug("primaryCTATitle" + primaryCTATitle
										+ "\n");
								log.debug("primaryCTADescription"
										+ primaryCTADescription + "\n");
								log.debug("primaryCTALinkText" + primaryCTALinkText
										+ "\n");
								log.debug("primaryCTALinkUrl" + primaryCTALinkUrl
										+ "\n");
								if (indexRightNode.hasNode("hero_tile")) {
									javax.jcr.Node primartCTANode = indexRightNode
											.getNode("hero_tile");
									if (StringUtils.isNotBlank(primaryCTATitle)) {
										primartCTANode.setProperty("title",
												primaryCTATitle);
									} else {
										sb.append(Constants.PRIMARY_CTA_TITLE_NOT_FOUND);
										log.debug("title property is not set at "
												+ primartCTANode.getPath());
									}
									if (StringUtils
											.isNotBlank(primaryCTADescription)) {
										primartCTANode.setProperty("description",
												primaryCTADescription);
									} else {
										sb.append(Constants.PRIMARY_CTA_DESCRIPTION_NOT_FOUND);
										log.debug("description property is not set at "
												+ primartCTANode.getPath());
									}
									if (StringUtils.isNotBlank(primaryCTALinkText)) {
										primartCTANode.setProperty("linktext",
												primaryCTALinkText);
									} else {
										sb.append(Constants.PRIMARY_CTA_LINKTEXT_NOT_FOUND);
										log.debug("linktext property is not set at "
												+ primartCTANode.getPath());
									}

									if (primartCTANode.hasNode("cta")) {
										javax.jcr.Node primartCTALinkUrlNode = primartCTANode
												.getNode("cta");
										if (StringUtils
												.isNotBlank(primaryCTALinkUrl)) {
											primartCTALinkUrlNode.setProperty(
													"url", primaryCTALinkUrl);
										} else {
											sb.append(Constants.PRIMARY_CTA_LINKURL_NOT_FOUND);
											log.debug("url property is not set at "
													+ primartCTALinkUrlNode
													.getPath());
										}
									} else {
										log.debug("<li>primary_cta_v2 node is not having linkurl node</li>");
									}
								} else {
									log.debug("<li>primary_cta_v2 node doesn't exists</li>");
								}
							} else {
								log.debug("<li>CTA element not found.</li>");
							}
						} else {
							sb.append(Constants.PRIMARY_CTA_COMPONENT_INWEB_NOT_FOUND);
						}
					} catch (Exception e) {
						log.debug("<li>Unable to update primary_cta_v2 component."
								+ e + "</li>");
					}

					// end set primary CTA content.

					//start of html blob
					NodeIterator htmlBlob = indexRightNode.getNodes("htmlblob*");
					NodeIterator letUsHelp = indexRightNode.getNodes("letushelp_*");
					if(htmlBlob != null && htmlBlob.getSize() > 0){
						int htmlBlobSize = (int)htmlBlob.getSize();
						if(letUsHelp != null && letUsHelp.getSize() > 0){
							htmlBlobSize=htmlBlobSize+(int)letUsHelp.getSize();
						}
						//						sb.append("<li>Extra HTML blog content found on locale page</li>");
						sb.append("<li>Mis-match of html blob components "+"web page has (0) and nodes are ("+htmlBlobSize+") </li>");
					}

					//end of html blob

					//start of tiles
					try{			
						Elements tileEle = doc.select("div.gd-right").select("div.c23-pilot");
						int tileEleSize = tileEle.size();
						NodeIterator tileNodeIterator = indexRightNode.hasNode("tile")?indexRightNode.getNodes("tile*"):null;
						int tileNodeSize = (int)tileNodeIterator.getSize();

						if(tileEleSize==tileNodeSize){
							setTile(tileEle, tileNodeIterator,locale, urlMap);
						}else if(tileEleSize>tileNodeSize){
							setTile(tileEle, tileNodeIterator,locale,urlMap);
							sb.append(Constants.MISMATCH_OF_TILES_IN_RIGHT_RAIL+tileEleSize+Constants.LIST_NODES_COUNT+" ("+tileNodeSize+")");
						}else if(tileEleSize<tileNodeSize){
							setTile(tileEle, tileNodeIterator,locale, urlMap);
							sb.append(Constants.MISMATCH_OF_TILES_NODES_IN_RIGHT_RAIL+tileEleSize+Constants.LIST_NODES_COUNT+" ("+tileNodeSize+")");
						}
					}catch(Exception e){
						log.error("Exception in right rail tile : ",e);
					}
					//end of tiles

					//start of list

					try{

						Elements listEle = doc.select("div.gd-right").select("div.n13-pilot");
						int listEleSize = 0;
						if(listEle!=null&&!listEle.isEmpty()){
							listEleSize = listEle.size();
						}
						NodeIterator listIterator = indexRightNode.hasNode("list_container")?indexRightNode.getNodes("list_container*"):null;
						int listNodeSize = 0;
						if(listIterator!=null){
							listNodeSize = (int)listIterator.getSize();
						}

						if(listEleSize>0&&listNodeSize>0){
							if(listEleSize==listNodeSize){
								setList(listEle, listIterator,urlMap);
							}else{
								setList(listEle, listIterator,urlMap);
								sb.append("Mis-match of list components in raight rail Elements are ("+listEleSize+") and nodes are ("+listNodeSize+").");
							}
						}else{
							log.debug("In list c00-pilot.");
							Element listElem = doc.select("div.gd-right").select("div.c00-pilot").first();
							if(listElem!=null){
								log.debug("c00-pilot != null.");
								Elements h2Ele = listElem.getElementsByTag("h2");
								if(h2Ele==null && h2Ele.isEmpty()){
									h2Ele = listElem.getElementsByTag("h3");
								}
								Elements ulEle = listElem.select("ul.no-bullets");
								if(h2Ele.size()==listIterator.getSize()){
									for(int i=0;i<h2Ele.size();i++){
										if(listIterator.hasNext()){
											Node listNode = (Node)listIterator.next();
											String title = h2Ele.get(i).text();
											listNode.setProperty("title",title);
											Element ul = ulEle.get(i);
											Elements liEle = ul.getElementsByTag("li");
											Node listParsys = listNode.hasNode("list_item_parsys")?listNode.getNode("list_item_parsys"):null;
											Node listContent=null;
											Node listitems=null;
											NodeIterator itemsItr=null;
											if(listParsys!=null){
												listContent =listParsys.hasNode("list_content")?listParsys.getNode("list_content"):null;
												if(listContent!=null){
													listitems =listContent.hasNode("listitems")?listContent.getNode("listitems"):null;
													if(listitems!=null){
														itemsItr = listitems.hasNodes()?listitems.getNodes("item_*"):null;
													}
												}
											}if(itemsItr.getSize()!=liEle.size()){
												sb.append("<li>Mis-match of links in list in right rail.</li>");
											}
											for(Element li : liEle){
												Element aEle = li.getElementsByTag("a").first();
												String linkText = aEle.text();
												String ownText = li.ownText();
												if(!ownText.equals("")){
													linkText = linkText+" "+ownText;
												}
												String linkUrl =aEle.absUrl("href");
												// Start extracting valid href
												log.debug("Before spotlight" + linkUrl + "\n");
												linkUrl = FrameworkUtils.getLocaleReference(linkUrl, urlMap);
												log.debug("after spotlight" + linkUrl + "\n");
												// End extracting valid href
												if(itemsItr.hasNext()){
													Node item = (Node)itemsItr.next();
													Node linkDataNode = item.hasNode("linkdata")?item.getNode("linkdata"):null;
													if(linkDataNode!=null){
														linkDataNode.setProperty("linktext",linkText);
														linkDataNode.setProperty("url",linkUrl);
													}
												}
											}
										}
									}
								}
							}
						}
					}catch(Exception e){
						log.error("Exception in right rail list : ",e);
					}

					//end of list

					//start of anchor image

					Elements gdRightEle = doc.select("div.gd-right");
					for(Element gdRight : gdRightEle){
						Element aEle = gdRight.getElementsByTag("a").last();
						Element aImg = aEle.getElementsByTag("img").first();
						if(aImg!=null){
							sb.append("<li>Extra image in anchor tag(element) found in right rail</li>");
						}
					}
					//end of anchor image

					//End of Rightrail

					//-------------------------------- end gd-right -------------------//
				}else{
					sb.append(Constants.URL_CONNECTION_EXCEPTION);	
				}

			} catch (Exception e) {
				log.error(e);
				sb.append(Constants.URL_CONNECTION_EXCEPTION);
			}
		}catch(Exception e){
			log.debug("Exception ", e);
		}

		sb.append("</ul></td>");
		session.save();
		return sb.toString();
	}
/*	public void setForHero(Elements heroElements, Node heroPanelLarge, String locale, Map<String, String> urlMap) {
		try {
			Value[] panelPropertiest = null;
			Property panelNodesProperty = heroPanelLarge.hasProperty("panelNodes") ? heroPanelLarge.getProperty("panelNodes") : null;
			if (panelNodesProperty.isMultiple()) {
				panelPropertiest = panelNodesProperty.getValues();
			}
			int i = 0;
			Node heroPanelNode = null;
			for (Element ele : heroElements) {
				if (panelPropertiest != null && i <= panelPropertiest.length) {
					String propertyVal = panelPropertiest[i].getString();
					if (StringUtils.isNotBlank(propertyVal)) {
						JSONObject jsonObj = new JSONObject(propertyVal);
						if (jsonObj.has("panelnode")) {
							String panelNodeProperty = jsonObj.get("panelnode").toString();
							heroPanelNode = heroPanelLarge.hasNode(panelNodeProperty) ? heroPanelLarge.getNode(panelNodeProperty) : null;
						}
					}
					i++;
				} else {
					sb.append("<li>No heropanel Node found.</li>");
				}
				heroPanelTranslate(heroPanelNode, ele, locale, urlMap);
			}
		} catch (Exception e) {
		}
	}

	//start setting of heropanel
	public void heroPanelTranslate(Node heroPanelNode, Element ele, String locale,Map<String,String> urlMap) {

		try {			
			String title = ele.getElementsByTag("h2")!=null?ele.getElementsByTag("h2").text():"";
			String desc = ele.getElementsByTag("p")!=null?ele.getElementsByTag("p").first().text():"";

			Elements anchor = ele.getElementsByTag("a");		
			String anchorText = anchor!=null?anchor.text():"";
			String anchorHref = anchor.attr("href");
			// Start extracting valid href
			log.debug("Before heroPanelLinkUrl" + anchorHref + "\n");
			anchorHref = FrameworkUtils.getLocaleReference(anchorHref, urlMap);
			log.debug("after heroPanelLinkUrl" + anchorHref + "\n");
			// End extracting valid href

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
					String fileReference = imageNode.hasProperty("fileReference")?imageNode.getProperty("fileReference").getString():"";
					heroImage = FrameworkUtils.migrateDAMContent(heroImage, fileReference, locale,sb);
					log.debug("heroImage after migration : " + heroImage + "\n");
					if (StringUtils.isNotBlank(heroImage)) {
						imageNode.setProperty("fileReference" , heroImage);
					}else{
						sb.append(Constants.HERO_IMAGE_NOT_AVAILABLE);
						log.debug("image path is blank.");mm
					}
				} else {
					sb.append("<li>hero image node doesn't exist</li>");
				}

				if(heroPanelPopUpNode != null){
					heroPanelPopUpNode.setProperty("popupHeader", title);
				}else{
					sb.append("<li>Hero content video pop up node not found.</li>");
				}

				heroPanelNode.setProperty("title", title);
				heroPanelNode.setProperty("description", desc);
				heroPanelNode.setProperty("linktext", anchorText);
				heroPanelNode.setProperty("linkurl", anchorHref);
			}
			// end image

		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	//end setting of heropanel
*/
	
	//start setting of selectorbar
	public void selectorBarTranslate(Node selectorBarPanelNode, Element ele,Map<String,String> urlMap) {

		try {
			String title = (ele!=null?ele.getElementsByTag("a").first().text():"");
			String titleUrl = ele.getElementsByTag("a").first().attr("href");
			// Start extracting valid href
			log.debug("Before selector bar title LinkUrl" + titleUrl + "\n");
			titleUrl = FrameworkUtils.getLocaleReference(titleUrl, urlMap);
			log.debug("after selector bar title LinkUrl" + titleUrl + "\n");
			// End extracting valid href
			log.debug("selector component titleUrl: "+ titleUrl);							
			selectorBarPanelNode.setProperty("title", title);

			if (ele.childNodeSize() >= 2) {
				log.debug("Child node size is greater than 2.");
				if (ele.select("div.menu").isEmpty()) {
					log.debug("Menu is not available.");
					sb.append("<li>Selector bar drop down menu elements does not exist on the locale page.</li>");
				}
				else {
					log.debug("Menu is available.");
					Element menuEle = ele.select("div.menu").first();
					if (menuEle != null) {
						log.debug("selector component menuEle: "+ menuEle.toString());
						Element anchor = menuEle.select("a.cta").first();
						String allLinkText = anchor!=null? anchor.text():"";
						String allLinkUrl = anchor!=null?anchor.attr("href"):"";
						// Start extracting valid href
						log.debug("Before selector bar menu LinkUrl" + allLinkUrl + "\n");
						allLinkUrl = FrameworkUtils.getLocaleReference(allLinkUrl, urlMap);
						log.debug("after selector bar menu LinkUrl" + allLinkUrl + "\n");
						// End extracting valid href
						selectorBarPanelNode.setProperty("seeall", allLinkText);
						Node seeAllLink = selectorBarPanelNode.getNode("seealllink");
						seeAllLink.setProperty("url", allLinkUrl);

						Element menuUlList = menuEle.getElementsByTag("ul").first();
						Node linksNode = selectorBarPanelNode.getNode("links");
						NodeIterator itemsItr = null;
						if(linksNode!=null){
							itemsItr = linksNode.getNodes("item*");
						}
						Elements liEle = menuUlList.getElementsByTag("li");
						int liEleSize = liEle.size();
						int liNodeSize = (int)itemsItr.getSize();
						if(liEleSize==liNodeSize){
							for(Element li : liEle){
								Element aEle = li.getElementsByTag("a").first();
								String aHref = aEle.absUrl("href");
								// Start extracting valid href
								log.debug("Before li" + aHref + "\n");
								aHref = FrameworkUtils.getLocaleReference(aHref, urlMap);
								log.debug("after li" + aHref + "\n");
								// End extracting valid href
								Node itemNode = (Node)itemsItr.next();
								Node linkNode = itemNode.getNode("link");
								linkNode.setProperty("linktext",aEle.text());
								linkNode.setProperty("url",aHref);
							}
						}else{
							for(Element li : liEle){
								Element aEle = li.getElementsByTag("a").first();
								String aHref = aEle.absUrl("href");
								// Start extracting valid href
								log.debug("Before li" + aHref + "\n");
								aHref = FrameworkUtils.getLocaleReference(aHref, urlMap);
								log.debug("after li" + aHref + "\n");
								// End extracting valid href
								Node itemNode = null;
								if(itemsItr.hasNext()){
									itemNode = (Node)itemsItr.next();
								}if(itemNode!=null){
									Node linkNode = itemNode.getNode("link");	
									linkNode.setProperty("linktext",aEle.text());
									linkNode.setProperty("url",aHref);
								}
							}
							sb.append("<li>Mis-match of links in selector bar component.</li>");
						}

					}
					else {
						sb.append("<li>Selector bar drop down menu elements does not exist on the locale page.</li>");
					}
				}
			}
			else {
				sb.append("<li>Selector bar drop down menu elements does not exist on the locale page.</li>");
			}

		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	//end setting of selectorbar

	//start for selector bar
	public void forSelectorBar(Elements selectorBarLargeElements, NodeIterator selectorBarPanel,Map<String,String> urlMap){
		for(Element ele : selectorBarLargeElements){
			Node selectorBarPanelNode;
			if (selectorBarPanel.hasNext()) {
				selectorBarPanelNode = (Node)selectorBarPanel.next();
				selectorBarTranslate(selectorBarPanelNode, ele,urlMap);
			}
		}
	}
	//end for selector bar

	//start setTile
	public void setTile(Elements tileEle,NodeIterator tileNodeIterator, String locale, Map<String, String> urlMap) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException{
		for(Element tile : tileEle){
			Element imgEle = tile.getElementsByTag("img").first();
			if(imgEle!=null){
				sb.append("<li>Extra image found in tile in right rail.</li>");
			}
			Element h2Ele = tile.getElementsByTag("h2").first();
			String tTitle = null;
			if(h2Ele!=null){
				tTitle = h2Ele.text();
			}else{
				Element h3Ele =  tile.getElementsByTag("h3").first();
				tTitle = h3Ele.text();
			}
			Element pEle = tile.getElementsByTag("p").first();
			String tDesc = null;
			boolean check=true;
			if(pEle!=null){
				tDesc = pEle.text();
			}else{
				Element ulEle = tile.getElementsByTag("ul").first();
				if(ulEle!=null){
					tDesc = FrameworkUtils.extractHtmlBlobContent(ulEle, "", locale, sb, urlMap);
					check = false;
				}else{
					sb.append("<li>No description foud for tile in right rail.</li>");
				}
			}
			Node tileNode = null;
			if(tileNodeIterator.hasNext()){
				tileNode = (Node)tileNodeIterator.next();
				tileNode.setProperty("title",tTitle);
				if(tDesc!=null){
					tileNode.setProperty("description",tDesc);
				}
				if(check){
					Element aEle = tile.getElementsByTag("a").first();
					Node ctaNode = tileNode.hasNode("cta")?tileNode.getNode("cta"):null;
					if(ctaNode!=null){
						String aText = aEle.text();
						String ownText = tile.ownText();
						if(!ownText.equals("")){
							aText = aText+" "+ownText;
						}
						ctaNode.setProperty("linktext",aText);
						if(tileNode.hasProperty("url")){
							// Start extracting valid href
							log.debug("Before anchorHref" + aEle.attr("href") + "\n");
							String anchorHref = FrameworkUtils.getLocaleReference(aEle.attr("href"), urlMap);
							log.debug("after anchorHref" + anchorHref + "\n");
							// End extracting valid href
							ctaNode.setProperty("url",anchorHref);	
						}
					}
				}else{
					sb.append("<li>No cta link available for tile in right rail.</li>");
				}
			}
		}
	}
	//end setTile

	//start setList
	public void setList(Elements listEle, NodeIterator listIterator,Map<String,String> urlMap) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException{
		for(Element list : listEle){
			Element h2Ele = list.getElementsByTag("h2").first();
			Element ulEle = list.select("ul.no-bullets").first();
			Node listNode = null;
			if(listIterator.hasNext()){
				listNode = (Node)listIterator.next();
				listNode.setProperty("title",h2Ele.text());

				Node listparsys = listNode.hasNode("list_item_parsys")?listNode.getNode("list_item_parsys"):null;
				if(listparsys!=null){
					Node listCont = listparsys.hasNode("list_content")?listparsys.getNode("list_content"):null;
					if(listCont!=null){
						Node listItems = listCont.hasNode("listitems")?listCont.getNode("listitems"):null;
						if(listItems!=null){
							NodeIterator items = listItems.hasNodes()?listItems.getNodes("item_*"):null;
							int itemsSize = 0;
							Elements liEle = ulEle.getElementsByTag("li");
							int liSize =0;
							if(liEle!=null&&!liEle.isEmpty()){
								liSize = liEle.size();
							}
							if(items!=null){
								itemsSize = (int)items.getSize();
							}
							if(itemsSize>0&&liSize>0){
								if(itemsSize==liSize){
									for(Element li : liEle){
										Element aEle = li.getElementsByTag("a").first();
										String linkText = aEle.text();
										String ownText = li.ownText();
										if(!ownText.equals("")){
											linkText = linkText+" "+ownText;
										}
										String aHref = aEle.absUrl("href");
										// Start extracting valid href
										log.debug("Before li" + aHref + "\n");
										aHref = FrameworkUtils.getLocaleReference(aHref, urlMap);
										log.debug("after li" + aHref + "\n");
										// End extracting valid href
										Node item = null;
										if(items.hasNext()){
											item = (Node)items.next();
											if(item.hasProperty("size")){
												log.debug("hasProperty size.");
												item.setProperty("size","");
											}
										}
										if(item!=null){
											Node linkData = item.hasNode("linkdata")?item.getNode("linkdata"):null;
											if(linkData!=null){
												linkData.setProperty("linktext",linkText);
												linkData.setProperty("url",aHref);
											}
										}

									}
								}else{
									for(Element li : liEle){
										Element aEle = li.getElementsByTag("a").first();
										String linkText = aEle.text();
										String ownText = li.ownText();
										if(!ownText.equals("")){
											linkText = linkText+" "+ownText;
										}
										String aHref = aEle.absUrl("href");
										// Start extracting valid href
										log.debug("Before li" + aHref + "\n");
										aHref = FrameworkUtils.getLocaleReference(aHref, urlMap);
										log.debug("after li" + aHref + "\n");
										// End extracting valid href
										Node item = null;
										if(items.hasNext()){
											item = (Node)items.next();
											if(item.hasProperty("size")){
												log.debug("hasProperty size.");
												item.setProperty("size","");
											}
										}
										if(item!=null){
											Node linkData = item.hasNode("linkdata")?item.getNode("linkdata"):null;
											if(linkData!=null){
												linkData.setProperty("linktext",linkText);
												linkData.setProperty("url",aHref);
											}
										}

									}
									sb.append(Constants.MISMATCH_RIGHT_RAIL_LIST);
								}
							}
						}
					}
				}
			}

		}
	}
	//end setList
}
