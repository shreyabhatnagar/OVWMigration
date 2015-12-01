package com.cisco.dse.global.migration.solutionlisting;

import java.io.IOException;
import java.util.ArrayList;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.sling.commons.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class SolutionListingVariation11 {

	Document doc;

	String title = null;

	StringBuilder sb = new StringBuilder(1024);

	String footerLinks = "{\"linktext\":\"<aaa>\",\"linkurl\":\"<bbb>\"}";

	static Logger log = Logger.getLogger(SolutionListingVariation11.class);

	public String translate(String loc, String prod, String type,
			String catType, String locale, Session session) throws IOException,
			ValueFormatException, VersionException, LockException,
			ConstraintViolationException, RepositoryException {
		BasicConfigurator.configure();
		log.debug("In the translate method");
		log.debug("In the translate method, catType is :" + catType);

		// Repo node paths
		String indexMidLeft = "/content/<locale>/"
				+ catType
				+ "/<prod>/solution-listing/jcr:content/content_parsys/solutions/layout-solutions/gd21v1/gd21v1-mid";

		String pageUrl = "http://chard.cisco.com:4502/content/<locale>/"
				+ catType + "/<prod>/solution-listing.html";

		pageUrl = pageUrl.replace("<locale>", locale).replace("<prod>", prod);

		sb.append("<td>" + "<a href=" + pageUrl + ">" + pageUrl + "</a>"
				+ "</td>");
		sb.append("<td>" + "<a href=" + loc + ">" + loc + "</a>" + "</td>");
		sb.append("<td><ul>");

		indexMidLeft = indexMidLeft.replace("<locale>", locale).replace(
				"<prod>", prod);
				Node indexMidLeftNode = null;

			try {
			indexMidLeftNode = session.getNode(indexMidLeft);

			try {
				doc = Jsoup.connect(loc).get();
			} catch (Exception e) {
				sb.append("<li>Cannot Connect to given URL. \n" + loc + "</li>");
			}

			title = doc.title();
			// start set text component.
			try {
				Elements textElements = doc.select("div.c00v0-alt1-pilot");
				if (textElements != null) {
					Node textNode = indexMidLeftNode.hasNode("text") ? indexMidLeftNode	.getNode("text"): null;
					if (textNode != null) {
						for (Element ele : textElements) {
							if (ele != null) {
								Elements textProp = ele.getElementsByTag("h2");
								log.debug("text property!: " + textProp);
								if(textProp != null){
									String textPropVal = textProp.outerHtml();
									textNode.setProperty("text", textPropVal);
								}else{
									sb.append("<li>Unable to update text component as h2 tag doesnot exist.</li>");
								}
								
							} else {
								sb.append("<li>Unable to update text component as there are no elements in the class c00-pilot.</li>");
							}

						}
					} else{
						sb.append("<li>Unable to update text component as its respective node is missing.</li>");
					}
				} else {
					sb.append("<li>Unable to update text component as its respective div is missing. c00-pilot class is missing.</li>");
				}

			} catch (Exception e) {
				sb.append("<li>Unable to update text component of solution listing."
						+ e + "</li>");
			}

			// end set text
			/*// ---------------------------------------------------------------------------------------------------------------------------------------
			// start set spotlight component.
			try {
				String h2Text = "";
				String pText = "";
				String aText = "";
				Elements spotLightElements = doc.select("div.c11-pilot");
				
				if (spotLightElements != null) {
					if (indexMidLeftNode != null) {
						int eleSize = spotLightElements.size();
						NodeIterator spoLightNodeIterator = indexMidLeftNode
								.getNodes("spotlight_large*");
						int nodeSize = (int) spoLightNodeIterator.getSize();
						if (eleSize == nodeSize) {
							for (Element ele : spotLightElements) {
								spoLightNodeIterator.hasNext();
								Node spotLightComponentNode = (Node) spoLightNodeIterator
										.next();

								Elements h2TagText = ele.getElementsByTag("h2");
								if (h2TagText != null) {
									h2Text = h2TagText.html();
								} else {
									sb.append("<li>Spotlight Component Heading element not having any title in it ('h2' is blank)</li>");
								}

								Elements descriptionText = ele
										.getElementsByTag("p");
								if (descriptionText != null) {
									pText = descriptionText.html();
								} else {
									sb.append("<li>Spotlight Component description element not having any title in it ('p' is blank)</li>");
								}

								Elements anchorText = ele.getElementsByTag("a");
								if (anchorText != null) {
									aText = anchorText.text();
								} else {
									sb.append("<li>Spotlight Component anchor tag not having any content in it ('<a>' is blank)</li>");
								}
								spotLightComponentNode.setProperty("title",
										h2Text);
								spotLightComponentNode.setProperty(
										"description", pText);
								spotLightComponentNode.setProperty("linktext",
										aText);

							}
						}

						if (nodeSize < eleSize) {
							int nodeCount = 1;

							for (Element ele : spotLightElements) {
								if (nodeCount <= nodeSize) {
									spoLightNodeIterator.hasNext();
									Node spotLightComponentNode = (Node) spoLightNodeIterator
											.next();
									Elements h2TagText = ele
											.getElementsByTag("h2");
									if (h2TagText != null) {
										h2Text = h2TagText.html();
									} else {
										sb.append("<li>Spotlight Component Heading element not having any title in it ('h2' is blank)</li>");
									}

									Elements descriptionText = ele
											.getElementsByTag("p");
									if (descriptionText != null) {
										pText = descriptionText.html();
									} else {
										sb.append("<li>Spotlight Component description element not having any title in it ('p' is blank)</li>");
									}

									Elements anchorText = ele
											.getElementsByTag("a");
									if (anchorText != null) {
										aText = anchorText.text();
									} else {
										sb.append("<li>Spotlight Component anchor tag not having any content in it ('<a>' is blank)</li>");
									}
									spotLightComponentNode.setProperty("title",
											h2Text);
									spotLightComponentNode.setProperty(
											"description", pText);
									spotLightComponentNode.setProperty(
											"linktext", aText);
									nodeCount++;

								} else {
									sb.append("<li>Unable to migrate one spotlight component. Count MisMatch.</li>");
									log.debug("Could not migrate one spotlight large node.");
								}
							}

						}
					}

				} else {
					sb.append("<li>Unable to update spotlight component as its respective div is not available.</li>");

				}
			} catch (Exception e) {
				sb.append("<li>Unable to update spotlight component." + e
						+ "</li>");
			}
			// end set spotlight nodes
*/			session.save();

		} catch (Exception e) {
			sb.append("<li>Exception as URL cannot be connected! </li>");
			log.debug("Exception as url cannot be connected: "+ e);
		}

		sb.append("</ul></td>");

		return sb.toString();
	}
}
