/* 
 * S.No		Name	Date			Description of change
 * 1		Rohan	13-Jan-16		Added the Java file to handle the migration of midsize/find-solution Web url variation 4 page.
 * 
 * */

package com.cisco.dse.global.migration.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
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

public class WebVariation4 extends BaseAction {

	Document doc;

	StringBuilder sb = new StringBuilder(1024);

	static Logger log = Logger.getLogger(WebVariation4.class);

	public String translate(String host, String loc, String prod, String type, String catType, String locale, Session session, Map<String, String> urlMap) throws IOException,
			ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
		
		log.debug("In the translate method of WebVariation4");
		log.debug("In the translate method, catType is :" + catType);

		// Repo node paths
		String pagePropertiesPath = "/content/<locale>/" + catType + "/<prod>/find-solutions/products/jcr:content";
		String pageUrl = host + "/content/<locale>/" + catType + "/<prod>/find-solutions/products.html";
		pagePropertiesPath = pagePropertiesPath.replace("<locale>", locale).replace("<prod>", prod);
		pageUrl = pageUrl.replace("<locale>", locale).replace("<prod>", prod);

		sb.append("<td>" + "<a href=" + pageUrl + ">" + pageUrl + "</a>" + "</td>");
		sb.append("<td>" + "<a href=" + loc + ">" + loc + "</a>" + "</td>");
		sb.append("<td><ul>");

		String parentNodePath = "/content/<locale>/" + catType + "/<prod>/find-solutions/products/jcr:content/content_parsys/solutions/layout-solutions";
		parentNodePath = parentNodePath.replace("<locale>", locale).replace("<prod>", prod);
		
		javax.jcr.Node parentNode = null;
		javax.jcr.Node pageJcrNode = null;
		
		try {
			parentNode = session.getNode(parentNodePath);
			pageJcrNode = session.getNode(pagePropertiesPath);
			
			try {
				doc = getConnection(loc);
			} catch (Exception e) {
				log.error("Exception ", e);
			}
			
			if (doc != null) {
				
				// start set page properties.
				log.debug("Started setting page properties");
				FrameworkUtils.setPageProperties(pageJcrNode, doc, session, sb);
				log.debug("Done with the setting page properties");
				// end set page properties.
				
				//Page migration starts
				Element headerElement = doc.select("div.c00-pilot").first();
				Element shareElement = doc.select("div.gd-right").select("div.s10-pilot").first();
				log.debug("Header element is "+headerElement+"]");
				log.debug("Share element is ["+shareElement+"]");
				
				Elements leftListElements = doc.select("div.gd22-pilot").select("div.gd-left");
				log.debug("Left list count is ["+leftListElements.size()+"]");
				Elements rightListElements = doc.select("div.gd22-pilot").select("div.gd-right");
				log.debug("Right list count is ["+rightListElements.size()+"]");
				
				Elements midTitleElements = doc.select("div.gd-mid");
				log.debug("MidElements count ["+midTitleElements.size()+"]");
				
				List<String> leftListContent = new ArrayList<String>();
				List<String> rightListContent = new ArrayList<String>();
				List<String> midTitleContent = new ArrayList<String>();
								
				for (Element leftListElement : leftListElements) {
					log.debug("left list element is ["+leftListElement+"]");
					leftListContent.add(FrameworkUtils.extractHtmlBlobContent(leftListElement, "", locale, sb, urlMap));
				}
				
				for (Element rightListElement : rightListElements) {
					log.debug("right list element is ["+rightListElement+"]");
					Element rightListEle = rightListElement.children().first();
					log.debug("right list element to be migrated is ["+rightListEle+"]");
					rightListContent.add(FrameworkUtils.extractHtmlBlobContent(rightListEle, "", locale, sb, urlMap));
				}
				
				String backToTopContent = "";
				String titleContent = "";
				for (Element midTitleElement : midTitleElements) {
					log.debug("mid list element is ["+midTitleElement+"]");
					Element paraElement = midTitleElement.select("div.c00-pilot").first().getElementsByTag("p").first();
					if(paraElement != null && paraElement.hasAttr("align")) {
						log.debug("This is a link for - back to top - element");
						backToTopContent = FrameworkUtils.extractHtmlBlobContent(midTitleElement, "", locale, sb, urlMap);
					}
					else {
						log.debug("This is a htmlblob header element");
						titleContent = FrameworkUtils.extractHtmlBlobContent(midTitleElement, "", locale, sb, urlMap);
						titleContent = backToTopContent + titleContent;
						midTitleContent.add(titleContent);
						backToTopContent = "";
					}
					
				}
				if (!backToTopContent.isEmpty()) {
					log.debug("Local variable backToTopContent is not blank.");
					midTitleContent.add(backToTopContent);
				}
				
				NodeIterator listNodeIterator = null;
				NodeIterator titleNodeIterator = null;
				Node listNode = null;
				Node leftNode = null;
				Node rightNode = null;
				Node leftHtmlBlob = null;
				Node rightHtmlBlob = null;
				Node titleNode = null;
				Node midNode = null, shareHtmlBlob = null;
				Node midHtmlBlob = null, headerHtmlBlob = null;
				Node firstHeaderNode = null, leftHeaderNode = null, rightShareNode = null;
				
				if (parentNode != null) {
					firstHeaderNode = parentNode.hasNode("gd22v2") ? parentNode.getNode("gd22v2") : null;
					
					leftHeaderNode = firstHeaderNode.hasNode("gd22v2-left")?firstHeaderNode.getNode("gd22v2-left"):null;
					rightShareNode = firstHeaderNode.hasNode("gd22v2-right")?firstHeaderNode.getNode("gd22v2-right"):null;
					
					if (leftHeaderNode !=null) {
						headerHtmlBlob = leftHeaderNode.hasNode("htmlblob")?leftHeaderNode.getNode("htmlblob"):null;
						if (headerHtmlBlob != null) {						
							log.debug("header element is ["+headerElement+"]");
							if (headerElement != null) {
								headerHtmlBlob.setProperty("html", FrameworkUtils.extractHtmlBlobContent(headerElement, "", locale, sb, urlMap));
							}
							else {
								log.debug("Title header not found on locale page.");
							}
						}
						else {
							sb.append(Constants.HTMLBLOB_NODE_DOES_NOT_EXIST);
						}
					}
					else {
						sb.append(Constants.HTMLBLOB_NODE_DOES_NOT_EXIST);
					}
					
					if (rightShareNode !=null) {
						shareHtmlBlob = rightShareNode.hasNode("htmlblob")?rightShareNode.getNode("htmlblob"):null;
						if (shareHtmlBlob != null) {				
							log.debug("share element is ["+shareElement+"]");
							if (shareElement != null) {
								shareHtmlBlob.setProperty("html", FrameworkUtils.extractHtmlBlobContent(shareElement, "", locale, sb, urlMap));
							}
							else {
								log.debug("Share element not found on locale page.");
							}							
						}
						else {
							sb.append(Constants.HTMLBLOB_NODE_DOES_NOT_EXIST);
						}
					}
					else {
						sb.append(Constants.HTMLBLOB_NODE_DOES_NOT_EXIST);
					}
					
					listNodeIterator = parentNode.hasNodes() ? parentNode.getNodes("gd22v2_*") : null;
					titleNodeIterator = parentNode.hasNodes() ? parentNode.getNodes("gd21v1_*") : null;
				}
				
				int leftListSize = leftListElements.size()-1;
				if (listNodeIterator.getSize() != leftListSize) {
					log.debug("Mismatch in htmlblob node and elements size.");
					sb.append(Constants.HTMLBLOB_COUNT_MISMATCH + leftListSize + Constants.HTMLBLOB_COUNT_MISMATCH_END + listNodeIterator.getSize() + Constants.LI_TAG_CLOSE);
				}
				
				if (titleNodeIterator.getSize() != midTitleContent.size()) {
					log.debug("Mismatch in htmlblob node and elements size.");
					sb.append(Constants.HTMLBLOB_COUNT_MISMATCH + midTitleContent.size() + Constants.HTMLBLOB_COUNT_MISMATCH_END + titleNodeIterator.getSize() + Constants.LI_TAG_CLOSE);
				}
				
				
				for (int loop = 1; loop < leftListContent.size(); loop++) {
					log.debug("Loop value is "+loop);
					if (listNodeIterator.hasNext()) {
						listNode = listNodeIterator.nextNode();
						leftNode = listNode.hasNode("gd22v2-left")?listNode.getNode("gd22v2-left"):null;
						rightNode = listNode.hasNode("gd22v2-right")?listNode.getNode("gd22v2-right"):null;
						
						if (leftNode !=null) {
							leftHtmlBlob = leftNode.hasNode("htmlblob")?leftNode.getNode("htmlblob"):null;
							if (leftHtmlBlob != null) {								
								leftHtmlBlob.setProperty("html", leftListContent.get(loop));
							}
							else {
								sb.append(Constants.HTMLBLOB_NODE_DOES_NOT_EXIST);
							}
						}
						else {
							sb.append(Constants.HTMLBLOB_NODE_DOES_NOT_EXIST);
						}
						if (rightNode !=null) {
							rightHtmlBlob = rightNode.hasNode("htmlblob")?rightNode.getNode("htmlblob"):null;
							if (rightHtmlBlob != null) {
								rightHtmlBlob.setProperty("html", rightListContent.get(loop));
							}
							else {
								sb.append(Constants.HTMLBLOB_NODE_DOES_NOT_EXIST);
							}
						}else {
							sb.append(Constants.HTMLBLOB_NODE_DOES_NOT_EXIST);
						}
					}
				}
				
				for (int midLoop = 0; midLoop < midTitleContent.size(); midLoop++) {
					log.debug("midLoop value is "+midLoop);
					if (titleNodeIterator.hasNext()) {
						titleNode = titleNodeIterator.nextNode();
						midNode = titleNode.hasNode("gd21v1-mid")?titleNode.getNode("gd21v1-mid"):null;
						
						if (midNode !=null) {
							midHtmlBlob = midNode.hasNode("htmlblob")?midNode.getNode("htmlblob"):null;
							if (midHtmlBlob != null) {								
								midHtmlBlob.setProperty("html", midTitleContent.get(midLoop));
							}
							else {
								sb.append(Constants.HTMLBLOB_NODE_DOES_NOT_EXIST);
							}
						}
						else {
							sb.append(Constants.HTMLBLOB_NODE_DOES_NOT_EXIST);
						}
					}
				}
				
			} else {
				sb.append(Constants.URL_CONNECTION_EXCEPTION);
			}
			
		} catch (Exception e) {
			log.error("Exception ", e);
			sb.append(Constants.UNABLE_TO_MIGRATE_PAGE);
		}		
		
		sb.append("</ul></td>");
		session.save();
		return sb.toString();
	}	
	
}
