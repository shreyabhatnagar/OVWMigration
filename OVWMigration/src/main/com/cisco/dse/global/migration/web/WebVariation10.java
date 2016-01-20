/* 
 * S.No		Name	Date		Description of change
 * 1		kiran   11-jan-16	Added the Java file to handle the migration of web about pages.
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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.cisco.dse.global.migration.config.BaseAction;
import com.cisco.dse.global.migration.config.Constants;
import com.cisco.dse.global.migration.config.FrameworkUtils;



public class WebVariation10 extends BaseAction{
	Document doc;
	StringBuilder sb = new StringBuilder(1024);
	static Logger log = Logger.getLogger(WebVariation10.class);
	
	int noImageCount = 0;
	
	public String translate(String host, String loc, String prod, String type,
			String catType, String locale, Session session, Map<String, String> urlMap) throws IOException,
			ValueFormatException, VersionException, LockException,
			ConstraintViolationException, RepositoryException {
		log.debug("In the translate method of WebVariation1");
		log.debug("In the translate method, catType is :" + catType);

		String pagePropertiesPath = "/content/<locale>/" + catType
				+ "/overview/jcr:content";
		String pageUrl = host + "/content/<locale>/" + catType
				+ "/overview.html";
		pagePropertiesPath = pagePropertiesPath.replace("<locale>", locale)
				.replace("<prod>", prod);
		pageUrl = pageUrl.replace("<locale>", locale).replace("<prod>", prod);
		
		sb.append("<td>" + "<a href=" + pageUrl + ">" + pageUrl + "</a>"
				+ "</td>");
		sb.append("<td>" + "<a href=" + loc + ">" + loc + "</a>" + "</td>");
		sb.append("<td><ul>");
		
		String firstRowLeftNodePath = "/content/<locale>/"+catType+"/overview/jcr:content/content_parsys/overview/layout-overview/gd12v2/gd12v2-left";
		firstRowLeftNodePath = firstRowLeftNodePath.replace("<locale>",	locale).replace("<prod>", prod);
		String secondRowLeftNodePath = "/content/<locale>/"+catType+"/overview/jcr:content/content_parsys/overview/layout-overview/gd12v2/gd12v2-left/gd23v1/gd23v1-left";
		secondRowLeftNodePath = secondRowLeftNodePath.replace("<locale>",locale).replace("<prod>", prod);
		String secondRowMidNodePath = "/content/<locale>/"+catType+"/overview/jcr:content/content_parsys/overview/layout-overview/gd12v2/gd12v2-left/gd23v1/gd23v1-mid";
		secondRowMidNodePath = secondRowMidNodePath.replace("<locale>",	locale).replace("<prod>", prod);
		String secondRowRightNodePath = "/content/<locale>/"+catType+"/overview/jcr:content/content_parsys/overview/layout-overview/gd12v2/gd12v2-left/gd23v1/gd23v1-right";
		secondRowRightNodePath = secondRowRightNodePath.replace("<locale>",locale).replace("<prod>", prod);
		String thirdRowHeadingNodePath = "/content/<locale>/"+catType+"/overview/jcr:content/content_parsys/overview/layout-overview/gd12v2/gd12v2-left/gd21v1/gd21v1-mid";
		thirdRowHeadingNodePath = thirdRowHeadingNodePath.replace("<locale>",locale).replace("<prod>", prod);
		String thirdRowLeftNodePath = "/content/<locale>/"+catType+"/overview/jcr:content/content_parsys/overview/layout-overview/gd12v2/gd12v2-left/gd21v1/gd21v1-mid/gd23v1/gd23v1-left";
		thirdRowLeftNodePath = thirdRowLeftNodePath.replace("<locale>",locale).replace("<prod>", prod);
		String thirdRowMidNodePath = "/content/<locale>/"+catType+"/overview/jcr:content/content_parsys/overview/layout-overview/gd12v2/gd12v2-left/gd21v1/gd21v1-mid/gd23v1/gd23v1-mid";
		thirdRowMidNodePath = thirdRowMidNodePath.replace("<locale>",locale).replace("<prod>", prod);
		String thirdRowRightNodePath = "/content/<locale>/"+catType+"/overview/jcr:content/content_parsys/overview/layout-overview/gd12v2/gd12v2-left/gd21v1/gd21v1-mid/gd23v1/gd23v1-right";
		thirdRowRightNodePath = thirdRowRightNodePath.replace("<locale>",locale).replace("<prod>", prod);
		String rightRailNodePath = "/content/<locale>/"+catType+"/overview/jcr:content/content_parsys/overview/layout-overview/gd12v2/gd12v2-right";
		rightRailNodePath = rightRailNodePath.replace("<locale>",locale).replace("<prod>", prod);
		
		Node firstRowLeftNode = null;
		Node secondRowLeftNode = null;
		Node secondRowMidNode = null;
		Node secondRowRightNode = null;
		Node thirdRowHeadingNode= null;
		Node thirdRowLeftNode= null;
		Node thirdRowMidNode = null;
		Node thirdRowRightNode= null;
		Node rightRailNode= null;
		Node pageJcrNode = null;
		try {

			firstRowLeftNode = session.getNode(firstRowLeftNodePath);
			secondRowLeftNode = session.getNode(secondRowLeftNodePath);
			secondRowMidNode = session.getNode(secondRowMidNodePath);
			secondRowRightNode = session.getNode(secondRowRightNodePath);
			thirdRowHeadingNode = session.getNode(thirdRowHeadingNodePath);
			thirdRowLeftNode = session.getNode(thirdRowLeftNodePath);
			thirdRowMidNode = session.getNode(thirdRowMidNodePath);
			thirdRowRightNode = session.getNode(thirdRowRightNodePath);
			rightRailNode = session.getNode(rightRailNodePath);
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
				// start of htmlblob component
				try {
					String htmlBlobContent = "";
					
					log.debug("Started migrating HtmlBlob content.");
					// Start get content.
					Element htmlBlobElement = doc.select("div.gd-left").select("div.c00-pilot").first();
					Element htmlBlobHeadElement = doc.select("div.gd-left").select("div.compact").first();
					if (htmlBlobElement != null) {
						htmlBlobContent = FrameworkUtils.extractHtmlBlobContent(htmlBlobElement, "", locale, sb, urlMap);
						String htmlBlobHeadHtml = "";
						if(htmlBlobHeadElement != null){
							htmlBlobHeadHtml = FrameworkUtils.extractHtmlBlobContent(htmlBlobHeadElement, "", locale, sb, urlMap);
						}
						htmlBlobContent = htmlBlobContent + htmlBlobHeadHtml;
						log.debug("htmlBlobContent is :"+ htmlBlobContent);
					
					//End of getContent
					//Start of set content
					if (firstRowLeftNode.hasNode("htmlblob")) {
						Node htmlBlobNode = firstRowLeftNode.getNode("htmlblob");
						if (!StringUtils.isEmpty(htmlBlobContent)) {
							htmlBlobNode.setProperty("html",htmlBlobContent);
							log.debug("HtmlBlob Content migrated is done.");
						}
					} else {
						sb.append(Constants.HTMLBLOB_NODE_NOT_FOUND);
					}
					}else{
						sb.append(Constants.HTMLBLOB_ELEMENT_NOT_FOUND+" in top left rail.</li>");
					}
				}	
				catch (Exception e) {
					sb.append(Constants.EXCEPTION_IN_HTMLBLOB);
					log.error("Exception " , e);
				}
				

				// End get content.
			//End of htmlblob Component
			//-------------------------------------------------------------------------------------
			//start of right-grid Component
			//start of htmlblob Component
				try {
					String leftHtmlBlobContent = "";
					String midHtmlBlobContent = "";
					String rightHtmlBlobContent = "";
					
					log.debug("Started migrating HtmlBlob content.");
					// Start get content.
					Element htmlBlobElement = doc.select("div.gd23-pilot").first();
					if (htmlBlobElement != null) {
						log.debug("htmlblobl element for first list kind: "+htmlBlobElement );
						Element leftHtmlblobElement = htmlBlobElement.select("div.gd-left").first();
						Element midHtmlblobElement = htmlBlobElement.select("div.gd-mid").first();
						Element rightHtmlblobElement = htmlBlobElement.select("div.gd-right").first();
						if(leftHtmlblobElement != null){
							leftHtmlBlobContent = FrameworkUtils.extractHtmlBlobContent(leftHtmlblobElement, "", locale, sb, urlMap);
						}
						log.debug("htmlblobl leftttt element for first list kind: "+leftHtmlblobElement );
						if(midHtmlblobElement != null){
							midHtmlBlobContent =  FrameworkUtils.extractHtmlBlobContent(midHtmlblobElement, "", locale, sb, urlMap);
						}
						if(rightHtmlblobElement != null){
							rightHtmlBlobContent =  FrameworkUtils.extractHtmlBlobContent(rightHtmlblobElement, "", locale, sb, urlMap);
						}
					//End of getContent
					//Start of set content
					if (secondRowLeftNode.hasNode("htmlblob")) {
						Node htmlBlobNode = secondRowLeftNode.getNode("htmlblob");
						if (!StringUtils.isEmpty(leftHtmlBlobContent)) {
							htmlBlobNode.setProperty("html",leftHtmlBlobContent);
							log.debug("HtmlBlob Content migrated is done at."+ secondRowLeftNode.getPath());
						}
					} else {
						sb.append(Constants.HTMLBLOB_NODE_NOT_FOUND);
					}
					if (secondRowMidNode.hasNode("htmlblob")) {
						Node htmlBlobNode = secondRowMidNode.getNode("htmlblob");
						if (!StringUtils.isEmpty(leftHtmlBlobContent)) {
							htmlBlobNode.setProperty("html",midHtmlBlobContent);
							log.debug("HtmlBlob Content migrated is done at."+ secondRowMidNode.getPath());
						}
					} else {
						sb.append(Constants.HTMLBLOB_NODE_NOT_FOUND);
					}
					if (secondRowRightNode.hasNode("htmlblob")) {
						Node htmlBlobNode = secondRowRightNode.getNode("htmlblob");
						if (!StringUtils.isEmpty(leftHtmlBlobContent)) {
							htmlBlobNode.setProperty("html",rightHtmlBlobContent);
							log.debug("HtmlBlob Content migrated is done at."+ secondRowRightNode.getPath());
						}
					} else {
						sb.append(Constants.HTMLBLOB_NODE_NOT_FOUND);
					}
					}else{
						sb.append(Constants.HTMLBLOB_ELEMENT_NOT_FOUND+" in left mid rail.</li>");
					}
				}
				catch (Exception e) {
					sb.append(Constants.EXCEPTION_IN_HTMLBLOB);
					log.error("Exception " , e);
				}
				// end htmlblob component.
				
				//start of htmlblob Component
				try {
					String htmlBlobContent = "";
					
					log.debug("Started migrating HtmlBlob content.");
					// Start get content.
					Element htmlBlobHeadElement = doc.select("div.c00v0-alt1-pilot").last();
					if (htmlBlobHeadElement != null) {
						htmlBlobContent = FrameworkUtils.extractHtmlBlobContent(htmlBlobHeadElement, "", locale, sb, urlMap);
					
					//End of getContent
					//Start of set content
					if (thirdRowHeadingNode.hasNode("htmlblob")) {
						Node htmlBlobNode = thirdRowHeadingNode.getNode("htmlblob");
						if (StringUtils.isNotBlank(htmlBlobContent)) {
							htmlBlobNode.setProperty("html",htmlBlobContent);
							log.debug("HtmlBlob Content migrated is done at ."+ thirdRowHeadingNode.getPath());
						}
					} else {
						sb.append(Constants.HTMLBLOB_NODE_NOT_FOUND);
					}
					}else{
						sb.append(Constants.HTMLBLOB_ELEMENT_NOT_FOUND+" at the middle of the page..</li>");
					}
				}
				catch (Exception e) {
					sb.append(Constants.EXCEPTION_IN_HTMLBLOB);
					log.error("Exception " , e);
				}
				// end htmlblob component.
				
				//start of htmlblob Component
				try {
					List<String> leftNodeItemsList = new ArrayList<String>();
					List<String> midNodeItemsList = new ArrayList<String>();
					List<String> rightNodeItemsList = new ArrayList<String>();
					
					
					log.debug("Started migrating HtmlBlob content.");
					// Start get content.
					Elements htmlBlobElements = doc.select("div.gd23v1-pilot");
					
					/*Element firstGd23V1 = htmlBlobElements.first();
					if(firstGd23V1 != null){
						
						firstGd23V1.remove();
					}
					*/
					log.debug("htmlBlobElements isssss::::: "+ htmlBlobElements);
					if (!htmlBlobElements.isEmpty()){
						for(Element eachHtmlElement : htmlBlobElements){
						Elements childElem =	eachHtmlElement.children();
						
						boolean isFirstBlob = false;
							log.debug("eachHtmlElement is: "+ eachHtmlElement);
							log.debug("eachHtmlElement size is: "+ childElem.size());
						if(childElem.size() == 3 || eachHtmlElement == htmlBlobElements.last()) {
						Element leftHtmlBlobElement = eachHtmlElement.select("div.gd-left").first();
						if(leftHtmlBlobElement != null && leftHtmlBlobElement.child(0).hasClass("n13-pilot")){
							isFirstBlob = true;
						}
						log.debug("isFirstBlob value:  "+ isFirstBlob);
						Element midHtmlBlobElement = eachHtmlElement.select("div.gd-mid").first();
						Element rightHtmlBlobElement = eachHtmlElement.select("div.gd-right").first();
						if(leftHtmlBlobElement != null && !isFirstBlob){
							String htmlContent = FrameworkUtils.extractHtmlBlobContent(leftHtmlBlobElement, "", locale, sb, urlMap);
							leftNodeItemsList.add(htmlContent);
						}
						if(midHtmlBlobElement != null && !isFirstBlob){
							String htmlContent = FrameworkUtils.extractHtmlBlobContent(midHtmlBlobElement, "", locale, sb, urlMap);
							if(midNodeItemsList.size() == 3){
								leftNodeItemsList.add(htmlContent);
							}
							else{
							midNodeItemsList.add(htmlContent);
							}
						}
						if(rightHtmlBlobElement != null && !isFirstBlob){
							String htmlContent = FrameworkUtils.extractHtmlBlobContent(rightHtmlBlobElement, "", locale, sb, urlMap);
							rightNodeItemsList.add(htmlContent);
						}
						
						}
					}
				
					//End of getContent
					//Start of set content
					
					NodeIterator thirdRowLeftIterator = thirdRowLeftNode.getNodes("htmlblob*");
					NodeIterator thirdRowMidIterator = thirdRowMidNode.getNodes("htmlblob*");
					NodeIterator thirdRowRightIterator = thirdRowRightNode.getNodes("htmlblob*");
					
					if(thirdRowLeftIterator != null){
						int i = 0;
						while(thirdRowLeftIterator.hasNext()){
							Node htmlBlobNode = (Node) thirdRowLeftIterator.next();
							if(i<leftNodeItemsList.size()){
								if(htmlBlobNode != null){
									htmlBlobNode.setProperty("html", leftNodeItemsList.get(i));
									i++;
								}
								
							}
						}
					}
					if(thirdRowMidIterator != null){
						int i = 0;
						while(thirdRowMidIterator.hasNext()){
							Node htmlBlobNode = (Node) thirdRowMidIterator.next();
							if(i<midNodeItemsList.size()){
								if(htmlBlobNode != null){
									htmlBlobNode.setProperty("html", midNodeItemsList.get(i));
									i++;
								}
								
							}
						}					
					}
					if(thirdRowRightIterator != null){
						int i = 0;
						while(thirdRowRightIterator.hasNext()){
							Node htmlBlobNode = (Node) thirdRowRightIterator.next();
							if(i<rightNodeItemsList.size()){
								if(htmlBlobNode != null){
									htmlBlobNode.setProperty("html", rightNodeItemsList.get(i));
									i++;
								}
								
							}
						}
					}
					}else{
						sb.append(Constants.HTMLBLOB_ELEMENT_NOT_FOUND);
					}
				}
					
					catch (Exception e) {
					sb.append(Constants.EXCEPTION_IN_HTMLBLOB);
					log.error("Exception " , e);
				}
				// end htmlblob component.
				
				//start of htmlblob Component
				try {
					String htmlBlobContent = "";
					String htmlBlobRightContent = "";
					Element fHolderElement = null;
					Element gdRightElement = null;
					log.debug("Started migrating HtmlBlob content.");
					// Start get content.
					Element htmlBlobElement = doc.select("div#framework-content-right").first();
					boolean extraTextExists = false;
					if(htmlBlobElement != null){
							Element childElement = htmlBlobElement.select("div.module-related").first();
							Element htmlBlobElementMain = doc.select("div#framework-content-main").first();
							log.debug("htmlBlobElementMain size is "+ htmlBlobElementMain);
							if(childElement != null){
								extraTextExists = true;
								htmlBlobElement = doc.select("div#framework-content-main").first();
								sb.append("<li> Extra text found on right side of web page. </li>");
								
							}
							Element gd12V1Element = htmlBlobElement.select("div.gd12-pilot").first();
							//log.debug("gd12V1Element size iss"+ gd12V1Element.size());
							if(gd12V1Element != null){
								log.debug("gd12V1Element is"+ gd12V1Element);
								gdRightElement = gd12V1Element.select("div.gd-right").last();
								log.debug("gdRightElement is"+ gdRightElement);
								if(gdRightElement != null){
							htmlBlobContent = FrameworkUtils.extractHtmlBlobContent(gdRightElement, "", locale, sb, urlMap);
							}
							}
					
					if (!extraTextExists) {
						fHolderElement = htmlBlobElement.select("div#t-col-2").first();
						if(fHolderElement != null){
						htmlBlobContent = FrameworkUtils.extractHtmlBlobContent(fHolderElement, "", locale, sb, urlMap);
						}else{
							sb.append(Constants.HTMLBLOB_ELEMENT_NOT_FOUND+" in right rail.</li>");
						}
						gdRightElement = doc.select("div.poly").first();
						if(gdRightElement != null){
						htmlBlobRightContent = FrameworkUtils.extractHtmlBlobContent(gdRightElement, "", locale, sb, urlMap);
						htmlBlobContent = htmlBlobContent.concat(htmlBlobRightContent);
						log.debug("right rail is: "+ htmlBlobContent);
					}
						
					}
					//End of getContent
					//Start of set content
					if (rightRailNode.hasNode("htmlblob")) {
						Node htmlBlobNode = rightRailNode.getNode("htmlblob");
						if (!StringUtils.isEmpty(htmlBlobContent)) {
							htmlBlobNode.setProperty("html",htmlBlobContent);
							log.debug("HtmlBlob Content migrated is done.");
						}
					} else {
						sb.append(Constants.HTMLBLOB_NODE_NOT_FOUND);
					}
					}else{
						sb.append(Constants.HTMLBLOB_ELEMENT_NOT_FOUND+" in right rail.</li>");
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