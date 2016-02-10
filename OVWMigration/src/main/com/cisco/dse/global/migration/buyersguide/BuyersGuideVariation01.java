/* 
 * S.No		Name	Date		Description of change
 * 1	    saikiran	18-Dec-15   	     Added the Java file to handle the migration of solution listing variation 9 page.
 * 
 * */


package com.cisco.dse.global.migration.buyersguide;

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
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.cisco.dse.global.migration.config.BaseAction;
import com.cisco.dse.global.migration.config.Constants;
import com.cisco.dse.global.migration.config.FrameworkUtils;



public class BuyersGuideVariation01 extends BaseAction {
	Document doc;
	StringBuilder sb = new StringBuilder(1024);
	static Logger log = Logger.getLogger(BuyersGuideVariation01.class);
	
	public String translate(String host, String loc, String prod, String type,
			String catType, String locale, Session session, Map<String, String> urlMap) throws IOException,
			ValueFormatException, VersionException, LockException,
			ConstraintViolationException, RepositoryException {

		BasicConfigurator.configure();
		log.debug("In the translate method");
		log.debug("In the translate method, catType is :" + catType);
		String pagePropertiesPath = "/content/<locale>/" + catType
				+ "/<prod>/buyers-guide/jcr:content";
		
		// Repo node paths
		
		String buyersTop = "/content/<locale>/"
				+ catType
				+ "/<prod>/buyers-guide/jcr:content/eotparsys";

		String buyersBottom = "/content/<locale>/"
				+ catType
				+ "/<prod>/buyers-guide/jcr:content/content_parsys";
		String pageUrl = host + "/content/<locale>/" + catType
				+ "/<prod>/buyers-guide.html";
		

		pageUrl = pageUrl.replace("<locale>", locale).replace("<prod>", prod);
		pagePropertiesPath = pagePropertiesPath.replace("<locale>", locale)
				.replace("<prod>", prod);
		buyersTop = buyersTop.replace("<locale>", locale).replace("<prod>",prod);
		buyersBottom = buyersBottom.replace("<locale>", locale).replace("<prod>",prod);
		sb.append("<td>" + "<a href=" + pageUrl + ">" + pageUrl + "</a>"
				+ "</td>");
		sb.append("<td>" + "<a href=" + loc + ">" + loc + "</a>" + "</td>");
		sb.append("<td><ul>");
		
		Node buyersTopNode = null;
		Node buyersBottomNode = null;
		Node pageJcrNode = null;
		
        try {
			
        	buyersTopNode = session.getNode(buyersTop);
        	buyersBottomNode = session.getNode(buyersBottom);
        	pageJcrNode = session.getNode(pagePropertiesPath);
        	
        	doc = getConnection(loc);
        	
             if (doc != null) {
				
				// ------------------------------------------------------------------------------------------------------------------------------------------
				// start set page properties.

				FrameworkUtils.setPageProperties(pageJcrNode, doc, session, sb);

				// end set page properties.
				// ------------------------------------------------------------------------------------------------------------------------------------------
				// start of htmlblob component
				try {
					String rawHtml = "";
					//StringBuilder oldImage = new StringBuilder();
					log.debug("Started migrating HtmlBlob content.");
					//Elements rawElements=doc.getElementsByTag("html");
					//Elements images=doc.getElementsByTag("img");
					Element tabElements = doc.select("div.gd01-pilot,div.sitecopy,div.gd21-pilot").first();
					
					/*if (rawElements != null
							&& !rawElements.isEmpty()) {
						rawHtml1 = rawElements.html();
				}
						 else {
						sb.append(Constants.HTMLBLOB_ELEMENT_NOT_FOUND);
					}
					*/
					if (tabElements != null){
						log.debug("tabElements before:"+tabElements);
							rawHtml = FrameworkUtils.extractHtmlBlobContent(tabElements, "", locale, sb, urlMap, catType, type);
							log.debug("tabElements after:"+rawHtml);
						 //oldImage.append(rawHtml2);
						//log.debug("oldImage:"+oldImage);
						
				}
						/*if("ja_jp".equals(locale)){
						String listDesc = tabElements.first().getElementsByTag("ul").addClass("no-bullets").outerHtml();
						rawHtml2 = rawHtml2 + listDesc;
						}*/
						 else {
						sb.append(Constants.HTMLBLOB_ELEMENT_NOT_FOUND);
					}
					
					// End get content.
					// Start set content.
					if (buyersTopNode.hasNode("raw_html")) {
						Node htmlBlobNode = buyersTopNode.getNode("raw_html");
						if (StringUtils.isNotBlank(rawHtml)) {
							htmlBlobNode.setProperty("htmlContent",rawHtml);
							log.debug("HtmlBlob Content migrated is done.");
						}
					} else {
						sb.append(Constants.HTMLBLOB_NODE_NOT_FOUND);
					}
					if (buyersBottomNode.hasNode("raw_html")) {
						Node htmlBlobNode = buyersBottomNode.getNode("raw_html");
						if (StringUtils.isNotBlank(rawHtml)) {
							htmlBlobNode.setProperty("htmlContent",rawHtml);
							log.debug("HtmlBlob Content migrated is done.");
						}
					} else {
						sb.append(Constants.HTMLBLOB_NODE_NOT_FOUND);
					}
					// End get content.
				} catch (Exception e) {
					log.error("Exception : ", e);
					sb.append(Constants.EXCEPTION_IN_HTMLBLOB);
				}
				// end htmlblob component.
				// ------------------------------------------------------------------------------------------------------------------------------------------
				session.save();
 			} else {
 				sb.append(Constants.URL_CONNECTION_EXCEPTION);
 			}
 		} catch (Exception e) {
 			sb.append(Constants.URL_CONNECTION_EXCEPTION);
 			log.debug("Exception as url cannot be connected: " + e);
 		}

 		sb.append("</ul></td>");

 		return sb.toString();

					
}
}
        
