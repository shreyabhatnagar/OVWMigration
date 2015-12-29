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

	public String translate(String host, String loc, String prod, String type, String catType,
			String locale, Session session) throws IOException,
			ValueFormatException, VersionException, LockException,
			ConstraintViolationException, RepositoryException {
		log.debug("Inside Trsnslate method of RSolutionIndex");
		log.debug("In the translate method, catType is :" + catType);
		BasicConfigurator.configure();
		// Repo node paths

		String pagePropertiesPath = "/content/<locale>/" + catType
				+ "/index/jcr:content";
		String pageUrl = host + "/content/<locale>/" + catType + "/index.html";
		pageUrl = pageUrl.replace("<locale>", locale);
		pagePropertiesPath = pagePropertiesPath.replace("<locale>", locale);
		String indexLeft = pagePropertiesPath+"/content_parsys/overview/layout-overview/gd12v2/gd12v2-left";
		String indexRight = pagePropertiesPath+"/content_parsys/overview/layout-overview/gd12v2/gd12v2-right";

		log.debug("Path is "+indexLeft);
		log.debug("Path is "+indexRight);

		sb.append("<td>" + "<a href="+pageUrl+">"+pageUrl+"</a>"+"</td>");
		sb.append("<td>" + "<a href="+loc+">"+loc +"</a>"+ "</td>");
		sb.append("<td><ul>");

		javax.jcr.Node indexLeftNode = null;
		javax.jcr.Node indexRightNode = null;
		javax.jcr.Node pageJcrNode = null;
		try{
			indexLeftNode = session.getNode(indexLeft);
			indexRightNode = session.getNode(indexRight);
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
					Element heroEle = doc.select("div.c50-pilot").first();
					migrateHero(heroEle , indexLeftNode , locale);
					log.debug("Hero Element Migrated");
				}catch(Exception e){
					sb.append(Constants.EXCEPTION_IN_HEROELEMENT);
					log.debug("Exception in Hero Element Migration"+e);
				}
				//End Hero Migration

				//start Html blob migration
				try{
					log.debug("start htmlblob migration");
					Element htmlBlobEle = doc.select("div.gd22-pilot").first();
					migrateHtmlBlob(htmlBlobEle , indexLeftNode);
					log.debug("htmlblob Element Migrated");
				}catch(Exception e){
					sb.append(Constants.EXCEPTION_IN_HTMLBLOB);
					log.debug("Exception in HtmlBlob Element Migration"+e);
				}
				//end Html blob migration

				//start spotlight migration
				try{
					log.debug("start spotlight migration");
					Elements spotLightEles = doc.select("div.c11-pilot");
					migrateSpotLight(spotLightEles , indexLeftNode, locale);
					log.debug("spotlight Element Migrated");
				}catch(Exception e){
					sb.append(Constants.EXCEPTION_SPOTLIGHT_COMPONENT);
					log.debug("Exception in spotlight Element Migration"+e);
				}
				//end spotlight migration

				//start let us help migration
				try{
					log.debug("start let us help migration");
					Element topRightEle = doc.select("div.f-holder").first();
					migrateHelpHtmlBlob(topRightEle , indexRightNode);
					log.debug("let us help Element Migrated");
				}catch(Exception e){
					sb.append(Constants.EXCEPTION_IN_HTMLBLOB);
					log.debug("Exception in let us help Element Migration"+e);
				}
				//end let us help migration

				//start RightRail list migration
				try{
					log.debug("start RightRail list migration");
					Element rightRailEle = doc.select("div.gd12v2-right").first();
					migrateRightList(rightRailEle , indexRightNode);
					log.debug("RightRail list Element Migrated");
				}catch(Exception e){
					sb.append(Constants.EXCEPTION_IN_UPDATING_LIST_COMPONENT);
					log.debug("Exception in RightRail list Element Migration"+e);
				}
				//end RightRail list migration

				//start TileBorder migration
				try{
					log.debug("start TileBorder migration");
					Element tile_BorderedEle = doc.select("div.tile_bordered").first();
					if(tile_BorderedEle != null){
						sb.append(Constants.TILE_BORDERED_NODES_NOT_FOUND);
						log.debug("TileBorder Element not Migrated");
					}
				}catch(Exception e){
					sb.append(Constants.UNABLE_TO_MIGRATE_TILE_BORDERED_COMPONENTS);
					log.debug("Exception in TileBorder Element Migration"+e);
				}
				//end TileBorder migration
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


	private void migrateHero(Element heroEle, Node indexLeftNode, String locale) throws PathNotFoundException, RepositoryException {
		if(heroEle != null){
			Node heroNode = indexLeftNode.hasNode("hero_large")?indexLeftNode.getNode("hero_large"):null;
			if(heroNode != null){
				Node heroPanelNode = heroNode.hasNode("heropanel_0")?heroNode.getNode("heropanel_0"):null;
				if(heroPanelNode != null){
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
						heroPanelNode.setProperty("linkurl", aHero.attr("href"));
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
					sb.append(Constants.HERO_CONTENT_NODE_NOT_FOUND);
				}
			}else{
				sb.append(Constants.HERO_CONTENT_NODE_NOT_FOUND);
			}
		}else{
			sb.append(Constants.HERO_CONTENT_PANEL_ELEMENT_NOT_FOUND);
		}

	}

	private void migrateHtmlBlob(Element htmlBlobEle, Node indexLeftNode) throws PathNotFoundException, RepositoryException, JSONException {
		if(htmlBlobEle != null){
			Node gdNode = indexLeftNode.hasNode("gd22v1_0")?indexLeftNode.getNode("gd22v1_0"):null;
			if(gdNode != null){
				NodeIterator gdLeftRightNodes = gdNode.hasNode("gd22v1-left")?gdNode.getNodes("gd22v1-*"):null;
				if(gdLeftRightNodes != null){
					Element listEle = htmlBlobEle.getElementsByClass("gd22v1-left").first();
					Node nextNode = gdLeftRightNodes.nextNode();
					if(listEle != null){
						Node listNode  = nextNode.hasNode("list")?nextNode.getNode("list"):null;
						if(listNode != null){
							Element heading = listEle.getElementsByTag("h2").first();
							if(heading != null){
								listNode.setProperty("title", heading.text());
							}else{
								sb.append(Constants.LIST_HEADING_COMPONENT_NOT_FOUND);
							}
							Elements listItems = listEle.getElementsByTag("a");
							if(listItems != null){
								Node eleListNode  = listNode.hasNode("element_list_0")?listNode.getNode("element_list_0"):null;
								if(eleListNode != null){
									JSONObject obj = new JSONObject();
									List<String> listAdd = new ArrayList<String>();
									for(Element anchor : listItems){
										obj.put("linktext",anchor.text());
										obj.put("linkurl",anchor.attr("href"));
										obj.put("icon","none");
										obj.put("size","");
										obj.put("description","");
										obj.put("openInNewWindow",false);
										listAdd.add(obj.toString());
									}
									eleListNode.setProperty("listitems", listAdd.toArray(new String[listAdd.size()]));
								}else{
									sb.append(Constants.NO_LIST_NODE_FOUND);
								}
							}else{
								sb.append(Constants.LIST_ELEMENT_NOT_FOUND);
							}
						}else{
							sb.append(Constants.NO_LIST_NODE_FOUND);
						}
					}else{
						sb.append(Constants.LIST_ELEMENT_NOT_FOUND);
					}
					Element htmlBlob = htmlBlobEle.getElementsByClass("gd22v1-right").first();
					if(htmlBlob != null){
						if(gdLeftRightNodes.hasNext()){
							Node htmlBlobNode = gdLeftRightNodes.nextNode();
							Node htmlBlobTextNode  = htmlBlobNode.hasNode("text_0")?htmlBlobNode.getNode("text_0"):null;
							if(htmlBlobTextNode != null){
								Element heading = htmlBlob.getElementsByTag("h2").first();
								if(heading != null){
									htmlBlobTextNode.setProperty("text", heading.outerHtml());
								}else{
									sb.append(Constants.LIST_HEADING_COMPONENT_NOT_FOUND);
								}
							}else{
								sb.append(Constants.TEXT_NODE_NOT_FOUND);
							}
							Node htmlBlobEleNode  = htmlBlobNode.hasNode("htmlblob")?htmlBlobNode.getNode("htmlblob"):null;
							if(htmlBlobEleNode != null){
								htmlBlob.getElementsByTag("h2").first().remove();
								htmlBlobEleNode.setProperty("html", htmlBlob.html());
							}else{
								sb.append(Constants.HTMLBLOB_NODE_NOT_FOUND);
							}
						}else{
							sb.append(Constants.HTMLBLOB_NODE_NOT_FOUND);
						}
					}else{
						sb.append(Constants.HTMLBLOB_ELEMENT_NOT_FOUND);
					}
				}else{
					sb.append(Constants.NO_LIST_NODES_FOUND);
				}
			}else{
				sb.append(Constants.NO_LIST_NODES_FOUND);
			}
		}else{
			sb.append(Constants.HTMLBLOB_ELEMENT_NOT_FOUND);
		}
	}

	private void migrateSpotLight(Elements spotLightEles, Node indexLeftNode, String locale) throws RepositoryException {
		if(spotLightEles != null){
			int spSize = spotLightEles.size();
			NodeIterator spotLightNodes = indexLeftNode.hasNode("spotlight_medium_v2")?indexLeftNode.getNodes("spotlight_medium_v2*"):null;
			if(spotLightNodes != null){
				int size = (int)spotLightNodes.getSize();
				if(size == spSize){
					Node spotLightNode;
					Element title = null;
					Element description = null;
					String fileReference = "";
					for(Element ele : spotLightEles){
						spotLightNode = spotLightNodes.nextNode();
						setSpotLight(ele , spotLightNode , title , description, fileReference, locale);
					}
				}else if(size < spSize){
					Node spotLightNode;
					Element title = null;
					Element description = null;
					String fileReference = "";
					for(Element ele : spotLightEles){
						spotLightNode = spotLightNodes.nextNode();
						if(spotLightNodes.hasNext()){
							setSpotLight(ele , spotLightNode , title , description, fileReference, locale);
						}else{
							sb.append(Constants.SPOTLIGHT_ELEMENT_MISMATCH + size + Constants.SPOTLIGHT_ELEMENT_COUNT + spSize +".</li>");
						}
					}
				}else if(size > spSize){
					Node spotLightNode;
					Element title = null;
					Element description = null;
					String fileReference = "";
					for(Element ele : spotLightEles){
						spotLightNode = spotLightNodes.nextNode();
						setSpotLight(ele , spotLightNode , title , description, fileReference, locale);
					}
					if(spotLightNodes.hasNext()){
						sb.append(Constants.SPOTLIGHT_ELEMENT_MISMATCH + size + Constants.SPOTLIGHT_ELEMENT_COUNT + spSize +".</li>");
					}
				}
			}else{
				sb.append(Constants.SPOTLIGHT_NODE_NOT_FOUND);
			}
		}else{
			sb.append(Constants.SPOTLIGHT_ELEMENT_NOT_FOUND);
		}

	}

	private void setSpotLight(Element ele, Node spotLightNode, Element title, Element description, String fileReference , String locale) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
		title = ele.getElementsByTag("h2").first().getElementsByTag("a").first();
		description = ele.getElementsByTag("p").first();
		fileReference = FrameworkUtils.extractImagePath(ele, sb);
		fileReference = FrameworkUtils.migrateDAMContent(fileReference, "", locale, sb);
		if(title != null){
			spotLightNode.setProperty("title", title.text());
			Node titleNode = spotLightNode.hasNode("titlelink")?spotLightNode.getNode("titlelink"):null;
			if(titleNode != null){
				titleNode.setProperty("url", title.attr("href"));
			}else{
				sb.append(Constants.SPOTLIGHT_TITLELINK_NODE_NOT_FOUND);
			}
		}else{
			sb.append(Constants.SPOTLIGHT_HEADING_ELEMENT_NOT_FOUND);
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
	}

	private void migrateHelpHtmlBlob(Element topRightEle, Node indexRightNode) throws PathNotFoundException, RepositoryException {
		if(topRightEle != null){
			Node htmlBlobNode = indexRightNode.hasNode("htmlblob")?indexRightNode.getNode("htmlblob"):null;
			if(htmlBlobNode != null){
				htmlBlobNode.setProperty("html", topRightEle.html());
			}else{
				sb.append(Constants.HTMLBLOB_NODE_NOT_FOUND);
			}
		}else{
			sb.append(Constants.HTMLBLOB_ELEMENT_NOT_FOUND);
		}
		Node partnerhelpNode = indexRightNode.hasNode("partnerhelp")?indexRightNode.getNode("partnerhelp"):null;
		if(partnerhelpNode != null){
			sb.append(Constants.PARTNER_HELP_COMPONENT_NOT_FOUND);
		}
	}

	private void migrateRightList(Element rightRailEle, Node indexRightNode) throws RepositoryException, JSONException {
		if(rightRailEle != null){
			NodeIterator listNodes = indexRightNode.hasNode("list")?indexRightNode.getNodes("list*"):null;
			if(listNodes != null){
				int size = (int)listNodes.getSize();
				Elements listEles = rightRailEle.getElementsByClass("n13-pilot");
				if(listEles != null){
					int eleSize = listEles.size();
					if(size == eleSize){
						Node listNode;
						Element title = null;
						Element subTitle = null;
						Elements listItems = null;
						Node eleListNode = null;
						NodeIterator eleListNodes = null;
						for(Element list : listEles){
							listNode = listNodes.nextNode();
							setRightList(listNode,list,title,subTitle,listItems,eleListNodes,eleListNode);
						}
					}else if(size > eleSize){
						Node listNode;
						Element title = null;
						Element subTitle = null;
						Elements listItems = null;
						Node eleListNode = null;
						NodeIterator eleListNodes = null;
						for(Element list : listEles){
							listNode = listNodes.nextNode();
							setRightList(listNode,list,title,subTitle,listItems,eleListNodes,eleListNode);
						}
						if(listNodes.hasNext()){
							sb.append(Constants.MISMATCH_IN_LIST_NODES +eleSize+Constants.LIST_NODES_COUNT +size+ ".</li>");
						}
					}else if(size < eleSize){
						Node listNode;
						Element title = null;
						Element subTitle = null;
						Elements listItems = null;
						Node eleListNode = null;
						NodeIterator eleListNodes = null;
						for(Element list : listEles){
							if(listNodes.hasNext()){
								listNode = listNodes.nextNode();
								setRightList(listNode,list,title,subTitle,listItems,eleListNodes,eleListNode);
							}else{
								sb.append(Constants.MISMATCH_IN_LIST_ELEMENT +eleSize+Constants.LIST_NODES_COUNT +size+ ".</li>");
							}
						}
					}
				}else{
					sb.append(Constants.RIGHT_GRID_ELEMENT_NOT_FOUND);
				}
			}else{
				sb.append(Constants.RIGHT_GRID_ELEMENT_LIST_NODE_NOT_FOUND);
			}
		}else{
			sb.append(Constants.RIGHT_GRID_ELEMENT_NOT_FOUND);
		}
	}

	private void setRightList(Node listNode, Element list, Element title, Element subTitle, Elements listItems, NodeIterator eleListNodes, Node eleListNode) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException, JSONException {
		title = list.getElementsByTag("h2").first();
		if(title != null){
			listNode.setProperty("title", title.text());
		}else{
			sb.append(Constants.RIGHT_LIST_COMPONENT_TITLE_NOT_FOUND);
		}
		eleListNodes  = listNode.hasNode("element_list_0")?listNode.getNodes("element_list_*"):null;
		if((int)eleListNodes.getSize() > 1){
			subTitle = list.getElementsByTag("h3").first();
			if(subTitle != null){
				eleListNode = eleListNodes.nextNode();
				eleListNode.setProperty("subtitle", subTitle.text());
			}else{
				sb.append(Constants.RIGHT_LIST_COMPONENT_SUBTITLE_NOT_FOUND);
			}
		}
		listItems = list.getElementsByTag("a");
		if(listItems != null){
			eleListNode = eleListNodes.nextNode();
			if(eleListNode != null){
				JSONObject obj = new JSONObject();
				List<String> listAdd = new ArrayList<String>();
				for(Element anchor : listItems){
					obj.put("linktext",anchor.text());
					obj.put("linkurl",anchor.attr("href"));
					obj.put("icon","none");
					obj.put("size","");
					obj.put("description","");
					obj.put("openInNewWindow",false);
					listAdd.add(obj.toString());
				}
				eleListNode.setProperty("listitems", listAdd.toArray(new String[listAdd.size()]));
			}else{
				sb.append(Constants.NO_LIST_NODE_FOUND);
			}
		}else{
			sb.append(Constants.LIST_ELEMENT_NOT_FOUND);
		}

	}

}