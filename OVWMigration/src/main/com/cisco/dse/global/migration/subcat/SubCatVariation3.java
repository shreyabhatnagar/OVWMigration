package com.cisco.dse.global.migration.subcat;

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
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.cisco.dse.global.migration.config.BaseAction;
import com.cisco.dse.global.migration.config.Constants;
import com.cisco.dse.global.migration.config.FrameworkUtils;

public class SubCatVariation3 extends BaseAction {

	Document doc;

	StringBuilder sb = new StringBuilder(1024);

	static Logger log = Logger.getLogger(SubCatVariation3.class);

	public String translate(String host, String loc, String prod, String type, String catType,
			String locale, Session session, Map<String, String> urlMap) throws IOException,
			ValueFormatException, VersionException, LockException,
			ConstraintViolationException, RepositoryException {
		BasicConfigurator.configure();
		// Repo node paths
		try {
			String pagePropertiesPath = "/content/<locale>/"+catType+"/<prod>/videoscape/index/jcr:content/";
			String pageUrl = host + "/content/<locale>/"+catType+"/<prod>/videoscape/index.html";

			pageUrl = pageUrl.replace("<locale>", locale).replace("<prod>", prod);
			pagePropertiesPath = pagePropertiesPath.replace("<locale>", locale).replace("<prod>", prod);
			String spLeft = pagePropertiesPath+"content_parsys/solutions/layout-solutions/gd22v2/gd22v2-left";
			String spRight = pagePropertiesPath+"content_parsys/solutions/layout-solutions/gd22v2/gd22v2-right";
			String spMid = pagePropertiesPath+"content_parsys/solutions/layout-solutions/gd21v1_0/gd21v1-mid";
			String spBottom = pagePropertiesPath+"content_parsys/solutions/layout-solutions/gd21v1/gd21v1-mid";

			log.debug("Path is "+spLeft);
			log.debug("Path is "+spRight);
			log.debug("Path is "+spMid);
			log.debug("Path is "+spBottom);

			sb.append("<td>" + "<a href="+pageUrl+">"+pageUrl+"</a>"+"</td>");
			sb.append("<td>" + "<a href="+loc+">"+loc +"</a>"+ "</td>");
			sb.append("<td><ul>");

			javax.jcr.Node spLeftNode = null;
			javax.jcr.Node spRightNode = null;
			javax.jcr.Node spMidNode = null;
			javax.jcr.Node spBottomNode = null;
			javax.jcr.Node pageJcrNode = null;

			spLeftNode = session.getNode(spLeft);
			spRightNode = session.getNode(spRight);
			spMidNode = session.getNode(spMid);
			spBottomNode = session.getNode(spBottom);
			pageJcrNode = session.getNode(pagePropertiesPath);

			try {
				doc = getConnection(loc);
				log.debug("Connected to the provided URL");
			} catch (Exception e) {
				log.error("Exception : ",e);
			}


			if (doc != null) {
				// start set page properties.
				FrameworkUtils.setPageProperties(pageJcrNode, doc, session, sb);
				// end set page properties.

				//start Hero Migration
				try{
					log.debug("Start Hero Element Migration");
					Element heroEle = doc.select("div.c50v4-pilot").first();
					migrateHero(heroEle , spLeftNode , locale , urlMap);
					log.debug("Hero Element Migrated");
				}catch(Exception e){
					sb.append(Constants.EXCEPTION_IN_HERO_MIGRATION);
					log.debug("Exception in Hero Element Migration"+e);
				}
				//End Hero Migration

				//start Let Us Help migration
				try{
					log.debug("start Let Us Help migration");
					Element contactUsElement = doc.select("div.f-holder").first();
					migrateContactUsElement(contactUsElement , spRightNode, locale , urlMap);
					log.debug("Let Us Help Element Migrated");
				}catch(Exception e){
					sb.append(Constants.UNABLE_TO_UPDATE_CONTACTUS);
					log.debug("Exception in Let Us Help Element Migration"+e);
				}
				//end Let Us Help migration

				//start Top Right migration
				try{
					log.debug("start Top Right migration");
					Element rightRailEle = doc.getElementsByClass("n13v1-pilot").first();
					migrateRightRail(rightRailEle , spRightNode, locale , urlMap);
					log.debug("Top Right Element Migrated");
				}catch(Exception e){
					sb.append(Constants.EXCEPTION_IN_HTMLBLOB);
					log.debug("Exception in Top Right Element Migration"+e);
				}
				//end Top Right migration

				//start Text Migration
				try{
					log.debug("start Text Migration");
					Elements textEles = doc.select("div.gd21v1-mid");
					Element textEle = textEles.first().getElementsByClass("c00-pilot").first();
					if(textEle == null){
						textEle = textEles.get(1).getElementsByClass("c00-pilot").first();
					}
					migrateText(textEle , spMidNode , locale, urlMap);
					log.debug("Text is Migrated");
				}catch(Exception e){
					log.debug("Exception in Text Migration");
					sb.append(Constants.EXCEPTION_TEXT_COMPONENT);
				}
				//End text Migration

				//start secondText Migration
				try{
					log.debug("start secondText Migration");
					Element textEle = doc.select("div.gd21-pilot").last();
					migrateSecondText(textEle , spBottomNode , locale, urlMap);
					log.debug("secondText is Migrated");
				}catch(Exception e){
					log.debug("Exception in secondText Migration");
					sb.append(Constants.EXCEPTION_TEXT_COMPONENT);
				}
				//End secondText Migration

			}else{
				sb.append(Constants.URL_CONNECTION_EXCEPTION);
			}
		}catch(Exception e){
			sb.append(Constants.UNABLE_TO_MIGRATE_PAGE);
			log.error("Exception : ",e);
		}
		sb.append("</ul></td>");
		session.save();
		log.debug("Msg returned is "+sb.toString());
		return sb.toString();
	}


	private void migrateHero(Element heroElement, Node spLeftNode, String locale, Map<String, String> urlMap) throws PathNotFoundException, RepositoryException, JSONException {
		if(heroElement != null){
			Elements heroEles = heroElement.getElementsByClass("frame");
			if(heroEles != null){
				Node heroNode = spLeftNode.hasNode("hero_medium")?spLeftNode.getNode("hero_medium"):null;
				if(heroNode != null){
					Property panelNodesProperty = heroNode.hasProperty("panelNodes")?heroNode.getProperty("panelNodes"):null;
					Value[] panelPropertiest = null;
					if (panelNodesProperty.isMultiple()) {
						panelPropertiest = panelNodesProperty.getValues();
					}
					int i = -1;
					for(Element heroEle : heroEles){
						if(panelPropertiest != null && i < panelPropertiest.length){
							String propertyVal = panelPropertiest[++i].getString();
							if (StringUtils.isNotBlank(propertyVal)) {
								JSONObject jsonObj = new JSONObject(propertyVal);
								if (jsonObj.has("panelnode")) {
									String nodeName = jsonObj.getString("panelnode");
									if(heroNode.hasNode(nodeName)){
										Node heroPanelNode = heroNode.getNode(nodeName);
										Element h2hero = heroEle.getElementsByTag("h2").first();
										if(h2hero != null){
											heroPanelNode.setProperty("title", h2hero.text());
										}else{
											sb.append(Constants.HERO_CONTENT_HEADING_ELEMENT_DOESNOT_EXISTS);
										}
										Element pHero = heroEle.getElementsByTag("p").first();
										if(pHero != null){
											heroPanelNode.setProperty("description", pHero.text());
										}else{
											sb.append(Constants.HERO_CONTENT_DESCRIPTION_ELEMENT_DOESNOT_EXISTS);
										}
										Element aHero = heroEle.getElementsByTag("p").last().getElementsByTag("a").first();
										if(aHero != null){
											heroPanelNode.setProperty("linktext", aHero.text());
											if(aHero.hasAttr("id")){
												heroPanelNode.setProperty("lightboxid", aHero.attr("id"));
											}else{
												String aHref = aHero.absUrl("href");
												if(StringUtil.isBlank(aHref)){
													aHref = aHero.attr("href");
												}
												heroPanelNode.setProperty("linkurl", FrameworkUtils.getLocaleReference(aHref, urlMap, locale, sb));
											}
										}else{
											sb.append(Constants.HERO_CONTENT_ANCHOR_ELEMENT_DOESNOT_EXISTS);
										}
										String heroImage = FrameworkUtils.extractImagePath(heroEle, sb);
										heroImage = FrameworkUtils.migrateDAMContent(heroImage, "", locale, sb);
										if(heroImage != ""){
											Node heroImageNode = heroPanelNode.hasNode("image")?heroPanelNode.getNode("image"):null;
											if(heroImageNode != null){
												heroImageNode.setProperty("fileReference", heroImage);
											}else{
												sb.append(Constants.HERO_IMAGE_NODE_NOT_FOUND);
											}
										}else{
											sb.append(Constants.HERO_IMAGE_NOT_AVAILABLE);
										}
									}else{
										sb.append(Constants.HERO_NODE_NOT_AVAILABLE);
									}
								}
							}
						}
					}
				}else{
					sb.append(Constants.HERO_CONTENT_NODE_NOT_FOUND);
				}
			}else{
				sb.append(Constants.HERO_CONTENT_PANEL_ELEMENT_NOT_FOUND);
			}
		}else{
			sb.append(Constants.HERO_CONTENT_PANEL_ELEMENT_NOT_FOUND);
		}

	}

	private void migrateContactUsElement(Element contactUsElement,
			Node spRightNode, String locale, Map<String, String> urlMap) throws PathNotFoundException, RepositoryException {
		if(contactUsElement != null){
			String titleText = "";
			String callText = "";
			Node letUsHelpNode = spRightNode.hasNode("letushelp")?spRightNode.getNode("letushelp"):null;
			if(letUsHelpNode != null){
				Elements titleElem = contactUsElement.getElementsByTag("h3");
				Elements liElements = contactUsElement.getElementsByTag("li");
				if(!titleElem.isEmpty()){
					titleText = titleElem.text();
				}
				else{
					sb.append(Constants.CONTACTUS_TITLE_NOT_FOUND);
				}
				log.debug("call text is : "+callText);
				if(!liElements.isEmpty()){
					callText = liElements.first().html();
					log.debug("call text is : "+callText);
					/*if(liElements.size() > 1){
					sb.append("<li> Contact Us Element has extra content(links/numbers) which cannot be migrated as English Page does not have.</li>");
				}*/
				}
				else{
					sb.append("<li> Contact Us Element Call text not found on locale page.</li>");
				}
				letUsHelpNode.setProperty("title",titleText);
				letUsHelpNode.setProperty("calltext",callText);
				if(letUsHelpNode.hasProperty("timetext")){
					sb.append("<li> Extra text(Time Text) in Contact Us element found on WEM page. </li>");
				}
			}else{
				sb.append(Constants.CONTACTUS_NODE_NOT_FOUND);
			}
		}else{
			sb.append(Constants.CONTACTUS_ELEMENT_NOT_FOUND);
		}
	}

	private void migrateRightRail(Element listEle, Node spRightNode,
			String locale, Map<String, String> urlMap) throws RepositoryException, JSONException {
		if(listEle != null){
			if(spRightNode.hasNode("list")){
				Node listNode = spRightNode.getNode("list");
				Element heading = listEle.getElementsByTag("h2").first();
				if(heading != null){
					listNode.setProperty("title", heading.text());
				}else{
					sb.append(Constants.RIGHT_LIST_COMPONENT_TITLE_NOT_FOUND);
				}
				if(listNode.hasNode("element_list_0")){
					Node elementNode = listNode.getNode("element_list_0");
					Elements links = listEle.getElementsByTag("a");
					if(links != null){
						List<String> list = new ArrayList<String>();
						String aURL = null;
						for(Element anchor : links){
							aURL = anchor.absUrl("href");
							if(StringUtil.isBlank(aURL)){
								aURL = anchor.attr("href");
							}
							aURL = FrameworkUtils.getLocaleReference(aURL, urlMap, locale, sb);
							JSONObject obj = new JSONObject();
							obj.put("linktext", anchor.text());
							obj.put("linkurl",aURL);
							obj.put("icon","");
							obj.put("size","");
							obj.put("description","");
							obj.put("openInNewWindow",false);
							list.add(obj.toString());
						}
						elementNode.setProperty("listitems",list.toArray(new String[list.size()]));
					}else{
						sb.append(Constants.RIGHT_GRID_ANCHOR_ELEMENTS_NOT_FOUND);
					}
				}else{
					sb.append(Constants.RIGHT_LIST_ITEMS_NODE_NOT_FOUND);
				}
			}else{
				sb.append(Constants.RIGHT_LIST_ITEMS_NODE_NOT_FOUND);
			}
		}else{
			sb.append(Constants.RIGHT_RAIL_LIST_NOT_FOUND);
		}
		Element followUs = doc.getElementsByClass("s14-pilot").first();
		if(followUs != null){
			if(spRightNode.hasNode("followus_0")){
				Node followUsNode = spRightNode.getNode("followus_0");
				Element title = followUs.getElementsByTag("h2").first();
				if(title != null){
					followUsNode.setProperty("title", title.text());
				}else{
					sb.append(Constants.FOLLOWUS_TITLE_NOT_FOUND);
				}
				List<String> list = new ArrayList<String>();
				Elements liElements = followUs.getElementsByTag("li");
				for (Element ele : liElements) {
					JSONObject obj = new JSONObject();
					String icon = ele.attr("class");
					Element aElement = ele.getElementsByTag("a").first();
					if (aElement != null) {
						obj.put("icon", icon);
						obj.put("linktext", aElement.attr("title"));
						String aHref = aElement.absUrl("href");
						if(StringUtil.isBlank(aHref)){
							aHref = aElement.attr("href");
						}
						aHref = FrameworkUtils.getLocaleReference(aHref, urlMap, locale, sb);
						obj.put("linkurl",aHref );
						list.add(obj.toString());
					} else {
						sb.append(Constants.FOLLOW_US_ANCHOR_ELEMENT_NOT_FOUND);
					}
				}
				if (list.size() > 0) {
					followUsNode.setProperty("links", list.toArray(new String[list.size()]));
				}
			}else{
				sb.append(Constants.FOLLOWUS_NODE_NOT_FOUND);
			}
		}else{
			sb.append(Constants.FOLLOWUS_ELEMENT_NOT_FOUND);
		}

	}

	private void migrateText(Element textEle, Node spMidNode,
			String locale, Map<String, String> urlMap) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
		if(textEle != null){
			Node textNode = spMidNode.hasNode("text")?spMidNode.getNode("text"):null;
			if(textNode != null){
				textNode.setProperty("text", FrameworkUtils.extractHtmlBlobContent(textEle, "", locale, sb, urlMap));
			}else{
				sb.append(Constants.TEXT_NODE_NOT_FOUND);
			}
		}else{
			sb.append(Constants.TEXT_ELEMENT_NOT_FOUND);
		}
	}

	private void migrateSecondText(Element textEle, Node spBottomNode,
			String locale, Map<String, String> urlMap) throws RepositoryException, JSONException {
		if(textEle != null){
			Elements textEles = textEle.getElementsByClass("c00-pilot");
			if(textEles != null){
				int eleSize = textEles.size();
				NodeIterator textNodes = spBottomNode.hasNode("text")?spBottomNode.getNodes("text*"):null;
				if(textNodes != null){
					int size = (int)textNodes.getSize();
					if(size == eleSize){
						for(Element textElement : textEles){
							Node textNode = textNodes.nextNode();
							textNode.setProperty("text", FrameworkUtils.extractHtmlBlobContent(textElement, "", locale, sb, urlMap));
						}
					}else if(size > eleSize){
						for(Element textElement : textEles){
							Node textNode = textNodes.nextNode();
							textNode.setProperty("text", FrameworkUtils.extractHtmlBlobContent(textElement, "", locale, sb, urlMap));
						}
						if(textNodes.hasNext()){
							sb.append(Constants.TEXT_NODE_MISMATCH + size + Constants.TEXT_ELEMENT_COUNT + eleSize + ".</li>");
						}
					}else if(size < eleSize){
						for(Element textElement : textEles){
							if(textNodes.hasNext()){
								Node textNode = textNodes.nextNode();
								textNode.setProperty("text", FrameworkUtils.extractHtmlBlobContent(textElement, "", locale, sb, urlMap));
							}else{
								sb.append(Constants.TEXT_NODE_MISMATCH + size + Constants.TEXT_ELEMENT_COUNT + eleSize + ".</li>");
							}
						}
					}
				}else{
					sb.append(Constants.TEXT_NODE_NOT_FOUND);
				}
			}else{
				sb.append(Constants.TEXT_ELEMENT_NOT_FOUND);
			}
			//Start Spotlight migration.
			Element spEle = textEle.getElementsByClass("c11v6-pilot").first();
			if(spEle != null){
				if(spBottomNode.hasNode("spotlight_medium_v2")){
					Node spotLightNode = spBottomNode.getNode("spotlight_medium_v2");
					Element title = spEle.getElementsByTag("h2").first();
					Element link = spEle.getElementsByTag("a").last();
					Element description = spEle.getElementsByTag("p").first();
					String fileReference = FrameworkUtils.extractImagePath(spEle, sb);
					fileReference = FrameworkUtils.migrateDAMContent(fileReference, "", locale, sb);
					if(title != null){
						spotLightNode.setProperty("title", title.text());
					}else{
						sb.append(Constants.SPOTLIGHT_HEADING_ELEMENT_NOT_FOUND);
					}
					if(link != null){
						String aURL = link.absUrl("href");
						if(StringUtil.isBlank(aURL)){
							aURL = link.attr("href");
						}
						aURL = FrameworkUtils.getLocaleReference(aURL, urlMap, locale, sb);
						spotLightNode.setProperty("linktext", link.text());
						Node linkNode = spotLightNode.hasNode("cta")?spotLightNode.getNode("cta"):null;
						if(linkNode != null){
							linkNode.setProperty("url", aURL);
						}else{
							sb.append(Constants.SPOTLIGHT_CTA_NODE_NOT_FOUND);
						}
					}else{
						sb.append(Constants.SPOTLIGHT_ANCHOR_ELEMENT_NOT_FOUND);
					}
					if(description != null){
						spotLightNode.setProperty("description", description.text());
					}else{
						sb.append(Constants.SPOTLIGHT_DESCRIPTION_ELEMENT_NOT_FOUND);
					}
					Node imageNode = spotLightNode.hasNode("image")?spotLightNode.getNode("image"):null;
					if(imageNode != null){
						if(fileReference != ""){
							imageNode.setProperty("fileReference", fileReference);
						}else{
							sb.append(Constants.SPOTLIGHT_IMAGE_NOT_AVAILABLE);
						}
					}else{
						sb.append(Constants.SPOTLIGHT_IMAGE_NODE_NOT_AVAILABLE);
					}
				}else{
					sb.append(Constants.SPOTLIGHT_NODE_NOT_FOUND);
				}
			}else{
				sb.append(Constants.SPOTLIGHT_ELEMENT_NOT_FOUND);
			}
			//Start List Element
			Element listEle = textEle.getElementsByClass("n13v12-pilot").first();
			if(listEle != null){
				if(spBottomNode.hasNode("list")){
					Node listNode = spBottomNode.getNode("list");
					Element heading = listEle.getElementsByTag("h2").first();
					if(heading != null){
						listNode.setProperty("title", heading.text());
					}else{
						sb.append(Constants.LIST_HEADING_COMPONENT_NOT_FOUND);
					}
					if(listNode.hasNode("element_list_0")){
						Node elementNode = listNode.getNode("element_list_0");
						Elements links = listEle.getElementsByTag("a");
						if(links != null){
							List<String> list = new ArrayList<String>();
							String aURL = null;
							for(Element anchor : links){
								aURL = anchor.absUrl("href");
								if(StringUtil.isBlank(aURL)){
									aURL = anchor.attr("href");
								}
								aURL = FrameworkUtils.getLocaleReference(aURL, urlMap, locale, sb);
								JSONObject obj = new JSONObject();
								obj.put("linktext", anchor.text());
								obj.put("linkurl",aURL);
								obj.put("icon","");
								obj.put("size","");
								obj.put("description","");
								obj.put("openInNewWindow",false);
								list.add(obj.toString());
							}
							elementNode.setProperty("listitems",list.toArray(new String[list.size()]));
						}else{
							sb.append(Constants.LIST_ANCHOR_ELEMENTS_NOT_FOUND);
						}
					}else{
						sb.append(Constants.LIST_ELEMENT_LIST_NODE_NOT_FOUND);
					}
				}else{
					sb.append(Constants.LIST_NODE_NOT_FOUND);
				}
			}else{
				sb.append(Constants.LIST_COMPONENT_NOT_FOUND);
			}
		}else{
			sb.append(Constants.TEXT_ELEMENT_NOT_FOUND);
		}
	}

}
