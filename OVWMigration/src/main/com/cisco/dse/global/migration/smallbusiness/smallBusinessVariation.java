package com.cisco.dse.global.migration.smallbusiness;

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
import org.apache.sling.commons.json.JSONException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.cisco.dse.global.migration.config.BaseAction;
import com.cisco.dse.global.migration.config.Constants;
import com.cisco.dse.global.migration.config.FrameworkUtils;

public class smallBusinessVariation extends BaseAction {

	Document doc;

	StringBuilder sb = new StringBuilder(1024);

	static Logger log = Logger.getLogger(smallBusinessVariation.class);

	public String translate(String host, String loc, String prod, String type, String catType,
			String locale, Session session, Map<String, String> urlMap) throws IOException,
			ValueFormatException, VersionException, LockException,
			ConstraintViolationException, RepositoryException {
		BasicConfigurator.configure();
		// Repo node paths
		try {
			log.debug("In the translate method");
			log.debug("In the translate method, catType is :" + catType);
			String pagePropertiesPath = "/content/<locale>/"+catType+"/small-business/jcr:content";

			String pageUrl = host + "/content/<locale>/"+ catType + "/small-business.html";
			pageUrl = pageUrl.replace("<locale>", locale);
			pagePropertiesPath = pagePropertiesPath.replace("<locale>", locale);
			String sBTopRight = pagePropertiesPath+"/Grid/solutions/layout-solutions/widenarrow/WN-Narrow-2/list_container";
			String sBHero = pagePropertiesPath+"/Grid/solutions/layout-solutions/widenarrow/WN-Wide-1/carousel/carouselContents";
			String sBThird1 = pagePropertiesPath+"/Grid/solutions/layout-solutions/thirds";
			String sBThird2 = pagePropertiesPath+"/Grid/solutions/layout-solutions/thirds_0";

			sb.append("<td>" + "<a href="+pageUrl+">"+pageUrl+"</a>"+"</td>");
			sb.append("<td>" + "<a href="+loc+">"+loc +"</a>"+ "</td>");
			sb.append("<td><ul>");

			javax.jcr.Node sBTopRightNode = null;
			javax.jcr.Node sBHeroNode = null;
			javax.jcr.Node sBThirdNode1 = null;
			javax.jcr.Node sBThirdNode2 = null;
			javax.jcr.Node pageJcrNode = null;

			sBHeroNode = session.getNode(sBHero);
			sBTopRightNode = session.getNode(sBTopRight);
			sBThirdNode1 = session.getNode(sBThird1);
			sBThirdNode2 = session.getNode(sBThird2);
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
					migrateHero(heroEle , sBHeroNode , locale , urlMap, catType, type);
					log.debug("Hero Element Migrated");
				}catch(Exception e){
					sb.append(Constants.EXCEPTION_IN_HERO_MIGRATION);
					log.debug("Exception in Hero Element Migration"+e);
				}
				//End Hero Migration

				//start Top Right Migration
				try{
					log.debug("Start Top Right Element Migration");
					Element listEle = doc.select("div.acc-panel").first();
					migrateTopList(listEle , sBTopRightNode , locale , urlMap, catType, type);
					log.debug("Top Right Element Migrated");
				}catch(Exception e){
					sb.append(Constants.EXCEPTION_IN_UPDATING_LIST_COMPONENT);
					log.debug("Exception in Top Right Element Migration"+e);
				}
				//End Top Right Migration

				//start Third1 Migration
				try{
					log.debug("Start Third1 Element Migration");
					Element listEle = doc.select("div.n04v8-pilot").first();
					migrateThird1(listEle , sBThirdNode1 , locale , urlMap, catType, type);
					log.debug("Third1 Element Migrated");
				}catch(Exception e){
					sb.append(Constants.EXCEPTION_IN_UPDATING_LIST_COMPONENT);
					log.debug("Exception in Third1 Element Migration"+e);
				}
				//End Third1 Migration

				//start Third2 Migration
				try{
					log.debug("Start Third2 Element Migration");
					Elements listEles = doc.select("div.bb-content");
					migrateThird2(listEles , sBThirdNode2 , locale , urlMap, catType, type);
					log.debug("Third2 Element Migrated");
				}catch(Exception e){
					sb.append(Constants.EXCEPTION_IN_UPDATING_LIST_COMPONENT);
					log.debug("Exception in Third2 Element Migration"+e);
				}
				//End Third2 Migration


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


	private void migrateThird2(Elements listEles, Node sBThirdNode2,
			String locale, Map<String, String> urlMap, String catType,
			String type) throws RepositoryException {
		if(listEles != null){
			int eleSize = listEles.size();
			NodeIterator thirdNodes = sBThirdNode2.hasNode("Th-Third-1")?sBThirdNode2.getNodes("Th-Third*"):null;
			if(thirdNodes != null){
				int size = (int)thirdNodes.getSize();
				for(Element list : listEles){
					if(thirdNodes.hasNext()){
						Node thirdNode = thirdNodes.nextNode();
						if(thirdNode.hasNode("tile")){
							Node tileNode = thirdNode.getNode("tile");
							Element title = list.getElementsByTag("h3").first();
							Element description = list.getElementsByTag("p").first();
							Element anchor = list.getElementsByTag("a").first();
							if(title != null){
								tileNode.setProperty("title", title.text());
							}else{
								sb.append(Constants.LIST_HEADING_COMPONENT_NOT_FOUND);
							}
							if(description != null){
								tileNode.setProperty("description", description.text());
							}else{
								sb.append(Constants.LIST_INTRO_PARAGRAPH_ELEMENT_NOT_FOUND);
							}
							if(anchor != null){
								if(tileNode.hasNode("cta")){
									Node ctaNode = tileNode.getNode("cta");
									ctaNode.setProperty("linktext", anchor.text());
									String aUrl = anchor.absUrl("href");
									if(aUrl.equals("")){
										aUrl = anchor.attr("href");
									}
									aUrl = FrameworkUtils.getLocaleReference(aUrl, urlMap, locale, sb, catType, type);
									if(!aUrl.equals("") && !aUrl.isEmpty()){
										ctaNode.setProperty("url", aUrl);	
									}else{
										sb.append(Constants.LINK_URL_NOT_FOUND_IN_LIST);
									}
								}else{
									sb.append(Constants.LINK_DATA_NODE_FOR_LIST_NOT_FOUND);
								}
								tileNode.setProperty("title", title.text());
							}else{
								sb.append(Constants.LIST_ANCHOR_ELEMENTS_NOT_FOUND);
							}
						}else{
							sb.append(Constants.LIST_ITEM_NODE_NOT_FOUND);
						}
					}else{
						sb.append(Constants.MISMATCH_IN_LIST_ELEMENT+eleSize+Constants.LIST_NODES_COUNT+size+".</li>");
					}
				}
				if(thirdNodes.hasNext()){
					Element letUsHelp = doc.select("div.rc-persel").first();
					if(letUsHelp != null){
						Node thirdNode = thirdNodes.nextNode();
						if(thirdNode.hasNode("tile")){
							boolean check = true;
							Node tileNode = thirdNode.getNode("tile");
							Element title = letUsHelp.getElementsByTag("h3").first();
							Element description = letUsHelp.getElementsByTag("p").first();
							Element dAnchor = letUsHelp.getElementsByTag("a").first();
							letUsHelp.getElementsByTag("a").first().remove();
							Element anchor = letUsHelp.getElementsByTag("a").last();
							if(title != null){
								tileNode.setProperty("title", title.text());
							}else{
								sb.append(Constants.LIST_HEADING_COMPONENT_NOT_FOUND);
							}
							if(description != null){
								String anchorTag = "";
								if(dAnchor != null){
									anchorTag = dAnchor.outerHtml();
								}else{
									check = false;
									sb.append(Constants.LIST_ANCHOR_ELEMENTS_NOT_FOUND);
								}
								if(description.text().equals("")){
									description = letUsHelp.getElementsByTag("p").last();
								}
								tileNode.setProperty("description", description.text()+"</br></br>"+anchorTag);
							}else{
								sb.append(Constants.LIST_INTRO_PARAGRAPH_ELEMENT_NOT_FOUND);
							}
							if(anchor != null){
								if(tileNode.hasNode("cta")){
									Node ctaNode = tileNode.getNode("cta");
									ctaNode.setProperty("linktext", anchor.text());
									String aUrl = anchor.absUrl("href");
									if(aUrl.equals("")){
										aUrl = anchor.attr("href");
									}
									aUrl = FrameworkUtils.getLocaleReference(aUrl, urlMap, locale, sb, catType, type);
									if(!aUrl.equals("") && !aUrl.isEmpty()){
										ctaNode.setProperty("url", aUrl);	
									}else{
										sb.append(Constants.LINK_URL_NOT_FOUND_IN_LIST);
									}
								}else{
									sb.append(Constants.LINK_DATA_NODE_FOR_LIST_NOT_FOUND);
								}
							}else{
								if(check)
									sb.append(Constants.LIST_ANCHOR_ELEMENTS_NOT_FOUND);
							}
						}else{
							sb.append(Constants.LIST_ITEM_NODE_NOT_FOUND);
						}
					}else{
						sb.append(Constants.MISMATCH_IN_LIST_NODES+eleSize+Constants.LIST_NODES_COUNT+size+".</li>");
					}
				}
			}else{
				sb.append(Constants.NO_LIST_NODES_FOUND);
			}
		}else{
			sb.append(Constants.LIST_COMPONENT_NOT_FOUND);
		}
	}


	private void migrateThird1(Element listEle, Node sBThirdNode1,
			String locale, Map<String, String> urlMap, String catType,
			String type) throws RepositoryException {
		if(listEle != null){
			Elements listEles = listEle.getElementsByClass("panel");
			if(listEles != null){
				int eleSize = listEles.size();
				NodeIterator thirdNodes = sBThirdNode1.hasNode("Th-Third-1")?sBThirdNode1.getNodes("Th-Third*"):null;
				if(thirdNodes != null){
					int size = (int)thirdNodes.getSize();
					for(Element list : listEles){
						if(thirdNodes.hasNext()){
							Node thirdNode = thirdNodes.nextNode();
							if(thirdNode.hasNode("list_container")){
								Node list_containerNode = thirdNode.getNode("list_container");
								Element title = list.getElementsByTag("h2").first();
								if(title != null){
									list_containerNode.setProperty("title", title.text());
								}else{
									sb.append(Constants.LIST_HEADING_COMPONENT_NOT_FOUND);
								}
								if(list_containerNode.hasNode("list_item_parsys")){
									Node list_item_parsysNode = list_containerNode.getNode("list_item_parsys");
									if(list_item_parsysNode.hasNode("list_content")){
										Node list_contentNode = list_item_parsysNode.getNode("list_content");
										if(list_contentNode.hasNode("listitems")){
											Node listitemsNode = list_contentNode.getNode("listitems");
											NodeIterator itemsNode = listitemsNode.hasNode("item_1")?listitemsNode.getNodes("item_*"):null;
											if(itemsNode != null){
												int itemSize = (int)itemsNode.getSize();
												Element ul = list.getElementsByTag("ul").first();
												Elements liEles = ul.getElementsByTag("li");
												if(liEles != null){
													int itemEleSize = liEles.size();
													for(Element li : liEles){
														if(itemsNode.hasNext()){
															Node itemNode = itemsNode.nextNode();
															if(itemNode.hasNode("linkdata")){
																Node linkdataNode = itemNode.getNode("linkdata");
																Element anchor = li.getElementsByTag("a").first();
																if(anchor != null){
																	linkdataNode.setProperty("linktext", anchor.text());
																	String aUrl = anchor.absUrl("href");
																	if(!aUrl.isEmpty() && ! aUrl.equals("")){
																		aUrl = anchor.attr("href");
																	}
																	aUrl = FrameworkUtils.getLocaleReference(aUrl, urlMap, locale, sb, catType, type);
																	if(!aUrl.isEmpty() && !aUrl.equals("")){
																		linkdataNode.setProperty("url", aUrl);
																	}else{
																		sb.append(Constants.LINK_URL_NOT_FOUND_IN_LIST);
																	}
																}else{
																	sb.append(Constants.LIST_ANCHOR_ELEMENTS_NOT_FOUND);
																}
															}else{
																sb.append(Constants.LINK_DATA_NODE_FOR_LIST_NOT_FOUND);
															}
														}else{
															sb.append(Constants.MISMATCH_IN_LIST_ELEMENT+itemEleSize+Constants.LIST_NODES_COUNT+itemSize+".</li>");
														}
													}
													if(itemsNode.hasNext()){
														sb.append(Constants.MISMATCH_IN_LIST_NODES+itemEleSize+Constants.LIST_NODES_COUNT+itemSize+".</li>");
													}
												}else{
													sb.append(Constants.LIST_ELEMENT_NOT_FOUND);
												}
											}else{
												sb.append(Constants.LIST_ITEM_NODE_NOT_FOUND);
											}
										}else{
											sb.append(Constants.LIST_ITEM_NODE_NOT_FOUND);
										}
									}else{
										sb.append(Constants.LIST_ITEM_NODE_NOT_FOUND);
									}
								}else{
									sb.append(Constants.LIST_ITEM_NODE_NOT_FOUND);
								}
							}else{
								sb.append(Constants.LIST_ITEM_NODE_NOT_FOUND);
							}
						}else{
							sb.append(Constants.MISMATCH_IN_LIST_ELEMENT+eleSize+Constants.LIST_NODES_COUNT+size+".</li>");
						}
					}
					if(thirdNodes.hasNext()){
						sb.append(Constants.MISMATCH_IN_LIST_NODES+eleSize+Constants.LIST_NODES_COUNT+size+".</li>");
					}
				}else{
					sb.append(Constants.NO_LIST_NODES_FOUND);
				}
			}else{
				sb.append(Constants.LIST_COMPONENT_NOT_FOUND);
			}
		}else{
			sb.append(Constants.LIST_COMPONENT_NOT_FOUND);
		}
	}


	private void migrateTopList(Element listEle, Node sBTopRightNode,
			String locale, Map<String, String> urlMap, String catType,
			String type) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
		if(listEle != null){
			Element title = listEle.getElementsByTag("a").first();
			if(title != null){
				sBTopRightNode.setProperty("title", title.text());
			}else{
				sb.append(Constants.LIST_HEADING_COMPONENT_NOT_FOUND);
			}
			Element ul = listEle.getElementsByTag("ul").first();
			if(ul != null){
				if(sBTopRightNode.hasNode("list_item_parsys")){
					Node list_item_parsysNode = sBTopRightNode.getNode("list_item_parsys");
					if(list_item_parsysNode.hasNode("list_content")){
						Node list_contentNode = list_item_parsysNode.getNode("list_content");
						if(list_contentNode.hasNode("listitems")){
							Node listitemsNode = list_contentNode.getNode("listitems");
							NodeIterator itemsNode = listitemsNode.hasNode("item_1")?listitemsNode.getNodes("item_*"):null;
							if(itemsNode != null){
								int size = (int)itemsNode.getSize();
								Elements liEles = ul.getElementsByTag("li");
								if(liEles != null){
									int eleSize = liEles.size();
									for(Element li : liEles){
										if(itemsNode.hasNext()){
											Node itemNode = itemsNode.nextNode();
											if(itemNode.hasNode("linkdata")){
												Node linkdataNode = itemNode.getNode("linkdata");
												Element anchor = li.getElementsByTag("a").first();
												if(anchor != null){
													linkdataNode.setProperty("linktext", anchor.text());
													String aUrl = anchor.absUrl("href");
													if(!aUrl.isEmpty() && ! aUrl.equals("")){
														aUrl = anchor.attr("href");
													}
													aUrl = FrameworkUtils.getLocaleReference(aUrl, urlMap, locale, sb, catType, type);
													if(!aUrl.isEmpty() && !aUrl.equals("")){
														linkdataNode.setProperty("url", aUrl);
													}else{
														sb.append(Constants.LINK_URL_NOT_FOUND_IN_LIST);
													}
												}else{
													sb.append(Constants.LIST_ANCHOR_ELEMENTS_NOT_FOUND);
												}
											}else{
												sb.append(Constants.LINK_DATA_NODE_FOR_LIST_NOT_FOUND);
											}
										}else{
											sb.append(Constants.MISMATCH_IN_LIST_ELEMENT+eleSize+Constants.LIST_NODES_COUNT+size+".</li>");
										}
									}
									if(itemsNode.hasNext()){
										sb.append(Constants.MISMATCH_IN_LIST_NODES+eleSize+Constants.LIST_NODES_COUNT+size+".</li>");
									}
								}else{
									sb.append(Constants.LIST_ELEMENT_NOT_FOUND);
								}
							}else{
								sb.append(Constants.LIST_ITEM_NODE_NOT_FOUND);
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
				sb.append(Constants.LIST_ELEMENT_NOT_FOUND);
			}
		}else{
			sb.append(Constants.LIST_COMPONENT_NOT_FOUND);
		}
	}


	private void migrateHero(Element heroElement, Node sBHeroNode, String locale,
			Map<String, String> urlMap, String catType, String type) throws PathNotFoundException, RepositoryException, JSONException {
		if(heroElement != null){
			Elements heroEles = heroElement.getElementsByClass("frame");
			if(heroEles != null){
				int eleSize = heroEles.size();
				NodeIterator heroNodes = sBHeroNode.hasNode("hero_panel")?sBHeroNode.getNodes("hero_panel*"):null;
				if(heroNodes != null){
					int size = (int)heroNodes.getSize();
					for(Element heroEle : heroEles){
						if(heroNodes.hasNext()){
							Node heroPanelNode = heroNodes.nextNode();
							Element title = heroEle.getElementsByTag("h2").first();
							Element description = heroEle.getElementsByTag("p").first();
							Element anchor = heroEle.getElementsByTag("a").first();
							if(title != null){
								heroPanelNode.setProperty("title", title.text());
							}else{
								sb.append(Constants.HERO_CONTENT_HEADING_ELEMENT_DOESNOT_EXISTS);
							}
							if(description != null){
								heroPanelNode.setProperty("description", description.text());
							}else{
								sb.append(Constants.HERO_CONTENT_DESCRIPTION_ELEMENT_DOESNOT_EXISTS);
							}
							if(anchor != null){
								heroPanelNode.setProperty("linktext", anchor.text());
								if(heroPanelNode.hasNode("cta")){
									Node ctaNode = heroPanelNode.getNode("cta");
									String aUrl = anchor.absUrl("href");
									if(aUrl.equals("")){
										aUrl = anchor.attr("href");
									}
									aUrl = FrameworkUtils.getLocaleReference(aUrl, urlMap, locale, sb, catType, type);
									if(!aUrl.equals("") && !aUrl.isEmpty()){
										ctaNode.setProperty("url", aUrl);	
									}else{
										sb.append(Constants.HERO_CONTENT_ANCHOR_LINK_IS_BLANK);
									}
								}else{
									sb.append(Constants.HERO_COMPONENT_CTA_NODE_NOT_FOUND);
								}
							}else{
								sb.append(Constants.HERO_CONTENT_ANCHOR_ELEMENT_DOESNOT_EXISTS);
							}
							if(heroPanelNode.hasNode("image")){
								Node imageNode = heroPanelNode.getNode("image");
								String fileReference = FrameworkUtils.extractImagePath(heroEle, sb);
								fileReference = FrameworkUtils.migrateDAMContent(fileReference, "", locale, sb, catType, type);
								if(!fileReference.equals("")){
									imageNode.setProperty("fileReference", fileReference);
								}else{
									sb.append(Constants.HERO_CONTENT_IMAGE_LINK_IS_BLANK);
								}
							}else{
								sb.append(Constants.HERO_CONTENT_IMAGE_NODE_NOT_FOUND);
							}
						}else{
							sb.append(Constants.MISMATCH_IN_HERO_SLIDES+"Element size is "+eleSize+" and panel nodes are "+size);
						}
					}
					if(heroNodes.hasNext()){
						sb.append(Constants.MISMATCH_IN_HERO_SLIDES+"Element size is "+eleSize+" and panel nodes are "+size);
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


}
