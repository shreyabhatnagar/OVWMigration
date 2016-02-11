package com.cisco.dse.global.migration.web;

/* S.No			Name		Date		Description of change
 * 1			Bhavya		20-Jan-16	Added the Java file to handle the migration of benifits variation 3 with 3url.
 * 
 * */
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.sling.commons.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.cisco.dse.global.migration.config.BaseAction;
import com.cisco.dse.global.migration.config.Constants;
import com.cisco.dse.global.migration.config.FrameworkUtils;

public class WebVariation14 extends BaseAction {

	Document doc = null;

	StringBuilder sb = new StringBuilder(1024);

	Logger log = Logger.getLogger(WebVariation14.class);

	public String translate(String host, String loc, String prod, String type,
			String catType, String locale, Session session,  Map<String, String> urlMap) throws IOException,
			ValueFormatException, VersionException, LockException,
			ConstraintViolationException, RepositoryException {

		log.debug("In the translate method WebVariation14");
		log.debug("In the translate method, catType is :" + catType);
		String pagePropertiesPath = "/content/<locale>/" + catType
				+ "/<prod>/mobility/index/jcr:content";
		String mobilityMid = "/content/<locale>/"
				+ catType
				+ "/<prod>/mobility/index/jcr:content/content_parsys/solutions/layout-solutions/gd21v1/gd21v1-mid";

		String pageUrl = host + "/content/<locale>/" + catType
				+ "/<prod>/mobility/index.html";
		pageUrl = pageUrl.replace("<locale>", locale).replace("<prod>", prod);
		pagePropertiesPath = pagePropertiesPath.replace("<locale>", locale)
				.replace("<prod>", prod);
		sb.append("<td>" + "<a href=" + pageUrl + ">" + pageUrl + "</a>"
				+ "</td>");
		sb.append("<td>" + "<a href=" + loc + ">" + loc + "</a>" + "</td>");
		sb.append("<td><ul>");

		mobilityMid = mobilityMid.replace("<locale>", locale).replace("<prod>",
				prod);

		javax.jcr.Node mobilityMidNode = null;
		javax.jcr.Node pageJcrNode = null;
		try {
			mobilityMidNode = session.getNode(mobilityMid);
			pageJcrNode = session.getNode(pagePropertiesPath);

			try {
				doc = getConnection(loc);
			} catch (Exception e) {
				log.error("Exception : ", e);
			}

			if (doc != null) {

				// start set page properties.

				FrameworkUtils.setPageProperties(pageJcrNode, doc, session, sb);

				// end set page properties.


				//Start of migration of Hero Large Component
				try {
					migrateHeroLarge(doc, mobilityMidNode,locale, urlMap);
				} catch (Exception e) {
					sb.append(Constants.EXCEPTION_IN_HERO_MIGRATION);
					log.error("Exception : ", e);
				}
				//End of migration of Hero Large Component

				//Start of migration of HTMLBLOB Component
				try {
					migrateHtmlBlob(doc, mobilityMidNode,locale, urlMap);
				} catch (Exception e) {
					sb.append(Constants.EXCEPTION_IN_HTMLBLOB);
					log.error("Exception : ", e);
				}
				//End of migration of HTMLBLOB Component
				
				//Check for mid list
				try {
					Node listNode = mobilityMidNode.hasNode("list") ? mobilityMidNode.getNode("list") : null;
					Elements list = doc.select("div.n13-dm");
					if(listNode != null){
						if(list.isEmpty()){
						sb.append(Constants.LIST_ELEMENT_NOT_FOUND);
						}else {
							log.debug("list Available to migrate");
						}
					}else {
						log.debug("gd-right is not available");
					}
					
				} catch (Exception e) {
					sb.append(Constants.EXCEPTION_TEXT_COMPONENT);
					log.error("Exception : ", e);
				}
				//Check for mid list
				
				//Check for tabset
				try {
					Node cqNode = mobilityMidNode.hasNode("c17v1_cq") ? mobilityMidNode.getNode("c17v1_cq") : null;
					Elements cq = doc.select("div.c17v1-cq");
					if(cqNode != null){
						if(cq.isEmpty()){
						sb.append("<li>tabset is not available to migrate</li>");
						}else {
							log.debug("tabset is available to migrate");
						}
					}else {
						log.debug("gd-right is not available");
					}
					
				} catch (Exception e) {
					sb.append(Constants.EXCEPTION_TEXT_COMPONENT);
					log.error("Exception : ", e);
				}
				//Check for tabset
				
				
				//Check for right panel
				try {
					Elements textElements = doc.select("div.gd-right");
					if(!textElements.isEmpty()){
						sb.append(Constants.RIGHT_PANEL_NODE_NOT_FOUND);
					}else {
						log.debug("gd-right is not available");
					}
					
				} catch (Exception e) {
					sb.append(Constants.EXCEPTION_TEXT_COMPONENT);
					log.error("Exception : ", e);
				}
				//Check for right panel
			} else {
				sb.append(Constants.URL_CONNECTION_EXCEPTION);
			}
		} catch (Exception e) {
			sb.append(Constants.UNABLE_TO_MIGRATE_PAGE);
			log.error("Exception : ", e);
		}
		sb.append("</ul></td>");
		session.save();
		log.debug("Msg returned is " + sb.toString());
		return sb.toString();

	}

	//Start of migration of hero Large
	private void migrateHeroLarge(Document doc,Node mobilityMidNode, String locale,Map<String, String> urlMap) {
		// TODO Auto-generated method stub
		try {
			String h2Text = "";
			String pText = "";
			String aText = "";
			String aHref = "";
			Node heroPanelNode = null;
			Elements heroElements = doc.select("div.c50-pilot");
			if(!heroElements.select("div.frame").isEmpty()){
				heroElements = heroElements.select("div.frame");
			}
			Node heroNode = mobilityMidNode.hasNode("hero_large") ? mobilityMidNode.getNode("hero_large") : null;

			if (heroNode != null) {
				NodeIterator heroPanelNodeIterator = heroNode.hasNode("heropanel_0") ? heroNode.getNodes("heropanel*") : null;
				if(heroPanelNodeIterator != null){
					if(heroElements != null){
						int eleSize = heroElements.size();
						int nodeSize = (int) heroPanelNodeIterator.getSize();
						if(eleSize != nodeSize){
							log.debug("Hero component node count mismatch!");
							sb.append("<li>Hero Component count mis match. Elements on page are: "+eleSize+" Node Count is: "+nodeSize+"</li>");
						}
						for (Element ele : heroElements) {
							if (heroPanelNodeIterator.hasNext()) {
								heroPanelNode = (Node) heroPanelNodeIterator.next();
								Elements h2TagText = ele.getElementsByTag("h2");
								if (h2TagText != null) {
									h2Text = h2TagText.text();
									heroPanelNode.setProperty("title", h2Text);
								} else {
									sb.append(Constants.HERO_CONTENT_HEADING_ELEMENT_DOESNOT_EXISTS);
								}

								Elements descriptionText = ele.getElementsByTag("p");
								if (descriptionText != null) {
									pText = descriptionText.first().text();
									heroPanelNode.setProperty("description", pText);
								} else {
									sb.append(Constants.HERO_CONTENT_DESCRIPTION_ELEMENT_DOESNOT_EXISTS);
								}

								Elements anchorText = ele.getElementsByTag("a");
								if (!anchorText.isEmpty()) {
									aText = anchorText.text();
									aHref = anchorText.attr("href");
									// Start extracting valid href
									log.debug("Before heroPanelLinkUrl" + aHref + "\n");
									aHref = FrameworkUtils.getLocaleReference(aHref, urlMap, locale, sb);
									log.debug("after heroPanelLinkUrl" + aHref + "\n");
									// End extracting valid href
									heroPanelNode.setProperty("linktext", aText);
									heroPanelNode.setProperty("linkurl", aHref);
								} else {
									sb.append(Constants.HERO_CONTENT_ANCHOR_TEXT_IS_BLANK);
								}

								// start image
								Node heroPanelPopUpNode = null;
								Elements lightBoxElements = ele.select("div.c50-image").select("a.c26v4-lightbox");
								if(lightBoxElements != null && !lightBoxElements.isEmpty()){
									Element lightBoxElement = lightBoxElements.first();
									heroPanelPopUpNode = FrameworkUtils.getHeroPopUpNode(heroPanelNode);
									if(heroPanelPopUpNode != null){
										heroPanelPopUpNode.setProperty("popupHeader", h2Text);
									}else{
										sb.append("<li>Hero content video pop up node not found.</li>");
									}
								}

								String heroImage = FrameworkUtils.extractImagePath(ele, sb);
								log.debug("heroImage before migration : " + heroImage + "\n");
								if (heroPanelNode.hasNode("image")) {
									Node imageNode = heroPanelNode.getNode("image");
									String fileReference = imageNode.hasProperty("fileReference") ? imageNode.getProperty("fileReference").getString():"";
									heroImage = FrameworkUtils.migrateDAMContent(heroImage, fileReference, locale,sb);
									log.debug("heroImage after migration : " + heroImage + "\n");
									if (StringUtils.isNotBlank(heroImage)) {
										imageNode.setProperty("fileReference", heroImage);
									} else {
										sb.append(Constants.HERO_IMAGE_NOT_AVAILABLE);
									}
								} else {
									sb.append("<li>hero image node doesn't exist</li>");
								}
								// end image
							}
						}
					}else {		
						sb.append(Constants.HERO_CONTENT_PANEL_ELEMENT_NOT_FOUND);
					}
				}else {
					log.debug("hero panel node is not found");
				}
			}
			else{
				if(heroElements.isEmpty()){
					log.debug("Hero Large node and elements are not found");
				}
				else {
					sb.append(Constants.HERO_NODE_NOT_AVAILABLE);
				}
			}
		} catch (Exception e) {
			sb.append(Constants.EXCEPTOIN_IN_UPDATING_HERO_CONTENT);
			log.error("hero Error" + e);
		}

	}
	//end of hero large migration

	//start of migration of htmlblob
	private void migrateHtmlBlob(Document doc,Node mobilityMidNode, String locale,Map<String, String> urlMap) throws PathNotFoundException, RepositoryException {
		Elements textElements = doc.select("div.c00-pilot");
		Node midNode = mobilityMidNode.hasNode("text") ? mobilityMidNode.getNode("text") : null ;
		Element firstEle = null;
		Element parentText = null;
		
		if(midNode != null){
			if(!textElements.isEmpty()){
				firstEle = textElements.first();
				if(firstEle != null){
					parentText = firstEle.parent();
					if(parentText != null){
						String html = FrameworkUtils.extractHtmlBlobContent(parentText, "",locale, sb, urlMap);
						midNode.setProperty("text", html);
					}else {
						sb.append("<li>Parent text does not exists</li>");
					}
				}else {
					sb.append(Constants.TEXT_DOES_NOT_EXIST);
				}
			}else {
				sb.append(Constants.TEXT_DOES_NOT_EXIST);
			}
		}else {
			if(textElements.isEmpty()){
				log.debug("nothing to migrate ");
			} else {
				sb.append(Constants.HTMLBLOB_NODE_DOES_NOT_EXIST);
			}
		}
	}
	//end of migration of htmlblob

}
