package com.cisco.dse.global.migration.architechture;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;

import org.apache.log4j.Logger;
import org.apache.sling.commons.json.JSONObject;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.cisco.dse.global.migration.config.BaseAction;
import com.cisco.dse.global.migration.config.Constants;
import com.cisco.dse.global.migration.config.FrameworkUtils;

public class ArchitechtureVariation1 extends BaseAction{

	/**
	 * @param args
	 */

	Document doc = null;

	StringBuilder sb = new StringBuilder(1024);

	Logger log = Logger.getLogger(ArchitechtureVariation1.class);

	public String translate(String host, String loc, String prod, String type,
			String catType, String locale, Session session, Map<String, String> urlMap) throws IOException,
			ValueFormatException, VersionException, LockException,
			ConstraintViolationException, RepositoryException {

		log.debug("In the translate method");
		log.debug("In the translate method, catType is :" + catType);
		String pagePropertiesPath = "/content/<locale>/"+catType+"/<prod>/architecture/jcr:content";
		String architectureLeft = "/content/<locale>/"+ catType+ "/<prod>/architecture/jcr:content/content_parsys/architecture/layout-architecture/gd12v2/gd12v2-left";
		String architectureRight = "/content/<locale>/"+ catType+ "/<prod>/architecture/jcr:content/content_parsys/architecture/layout-architecture/gd12v2/gd12v2-right";

		String pageUrl = host + "/content/<locale>/"+ catType + "/<prod>/architecture.html";
		pageUrl = pageUrl.replace("<locale>", locale).replace("<prod>", prod);
		pagePropertiesPath = pagePropertiesPath.replace("<locale>", locale).replace("<prod>", prod);
		sb.append("<td>" + "<a href=" + pageUrl + ">" + pageUrl + "</a>"+ "</td>");
		sb.append("<td>" + "<a href=" + loc + ">" + loc + "</a>" + "</td>");
		sb.append("<td><ul>");

		architectureLeft = architectureLeft.replace("<locale>", locale).replace("<prod>", prod);
		architectureRight = architectureRight.replace("<locale>", locale).replace("<prod>", prod);

		javax.jcr.Node architectureLeftNode = null;
		javax.jcr.Node architectureRightNode = null;
		javax.jcr.Node pageJcrNode = null;
		try {
			architectureLeftNode = session.getNode(architectureLeft);
			architectureRightNode = session.getNode(architectureRight);
			pageJcrNode = session.getNode(pagePropertiesPath);


			try{
				doc = getConnection(loc);
			}catch(Exception e){
				log.error("Exception : ",e);
			}


			if(doc != null){

				// start set page properties.

				FrameworkUtils.setPageProperties(pageJcrNode, doc, session, sb);

				// end set page properties.

				// start of text component
				try{
					migrateTextComponents(doc, architectureLeftNode ,locale, urlMap);
				}catch(Exception e){
					sb.append(Constants.EXCEPTION_TEXT_COMPONENT);
					log.error("Exception : ",e);
				}	
				// end of text component

				//Start of List Component
				try {
					migratelistElements(doc,architectureLeftNode,session,urlMap,locale);
				}
				catch(Exception e){
					sb.append("Exception in List Component");
					log.error("Exception : ",e);
				}

				//End of List Component

				//Start of Right Rail
				try{
					migraterightRailElements(doc,architectureRightNode,urlMap,locale);
				}
				catch(Exception e)
				{
					sb.append("Exception in right rail Elements");
					log.error("Exception : ",e);
				}
				// End of Right Rail
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

	// Start Migrate Text Method
	public void migrateTextComponents(Document doc, Node architectureLeftNode, String locale, Map<String, String> urlMap) throws RepositoryException{

		Elements textElements = doc.select("div.c00-pilot");

		if(textElements == null){
			sb.append(Constants.TEXT_ELEMENT_NOT_FOUND);
		}
		else{
			int eleSize = textElements.size();
			NodeIterator textNodeIterator = architectureLeftNode.hasNode("text") ?architectureLeftNode.getNodes("text*"):null;
			if(textNodeIterator != null){
				int nodeSize = (int)textNodeIterator.getSize();
				log.debug("node Size" + nodeSize + "ele Size" + eleSize);
				if(eleSize == nodeSize){
					Node textNode;
					for(Element ele : textElements){
						textNode = (Node)textNodeIterator.next();
						if(ele.hasClass("mboxDefault")){
							log.debug("has mboxDefault class..");
							ele.select("div.mboxDefault").remove();
							log.debug("mboxDefault removed..");
						}
						textNode.setProperty("text", FrameworkUtils.extractHtmlBlobContent(ele, "", locale, sb, urlMap));
					}
				}
				else if(nodeSize < eleSize){
					Node textNode;
					for(Element ele : textElements){
						if(textNodeIterator.hasNext()){
							textNode = (Node)textNodeIterator.next();
							if(ele.hasClass("mboxDefault")){
								log.debug("has mboxDefault class..");
								ele.select("div.mboxDefault").remove();
								log.debug("mboxDefault removed..");
							}
							textNode.setProperty("text", FrameworkUtils.extractHtmlBlobContent(ele, "", locale, sb, urlMap));
						}
						else{
							sb.append(Constants.Text_Element_Mismatch+nodeSize+Constants.TEXT_ELEMENT_COUNT+eleSize+"</li>");
						}
					}
				}
				else if(nodeSize > eleSize){
					Node textNode;
					for(Element ele : textElements){
						textNode = (Node)textNodeIterator.next();
						if(ele.hasClass("mboxDefault")){
							log.debug("has mboxDefault class..");
							ele.select("div.mboxDefault").remove();
							log.debug("mboxDefault removed..");
						}
						textNode.setProperty("text", FrameworkUtils.extractHtmlBlobContent(ele, "", locale, sb, urlMap));
					}
					sb.append(Constants.Text_Element_Mismatch+nodeSize+Constants.TEXT_ELEMENT_COUNT+eleSize+"</li>");
				}
			}
			else{
				sb.append(Constants.TEXT_NODE_NOT_FOUND);
			}
		}
	}
	//End of Migrate Text Method

	// Start of Migrate List Elements method
	private void migratelistElements(Document doc, Node architectureLeftNode, Session session, Map<String, String> urlMap, String locale) throws RepositoryException {
		Elements listElements = doc.select("div.gd-left").select("div.n13-pilot");

		if(listElements == null || listElements.size() == 0){
			sb.append(Constants.LIST_COMPONENT_NOT_FOUND);
		}
		else {
			int eleSize = listElements.size();
			NodeIterator listNodeIterator = architectureLeftNode.hasNode("list") ?architectureLeftNode.getNodes("list*"):null;
			if(listNodeIterator != null){
				int nodeSize = (int)listNodeIterator.getSize();
				log.debug("node Size" + nodeSize + "ele Size" + eleSize);
				if(eleSize == nodeSize){
					Node listNode;
					for(Element ele : listElements){
						listNode = (Node)listNodeIterator.next();
						setListElements(ele , listNode ,session,urlMap, locale);
					}
				}
				else if(nodeSize < eleSize){
					Node listNode;
					for(Element ele : listElements){
						if(listNodeIterator.hasNext()){
							listNode = (Node)listNodeIterator.next();
							setListElements(ele , listNode ,session,urlMap, locale);
						}
					}	
					sb.append(Constants.MISMATCH_IN_LIST_NODES+eleSize+Constants.LIST_NODES_COUNT+nodeSize);
				}

				else if(nodeSize > eleSize){
					Node listNode;
					for(Element ele : listElements){
						listNode = (Node)listNodeIterator.next();
						setListElements(ele , listNode ,session,urlMap, locale);
					}
					sb.append(Constants.MISMATCH_IN_LIST_NODES+eleSize+Constants.LIST_NODES_COUNT+nodeSize);
				}
			}

			else {
				sb.append(Constants.NO_LIST_NODE_FOUND);
			}

		}

	}

	private void setListElements(Element ele, Node architectureListNode, Session session, Map<String, String> urlMap, String locale) {
		try{
			Elements h2Ele = ele.getElementsByTag("h2");
			Elements h3Ele = ele.getElementsByTag("h3");
			Elements ulEle = ele.getElementsByTag("ul");
			//title Node
			if(!h2Ele.isEmpty() && h2Ele != null){
				architectureListNode.setProperty("title", h2Ele.first().text());
			}else{
				sb.append(Constants.LIST_HEADING_COMPONENT_NOT_FOUND);
			}

			//subtitle Node

			NodeIterator h3NodeIterator = architectureListNode.hasNode("element_subtitle_0") ?architectureListNode.getNodes("element_subtitle*"):null;
			if(h3NodeIterator != null){
				int listNodeSize = (int)h3NodeIterator.getSize();
				if(!h3Ele.isEmpty() && h3Ele != null){
					int h3EleSize = h3Ele.size();
					if(h3EleSize == listNodeSize){
						Node h3Node;
						for(Element h3 : h3Ele){
							h3Node = (Node)h3NodeIterator.next();
							h3Node.setProperty("subtitle",h3.text());
						}
					}
					else if(h3EleSize > listNodeSize){
						Node h3Node;
						for(Element h3 : h3Ele){
							if(h3NodeIterator.hasNext()){
								h3Node = (Node)h3NodeIterator.next();
								h3Node.setProperty("subtitle",h3.text());
							}
						}
					}
					else if(h3EleSize < listNodeSize){
						Node h3Node;
						for(Element h3 : h3Ele){
							h3Node = (Node)h3NodeIterator.next();
							h3Node.setProperty("subtitle",h3.text());
						}
					}
				}else{
					sb.append(Constants.NO_SUBTITLE_LIST_ELEMENT_FOUND);
				}
			}else {
				sb.append(Constants.NO_SUBTITLE_LIST_NODE_FOUND);
			}

			// Element List
			NodeIterator ulNodeIterator = architectureListNode.hasNode("element_list_0") ? architectureListNode.getNodes("element_list*") : null;
			if(ulNodeIterator != null){
				Node ulnodeList;
				for(Element ulItr : ulEle){
					if(ulNodeIterator.hasNext()){
						ulnodeList = (Node)ulNodeIterator.next();
						if(ulnodeList.getName().equals("element_list_0")){
							ulnodeList = (Node)ulNodeIterator.next();
						}
						if(!ulItr.hasClass("no-bullets")){
							Elements list = ulItr.getElementsByTag("li");
							List<String> listAdd = new ArrayList<String>();
							for (Element li : list){
								boolean openNewWindow = false;
								//pdf content
								String pdf = li.ownText().trim();
								String pdfIcon = null;
								try{
									log.debug(pdf);
									if (pdf.length() > 0) {
										if (pdf.toLowerCase().contains("pdf"))
											pdfIcon = "pdf";
										int i = 0;
										for (; i < pdf.length(); i++) {
											char character = pdf.charAt(i);
											boolean isDigit = Character.isDigit(character);
											if (isDigit) {
												break;
											}
										}
										pdf = pdf.substring(i, pdf.length() - 1);
									}
									pdf = pdf.trim();
								}catch(Exception e){
									sb.append(Constants.Exception_BY_SPECIAL_CHARACTER);
									log.error("Exception : ",e);
								}
								Element a = li.getElementsByTag("a").first();
								String aURL = a.absUrl("href");
								if(StringUtil.isBlank(aURL)){
									aURL = a.attr("href");
								}
								aURL = FrameworkUtils.getLocaleReference(aURL, urlMap, locale, sb);
								JSONObject obj = new JSONObject();
								obj.put("linktext", a.text());
								obj.put("linkurl",aURL);
								obj.put("icon",pdfIcon);
								obj.put("size",pdf);
								obj.put("description","");
								obj.put("openInNewWindow",openNewWindow);
								listAdd.add(obj.toString());
							}
							Property listitems = ulnodeList.getProperty("listitems");
							if (!listitems.isMultiple()) {
								listitems.remove();
								session.save();
							}
							ulnodeList.setProperty("listitems",listAdd.toArray(new String[listAdd.size()]));
						}else{
							sb.append("<li>Extra list elemnt found in web publisher page left rail. As there is no node to map it, that list is not migrated</li>");
						}
					}else{
						sb.append(Constants.MISMATCH_IN_LIST_COUNT);
					}
				}
				if(ulNodeIterator.hasNext()){
					sb.append(Constants.MISMATCH_IN_LIST_COUNT);
				}
			}else{
				sb.append(Constants.NO_LIST_NODES_FOUND);
			}
			// End of Element List
		}catch(Exception e){
			sb.append(Constants.UNABLE_TO_MIGRATE_LIST_COMPONENT);
			log.error("Exception : ",e);
		}

	}
	//End of Migrate List Elements Method

	//start Migrate right rail Method
	private void migraterightRailElements(Document doc, Node architectureRightNode, Map<String, String> urlMap, String locale) {
		try {
			boolean migrate = true;
			Elements rightRailList = doc.select("div.gd-right").select("div.c23-pilot");

			if (!rightRailList.isEmpty() && rightRailList != null ) {
				int eleSize = rightRailList.size();
				if (eleSize == 1) {
					Element rightListElem =  rightRailList.first();
					if (rightListElem != null) {
						Elements ulElements = rightListElem.getElementsByTag("ul");
						if (ulElements.size() > 1) {
							sb.append(Constants.UNABLE_TO_MIGRATE_RIGHTRAIL);
							migrate = false;
						}
					}
				}
			}

			if (migrate) {
				if (rightRailList.isEmpty()) {
					log.debug("No right rail elements found on locale page");
					sb.append(Constants.RIGHT_GRID_ELEMENT_NOT_FOUND);
				}
				else {
					int eleSize = rightRailList.size();
					NodeIterator tileIterator = architectureRightNode.hasNode("tile_bordered") ? architectureRightNode.getNodes("tile_bordered*") : null;
					if(tileIterator != null){
						int nodeSize = (int)tileIterator.getSize();

						if(eleSize == nodeSize){
							Node listNode;
							for (Element rightListEle : rightRailList) {
								listNode = (Node)tileIterator.next();
								setRightRailList(listNode, rightListEle,urlMap,locale);
							}
						}
						else if (eleSize > nodeSize) {
							Node listNode;
							for (Element rightListEle : rightRailList) {
								if (tileIterator.hasNext()) {
									listNode = (Node)tileIterator.next();
									setRightRailList(listNode, rightListEle,urlMap,locale);						}
								else {
									log.debug("Next node not found");
									sb.append(Constants.TILEBORDER_Element_ON_LOCALE_PAGE+ eleSize +Constants.TILEBORDER_NODE+ nodeSize +"</li>");
									break;								
								}

							}
						}
						else if (eleSize < nodeSize) {
							Node listNode;
							for (Element rightListEle : rightRailList) {
								listNode = (Node)tileIterator.next();
								setRightRailList(listNode, rightListEle,urlMap,locale);						
							}
							sb.append(Constants.TILEBORDER_Element_ON_LOCALE_PAGE+ eleSize +Constants.TILEBORDER_NODE+ nodeSize +"</li>");
						}
					}
				}
			}

		} catch (Exception e) {
			sb.append(Constants.UNABLE_TO_MIGRATE_RIGHTRAIL);
			log.error("Exception : ",e);
		}
	}

	public void setRightRailList (Node listNode, Element rightListEle, Map<String, String> urlMap, String locale) {
		try {
			Element title;
			Element description;
			Elements anchor = rightListEle.getElementsByTag("a");
			Elements headElements = rightListEle.getElementsByTag("h2");

			if (headElements.size() > 1) {
				title = rightListEle.getElementsByTag("h2").last();
				description = rightListEle.getElementsByTag("p").last();
				sb.append(Constants.MISMATCH_IN_TILEBORDER_COUNT);
			}
			else {
				title = rightListEle.getElementsByTag("h2").first();
				description = rightListEle.getElementsByTag("p").first();
			}

			listNode.setProperty("title", title.text());
			listNode.setProperty("description", description.text());


			if(anchor.size() > 1){
				sb.append(Constants.TILEBORDER_EXTRA_ANCHOR_LINK);
			}

			Element listtext = anchor.first();
			String listurl =listtext.absUrl("href");
			if(StringUtil.isBlank(listurl)){
				listurl = listtext.attr("href");
			}
			listurl = FrameworkUtils.getLocaleReference(listurl, urlMap, locale, sb);
			listNode.setProperty("linktext", listtext.text()+rightListEle.ownText());
			if(!StringUtil.isBlank(listurl)){
				listNode.setProperty("linkurl",listurl);
			}else{
				sb.append(Constants.LINK_URL_NOT_FOUND_IN_LIST);
			}

			log.debug("Updated title, descriptoin and linktext at "+listNode.getPath());
		}
		catch (Exception e) {
			log.error("Exception : ",e);
		}

	}
	//End of Migrate right rail Method

}

