package com.cisco.dse.global.migration.architechture;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;

import org.apache.log4j.Logger;
import org.apache.sling.commons.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.cisco.dse.global.migration.config.BaseAction;
import com.cisco.dse.global.migration.config.Constants;
import com.cisco.dse.global.migration.config.FrameworkUtils;

public class ArchitechtureVariation3 extends BaseAction{

	/**
	 * @param args
	 */

	Document doc = null;

	StringBuilder sb = new StringBuilder(1024);

	Logger log = Logger.getLogger(ArchitechtureVariation3.class);

	public String translate(String host, String loc, String prod, String type,
			String catType, String locale, Session session) throws IOException,
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

			try {
				doc = getConnection(loc);
				log.debug("Connected to the provided URL");
			} catch (Exception e) {
				sb.append(Constants.URL_CONNECTION_EXCEPTION);
			}

			if(doc != null){
				// start set page properties.

				FrameworkUtils.setPageProperties(pageJcrNode, doc, session, sb);

				// end set page properties.

				//Start of List Component
				try {
					migrateListContent(doc,architectureLeftNode );
				}
				catch(Exception e){
					sb.append(Constants.UNABLE_TO_UPDATE_LIST);
				}

				//End of List Component

				// start of text component
				try{
					migrateTextContent(doc, architectureLeftNode, locale);
				}catch(Exception e){
					log.error("exceptionnn"+e);
					sb.append(Constants.EXCEPTION_TEXT_COMPONENT);
				}	
				// end of text component

				//Start of Right Rail
				try{
					migrateRightRailContent(doc,architectureRightNode);
				}
				catch(Exception e)
				{
					sb.append(Constants.UNABLE_TO_MIGRATE_RIGHT_GRID);
				}
				// End of Right Rail
			}else{
				sb.append(Constants.URL_CONNECTION_EXCEPTION);
			}
		}catch(Exception e){
			sb.append(Constants.URL_CONNECTION_EXCEPTION);
		}
		sb.append("</ul></td>");
		session.save();
		log.debug("Msg returned is "+sb.toString());
		return sb.toString();
	}

	// Start of Text Content migraion
	public void migrateTextContent(Document doc, Node architectureLeftNode, String locale) throws RepositoryException{

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

				//Last ul and h2 
				Element secondPilot = textElements.last();
				Elements childs = secondPilot.children();
				Elements tableUlLists = secondPilot.getElementsByTag("table");
				
				String tableUlList = null;
				String lastTag = null;
				if(tableUlLists != null){
					Element tableUlListstring=tableUlLists.last();
					if(tableUlListstring != null){
						tableUlList = tableUlListstring.toString();
					}
				}
				if(childs != null){
					Element lastTagString = childs.last();
					if(lastTagString !=null){
						lastTag = lastTagString.toString();
					}
				}

				Node textNode = null;
				Elements tableUl = null;
				Elements lastH2 = null;
				Elements lastUl = null;

				if(eleSize == nodeSize){

					for(Element ele : textElements){
						textNode = (Node)textNodeIterator.next();
						if((lastTag).equals(tableUlList)){
							tableUl = ele.getElementsByTag("table");
							lastH2 = ele.getElementsByTag("h2");	
							if(tableUl != null && lastH2 != null){
								Element lastTableElement = tableUl.last();
								Element lastH2Element = lastH2.last();
								if(lastTableElement != null && lastH2Element != null){
									lastTableElement.remove();
									lastH2Element.remove();
								}
							}
							textNode.setProperty("text", FrameworkUtils.extractHtmlBlobContent(ele, "", locale, sb));

						}
						else{
							lastUl = ele.getElementsByTag("ul");
							lastH2 = ele.getElementsByTag("h2");
							if(lastUl != null && lastH2 != null){
								Element lastUlElement = lastUl.last();
								Element lastH2Element = lastH2.last();
								if(lastUlElement != null && lastH2Element != null)
								{
									lastUlElement.remove();
									lastH2Element.remove();
								}
							}
							textNode.setProperty("text", FrameworkUtils.extractHtmlBlobContent(ele, "", locale, sb));

						}
					}
				}
					else if(nodeSize < eleSize){
					for(Element ele : textElements){
						textNode = (Node)textNodeIterator.next();
						if((lastTag).equals(tableUlList)){
							tableUl = ele.getElementsByTag("table");
							lastH2 = ele.getElementsByTag("h2");	
							if(tableUl != null && lastH2 != null){
								Element lastTableElement = tableUl.last();
								Element lastH2Element = lastH2.last();
								if(lastTableElement != null && lastH2Element != null){
									lastTableElement.remove();
									lastH2Element.remove();
								}
							}
							textNode.setProperty("text", FrameworkUtils.extractHtmlBlobContent(ele, "", locale, sb));

						}
						else{
							lastUl = ele.getElementsByTag("ul");
							lastH2 = ele.getElementsByTag("h2");
							if(lastUl != null && lastH2 != null){
								Element lastUlElement = lastUl.last();
								Element lastH2Element = lastH2.last();
								if(lastUlElement != null && lastH2Element != null)
								{
									lastUlElement.remove();
									lastH2Element.remove();
								}
							}
							textNode.setProperty("text", FrameworkUtils.extractHtmlBlobContent(ele, "", locale, sb));

						}
					}
				}
				else if(nodeSize > eleSize){
					for(Element ele : textElements){
						textNode = (Node)textNodeIterator.next();
						if((lastTag).equals(tableUlList)){
							tableUl = ele.getElementsByTag("table");
							lastH2 = ele.getElementsByTag("h2");	
							if(tableUl != null && lastH2 != null){
								Element lastTableElement = tableUl.last();
								Element lastH2Element = lastH2.last();
								if(lastTableElement != null && lastH2Element != null){
									lastTableElement.remove();
									lastH2Element.remove();
								}
							}
							textNode.setProperty("text", FrameworkUtils.extractHtmlBlobContent(ele, "", locale, sb));

						}
						else{
							lastUl = ele.getElementsByTag("ul");
							lastH2 = ele.getElementsByTag("h2");
							if(lastUl != null && lastH2 != null){
								Element lastUlElement = lastUl.last();
								Element lastH2Element = lastH2.last();
								if(lastUlElement != null && lastH2Element != null)
								{
									lastUlElement.remove();
									lastH2Element.remove();
								}
							}
							textNode.setProperty("text", FrameworkUtils.extractHtmlBlobContent(ele, "", locale, sb));

						}
					}
				sb.append(Constants.TEXT_NODE_COUNT+nodeSize+Constants.TEXT_ELEMENT_COUNT+eleSize+"</li>");
				}
			}
			else{
				sb.append(Constants.TEXT_NODE_NOT_FOUND);
			}
		}
	}
	//End of Text Content migraion


	// Start of List Content Migration
	private void migrateListContent(Document doc, Node architectureLeftNode) throws RepositoryException {
		Elements secondPilot = doc.select("div.c00-pilot");
		Element lastTag = secondPilot.last().children().last();
		Element h2Ele = secondPilot.last().getElementsByTag("h2").last();
		Element ulEle = secondPilot.last().getElementsByTag("ul").last();
		Elements tableUlLists = secondPilot.last().getElementsByTag("table");
		//Element ulEle = null;


		Node listNodeIterator = architectureLeftNode.hasNode("list") ?architectureLeftNode.getNode("list"):null;
		if(listNodeIterator != null){
			//setting h2 Content
			if(h2Ele != null){
				log.debug("h2 of list" + listNodeIterator.hasProperty("title"));
				listNodeIterator.setProperty("title", h2Ele.html());
			}else{
				sb.append(Constants.NO_H2_ELEMENT_IN_LIST);
			}

			//setting ul Content
			Node elementNode = listNodeIterator.hasNode("element_list_0") ?listNodeIterator.getNode("element_list_0"):null;
			if(elementNode != null){
				Elements liEles = null;
				if((lastTag.toString()).equals(ulEle.toString())){
					//	ulEle = ulEles.last();
					liEles = ulEle.getElementsByTag("li");
					setListContentToNodes(liEles , elementNode);
				}
				else{
					if(tableUlLists != null){
						Element tableUllist = tableUlLists.last();
						if((lastTag.toString()).equals(tableUllist.toString())){
							liEles = tableUllist.getElementsByTag("li");
							setListContentToNodes(liEles , elementNode);
						}
					}
				}
			}
			else {
				sb.append(Constants.LEFT_GRID_ELEMENT_LIST_NODE_NOT_FOUND);
			}
		}
		else {
			sb.append(Constants.LEFT_GRID_LIST_NODE_NOT_FOUND);
		}
	}
	// End of List Content Migration

	//Start  of Setting List Content
	private void setListContentToNodes(Elements liList, Node elementNode) {
		try{
			List<String> listAdd = new ArrayList<String>();
			String icon = null;
			String size = null;
			boolean openNewWindow = false;

			for(Element li : liList){
				icon = "none";
				size = "";

				//pdf content
				String pdf = li.ownText().trim();
				if(pdf != null){		
					openNewWindow = true;
				}else{				
					openNewWindow = false;}


				Elements aEle = li.getElementsByTag("a");
				for(Element a : aEle){
					JSONObject obj = new JSONObject();
					obj.put("linktext", a.text());
					obj.put("linkurl",a.attr("href"));
					obj.put("icon",icon);
					obj.put("size",size);
					obj.put("description","");
					obj.put("openInNewWindow",openNewWindow);
					listAdd.add(obj.toString());
				}
			}
			elementNode.setProperty("listitems", listAdd.toArray(new String[listAdd.size()]));
		}catch(Exception e){
			sb.append(Constants.UNABLE_TO_UPDATE_LIST);
		}

	}
	//End of Setting List Content

	//Start of Right rail migration
	private void migrateRightRailContent(Document doc, Node architectureRightNode) {
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
					log.debug("No right rail elements found with div class name.");
					sb.append(Constants.LIST_NOT_FOUND_IN_RIGHT_RAIL);
				}
				else {
					int eleSize = rightRailList.size();
					NodeIterator tileIterator = architectureRightNode.hasNode("tile_bordered") ? architectureRightNode.getNodes("tile_bordered*") : null;
					if(tileIterator != null){
						int nodeSize = (int)tileIterator.getSize();
						Node listNode = null;

						if(eleSize == nodeSize){
							for (Element rightListEle : rightRailList) {
								if (tileIterator.hasNext()) {
									listNode = (Node)tileIterator.next();
									setRightRailContent(listNode, rightListEle);
								}
								else {
									log.debug("Next node not found");								
								}

							}
						}
						else if (eleSize > nodeSize) {
							for (Element rightListEle : rightRailList) {
								if (tileIterator.hasNext()) {
									listNode = (Node)tileIterator.next();
									setRightRailContent(listNode, rightListEle);						}
								else {
									log.debug("Next node not found");
									sb.append(Constants.RIGHT_RAIL_NODE_COUNT+nodeSize+Constants.RIGHT_RAIL_ELEMENT_COUNT+eleSize+"</li>");
									break;								
								}

							}
						}
						else if (eleSize < nodeSize) {
							for (Element rightListEle : rightRailList) {
								if (tileIterator.hasNext()) {
									listNode = (Node)tileIterator.next();
									setRightRailContent(listNode, rightListEle);						}
								else {
									log.debug("Next node not found");
								}
							}
							sb.append(Constants.RIGHT_RAIL_NODE_COUNT+nodeSize+Constants.RIGHT_RAIL_ELEMENT_COUNT+eleSize+"</li>");
						}
					}
				}
			}

		} catch (Exception e) {
			sb.append(Constants.UNABLE_TO_MIGRATE_RIGHT_GRID);
		}
	}
	//End of right rail migration

	//Start of setting Right rail Content
	public void setRightRailContent (Node listNode, Element rightListEle) {
		try {
			Element title;
			Element description;
			Elements anchor = rightListEle.getElementsByTag("a");
			Elements headElements = rightListEle.getElementsByTag("h3");

			if (headElements.size() > 1) {
				title = rightListEle.getElementsByTag("h3").last();
				description = rightListEle.getElementsByTag("p").last();
			}
			else {
				title = rightListEle.getElementsByTag("h3").first();
				description = rightListEle.getElementsByTag("p").first();
			}

			listNode.setProperty("title", title.text());
			listNode.setProperty("description", description.html());

			Element listtext = anchor.first();
			Element listurl =anchor.first();			
			if(listNode.getProperty("linktrigger").getValue().equals("none")){
				sb.append(Constants.LINK_IS_DISABLED_IN_RIGHT_RAIL);
			}else{
				listNode.setProperty("linktext", listtext.text());
				listNode.setProperty("linkurl",listurl.attr("href"));
			}
			log.debug("Updated title, descriptoin and linktext at "+listNode.getPath());
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
	//End  of setting Right rail Content
}

