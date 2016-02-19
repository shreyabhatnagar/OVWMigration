/* 
 * S.No		Name	Date		Description of change
 * 1		kiran   13-jan-16	Added the Java file to handle the migration of web about pages.
 * 
 * */

package com.cisco.dse.global.migration.web;

import java.io.IOException;
import java.util.Map;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.cisco.dse.global.migration.config.BaseAction;
import com.cisco.dse.global.migration.config.Constants;
import com.cisco.dse.global.migration.config.FrameworkUtils;

public class WebVariation12 extends BaseAction {
	Document doc;
	StringBuilder sb = new StringBuilder(1024);
	static Logger log = Logger.getLogger(WebVariation12.class);
	
	int noImageCount = 0;
	
	public String translate(String host, String loc, String prod, String type,
			String catType, String locale, Session session, Map<String, String> urlMap) throws IOException,
			ValueFormatException, VersionException, LockException,
			ConstraintViolationException, RepositoryException {
		log.debug("In the translate method of WebVariation12");
		log.debug("In the translate method, catType is :" + catType);
		
		String pagePropertiesPath = "/content/<locale>/"+catType+"/<prod>/iot-products";
		String pageUrl = host + "/content/<locale>/"+catType+"/<prod>/iot-products/";
		if(type.equals("services-iotproductsvar12")){
			log.debug("services-iotproductsvar12");
			pagePropertiesPath = pagePropertiesPath+"/services/jcr:content/";
			pageUrl = pageUrl +"services.html";
		}else if(type.equals("solutions-iotproductsvar12")){
			log.debug("solutions-iotproductsvar12");
			pagePropertiesPath = pagePropertiesPath+"/solutions/jcr:content/";
			pageUrl = pageUrl +"solutions.html";
		}
		pageUrl = pageUrl.replace("<locale>", locale).replace("<prod>", prod);
		pagePropertiesPath = pagePropertiesPath.replace("<locale>", locale).replace("<prod>", prod);
		String webNodePath = pagePropertiesPath+"content_parsys/solutions/layout-solutions/gd22v2";

		sb.append("<td>" + "<a href="+pageUrl+">"+pageUrl+"</a>"+"</td>");
		sb.append("<td>" + "<a href="+loc+">"+loc +"</a>"+ "</td>");
		sb.append("<td><ul>");
		webNodePath = webNodePath.replace("<locale>", locale).replace("<prod>", prod);
		
		javax.jcr.Node webNode = null;
		javax.jcr.Node pageJcrNode = null;
		try {

			webNode = session.getNode(webNodePath);
			pageJcrNode = session.getNode(pagePropertiesPath);
			try {
				doc = getConnection(loc);
			} catch (Exception e) {
				log.error("Exception ", e);
			}
			if (doc != null) {
				// ------------------------------------------------------------------------------------------------------------------------------------------
				// start set page properties.

				FrameworkUtils.setPageProperties(pageJcrNode, doc, session, sb);

				// end set page properties.
				// ------------------------------------------------------------------------------------------------------------------------------------------
                // Start of left grid component
				//start of html blob component
				try {
					String htmlBlobContent = "";
					StringBuilder oldImage = new StringBuilder();
					
					log.debug("Started migrating HtmlBlob content.");
					// Start get content.
					Elements htmlBlobElements = doc.select("div.gd-left").select("div.c00-pilot,div.cc00-pilot,div.c11-pilot");
					if (htmlBlobElements != null) {
						for(Element ele :htmlBlobElements ){
							htmlBlobContent = FrameworkUtils.extractHtmlBlobContent(ele, "", locale, sb, urlMap);
							oldImage.append(htmlBlobContent);
						}
					}
				 else {
					sb.append(Constants.HTMLBLOB_ELEMENT_NOT_FOUND);
				}
					//End of getContent
					//Start of set content
					if (webNode.hasNode("gd22v2-left/htmlblob")) {
						Node htmlBlobNode = webNode.getNode("gd22v2-left/htmlblob");
						if (!StringUtils.isEmpty(htmlBlobContent)) {
							htmlBlobNode.setProperty("html",oldImage.toString());
							log.debug("HtmlBlob Content migrated is done.");
						}
					} else {
						sb.append(Constants.HTMLBLOB_NODE_NOT_FOUND);
					}
				}	
				catch (Exception e) {
					sb.append(Constants.EXCEPTION_IN_HTMLBLOB);
					log.error("Exception " , e);
				}
				

				// End get content.
			//End of htmlblob Component
			//End of left grid component
			//------------------------------------------------------------------------------------------------------------------------------
				//start of right grid component
				//start of html blob
				//-------------------------------------------------------------------------------------------------------------------------
				try {
					String htmlBlobContent = "";
					StringBuilder oldImage = new StringBuilder();
					
					log.debug("Started migrating HtmlBlob content.");
					// Start get content.
					Element htmlBlobElements = doc.select("div.gd-right").last();
					if (htmlBlobElements != null) {
							htmlBlobContent = FrameworkUtils.extractHtmlBlobContent(htmlBlobElements, "", locale, sb, urlMap);
							
							oldImage.append(htmlBlobContent);
					}
					//End of getContent
					//Start of set content
					if (webNode.hasNode("gd22v2-right/htmlblob")) {
						Node htmlBlobNode = webNode.getNode("gd22v2-right/htmlblob");
						if (!StringUtils.isEmpty(htmlBlobContent)) {
							//String html =oldImage.toString().replaceAll("<br>", "");
							htmlBlobNode.setProperty("html",oldImage.toString());
							log.debug("HtmlBlob Content migrated is done.");
						}
					} else {
						sb.append(Constants.HTMLBLOB_NODE_NOT_FOUND);
					}
				}
				catch (Exception e) {
					sb.append(Constants.EXCEPTION_IN_HTMLBLOB);
					log.error("Exception " , e);
				}
				// end htmlblob component.
				
				session.save();
			} else {
				sb.append(Constants.URL_CONNECTION_EXCEPTION);
			}
		} catch (Exception e) {
			sb.append(Constants.UNABLE_TO_MIGRATE_PAGE);
			log.debug("Exception as url cannot be connected: " + e);
			log.error("Exception " , e);
		}

		sb.append("</ul></td>");

		return sb.toString();

	}

}
