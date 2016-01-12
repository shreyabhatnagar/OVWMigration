package com.cisco.dse.global.migration.rroot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.cisco.dse.global.migration.config.BaseAction;
import com.cisco.dse.global.migration.config.Constants;
import com.cisco.dse.global.migration.config.FrameworkUtils;

public class RSolutionIndex extends BaseAction {

	Document doc;

	StringBuilder sb = new StringBuilder(1024);

	static Logger log = Logger.getLogger(RSolutionIndex.class);
	
	private int noImageSize = 0;

	public String translate(String host, String loc, String prod, String type, String catType,
			String locale, Session session) throws IOException,
			ValueFormatException, VersionException, LockException,
			ConstraintViolationException, RepositoryException {
		log.debug("Inside Trsnslate method of RSolutionIndex");
		log.debug("In the translate method, catType is :" + catType);
		BasicConfigurator.configure();
		// Repo node paths

		String pagePropertiesPath = "/content/<locale>/" + catType + "/index/jcr:content";
		String pageUrl = host + "/content/<locale>/" + catType + "/index.html";
		pageUrl = pageUrl.replace("<locale>", locale);
		pagePropertiesPath = pagePropertiesPath.replace("<locale>", locale);
		
		
		String indexLeft = pagePropertiesPath+"/Grid/solutions/layout-solutions/widenarrow/WN-Wide-1";
		String indexRight = pagePropertiesPath+"/content_parsys/overview/layout-overview/gd12v2/gd12v2-right";

		log.debug("Path is "+indexLeft);
		log.debug("Path is "+indexRight);

		sb.append("<td>" + "<a href="+pageUrl+">"+pageUrl+"</a>"+"</td>");
		sb.append("<td>" + "<a href="+loc+">"+loc +"</a>"+ "</td>");
		sb.append("<td><ul>");

		Node indexLeftNode = null;
		Node pageJcrNode = null;
		try{
			indexLeftNode = session.getNode(indexLeft);
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
					Elements heroElements = doc.select("div.c50-pilot");
					
					String h2Text = null;
					String pText = null;
					String aText = null;
					String aLink = null;
					String imagePath = null;
					
					//Start of get content logic.
					if(heroElements != null && !heroElements.isEmpty()){
						Element heroElement = heroElements.first();
						Elements textElements = heroElement.select("div.c50-text");
						if(textElements != null && !textElements.isEmpty()){
							Element textElement = textElements.first();
							Elements h2Elements = textElement.getElementsByTag("h2");
							if(h2Elements != null && !h2Elements.isEmpty()){
								Element h2Element = h2Elements.first();
								h2Text = h2Element.text();
							}else{
								sb.append(Constants.HERO_CONTENT_HEADING_ELEMENT_DOESNOT_EXISTS);
								log.debug("No h2 elements found with in the div class 'c50-text' with in div class 'c50-pilot'");
							}
							Elements pElements = textElement.getElementsByTag("p");
							if(pElements != null && !pElements.isEmpty()){
								Element pElement = pElements.first();
								pText = pElement.text();
							}else{
								sb.append(Constants.HERO_CONTENT_DESCRIPTION_ELEMENT_DOESNOT_EXISTS);
								log.debug("No p elements found with in the div class 'c50-text' with in div class 'c50-pilot'");
							}
							Elements aElements = textElement.getElementsByTag("a");
							if(aElements != null && !aElements.isEmpty()){
								Element aElement = aElements.first();
								aText = aElement.text();
								aLink = aElement.attr("href");
							}else{
								sb.append(Constants.HERO_CONTENT_DESCRIPTION_ELEMENT_DOESNOT_EXISTS);
								log.debug("No p elements found with in the div class 'c50-text' with in div class 'c50-pilot'");
							}
						}else{
							sb.append(Constants.HERO_CONTENT_PANEL_TEXT_ELEMENT_NOT_FOUND);	
							log.debug("No element found with div class 'c50-text'");
						}
						Elements imgElements = heroElement.select("div.c50-image");
						if(imgElements != null && !imgElements.isEmpty()){
							Element imgElement = imgElements.first();
							Elements imageElements = imgElement.getElementsByTag("img");
							if(imageElements != null && !imageElements.isEmpty()){
								Element imageElement = imageElements.first();
								imagePath = imageElement.attr("src");
							}else{
								sb.append(Constants.HERO_CONTENT_PANEL_IMAGE_ELEMENT_NOT_FOUND);
							}
						}else{
							sb.append(Constants.HERO_CONTENT_PANEL_IMAGE_ELEMENT_NOT_FOUND);
						}
					}else{
						sb.append(Constants.HERO_CONTENT_PANEL_ELEMENT_NOT_FOUND);
						log.debug("No element found with div class 'c50-pilot'");
					}
					//end of get content logic.
					//start of set content logic.
					
					
					if(indexLeftNode.hasNode("hero_panel")){
						Node hero_panel_Node = indexLeftNode.getNode("hero_panel");
						
						if(StringUtils.isNotBlank(h2Text)){
							hero_panel_Node.setProperty("title", h2Text);
						}else{
							sb.append(Constants.HERO_CONTENT_HEADING_IS_BLANK);
						}
						
						if(StringUtils.isNotBlank(pText)){
							hero_panel_Node.setProperty("description", pText);
						}else{
							sb.append(Constants.HERO_CONTENT_DESCRIPTION_IS_BLANK);
						}
						
						if(StringUtils.isNotBlank(aText)){
							hero_panel_Node.setProperty("linktext", aText);
						}else{
							sb.append(Constants.HERO_CONTENT_ANCHOR_TEXT_IS_BLANK);
						}
						if(hero_panel_Node.hasNode("cta")){
							Node cta_Node = hero_panel_Node.getNode("cta");
							if(StringUtils.isNotBlank(aLink)){
								cta_Node.setProperty("url", aLink);
							}else{
								sb.append(Constants.HERO_CONTENT_ANCHOR_LINK_IS_BLANK);
							}
						}else{
							sb.append(Constants.HERO_CONTENT_ANCHOR_NODE_NOT_FOUND);
						}
						if(hero_panel_Node.hasNode("image")){
							Node imageNode = hero_panel_Node.getNode("image");
							if(StringUtils.isNotBlank(imagePath)){
								String fileReference = imageNode.hasProperty("fileReference")?imageNode.getProperty("fileReference").getString():"";
								log.debug("imagePath before migration : "+imagePath);
								imagePath = FrameworkUtils.migrateDAMContent(imagePath, fileReference, locale, sb);
								log.debug("imagePath after migration : "+imagePath);
								imageNode.setProperty("fileReference", imagePath);
							}else{
								sb.append(Constants.HERO_CONTENT_IMAGE_LINK_IS_BLANK);
							}
						}else{
							sb.append(Constants.HERO_CONTENT_IMAGE_NODE_NOT_FOUND);
						}
					}else{
						sb.append(Constants.HERO_CONTENT_NODE_NOT_FOUND);
					}
					
					//end of set content logic.
					
				}catch(Exception e){
					sb.append(Constants.EXCEPTION_IN_HERO_MIGRATION);
					log.debug("Exception in Hero Element Migration"+e);
				}
				//End Hero Migration
				
				
				
				
				//Start
				try{
					//start of get logic.
					Elements grid_elements = doc.select("div.gd22v1-pilot");
					if(grid_elements != null && !grid_elements.isEmpty()){
						Element grid_element = grid_elements.first();
						Elements gd_left_Elements = grid_element.select("div.gd-left");
						if(gd_left_Elements != null && !gd_left_Elements.isEmpty()){
							Element gd_left_Element = gd_left_Elements.first();
						}else{
							sb.append(Constants.LEFT_GRID_ELEMENT_NOT_FOUND);
						}
						Elements gd_right_Elements = grid_element.select("div.gd-right");
					}else{
						sb.append(Constants.LEFT_GRID_ELEMENT_NOT_FOUND);
					}
					//end of get logic.
					//start of set logic.
					//end of set logic.
				}catch(Exception e){
					sb.append(Constants.GRID_ELEMENT_NOT_FOUND);
					log.debug("Exception : ",e);
				}
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
	public String migrateLinksInHtmlBlob(Element src, Element target){
		Elements li_Elements = src.getElementsByTag("li");
		for(Element ele : li_Elements){
			Elements aElements = ele.getElementsByTag("a");
			if(aElements != null && !aElements.isEmpty()){
				Element aElement = aElements.first();
				String aText = aElement.text();
				String aHref = aElement.attr("href");
			}else{
				sb.append(Constants.LEFT_GRID_ANCHOR_ELEMENTS_NOT_FOUND);
			}
		}
		return null;
	}
}