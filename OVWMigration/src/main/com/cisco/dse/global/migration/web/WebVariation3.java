package com.cisco.dse.global.migration.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
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
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.cisco.dse.global.migration.config.BaseAction;
import com.cisco.dse.global.migration.config.Constants;
import com.cisco.dse.global.migration.config.FrameworkUtils;

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
		String pageUrl = host + "/content/<locale>/" + catType + "/<prod>/overview.html";

		pageUrl = pageUrl.replace("<locale>", locale).replace("<prod>", prod);
		pagePropertiesPath = pagePropertiesPath.replace("<locale>", locale).replace("<prod>", prod);

		String midSizeUpperLeft = pagePropertiesPath+"/content_parsys/solutions/layout-solutions/gd12v2/gd12v2-left";
		String midSizeUpperRight = pagePropertiesPath+"/content_parsys/solutions/layout-solutions/gd12v2/gd12v2-right";
		String midSizeLowerLeft = pagePropertiesPath+"/content_parsys/solutions/layout-solutions/gd12v2_0/gd12v2-left";
		String midSizeLowerRight = pagePropertiesPath+"/content_parsys/solutions/layout-solutions/gd12v2_0/gd12v2-right";
		String midSizeMiddle = pagePropertiesPath+"/content_parsys/solutions/layout-solutions/gd11v1/gd11v1-mid";

		pageUrl = pageUrl.replace("<locale>", locale).replace("<prod>", prod);
		pagePropertiesPath = pagePropertiesPath.replace("<locale>", locale).replace("<prod>", prod);

		sb.append("<td>" + "<a href=" + pageUrl + ">" + pageUrl + "</a>" + "</td>");
		sb.append("<td>" + "<a href=" + loc + ">" + loc + "</a>" + "</td>");
		sb.append("<td><ul>");

		log.debug("In the translate method to migarate " + loc + " to " + pageUrl);

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
					Elements heroLargeElements = doc.select("div.c50-pilot");
					migrateHero(heroLargeElements,midSizeUpperLeftNode,locale,urlMap);

				} catch (Exception e) {
					log.debug("<li>Unable to update hero_large component.</li>");
					log.debug("Exception : ", e);
				}
				// end set Hero Large content.

				// start of Upper Right component content.
				try {
					log.debug("Started Migrating Upper Right.");
					Element upperRightElement = doc.select("div.c47-pilot").first();
					migratePrimaryCta(upperRightElement,midSizeUpperRightNode,locale,urlMap);
				}
				catch (Exception e) {
					log.debug("Excepiton : ", e);
					sb.append(Constants.PRIMARY_CTA_COMPONENT_NOT_UPDATED);
				}
				// end Upper Right component content.
				// ---------------------------------------------------------------------------------------------------------------------------------------
				// start of  html blob components content.
				try {
					log.debug("Started Migrating  html blob.");
					Element gdMidElement = doc.select("div.gd-mid").first();
					migrateHtmlBlob(gdMidElement,midSizeMiddleNode,locale,urlMap);
				} catch (Exception e) {
					log.debug("Excepiton : ", e);
					sb.append(Constants.EXCEPTION_IN_HTMLBLOB);
				}
				// end set html blob component content.
				// ---------------------------------------------------------------------------------------------------------------------------------------
				// start of text content.
				try {
					log.debug("Started Migrating text.");
					Element textElement = doc.select("div.c00-pilot").first();
					migrateText(textElement,midSizeLowerLeftNode,locale,urlMap);
				} catch (Exception e) {
					sb.append(Constants.UNABLE_TO_MIGRATE_TEXT);
					log.debug("Excepiton : ", e);
				}
				// end text component content.

				//start spotlights migration
				try{
					log.debug("start spotlight migration.");
					Elements spEles = doc.select("div.c11-pilot");
					migrateSpotLight(spEles,midSizeLowerLeftNode,locale,urlMap);
				}catch(Exception e){
					log.error("Exception in spotlight migration.");
					sb.append(Constants.EXCEPTION_SPOTLIGHT_COMPONENT);
				}
				//end spotlight migration

				//start of tile Border Component migration.
				try{
					log.debug("start of tile Border Component migration.");
					Elements tileEles = doc.getElementsByClass("c23-pilot");
					migrateTileBoreder(tileEles,midSizeLowerRightNode,locale,urlMap);
				}catch(Exception e){
					log.error("Exception in tileBolder migration");
					sb.append(Constants.UNABLE_TO_MIGRATE_TILE_BORDERED_COMPONENTS);
				}
				//End of tile border component migration

				//start of RightRailImage Component migration.
				try{
					log.debug("start of RightRailImage Component migration.");
					if(doc.select("div.c00-pilot").size() > 1){
						Element imageEle = doc.select("div.c00-pilot").last();
						migrateRightRailImage(imageEle,midSizeLowerRightNode,locale,urlMap);
					}
				}catch(Exception e){
					log.error("Exception in tileBolder migration");
					sb.append(Constants.UNABLE_TO_MIGRATE_TILE_BORDERED_COMPONENTS);
				}
				//End of RightRailImage component migration

				//start of FollowUs Component migration.
				try{
					log.debug("start of FollowUs Component migration.");
					Element followUsEle = doc.select("div.s14-pilot").first();
					migrateFollowUsImage(followUsEle,midSizeLowerRightNode,locale,urlMap);
				}catch(Exception e){
					log.error("Exception in tileBolder migration");
					sb.append(Constants.UNABLE_TO_MIGRATE_TILE_BORDERED_COMPONENTS);
				}
				//End of FollowUs component migration

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

	private void migrateFollowUsImage(Element followUsEle,
			Node midSizeLowerRightNode, String locale,
			Map<String, String> urlMap) throws PathNotFoundException, RepositoryException, JSONException {
		if(followUsEle != null){
			if(midSizeLowerRightNode.hasNode("followus")){
				Node followUsNode = midSizeLowerRightNode.getNode("followus");
				Element title = followUsEle.getElementsByTag("h2").first();
				if(title != null){
					followUsNode.setProperty("title", title.text());
				}else{
					sb.append(Constants.FOLLOWUS_TITLE_NOT_FOUND);
				}
				Elements list = followUsEle.getElementsByTag("li");
				if(list != null){
					List<String> listAdd = new ArrayList<String>();
					for(Element li : list){
						Element a = li.getElementsByTag("a").first();
						if(a != null){
							String aURL = a.absUrl("href");
							if(aURL.equals("")){
								aURL = a.attr("href");
							}
							aURL = FrameworkUtils.getLocaleReference(aURL, urlMap,locale, sb);
							JSONObject obj = new JSONObject();
							obj.put("linktext", a.text());
							obj.put("linkurl",aURL);
							obj.put("icon",li.className());
							listAdd.add(obj.toString());
						}else{
							sb.append(Constants.FOLLOW_US_ANCHOR_ELEMENT_NOT_FOUND);
						}
					}
					followUsNode.setProperty("links",listAdd.toArray(new String[listAdd.size()]));
				}else{
					sb.append(Constants.FOLLOW_US_ANCHOR_ELEMENT_NOT_FOUND);
				}
			}else{
				sb.append(Constants.FOLLOWUS_NODE_NOT_FOUND);
			}
		}else{
			sb.append(Constants.FOLLOWUS_ELEMENT_NOT_FOUND);
		}
	}

	private void migrateHero(Elements heroLargeElements,
			Node midSizeUpperLeftNode, String locale, Map<String, String> urlMap) throws PathNotFoundException, ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException, JSONException {
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

		if (heroLargeElements != null) {
			Element heroLargeElement = heroLargeElements.first();
			if (heroLargeElement != null) {
				int eleSize = heroLargeElement.select("div.frame").size();
				Elements heroLargeFrameElements = heroLargeElement.select("div.frame");
				Node heroPanelNode = null;
				if (heroLargeFrameElements != null) {
					if (eleSize != heroLargeNode.getNodes("heropanel*").getSize()) {
						sb.append(Constants.MISMATCH_IN_HERO_SLIDES);
						heroLargeElement.select("div.frame").first().remove();
						heroLargeFrameElements = heroLargeElement.select("div.frame");
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
								sb.append(Constants.HERO_CONTENT_HEADING_ELEMENT_DOESNOT_EXISTS);
								log.debug("No h2 first element found with in the class 'frame' of div.");
							}
						} else {
							sb.append(Constants.HERO_CONTENT_HEADING_ELEMENT_DOESNOT_EXISTS);
							log.debug("No h2 found with in the class 'frame' of div.");
						}
						Elements heroDescriptionElements = ele.getElementsByTag("p");
						if (heroDescriptionElements != null) {
							Element heroDescriptionElement = heroDescriptionElements.first();
							if (heroDescriptionElement != null) {
								heroPanelDescription = heroDescriptionElement.text();
							} else {
								sb.append(Constants.HERO_CONTENT_DESCRIPTION_ELEMENT_DOESNOT_EXISTS);
								log.debug("No p frist element found with in the class 'frame' of div.");
							}
						} else {
							sb.append(Constants.HERO_CONTENT_DESCRIPTION_ELEMENT_DOESNOT_EXISTS);
							log.debug("No p elemtn found with in the class 'frame' of div.");
						}
						Elements heroPanelLinkTextElements = ele.getElementsByTag("b");
						if (heroPanelLinkTextElements != null) {
							Element heroPanelLinkTextElement = heroPanelLinkTextElements.first();
							if (heroPanelLinkTextElement != null) {
								heroPanelLinkText = heroPanelLinkTextElement.text();
							} else {
								sb.append(Constants.HERO_CONTENT_ANCHOR_TEXT_IS_BLANK);
								log.debug("No b tags first elemtn found with in the class 'frame' of div.");
							}
						} else {
							sb.append(Constants.HERO_CONTENT_ANCHOR_TEXT_IS_BLANK);
							log.debug("No b tag found with the class 'frame' of div.");
						}
						Elements heroPanelLinkUrlElements = ele.getElementsByTag("a");
						if (heroPanelLinkUrlElements != null) {
							Element heroPanelLinkUrlElement = heroPanelLinkUrlElements.first();
							if (heroPanelLinkUrlElement != null) {
								heroPanellinkUrl = heroPanelLinkUrlElement.absUrl("href");
								if(heroPanellinkUrl.equals("")){
									heroPanellinkUrl = heroPanelLinkUrlElement.attr("href");
								}
								// Start extracting valid href
								log.debug("heroPanellinkUrl before migration : " + heroPanellinkUrl);
								heroPanellinkUrl = FrameworkUtils.getLocaleReference(heroPanellinkUrl, urlMap,locale, sb);
								log.debug("heroPanellinkUrl after migration : " + heroPanellinkUrl);
								// End extracting valid href
							} else {
								sb.append(Constants.HERO_CONTENT_ANCHOR_LINK_IS_BLANK);
								log.debug("No anchor first element found with in the class 'frame' of div.");
							}
						} else {
							sb.append(Constants.HERO_CONTENT_ANCHOR_LINK_IS_BLANK);
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
									log.debug("hero_node_Name : "+heroPanelNode.getName());
								}
							}
							i++;
						} else {
							sb.append(Constants.HERO_CONTENT_NODE_NOT_FOUND);
							log.debug("No list panelProperties found for the hero compoent order.");
						}

						if (heroPanelNode != null) {
							Node heroPanelPopUpNode = null;
							Elements lightBoxElements = ele.select("div.c50-image").select("a.c26v4-lightbox");
							if (lightBoxElements != null && !lightBoxElements.isEmpty()) {
								heroPanelPopUpNode = FrameworkUtils.getHeroPopUpNode(heroPanelNode);
							}
							if (StringUtils.isNotBlank(heroPanelTitle)) {
								heroPanelNode.setProperty("title", heroPanelTitle);
								if (heroPanelPopUpNode != null) {
									heroPanelPopUpNode.setProperty("popupHeader", heroPanelTitle);
								} else {
									if(lightBoxElements != null && lightBoxElements.size() != 0){
										sb.append("<li>Hero content video pop up node not found.</li>");
										log.debug("No pop-up node found for the hero panel node " + heroPanelNode.getPath());
									}
								}
							} else {
								sb.append(Constants.HERO_SLIDE_TITLE_NOT_FOUND);
								log.debug("Title is blank with in the 'frame' class of div.");
							}
							if (StringUtils.isNotBlank(heroPanelDescription)) {
								heroPanelNode.setProperty("description", heroPanelDescription);
							} else {
								sb.append(Constants.HERO_SLIDE_DESCRIPTION_NOT_FOUND);
								log.debug("Description is blank with in the 'frame' class of the div.");
							}
							if (StringUtils.isNotBlank(heroPanelLinkText)) {
								heroPanelNode.setProperty("linktext", heroPanelLinkText);
							} else {
								sb.append(Constants.HERO_SLIDE_DESCRIPTION_NOT_FOUND);
								log.debug("Link Text doesn't exists with in the class 'frame' of the div.");
							}
							if (StringUtils.isNotBlank(heroPanellinkUrl)) {
								heroPanelNode.setProperty("linkurl", heroPanellinkUrl);
							} else {
								sb.append(Constants.HERO_SLIDE_LINKURL_NOT_FOUND);
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
								sb.append(Constants.HERO_SLIDE_IMAGE_NODE_NOT_FOUND);
								log.debug("'image' node doesn't exists in " + heroPanelNode.getPath());
							}
						}
					}
				} else {
					log.debug("<li>Hero Large Frames/Panel Elements is not found</li>");
					log.debug("No div found with class 'frame'");
				}
			} else {
				sb.append(Constants.HERO_LARGE_COMPONENT_NOT_FOUND);
				log.debug("No first element found with class 'c50-pilot'");
			}
		} else {
			sb.append("<li>Hero Large component is not found on web publisher page</li>");
			log.debug("No element found with class 'c50-pilot'");
		}

	}

	private void migratePrimaryCta(Element upperRightElement,
			Node midSizeUpperRightNode, String locale,
			Map<String, String> urlMap) throws PathNotFoundException, ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
		if (upperRightElement != null) {
			if (midSizeUpperRightNode.hasNode("primary_cta_v2")) {
				Element title = upperRightElement.getElementsByTag("h3").first();
				Element description = upperRightElement.getElementsByTag("p").first();
				Element link = upperRightElement.getElementsByTag("a").first();
				Node ctaNode = midSizeUpperRightNode.getNode("primary_cta_v2");
				if(title != null){
					ctaNode.setProperty("title", title.text());
				}else{
					sb.append(Constants.PRIMARY_CTA_TITLE_ELEMENT_NOT_FOUND);
				}
				if(description != null){
					ctaNode.setProperty("description", description.text());
				}else{
					sb.append(Constants.PRIMARY_CTA_DESCRIPTION_ELEMENT_NOT_FOUND);
				}
				if(link != null){
					ctaNode.setProperty("linktext", link.text());
					if(ctaNode.hasNode("linkurl")){
						String aUrl = link.absUrl("href");
						if(aUrl.equals("")){
							aUrl = link.attr("href");
						}
						aUrl = FrameworkUtils.getLocaleReference(aUrl, urlMap,locale, sb);
						Node linkUrlNode = ctaNode.getNode("linkurl");
						linkUrlNode.setProperty("url", aUrl);
					}else{
						sb.append(Constants.PRIMARY_CTA_LINK_URL_NODE_NOT_FOUND);
					}
				}else{
					sb.append(Constants.PRIMARY_CTA_ANCHOR_ELEMENT_NOT_FOUND);
				}
			}else{
				sb.append(Constants.PRIMARY_CTA_COMPONENT_NOT_FOUND);
			}
		} else {
			sb.append(Constants.PRIMARY_CTA_COMPONENT_INWEB_NOT_FOUND);
		}

	}

	private void migrateHtmlBlob(Element gdMidElement, Node midSizeMiddleNode,
			String locale, Map<String, String> urlMap) throws PathNotFoundException, ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
		if (gdMidElement != null) {
			if (midSizeMiddleNode.hasNode("htmlblob")) {
				Node htmlblobNode = midSizeMiddleNode.getNode("htmlblob");
				htmlblobNode.setProperty("html", FrameworkUtils.extractHtmlBlobContent(gdMidElement, "", locale, sb, urlMap));
			}else {
				sb.append(Constants.HTMLBLOB_NODE_NOT_FOUND);
			}
		} else {
			sb.append(Constants.HTMLBLOB_ELEMENT_NOT_FOUND);
		}
	}

	private void migrateText(Element textElement, Node midSizeLowerLeftNode,
			String locale, Map<String, String> urlMap) throws PathNotFoundException, ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
		if (textElement != null) {
			if (midSizeLowerLeftNode.hasNode("text")) {
				Node textNode = midSizeLowerLeftNode.getNode("text");
				String text = FrameworkUtils.extractHtmlBlobContent(textElement, "", locale, sb, urlMap);
				text = text.replace(text.substring(0, text.indexOf("<h2>")), "").replace("</div>", "");
				textNode.setProperty("text",text );
			}else{
				sb.append(Constants.TEXT_NODE_NOT_FOUND);
			}
		} else {
			sb.append(Constants.TEXT_ELEMENT_NOT_FOUND);
		}
	}

	private void migrateSpotLight(Elements spEles, Node midSizeLowerLeftNode,
			String locale, Map<String, String> urlMap) throws PathNotFoundException, ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
		if(spEles != null){
			int elesize = spEles.size();
			NodeIterator spNodes = midSizeLowerLeftNode.hasNode("spotlight_large_v2")?midSizeLowerLeftNode.getNodes("spotlight_large_v2*"):null;
			if(spNodes != null){
				int size = (int)spNodes.getSize();
				for(Element spEle : spEles){
					if(spNodes.hasNext()){
						Node spNode = spNodes.nextNode();
						Element title = spEle.getElementsByTag("h2").first();
						if(title != null){
							spNode.setProperty("title", title.text());
						}else{
							sb.append(Constants.SPOTLIGHT_HEADING_ELEMENT_NOT_FOUND);
						}
						Element description = spEle.getElementsByTag("p").first();
						if(description != null){
							spNode.setProperty("description", description.text());
						}else{
							sb.append(Constants.SPOTLIGHT_DESCRIPTION_ELEMENT_NOT_FOUND);
						}
						spEle.getElementsByTag("p").first().remove();
						Elements bullets = spEle.getElementsByTag("li");
						if(bullets.size() == 0 ){
							Element bullet = spEle.getElementsByTag("p").first();
							if(bullet != null && !bullet.ownText().isEmpty()){
								String link = FrameworkUtils.extractHtmlBlobContent(bullet, "", locale, sb, urlMap);
								String bulletValue[] = {link};
								spNode.setProperty("bullets", bulletValue);
								spEle.getElementsByTag("p").first().remove();
							}else{
								sb.append(Constants.SPOTLIGHT_ANCHOR_ELEMENT_NOT_FOUND);
							}
						}else{
							List<String> list = new ArrayList<String>();
							for(Element li : bullets){
								String link = FrameworkUtils.extractHtmlBlobContent(li, "", locale, sb, urlMap);
								link = link.replace("<li>", "").replace("</li>", "");
								list.add(link);
							}
							spNode.setProperty("bullets", list.toArray(new String[list.size()]));
						}
						Element splink = spEle.getElementsByTag("p").first();
						if(splink != null){
							Element spLink = splink.getElementsByTag("a").first();
							if(spNode.hasProperty("linktext")){
								if(spLink != null){
									spNode.setProperty("linktext", spLink.text());
									if(spNode.hasNode("cta")){
										Node ctaNode = spNode.getNode("cta");
										String aUrl = spLink.absUrl("href");
										if(aUrl.equals("")){
											aUrl = spLink.attr("href");
										}
										aUrl = FrameworkUtils.getLocaleReference(aUrl, urlMap, locale, sb);
										ctaNode.setProperty("url", aUrl);
									}
								}else{
									sb.append(Constants.SPOTLIGHT_ANCHOR_ELEMENT_NOT_FOUND);
								}
							}
						}else{
							if(spNode.hasProperty("linktext")){
								sb.append(Constants.SPOTLIGHT_ANCHOR_ELEMENT_NOT_FOUND);
							}
						}
						if(spNode.hasNode("image")){
							Node imageNode = spNode.getNode("image");
							String fileReference = FrameworkUtils.extractImagePath(spEle, sb);
							fileReference = FrameworkUtils.migrateDAMContent(fileReference, "", locale, sb);
							if(fileReference != ""){
								imageNode.setProperty("fileReference", fileReference);
							}else{
								sb.append(Constants.SPOTLIGHT_IMAGE_NOT_AVAILABLE);
							}
						}else{
							sb.append(Constants.SPOTLIGHT_IMAGE_NODE_NOT_AVAILABLE);
						}
					}else{
						//						sb.append(Constants.SPOTLIGHT_ELEMENT_MISMATCH+size+Constants.SPOTLIGHT_ELEMENT_COUNT+elesize+".</li>");
					}
				}
				if(size!=elesize){
					sb.append(Constants.SPOTLIGHT_ELEMENT_MISMATCH+size+Constants.SPOTLIGHT_ELEMENT_COUNT+elesize+".</li>");
				}
				/*if(spNodes.hasNext()){
					sb.append(Constants.SPOTLIGHT_ELEMENT_MISMATCH+size+Constants.SPOTLIGHT_ELEMENT_COUNT+elesize+".</li>");
				}*/
			}/*else{
				sb.append(Constants.SPOTLIGHT_NODE_NOT_FOUND);
			}*/
		}/*else{
			sb.append(Constants.SPOTLIGHT_ELEMENT_NOT_FOUND);
		}*/

	}

	private void migrateRightRailImage(Element imageEle,
			Node midSizeLowerRightNode, String locale,
			Map<String, String> urlMap) throws RepositoryException {
		if(imageEle != null){
			if(midSizeLowerRightNode.hasNode("htmlblob_0")){
				Node htmlBlobNode = midSizeLowerRightNode.getNode("htmlblob_0");
				htmlBlobNode.setProperty("html", FrameworkUtils.extractHtmlBlobContent(imageEle, "", locale, sb, urlMap));
			}else{
				sb.append(Constants.IMAGE_LINK_NODE_NOT_FOUND);
			}
		}else{
			sb.append(Constants.IMAGE_NOT_FOUND_IN_LOCALE_PAGE);
		}

	}

	private void migrateTileBoreder(Elements tileEles,
			Node midSizeLowerRightNode, String locale,
			Map<String, String> urlMap) throws RepositoryException {
		if(tileEles != null){
			int eleSize = tileEles.size();
			NodeIterator tileNodes = midSizeLowerRightNode.hasNode("tile_bordered")?midSizeLowerRightNode.getNodes("tile_bordered*"):null;
			if(tileNodes != null){
				int size = (int)tileNodes.getSize();
				for(Element tileEle : tileEles){
					if(tileNodes.hasNext()){
						Node tileNode = tileNodes.nextNode();
						Element title = tileEle.getElementsByTag("h2").first();
						if(title != null){
							tileNode.setProperty("title", title.text());
						}else{
							sb.append(Constants.TILE_BORDERED_TITLE_ELEMENT_NOT_FOUND);
						}
						Element description = tileEle.getElementsByTag("p").first();
						if(description != null){
							tileNode.setProperty("description", description.text());
						}else{
							sb.append(Constants.TILE_BORDERED_DESCRIPTION_NOT_FOUND);
						}
						Element anchor = tileEle.getElementsByTag("a").first();
						if(anchor != null){
							tileNode.setProperty("linktext", anchor.text());
							String linkurl = anchor.absUrl("href");
							if(linkurl.equals("")){
								linkurl = anchor.attr("href");
							}
							linkurl = FrameworkUtils.getLocaleReference(linkurl, urlMap, locale, sb);
							tileNode.setProperty("linkurl", linkurl);
						}else{
							sb.append(Constants.TILE_BORDERED_ANCHOR_ELEMENTS_NOT_FOUND);
						}
					}else{
						sb.append(Constants.MISMATCH_IN_TILEBORDER_COUNT+eleSize+Constants.TILEBORDER_NODE+size+".</li>");
					}
				}
				if(tileNodes.hasNext()){
					sb.append(Constants.MISMATCH_IN_TILEBORDER_COUNT+eleSize+Constants.TILEBORDER_NODE+size+".</li>");
				}
			}else{
				sb.append(Constants.TILE_BORDERED_NODES_NOT_FOUND);
			}
		}else{
			sb.append(Constants.TILE_BORDERED_COMPONENT_NOT_FOUND);
		}
	}

}
