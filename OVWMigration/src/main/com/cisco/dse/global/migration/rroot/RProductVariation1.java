package com.cisco.dse.global.migration.rroot;


import java.io.IOException;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.jsoup.helper.StringUtil;
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
			String catType, String locale, Session session, Map<String, String> urlMap) throws IOException,
			ValueFormatException, VersionException, LockException,
			ConstraintViolationException, RepositoryException {
		BasicConfigurator.configure();
		log.debug("In the translate method of ProductIndex");
		log.debug("In the translate method, catType is :" + catType);

		// Repo node paths
		String pagePropertiesPath = "/content/<locale>/" + catType
				+ "/index/jcr:content";
		String indexTopRight = "/content/<locale>/" + catType
				+ "/index/jcr:content/Grid/widenarrow/WN-Narrow-2";
		String indexHero = "/content/<locale>/" + catType
				+ "/index/jcr:content/Grid/widenarrow/WN-Wide-1/carousel/carouselContents";
		String indexThird1 = "/content/<locale>/" + catType
				+ "/index/jcr:content/Grid/thirds/Th-Third-1";
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

		pageUrl = pageUrl.replace("<locale>", locale);
		pagePropertiesPath = pagePropertiesPath.replace("<locale>", locale);
		sb.append("<td>" + "<a href=" + pageUrl + ">" + pageUrl + "</a>"
				+ "</td>");
		sb.append("<td>" + "<a href=" + loc + ">" + loc + "</a>" + "</td>");
		sb.append("<td><ul>");

		indexTopRight = indexTopRight.replace("<locale>", locale);
		indexHero = indexHero.replace("<locale>", locale);
		indexThird1 = indexThird1.replace("<locale>", locale);
		indexThird21 = indexThird21.replace("<locale>", locale);
		indexThird22 = indexThird22.replace("<locale>", locale);
		indexThird31 = indexThird31.replace("<locale>", locale);
		indexThird32 = indexThird32.replace("<locale>", locale);
		indexTwothirdsthird = indexTwothirdsthird.replace("<locale>", locale);
		log.debug("Index : " + indexThird1);
		javax.jcr.Node indexTopRightNode = null;
		javax.jcr.Node indexHeroNode = null;
		javax.jcr.Node indexThirdNode1 = null;
		javax.jcr.Node indexThirdNode21 = null;
		javax.jcr.Node indexThirdNode22 = null;
		javax.jcr.Node indexThirdNode31 = null;
		javax.jcr.Node indexThirdNode32 = null;
		javax.jcr.Node indexTwothirdsthirdNode = null;
		javax.jcr.Node pageJcrNode = null;
		try {
			indexHeroNode = session.getNode(indexHero);
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

				//start of top-gd
				NodeIterator heroNodes = indexHeroNode.hasNode("hero_panel")?indexHeroNode.getNodes("hero_panel*"):null;
				if(heroNodes != null){
					sb.append(Constants.HERO_CONTENT_PANEL_ELEMENT_NOT_FOUND);
				}

				Element htmlBlobEle = doc.select("div.gd12v2-pilot").first();
				if(htmlBlobEle != null){
					sb.append(Constants.HERO_IMAGE_NODE_NOT_FOUND);
				}

				Element topRight = doc.select("div.c23v5-pilot").first();
				if(topRight != null){
					Node htmlBlobNode = indexTopRightNode.hasNode("htmlblob_0")?indexTopRightNode.getNode("htmlblob_0"):null;
					if(htmlBlobNode != null){
						htmlBlobNode.setProperty("html", topRight.outerHtml());
					}else{
						sb.append(Constants.HTMLBLOB_NODE_NOT_FOUND);
					}
				}else{
					sb.append(Constants.HTMLBLOB_ELEMENT_NOT_FOUND);
				}

				if(indexTopRightNode.hasNode("letushelp_eot_partne")){
					sb.append(Constants.PARTNER_HELP_COMPONENT_NOT_FOUND);
				}

				//end of top-gd

				// start of Third Elements

				//start of third1
				Element thirdElement1 = doc.select("div.n04-pilot").first();
				if(thirdElement1 != null){
					Node indexThirdNode11 = indexThirdNode1.hasNode("list_container")?indexThirdNode1.getNode("list_container"):null;
					if(indexThirdNode11 != null){
						//Heading h2
						migrateListHeading(thirdElement1, indexThirdNode11);
						//End of Heading h2

						//List items
						if(indexThirdNode11.hasNode("list_item_parsys")){
							Node list_item_parsys = indexThirdNode11.getNode("list_item_parsys");
							if(list_item_parsys.hasNode("list_content")){
								Node list_content =  list_item_parsys.getNode("list_content");
								migrateListContentT(thirdElement1, list_content,urlMap);
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

				if(indexThirdNode1.hasNode("list_container_0")){
					sb.append(Constants.LIST_COMPONENT_NOT_FOUND);
				}
				//end of third1

				//start of third21
				Element thirdElement21 = doc.select("div.c23v2-pilot").first();
				if(thirdElement21 != null){
					if(thirdElement21.getElementsByTag("img") != null){
						sb.append(Constants.EXTRA_IMAGE_TAG_FOUND);
					}
					if(indexThirdNode21 != null){
						//Heading h2
						migrateListHeading(thirdElement21, indexThirdNode21);
						//End of Heading h2

						//List items
						if(indexThirdNode21.hasNode("list_item_parsys")){
							Node list_item_parsys = indexThirdNode21.getNode("list_item_parsys");
							if(list_item_parsys.hasNode("list_content")){
								Node list_content =  list_item_parsys.getNode("list_content");
								migrateListContent(thirdElement21,list_content,urlMap);
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
								migrateListContent(thirdElement22,list_content,urlMap);
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
								migrateListContent(thirdElement31,list_content,urlMap);
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
								migrateListContent(thirdElement32, list_content,urlMap);
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
								String htmlWEBContent = FrameworkUtils.extractHtmlBlobContent(htmlBlobs.get(i), "", locale, sb, urlMap)+FrameworkUtils.extractHtmlBlobContent(htmlBlobs.get(i+1), "", locale, sb, urlMap);
								String htmlWEMContent = htmlBlob.hasProperty("html")?htmlBlob.getProperty("html").getString():"";
								if(StringUtils.isNotBlank(htmlWEMContent)){
									if(StringUtils.isNotBlank(htmlWEBContent)){
										htmlWEBContent = FrameworkUtils.UpdateHyperLinksInHtml(htmlWEBContent, htmlWEMContent, doc, sb);//first parameter is source and second is target.
									}else{
										log.debug("html Content in WEB is blank.");
									}
								}else{
									log.debug("html Content in WEM is blank at : "+htmlBlob.getPath());
								}
								htmlBlob.setProperty("html", htmlWEBContent);
							}else{
								sb.append(Constants.HTMLBLOB_NODE_NOT_FOUND);
							}
							i+=2;
							if(twoThird.hasNode("list_container")){
								sb.append(Constants.LIST_ELEMENT_NOT_FOUND);
							}
						}
					}else{
						sb.append(Constants.HTMLBLOB_NODE_NOT_FOUND);
					}
				}else{
					sb.append(Constants.HTMLBLOB_ELEMENT_NOT_FOUND);
				}

				Elements htmlBlobCheck = doc.select("div.gd13v2-pilot");
				if(htmlBlobCheck != null){
					int mismatchCount = htmlBlobCheck.size()-3;
					if(mismatchCount > 0 ){
						sb.append("<li>"+mismatchCount+Constants.EXTRA_HTMLBLOB_ELEMENT_FOUND);
					}
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
	private void migrateListContent(Element thirdElement, Node list_content, Map<String, String> urlMap) throws PathNotFoundException, RepositoryException {
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
							setThirdLinks(cqAnchor,items, anchorText,urlMap);
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
								setThirdLinks(cqAnchor,items, anchorText,urlMap);
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
							setThirdLinks(cqAnchor,items, anchorText,urlMap);
						}
					}
					sb.append(Constants.MISMATCH_IN_LIST_NODES+eleSize+Constants.LIST_NODES_COUNT+nodeSize+".</li>");
				}
			}
		}
	}

	//Migrate List Third1 Content Method
	private void migrateListContentT(Element thirdElement, Node list_content, Map<String, String> urlMap) throws PathNotFoundException, RepositoryException {
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
							setThirdLinks(cqAnchor,items, anchorText,urlMap);
						}
						if(ele.child(0).hasAttr("href")){
							log.debug("Extra image link url : "+ele.child(0).attr("href"));
							sb.append(Constants.IMAGE_LINK_NODE_NOT_FOUND+ anchorText +"'.</li>");
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
								setThirdLinks(cqAnchor,items, anchorText,urlMap);
							}
							if(ele.child(0).hasAttr("href")){
								log.debug("Extra image link url : "+ele.child(0).attr("href"));
								sb.append(Constants.IMAGE_LINK_NODE_NOT_FOUND+ anchorText +"'.</li>");
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
							setThirdLinks(cqAnchor,items, anchorText,urlMap);
						}
						if(ele.child(0).hasAttr("href")){
							log.debug("Extra image link url : "+ele.child(0).attr("href"));
							sb.append(Constants.IMAGE_LINK_NODE_NOT_FOUND+ anchorText +"'.</li>");
						}
					}
					sb.append(Constants.MISMATCH_IN_LIST_NODES+eleSize+Constants.LIST_NODES_COUNT+nodeSize+".</li>");
				}
			}
		}
	}

	//Set ThirdLinks method
	private void setThirdLinks(Element cqAnchor, NodeIterator items,String anchorText, Map<String, String> urlMap) throws PathNotFoundException, RepositoryException {
		String anchorHref  = cqAnchor.absUrl("href");
		if(StringUtil.isBlank(anchorHref)){
			anchorHref = cqAnchor.attr("href");
		}
		anchorHref = FrameworkUtils.getLocaleReference(anchorHref, urlMap);
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
