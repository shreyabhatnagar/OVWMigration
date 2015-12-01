package com.cisco.dse.global.migration.solutionlisting;

import java.io.IOException;

import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.cisco.dse.global.migration.config.Constants;

public class SolutionListingVariation2 {


	Document doc;

	String title = null;

	StringBuilder sb = new StringBuilder(1024);
	
	static Logger log = Logger.getLogger(SolutionListingVariation2.class);

	public String translate(String host, String loc, String prod, String type, String catType,
			String locale, Session session) throws IOException,
			ValueFormatException, VersionException, LockException,
			ConstraintViolationException, RepositoryException {
		BasicConfigurator.configure();
		log.debug("In the translate method");
		log.debug("In the translate method, catType is :"+ catType);

		// Repo node paths
		String solutionListingParsysPath = "/content/<locale>/"+catType+"/<prod>/solution-listing/jcr:content/content_parsys/solutions/layout-solutions/gd21v1/gd21v1-mid";
		String pageUrl = host + "/content/<locale>/"+catType+"/<prod>/solution-listing.html";
		
		pageUrl = pageUrl.replace("<locale>", locale).replace("<prod>", prod);
		
		sb.append("<td>" + "<a href="+pageUrl+">"+pageUrl+"</a>"+"</td>");
		sb.append("<td>" + "<a href="+loc+">"+loc +"</a>"+ "</td>");
		sb.append("<td><ul>");


		solutionListingParsysPath = solutionListingParsysPath.replace("<locale>", locale).replace("<prod>", prod);

		javax.jcr.Node solutionListingParsysNode = null;
		try {
			solutionListingParsysNode = session.getNode(solutionListingParsysPath);

			try {
				doc = Jsoup.connect(loc).get();
			} catch (Exception e) {
				sb.append("<li>Cannot Connect to given URL. \n"+loc+"</li>");
			}

			title = doc.title();
			// ------------------------------------------------------------------------------------------------------------------------------------------
			// start set text component content.
			NodeIterator textNodesIterator = null;
			javax.jcr.Node textNode = null;
			if (solutionListingParsysNode != null && solutionListingParsysNode.hasNodes()) {
				textNodesIterator = solutionListingParsysNode.getNodes("text*");
			} else {
				log.debug("unable to find node to set content/text components are not present");
			}
			try {
				Elements textElements = doc.select("div.c00-pilot, div.cc00-pilot");
				if (textElements != null) {
					if (textElements.size() != textNodesIterator.getSize()) {
						sb.append("<li>Number of nodes("
								+ textNodesIterator.getSize()
								+ ") and elements("
								+ textElements.size()
								+ ") of text component doesn't match.</li>");
					}
					for (Element textElement : textElements) {
						String text = "";
						text = textElement.outerHtml();
						if (textNodesIterator != null && textNodesIterator.hasNext())
							textNode = textNodesIterator.nextNode();
						if (textNode != null) {
							if (StringUtils.isNotBlank(text)) {
								textNode.setProperty("text", text);
								System.out.println("Text component is "+text);
							} else {
								sb.append(Constants.TEXT_DOES_NOT_EXIST); // Text content is not available on the web publisher page.
							}
						}
					}
					
				} else {
					sb.append(Constants.TEXT_ELEMENT_NOT_FOUND);
				}
			} catch (Exception e) {
					log.debug("<li>Unable to update text component."+e+"</li>");
			}
			// end set text component content.
			// ---------------------------------------------------------------------------------------------------------------------------------------
			
			// ------------------------------------------------------------------------------------------------------------------------------------------
			// start set htmlblob component content.
			NodeIterator htmlBlobIterator = null;
			javax.jcr.Node htmlBlobNode = null;
			if (solutionListingParsysNode != null && solutionListingParsysNode.hasNodes()) {
				htmlBlobIterator = solutionListingParsysNode.getNodes("htmlblob*");
			} else {
				log.debug("unable to find node to set content/htmlblob components are not present");
			}
			try {
				Elements htmlblobElements = doc.select("div.c11-pilot, div.c39");
				if (htmlblobElements != null) {
					if (htmlblobElements.size() != htmlBlobIterator.getSize()) {
						sb.append("<li>Number of nodes("
								+ htmlBlobIterator.getSize()
								+ ") and elements("
								+ htmlblobElements.size()
								+ ") of html blob component doesn't match.</li>"); 
					}
					for (Element htmlblobElement : htmlblobElements) {
						Element htmlBlobParent = htmlblobElement.parent();
						if (htmlblobElement.hasClass("c39")) {
							if (htmlBlobParent.hasClass("cc00-pilot")) {
								sb.append(Constants.HTMLBLOB_CONTENT_DOES_NOT_EXIST);
								continue;
							}
						}
						
						String htmlblobtext = "";
						htmlblobtext = htmlblobElement.outerHtml();
						if (htmlBlobIterator != null && htmlBlobIterator.hasNext())
							htmlBlobNode = htmlBlobIterator.nextNode();
						if (htmlBlobNode != null) {
							if (StringUtils.isNotBlank(htmlblobtext)) {
								System.out.println("Html blob component is "+htmlblobtext);
								htmlBlobNode.setProperty("html", htmlblobtext);
							} else {
								sb.append(Constants.HTMLBLOB_CONTENT_DOES_NOT_EXIST);
							}
						}
					}
					
				} else {
					sb.append(Constants.HTMLBLOB_ELEMENT_NOT_FOUND);
				}
			} catch (Exception e) {
					log.debug("<li>Unable to update htmlblob component."+e+"</li>");
			}
			// end set htmlblob component content.
			// ---------------------------------------------------------------------------------------------------------------------------------------

			session.save();
			

		} catch (Exception e) {
			log.debug("Exception ", e);
		}
		
		sb.append("</ul></td>");
				
		return sb.toString();
	}

}
