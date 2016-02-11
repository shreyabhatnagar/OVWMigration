/* 
 * S.No		Name	Date		Description of change
 * 1	    vidya	14-Dec-15   	     Added the Java file to handle the migration of solution listing variation 9 page.
 * 
 * */
package com.cisco.dse.global.migration.solutionlisting;

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

public class SolutionListingVariation09 extends BaseAction {
	Document doc;
	StringBuilder sb = new StringBuilder(1024);
	static Logger log = Logger.getLogger(SolutionListingVariation09.class);

	public String translate(String host, String loc, String prod, String type,
			String catType, String locale, Session session, Map<String, String> urlMap) throws IOException,
			ValueFormatException, VersionException, LockException,
			ConstraintViolationException, RepositoryException {
		log.debug("In the translate method of SolutionListingVariation09");
		log.debug("In the translate method, catType is :" + catType);
		String pagePropertiesPath = "/content/<locale>/" + catType
				+ "/<prod>/solutions-listing/jcr:content";

		// Repo node paths
		
		String pageUrl = host + "/content/<locale>/" + catType
				+ "/<prod>/solutions-listing.html";
		pageUrl = pageUrl.replace("<locale>", locale).replace("<prod>", prod);
		pagePropertiesPath = pagePropertiesPath.replace("<locale>", locale)
				.replace("<prod>", prod);
		
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
		Node pageJcrNode = null;
		
		
		try {

			solutionListingMidnode = session.getNode(solutionListing);
			pageJcrNode = session.getNode(pagePropertiesPath);
			try {
				doc = getConnection(loc);
			} catch (Exception e) {
				log.error("Exception " , e);
			}

			if (doc != null) {
				
				// ------------------------------------------------------------------------------------------------------------------------------------------
				// start set page properties.

				FrameworkUtils.setPageProperties(pageJcrNode, doc, session, sb);

				// end set page properties.
				// ------------------------------------------------------------------------------------------------------------------------------------------
				//start of spotlight component
				try{
				Node spotlightNode = solutionListingMidnode.hasNode("spotlight_medium_v2") ? solutionListingMidnode
						.getNode("spotlight_medium_v2") : null;
				if(spotlightNode != null){
					sb.append(Constants.SPOTLIGHT_ELEMENT_NOT_FOUND);
				}
				}catch(Exception e){
					sb.append(Constants.UNABLE_TO_UPDATE_SPOTLIGHT);
					log.error("Exception :", e);
				}
				//end of spotlight component
				//------------------------------------------------------------------------------------------------------------------------
				// start of text component properties setting
				try {
					String textProp = "";
					StringBuilder textContent = new StringBuilder();
					Elements textHeadingElements = doc.select("div.c00v0-pilot");
					Elements textDescriptionElements = doc.select("div.c00v1-pilot");
					if (textHeadingElements != null ) {
						textContent.append(FrameworkUtils.extractHtmlBlobContent(textHeadingElements.first(), "", locale, sb, urlMap));
					}else {
							sb.append(Constants.TEXT_ELEMENT_NOT_FOUND);
						}
					if(!textDescriptionElements.isEmpty()){
						textContent.append(FrameworkUtils.extractHtmlBlobContent(textDescriptionElements.first(), "", locale, sb, urlMap));
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
					sb.append(Constants.UNABLE_TO_MIGRATE_TEXT);
					log.error("Exception " , e);
				}
				// end of text component properties setting
				//----------------------------------------------------------------------------------------------------------------
				// start of htmlblob component
				try {
					String htmlBlobContent = "";
					StringBuilder oldImage = new StringBuilder();
					log.debug("Started migrating HtmlBlob content.");
					// Start get content.
					Element htmlBlobElements = doc.select("div.htmlblob").last();
					if (htmlBlobElements != null) {
							Element h2Ele = htmlBlobElements.select("h2").first();
							oldImage.append(h2Ele);
							oldImage.append("<table><tr>");
							Elements images = htmlBlobElements.select("td");
							for(Element ele:images)
							{
								htmlBlobContent = FrameworkUtils.extractHtmlBlobContent(ele, "", locale, sb, urlMap);
								oldImage.append(htmlBlobContent);
							}
							oldImage.append("</tr></table>");
						}
					 else {
						sb.append(Constants.HTMLBLOB_ELEMENT_NOT_FOUND);
					}
					// End get content.
					// Start set content.
					if (solutionListingMidnode.hasNode("htmlblob_0")) {
						Node htmlBlobNode = solutionListingMidnode.getNode("htmlblob_0");
						if (!StringUtils.isEmpty(htmlBlobContent)) {
							htmlBlobNode.setProperty("html",oldImage.toString());
							log.debug("HtmlBlob Content migrated is done.");
						}
					} else {
						sb.append(Constants.HTMLBLOB_NODE_NOT_FOUND);
					}
					// End get content.
				} catch (Exception e) {
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
