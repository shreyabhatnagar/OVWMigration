package com.cisco.dse.global.migration.technology;

/* S.No			Name		Date		Description of change
 * 1			Anudeep		14-Dec-15	Added the Java file to handle the migration of technology variation 2 with 1url.
 * 2			Anudeep		15-Dec-15	Added dam image migration code at  line 89.
 * 3			Anudeep		17-Dec-15	Fixed code for ru_ru locale
 * 
 * */

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.sling.commons.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.cisco.dse.global.migration.config.Constants;
import com.cisco.dse.global.migration.config.BaseAction;
import com.cisco.dse.global.migration.config.FrameworkUtils;

public class TechnologyVariation2 extends BaseAction {

	Document doc;

	StringBuilder sb = new StringBuilder(1024);

	static Logger log = Logger.getLogger(TechnologyVariation2.class);

	public String translate(String host, String loc, String prod, String type, String catType,
			String locale, Session session) throws IOException,
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

			technologyLeft = technologyLeft.replace("<locale>", locale).replace("<prod>", prod);
			technologyRight = technologyRight.replace("<locale>", locale).replace("<prod>", prod);
			javax.jcr.Node technologyLeftNode = null;
			javax.jcr.Node technologyRightNode = null;
			javax.jcr.Node pageJcrNode = null;

			technologyLeftNode = session.getNode(technologyLeft);
			technologyRightNode = session.getNode(technologyRight);
			pageJcrNode = session.getNode(pagePropertiesPath);
			try {
				doc = getConnection(loc);
				if(doc!=null){
					try{
						Element textEle = doc.select("div.c00-pilot").first();
						if(textEle != null){
							Element titleEle = textEle.getElementsByTag("h1").first();
							String titleText = titleEle.outerHtml();
							Node titleNode = technologyLeftNode.hasNode("text")?technologyLeftNode.getNode("text"):null;
							if(titleNode != null){
								titleNode.setProperty("text",titleText);
							}
							else{
								sb.append(Constants.TEXT_NODE_NOT_FOUND);
							}

							Element listEle = doc.select("div.gd-left").select("div.nn13-pilot").first();
							String h1Tag = textEle.getElementsByTag("h1").first().outerHtml();
							String textDesc = FrameworkUtils.extractHtmlBlobContent(textEle, "",locale, sb); //2
							if(textDesc.equals("")){ //3
								textDesc = textEle.outerHtml();
							} //3
							textDesc = textDesc.replaceFirst(h1Tag,"");
							String listTitle = listEle.getElementsByTag("h2").first().outerHtml();
							String listDesc = listEle.getElementsByTag("ul").addClass("no-bullets").outerHtml();
							if(listDesc!=null){
								textDesc = textDesc+listTitle+listDesc;
							}
							Node descNode = technologyLeftNode.hasNode("text_0")?technologyLeftNode.getNode("text_0"):null;
							if(descNode!=null){
								descNode.setProperty("text",textDesc);
							}else{
								sb.append(Constants.TEXT_NODE_NOT_FOUND);
							}
						}	
						else{
							sb.append(Constants.TEXT_ELEMENT_NOT_FOUND);
						}
					}catch(Exception e){
						log.debug("Exception in title",e);
					}
					//end of text	

					//start of list in right rail
					try{
						Elements tileEle = doc.select("div.gd-right").select("div.c23-pilot");
						if(!tileEle.isEmpty()){
							int tileSize = tileEle.size();
							Node listNode = technologyRightNode.hasNode("list")?technologyRightNode.getNode("list"):null;
							if(listNode!=null){
								Element tile = null;
								if(tileSize==1){
									tile = tileEle.first();
								}
								else{
									tile = tileEle.last();
									sb.append(Constants.MISMATCH_OF_LIST_IN_RIGHT_RAIL+tileSize+Constants.LIST_NODES_COUNT+" (1).");
								}
								String title = tile.getElementsByTag("h3").text();
								listNode.setProperty("title",title);

								Elements aEle = tile.getElementsByTag("a");
								List<String> aList = new ArrayList<String>();
								Node eleListNode = listNode.hasNode("element_list_0")?listNode.getNode("element_list_0"):null;
								if(eleListNode!=null){
									for(Element a : aEle){
										JSONObject obj = new JSONObject();
										obj.put("linktext",a.text());
										obj.put("linkurl",a.attr("href"));
										obj.put("icon","none");
										obj.put("size","");
										obj.put("description","");
										obj.put("openInNewWindow",false);
										aList.add(obj.toString());
									}
									eleListNode.setProperty("listitems", aList.toArray(new String[aList.size()]));
								}

							}else{
								sb.append(Constants.NO_LIST_NODES_FOUND);
							}
						}else{
							sb.append(Constants.LIST_NOT_FOUND_IN_RIGHT_RAIL);
						}
					}catch(Exception e){
						log.debug("Exception ", e);
					}
					//end of list in right rail
					
					// ------------------------------------------------------------------------------------------------------------------------------------------
					// start set page properties.

					FrameworkUtils.setPageProperties(pageJcrNode, doc, session, sb);

					// end set page properties.
					// ------------------------------------------------------------------------------------------------------------------------------------------
					
				
				}else{
					sb.append(Constants.URL_CONNECTION_EXCEPTION);	
				}
			} catch (Exception e) {
				log.error(e);
				sb.append(Constants.URL_CONNECTION_EXCEPTION);
			}

		} catch (Exception e) {
			log.debug("Exception ", e);
		}
		sb.append("</ul></td>");
		session.save();
		return sb.toString();
	}	
}
