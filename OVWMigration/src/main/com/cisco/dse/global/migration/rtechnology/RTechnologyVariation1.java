package com.cisco.dse.global.migration.rtechnology;

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
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.cisco.dse.global.migration.config.BaseAction;
import com.cisco.dse.global.migration.config.Constants;
import com.cisco.dse.global.migration.config.FrameworkUtils;

public class RTechnologyVariation1 extends BaseAction {

	Document doc;

	StringBuilder sb = new StringBuilder(1024);

	static Logger log = Logger.getLogger(RTechnologyVariation1.class);

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
			String technologyLeft = pagePropertiesPath+"Grid/category/layout-category/widenarrow/WN-Wide-1";
			String technologyRight = pagePropertiesPath+"Grid/category/layout-category/widenarrow/WN-Narrow-2/list_container";

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
					log.debug("start Text Migration");
					Element textEle = doc.select("div.c00-pilot").first();
					migrateText(textEle , technologyLeftNode , locale, urlMap);
					log.debug("Text is Migrated");
				}catch(Exception e){
					log.debug("Exception in Text Migration");
					sb.append(Constants.EXCEPTION_TEXT_COMPONENT);
				}
				//End text Migration

				//start List Migration
				try{
					log.debug("start List Migration");
					Element listEle = doc.select("div.nn13-pilot").first();
					migrateList(listEle , technologyLeftNode , locale, urlMap);
					log.debug("List is Migrated");
				}catch(Exception e){
					log.debug("Exception in List Migration");
					sb.append(Constants.EXCEPTION_IN_UPDATING_LIST_COMPONENT);
				}
				//End List Migration

				//Start tile Border Migration
				try{
					log.debug("start tile Border Migration");
					Elements tileBorderEles = doc.select("div.c23-pilot");
					Element tileBorderEle = null;
					if(tileBorderEles.size() > 1 && tileBorderEles.first().getElementsByClass("compact").first() != null){
						tileBorderEle = tileBorderEles.last();
						sb.append(Constants.MISMATCH_IN_TILEBORDER_COUNT+tileBorderEles.size()+Constants.TILEBORDER_NODE_COUNT+" 1.</li>");
					}else{
						tileBorderEle = tileBorderEles.first();
					}
					migrateTileElements(tileBorderEle , technologyRightNode , urlMap);
					log.debug("Tile Border is Migrated");
				}catch(Exception e){
					log.debug("Exception in tile Border Migration");
					sb.append(Constants.UNABLE_TO_MIGRATE_TILE_BORDERED_COMPONENTS);
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

	private void migrateText(Element textEle, Node technologyLeftNode,
			String locale, Map<String, String> urlMap) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
		if(textEle != null){
			NodeIterator textNodes = technologyLeftNode.hasNode("text")?technologyLeftNode.getNodes("text*"):null;
			if(textNodes != null){
				int size = (int)textNodes.getSize();
				int eleSize = 0;
				Node textHNode = textNodes.nextNode();
				Element hEle = textEle.getElementsByTag("h1").first();
				if(hEle != null){
					eleSize++;
					textHNode.setProperty("text", hEle.outerHtml());
					hEle.remove();
				}else{
					sb.append(Constants.TEXT_HAEDING_NOT_FOUND);
				}

				if(textNodes.hasNext()){
					Node textNode = textNodes.nextNode();
					Element pEle = textEle.getElementsByTag("p").first();
					if(pEle != null){
						eleSize++;
						textNode.setProperty("text", pEle.outerHtml());
						pEle.remove();
					}else{
						sb.append(Constants.TEXT_ELEMENT_NOT_FOUND);
					}

					Element imageEle = textEle.getElementsByTag("img").first();
					if(imageEle != null){
						Node textImgaeNode = technologyLeftNode.hasNode("image")?technologyLeftNode.getNode("image"):null;
						String textImageEle = FrameworkUtils.extractImagePath(imageEle, sb);
						String textImage = FrameworkUtils.migrateDAMContent(textImageEle, "", locale, sb);
						if(textImage != ""){
							Node imageNode = textImgaeNode.hasNode("image")?textImgaeNode.getNode("image"):null;
							if(imageNode != null){
								imageNode.setProperty("fileReference", textImage);
							}else{
								sb.append(Constants.IMAGE_LINK_NODE_NOT_FOUND);
							}
						}else{
							if(textImageEle.isEmpty()){
							sb.append(Constants.IMAGE_NOT_FOUND_IN_LOCALE_PAGE);
							}else {
								log.debug("image path returned is null but image exists in the both the pages");
							}
						}
						imageEle.remove();
					}else{
						sb.append(Constants.IMAGE_NOT_FOUND_IN_LOCALE_PAGE);
					}

					if(textNodes.hasNext()){
						Node textLastNode = textNodes.nextNode();
						if(textEle.html() != ""){
							eleSize++;
							textLastNode.setProperty("text", FrameworkUtils.extractHtmlBlobContent(textEle, "", locale, sb, urlMap));
						}else{
							sb.append(Constants.TEXT_ELEMENT_NOT_FOUND);
						}
					}else{
						sb.append(Constants.TEXT_NODE_NOT_FOUND);
					}
				}else{
					sb.append(Constants.TEXT_NODE_NOT_FOUND);
				}

				if(size > eleSize){
					sb.append(Constants.TEXT_NODE_MISMATCH+size+Constants.TEXT_ELEMENT_COUNT+eleSize+".</li>");
				}else if(size < eleSize){
					sb.append(Constants.TEXT_NODE_MISMATCH+size+Constants.TEXT_ELEMENT_COUNT+eleSize+".</li>");
				}
			}else{
				sb.append(Constants.TEXT_NODE_NOT_FOUND);
			}
		}else{
			sb.append(Constants.TEXT_ELEMENT_NOT_FOUND);
		}
	}

	private void migrateList(Element listEle, Node technologyLeftNode,
			String locale, Map<String, String> urlMap) throws PathNotFoundException, RepositoryException {
		if(listEle != null){
			Node listContainerNode = technologyLeftNode.hasNode("list_container")?technologyLeftNode.getNode("list_container"):null;
			if(listContainerNode!= null){
				Element heading = listEle.getElementsByTag("h2").first();
				if(heading != null){
					listContainerNode.setProperty("title", heading.text());
				}else{
					sb.append(Constants.LIST_HEADING_COMPONENT_NOT_FOUND);
				}
				if(listContainerNode.hasNode("list_item_parsys")){
					Node list_item_parsysNode = listContainerNode.getNode("list_item_parsys");
					if(list_item_parsysNode.hasNode("list_content")){
						Node list_contentNode = list_item_parsysNode.getNode("list_content");
						if(list_contentNode.hasNode("listitems")){
							Node listitemsNode = list_contentNode.getNode("listitems");
							NodeIterator itemNodes = listitemsNode.hasNode("item_1")?listitemsNode.getNodes("item_*"):null;
							if(itemNodes != null){
								int size = (int)itemNodes.getSize();
								Elements listEles = listEle.getElementsByTag("a");
								if(listEles != null){
									int eleSize = listEles.size();
									if(size == eleSize){
										for(Element anchor:listEles){
											Node itemNode = itemNodes.nextNode();
											if(itemNode.hasNode("linkdata")){
												Node linkdataNode = itemNode.getNode("linkdata");
												String linkText = anchor.text();
												String url = anchor.absUrl("href");
												if(StringUtil.isBlank(url)){
													url = anchor.attr("href");
												}
												url = FrameworkUtils.getLocaleReference(url, urlMap);
												linkdataNode.setProperty("linktext", linkText);
												linkdataNode.setProperty("url", url);
											}else{
												sb.append(Constants.LIST_NODE_NOT_FOUND);
											}
										}
									}else if(size > eleSize){
										for(Element anchor:listEles){
											Node itemNode = itemNodes.nextNode();
											if(itemNode.hasNode("linkdata")){
												Node linkdataNode = itemNode.getNode("linkdata");
												String linkText = anchor.text();
												String url = anchor.absUrl("href");
												if(StringUtil.isBlank(url)){
													url = anchor.attr("href");
												}
												url = FrameworkUtils.getLocaleReference(url, urlMap);
												linkdataNode.setProperty("linktext", linkText);
												linkdataNode.setProperty("url", url);
											}else{
												sb.append(Constants.LIST_NODE_NOT_FOUND);
											}
										}
										if(itemNodes.hasNext()){
											sb.append(Constants.MISMATCH_IN_LIST_NODES+eleSize+Constants.LIST_NODES_COUNT+size+".</li>");
										}
									}else if(size < eleSize){
										for(Element anchor:listEles){
											if(itemNodes.hasNext()){
												Node itemNode = itemNodes.nextNode();
												if(itemNode.hasNode("linkdata")){
													Node linkdataNode = itemNode.getNode("linkdata");
													String linkText = anchor.text();
													String url = anchor.absUrl("href");
													if(StringUtil.isBlank(url)){
														url = anchor.attr("href");
													}
													url = FrameworkUtils.getLocaleReference(url, urlMap);
													linkdataNode.setProperty("linktext", linkText);
													linkdataNode.setProperty("url", url);
												}else{
													sb.append(Constants.LIST_NODE_NOT_FOUND);
												}
											}else{
												sb.append(Constants.MISMATCH_IN_LIST_ELEMENT+eleSize+Constants.LIST_NODES_COUNT+size+".</li>");
												break;
											}
										}
									}
								}else{
									sb.append(Constants.LIST_ANCHOR_ELEMENTS_NOT_FOUND);
								}
							}else{
								sb.append(Constants.LIST_NODE_NOT_FOUND);
							}
						}else{
							sb.append(Constants.LIST_NODE_NOT_FOUND);
						}
					}else{
						sb.append(Constants.LIST_NODE_NOT_FOUND);
					}
				}else{
					sb.append(Constants.LIST_NODE_NOT_FOUND);
				}
			}else{
				sb.append(Constants.LIST_NODE_NOT_FOUND);
			}
		}else{
			sb.append(Constants.LIST_COMPONENT_NOT_FOUND);
		}

	}

	private void migrateTileElements(Element tileBorderEle,
			Node technologyRightNode, Map<String, String> urlMap) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
		if(tileBorderEle != null){
			Element heading = tileBorderEle.getElementsByTag("h3").first();
			if(heading != null){
				technologyRightNode.setProperty("title", heading.text());
			}else{
				sb.append(Constants.TILE_BORDERED_TITLE_NOT_FOUND);
			}
			Elements listEles = tileBorderEle.getElementsByTag("a");
			if(listEles != null){
				int eleSize = listEles.size();
				if(technologyRightNode.hasNode("list_item_parsys")){
					Node list_item_parsysNode = technologyRightNode.getNode("list_item_parsys");
					if(list_item_parsysNode.hasNode("list_content")){
						Node list_contentNode = list_item_parsysNode.getNode("list_content");
						if(list_contentNode.hasNode("listitems")){
							Node listitemsNode = list_contentNode.getNode("listitems");
							NodeIterator itemsNode = listitemsNode.hasNode("item_1")?listitemsNode.getNodes("item_*"):null;
							if(itemsNode != null){
								int size = (int)itemsNode.getSize();
								if(size == eleSize){
									for(Element anchor : listEles){
										Node itemNode = itemsNode.nextNode();
										if(itemNode.hasProperty("icon")){
											String lock = itemNode.getProperty("icon").getValue().getString();
											if(lock.equals("lock")){
												sb.append(Constants.EXTRA_LOCK_IMG_FOUND_IN_LIST);
											}
											
										}
										if(itemNode.hasNode("linkdata")){
											Node linkdataNode = itemNode.getNode("linkdata");
											String linkText = anchor.text();
											String url = anchor.absUrl("href");
											if(StringUtil.isBlank(url)){
												url = anchor.attr("href");
											}
											url = FrameworkUtils.getLocaleReference(url, urlMap);
											linkdataNode.setProperty("linktext", linkText);
											linkdataNode.setProperty("url", url);
										}else{
											sb.append(Constants.TILE_BORDERED_LINK_NODE_NOT_FOUND);
										}
									}
								}else if(size > eleSize){
									for(Element anchor : listEles){
										Node itemNode = itemsNode.nextNode();
										if(itemNode.hasNode("linkdata")){
											Node linkdataNode = itemNode.getNode("linkdata");
											String linkText = anchor.text();
											String url = anchor.absUrl("href");
											if(StringUtil.isBlank(url)){
												url = anchor.attr("href");
											}
											url = FrameworkUtils.getLocaleReference(url, urlMap);
											linkdataNode.setProperty("linktext", linkText);
											linkdataNode.setProperty("url", url);
										}else{
											sb.append(Constants.TILE_BORDERED_LINK_NODE_NOT_FOUND);
										}
									}
									if(itemsNode.hasNext()){
										sb.append(Constants.MISMATCH_IN_TILELINKS_NODES+eleSize+Constants.TILELINKS_NODES_COUNT+size+".</li>");
									}
								}else if(size < eleSize){
									for(Element anchor : listEles){
										if(itemsNode.hasNext()){
											Node itemNode = itemsNode.nextNode();
											if(itemNode.hasNode("linkdata")){
												Node linkdataNode = itemNode.getNode("linkdata");
												String linkText = anchor.text();
												String url = anchor.absUrl("href");
												if(StringUtil.isBlank(url)){
													url = anchor.attr("href");
												}
												url = FrameworkUtils.getLocaleReference(url, urlMap);
												linkdataNode.setProperty("linktext", linkText);
												linkdataNode.setProperty("url", url);
											}else{
												sb.append(Constants.TILE_BORDERED_LINK_NODE_NOT_FOUND);
											}
										}else{
											sb.append(Constants.MISMATCH_IN_TILELINKS_ELEMENT+eleSize+Constants.TILELINKS_NODES_COUNT+size+".</li>");
										}
									}
								}
							}else{
								sb.append(Constants.TILE_BORDERED_LINK_NODE_NOT_FOUND);
							}
						}else{
							sb.append(Constants.TILE_BORDERED_LINK_NODE_NOT_FOUND);
						}					
					}else{
						sb.append(Constants.TILE_BORDERED_LINK_NODE_NOT_FOUND);
					}
				}else{
					sb.append(Constants.TILE_BORDERED_LINK_NODE_NOT_FOUND);
				}
			}else{
				sb.append(Constants.TILE_BORDERED_ANCHOR_ELEMENTS_NOT_FOUND);
			}
		}else{
			sb.append(Constants.TILE_BORDERED_COMPONENT_NOT_FOUND);
		}

	}


}
