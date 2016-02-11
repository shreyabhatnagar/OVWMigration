package com.cisco.dse.global.migration.technology;

/* S.No			Name		Date		Description of change
 * 1			Aziz		22-jan-16	Wrote the code.
 * 2			Aziz		29-jan-16	commited the code.
 * */

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
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.cisco.dse.global.migration.config.Constants;
import com.cisco.dse.global.migration.config.BaseAction;
import com.cisco.dse.global.migration.config.FrameworkUtils;

public class TechnologyVariation1 extends BaseAction {

	Document doc;

	StringBuilder sb = new StringBuilder(1024);

	static Logger log = Logger.getLogger(TechnologyVariation1.class);

	public String translate(String host, String loc, String prod, String type, String catType,
			String locale, Session session, Map<String, String> urlMap) throws IOException,
			ValueFormatException, VersionException, LockException,
			ConstraintViolationException, RepositoryException {
		BasicConfigurator.configure();
		// Repo node paths
		try {
			String pagePropertiesPath = "/content/<locale>/"+catType+"/<prod>/technology/jcr:content/";
			String pageUrl = host + "/content/<locale>/"+catType+"/<prod>/technology.html";

			pageUrl = pageUrl.replace("<locale>", locale).replace("<prod>", prod);
			pagePropertiesPath = pagePropertiesPath.replace("<locale>", locale).replace("<prod>", prod);
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

				//start Text migration
				try{
					log.debug("start Text migration");
					Elements textEles = doc.select("div.c00-pilot,div.cc00-pilot,div.nn13-pilot");
					if(textEles != null){
						migrateText(textEles , technologyLeftNode, locale , urlMap);
						log.debug("Text Element Migrated");
					}else{
						sb.append(Constants.TEXT_ELEMENT_NOT_FOUND);
					}
				}catch(Exception e){
					sb.append(Constants.UNABLE_TO_MIGRATE_TEXT);
					log.debug("Exception in Text Element Migration"+e);
				}
				//End of Text Migration.

				//start of list Component migration.
				try{
					log.debug("start of list Component migration.");
					Element listEle = doc.getElementsByClass("n13-pilot").first();
					if(listEle != null){
						migrateList(listEle,technologyRightNode,locale,urlMap);
					}else{
						sb.append(Constants.LIST_COMPONENT_NOT_FOUND);
					}
				}catch(Exception e){
					log.error("Exception in list migration");
					sb.append(Constants.UNABLE_TO_MIGRATE_LIST_COMPONENT);
				}
				//End of tile border component migration

				//start of tile Border Component migration.
				try{
					log.debug("start of tile Border Component migration.");
					Elements tileEles = doc.select("div.c23-pilot,div.cc23-pilot");
					migrateTileBoreder(tileEles,technologyRightNode,locale,urlMap);
				}catch(Exception e){
					log.error("Exception in tileBolder migration");
					sb.append(Constants.UNABLE_TO_MIGRATE_TILE_BORDERED_COMPONENTS);
				}
				//End of tile border component migration

			}else{
				sb.append(Constants.URL_CONNECTION_EXCEPTION);	
			}
		} catch (Exception e) {
			log.error(e);
			sb.append(Constants.URL_CONNECTION_EXCEPTION);
		}


		sb.append("</ul></td>");
		session.save();
		return sb.toString();
	}	
	private void migrateList(Element listEle, Node technologyRightNode,
			String locale, Map<String, String> urlMap) throws PathNotFoundException, RepositoryException, JSONException {
		Node listNode = technologyRightNode.hasNode("list")?technologyRightNode.getNode("list"):null;
		if(listNode != null){
			Element title = listEle.getElementsByTag("h2").first();
			if(title != null){
				listNode.setProperty("title",title.text());
			}else{
				sb.append(Constants.LIST_HEADING_COMPONENT_NOT_FOUND);
			}
			Elements aEle = listEle.getElementsByTag("a");
			List<String> aList = new ArrayList<String>();
			Node eleListNode = listNode.hasNode("element_list_0")?listNode.getNode("element_list_0"):null;
			if(eleListNode != null){
				for(Element a : aEle){
					JSONObject obj = new JSONObject();
					String aUrl = a.absUrl("href");
					if(aUrl == ""){
						aUrl = a.attr("href");
					}
					aUrl = FrameworkUtils.getLocaleReference(aUrl, urlMap, locale, sb);
					obj.put("linktext",a.text());
					obj.put("linkurl",aUrl);
					obj.put("icon","none");
					obj.put("size","");
					obj.put("description","");
					obj.put("openInNewWindow",false);
					aList.add(obj.toString());
				}
				eleListNode.setProperty("listitems", aList.toArray(new String[aList.size()]));
			}else{
				sb.append(Constants.NO_LIST_NODES_FOUND);
			}
		}else{
			sb.append(Constants.NO_LIST_NODES_FOUND);
		}

	}
	private void migrateTileBoreder(Elements tileEles,
			Node technologyRightNode, String locale, Map<String, String> urlMap) throws RepositoryException {
		if(tileEles != null){
			int eleSize = tileEles.size();
			NodeIterator tileNodes = technologyRightNode.hasNode("tile_bordered_1")?technologyRightNode.getNodes("tile_bordered*"):null;
			if(tileNodes != null){
				int size = (int)tileNodes.getSize();
				for(Element tileEle : tileEles){
					if(tileNodes.hasNext()){
						Node tileNode = tileNodes.nextNode();
						Element title = tileEle.getElementsByTag("h2").first();
						if(title==null){
							title = tileEle.getElementsByTag("h3").first();
						}
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
							linkurl = linkurl.replaceAll(" ", "").replaceAll("%20", "");
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
	private void migrateText(Elements textElements, Node technologyLeftNode,
			String locale, Map<String, String> urlMap) throws PathNotFoundException, ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
		NodeIterator textNodes = technologyLeftNode.hasNode("text")?technologyLeftNode.getNodes("text*"):null;
		int eleSize = textElements.size();
		if (textNodes != null) {
			int size = (int)textNodes.getSize();
			for(Element textElement : textElements){
				if(textNodes.hasNext()){
					Node textNode = textNodes.nextNode();
					textNode.setProperty("text", FrameworkUtils.extractHtmlBlobContent(textElement, "", locale, sb, urlMap));
				}else{
					sb.append(Constants.TEXT_NODE_MISMATCH+size+Constants.TEXT_ELEMENT_COUNT+eleSize+".</li>");
				}
			}
			if(textNodes.hasNext()){
				sb.append(Constants.TEXT_NODE_MISMATCH+size+Constants.TEXT_ELEMENT_COUNT+eleSize+".</li>");
			}
		}else{
			sb.append(Constants.TEXT_NODE_NOT_FOUND);
		}
	}
}
