package com.cisco.dse.global.migration.web;

/* S.No			Name		Date		Description of change
 * 1			Bhavya		11-Jan-16	Added the Java file to handle the migration of Marco Page.
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

import com.cisco.dse.global.migration.benefit.BenefitsVariation03;
import com.cisco.dse.global.migration.config.BaseAction;
import com.cisco.dse.global.migration.config.Constants;
import com.cisco.dse.global.migration.config.FrameworkUtils;


public class WebVariation6 extends BaseAction{
	Document doc = null;

	StringBuilder sb = new StringBuilder(1024);

	Logger log = Logger.getLogger(BenefitsVariation03.class);

	public String translate(String host, String loc, String prod, String type,
			String catType, String locale, Session session,  Map<String, String> urlMap) throws IOException,
			ValueFormatException, VersionException, LockException,
			ConstraintViolationException, RepositoryException {
		log.debug("In the translate method WebVariation6");
		log.debug("In the translate method, catType is :" + catType);
		String pagePropertiesPath = "/content/<locale>/" + catType
				+ "/<prod>/private-hybrid-solutions/jcr:content";
		String privateHybridRight = "/content/<locale>/"
				+ catType
				+ "/<prod>/private-hybrid-solutions/jcr:content/content_parsys/solutions/layout-solutions/gd21v1/gd21v1-mid";

		String pageUrl = host + "/content/<locale>/" + catType
				+ "/<prod>/private-hybrid-solutions.html";
		pageUrl = pageUrl.replace("<locale>", locale).replace("<prod>", prod);
		pagePropertiesPath = pagePropertiesPath.replace("<locale>", locale)
				.replace("<prod>", prod);
		sb.append("<td>" + "<a href=" + pageUrl + ">" + pageUrl + "</a>"
				+ "</td>");
		sb.append("<td>" + "<a href=" + loc + ">" + loc + "</a>" + "</td>");
		sb.append("<td><ul>");

		privateHybridRight = privateHybridRight.replace("<locale>", locale).replace("<prod>",
				prod);

		javax.jcr.Node privateHybridRightNode = null;
		javax.jcr.Node pageJcrNode = null;
		try {
			privateHybridRightNode = session.getNode(privateHybridRight);
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

				//Start of migration of the page
				try {
					migratePageElements(doc, privateHybridRightNode, locale, urlMap);
				} catch (Exception e) {
					sb.append("Exception in List Component");
					log.error("Exception : ", e);
				}
				//End of Migration of the page


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

	//Start of page Migration
	private void migratePageElements(Document doc,Node privateHybridRightNode,String locale, Map<String, String> urlMap) throws PathNotFoundException, RepositoryException {

		Elements rightElements = doc.select("div.gd12-pilot");
		doc.select("div.gd-left").remove();
		Node midNode = privateHybridRightNode.hasNode("htmlblob") ? privateHybridRightNode.getNode("htmlblob") : null ;

		if(midNode != null){
			if(!rightElements.isEmpty()){
				Element textElement = rightElements.first();
				if(textElement != null){
					String html = FrameworkUtils.extractHtmlBlobContent(textElement, "",locale, sb, urlMap);
					Element migEle = doc.getElementsByTag("migrate").first();
					if(migEle!=null){
						html = migEle.outerHtml() + html;
					}
					midNode.setProperty("html", html);
				}
			}
			else {
				sb.append(Constants.RIGHT_GRID_ELEMENT_NOT_FOUND);
			}
		}else {
			if(rightElements.isEmpty()){
				log.debug("nothing to migrate ");
			} else {
				sb.append(Constants.HTMLBLOB_NODE_DOES_NOT_EXIST);
			}
		}

	}
	//End of page Migration

}
