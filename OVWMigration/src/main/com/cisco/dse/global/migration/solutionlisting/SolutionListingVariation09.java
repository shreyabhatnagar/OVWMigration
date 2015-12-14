/* 
 * S.No		Name			Description of change
 * 1	    vidya		Added the Java file to handle the migration of solution listing variation 9 page.
 * 
 * */
package com.cisco.dse.global.migration.solutionlisting;

import java.io.IOException;

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

public class SolutionListingVariation09 extends BaseAction {
	Document doc;
	StringBuilder sb = new StringBuilder(1024);
	static Logger log = Logger.getLogger(SolutionListingVariation09.class);

	public String translate(String host, String loc, String prod, String type,
			String catType, String locale, Session session) throws IOException,
			ValueFormatException, VersionException, LockException,
			ConstraintViolationException, RepositoryException {
		log.debug("In the translate method of SolutionListingVariation09");
		log.debug("In the translate method, catType is :" + catType);

		// Repo node paths
		
		String pageUrl = host + "/content/<locale>/" + catType
				+ "/<prod>/solutions-listing.html";
		pageUrl = pageUrl.replace("<locale>", locale).replace("<prod>", prod);
		
		sb.append("<td>" + "<a href=" + pageUrl + ">" + pageUrl + "</a>"
				+ "</td>");
		sb.append("<td>" + "<a href=" + loc + ">" + loc + "</a>" + "</td>");
		sb.append("<td><ul>");

		String solutionListing = "/content/<locale>/"
				+ catType
				+ "/<prod>/solutions-listing/jcr:content/content_parsys/solutions/layout-solutions/gd21v1/gd21v1-mid";

		solutionListing = solutionListing.replace("<locale>", locale).replace(
				"<prod>", prod);
		javax.jcr.Node solutionListingMidnode = null;
		try {

			solutionListingMidnode = session.getNode(solutionListing);
			doc = getConnection(loc);

			if (doc != null) {
				// ----------------------------------------------------------------------------------
				// start of text component properties setting
				try {
					String textProp = "";
					StringBuilder textContent = new StringBuilder();
					Elements textHeadingElements = doc.select("div.c00v0-pilot");
					Elements textDescriptionElements = doc.select("div.c00v1-pilot");
					if (textHeadingElements != null ) {
						textContent.append(textHeadingElements.first().html());
					}else {
							sb.append(Constants.TEXT_ELEMENT_NOT_FOUND);
						}
					if(textDescriptionElements != null){
						textContent.append(textDescriptionElements.html());
					}
					textProp = textContent.toString();
					Node textNode = solutionListingMidnode.hasNode("text") ? solutionListingMidnode
								.getNode("text") : null;
						if (textNode != null) {
							if (textProp != null) {
								textNode.setProperty("text", textProp);
							}
						} else {
							sb.append(Constants.TEXT_NODE_NOT_FOUND);
						}
				} catch (Exception e) {
					sb.append(Constants.EXCEPTION_TEXT_COMPONENT);
				}
				// end of text component properties setting
				//----------------------------------------------------------------------------------------------------------------
				// start of htmlblob component
				try {
					String htmlBlobContent = "";
					log.debug("Started migrating HtmlBlob content.");
					// Start get content.
					Elements htmlBlobElements = doc
							.select("div.htmlblob");
					if (htmlBlobElements != null
							&& !htmlBlobElements.isEmpty()) {
							htmlBlobContent = htmlBlobElements.select("div.c00-pilot").outerHtml();
						}
					 else {
						sb.append(Constants.HTMLBLOB_ELEMENT_NOT_FOUND);
					}
					// End get content.
					// Start set content.
					if (solutionListingMidnode.hasNode("htmlblob_0")) {
						Node htmlBlobNode = solutionListingMidnode.getNode("htmlblob_0");
						if (!StringUtils.isEmpty(htmlBlobContent)) {
							htmlBlobNode.setProperty("html",htmlBlobContent);
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
