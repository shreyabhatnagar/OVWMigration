/* 
 * S.No		Name	Date		Description of change
 * 1		kiran   25-jan-16	Added the Java file to handle the migration of web about pages.
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
import org.jsoup.select.Elements;

import com.cisco.dse.global.migration.config.BaseAction;
import com.cisco.dse.global.migration.config.Constants;
import com.cisco.dse.global.migration.config.FrameworkUtils;

public class WebVariation13 extends BaseAction {
	Document doc;
	StringBuilder sb = new StringBuilder(1024);
	static Logger log = Logger.getLogger(WebVariation13.class);
	
	int noImageCount = 0;
	
	public String translate(String host, String loc, String prod, String type,
			String catType, String locale, Session session, Map<String, String> urlMap) throws IOException,
			ValueFormatException, VersionException, LockException,
			ConstraintViolationException, RepositoryException {
		log.debug("In the translate method of WebVariation13");
		log.debug("In the translate method, catType is :" + catType);
		// start 
		String pagePropertiesPath = "/content/<locale>/" + catType + "/index/jcr:content";
		//end
		String pageUrl = host + "/content/<locale>/"+catType+"/index.html";
		
		pageUrl = pageUrl.replace("<locale>", locale);
		pagePropertiesPath = pagePropertiesPath.replace("<locale>", locale);
		String webNodeTopPath = pagePropertiesPath+"/content_parsys/solutions/layout-solutions/gd11v1";
		String webNodeBottomPath = pagePropertiesPath+"/content_parsys/solutions/layout-solutions/gd12v2";

		sb.append("<td>" + "<a href="+pageUrl+">"+pageUrl+"</a>"+"</td>");
		sb.append("<td>" + "<a href="+loc+">"+loc +"</a>"+ "</td>");
		sb.append("<td><ul>");
		
		javax.jcr.Node webTopNode = null;
		javax.jcr.Node webBottomNode = null;
		javax.jcr.Node pageJcrNode = null;
		try {
			webTopNode = session.getNode(webNodeTopPath);
			webBottomNode = session.getNode(webNodeBottomPath);
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
				//start of top node
				//start of right html blob component
				try {
					String htmlBlobContent = "";
					StringBuilder oldImage = new StringBuilder();
					
					
					log.debug("Started migrating HtmlBlob content.");
					// Start get content.
					
					Elements htmlBlobTitle = doc.select("div#mb-title-nav-bar");
					if (htmlBlobTitle != null && !htmlBlobTitle.isEmpty()) {
						htmlBlobContent = FrameworkUtils.extractHtmlBlobContent(htmlBlobTitle.last(), "", locale, sb, urlMap);
						oldImage.append(htmlBlobContent);
					}
					Elements htmlBlobElements = doc.select("div#location_wrapper");
					if (htmlBlobElements != null && !htmlBlobElements.isEmpty()) {
							htmlBlobContent = FrameworkUtils.extractHtmlBlobContent(htmlBlobElements.first(), "", locale, sb, urlMap);
							oldImage.append(htmlBlobContent);
						}
					
					//End of getContent
					//Start of set content
					if (webTopNode.hasNode("gd11v1-mid/htmlblob")) {
						Node htmlBlobNode = webTopNode.getNode("gd11v1-mid/htmlblob");
						if (StringUtils.isNotBlank(htmlBlobContent)) {
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
				
				
				//--------------------------------------------------------------------------------------
				//start of left html blob component
				try {
					String htmlBlobContent = "";
					
					
					log.debug("Started migrating HtmlBlob content.");
					// Start get content.
					Elements htmlBlobElements = doc.select("div.guest");
					if(htmlBlobElements != null && !htmlBlobElements.isEmpty()){
					htmlBlobContent = FrameworkUtils.extractHtmlBlobContent(htmlBlobElements.first(), "", locale, sb, urlMap);
}
					//End of getContent
					//Start of set content
					if (webBottomNode.hasNode("gd12v2-left/htmlblob")) {
						Node htmlBlobNode = webBottomNode.getNode("gd12v2-left/htmlblob");
						if (StringUtils.isNotBlank(htmlBlobContent)) {
							htmlBlobNode.setProperty("html",htmlBlobContent);
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
			//-------------------------------------------------------------------------------------
				//start of right html blob component
				try {
					String htmlBlobContent = "";
					log.debug("Started migrating HtmlBlob content.");
					Elements htmlBlobElements = doc.select("div.guest");
					Elements htmlBlob4Ele  = htmlBlobElements.select("div#col3");
					if (htmlBlob4Ele != null) {
							htmlBlobContent = FrameworkUtils.extractHtmlBlobContent(htmlBlob4Ele.first(), "", locale, sb, urlMap);
					}
					
					
					//End of getContent
					//Start of set content
					if (webBottomNode.hasNode("gd12v2-right/htmlblob")) {
						Node htmlBlobNode = webBottomNode.getNode("gd12v2-right/htmlblob");
						if (StringUtils.isNotBlank(htmlBlobContent)) {
							htmlBlobNode.setProperty("html",htmlBlobContent);
							log.debug("HtmlBlob Content migrated is done.");
						}
					} else {
						sb.append(Constants.HTMLBLOB_NODE_NOT_FOUND);
					}
				}catch (Exception e) {
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
