package com.cisco.dse.global.migration.rsolutionlisting;

/* S.No			Name		Date		Description of change
 * 1			Bhavya		21-Dec-15	Added the Java file to handle the migration of responsive solutionlisting variation 1 with 2url.
 * 2			Bhavya		06-Dec-15	Added the Java file to handle the migration of Server Unified Computing Url.
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

public class RSolutionListingVariation01 extends BaseAction{
	Document doc = null;

	StringBuilder sb = new StringBuilder(1024);

	Logger log = Logger.getLogger(RSolutionListingVariation01.class);

	public String translate(String host, String loc, String prod, String type,
			String catType, String locale, Session session, Map<String, String> urlMap) throws IOException,
			ValueFormatException, VersionException, LockException,
			ConstraintViolationException, RepositoryException {
		String pagePropertiesPath = "/content/<locale>/"+catType+"/<prod>/solution-listing/jcr:content";
		String solutionRight = "/content/<locale>/"+ catType+ "/<prod>/solution-listing/jcr:content/Grid/category/layout-category/narrowwide/NW-Wide-2";

		String pageUrl = host + "/content/<locale>/"+ catType + "/<prod>/solution-listing.html";
		pageUrl = pageUrl.replace("<locale>", locale).replace("<prod>", prod);
		pagePropertiesPath = pagePropertiesPath.replace("<locale>", locale).replace("<prod>", prod);
		sb.append("<td>" + "<a href=" + pageUrl + ">" + pageUrl + "</a>"+ "</td>");
		sb.append("<td>" + "<a href=" + loc + ">" + loc + "</a>" + "</td>");
		sb.append("<td><ul>");

		log.debug("In the translate method to migrate : '"+loc+"' to '"+pageUrl+"'");
		
		solutionRight = solutionRight.replace("<locale>", locale).replace("<prod>", prod);

		Node solutionWideNode = null;
		Node pageJcrNode = null;

		try {
			solutionWideNode = session.getNode(solutionRight);
			pageJcrNode = session.getNode(pagePropertiesPath);

			try {
				doc = getConnection(loc);
				log.debug("Connected to the provided URL");
			} catch (Exception e) {
				sb.append(Constants.URL_CONNECTION_EXCEPTION);
			}

			if(doc != null){
				// start set page properties.

				FrameworkUtils.setPageProperties(pageJcrNode, doc, session, sb);

				// end set page properties.

				//Check for Hero Component
				try {
					Elements heroElements = doc.select("div.gd-right").select("div.c39");
					if(!heroElements.isEmpty()){
						sb.append(Constants.HERO_CONTENT_NODE_NOT_FOUND);
						log.debug("hero content is there");
					}
					else
					{
						log.debug("hero content not there");
					}
				}
				catch(Exception e){
					sb.append(Constants.EXCEPTION_IN_HERO_MIGRATION);
				}
				//Check for Hero Component

				//Start of text Element
				try {
					migrateTextContent(doc,solutionWideNode, locale, urlMap);
				}
				catch(Exception e){
					sb.append(Constants.UNABLE_TO_MIGRATE_TEXT);
				}
				//End of text Element

			}else{
				sb.append(Constants.URL_CONNECTION_EXCEPTION);
			}
		}catch(Exception e){
			log.error("Exception : ",e);
		}
		sb.append("</ul></td>");
		session.save();
		log.debug("Msg returned is "+sb.toString());
		return sb.toString();
	}

	private void migrateTextContent(Document doc, Node solutionWideNode, String locale, Map<String, String> urlMap) throws PathNotFoundException, RepositoryException {

		Elements rightElements = doc.select("div.gd-right");
		Elements c100Elements = null;
		Elements cc00Elements = null;
		Element setTitle = null;
		Element c100 = null;

		if(rightElements != null){
			c100Elements = rightElements.select("div.gd21v1-mid").select("div.c100-pilot,div.c100-dm");
			cc00Elements = rightElements.select("div.cc00-pilot,div.c00-pilot");

			if(!c100Elements.isEmpty()){
				c100 = c100Elements.first();
				Elements h2Tags = c100.getElementsByTag("h2");
				if(h2Tags != null){
					setTitle = h2Tags.first();
				}
			}
			else if(!cc00Elements.isEmpty()){
				Element cc00 = cc00Elements.first();
				Elements h1Tags = cc00.getElementsByTag("h1");
				if(h1Tags != null){
					setTitle = h1Tags.first();
				}
			}
			else{
				sb.append(Constants.TEXT_DOES_NOT_EXIST);
			}
		}


		Node textNode = solutionWideNode.hasNode("text") ? solutionWideNode.getNode("text"):null;
		Node headerNode = solutionWideNode.hasNode("header") ? solutionWideNode.getNode("header"):null;
		Elements heroElements = doc.select("div.c39");

		String html = "";
		if(headerNode != null){
			if(setTitle != null){
				html = FrameworkUtils.extractHtmlBlobContent(setTitle, "",locale, sb, urlMap);
				headerNode.setProperty("title", html);
			}else {
				sb.append(Constants.TEXT_DOES_NOT_EXIST);
			}
		}
		else if(textNode != null){
			if(setTitle != null){
				html = FrameworkUtils.extractHtmlBlobContent(setTitle, "",locale, sb, urlMap);
				textNode.setProperty("text", html);
			}else {
				sb.append(Constants.TEXT_DOES_NOT_EXIST);
			}
		}else{
			sb.append(Constants.TEXT_NODE_NOT_FOUND);
		}

		Node textNode0 = solutionWideNode.hasNode("text_0") ? solutionWideNode.getNode("text_0"):null;
		if(textNode0 != null){
			if(!c100Elements.isEmpty()){
				Elements gdv1 = rightElements.select("div.gd21v1-mid");
				if(gdv1 != null){
					Element gdv1Element = gdv1.first();
					if (gdv1Element != null) {
						if(setTitle != null){
							String textString = setTitle.toString();
							html = FrameworkUtils.extractHtmlBlobContent(gdv1Element, "",locale, sb, urlMap);
							textNode0.setProperty("text", html.replace(textString, ""));
						}
					}
				}
			}else if(!rightElements.isEmpty()){
				Element rightElement = rightElements.first();
				if (rightElement != null) {
					if(setTitle != null){
						String textString = setTitle.toString();
						html = FrameworkUtils.extractHtmlBlobContent(rightElement, "",locale, sb, urlMap);
						textNode0.setProperty("text", html.replace(textString, ""));
					}
				}
			}else {
				sb.append(Constants.TEXT_DOES_NOT_EXIST);
			}
		}
		else if (textNode != null){
			if(!c100Elements.isEmpty()){
				Elements gdv1 = rightElements.select("div.gd21v1-mid");
				if(gdv1 != null){
					Element gdv1Element = gdv1.first();
					if (gdv1Element != null) {
						if(setTitle != null){
							String textString = setTitle.toString();
							html = FrameworkUtils.extractHtmlBlobContent(gdv1Element, "",locale, sb, urlMap);
							textNode.setProperty("text", html.replace(textString, ""));
						}
					}
				}
			}else if(!cc00Elements.isEmpty()){
				Element cc00Element = cc00Elements.last();
				if (cc00Element != null) {
					if(setTitle != null){
						String textString = setTitle.toString();
						String heroElement = "";
						if(!heroElements.isEmpty()){
							heroElement = heroElements.toString();
						}
						heroElements.remove();
						html = FrameworkUtils.extractHtmlBlobContent(cc00Element, "",locale, sb, urlMap);
						textNode.setProperty("text", html.replace(textString, ""));
					}
				}
			}else {
				sb.append(Constants.TEXT_DOES_NOT_EXIST);
			}

		}else{
			sb.append(Constants.TEXT_NODE_NOT_FOUND);
		}
	}

}
