package com.cisco.dse.global.migration.partner;

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

public class PartnerVariation1 extends BaseAction {

	Document doc;

	StringBuilder sb = new StringBuilder(1024);

	static Logger log = Logger.getLogger(PartnerVariation1.class);

	public String translate(String host, String loc, String prod, String type, String catType,
			String locale, Session session, Map<String, String> urlMap) throws IOException,
			ValueFormatException, VersionException, LockException,
			ConstraintViolationException, RepositoryException {
		BasicConfigurator.configure();
		// Repo node paths
		try {
			String pagePropertiesPath = "/content/<locale>/partners/jcr:content/";
			String pageUrl = host + "/content/<locale>/partners.html";

			pageUrl = pageUrl.replace("<locale>", locale);
			pagePropertiesPath = pagePropertiesPath.replace("<locale>", locale);
			String partnerLeft = pagePropertiesPath+"content_parsys/partner_central/layout-partner-central/gd12v2/gd12v2-left";
			String partnerRight = pagePropertiesPath+"content_parsys/partner_central/layout-partner-central/gd12v2/gd12v2-right";
			String partnerMid = pagePropertiesPath+"content_parsys/partner_central/layout-partner-central/gd13v2";
			String partnerBottom = pagePropertiesPath+"content_parsys/partner_central/layout-partner-central/gd11v1/gd11v1-mid";

			log.debug("Path is "+partnerLeft);
			log.debug("Path is "+partnerRight);
			log.debug("Path is "+partnerMid);
			log.debug("Path is "+partnerBottom);

			sb.append("<td>" + "<a href="+pageUrl+">"+pageUrl+"</a>"+"</td>");
			sb.append("<td>" + "<a href="+loc+">"+loc +"</a>"+ "</td>");
			sb.append("<td><ul>");

			javax.jcr.Node partnerLeftNode = null;
			javax.jcr.Node partnerRightNode = null;
			javax.jcr.Node partnerMidNode = null;
			javax.jcr.Node partnerBottomNode = null;
			javax.jcr.Node pageJcrNode = null;

			partnerLeftNode = session.getNode(partnerLeft);
			partnerRightNode = session.getNode(partnerRight);
			partnerMidNode = session.getNode(partnerMid);
			partnerBottomNode = session.getNode(partnerBottom);
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
					migrateHero(heroEle , partnerLeftNode , locale , urlMap);
					log.debug("Hero Element Migrated");
				}catch(Exception e){
					sb.append(Constants.EXCEPTION_IN_HERO_MIGRATION);
					log.debug("Exception in Hero Element Migration"+e);
				}
				//End Hero Migration

				//start Top Right migration
				try{
					log.debug("start Top Right migration");
					Element topRightEle = doc.select("div.c47v1-pilot").first();
					migrateHelpHtmlBlob(topRightEle , partnerRightNode, locale , urlMap);
					log.debug("Top Right Element Migrated");
				}catch(Exception e){
					sb.append(Constants.EXCEPTION_IN_HTMLBLOB);
					log.debug("Exception in Top Right Element Migration"+e);
				}
				//end Top Right migration

				//start Html Blobs migration
				try{
					log.debug("start Html Blobs migration");
					Element midEle = doc.select("div.gd13v2-pilot").first();
					migrateHtmlBlobs(midEle , partnerMidNode, locale , urlMap);
					log.debug("Html Blobs Element Migrated");
				}catch(Exception e){
					sb.append(Constants.EXCEPTION_IN_HTMLBLOB);
					log.debug("Exception in Top Right Element Migration"+e);
				}
				//end Html Blobs migration

				//start Text Migration
				try{
					log.debug("start Text Migration");
					Element textEle = doc.select("div.gd11-pilot").last().getElementsByClass("compact").first();
					migrateText(textEle , partnerBottomNode , locale, urlMap);
					log.debug("Text is Migrated");
				}catch(Exception e){
					log.debug("Exception in Text Migration");
					sb.append(Constants.EXCEPTION_TEXT_COMPONENT);
				}
				//End text Migration


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


	private void migrateHero(Element heroEle, Node partnerLeftNode, String locale, Map<String, String> urlMap) throws PathNotFoundException, RepositoryException {
		if(heroEle != null){
			Node heroNode = partnerLeftNode.hasNode("hero_large")?partnerLeftNode.getNode("hero_large"):null;
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
						heroPanelNode.setProperty("linkurl", FrameworkUtils.getLocaleReference(aHero.absUrl("href"), urlMap));
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

	private void migrateHelpHtmlBlob(Element topRightEle, Node partnerRightNode, String locale, Map<String, String> urlMap) throws PathNotFoundException, RepositoryException {
		if(topRightEle != null){
			Node htmlBlobNode = partnerRightNode.hasNode("htmlblob")?partnerRightNode.getNode("htmlblob"):null;
			if(htmlBlobNode != null){
				htmlBlobNode.setProperty("html", FrameworkUtils.extractHtmlBlobContent(topRightEle, "", locale, sb, urlMap));
			}else{
				sb.append(Constants.HTMLBLOB_NODE_NOT_FOUND);
			}
		}else{
			sb.append(Constants.HTMLBLOB_ELEMENT_NOT_FOUND);
		}
	}

	private void migrateHtmlBlobs(Element midEle, Node partnerMidNode,
			String locale, Map<String, String> urlMap) throws RepositoryException {
		if(midEle != null){
			NodeIterator midNodes = partnerMidNode.hasNode("gd13v2-left")?partnerMidNode.getNodes("gd13v2-*"):null;
			if(midNodes != null){
				int size = (int)midNodes.getSize();
				Elements htmlBlobEles = midEle.getElementsByClass("c00-pilot");
				Elements htmlBlob_0_Eles = midEle.getElementsByClass("n13v12-pilot");
				if(htmlBlobEles != null && htmlBlob_0_Eles != null){
					int countSize = htmlBlob_0_Eles.size(),count=0;
					int eleSize = htmlBlobEles.size();
					if(size == eleSize){
						for(Element htmlBlob : htmlBlobEles){
							Node midNode = midNodes.nextNode();
							if(midNode.hasNode("htmlblob")){
								Node htmlBlobNode = midNode.getNode("htmlblob");
								htmlBlobNode.setProperty("html", FrameworkUtils.extractHtmlBlobContent(htmlBlob, "", locale, sb, urlMap));
							}else{
								sb.append(Constants.HTMLBLOB_NODE_NOT_FOUND);
							}
							if(midNode.hasNode("htmlblob_0")){
								if(count < countSize){
									htmlBlob = htmlBlob_0_Eles.get(count);
									count++;
								}else{
									sb.append(Constants.HTMLBLOB_ELEMENT_NOT_FOUND);
								}
								Node htmlBlobNode = midNode.getNode("htmlblob_0");
								htmlBlobNode.setProperty("html",  FrameworkUtils.extractHtmlBlobContent(htmlBlob, "", locale, sb, urlMap));
							}else{
								sb.append(Constants.HTMLBLOB_NODE_NOT_FOUND);
							}
						}
					}else if(size > eleSize){
						for(Element htmlBlob : htmlBlobEles){
							Node midNode = midNodes.nextNode();
							if(midNode.hasNode("htmlblob")){
								Node htmlBlobNode = midNode.getNode("htmlblob");
								htmlBlobNode.setProperty("html",  FrameworkUtils.extractHtmlBlobContent(htmlBlob, "", locale, sb, urlMap));
							}else{
								sb.append(Constants.HTMLBLOB_NODE_NOT_FOUND);
							}
							if(midNode.hasNode("htmlblob_0")){
								if(count < countSize){
									htmlBlob = htmlBlob_0_Eles.get(count);
									count++;
								}else{
									sb.append(Constants.HTMLBLOB_ELEMENT_NOT_FOUND);
								}
								Node htmlBlobNode = midNode.getNode("htmlblob_0");
								htmlBlobNode.setProperty("html",  FrameworkUtils.extractHtmlBlobContent(htmlBlob, "", locale, sb, urlMap));
							}else{
								sb.append(Constants.HTMLBLOB_NODE_NOT_FOUND);
							}
						}
						if(midNodes.hasNext()){
							sb.append(Constants.MISMATCH_IN_HTMLBLOB_NODES+eleSize+Constants.HTMLBLOB_NODES_COUNT+size+".</li>");
						}
					}else if(size < eleSize){
						for(Element htmlBlob : htmlBlobEles){
							if(midNodes.hasNext()){
								Node midNode = midNodes.nextNode();
								if(midNode.hasNode("htmlblob")){
									Node htmlBlobNode = midNode.getNode("htmlblob");
									htmlBlobNode.setProperty("html",  FrameworkUtils.extractHtmlBlobContent(htmlBlob, "", locale, sb, urlMap));
								}else{
									sb.append(Constants.HTMLBLOB_NODE_NOT_FOUND);
								}
								if(midNode.hasNode("htmlblob_0")){
									if(count < countSize){
										htmlBlob = htmlBlob_0_Eles.get(count);
										count++;
									}else{
										sb.append(Constants.HTMLBLOB_ELEMENT_NOT_FOUND);
									}
									Node htmlBlobNode = midNode.getNode("htmlblob_0");
									htmlBlobNode.setProperty("html",  FrameworkUtils.extractHtmlBlobContent(htmlBlob, "", locale, sb, urlMap));
								}else{
									sb.append(Constants.HTMLBLOB_NODE_NOT_FOUND);
								}
							}else{
								sb.append(Constants.MISMATCH_IN_HTMLBLOB_ELEMENT+eleSize+Constants.HTMLBLOB_NODES_COUNT+size+".</li>");
							}
						}
					}
				}else{
					sb.append(Constants.HTMLBLOB_CONTENT_DOES_NOT_EXIST);
				}
			}else{
				sb.append(Constants.HTMLBLOB_NODE_DOES_NOT_EXIST);
			}
		}else{
			sb.append(Constants.HTMLBLOB_CONTENT_DOES_NOT_EXIST);
		}
	}

	private void migrateText(Element textEle, Node partnerBottomNode,
			String locale, Map<String, String> urlMap) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
		if(textEle != null){
			Node textNode = partnerBottomNode.hasNode("text_0")?partnerBottomNode.getNode("text_0"):null;
			if(textNode != null){
				textNode.setProperty("text", FrameworkUtils.extractHtmlBlobContent(textEle, "", locale, sb, urlMap));
			}else{
				sb.append(Constants.TEXT_NODE_NOT_FOUND);
			}
		}else{
			sb.append(Constants.TEXT_ELEMENT_NOT_FOUND);
		}
	}

}