package com.cisco.dse.global.migration.servicelisting;

import java.io.IOException;

import javax.jcr.Node;
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
import com.cisco.dse.global.migration.config.FrameworkUtils;

public class ServiceListingVariation03 {

	Document doc;
	StringBuilder sb = new StringBuilder(1024);
	static Logger log = Logger.getLogger(ServiceListingVariation03.class);

	public String translate(String host, String loc, String prod, String type,
			String catType, String locale, Session session) throws IOException,
			ValueFormatException, VersionException, LockException,
			ConstraintViolationException, RepositoryException {
		BasicConfigurator.configure();
		log.debug("In the translate method of ServiceListingVariation03");
		log.debug("In the translate method, catType is :" + catType);

		// Repo node paths
		String pagePropertiesPath = "/content/<locale>/"+catType+"/<prod>/service-listing/jcr:content";
		String pageUrl = host + "/content/<locale>/" + catType
				+ "/<prod>/service-listing.html";
		pageUrl = pageUrl.replace("<locale>", locale).replace("<prod>", prod);
		pagePropertiesPath = pagePropertiesPath.replace("<locale>", locale).replace("<prod>", prod);
		sb.append("<td>" + "<a href=" + pageUrl + ">" + pageUrl + "</a>"
				+ "</td>");
		sb.append("<td>" + "<a href=" + loc + ">" + loc + "</a>" + "</td>");
		sb.append("<td><ul>");

		String serviceListing = "/content/<locale>/"
				+ catType
				+ "/<prod>/service-listing/jcr:content/content_parsys/services/layout-services/gd21v1/gd21v1-mid";

		serviceListing = serviceListing.replace("<locale>", locale).replace(
				"<prod>", prod);

		javax.jcr.Node serviceListingMidnode = null;
		javax.jcr.Node pageJcrNode = null;
		try {

			serviceListingMidnode = session.getNode(serviceListing);
			pageJcrNode = session.getNode(pagePropertiesPath);
			try {
				doc = Jsoup.connect(loc).get();
			} catch (Exception e) {
				log.error("Exception : ", e);
				sb.append("<li>Cannot Connect to given URL. \n" + loc + "</li>");
			}

			// ------------------------------------------------------------------------------------------------------------------------------------------
			// start set page properties.
			
			FrameworkUtils.setPageProperties(pageJcrNode, doc, session, sb);
			
			// end set page properties.
			// ------------------------------------------------------------------------------------------------------------------------------------------
						
			
			// ----------------------------------------------------------------------------------
			// start of text component properties setting

			try {
				String textProp = "";
				Elements textElements = doc.select("div.c00-pilot");
				if (textElements != null) {
					textProp = textElements.html();

					Node textNode = serviceListingMidnode.hasNode("text") ? serviceListingMidnode
							.getNode("text") : null;
					if (textNode != null) {
						if (!StringUtils.isEmpty(textProp)) {
							textNode.setProperty("text", textProp);
						} else {
							sb.append(Constants.TEXT_DOES_NOT_EXIST);
						}
					} else {
						sb.append(Constants.TEXT_NODE_NOT_FOUND);
					}
				} else {
					sb.append(Constants.TEXT_ELEMENT_NOT_FOUND);
				}

			} catch (Exception e) {
				sb.append("<li>" + Constants.EXCEPTION_TEXT_COMPONENT + e
						+ "</li>");
			}
			// end of text component properties setting
			// ---------------------------------------------------------------------------------------------------------------------------------------
			// start set spotlight component.
			try {
				String h2Text = "";
				String pText = "";
				String aText = "";
				String aHref = "";
				Elements spotLightElements = doc.select("div.c11-pilot");

				if (spotLightElements != null) {
					if (serviceListingMidnode != null) {

						int eleSize = spotLightElements.size();

						NodeIterator spoLightNodeIterator = serviceListingMidnode
								.getNodes("spotlight_medium*");

						int nodeSize = (int) spoLightNodeIterator.getSize();
						if (eleSize != nodeSize) {
							sb.append("<li>"
									+ Constants.EXCEPTION_SPOTLIGHT_COMPONENT
									+ nodeSize
									+ Constants.SPOTLIGHT_ELEMENT_COUNT
									+ eleSize + "</li>");

						}
						// Copy of content
						for (Element ele : spotLightElements) {
							spoLightNodeIterator.hasNext();
							Node spotLightComponentNode = (Node) spoLightNodeIterator
									.next();

							Elements h2TagText = ele.getElementsByTag("h2");
							if (h2TagText != null) {
								h2Text = h2TagText.html();
							} else {
								sb.append(Constants.SPOTLIGHT_HEADING_ELEMENT_NOT_FOUND);
							}

							Elements descriptionText = ele.getElementsByTag("p");
							if (descriptionText != null) {
								pText = descriptionText.html();
							} else {
								sb.append(Constants.SPOTLIGHT_DESCRIPTION_ELEMENT_NOT_FOUND);
							}

							Elements anchorText = ele.getElementsByTag("a");
							if (anchorText != null) {
								aText = anchorText.text();
								aHref = anchorText.attr("href");
							} else {
								sb.append(Constants.SPOTLIGHT_ANCHOR_ELEMENT_NOT_FOUND);
							}
							if (!StringUtils.isEmpty(h2Text)) {
								spotLightComponentNode.setProperty("title",h2Text);
							} else {
								sb.append(Constants.SPOTLIGHT_HEADING_TEXT_NOT_FOUND);
							}
							if (!StringUtils.isEmpty(pText)) {
								spotLightComponentNode.setProperty(
										"description", pText);
							} else {
								sb.append(Constants.SPOTLIGHT_DESCRIPTION_TEXT_NOT_FOUND);
							}
							if (!StringUtils.isEmpty(aText)) {
								spotLightComponentNode.setProperty("linktext",aText);
							} else {
								sb.append(Constants.SPOTLIGHT_ANCHOR_TEXT_NOT_FOUND);
							}
							if (!StringUtils.isEmpty(aHref)) {
								spotLightComponentNode.setProperty("linkurl",aHref);
							} else {
								sb.append(Constants.SPOTLIGHT_ANCHOR_LINK_NOT_FOUND);
							}

						}

					} else {
						sb.append(Constants.SPOTLIGHT_NODE_NOT_FOUND);
					}

				} else {
					sb.append(Constants.SPOTLIGHT_ELEMENT_NOT_FOUND);

				}
			} catch (Exception e) {
				sb.append("<li>" + Constants.EXCEPTION_SPOTLIGHT_COMPONENT + e
						+ "</li>");
			}
			// end set spotlight nodes
			// ---------------------------------------------------------------------------------------------------------------------------------------

			session.save();
		} catch (Exception e) {
			sb.append(Constants.URL_CONNECTION_EXCEPTION);
			log.debug("Exception as url cannot be connected: " + e);
		}

		sb.append("</ul></td>");

		return sb.toString();

	}

}
