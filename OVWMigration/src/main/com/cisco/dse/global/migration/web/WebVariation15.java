/* 
 * S.No		Name	Date		Description of change
 * 1		kiran   20-jan-16	Added the Java file to handle the migration of web about pages.
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
import com.cisco.dse.global.migration.config.BaseAction;
import com.cisco.dse.global.migration.config.Constants;
import com.cisco.dse.global.migration.config.FrameworkUtils;

public class WebVariation15 extends BaseAction{
	Document doc;
	StringBuilder sb = new StringBuilder(1024);
	static Logger log = Logger.getLogger(WebVariation12.class);
	
	int noImageCount = 0;
	
	public String translate(String host, String loc, String prod, String type,
			String catType, String locale, Session session, Map<String, String> urlMap) throws IOException,
			ValueFormatException, VersionException, LockException,
			ConstraintViolationException, RepositoryException {
		log.debug("In the translate method of WebVariation");
		log.debug("In the translate method, catType is :" + catType);

		String pagePropertiesPath = "/content/<locale>/" + catType
				+ "/<prod>/root/jcr:content";
		String pageUrl = host + "/content/<locale>/" + catType
				+ "/<prod>/root.html";
		pagePropertiesPath = pagePropertiesPath.replace("<locale>", locale)
				.replace("<prod>", prod);
		pageUrl = pageUrl.replace("<locale>", locale).replace("<prod>", prod);
		
		sb.append("<td>" + "<a href=" + pageUrl + ">" + pageUrl + "</a>"
				+ "</td>");
		sb.append("<td>" + "<a href=" + loc + ">" + loc + "</a>" + "</td>");
		sb.append("<td><ul>");
		String webTopNodePath = "/content/<locale>/"
				+ catType
				+ "/<prod>/root/jcr:content/content_parsys/overview_alt1/layout-overview-alt1/gd12v2";
		String webMidNodePath = "/content/<locale>/"
				+ catType
				+ "/<prod>/root/jcr:content/content_parsys/overview_alt1/layout-overview-alt1/gd12v2_0";
		String webBottomNodePath = "/content/<locale>/"
				+ catType
				+ "/<prod>/root/jcr:content/content_parsys/overview_alt1/layout-overview-alt1/gd12v2_1";
		
		webTopNodePath = webTopNodePath.replace("<locale>",locale).replace("<prod>", prod);
		webMidNodePath = webMidNodePath.replace("<locale>",locale).replace("<prod>", prod);
		webBottomNodePath = webBottomNodePath.replace("<locale>",locale).replace("<prod>", prod);
		
		javax.jcr.Node webTopNode = null;
		javax.jcr.Node webMidNode = null;
		javax.jcr.Node webBottomNode = null;
		javax.jcr.Node pageJcrNode = null;
		
		try {

			webTopNode = session.getNode(webTopNodePath);
			webMidNode = session.getNode(webMidNodePath);
			webBottomNode = session.getNode(webBottomNodePath);
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
				// start of top node left html blob component
				try {
					String htmlBlobContent = "";
					StringBuilder oldImage = new StringBuilder();
					
					log.debug("Started migrating HtmlBlob content.");
					// Start get content.
					Element htmlBlobElement = doc.select("div.gd21-pilot,gd12-pilot").first();
					if (htmlBlobElement != null) {
						htmlBlobContent = FrameworkUtils.extractHtmlBlobContent(htmlBlobElement, "", locale, sb, urlMap);
						oldImage.append(htmlBlobContent);
					}else {
				sb.append(Constants.HTMLBLOB_ELEMENT_NOT_FOUND);
			}
				//End of getContent
				//Start of set content
				if (webTopNode.hasNode("gd12v2-left/htmlblob")) {
					Node htmlBlobNode = webTopNode.getNode("gd12v2-left/htmlblob");
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
		//End of top node left html blob component
		//-------------------------------------------------------------------------------------
		//start of top node right html blob component
				try {
					String htmlBlobContent = "";
					StringBuilder oldImage = new StringBuilder();
					
					log.debug("Started migrating HtmlBlob content.");
					// Start get content.
					Element htmlBlobElement = doc.select("div#framework-content-right,div.gd-right").first();
					if (htmlBlobElement != null) {
						
							htmlBlobContent = FrameworkUtils.extractHtmlBlobContent(htmlBlobElement, "", locale, sb, urlMap);
							oldImage.append(htmlBlobContent);
							}
					
					//End of getContent
					//Start of set content
					if (webTopNode.hasNode("gd12v2-right/htmlblob")) {
						Node htmlBlobNode = webTopNode.getNode("gd12v2-right/htmlblob");
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
				// end top node right htmlblob component.
				//-----------------------------------------------------------------------------------------------------------------------
				//start of mid node left html blob component.
				try {
					log.debug("Started migrating HtmlBlob content.");
					
				//Start of set content
					Node htmlBlobNode_0 = webMidNode.hasNode("gd12v2-left/htmlblob")?webMidNode.getNode("gd12v2-left/htmlblob"):null;
					if (htmlBlobNode_0 != null) {
						sb.append(Constants.HTMLBLOB_NODE_NOT_FOUND);					
					}
				Node htmlBlobNode_1 = webMidNode.hasNode("gd12v2-left/gd22v2/gd22v2-left/htmlblob")?webMidNode.getNode("gd12v2-left/gd22v2/gd22v2-left/htmlblob"):null;
				if (htmlBlobNode_1 != null) {
					sb.append(Constants.HTMLBLOB_NODE_NOT_FOUND);					
				}
				Node htmlBlobNode_2 = webMidNode.hasNode("gd12v2-left/gd22v2/gd22v2-right/htmlblob")?webMidNode.getNode("gd12v2-left/gd22v2/gd22v2-right/htmlblob"):null;
				if (htmlBlobNode_2 != null) {
					sb.append(Constants.HTMLBLOB_NODE_NOT_FOUND);					
				}
				Node textNode = webMidNode.hasNode("gd12v2-left/gd21v1/gd21v1-mid/text")?webMidNode.getNode("gd12v2-left/gd21v1/gd21v1-mid/text"):null;
				if (textNode != null) {
					sb.append(Constants.TEXT_NODE_NOT_FOUND);					
				}
				Node htmlBlobNode_3 = webMidNode.hasNode("gd12v2-left/gd21v1/gd21v1-mid/gd22v2/gd22v2-left/htmlblob")?webMidNode.getNode("gd12v2-left/gd21v1/gd21v1-mid/gd22v2/gd22v2-left/htmlblob"):null;
				if (htmlBlobNode_3 != null) {
					sb.append(Constants.HTMLBLOB_NODE_NOT_FOUND);					
				}
				Node htmlBlobNode_4 = webMidNode.hasNode("gd12v2-left/gd21v1/gd21v1-mid/gd22v2/gd22v2-right/htmlblob")?webMidNode.getNode("gd12v2-left/gd21v1/gd21v1-mid/gd22v2/gd22v2-right/htmlblob"):null;
				if (htmlBlobNode_4 != null) {
					sb.append(Constants.HTMLBLOB_NODE_NOT_FOUND);					
				}
				}
			catch (Exception e) {
				sb.append(Constants.EXCEPTION_IN_HTMLBLOB);
				log.error("Exception " , e);
			}
			

			// End get content.
		//End of mid node left html blob component
		//---------------------------------------------------------------------------------------------------------------------------------
		//start of mid node right html blob component		
				try {
					log.debug("Started migrating HtmlBlob content.");
					
				//Start of set content
					Node htmlBlobNode = webMidNode.hasNode("gd12v2-right/htmlblob")?webMidNode.getNode("gd12v2-right/htmlblob"):null;
					if (htmlBlobNode != null) {
						sb.append(Constants.HTMLBLOB_NODE_NOT_FOUND);					
					}	
				}
				catch (Exception e) {
					sb.append(Constants.EXCEPTION_IN_HTMLBLOB);
					log.error("Exception " , e);
				}
				// End get content.
				//End of mid node right html blob component
				//----------------------------------------------------------------------------------------------------------------------------
                // start of bottom node html blob component
				try {
					log.debug("Started migrating HtmlBlob content.");
					
				//Start of set content
					Node htmlBlobNode_0 = webBottomNode.hasNode("gd12v2-left/htmlblob")?webBottomNode.getNode("gd12v2-left/htmlblob"):null;
					if (htmlBlobNode_0 != null) {
						sb.append(Constants.HTMLBLOB_NODE_NOT_FOUND);					
					}
					Node htmlBlobNode_1 = webBottomNode.hasNode("gd12v2-left/gd22v2/gd22v2-left/htmlblob")?webBottomNode.getNode("gd12v2-left/gd22v2/gd22v2-left/htmlblob"):null;
					if (htmlBlobNode_1 != null) {
						sb.append(Constants.HTMLBLOB_NODE_NOT_FOUND);					
					}
					Node htmlBlobNode_2 = webBottomNode.hasNode("gd12v2-left/gd22v2/gd22v2-right/htmlblob")?webBottomNode.getNode("gd12v2-left/gd22v2/gd22v2-right/htmlblob"):null;
					if (htmlBlobNode_2 != null) {
						sb.append(Constants.HTMLBLOB_NODE_NOT_FOUND);					
					}
					Node htmlBlobNode_3 = webBottomNode.hasNode("gd12v2-left/gd21v1/gd21v1-mid/htmlblob")?webBottomNode.getNode("gd12v2-left/gd21v1/gd21v1-mid/htmlblob"):null;
					if (htmlBlobNode_3 != null) {
						sb.append(Constants.HTMLBLOB_NODE_NOT_FOUND);					
					}
					Node htmlBlobNode_4 = webBottomNode.hasNode("gd12v2-left/gd21v1/gd21v1-mid/gd22v2/gd22v2-left/htmlblob")?webBottomNode.getNode("gd12v2-left/gd21v1/gd21v1-mid/gd22v2/gd22v2-left/htmlblob"):null;
					if (htmlBlobNode_4 != null) {
						sb.append(Constants.HTMLBLOB_NODE_NOT_FOUND);					
					}
					Node htmlBlobNode_5 = webBottomNode.hasNode("gd12v2-left/gd21v1/gd21v1-mid/gd22v2/gd22v2-right/htmlblob")?webBottomNode.getNode("gd12v2-left/gd21v1/gd21v1-mid/gd22v2/gd22v2-right/htmlblob"):null;
					if (htmlBlobNode_5 != null) {
						sb.append(Constants.HTMLBLOB_NODE_NOT_FOUND);					
					}
				}
				catch (Exception e) {
					sb.append(Constants.EXCEPTION_IN_HTMLBLOB);
					log.error("Exception " , e);
				}
				// End get content.
				//End of bottom node left html blob component
				//----------------------------------------------------------------------------------------------------------------------------
				//start of bottom node right html blob component
				try {
					log.debug("Started migrating HtmlBlob content.");
					
				//Start of set content
					Node htmlBlobNode = webBottomNode.hasNode("gd12v2-right/htmlblob")?webBottomNode.getNode("gd12v2-right/htmlblob"):null;
					if (htmlBlobNode != null) {
						sb.append(Constants.HTMLBLOB_NODE_NOT_FOUND);					
					}	
				}
				catch (Exception e) {
					sb.append(Constants.EXCEPTION_IN_HTMLBLOB);
					log.error("Exception " , e);
				}
				// End get content.
				//End of bottom node right html blob component
				//----------------------------------------------------------------------------------------------------------------------------
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
