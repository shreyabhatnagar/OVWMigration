package com.cisco.dse.global.migration.architechture;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
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

public class ArchitechtureVariation3 extends BaseAction{

	/**
	 * @param args
	 */

	Document doc;

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
		try {
			architectureLeftNode = session.getNode(architectureLeft);
			architectureRightNode = session.getNode(architectureRight);
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
					System.out.println("exceeeppttionn" + e);
					sb.append(Constants.EXCEPTION_TEXT_COMPONENT);
				}	
				// end of text component

				//Start of List Component
				try {
					listElements(doc,architectureLeftNode );
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

				//Last ul and h2 
				Element secondPilot = textElements.last();
				Element lastTag = secondPilot.children().last();
				Elements lastUlList = secondPilot.getElementsByTag("ul");
				Elements lastH2List = secondPilot.getElementsByTag("h2");
				Elements tableUlList = secondPilot.getElementsByTag("table");

				if(eleSize == nodeSize){
					for(Element ele : textElements){
						System.out.println("equalssssssssss" + (lastTag.toString()).equals(tableUlList.toString()));
						Node textNode = (Node)textNodeIterator.next();
						if((lastTag.toString()).equals(tableUlList.toString())){
							if(tableUlList != null && lastH2List != null){
								String tableUl = tableUlList.last().html().toString();
								String lastH2 = lastH2List.last().toString();	
								textNode.setProperty("text", ele.html().replace(tableUl, "").replace(lastH2, ""));
							}
						}
						else{
							if(lastUlList != null && lastH2List != null){
								String lastUl = lastUlList.last().toString();
								String lastH2 = lastH2List.last().toString();
								textNode.setProperty("text", ele.html().replace(lastUl, "").replace(lastH2, ""));
							}
						}
					}
				}
				else if(nodeSize < eleSize){
					for(Element ele : textElements){
						if(textNodeIterator.hasNext()){
							Node textNode = (Node)textNodeIterator.next();
							if((lastTag.toString()).equals(tableUlList.toString())){
								if(tableUlList != null && lastH2List != null){
									String tableUl = tableUlList.last().html().toString();
									String lastH2 = lastH2List.last().toString();
									textNode.setProperty("text", ele.html().replace(tableUl, "").replace(lastH2, ""));
								}
							}
							else{
								if(lastUlList != null && lastH2List != null){
									String lastUl = lastUlList.last().toString();
									String lastH2 = lastH2List.last().toString();
									textNode.setProperty("text", ele.html().replace(lastUl, "").replace(lastH2, ""));
								}
							}						}
						else{
							sb.append(Constants.TEXT_NODE_COUNT+nodeSize+Constants.TEXT_ELEMENT_COUNT+eleSize+"</li>");
						}
					}
				}
				else if(nodeSize > eleSize){
					for(Element ele : textElements){
						Node textNode = (Node)textNodeIterator.next();
						if((lastTag.toString()).equals(tableUlList.toString())){
							if(tableUlList != null && lastH2List != null){
								String tableUl = tableUlList.last().html().toString();
								String lastH2 = lastH2List.last().toString();
								textNode.setProperty("text", ele.html().replace(tableUl, "").replace(lastH2, ""));
							}
						}
						else{
							if(lastUlList != null && lastH2List != null){
								String lastUl = lastUlList.last().toString();
								String lastH2 = lastH2List.last().toString();
								textNode.setProperty("text", ele.html().replace(lastUl, "").replace(lastH2, ""));
							}
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
	//End of Text Method


	// Start of List Elements method
	private void listElements(Document doc, Node architectureLeftNode) throws RepositoryException {
		Element secondPilot = doc.select("div.c00-pilot").last();

		Element lastTag = secondPilot.children().last();
		Element h2Ele = secondPilot.getElementsByTag("h2").last();
		Element ulEle = secondPilot.getElementsByTag("ul").last();
		Element tableUlList = secondPilot.getElementsByTag("table").last();


		Node listNodeIterator = architectureLeftNode.hasNode("list") ?architectureLeftNode.getNode("list"):null;
		if(listNodeIterator != null){
			//setting h2
			if(h2Ele != null){
				log.debug("h2 of list" + listNodeIterator.hasProperty("title"));
				listNodeIterator.setProperty("title", h2Ele.html());
			}else{
				sb.append("<li>Mismatch of h2 elements in list.</li>");
			}

			//setting ul
			Node elementNode = listNodeIterator.hasNode("element_list_0") ?listNodeIterator.getNode("element_list_0"):null;
			if(elementNode != null){
				Elements liEles = null;
				if((lastTag.toString()).equals(tableUlList.toString())){
					System.out.println("lastttt tag tableeeee");
					liEles = tableUlList.getElementsByTag("li");
				}
				else{
					System.out.println("lastttt tag ulll");
					liEles = ulEle.getElementsByTag("li");	
				}
				setListElements(liEles , elementNode);

			}
			else {
				sb.append("List node not found");
			}
		}
	}


	//SetList Method
	private void setListElements(Elements liList, Node elementNode) {
		try{
			if(elementNode != null){
				List<String> listAdd = new ArrayList<String>();
				for(Element li : liList){

					String icon = "none";
					String size = "";
					boolean openNewWindow = false;

					//pdf content

					try{
						String pdf = li.ownText().trim();
						if(pdf.length()>0){	
							int i;
							for(i=0;i<pdf.length();i++){
								char c = pdf.charAt(i);												
								boolean b = Character.isDigit(c);
								if(b){
									break;
								} 
							}										
							size = pdf.substring(i, pdf.length()-1);
							icon = pdf.substring(1, i-2);
							openNewWindow = true;
						}

					}catch(Exception e){
						sb.append("<li>Special Characters in the link. Need to migrate manually</li>");
					}


					Elements aEle = li.getElementsByTag("a");
					for(Element a : aEle){
						System.out.println("jsonnnnnnnnnnnn"+ size);
						JSONObject obj = new JSONObject();
						obj.put("linktext", a.text());
						obj.put("linkurl",a.attr("href"));
						obj.put("icon",icon);
						obj.put("size",size);
						obj.put("description","");
						obj.put("openInNewWindow",openNewWindow);
						listAdd.add(obj.toString());
						System.out.println("objjjj"+obj);
					}
				}
				elementNode.setProperty("listitems", listAdd.toArray(new String[listAdd.size()]));
				System.out.println("finaleeeeeeeeeeeee" + listAdd);
			}
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
							sb.append("<li>Mismatch in the count of list panels. Additional node(s) found. Locale page has "+ eleSize +" panels and there are "+ nodeSize +" nodes.</li>");
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
			Elements headElements = rightListEle.getElementsByTag("h3");

			if (headElements.size() > 1) {
				title = rightListEle.getElementsByTag("h3").last();
				description = rightListEle.getElementsByTag("p").last();
				sb.append("<li>Mismatch in count of list panel component in right rail.</li>");
			}
			else {
				title = rightListEle.getElementsByTag("h3").first();
				description = rightListEle.getElementsByTag("p").first();
			}

			listNode.setProperty("title", title.text());
			listNode.setProperty("description", description.html());


			if(anchor.size() > 1){
				sb.append("Extra link found in the locale page");
			}

			Element listtext = anchor.first();
			Element listurl =anchor.first();			
			if(listNode.getProperty("linktrigger").getValue().equals("none")){
				sb.append("<li>Link is disabled since link trigger value is none</li>");
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
	//End of Right rail method

}

