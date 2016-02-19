package com.cisco.dse.global.migration.web;

/* S.No			Name		Date		Description of change
 * 1			Bhavya		28-Dec-15	Added the Java file to handle the migration of benifits variation 3 with 3url.
 * 
 * */
import java.io.IOException;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;

import org.apache.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.cisco.dse.global.migration.config.BaseAction;
import com.cisco.dse.global.migration.config.Constants;
import com.cisco.dse.global.migration.config.FrameworkUtils;

public class WebVariation11 extends BaseAction{


	Document doc = null;

	StringBuilder sb = new StringBuilder(1024);

	Logger log = Logger.getLogger(WebVariation11.class);

	public String translate(String host, String loc, String prod, String type,
			String catType, String locale, Session session,  Map<String, String> urlMap) throws IOException,
			ValueFormatException, VersionException, LockException,
			ConstraintViolationException, RepositoryException {

		log.debug("In the translate method WebVariation11");
		log.debug("In the translate method, catType is :" + catType);
		prod = "order-services";
		String pagePropertiesPath = "/content/<locale>/" + catType
				+ "/<prod>/jcr:content";
		String orderServicesLeft = "/content/<locale>/"
				+ catType
				+ "/<prod>/jcr:content/content_parsys/services/layout-services/gd12v2/gd12v2-left";
		String orderServicesRight = "/content/<locale>/"
				+ catType
				+ "/<prod>/jcr:content/content_parsys/services/layout-services/gd12v2/gd12v2-right";


		String pageUrl = host + "/content/<locale>/" + catType
				+ "/order-services.html";
		pageUrl = pageUrl.replace("<locale>", locale).replace("<prod>", prod);
		pagePropertiesPath = pagePropertiesPath.replace("<locale>", locale)
				.replace("<prod>", prod);
		sb.append("<td>" + "<a href=" + pageUrl + ">" + pageUrl + "</a>"
				+ "</td>");
		sb.append("<td>" + "<a href=" + loc + ">" + loc + "</a>" + "</td>");
		sb.append("<td><ul>");

		orderServicesLeft = orderServicesLeft.replace("<locale>", locale).replace("<prod>",prod);
		orderServicesRight = orderServicesRight.replace("<locale>", locale).replace("<prod>",prod);

		javax.jcr.Node orderServicesLeftNode = null;
		javax.jcr.Node orderServicesRightNode = null;

		javax.jcr.Node pageJcrNode = null;
		try {
			orderServicesLeftNode = session.getNode(orderServicesLeft);
			orderServicesRightNode = session.getNode(orderServicesRight);
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

				// Start of migration of header
				try {
					migrateHeaderHtmlBlob(doc, orderServicesLeftNode,locale, urlMap);
				} catch (Exception e) {
					sb.append(Constants.EXCEPTION_TEXT_COMPONENT);
					log.error("Exception : ", e);
				}
				// End of migration of header

				// Start of migration of grids
				try {
					migrateGrids(doc, orderServicesLeftNode,locale, urlMap);
				} catch (Exception e) {
					sb.append(Constants.EXCEPTION_TEXT_COMPONENT);
					log.error("Exception : ", e);
				}
				// End of migration of grids

				// Start of migration of bottom Grid
				try {
					migrateBottomGrid(doc, orderServicesLeftNode,locale, urlMap);
				} catch (Exception e) {
					sb.append(Constants.EXCEPTION_TEXT_COMPONENT);
					log.error("Exception : ", e);
				}
				// End of migration of bottom Grid

				// Start of migration of right panel
				try {
					migrateRightPanel(doc, orderServicesRightNode,locale, urlMap);
				} catch (Exception e) {
					sb.append(Constants.EXCEPTION_TEXT_COMPONENT);
					log.error("Exception : ", e);
				}
				// End of migration of right panel

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


	private void migrateHeaderHtmlBlob(Document doc,Node orderServicesLeftNode, String locale,Map<String, String> urlMap) throws PathNotFoundException, RepositoryException {
		Elements textElements = doc.select("div.c00-pilot");
		Node htmlBlob = orderServicesLeftNode.hasNode("htmlblob") ? orderServicesLeftNode.getNode("htmlblob") : null;

		if(htmlBlob != null){
			if(!textElements.isEmpty()){
				Element textElement = textElements.first();
				if(textElement != null){
						String html = FrameworkUtils.extractHtmlBlobContent(textElement, "",locale, sb, urlMap);
						htmlBlob.setProperty("html", html);
				}
			} else {
				log.debug("header pilot is not available");
				sb.append(Constants.HEADER_ELEMENT_NOT_FOUND);
			}
		} else {
			if(!textElements.isEmpty()){
				log.debug("header htmlblob is not found");
				sb.append(Constants.HEADER_NODE_NOT_FOUND);
			} else {
				log.debug("header and node are not found");
			}
		}

	}

	private void migrateGrids(Document doc, Node orderServicesLeftNode,String locale, Map<String, String> urlMap) throws PathNotFoundException, RepositoryException {
		Elements grids = doc.select("div.gd23-pilot");
		Node gd23v1 = orderServicesLeftNode.hasNode("gd23v1") ? orderServicesLeftNode.getNode("gd23v1") : null ;
		if(gd23v1 != null) {

			// Start of left grid
			Node gd23v1left = gd23v1.hasNode("gd23v1-left") ? gd23v1.getNode("gd23v1-left") : null;
			Elements leftGrid = grids.select("div.gd-left");
			if(gd23v1left != null){
				Node htmlBlob = gd23v1left.hasNode("htmlblob") ? gd23v1left.getNode("htmlblob") : null;
				if(htmlBlob != null){
					if(!leftGrid.isEmpty()){
						String html = FrameworkUtils.extractHtmlBlobContent(leftGrid.first(), "",locale, sb, urlMap);
						htmlBlob.setProperty("html", html);
					}else {
						sb.append(Constants.LEFT_GRID_ELEMENT_NOT_FOUND);
					}
				}else {
					if(!leftGrid.isEmpty()){
						log.debug("grids are not found");
						sb.append(Constants.HTMLBLOB_NODE_DOES_NOT_EXIST);
					}else {
						log.debug("leftGrid and leftnodes are not found");
					}
				}
			}else {
				if(!leftGrid.isEmpty()){
					log.debug("grids are not found");
					sb.append(Constants.LEFT_GRID_NODE_NOT_FOUND);
				}else {
					log.debug("leftGrid and leftnodes are not found");
				}
			}
			// End of left grid

			// Start of mid grid
			Node gd23v1mid = gd23v1.hasNode("gd23v1-mid") ? gd23v1.getNode("gd23v1-mid") : null;
			Elements midGrid = grids.select("div.gd-mid");
			if(gd23v1mid != null){
				Node htmlBlob = gd23v1mid.hasNode("htmlblob") ? gd23v1mid.getNode("htmlblob") : null;
				if(htmlBlob != null){
					if(!midGrid.isEmpty()){
						String html = FrameworkUtils.extractHtmlBlobContent(midGrid.first(), "",locale, sb, urlMap);
						htmlBlob.setProperty("html", html);
					}else {
						sb.append(Constants.MID_GRID_ELEMENT_NOT_FOUND);
					}
				}else {
					if(!midGrid.isEmpty()){
						log.debug("grids are not found");
						sb.append(Constants.HTMLBLOB_NODE_DOES_NOT_EXIST);
					}else {
						log.debug("midGrid and leftnodes are not found");
					}
				}
			}else {
				if(!midGrid.isEmpty()){
					log.debug("grids are not found");
					sb.append(Constants.MID_GRID_NODE_NOT_FOUND);
				}else {
					log.debug("midGrid and leftnodes are not found");
				}
			}
			// End of mid grid

			// Start of right grid
			Node gd23v1right = gd23v1.hasNode("gd23v1-right") ? gd23v1.getNode("gd23v1-right") : null;
			Elements rightGrid = grids.select("div.gd-right");
			if(gd23v1right != null){
				Node htmlBlob = gd23v1right.hasNode("htmlblob") ? gd23v1right.getNode("htmlblob") : null;
				if(htmlBlob != null){
					if(!rightGrid.isEmpty()){
						String html = FrameworkUtils.extractHtmlBlobContent(rightGrid.first(), "",locale, sb, urlMap);
						htmlBlob.setProperty("html", html);
					}else {
						sb.append(Constants.RIGHT_GRID_ELEMENT_NOT_FOUND);
					}
				}else {
					if(!rightGrid.isEmpty()){
						log.debug("rightGrid are not found");
						sb.append(Constants.HTMLBLOB_NODE_NOT_FOUND);
					}else {
						log.debug("rightGrid and leftnodes are not found");
					}
				}
			}else {
				if(!midGrid.isEmpty()){
					log.debug("rightGrid are not found");
					sb.append("rightGrid node not found");
				}else {
					log.debug(Constants.RIGHT_GRID_NODE_NOT_FOUND);
				}
			}
			// End of right grid

		} else {
			if(!grids.isEmpty()){
				log.debug("grids are not found");
				sb.append("grid node not found");
			}else {
				log.debug("grids and nodes are not found");
			}
		}
	}

	private void migrateBottomGrid(Document doc, Node orderServicesLeftNode,String locale, Map<String, String> urlMap) throws PathNotFoundException, RepositoryException {
		Elements grids = doc.select("div.gd21-pilot,div.n05v16");
		Node gd23v1 = orderServicesLeftNode.hasNode("gd21v1") ? orderServicesLeftNode.getNode("gd21v1") : null ;
		if(grids.isEmpty()){
		grids = doc.select("div.n05v16");
		}
		if(gd23v1 != null){
			if(!grids.isEmpty()){
				// Start of mid grid
				Node gd23v1mid = gd23v1.hasNode("gd21v1-mid") ? gd23v1.getNode("gd21v1-mid") : null;
				Elements midGrid = grids.select("div.gd-mid");
				Elements midGridJp = null;
				if(midGrid.isEmpty()){
					midGridJp = doc.select("div.n05v16");
				}
				if(gd23v1mid != null){
					Node htmlBlob = gd23v1mid.hasNode("htmlblob") ? gd23v1mid.getNode("htmlblob") : null;
					if(htmlBlob != null){
						if(!midGrid.isEmpty()){
							String html = FrameworkUtils.extractHtmlBlobContent(midGrid.first(), "",locale, sb, urlMap);
							htmlBlob.setProperty("html", html);
						}else if(!midGridJp.isEmpty()) {
							Element midGridEle = midGridJp.first();
							log.debug("before loop "+ midGridEle);
							for(Element ele : midGridJp){
								if(!ele.equals(midGridEle)){
								midGridEle.append(ele.html());
								}
							}
							log.debug("after loop "+ midGridEle);
							String html = FrameworkUtils.extractHtmlBlobContent(midGridEle, "",locale, sb, urlMap);
							htmlBlob.setProperty("html", html);
						}else {
							sb.append("<li>Bottom Grid Element not available</li>");
						}
					}else {
						if(!midGrid.isEmpty()){
							log.debug("grids are not found");
							sb.append("htmlblob node not found");
						}else {
							log.debug("midGrid and leftnodes are not found");
						}
					}

				}else {
					if(!midGrid.isEmpty()){
						log.debug("grids are not found");
						sb.append("midGrid node not found");
					}else {
						log.debug("midGrid and leftnodes are not found");
					}
				}
				// End of mid grid
			}else {
				sb.append("<li>Bottom Grid Element not available</li>");
			}
		} else {
			if(!grids.isEmpty()){
				log.debug("grids are not found");
				sb.append("grid node not found");
			}else {
				log.debug("grids and nodes are not found");
			}
		}
	}

	private void migrateRightPanel(Document doc, Node orderServicesRightNode,String locale, Map<String, String> urlMap) throws PathNotFoundException, RepositoryException {

//		Elements textElements = doc.select("div.fw-cisco-assistant,div.s01v8-pilot"); fix-Anudeep
		Elements textElements = doc.select("div.contact_us");
		Node htmlBlob = orderServicesRightNode.hasNode("htmlblob") ? orderServicesRightNode.getNode("htmlblob") : null;
		
		if(textElements.isEmpty()){
//			textElements = doc.select("div.s01-pilot"); fix-Anudeep
			textElements = doc.select("div#framework-content-right");
		}
		if(textElements.isEmpty()){
			textElements = doc.select("div.fw-cisco-assistant,div.s01v8-pilot");
		}
		if(textElements.isEmpty()){
			textElements = doc.select("div.s01-pilot");
		}
		boolean fHolderExists = false;
		if(htmlBlob != null){
			if(!textElements.isEmpty()){
				Element textElement = textElements.first();
				/*if(textElement != null){
					Elements childelements = textElement.children(); fix-Anudeep
					for(Element childElement : childelements){
						if(childElement.hasClass("local-f-holder_a")){
							fHolderExists =  true;
						}
						
					}
					if(!fHolderExists){
						textElement = doc.select("div.contcss").first();
						//log.debug("text element: "+ textElement);
					}*/
					if(textElement != null){
						String html = FrameworkUtils.extractHtmlBlobContent(textElement, "",locale, sb, urlMap);
						htmlBlob.setProperty("html", html);
					}
					
					
//				} fix-Anudeep
			} else {
				log.debug("Right panel is not available");
				sb.append(Constants.RIGHT_PANEL_ELEMENT_NOT_FOUND);
			}
		} else {
			if(!textElements.isEmpty()){
				log.debug("header htmlblob is not found");
				sb.append(Constants.RIGHT_PANEL_NODE_NOT_FOUND);
			} else {
				log.debug("right panel and node are not found");
			}
		}

	}

}
