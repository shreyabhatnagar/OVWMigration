package com.cisco.dse.global.migration.architecture;

import java.io.IOException;

import javax.jcr.Node;
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

public class ArchitectureVariation04 extends BaseAction {
	Document doc;
	StringBuilder sb = new StringBuilder(1024);
	static Logger log = Logger.getLogger(ArchitectureVariation04.class);

	public String translate(String host, String loc, String prod, String type,
			String catType, String locale, Session session) throws IOException,
			ValueFormatException, VersionException, LockException,
			ConstraintViolationException, RepositoryException {
		log.debug("In the translate method of ArchitectureVariation04");
		log.debug("In the translate method, catType is :" + catType);

		// Repo node paths

		String pageUrl = host + "/content/<locale>/" + catType
				+ "/<prod>/solutions-listing.html";
		pageUrl = pageUrl.replace("<locale>", locale).replace("<prod>", prod);

		sb.append("<td>" + "<a href=" + pageUrl + ">" + pageUrl + "</a>"
				+ "</td>");
		sb.append("<td>" + "<a href=" + loc + ">" + loc + "</a>" + "</td>");
		sb.append("<td><ul>");

		String architectureLeftPath = "/content/<locale>/"
				+ catType
				+ "/<prod>/architecture/jcr:content/content_parsys/architecture/layout-architecture/gd12v2/gd12v2-left";
		String architectureRightPath = "/content/<locale>/"
				+ catType
				+ "/<prod>/architecture/jcr:content/content_parsys/architecture/layout-architecture/gd12v2/gd12v2-right";
		architectureLeftPath = architectureLeftPath.replace("<locale>", locale).replace(
				"<prod>", prod);
		architectureRightPath = architectureRightPath.replace("<locale>", locale).replace(
				"<prod>", prod);
		javax.jcr.Node architectureLeftNode = null;
		javax.jcr.Node architectureRightNode = null;
		try {

			architectureLeftNode = session.getNode(architectureLeftPath);
			architectureRightNode = session.getNode(architectureRightPath);
			doc = getConnection(loc);

			if (doc != null) {
				// start of text component properties setting
				try {
				/*	String textProp = "";
					StringBuilder textContent = new StringBuilder();
					Element textElements = doc.select("div.gd22v2-left").select("c00-pilot").first();
					if (textElements != null ) {
						Element h2Element = textElements.getElementsByTag("h2").first();
						textContent.append(textElements.select("h2"));
					}else {
							sb.append(Constants.TEXT_ELEMENT_NOT_FOUND);
						}
					
					textProp = textContent.toString();
					Node textNode = architectureLeftNode.hasNode("text") ? architectureLeftNode
								.getNode("text") : null;
						if (textNode != null) {
							if (textProp != null) {
								textNode.setProperty("text", textProp);
							}
						} else {
							sb.append(Constants.TEXT_NODE_NOT_FOUND);
						}
						*/
					//After text and 1st list section
						
					Element listElements = doc.select("div.c100-dm").first();
					System.out.println(listElements.getElementsByTag("h3").first());
//					System.out.println("Fisrt one "+listElements.firstElementSibling());
//					System.out.println("Next one "+listElements.nextElementSibling());
				
				} catch (Exception e) {
					sb.append(Constants.EXCEPTION_TEXT_COMPONENT);
				}
				// end of text component properties setting
				
				
				
				
				
				
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
