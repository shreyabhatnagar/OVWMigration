package com.cisco.dse.global.migration.web;

import java.io.IOException;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.cisco.dse.global.migration.config.BaseAction;
import com.cisco.dse.global.migration.config.Constants;
import com.cisco.dse.global.migration.config.FrameworkUtils;

public class WebVariation8 extends BaseAction {

	Document doc;

	StringBuilder sb = new StringBuilder(1024);

	static Logger log = Logger.getLogger(WebVariation8.class);

	public String translate(String host, String loc, String prod, String type, String catType,
			String locale, Session session, Map<String, String> urlMap) throws IOException,
			ValueFormatException, VersionException, LockException,
			ConstraintViolationException, RepositoryException {
		BasicConfigurator.configure();
		// Repo node paths
		try {
			log.debug("In the translate method");
			log.debug("In the translate method, catType is :" + catType);
			String pagePropertiesPath = "/content/<locale>/"+catType+"/industries/jcr:content";

			String pageUrl = host + "/content/<locale>/"+ catType + "/industries.html";
			pageUrl = pageUrl.replace("<locale>", locale);
			pagePropertiesPath = pagePropertiesPath.replace("<locale>", locale);
			String industriesLeft = pagePropertiesPath+"/content_parsys/solutions/layout-solutions/gd22v2/gd22v2-left";
			String industriesRight = pagePropertiesPath+"/content_parsys/solutions/layout-solutions/gd22v2/gd22v2-right";

			log.debug("Path is "+industriesLeft);
			log.debug("Path is "+industriesRight);

			sb.append("<td>" + "<a href="+pageUrl+">"+pageUrl+"</a>"+"</td>");
			sb.append("<td>" + "<a href="+loc+">"+loc +"</a>"+ "</td>");
			sb.append("<td><ul>");

			javax.jcr.Node industriesLeftNode = null;
			javax.jcr.Node industriesRightNode = null;
			javax.jcr.Node pageJcrNode = null;

			industriesLeftNode = session.getNode(industriesLeft);
			industriesRightNode = session.getNode(industriesRight);
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
					Element heroEle = doc.select("div.c50v3-pilot").first();
					Element headingEle = doc.select("div.c00v0-alt1-pilot").first();
					if(heroEle != null){
						migrateHero(industriesLeftNode, heroEle, headingEle , locale, urlMap);
						log.debug("Hero Element Migrated");
					}else{
						sb.append(Constants.HERO_CONTENT_PANEL_ELEMENT_NOT_FOUND);
					}
				}catch(Exception e){
					sb.append(Constants.EXCEPTION_IN_HERO_MIGRATION);
					log.debug("Exception in Hero Element Migration"+e);
				}
				//End Hero Migration

				//start Html Blobs migration
				try{
					log.debug("start Html Blobs migration");
					Elements midEles = !doc.select("div.gd42v1-pilot").isEmpty() ?doc.select("div.gd42v1-pilot").first().select("div.c00-pilot"):null;
					if(midEles == null){
						
						midEles = doc.select("div.standard-holder-in");
						
					}
					if(midEles != null){
						migrateHtmlBlobs(midEles , industriesLeftNode, locale , urlMap);
						log.debug("Html Blobs Element Migrated");
					}else{
						sb.append(Constants.HTMLBLOB_CONTENT_DOES_NOT_EXIST);
					}
				}catch(Exception e){
					sb.append(Constants.EXCEPTION_IN_HTMLBLOB);
					log.debug("Exception in Html Blobs Element Migration", e);
				}
				//end Html Blobs migration

				//start RightRail migration
				try{
					log.debug("start RightRail migration");
					Element topRightEle = doc.select("div.f-holder").first();
					if(topRightEle == null){
						topRightEle = doc.select("div.feature-holder").first();
					}
					migrateHelpHtmlBlob(topRightEle , industriesRightNode, locale , urlMap);
					log.debug("RightRail Element Migrated");
				}catch(Exception e){
					sb.append(Constants.EXCEPTION_IN_HTMLBLOB);
					log.debug("Exception in RightRail Element Migration"+e);
				}
				//end RightRail migration

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


	private void migrateHero(Node htmlBlob , Element htmlBlobEle , Element headingEle, String locale , Map<String, String> urlMap) throws PathNotFoundException, RepositoryException {
		Node htmlBlobNode = htmlBlob.hasNode("htmlblob")?htmlBlob.getNode("htmlblob"):null;
		if(htmlBlobNode != null){
			String heading = "";
			if(headingEle != null){
				heading = FrameworkUtils.extractHtmlBlobContent(headingEle, "", locale, sb, urlMap);
			}else{
				sb.append(Constants.LIST_HEADING_COMPONENT_NOT_FOUND);
			}
			htmlBlobNode.setProperty("html", FrameworkUtils.extractHtmlBlobContent(htmlBlobEle, "", locale, sb, urlMap)+heading);
		}else{
			sb.append(Constants.HERO_NODE_NOT_AVAILABLE);
		}
	}

	private void migrateHelpHtmlBlob(Element topRightEle, Node industriesRightNode, String locale, Map<String, String> urlMap) throws PathNotFoundException, RepositoryException {
		if(topRightEle != null){
			Node htmlBlobNode = industriesRightNode.hasNode("htmlblob")?industriesRightNode.getNode("htmlblob"):null;
			if(htmlBlobNode != null){
				String outerHtml1 = FrameworkUtils.extractHtmlBlobContent(topRightEle, "", locale, sb, urlMap);
				String outerHtml2 = "";
				topRightEle = doc.select("div.n13v12-pilot").first();
				if(topRightEle != null){
					outerHtml2 = FrameworkUtils.extractHtmlBlobContent(topRightEle, "", locale, sb, urlMap);
				}
				htmlBlobNode.setProperty("html",outerHtml1+outerHtml2);
			}else{
				sb.append(Constants.RIGHT_GRID_NODE_NOT_FOUND);
			}
		}else{
			sb.append(Constants.RIGHT_GRID_ELEMENT_NOT_FOUND);
		}
	}

	private void migrateHtmlBlobs(Elements midEles, Node industriesLeftNode,
			String locale, Map<String, String> urlMap) throws RepositoryException {
		if(industriesLeftNode.hasNode("gd42v1")){
			Node gd42viNode = industriesLeftNode.getNode("gd42v1");
			NodeIterator midNodes = gd42viNode.hasNode("gd42v1-left")?gd42viNode.getNodes("gd42v1-*"):null;
			if(midNodes != null){
				int size = (int)midNodes.getSize();
				int eleSize = midEles.size();
				if(size == eleSize){
					for(Element htmlBlob : midEles){
						Node midNode = midNodes.nextNode();
						if(midNode.hasNode("htmlblob")){
							Node htmlBlobNode = midNode.getNode("htmlblob");
							String outerHtml = FrameworkUtils.extractHtmlBlobContent(htmlBlob, "", locale, sb, urlMap);
							htmlBlobNode.setProperty("html", outerHtml);
						}else{
							sb.append(Constants.HTMLBLOB_NODE_NOT_FOUND);
						}
					}
				}else if(size > eleSize){
					for(Element htmlBlob : midEles){
						Node midNode = midNodes.nextNode();
						if(midNode.hasNode("htmlblob")){
							Node htmlBlobNode = midNode.getNode("htmlblob");
							String outerHtml = FrameworkUtils.extractHtmlBlobContent(htmlBlob, "", locale, sb, urlMap);
							htmlBlobNode.setProperty("html", outerHtml);
						}else{
							sb.append(Constants.HTMLBLOB_NODE_NOT_FOUND);
						}
					}
					if(midNodes.hasNext()){
						sb.append(Constants.MISMATCH_IN_HTMLBLOB_NODES+eleSize+Constants.HTMLBLOB_NODES_COUNT+size+".</li>");
					}
				}else if(size < eleSize){
					for(Element htmlBlob : midEles){
						if(midNodes.hasNext()){
							Node midNode = midNodes.nextNode();
							if(midNode.hasNode("htmlblob")){
								Node htmlBlobNode = midNode.getNode("htmlblob");
								String outerHtml = FrameworkUtils.extractHtmlBlobContent(htmlBlob, "", locale, sb, urlMap);
								htmlBlobNode.setProperty("html", outerHtml);
							}else{
								sb.append(Constants.HTMLBLOB_NODE_NOT_FOUND);
							}
						}else{
							sb.append(Constants.MISMATCH_IN_HTMLBLOB_ELEMENT+eleSize+Constants.HTMLBLOB_NODES_COUNT+size+".</li>");
						}
					}
				}
			}else{
				sb.append(Constants.HTMLBLOB_NODE_DOES_NOT_EXIST);
			}
		}else{
			sb.append(Constants.HTMLBLOB_NODE_DOES_NOT_EXIST);
		}
	}
}
