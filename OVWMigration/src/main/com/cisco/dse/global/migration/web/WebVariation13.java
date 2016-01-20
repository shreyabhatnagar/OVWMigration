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

public class WebVariation13 extends BaseAction {
	Document doc;
	StringBuilder sb = new StringBuilder(1024);
	static Logger log = Logger.getLogger(WebVariation13.class);
	
	int noImageCount = 0;
	
	public String translate(String host, String loc, String prod, String type,
			String catType, String locale, Session session, Map<String, String> urlMap) throws IOException,
			ValueFormatException, VersionException, LockException,
			ConstraintViolationException, RepositoryException {
		log.debug("In the translate method of WebVariation12");
		log.debug("In the translate method, catType is :" + catType);
		// start 
		String pagePropertiesPath = "/content/<locale>/" + catType + "/index/jcr:content";
		//end
		String pageUrl = host + "/content/<locale>/"+catType+"/index.html";
		
		pageUrl = pageUrl.replace("<locale>", locale);
		pagePropertiesPath = pagePropertiesPath.replace("<locale>", locale);
		String rightNodePath = pagePropertiesPath+"/content_parsys/overview/layout-overview/gd12v2/gd12v2-right";
		String leftNodePath = pagePropertiesPath+"/content_parsys/overview/layout-overview/gd12v2/gd12v2-left";

		sb.append("<td>" + "<a href="+pageUrl+">"+pageUrl+"</a>"+"</td>");
		sb.append("<td>" + "<a href="+loc+">"+loc +"</a>"+ "</td>");
		sb.append("<td><ul>");
		
		
		javax.jcr.Node rightNode = null;
		javax.jcr.Node leftNode = null;
		javax.jcr.Node pageJcrNode = null;
		try {

			rightNode = session.getNode(rightNodePath);
			leftNode = session.getNode(leftNodePath);
			pageJcrNode = session.getNode(pagePropertiesPath);
			try {
				doc = getConnection(loc);
			} catch (Exception e) {
				log.error("Exception ", e);
			}
			log.debug("pagePropertiesPath********** " + pagePropertiesPath);
			log.debug("rightNodePath********** " + rightNodePath);
			log.debug("leftNodePath********** " + leftNodePath);
			if (doc != null) {
				// ------------------------------------------------------------------------------------------------------------------------------------------
				// start set page properties.

				FrameworkUtils.setPageProperties(pageJcrNode, doc, session, sb);

				// end set page properties.
				// ------------------------------------------------------------------------------------------------------------------------------------------
				//start of right html blob component
				try {
					if (doc.getElementById("col3") != null) {
						Element htmlBlobElement =  doc.getElementById("col3");
						String html = FrameworkUtils.extractHtmlBlobContent(htmlBlobElement, "", locale, sb, urlMap);
						if (rightNode.hasNode("htmlblob")) {
							Node htmlblobNode = rightNode.getNode("htmlblob");
							if (StringUtils.isNotBlank(html)) {
								log.debug("right html &&&&&&&&&&& " + html);
								htmlblobNode.setProperty("html", html);
							}
						}
					} else {
						sb.append("<li>right htmlblob component not found on publisher page </li>");
					}				
				} catch (Exception e) {
					log.debug("Excepiton : ", e);
				}
				//End of right htmlblob Component
				//------------------------------------------------------------------------------------------------------------------------------
				//start of left html blob component
				//-------------------------------------------------------------------------------------------------------------------------
				try {
					if (doc.select("div.guest").size() > 0) {
						Element guestElement = doc.select("div.guest").first();
						if (guestElement != null) {
							/*Elements popUpContainer = guestElement.select("div.popUpContainer");
							if (popUpContainer.size() > 0) {
								Element containerElement = popUpContainer.first();
								containerElement.remove();
							}*/
							Element id21Element = guestElement.getElementById("col3");
							if (id21Element != null) {
								id21Element.remove();
							}
							Element htmlBlobElementAfterRemove =  doc.select("div.guest").first();
							String html = FrameworkUtils.extractHtmlBlobContent(htmlBlobElementAfterRemove, "", locale, sb, urlMap);
							if (leftNode.hasNode("htmlblob")) {
								Node htmlblobNode = leftNode.getNode("htmlblob");
								if (StringUtils.isNotBlank(html)) {
									log.debug("left html &&&&&&&&&&& " + html);
									htmlblobNode.setProperty("html", html);
								}
							}
						}
					} else {
						sb.append("<li>left htmlblob component not found on publisher page </li>");
					}		
				}
				catch (Exception e) {
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
