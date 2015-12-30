package com.cisco.dse.global.migration.benefit;

import java.io.IOException;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.cisco.dse.global.migration.config.BaseAction;
import com.cisco.dse.global.migration.config.Constants;
import com.cisco.dse.global.migration.config.FrameworkUtils;

public class BenifitsVariation2 extends BaseAction{
	Document doc;

	StringBuilder sb = new StringBuilder(1024);

	static Logger log = Logger.getLogger(BenifitsVariation2.class);

	public String translate(String host, String loc, String prod, String type, String catType,
			String locale, Session session,Map<String,String> urlMap) throws IOException,
			ValueFormatException, VersionException, LockException,
			ConstraintViolationException, RepositoryException {
		BasicConfigurator.configure();

		// Repo node paths
		try {
			String pagePropertiesPath = "/content/<locale>/"+catType+"/<prod>/benefit/jcr:content/";
			String pageUrl = host + "/content/<locale>/"+catType+"/<prod>/benefit.html";

			pageUrl = pageUrl.replace("<locale>", locale).replace("<prod>", prod);
			pagePropertiesPath = pagePropertiesPath.replace("<locale>", locale).replace("<prod>", prod);

			String benifitsLeft = pagePropertiesPath+"content_parsys/benefits/layout-benefits/gd12v2/gd12v2-left";
			String benifitsRight = pagePropertiesPath+"content_parsys/benefits/layout-benefits/gd12v2/gd12v2-right";

			log.debug("Path is "+benifitsLeft);
			log.debug("Path is "+benifitsRight);

			sb.append("<td>" + "<a href="+pageUrl+">"+pageUrl+"</a>"+"</td>");
			sb.append("<td>" + "<a href="+loc+">"+loc +"</a>"+ "</td>");
			sb.append("<td><ul>");

			benifitsLeft = benifitsLeft.replace("<locale>", locale).replace("<prod>", prod);
			benifitsRight = benifitsRight.replace("<locale>", locale).replace("<prod>", prod);

			javax.jcr.Node benifitsLeftNode = null;
			javax.jcr.Node benifitsRightNode = null;
			javax.jcr.Node pageJcrNode = null;

			benifitsLeftNode = session.getNode(benifitsLeft);
			benifitsRightNode = session.getNode(benifitsRight);
			pageJcrNode = session.getNode(pagePropertiesPath);

			try {
				doc = getConnection(loc);
				if(doc!=null){
					// ------------------------------------------------------------------------------------------------------------------------------------------
					// start set page properties.

					FrameworkUtils.setPageProperties(pageJcrNode, doc, session, sb);

					// end set page properties.
					// ------------------------------------------------------------------------------------------------------------------------------------------

					//------------------ (start gd-left) ---------------//

					//start of hero
					try{
						Element heroEle = doc.select("div.gd-left").select("div.c50-pilot").first();
						if(heroEle!=null){
							log.debug("Hero node Not Available");
							sb.append(Constants.HERO_NODE_NOT_AVAILABLE);
						}
					}catch(Exception e){
						log.error("Unable to update hero : ",e);
					}
					//end of Hero

					//start of text
					try{
						log.debug("start of text...");
						Elements textEle = doc.select("div.gd-left").select("div.text");
						NodeIterator textNodeIterator = benifitsLeftNode.hasNodes()?benifitsLeftNode.getNodes("text*"):null;
						int textNodeSize = (int)textNodeIterator.getSize();
						int textEleSize = textEle.size();
						log.debug("Text Element size : "+textEleSize+"-- Text Node size : "+textNodeSize);
						Node textNode = null;
						if(!textEle.isEmpty()){
							if(textEleSize==textNodeSize){
								for(Element text : textEle){
									if(textNodeIterator.hasNext()){
										textNode = (Node)textNodeIterator.next();
										textNode.setProperty("text",text.outerHtml());
										log.debug("text node updated...");
									}
								}
							}else if((textEleSize<textNodeSize)){
								for(Element text : textEle){
									if(textNodeIterator.hasNext()){
										textNode = (Node)textNodeIterator.next();
										textNode.setProperty("text",text.outerHtml());
										log.debug("text node updated...");
									}

								}
								if(textNodeIterator.hasNext()){	
									textNodeIterator.next();
									log.debug("skiped middle text node..");
								}

								Element list = doc.select("div.gd-left").select("div.gd23-pilot").first();
								if(list!=null){
									if(textNodeIterator.hasNext()){
										textNode = (Node)textNodeIterator.next();
										textNode.setProperty("text",list.outerHtml());
										log.debug("text node updated...");
									}
								}
								sb.append(Constants.EXTRA_TEXT_NODE_FOUND);
							}
							log.debug("end of text...");
						}else{
							Element list = null;
							Element text = doc.select("div.gd-left").select("div.c00-pilot").first();
							if(text.select("div.gd23-pilot")!=null){
								list = text.select("div.gd23-pilot").remove().first();
							}
							if(textNodeIterator.hasNext()){
								textNode = (Node)textNodeIterator.nextNode();
								textNode.setProperty("text",text.outerHtml());
								log.debug("text node updated...");
							}
							if(textNodeIterator.hasNext()){
								textNodeIterator.next();
								log.debug("skiped middle text node..");

							}
							if(textNodeIterator.hasNext()){
								textNode = (Node)textNodeIterator.next();
								textNode.setProperty("text",list.outerHtml());
								log.debug("text node updated...");
							}
							sb.append(Constants.EXTRA_TEXT_NODE_FOUND);
						}
						log.debug("end of text...");
					}catch(Exception e){
						log.error("Unable to update text component due to : ",e);
						sb.append(Constants.UNABLE_TO_MIGRATE_TEXT);
					}
					//end of text

					//start of spotlight
					try{
						log.debug("Start of spotlight..");
						Elements spElem = doc.select("div.gd-left").select("div.spotlight-large-v2");
						NodeIterator spNodeIterator = benifitsLeftNode.hasNodes()?benifitsLeftNode.getNodes("spotlight_large_v2*"):null;
						Node spNode = null;
						log.debug("Spotlight Element size : "+spElem.size()+"Spotlight Node size : "+spNodeIterator.getSize());
						if(!spElem.isEmpty()){
							for(Element sp : spElem){
								Element h2Ele = sp.getElementsByTag("h2").first();
								Element pEle = sp.getElementsByTag("p").first();
								Element aEle = sp.select("a.cta").first();
								if(spNodeIterator.hasNext()){
									spNode = (Node)spNodeIterator.next();
									spNode.setProperty("title",h2Ele.text());
									spNode.setProperty("description",pEle.text());
									// start image
									String spotLightImage = FrameworkUtils.extractImagePath(sp, sb);
									log.debug("spotLightImage befor migration : " + spotLightImage + "\n");
									Node imageNode = spNode.hasNode("image")?spNode.getNode("image"):null;
									if(imageNode!=null){
										String fileReference = imageNode.hasProperty("fileReference")?imageNode.getProperty("fileReference").getString():"";
										spotLightImage = FrameworkUtils.migrateDAMContent(spotLightImage, fileReference, locale,sb);
										log.debug("spotLightImage after migration : " + spotLightImage + "\n");
										if (StringUtils.isNotBlank(spotLightImage)) {
											imageNode.setProperty("fileReference" , spotLightImage);
										}
									}else{
										sb.append(Constants.SPOTLIGHT_IMAGE_NODE_NOT_AVAILABLE);
									}
									// end image
									if(aEle!=null){
										spNode.setProperty("linktext",aEle.text());
										Node spCta = spNode.hasNode("cta")?spNode.getNode("cta"):null;
										if(spCta!=null){
											spCta.setProperty("url",aEle.attr("href"));
										}else{
											sb.append(Constants.CTA_NOT_AVAILABLE);
										}
									}
								}
							}
						}else{
							sb.append(Constants.SPOTLIGHT_ELEMENT_NOT_FOUND);
						}
						log.debug("Spotlight component(s) updated..");
					}catch(Exception e){
						sb.append(Constants.EXCEPTION_SPOTLIGHT_COMPONENT);
						log.error("unable to update spotlight component due to : ",e);
					}
					//end of spotlight

					//start of html blob
					try{
						Element htmlblobEle = doc.select("div.gd-left").select("div.htmlblob").first();
						Node hBlobNode = benifitsLeftNode.hasNode("htmlblob")?benifitsLeftNode.getNode("htmlblob"):null;
						if(htmlblobEle!=null){
							Element iframe = htmlblobEle.getElementsByTag("iframe").first();
							if(iframe!=null){
								if(hBlobNode!=null){
									hBlobNode.setProperty("html",htmlblobEle.outerHtml());
								}
							}else{
								sb.append(Constants.HTMLBLOB_ELEMENT_NOT_FOUND);
							}
						}else{
							sb.append(Constants.HTMLBLOB_ELEMENT_NOT_FOUND);
						}
					}catch(Exception e){
						sb.append(Constants.EXCEPTION_IN_HTMLBLOB);
						log.error("unable to update htmlblob due to : ",e);
					}
					//end of html blob
					//------------------ (end gd-left) ---------------//
					
					//------------------ (start gd-right) ---------------//
					
					//Start of Rightrail
					try{
						log.debug("start of right rail..");
						Elements tileEle = doc.select("div.gd-right").select("div.c23-pilot");
						int tileEleSize = tileEle.size();
						NodeIterator tileNodeIterator = benifitsRightNode.hasNode("tile_bordered")?benifitsRightNode.getNodes("tile_bordered*"):null;
						int tileNodeSize = (int)tileNodeIterator.getSize();
						if(tileEleSize==tileNodeSize){
							setTile(tileEle, tileNodeIterator);
						}else if(tileEleSize>tileNodeSize){
							setTile(tileEle, tileNodeIterator);
							sb.append(Constants.MISMATCH_OF_TILES_IN_RIGHT_RAIL+tileEleSize+Constants.LIST_NODES_COUNT+" ("+tileNodeSize+")");
						}else if(tileEleSize<tileNodeSize){
							setTile(tileEle, tileNodeIterator);
							sb.append(Constants.MISMATCH_OF_TILES_NODES_IN_RIGHT_RAIL+tileEleSize+Constants.LIST_NODES_COUNT+" ("+tileNodeSize+")");
						}
						log.debug("end of right rail..");
					}catch(Exception e){
						log.error("Exception in right rail : ",e);
					}
					//End of Rightrail
					
					//------------------ (end gd-right) ---------------//
					
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
	public void setTile(Elements tileEle,NodeIterator tileNodeIterator) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException{
		if(tileEle!=null){
			for(Element tile : tileEle){
				Element tTitleEle = tile.getElementsByTag("h2").first();
				if(tTitleEle==null){
					tTitleEle = tile.getElementsByTag("h3").first();
				}
				String tTitle = tTitleEle.text();
				String tDesc = tile.getElementsByTag("p").first().text();
				Element aEle = tile.getElementsByTag("a").first();
				Node tileNode = null;
				if(tileNodeIterator.hasNext()){
					tileNode = (Node)tileNodeIterator.next();
					tileNode.setProperty("title",tTitle);
					tileNode.setProperty("description",tDesc);
					tileNode.setProperty("linktext",aEle.text());
					if(tileNode.hasProperty("linkurl")){
						tileNode.setProperty("linkurl",aEle.attr("href"));	
					}
				}
				log.debug("tile updated..");
			}
		}else{
			sb.append("<li>tile elements in right rail not found</li>");
		}
	}
}
