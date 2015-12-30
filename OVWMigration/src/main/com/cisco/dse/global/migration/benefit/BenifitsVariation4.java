package com.cisco.dse.global.migration.benefit;

/* S.No			Name		Date		Description of change
 * 1			Anudeep		17-Dec-15	Added the Java file to handle the migration of benifits variation 4 with 1url.
 * 
 * */

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

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.cisco.dse.global.migration.config.Constants;
import com.cisco.dse.global.migration.config.BaseAction;
import com.cisco.dse.global.migration.config.FrameworkUtils;

public class BenifitsVariation4 extends BaseAction {

	Document doc;

	StringBuilder sb = new StringBuilder(1024);

	static Logger log = Logger.getLogger(BenifitsVariation4.class);

	public String translate(String host, String loc, String prod, String type, String catType,
			String locale, Session session, Map<String, String> urlMap) throws IOException,
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
					try{
						Elements textEle = doc.select("div.gd-left").select("div.c00-pilot");
						NodeIterator textNodeIterator = benifitsLeftNode.hasNodes()?benifitsLeftNode.getNodes("text*"):null;
						Node textNode = null;
						if(textEle != null){
							for(Element text : textEle){
								if(textNodeIterator.hasNext()){
									textNode = (Node)textNodeIterator.next();
									String textHtml = FrameworkUtils.extractHtmlBlobContent(text, "",locale, sb,urlMap);
									textNode.setProperty("text",textHtml);
								}
								else{
									sb.append(Constants.TEXT_NODE_NOT_FOUND);
								}
							}	
						}	
						else{
							sb.append(Constants.TEXT_ELEMENT_NOT_FOUND);
						}
					}catch(Exception e){
						log.debug("Exception in title" +e);
					}
					//end of text	

					//Start of Hero

					try{
						Element heroEle = doc.select("div.gd-left").select("div.heropanel").first();
						if(heroEle!=null){
							String heroTitle = heroEle.getElementsByTag("h2").first().text();
							String heroDesc = heroEle.getElementsByTag("p").first().text();
							Element heroCtaEle = heroEle.select("p.cta-link").first();
							Element heroCtaText = heroCtaEle.getElementsByTag("a").first();

							Node heroNode = benifitsLeftNode.hasNode("hero_large")?benifitsLeftNode.getNode("hero_large").getNode("heropanel_0"):null;
							if(heroNode!=null){
								// Start extracting valid href
								log.debug("Before anchorHref" + heroCtaText.attr("href") + "\n");
								String anchorHref = FrameworkUtils.getLocaleReference(heroCtaText.attr("href"), urlMap);
								log.debug("after anchorHref" + anchorHref + "\n");
								// End extracting valid href
								heroNode.setProperty("title",heroTitle);
								heroNode.setProperty("description",heroDesc);
								heroNode.setProperty("linktext",heroCtaText.text());
								heroNode.setProperty("linkurl",anchorHref);
							}else{
								sb.append(Constants.HERO_CONTENT_NODE_NOT_FOUND);
							}

						}else{
							sb.append(Constants.HERO_CONTENT_PANEL_ELEMENT_NOT_FOUND);
						}
					}catch(Exception e){
						log.error(e);
					}

					//End of Hero

					//Start of Spotlight
					try{
						Element spotLightEle = doc.select("div.gd-left").select("div.c11-pilot").first();
						if(spotLightEle!=null){
							String slTitle = spotLightEle.getElementsByTag("h2").first().text();
							String slDesc = spotLightEle.getElementsByTag("p").first().text();
							Element aEle = spotLightEle.getElementsByTag("a").first();

							Node spNode = benifitsLeftNode.hasNode("spotlight_medium_v2")?benifitsLeftNode.getNode("spotlight_medium_v2"):null;
							if(spNode!=null){
								spNode.setProperty("title",slTitle);
								spNode.setProperty("description",slDesc);
								spNode.setProperty("linktext",aEle.text());
								Node ctaNode = spNode.hasNode("cta")?spNode.getNode("cta"):null;
								if(ctaNode!=null){
									// Start extracting valid href
									log.debug("Before anchorHref" + aEle.attr("href") + "\n");
									String anchorHref = FrameworkUtils.getLocaleReference(aEle.attr("href"), urlMap);
									log.debug("after anchorHref" + anchorHref + "\n");
									// End extracting valid href
									spNode.setProperty("url",anchorHref);
								}else{
									sb.append(Constants.SPOTLIGHT_CTA_NODE_NOT_FOUND);
								}
							}else{
								sb.append(Constants.SPOTLIGHT_NODE_NOT_FOUND);
							}
						}else{
							sb.append(Constants.SPOTLIGHT_ELEMENT_NOT_FOUND);
						}
					}catch(Exception e){
						log.error("Exception in spotlight : "+e);
					}
					//End of Spotlight

					//Start of Rightrail
					try{			
						Elements tileEle = doc.select("div.gd-right").select("div.c23-pilot");
						int tileEleSize = tileEle.size();
						NodeIterator tileNodeIterator = benifitsRightNode.hasNode("tile_bordered")?benifitsRightNode.getNodes("tile_bordered*"):null;
						int tileNodeSize = (int)tileNodeIterator.getSize();

						if(tileEleSize==tileNodeSize){
							setTile(tileEle, tileNodeIterator, urlMap);
						}else if(tileEleSize>tileNodeSize){
							setTile(tileEle, tileNodeIterator, urlMap);
							sb.append(Constants.MISMATCH_OF_TILES_IN_RIGHT_RAIL+tileEleSize+Constants.LIST_NODES_COUNT+" ("+tileNodeSize+")");
						}else if(tileEleSize<tileNodeSize){
							setTile(tileEle, tileNodeIterator, urlMap);
							sb.append(Constants.MISMATCH_OF_TILES_NODES_IN_RIGHT_RAIL+tileEleSize+Constants.LIST_NODES_COUNT+" ("+tileNodeSize+")");
						}
					}catch(Exception e){
						log.error("Exception in right rail : "+e);
					}
					//End of Rightrail

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
	public void setTile(Elements tileEle,NodeIterator tileNodeIterator, Map<String, String> urlMap) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException{
		for(Element tile : tileEle){
			String tTitle = tile.getElementsByTag("h2").first().text();
			String tDesc = tile.getElementsByTag("p").first().text();
			Element aEle = tile.getElementsByTag("a").first();
			Node tileNode = null;
			if(tileNodeIterator.hasNext()){
				tileNode = (Node)tileNodeIterator.next();
				tileNode.setProperty("title",tTitle);
				tileNode.setProperty("description",tDesc);
				tileNode.setProperty("linktext",aEle.text());
				if(tileNode.hasProperty("linkurl")){
					// Start extracting valid href
					log.debug("Before anchorHref" + aEle.attr("href") + "\n");
					String anchorHref = FrameworkUtils.getLocaleReference(aEle.attr("href"), urlMap);
					log.debug("after anchorHref" + anchorHref + "\n");
					// End extracting valid href
					tileNode.setProperty("linkurl",anchorHref);	
				}/*else{
					sb.append(Constants.URL_NODE_NOT_FOUND_FOR_TILE_IN_RIGHT_RAIL);
				}*/
			}
		}
	}
}