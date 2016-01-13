package com.cisco.dse.global.migration.trainingevents;

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

import com.cisco.dse.global.migration.benefit.BenefitsVariation03;
import com.cisco.dse.global.migration.config.BaseAction;
import com.cisco.dse.global.migration.config.Constants;
import com.cisco.dse.global.migration.config.FrameworkUtils;

public class TrainingAndEventsVariation1 extends BaseAction{
	Document doc = null;

	StringBuilder sb = new StringBuilder(1024);

	Logger log = Logger.getLogger(TrainingAndEventsVariation1.class);

	public String translate(String host, String loc, String prod, String type,
			String catType, String locale, Session session,  Map<String, String> urlMap) throws IOException,
			ValueFormatException, VersionException, LockException,
			ConstraintViolationException, RepositoryException {

		log.debug("In the translate method TrainingAndEventsVariation1");
		log.debug("In the translate method, catType is :" + catType);
		String pagePropertiesPath = "/content/<locale>/" + catType+ "/jcr:content";
		String trainingAndEventsLeft = "/content/<locale>/"+ catType + "/jcr:content/content_parsys/training/layout-training/gd12v2/gd12v2-left";
		String trainingAndEventsRight = "/content/<locale>/"+ catType + "/jcr:content/content_parsys/training/layout-training/gd12v2/gd12v2-right";

		String pageUrl = host + "/content/<locale>/" + catType+ ".html";
		pageUrl = pageUrl.replace("<locale>", locale);
		pagePropertiesPath = pagePropertiesPath.replace("<locale>", locale);
		sb.append("<td>" + "<a href=" + pageUrl + ">" + pageUrl + "</a>" + "</td>");
		sb.append("<td>" + "<a href=" + loc + ">" + loc + "</a>" + "</td>");
		sb.append("<td><ul>");

		trainingAndEventsLeft = trainingAndEventsLeft.replace("<locale>", locale);
		trainingAndEventsRight = trainingAndEventsRight.replace("<locale>", locale);

		javax.jcr.Node trainingAndEventsLeftNode = null;
		javax.jcr.Node trainingAndEventsRightNode = null;
		javax.jcr.Node pageJcrNode = null;
		try {
			trainingAndEventsLeftNode = session.getNode(trainingAndEventsLeft);
			trainingAndEventsRightNode = session.getNode(trainingAndEventsRight);
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
					migrateHeroLarge(doc, trainingAndEventsLeftNode,locale, urlMap);
				} catch (Exception e) {
					sb.append(Constants.EXCEPTION_TEXT_COMPONENT);
					log.error("Exception : ", e);
				}
				//End of migration of Hero Large Component

				//Start of migration of HTMLBLOB Component
				try {
					migrateTextAndHtmlBlob(doc, trainingAndEventsLeftNode,locale, urlMap);
				} catch (Exception e) {
					log.debug(Constants.EXCEPTION_TEXT_COMPONENT);
					log.error("Exception : ", e);
				}
				//End of migration of HTMLBLOB Component

				//Start of migration of Right List Component
				try {
					migrateRightList(doc, trainingAndEventsRightNode,session, locale, urlMap);
				} catch (Exception e) {
					log.debug(Constants.EXCEPTION_TEXT_COMPONENT);
					log.error("Exception : ", e);
				}
				//End of migration of Right List Component
				
				//Check for optin Banner
				Element optinBanner = doc.getElementById("optinbanner");
				if(optinBanner != null){
					sb.append(Constants.EXTRA_IMG_FOUND_IN_RIGHT_PANEL);
				}
				//Check for optin Banner
				

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

	private void migrateHeroLarge(Document doc,Node trainingAndEventsLeftNode, String locale,Map<String, String> urlMap) {
		// TODO Auto-generated method stub
		try {
			String h2Text = "";
			String pText = "";
			String aText = "";
			String aHref = "";
			Node heroPanelNode = null;
			Elements heroElements = doc.select("div.c50-pilot");
			if (heroElements.size() > 0) {
			
				if(!heroElements.select("div.frame").isEmpty()){
				heroElements = heroElements.select("div.frame");
				}
				Node heroNode = trainingAndEventsLeftNode.hasNode("hero_large") ? trainingAndEventsLeftNode.getNode("hero_large") : null;
	
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
									String heroImage = FrameworkUtils.extractImagePath(ele, sb);
									log.debug("heroImage before migration : " + heroImage + "\n");
									if (heroPanelNode.hasNode("image")) {
										Node imageNode = heroPanelNode.getNode("image");
										String fileReference = imageNode.hasProperty("fileReference") ? imageNode.getProperty("fileReference").getString():"";
										heroImage = FrameworkUtils.migrateDAMContent(heroImage, fileReference, locale,sb);
										log.debug("heroImage after migration : " + heroImage + "\n");
										if (StringUtils.isNotBlank(heroImage)) {
											imageNode.setProperty("fileReference", heroImage);
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
		 } else {
			 sb.append("<li>Hero component not found in web url</li>");
		 }
		} catch (Exception e) {
			sb.append(Constants.EXCEPTOIN_IN_UPDATING_HERO_CONTENT);
			log.error("hero Error" + e);
		}

	}

	private void migrateTextAndHtmlBlob(Document doc,Node trainingAndEventsLeftNode, String locale,Map<String, String> urlMap) throws PathNotFoundException, RepositoryException {
		Elements htmlBlobElements = null;
		
		Node htmlBlobNode = trainingAndEventsLeftNode.hasNode("htmlblob_0") ? trainingAndEventsLeftNode.getNode("htmlblob_0") : null;
		if (locale.equals("en_au")) {
			String html = "";
			Elements gd21PilotElements = doc.select("div.gd21-pilot");
			if (gd21PilotElements != null) {
				for (Element gd21PilotElement : gd21PilotElements) {
					if (gd21PilotElement.select("div.c50-pilot ").size() > 0) {
						gd21PilotElement.select("div.c50-pilot ").remove();
					}
					html = html + FrameworkUtils.extractHtmlBlobContent(gd21PilotElement, "",locale, sb, urlMap);
				}
			}
			Elements gd22PilotElements = doc.select("div.gd22-pilot");
			if (gd22PilotElements != null) {
				for (Element gd22PilotElement : gd22PilotElements) {
					html = html + FrameworkUtils.extractHtmlBlobContent(gd22PilotElement, "",locale, sb, urlMap);
				}
			}
			if(htmlBlobNode != null){
				if (StringUtils.isNotBlank(html)) {
					htmlBlobNode.setProperty("html", html);
				}
			} 
		} else {
			Elements gdLeftElements = doc.select("div.gd-left");
			if (gdLeftElements != null) {
				Element gdLeftElement = gdLeftElements.first();
				if (gdLeftElement != null) {
					htmlBlobElements = gdLeftElement.getElementsByTag("table");
				}
			}
			
			if(htmlBlobElements == null){
				htmlBlobElements = doc.select("div.c00-pilot");
			}
	     
		if(htmlBlobElements != null && htmlBlobElements.size() > 0){
			if(htmlBlobNode != null){
				
					Element htmlBlobEle = htmlBlobElements.first();
					String html = FrameworkUtils.extractHtmlBlobContent(htmlBlobEle, "",locale, sb, urlMap);
					htmlBlobNode.setProperty("html", html);
				
			}
			else {
				if(!htmlBlobElements.isEmpty()){
					sb.append(Constants.HTMLBLOB_NODE_DOES_NOT_EXIST);
					log.debug("html blob element exists but node does not exists");
				}
				else {
					log.debug("htmlblob element and node does not exists");
				}
			}
		} else {
			sb.append(Constants.HTMLBLOB_ELEMENT_NOT_FOUND);
	}
	}
	}

	private void migrateRightList(Document doc,Node trainingAndEventsRightNode, Session session, String locale,Map<String, String> urlMap) throws RepositoryException {
		Elements listElements = doc.select("div.n13-pilot");
		
		//Check for the follow us
			Elements followUs = doc.select("div.s14-pilot");
			if(!followUs.isEmpty()){
				sb.append(Constants.FOLLOWUS_NODE_NOT_FOUND);
			}
			else{
				log.debug("Follow us does not exists");
			}
		//end of check for follow us
			
		//Check for image
			Element listEle = listElements.first();
			if(listEle != null){
				Elements imgElements = listEle.getElementsByTag("img");
				if (imgElements != null && imgElements.size() > 0) {
					int count = 0;
					for (Element imgElement : imgElements) {
						count = count + 1;
					}
					sb.append("<li>" +""+count +" extra images found in the right List</li>");
				}
				Element sibling = listEle.nextElementSibling();
				if(sibling != null){
					Elements image = sibling.getElementsByTag("img");
					if(!image.isEmpty()){
					sb.append(Constants.EXTRA_IMG_FOUND_IN_RIGHT_PANEL);
					}
				}
			}
		//end of check for image
		if (listElements.size() > 0) {
			int count = 0;
			for (Element listElement : listElements) {
				if (listElement.parent().hasClass("gd-right")) {
					count = count + 1;
				}
			}
			NodeIterator listNodeIterator = trainingAndEventsRightNode.hasNodes() ? trainingAndEventsRightNode.getNodes("list*") : null;
			if (listNodeIterator != null) {
				int nodeSize = (int) listNodeIterator.getSize();
				log.debug("node Size" + nodeSize + "ele Size" + count);
				if (count == nodeSize) {
					Node listNode = null;
					for (Element ele : listElements) {
						listNode = (Node) listNodeIterator.next();
						setListElements(ele, listNode, session, locale, urlMap);
					}
				} else if (nodeSize < count) {
					Node listNode;
					for (Element ele : listElements) {
						if (listNodeIterator.hasNext()) {
							listNode = (Node) listNodeIterator.next();
							setListElements(ele, listNode, session, locale, urlMap);
						}
					}
					sb.append(Constants.MISMATCH_IN_LIST_NODES + count
							+ Constants.LIST_NODES_COUNT + nodeSize);
				}
	
				else if (nodeSize > count) {
					Node listNode;
					for (Element ele : listElements) {
						listNode = (Node) listNodeIterator.next();
						setListElements(ele, listNode, session, locale, urlMap);
					}
					sb.append(Constants.MISMATCH_IN_LIST_NODES + count
							+ Constants.LIST_NODES_COUNT + nodeSize);
				}
			} else {
				sb.append(Constants.LIST_NODE_NOT_FOUND);
			}
		} else {
			sb.append("<li>List component not found in web url</li>");
		}
	}

	private void setListElements(Element ele, Node rightListNode,Session session, String locale, Map<String, String> urlMap) {
		try {
			String ownPdfText = "";
			String pdfIcon = "";
			String pdfSize = "";
			Elements h2Ele = ele.getElementsByTag("h2");
			Elements h3Ele = ele.getElementsByTag("h3");
			Elements ulEle = ele.getElementsByTag("ul");
			String h2Text = null;
			String h3Text = null;

			// start of handling title of list component
			if(!h2Ele.isEmpty()){
				h2Text = h2Ele.first().text();
				rightListNode.setProperty("title", h2Text);
				if(h2Ele.size() >1){
					sb.append(Constants.MISMATCH_IN_RIGHT_LIST_COUNT);
				}
			} else {
				sb.append(Constants.LIST_HEADING_COMPONENT_NOT_FOUND);

			}
			// end of handling title of list component

			// start of handling title of list component
			NodeIterator h3Iterator = rightListNode.hasNode("element_subtitle_0") ? rightListNode.getNodes("element_subtitle*") : null;
			if(h3Iterator != null){
				if(!h3Ele.isEmpty()){
					int eleSize = h3Ele.size();
					int nodeSize = (int) h3Iterator.getSize();
					Node h3nodeList;
					if (eleSize == nodeSize) {
						for(Element h3Itr :h3Ele ){
							h3nodeList = (Node) h3Iterator.next();
							h3Text = h3Itr.text();
							h3nodeList.setProperty("subtitle", h3Text);
						}
					}

					if (nodeSize < eleSize) {
						for(Element h3Itr :h3Ele ){
							if(h3Iterator.hasNext()){
								h3nodeList = (Node) h3Iterator.next();
								h3Text = h3Itr.text();
								h3nodeList.setProperty("subtitle", h3Text);	
							}
						}
						sb.append(Constants.MISMATCH_IN_LIST_ELEMENT + nodeSize + Constants.SPOTLIGHT_ELEMENT_COUNT + eleSize);
					}
					if (nodeSize > eleSize) {
						for(Element h3Itr :h3Ele ){
							h3nodeList = (Node) h3Iterator.next();
							h3Text = h3Itr.text();
							h3nodeList.setProperty("subtitle", h3Text);	
						}
						sb.append(Constants.LIST_ELEMENTS_COUNT_MISMATCH + nodeSize + Constants.SPOTLIGHT_ELEMENT_COUNT + eleSize);
					}

				}else {
					sb.append(Constants.LIST_HEADING_COMPONENT_NOT_FOUND);
					log.debug("h3 text is not avalable");
				}
			}else {
				if(!h3Ele.isEmpty()){
					log.debug("subtitle node doesnot exist but ele exist");
					sb.append(Constants.LIST_COMPONENT_NOT_FOUND);
				}
			}
			// end of handling title of list component

			//Element List
			NodeIterator ulNodeIterator = rightListNode.hasNode("element_list_0") ? rightListNode.getNodes("element_list*") : null;
			if (ulNodeIterator != null) {
				Node ulnodeList;
				for (Element ulItr : ulEle) {
					if (ulNodeIterator.hasNext()) {
						ulnodeList = (Node) ulNodeIterator.next();
						Elements list = ulItr.getElementsByTag("li");
						List<String> listAdd = new ArrayList<String>();
						for (Element li : list) {
							pdfIcon = "";
							pdfSize = "";
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
									}
								}
							} catch (Exception e) {
								sb.append(Constants.Exception_BY_SPECIAL_CHARACTER);
								log.error("Exception : ", e);
							}
							
							//fix for new win icon
							Elements newwinCheck = li.select("span.newwin");
							if(!newwinCheck.isEmpty()){
								log.debug("extra new win icon found");
								sb.append(Constants.EXTRA_ICON_FOUND_IN_LIST);
							}
							
							//check for the lock icon
							Elements imgInList = li.getElementsByTag("img");
							if(!imgInList.isEmpty()){
								String altImg = imgInList.attr("alt");
								if(altImg.equals("lock_icon")){
									log.debug("lock icon found in the list");
									sb.append(Constants.EXTRA_LOCK_IMG_FOUND_IN_LIST);
								}
							}
							
							if(!li.getElementsByTag("a").isEmpty()){
								Element a = li.getElementsByTag("a").first();
								// Start extracting valid href
								log.debug("Before anchorHref" + a.absUrl("href") + "\n");
								String anchorHref = FrameworkUtils.getLocaleReference(a.absUrl("href"), urlMap);
								log.debug("after anchorHref" + anchorHref + "\n");
								// End extracting valid href
								JSONObject obj = new JSONObject();
								obj.put("linktext", a.text());
								obj.put("linkurl", anchorHref);
								obj.put("icon", pdfIcon);
								obj.put("size", pdfSize);
								obj.put("description", "");
								obj.put("openInNewWindow", openNewWindow);
								listAdd.add(obj.toString());
							}
						}
						ulnodeList.setProperty("listitems",listAdd.toArray(new String[listAdd.size()]));
					}
				}
					if(ulNodeIterator.hasNext()){
					sb.append(Constants.MISMATCH_IN_RIGHT_LIST_COUNT);
				}
			} else {
				sb.append(Constants.NO_LIST_NODES_FOUND);
			}
			// End of Element List
		} catch (Exception e) {
			sb.append(Constants.UNABLE_TO_MIGRATE_LIST_COMPONENT);
			log.error("Exception : ", e);
		}

	}

	// End of Migrate List Elements Method

}


