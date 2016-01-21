package com.cisco.dse.global.migration.rtechnology;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.cisco.dse.global.migration.config.BaseAction;
import com.cisco.dse.global.migration.config.Constants;
import com.cisco.dse.global.migration.config.FrameworkUtils;

public class RProductTechnology extends BaseAction {

	Document doc;

	StringBuilder sb = new StringBuilder(1024);

	static Logger log = Logger.getLogger(RProductTechnology.class);

	public String translate(String host, String loc, String prod, String type, String catType,
			String locale, Session session, Map<String, String> urlMap) throws IOException,
			ValueFormatException, VersionException, LockException,
			ConstraintViolationException, RepositoryException {
		BasicConfigurator.configure();
		// Repo node paths

		String pagePropertiesPath = "/content/<locale>/"+catType+"/switches/technology/jcr:content/";
		String pageUrl = host + "/content/<locale>/"+catType+"/switches/technology.html";

		pageUrl = pageUrl.replace("<locale>", locale);
		pagePropertiesPath = pagePropertiesPath.replace("<locale>", locale);
		String technologyLeft = pagePropertiesPath+"content_parsys/technology/layout-technology/gd12v2/gd12v2-left";
		String technologyRight = pagePropertiesPath+"content_parsys/technology/layout-technology/gd12v2/gd12v2-right";

		log.debug("Path is "+technologyLeft);
		log.debug("Path is "+technologyRight);

		sb.append("<td>" + "<a href="+pageUrl+">"+pageUrl+"</a>"+"</td>");
		sb.append("<td>" + "<a href="+loc+">"+loc +"</a>"+ "</td>");
		sb.append("<td><ul>");

		javax.jcr.Node technologyLeftNode = null;
		javax.jcr.Node technologyRightNode = null;
		javax.jcr.Node pageJcrNode = null;
		try{
			technologyLeftNode = session.getNode(technologyLeft);
			technologyRightNode = session.getNode(technologyRight);
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

				//start Text Migration
				try{
					Element textEle = doc.select("div.cc00-pilot").first();
					migrateText(textEle , technologyLeftNode , locale, urlMap);
				}catch(Exception e){
					sb.append(Constants.EXCEPTION_TEXT_COMPONENT);
				}
				//End text Migration

				//Start tile Border Migration
				try{
					Elements tileBorderEle = doc.select("div.cc23-pilot");
					migrateTileElements(tileBorderEle , technologyRightNode,urlMap);
				}catch(Exception e){
					sb.append(Constants.EXCEPTION_TEXT_COMPONENT);
				}
				//End tile border migration

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


	private void migrateText(Element textEle, Node technologyLeftNode, String locale, Map<String, String> urlMap) throws RepositoryException {
		if(textEle != null){
			NodeIterator textNodes = technologyLeftNode.hasNode("text")?technologyLeftNode.getNodes("text*"):null;
			if(textNodes != null){
				int size = (int)textNodes.getSize();
				int eleSize = 0;
				Node textHNode = textNodes.nextNode();
				String hEle = textEle.getElementsByTag("h1").first().outerHtml();
				if(hEle != null){
					eleSize++;
					textHNode.setProperty("text", hEle);
				}else{
					sb.append(Constants.TEXT_HAEDING_NOT_FOUND);
				}
				if(textNodes.hasNext()){
					Node textNode = textNodes.nextNode();
					textEle.getElementsByTag("h1").first().remove();
					String text = textEle.outerHtml();
					if(text != ""){
						eleSize++;
						try{
							text = FrameworkUtils.extractHtmlBlobContent(textEle, "", locale, sb, urlMap);
						}catch(Exception e){
							sb.append(Constants.UNABLE_TO_MIGRATE_TEXT_IMAGE);
						}
						textNode.setProperty("text", text);
					}else{
						sb.append(Constants.TEXT_ELEMENT_NOT_FOUND);
					}
				}else{
					sb.append(Constants.TEXT_NODE_NOT_FOUND);
				}
				if(textNodes.hasNext()){
					eleSize++;
					Node textNode = textNodes.nextNode();
					textEle = doc.select("div.nn13-pilot").first();
					if(textEle != null){
						textNode.setProperty("text", textEle.html());
					}else{
						sb.append(Constants.LIST_COMPONENT_NOT_FOUND);
					}
				}else{
					sb.append(Constants.TEXT_NODE_NOT_FOUND);
				}
				if(size > eleSize){
					sb.append(Constants.TEXT_NODE_COUNT+size+Constants.TEXT_ELEMENT_COUNT+eleSize+".</li>");
				}
			}else{
				sb.append(Constants.TEXT_NODE_NOT_FOUND);
			}
		}else{
			sb.append(Constants.TEXT_ELEMENT_NOT_FOUND);
		}
	}

	private void migrateTileElements(Elements tileBorderEle,
			Node technologyRightNode,Map<String,String> urlMap) throws PathNotFoundException, RepositoryException, JSONException {
		if(tileBorderEle != null){
			//List Component
			Element listEle = tileBorderEle.first();
			Element ulEle = listEle.getElementsByClass("compact").first();
			int index = 0;
			if(ulEle.outerHtml() != ""){
				index++;
				Node listNode = technologyRightNode.hasNode("list")?technologyRightNode.getNode("list"):null;
				if(listNode != null){
					String title = listEle.getElementsByTag("h3").first().text();
					if(title != ""){
						listNode.setProperty("title", title);
					}else{
						sb.append(Constants.LIST_HEADING_COMPONENT_NOT_FOUND);
					}
					Node eleListNode = listNode.hasNode("element_list_0")?listNode.getNode("element_list_0"):null;
					if(eleListNode != null){
						Elements liEle = ulEle.getElementsByTag("li");
						JSONObject obj = new JSONObject();
						List<String> listAdd = new ArrayList<String>();
						Element a;
						String linktext;
						String linkurl;
						for(Element li : liEle){
							boolean check = false;
							a = li.getElementsByTag("a").first();
							if(a != null){
								linktext = a.text();
								linkurl = a.absUrl("href");
								if(StringUtil.isBlank(linkurl)){
									linkurl = a.attr("href");
								}
								if(a.hasAttr("target")){
									check= true;
								}
							}else{
								linktext = li.ownText();
								linkurl = "";
							}
							obj.put("linktext",linktext);
							obj.put("linkurl",linkurl);
							obj.put("icon","");
							obj.put("size","");
							obj.put("description","");
							obj.put("openInNewWindow",check);
							listAdd.add(obj.toString());
						}
						eleListNode.setProperty("listitems", listAdd.toArray(new String[listAdd.size()]));	
					}else{
						sb.append(Constants.LIST_NODE_NOT_FOUND);
					}
				}else{
					sb.append(Constants.LIST_NODE_NOT_FOUND);
				}
			}else{
				sb.append(Constants.LIST_COMPONENT_NOT_FOUND);
			}
			//End of List

			//Tile Elements
			Element tileEle;
			NodeIterator tileNodes = technologyRightNode.hasNode("tile_bordered_0")?technologyRightNode.getNodes("tile_bordered_*"):null;
			if(tileNodes != null){
				int nodeSize = (int)tileNodes.getSize();
				int eleSize = tileBorderEle.size()-index;
				Node tileNode;
				if(nodeSize == eleSize){
					for(int count = index ; count < tileBorderEle.size() ; count++){
						tileEle = tileBorderEle.get(count);
						if(tileEle.outerHtml() != ""){
							String title = "";
							String lightboxtrigger = "none";
							String lightboxid = null;
							Element titleh3 = tileEle.getElementsByTag("h3").first();
							Element titleA = titleh3.getElementsByTag("a").first();
							if(titleA == null){
								title = titleh3.text();
							}else{
								title = titleA.text();
								lightboxtrigger = "title";
								lightboxid = titleA.hasAttr("rel")?titleA.attr("rel"):null;
							}
							String description = tileEle.getElementsByTag("p").first().html();
							Element a = tileEle.getElementsByTag("p").last().getElementsByTag("a").first();
							tileNode = tileNodes.nextNode();
							tileNode.setProperty("title", title);
							tileNode.setProperty("description", description);
							tileNode.setProperty("linktext", a.text());
							String aHref = a.absUrl("href");
							if(StringUtil.isBlank(aHref)){
								aHref = a.attr("href");
							}
							aHref = FrameworkUtils.getLocaleReference(aHref, urlMap);
							tileNode.setProperty("linkurl",aHref );
							tileNode.setProperty("lightboxtrigger", lightboxtrigger);
							if(lightboxid != null){
								tileNode.setProperty("lightboxid", lightboxid);
							}
						}else{
							sb.append(Constants.TILEBORDER_COMPONENT_NOT_FOUND);
						}
					}
				}else if(nodeSize > eleSize){
					for(int count = index ; count < tileBorderEle.size() ; count++){
						tileEle = tileBorderEle.get(count);
						if(tileEle.outerHtml() != ""){
							String title = "";
							String lightboxtrigger = "none";
							String lightboxid = null;
							Element titleh3 = tileEle.getElementsByTag("h3").first();
							Element titleA = titleh3.getElementsByTag("a").first();
							if(titleA == null){
								title = titleh3.text();
							}else{
								title = titleA.text();
								lightboxtrigger = "title";
								lightboxid = titleA.hasAttr("rel")?titleA.attr("rel"):null;
							}
							String description = tileEle.getElementsByTag("p").first().html();
							Element a = tileEle.getElementsByTag("p").last().getElementsByTag("a").first();
							tileNode = tileNodes.nextNode();
							tileNode.setProperty("title", title);
							tileNode.setProperty("description", description);
							tileNode.setProperty("linktext", a.text());
							String aHref = a.absUrl("href");
							if(StringUtil.isBlank(aHref)){
								aHref = a.attr("href");
							}
							aHref = FrameworkUtils.getLocaleReference(aHref, urlMap);
							tileNode.setProperty("linkurl", aHref);
							tileNode.setProperty("lightboxtrigger", lightboxtrigger);
							if(lightboxid != null){
								tileNode.setProperty("lightboxid", lightboxid);
							}
						}else{
							sb.append(Constants.TILEBORDER_COMPONENT_NOT_FOUND);
						}
					}
					if(tileNodes.hasNext()){
						sb.append(Constants.MISMATCH_IN_TILEBORDER_COUNT + eleSize + Constants.TILEBORDER_NODE_COUNT + nodeSize +".</li>");
					}
				}else if(nodeSize < eleSize){
					for(int count = index ; count < tileBorderEle.size() ; count++){
						if(tileNodes.hasNext()){
							tileEle = tileBorderEle.get(count);
							if(tileEle.outerHtml() != ""){
								String title = "";
								String lightboxtrigger = "none";
								String lightboxid = null;
								Element titleh3 = tileEle.getElementsByTag("h3").first();
								Element titleA = titleh3.getElementsByTag("a").first();
								if(titleA == null){
									title = titleh3.text();
								}else{
									title = titleA.text();
									lightboxtrigger = "title";
									lightboxid = titleA.hasAttr("rel")?titleA.attr("rel"):null;
								}
								String description = tileEle.getElementsByTag("p").first().html();
								Element a = tileEle.getElementsByTag("p").last().getElementsByTag("a").first();
								tileNode = tileNodes.nextNode();
								tileNode.setProperty("title", title);
								tileNode.setProperty("description", description);
								tileNode.setProperty("linktext", a.text());
								String aHref = a.absUrl("href");
								if(StringUtil.isBlank(aHref)){
									aHref = a.attr("href");
								}
								aHref = FrameworkUtils.getLocaleReference(aHref, urlMap);
								tileNode.setProperty("linkurl", aHref);
								tileNode.setProperty("lightboxtrigger", lightboxtrigger);
								if(lightboxid != null){
									tileNode.setProperty("lightboxid", lightboxid);
								}
							}else{
								sb.append(Constants.TILEBORDER_COMPONENT_NOT_FOUND);
							}
						}
					}
				}
			}else{
				sb.append(Constants.TILE_BORDERED_NODES_NOT_FOUND);
			}
		}else{
			sb.append(Constants.TILE_BORDERED_ELEMENTS_NOT_FOUND);
		}

	}
}