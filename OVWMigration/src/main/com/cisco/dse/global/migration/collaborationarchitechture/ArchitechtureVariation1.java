package com.cisco.dse.global.migration.collaborationarchitechture;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.sling.commons.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.cisco.dse.global.migration.config.BaseAction;
import com.cisco.dse.global.migration.config.Constants;
import com.cisco.dse.global.migration.config.FrameworkUtils;
import com.cisco.dse.global.migration.productlanding.ProductLandingVariation10;

public class ArchitechtureVariation1 extends BaseAction{

	/**
	 * @param args
	 */

	Document doc;

	StringBuilder sb = new StringBuilder(1024);

	Logger log = Logger.getLogger(ArchitechtureVariation1.class);

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

			/*// start set page properties.

			FrameworkUtils.setPageProperties(pageJcrNode, doc, session, sb);

			// end set page properties.
			 */
			try {
				doc = Jsoup.connect(loc).get();
				log.debug("Connected to the provided URL");
			} catch (Exception e) {
				doc = getConnection(loc);
			}

			if(doc != null){

				// start of text component
				try{
					setText(doc, architectureLeftNode);
				}catch(Exception e){
					sb.append(Constants.EXCEPTION_TEXT_COMPONENT);
				}	
				// end of text component

				//Start of List Component
				try {
					listElements(doc,architectureLeftNode,session);
				}
				catch(Exception e){
					sb.append("Exception in List Component");
				}

				//End of List Component

				//Start of Right Rail
				try{
					rightRailElements(doc,architectureRightNode);
				}
				catch(Exception e)
				{
					sb.append("Exception in right rail Elements");
				}
				// End of Right Rail
			}else{
				sb.append(Constants.URL_CONNECTION_EXCEPTION);
			}
		}catch(Exception e){
			sb.append("<li>unable to migrate page"+e+"</li>");
		}
		sb.append("</ul></td>");
		session.save();
		log.debug("Msg returned is "+sb.toString());
		return sb.toString();
	}

	// Set Text Method
	public void setText(Document doc, Node architectureLeftNode) throws RepositoryException{

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
					log.debug("////////////Inside///////////");
					for(Element ele : textElements){
						Node textNode = (Node)textNodeIterator.next();
						textNode.setProperty("text", ele.html());
					}
				}
				else if(nodeSize < eleSize){
					for(Element ele : textElements){
						if(textNodeIterator.hasNext()){
							Node textNode = (Node)textNodeIterator.next();
							textNode.setProperty("text", ele.html());
						}
						else{
							sb.append(Constants.TEXT_NODE_COUNT+nodeSize+Constants.TEXT_ELEMENT_COUNT+eleSize+"</li>");
						}
					}
				}
				else if(nodeSize > eleSize){
					for(Element ele : textElements){
						Node textNode = (Node)textNodeIterator.next();
						textNode.setProperty("text", ele.html());
					}
					sb.append(Constants.TEXT_NODE_COUNT+nodeSize+Constants.TEXT_ELEMENT_COUNT+eleSize+"</li>");
				}
			}
			else{
				sb.append(Constants.TEXT_NODE_NOT_FOUND);
			}
		}
	}
	//End of Text Method


	// Start of List Elements method
	private void listElements(Document doc, Node architectureLeftNode, Session session) throws RepositoryException {
		Elements listElements = doc.select("div.n13-pilot");

		if(listElements == null || listElements.size() == 0){
			sb.append("<li>List Component not found on locale page. Extra list Node need to be deleted manually </li>");
		}
		else {
			int eleSize = listElements.size();
			NodeIterator listNodeIterator = architectureLeftNode.hasNode("list") ?architectureLeftNode.getNodes("list*"):null;
			if(listNodeIterator != null){
				int nodeSize = (int)listNodeIterator.getSize();
				log.debug("node Size" + nodeSize + "ele Size" + eleSize);
				if(eleSize == nodeSize){
					for(Element ele : listElements){
						Node listNode = (Node)listNodeIterator.next();
						setListElements(ele , listNode ,session);
					}
				}
				else if(nodeSize < eleSize){
					for(Element ele : listElements){
						if(listNodeIterator.hasNext()){
							Node listNode = (Node)listNodeIterator.next();
							setListElements(ele , listNode ,session);
						}
					}	
					sb.append("<li>extra element found on locale page</li>");
				}

				else if(nodeSize > eleSize){
					for(Element ele : listElements){
						Node listNode = (Node)listNodeIterator.next();
						setListElements(ele , listNode ,session);
					}
					sb.append("<li>extra node found</li>");
				}
			}

			else {
				sb.append("List node not found");
			}

		}

	}
	//SetList Method
	private void setListElements(Element ele, Node architectureListNode, Session session) {
		try{
			Elements h2Ele = ele.getElementsByTag("h2");
			Elements h3Ele = ele.getElementsByTag("h3");
			Elements ulEle = ele.getElementsByTag("ul");
			//title Node
			if(!h2Ele.isEmpty() && h2Ele != null && h2Ele.size()==1){
				log.debug("h2 of list");
				architectureListNode.setProperty("title", h2Ele.first().html());
			}else{
				sb.append("<li>Mismatch of h2 elements in list.</li>");
			}

			//subtitle Node

			NodeIterator h3NodeIterator = architectureListNode.hasNode("element_subtitle_0") ?architectureListNode.getNodes("element_subtitle*"):null;
			if(h3NodeIterator != null){
				int listNodeSize = (int)h3NodeIterator.getSize();
				if(!h3Ele.isEmpty() && h3Ele != null){
					int h3elesize = h3Ele.size();
					if(h3elesize == listNodeSize){
						for(Element h3 : h3Ele){
							Node h3Node = (Node)h3NodeIterator.next();
							h3Node.setProperty("subtitle",h3.html());
						}
					}
					else if(h3elesize > listNodeSize){
						for(Element h3 : h3Ele){
							if(h3NodeIterator.hasNext()){
								Node h3Node = (Node)h3NodeIterator.next();
								h3Node.setProperty("subtitle",h3.html());
							}
						}
					}
					else if(h3elesize < listNodeSize){
						for(Element h3 : h3Ele){
							Node h3Node = (Node)h3NodeIterator.next();
							h3Node.setProperty("subtitle",h3.html());
						}
					}
				}
			}else {
				if(h3Ele != null && !h3Ele.isEmpty()){
					sb.append("<li>element_subtitle_Nodes are not found</li>");
				}
			}

			// Element List
			NodeIterator ulNodeIterator = architectureListNode.hasNode("element_list_0") ? architectureListNode.getNodes("element_list*") : null;
			if(ulNodeIterator != null){
				for(Element ulItr : ulEle){
					if(ulNodeIterator.hasNext()){
						Node ulnodeList = (Node)ulNodeIterator.next();
						if(ulnodeList.getName().equals("element_list_0")){
							ulnodeList = (Node)ulNodeIterator.next();
						}
						if(!ulItr.hasClass("no-bullets")){
							Elements list = ulItr.getElementsByTag("li");
							System.out.println("--------------"+list.size());
							List<String> listAdd = new ArrayList<String>();
							for (Element li : list){
								String icon = "none";
								String size = "";
								boolean openNewWindow = false;

								//pdf content
								try{
									String pdf = li.ownText().trim();
									System.out.println(pdf);
									if(pdf.length()>0){	
										int i;
										for(i=0;i<pdf.length();i++){
											char c = pdf.charAt(i);												
											boolean b = Character.isDigit(c);
											if(b){
												break;
											} 
										}										
										size = pdf.substring(i,  pdf.length()-1);
										icon = pdf.substring(1, i-2);
										openNewWindow = true;
									}
								}catch(Exception e){
									sb.append("<li>Special Characters in the link. Need to migrate manually</li>");
								}

									Element a = li.getElementsByTag("a").first();
									JSONObject obj = new JSONObject();
									obj.put("linktext", a.text());
									obj.put("linkurl",a.attr("href"));
									obj.put("icon",icon);
									obj.put("size",size);
									obj.put("description","");
									obj.put("openInNewWindow",openNewWindow);
									listAdd.add(obj.toString());
									System.out.println(",,,,,,,,,"+listAdd.toString());
								}
								Property listitems = ulnodeList.getProperty("listitems");
								if (!listitems.isMultiple()) {
									listitems.remove();
									session.save();
								}
								ulnodeList.setProperty("listitems",listAdd.toArray(new String[listAdd.size()]));
							}else{
								sb.append("<li>No node for extra UL content</li>");
							}
						}else{
							sb.append("<li>Mismatch of ul list count</li>");
						}
					}
				if(ulNodeIterator.hasNext()){
					sb.append("<li>Mismatch in count of ul elements on locale page are less than count of nodes</li>");
				}
				}else{
					sb.append("<li>Element_List Node not found</li>");
				}
				// End of Element List
			}catch(Exception e){
				sb.append("<li>Unable to update List Component...</li>");
			}

		}
		//End of Set List Elements Method

		//start right rail properties

		private void rightRailElements(Document doc, Node architectureRightNode) {
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
								sb.append("<li>The HTML structure for list component in right rail on the locale page is different and hence migration needs to be done manually.</li>");
								migrate = false;
							}
						}
					}
				}

				if (migrate) {
					if (rightRailList.isEmpty()) {
						log.debug("No right rail elements found with div class name.");
						sb.append("<li>Right rail component of class name does not exist on locale page.</li>");
					}
					else {
						int eleSize = rightRailList.size();
						NodeIterator tileIterator = architectureRightNode.hasNode("tile_bordered") ? architectureRightNode.getNodes("tile_bordered*") : null;
						if(tileIterator != null){
							int nodeSize = (int)tileIterator.getSize();

							if(eleSize == nodeSize){
								for (Element rightListEle : rightRailList) {
									Node listNode;
									if (tileIterator.hasNext()) {
										listNode = (Node)tileIterator.next();
										setRightRailList(listNode, rightListEle);
									}
									else {
										log.debug("Next node not found");								
									}

								}
							}
							else if (eleSize > nodeSize) {
								for (Element rightListEle : rightRailList) {
									Node listNode;
									if (tileIterator.hasNext()) {
										listNode = (Node)tileIterator.next();
										setRightRailList(listNode, rightListEle);						}
									else {
										log.debug("Next node not found");
										sb.append("<li>Mismatch in the count of list panels. Additional panel(s) found on locale page. Locale page has "+ eleSize +" panels and there are "+ nodeSize +" nodes.</li>");
										break;								
									}

								}
							}
							else if (eleSize < nodeSize) {
								for (Element rightListEle : rightRailList) {
									Node listNode;
									if (tileIterator.hasNext()) {
										listNode = (Node)tileIterator.next();
										setRightRailList(listNode, rightListEle);						}
									else {
										log.debug("Next node not found");
									}
								}
								sb.append("<li>Mismatch in the count of Tile border panels. Additional node(s) found. Locale page has "+ eleSize +" panels and there are "+ nodeSize +" nodes.</li>");
							}
						}
					}
				}

			} catch (Exception e) {
				sb.append("<li>Unable to update benefits tile_bordered component.</li>");
				log.error("Exception : ",e);
			}
		}
		//end of right rail 

		//Start of setting Right rail method
		public void setRightRailList (Node listNode, Element rightListEle) {
			try {
				Element title;
				Element description;
				Elements anchor = rightListEle.getElementsByTag("a");
				Elements headElements = rightListEle.getElementsByTag("h2");

				if (headElements.size() > 1) {
					title = rightListEle.getElementsByTag("h2").last();
					description = rightListEle.getElementsByTag("p").last();
					sb.append("<li>Mismatch in count of list panel component in right rail.</li>");
				}
				else {
					title = rightListEle.getElementsByTag("h2").first();
					description = rightListEle.getElementsByTag("p").first();
				}

				listNode.setProperty("title", title.text());
				listNode.setProperty("description", description.html());


				if(anchor.size() > 1){
					sb.append("Extra link found in the locale page");
				}

				Element listtext = anchor.first();
				Element listurl =anchor.first();

				listNode.setProperty("linktext", listtext.text()+rightListEle.ownText());
				listNode.setProperty("linkurl",listurl.attr("href"));

				log.debug("Updated title, descriptoin and linktext at "+listNode.getPath());
			}
			catch (Exception e) {
				e.printStackTrace();
			}

		}
		//End of Right rail method

	}

