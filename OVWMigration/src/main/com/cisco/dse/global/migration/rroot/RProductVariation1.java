package com.cisco.dse.global.migration.rproductindex;


import java.io.IOException;

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


public class RProductVariation1 extends BaseAction {

	Document doc = null;
	StringBuilder sb = new StringBuilder(1024);
	static Logger log = Logger.getLogger(RProductVariation1.class);

	public String translate(String host, String loc, String prod, String type,
			String catType, String locale, Session session) throws IOException,
			ValueFormatException, VersionException, LockException,
			ConstraintViolationException, RepositoryException {
		BasicConfigurator.configure();
		log.debug("In the translate method of ProductIndex");
		log.debug("In the translate method, catType is :" + catType);

		// Repo node paths
		String pagePropertiesPath = "/content/<locale>/" + catType
				+ "/index/jcr:content";
		String indexTopRight = "/content/<locale>/" + catType
				+ "/index/jcr:content/Grid/widenarrow/WN-Narrow-2/htmlblob_0";
		String indexThird1 = "/content/<locale>/" + catType
				+ "/index/jcr:content/Grid/thirds/Th-Third-1/list_container";
		String indexThird21 = "/content/<locale>/" + catType
				+ "/index/jcr:content/Grid/thirds/Th-Third-2/list_container";
		String indexThird22 = "/content/<locale>/" + catType
				+ "/index/jcr:content/Grid/thirds/Th-Third-2/list_container_0";
		String indexThird31 = "/content/<locale>/" + catType
				+ "/index/jcr:content/Grid/thirds/Th-Third-3/list_container";
		String indexThird32 = "/content/<locale>/" + catType
				+ "/index/jcr:content/Grid/thirds/Th-Third-3/list_container_0";
		String indexTwothirdsthird = "/content/<locale>/" + catType
				+ "/index/jcr:content/Grid/twothirdsthird";

		String pageUrl = host + "/content/<locale>/" + catType + "/index.html";

		pageUrl = pageUrl.replace("<locale>", locale).replace("<prod>", prod);
		pagePropertiesPath = pagePropertiesPath.replace("<locale>", locale)
				.replace("<prod>", prod);
		sb.append("<td>" + "<a href=" + pageUrl + ">" + pageUrl + "</a>"
				+ "</td>");
		sb.append("<td>" + "<a href=" + loc + ">" + loc + "</a>" + "</td>");
		sb.append("<td><ul>");

		indexTopRight = indexTopRight.replace("<locale>", locale).replace(
				"<prod>", prod);
		indexThird1 = indexThird1.replace("<locale>", locale).replace(
				"<prod>", prod);
		indexThird21 = indexThird21.replace("<locale>", locale).replace(
				"<prod>", prod);
		indexThird22 = indexThird22.replace("<locale>", locale).replace(
				"<prod>", prod);
		indexThird31 = indexThird31.replace("<locale>", locale).replace(
				"<prod>", prod);
		indexThird32 = indexThird32.replace("<locale>", locale).replace(
				"<prod>", prod);
		indexTwothirdsthird = indexTwothirdsthird.replace("<locale>", locale).replace(
				"<prod>", prod);
		log.debug("Index : " + indexThird1);
		javax.jcr.Node indexTopRightNode = null;
		javax.jcr.Node indexThirdNode1 = null;
		javax.jcr.Node indexThirdNode21 = null;
		javax.jcr.Node indexThirdNode22 = null;
		javax.jcr.Node indexThirdNode31 = null;
		javax.jcr.Node indexThirdNode32 = null;
		javax.jcr.Node indexTwothirdsthirdNode = null;
		javax.jcr.Node pageJcrNode = null;
		try {
			indexTopRightNode = session.getNode(indexTopRight);
			indexThirdNode1 = session.getNode(indexThird1);
			indexThirdNode21 = session.getNode(indexThird21);
			indexThirdNode22 = session.getNode(indexThird22);
			indexThirdNode31 = session.getNode(indexThird31);
			indexThirdNode32 = session.getNode(indexThird32);
			indexTwothirdsthirdNode = session.getNode(indexTwothirdsthird);
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

				//start of top-gd-right

				Element topRight = doc.select("div.c23v5-pilot").first();
				if(topRight != null){
					if(indexTopRightNode != null){
						indexTopRightNode.setProperty("html", topRight.outerHtml());
					}
				}

				//end of top-gd-right

				// start of Third Elements

				//start of third1
				Element thirdElement1 = doc.select("div.n04-pilot").first();
				if(thirdElement1 != null){
					if(indexThirdNode1 != null){
						//Heading h2
						migrateListHeading(thirdElement1, indexThirdNode1);
						//End of Heading h2

						//List items
						if(indexThirdNode1.hasNode("list_item_parsys")){
							Node list_item_parsys = indexThirdNode1.getNode("list_item_parsys");
							if(list_item_parsys.hasNode("list_content")){
								Node list_content =  list_item_parsys.getNode("list_content");
								migrateListContentT(thirdElement1, list_content);
							}else{
								sb.append(Constants.NO_LIST_NODE_FOUND);
							}
						}else{
							sb.append(Constants.NO_LIST_NODE_FOUND);
						}
					}
					else{
						sb.append(Constants.NO_LIST_NODE_FOUND);
					}
				}else{
					sb.append(Constants.LIST_COMPONENT_NOT_FOUND);
				}
				//end of third1

				//start of third21
				Element thirdElement21 = doc.select("div.c23v2-pilot").first();
				if(thirdElement21 != null){
					if(indexThirdNode21 != null){
						//Heading h2
						migrateListHeading(thirdElement21, indexThirdNode21);
						//End of Heading h2

						//List items
						if(indexThirdNode21.hasNode("list_item_parsys")){
							Node list_item_parsys = indexThirdNode21.getNode("list_item_parsys");
							if(list_item_parsys.hasNode("list_content")){
								Node list_content =  list_item_parsys.getNode("list_content");
								migrateListContent(thirdElement21,list_content);
							}else{
								sb.append(Constants.NO_LIST_NODE_FOUND);
							}
						}else{
							sb.append(Constants.NO_LIST_NODE_FOUND);
						}
					}else{
						sb.append(Constants.NO_LIST_NODE_FOUND);
					}
				}else{
					sb.append(Constants.LIST_COMPONENT_NOT_FOUND);
				}
				//end of third21

				//start of third22
				Element thirdElement22 = doc.select("div.list").select("div.section").last();
				if(thirdElement22 != null){
					if(indexThirdNode22 != null){
						//Heading h2
						migrateListHeading(thirdElement22, indexThirdNode22);
						//End of Heading h2

						//List items
						if(indexThirdNode22.hasNode("list_item_parsys")){
							Node list_item_parsys = indexThirdNode22.getNode("list_item_parsys");
							if(list_item_parsys.hasNode("list_content")){
								Node list_content =  list_item_parsys.getNode("list_content");
								migrateListContent(thirdElement22,list_content);
							}
							else{
								sb.append(Constants.NO_LIST_NODE_FOUND);
							}
						}else{
							sb.append(Constants.NO_LIST_NODE_FOUND);
						}
					}else{
						sb.append(Constants.NO_LIST_NODE_FOUND);
					}
				}else{
					sb.append(Constants.LIST_COMPONENT_NOT_FOUND);
				}
				//end of third22

				//start of third31
				Element thirdElement31 = doc.select("div.list").select("div.section").first();
				if(thirdElement31 != null){
					if(indexThirdNode31 != null){
						//Heading h2
						migrateListHeading(thirdElement31, indexThirdNode31);
						//End of Heading h2

						//List items
						if(indexThirdNode31.hasNode("list_item_parsys")){
							Node list_item_parsys = indexThirdNode31.getNode("list_item_parsys");
							if(list_item_parsys.hasNode("list_content_3")){
								Node list_content =  list_item_parsys.getNode("list_content_3");
								migrateListContent(thirdElement31,list_content);
							}
							else{
								sb.append(Constants.NO_LIST_NODE_FOUND);
							}
						}else{
							sb.append(Constants.NO_LIST_NODE_FOUND);
						}
					}else{
						sb.append(Constants.NO_LIST_NODE_FOUND);
					}
				}else{
					sb.append(Constants.LIST_COMPONENT_NOT_FOUND);
				}
				//end of third31

				//start of third32
				Element thirdElement32 = doc.select("div.list").select("div.section").get(1);
				if(thirdElement32 != null){
					if(indexThirdNode32 != null){
						//Heading h2
						migrateListHeading(thirdElement32, indexThirdNode32);
						//End of Heading h2

						//List items
						if(indexThirdNode32.hasNode("list_item_parsys")){
							Node list_item_parsys = indexThirdNode32.getNode("list_item_parsys");
							if(list_item_parsys.hasNode("list_content")){
								Node list_content =  list_item_parsys.getNode("list_content");
								migrateListContent(thirdElement32, list_content);
							}
							else{
								sb.append(Constants.NO_LIST_NODE_FOUND);
							}
						}else{
							sb.append(Constants.NO_LIST_NODE_FOUND);
						}
					}else{
						sb.append(Constants.NO_LIST_NODE_FOUND);
					}
				}else{
					sb.append(Constants.LIST_COMPONENT_NOT_FOUND);
				}
				//end of third32

				//End of Third Elements

				//Start of Two Third Elements
				Elements htmlBlobs = doc.select("div.gd11-pilot,div.gd12-pilot");
				if(htmlBlobs != null){
					NodeIterator twoThirdNodes = indexTwothirdsthirdNode.hasNode("TthTh-TwoThirds-1")?indexTwothirdsthirdNode.getNodes("TthTh-*"):null;
					if(twoThirdNodes != null){
						int i = 3;
						for(int j=0;j<twoThirdNodes.getSize();j++){
							Node twoThird = twoThirdNodes.nextNode();
							Node htmlBlob = twoThird.hasNode("htmlblob")?twoThird.getNode("htmlblob"):null;
							if(htmlBlob != null){
								htmlBlob.setProperty("html", htmlBlobs.get(i).outerHtml()+htmlBlobs.get(i+1).outerHtml());
							}else{
								sb.append(Constants.HTMLBLOB_NODE_NOT_FOUND);
							}
							i+=2;
						}
					}else{
						sb.append(Constants.HTMLBLOB_NODE_NOT_FOUND);
					}
				}else{
					sb.append(Constants.HTMLBLOB_ELEMENT_NOT_FOUND);
				}
				//End of Two Third Elements

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

	//Migrate List Heading Method
	private void migrateListHeading(Element thirdElement, Node indexThirdNode) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException{
		String eleh2 = thirdElement.getElementsByTag("h2").first().text();
		if(eleh2 != null){
			indexThirdNode.setProperty("title", eleh2);
		}else{
			sb.append(Constants.LIST_HEADING_COMPONENT_NOT_FOUND);
		}
	}

	//Migrate List Content Method
	private void migrateListContent(Element thirdElement, Node list_content) throws PathNotFoundException, RepositoryException {
		if(list_content.hasNode("listitems")){
			Node listitems = list_content.getNode("listitems");
			NodeIterator items = listitems.hasNode("item_1")?listitems.getNodes("item_*"):null;
			if(listitems != null){
				int nodeSize = (int)items.getSize();
				Elements listElements = thirdElement.getElementsByTag("ul").first().getElementsByTag("li");
				int eleSize = listElements.size();
				if(nodeSize == eleSize){
					Element cqAnchor;
					String anchorText = "";
					for(Element ele : listElements){
						cqAnchor = ele.getElementsByTag("a").first();
						if(cqAnchor != null){
							anchorText = cqAnchor.text();
							setThirdLinks(cqAnchor,items, anchorText);
						}
					}
				}else if(nodeSize < eleSize){
					Element cqAnchor;
					String anchorText = "";
					for(Element ele : listElements){
						if(items.hasNext()){
							cqAnchor = ele.getElementsByTag("a").first();
							if(cqAnchor != null){
								anchorText = cqAnchor.text();
								setThirdLinks(cqAnchor,items, anchorText);
							}
						}else{
							sb.append(Constants.MISMATCH_IN_LIST_COUNT+eleSize+Constants.LIST_NODES_COUNT+nodeSize+".</li>");
							break;
						}
					}
				}else if(nodeSize > eleSize){
					Element cqAnchor;
					String anchorText = "";
					for(Element ele : listElements){
						cqAnchor = ele.getElementsByTag("a").first();
						if(cqAnchor != null){
							anchorText = cqAnchor.text();
							setThirdLinks(cqAnchor,items, anchorText);
						}
					}
					sb.append(Constants.MISMATCH_IN_LIST_NODES+eleSize+Constants.LIST_NODES_COUNT+nodeSize+".</li>");
				}
			}
		}
	}

	//Migrate List Third1 Content Method
	private void migrateListContentT(Element thirdElement, Node list_content) throws PathNotFoundException, RepositoryException {
		if(list_content.hasNode("listitems")){
			Node listitems = list_content.getNode("listitems");
			NodeIterator items = listitems.hasNode("item_1")?listitems.getNodes("item_*"):null;
			if(items != null){
				int nodeSize = (int)items.getSize();
				Elements listElements = thirdElement.getElementsByClass("panel");
				int eleSize = listElements.size();
				if(nodeSize == eleSize){
					Element cqAnchor;
					String anchorText = "";
					for(Element ele : listElements){
						cqAnchor = ele.getElementsByTag("li").first().getElementsByTag("a").first();
						if(cqAnchor != null){
							anchorText = cqAnchor.text();
							setThirdLinks(cqAnchor,items, anchorText);
						}
						if(ele.child(0).hasAttr("href")){
							log.debug("Extra image link url : "+ele.child(0).attr("href"));
							sb.append("<li>Image Link node not found. Addtional image exist on locale page along with the link '"+ anchorText +"'.</li>");
						}
					}
				}else if(nodeSize < eleSize){
					Element cqAnchor;
					String anchorText = "";
					for(Element ele : listElements){
						if(items.hasNext()){
							cqAnchor = ele.getElementsByTag("li").first().getElementsByTag("a").first();
							if(cqAnchor != null){
								anchorText = cqAnchor.text();
								setThirdLinks(cqAnchor,items, anchorText);
							}
							if(ele.child(0).hasAttr("href")){
								log.debug("Extra image link url : "+ele.child(0).attr("href"));
								sb.append("<li>Image Link node not found. Addtional image exist on locale page along with the link '"+ anchorText +"'.</li>");
							}
						}else{
							sb.append(Constants.MISMATCH_IN_LIST_COUNT+eleSize+Constants.LIST_NODES_COUNT+nodeSize+".</li>");
							break;
						}
					}
				}else if(nodeSize > eleSize){
					Element cqAnchor;
					String anchorText = "";
					for(Element ele : listElements){
						cqAnchor = ele.getElementsByTag("li").first().getElementsByTag("a").first();
						if(cqAnchor != null){
							anchorText = cqAnchor.text();
							setThirdLinks(cqAnchor,items, anchorText);
						}
						if(ele.child(0).hasAttr("href")){
							log.debug("Extra image link url : "+ele.child(0).attr("href"));
							sb.append("<li>Image Link node not found. Addtional image exist on locale page along with the link '"+ anchorText +"'.</li>");
						}
					}
					sb.append(Constants.MISMATCH_IN_LIST_NODES+eleSize+Constants.LIST_NODES_COUNT+nodeSize+".</li>");
				}
			}
		}
	}

	//Set ThirdLinks method
	private void setThirdLinks(Element cqAnchor, NodeIterator items,String anchorText) throws PathNotFoundException, RepositoryException {
		String anchorHref  = cqAnchor.attr("href");
		Node item = items.nextNode();
		Node linkdata = item.hasNode("linkdata")?item.getNode("linkdata"):null;
		if(linkdata != null){
			linkdata.setProperty("linktext", anchorText);
			linkdata.setProperty("url", anchorHref);
		}else{
			sb.append(Constants.LIST_NODE_NOT_FOUND);
		}
	}
}
