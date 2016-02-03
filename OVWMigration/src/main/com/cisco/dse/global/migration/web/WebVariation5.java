package com.cisco.dse.global.migration.web;

/* S.No			Name		Date		Description of change
 * 1			Bhavya		28-Dec-15	Added the Java file to handle the migration of benifits variation 3 with 3url.
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

public class WebVariation5 extends BaseAction{

	Document doc = null;

	StringBuilder sb = new StringBuilder(1024);

	Logger log = Logger.getLogger(WebVariation5.class);

	public String translate(String host, String loc, String prod, String type,
			String catType, String locale, Session session,  Map<String, String> urlMap) throws IOException,
			ValueFormatException, VersionException, LockException,
			ConstraintViolationException, RepositoryException {

		log.debug("In the translate method WebVariation5");
		log.debug("In the translate method, catType is :" + catType);
		String pagePropertiesPath = "/content/<locale>/" + catType
				+ "/<prod>/professional-services/managed-cloud-services/jcr:content";
		String managedCloudServiceMid = "/content/<locale>/"
				+ catType
				+ "/<prod>/professional-services/managed-cloud-services/jcr:content/content_parsys/services/layout-services/gd21v1/gd21v1-mid";

		String pageUrl = host + "/content/<locale>/" + catType
				+ "/<prod>/professional-services/managed-cloud-services.html";
		pageUrl = pageUrl.replace("<locale>", locale).replace("<prod>", prod);
		pagePropertiesPath = pagePropertiesPath.replace("<locale>", locale)
				.replace("<prod>", prod);
		sb.append("<td>" + "<a href=" + pageUrl + ">" + pageUrl + "</a>"
				+ "</td>");
		sb.append("<td>" + "<a href=" + loc + ">" + loc + "</a>" + "</td>");
		sb.append("<td><ul>");

		managedCloudServiceMid = managedCloudServiceMid.replace("<locale>", locale).replace("<prod>",
				prod);

		javax.jcr.Node managedCloudServiceMidNode = null;
		javax.jcr.Node pageJcrNode = null;
		try {
			managedCloudServiceMidNode = session.getNode(managedCloudServiceMid);
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
					migrateHeroLarge(doc, managedCloudServiceMidNode,locale, urlMap);
				} catch (Exception e) {
					sb.append(Constants.EXCEPTION_TEXT_COMPONENT);
					log.error("Exception : ", e);
				}
				//End of migration of Hero Large Component
				
				//Start of migration of List Component
				try {
					migrateList(doc, managedCloudServiceMidNode,session, locale, urlMap);
				} catch (Exception e) {
					sb.append(Constants.EXCEPTION_TEXT_COMPONENT);
					log.error("Exception : ", e);
				}
				//End of migration of List Component

				
				//Start of migration of HTMLBLOB Component
				try {
					migrateHtmlBlob(doc, managedCloudServiceMidNode,locale, urlMap);
				} catch (Exception e) {
					sb.append(Constants.EXCEPTION_TEXT_COMPONENT);
					log.error("Exception : ", e);
				}
				//End of migration of HTMLBLOB Component

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
	private void migrateHeroLarge(Document doc,Node managedCloudServiceMidNode, String locale,Map<String, String> urlMap) {
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
			Node heroNode = managedCloudServiceMidNode.hasNode("hero_large") ? managedCloudServiceMidNode.getNode("hero_large") : null;

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
									aHref = FrameworkUtils.getLocaleReference(aHref, urlMap);
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


	private void migrateList(Document doc,Node managedCloudServiceMidNode, Session session, String locale,Map<String, String> urlMap) throws RepositoryException {
		Elements textElements = doc.select("div.c00-pilot");
		Element listElement = textElements.last();

		Node listNode = managedCloudServiceMidNode.hasNode("list") ? managedCloudServiceMidNode.getNode("list") : null;
		if (listNode != null) {
			if(listElement != null){
				try {
					String ownPdfText = "";
					String pdfIcon = "";
					String pdfSize = "";
					String ownText = "";
					Element h3Ele = listElement.getElementsByTag("h3").last();
					Element ulEle = listElement.getElementsByTag("ul").last();
					String h3Text = null;

					// start of handling title of list component
					if(h3Ele != null){
						h3Text = h3Ele.text();
						listNode.setProperty("title", h3Text);
					} else {
						sb.append(Constants.LIST_HEADING_COMPONENT_NOT_FOUND);

					}
					// end of handling title of list component

					//Element List
					Node ulNode = listNode.hasNode("element_list_0") ? listNode.getNode("element_list_0") : null;
					if (ulNode != null) {
						Elements list = ulEle.getElementsByTag("li");
						List<String> listAdd = new ArrayList<String>();
						for (Element li : list) {
							pdfIcon = "";
							pdfSize = "";
							ownText = "";
							boolean openNewWindow = false;
							// pdf content
							try {
								ownPdfText = li.ownText();
								if (StringUtils.isNotEmpty(ownPdfText)) {
									log.debug("OWn text is:" + ownPdfText);
									if (ownPdfText.toLowerCase()
											.contains("pdf")
											|| ownPdfText.toLowerCase()
											.contains("video")) {
										pdfIcon = "pdf";
										if (ownPdfText.toLowerCase().contains(
												"video")) {
											pdfIcon = "video";
										}
										int i = 0;
										for (; i < ownPdfText.length(); i++) {
											char character = ownPdfText
													.charAt(i);
											boolean isDigit = Character
													.isDigit(character);
											if (isDigit) {
												break;
											}
										}
										pdfSize = ownPdfText.substring(i,
												ownPdfText.length() - 1);
										pdfSize = pdfSize.replace(")", "");
										pdfSize = pdfSize.trim();
										/*if(ownPdfText.length() >16){
											ownText = li.ownText();
											pdfIcon = "";
											pdfSize = "";
										}*/
									}else {
										ownText = li.ownText();
									}
								}
							} catch (Exception e) {
								sb.append(Constants.Exception_BY_SPECIAL_CHARACTER);
								log.error("Exception : ", e);
							}

							if(!li.getElementsByTag("a").isEmpty()){
								Element a = li.getElementsByTag("a").first();
								// Start extracting valid href
								log.debug("Before anchorHref" + a.absUrl("href") + "\n");
								String anchorHref = FrameworkUtils.getLocaleReference(a.absUrl("href"), urlMap);
								log.debug("after anchorHref" + anchorHref + "\n");
								// End extracting valid href
								JSONObject obj = new JSONObject();
								obj.put("linktext", a.text()+ownText);
								obj.put("linkurl", anchorHref);
								obj.put("icon", pdfIcon);
								obj.put("size", pdfSize);
								obj.put("description", "");
								obj.put("openInNewWindow", openNewWindow);
								listAdd.add(obj.toString());
							}
						}
						ulNode.setProperty("listitems",listAdd.toArray(new String[listAdd.size()]));
					} else {
						sb.append(Constants.NO_LIST_NODES_FOUND);
					}
					// End of Element List
				} catch (Exception e) {
					sb.append(Constants.UNABLE_TO_MIGRATE_LIST_COMPONENT);
					log.error("Exception : ", e);
				}
			}else {
				sb.append(Constants.LIST_ELEMENT_NOT_FOUND);
			}
		} else {
			if(listElement != null){
				sb.append(Constants.LIST_NODE_NOT_FOUND);
			}
			else {
				log.debug("no list node or element to migrate");
			}
		}
	}

	// End of Migrate List Elements Method

	//start of migration of htmlblob
	private void migrateHtmlBlob(Document doc,Node managedCloudServiceMidNode, String locale,Map<String, String> urlMap) throws PathNotFoundException, RepositoryException {
		Elements textElements = doc.select("div.c00-pilot");
		Node midNode = managedCloudServiceMidNode.hasNode("htmlblob") ? managedCloudServiceMidNode.getNode("htmlblob") : null ;
		Element h3Last = null;
		Element ulLast = null;
		
		if(midNode != null){
			if(!textElements.isEmpty()){
				Element textElement = textElements.first();
				if(textElement != null){
					h3Last = textElement.getElementsByTag("h3").last();
					ulLast = textElement.getElementsByTag("ul").last();
					if(h3Last != null){
					h3Last.remove();
					}
					if(ulLast != null){
					ulLast.remove();
					}
					String html = FrameworkUtils.extractHtmlBlobContent(textElement, "",locale, sb, urlMap);
					midNode.setProperty("html", html);
				}
			}
			else {
				sb.append(Constants.RIGHT_GRID_ELEMENT_NOT_FOUND);
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



