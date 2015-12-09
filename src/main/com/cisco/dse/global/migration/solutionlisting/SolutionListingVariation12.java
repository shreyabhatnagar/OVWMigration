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

import com.cisco.dse.global.migration.config.BaseAction;
import com.cisco.dse.global.migration.config.Constants;
import com.cisco.dse.global.migration.config.FrameworkUtils;

public class SolutionListingVariation12 extends BaseAction{


	Document doc;

	String title = null;

	StringBuilder sb = new StringBuilder(1024);

	static Logger log = Logger.getLogger(SolutionListingVariation12.class);

	public String translate(String host, String loc, String prod, String type, String catType,
			String locale, Session session) throws IOException,
			ValueFormatException, VersionException, LockException,
			ConstraintViolationException, RepositoryException {
		BasicConfigurator.configure();
		log.debug("In the translate method");
		log.debug("In the translate method, catType is :"+ catType);

		// Repo node paths
		String pagePropertiesPath = "/content/<locale>/"+catType+"/<prod>/solution-listing/jcr:content";
		String solutionListingParsysPath = "/content/<locale>/"+catType+"/<prod>/solution-listing/jcr:content/content_parsys/solutions/layout-solutions/gd21v1/gd21v1-mid";
		String pageUrl = host + "/content/<locale>/"+catType+"/<prod>/solution-listing.html";

		pageUrl = pageUrl.replace("<locale>", locale).replace("<prod>", prod);
		pagePropertiesPath = pagePropertiesPath.replace("<locale>", locale).replace("<prod>", prod);
		sb.append("<td>" + "<a href="+pageUrl+">"+pageUrl+"</a>"+"</td>");
		sb.append("<td>" + "<a href="+loc+">"+loc +"</a>"+ "</td>");
		sb.append("<td><ul>");


		solutionListingParsysPath = solutionListingParsysPath.replace("<locale>", locale).replace("<prod>", prod);

		javax.jcr.Node solutionListingParsysNode = null;
		javax.jcr.Node pageJcrNode = null;
		try {
			solutionListingParsysNode = session.getNode(solutionListingParsysPath);
			pageJcrNode = session.getNode(pagePropertiesPath);
			try {
				doc = Jsoup.connect(loc).get();
			} catch (Exception e) {
				doc = getConnection(loc);
			}
			if(doc != null){
				title = doc.title();

				// ------------------------------------------------------------------------------------------------------------------------------------------
				// start set page properties.

				FrameworkUtils.setPageProperties(pageJcrNode, doc, session, sb);

				// end set page properties.
				// ------------------------------------------------------------------------------------------------------------------------------------------


				// ------------------------------------------------------------------------------------------------------------------------------------------
				// start set text component content.
				javax.jcr.Node textNode = null;
				if (solutionListingParsysNode != null && solutionListingParsysNode.hasNode("text")) {
					textNode = solutionListingParsysNode.getNode("text");
				} else {
					log.debug("unable to find node to set content/text components are not present");
				}
				String text = "";
				try {
					Elements gdRightElements = doc.select("div.gd-right");
					if (gdRightElements != null) {
						Element gdRightElement = gdRightElements.first();
						if (gdRightElement != null) {
							Elements textElements = gdRightElement.getElementsByTag("h1");
							if (textElements != null) {
								Element textElement = textElements.first();
								if (textElement != null) {
									text = textElement.outerHtml();
								}
							}
							if (textNode != null) {
								if (StringUtils.isNotBlank(text)) {
									textNode.setProperty("text", text);
									System.out.println("Text component is "+text);
								} else {
									sb.append(Constants.TEXT_DOES_NOT_EXIST); // Text content is not available on the web publisher page.
								}
							} else {
								sb.append(Constants.TEXT_NODE_NOT_FOUND);
							}
						}
					}
				} catch (Exception e) {
					log.debug("<li>Unable to update text component."+e+"</li>");
				}

				// end set text component content.
				// ---------------------------------------------------------------------------------------------------------------------------------------

				// ------------------------------------------------------------------------------------------------------------------------------------------
				// start set htmlblob component content.
				//Var 12 modification - rohan - start
				NodeIterator htmlBlobIterator = null;
				javax.jcr.Node htmlBlobNode = null;
				if (solutionListingParsysNode != null && solutionListingParsysNode.hasNodes()) {
					htmlBlobIterator = solutionListingParsysNode.getNodes("htmlblob*");
				} else {
					log.debug("unable to find node to set content/htmlblob components are not present");
				}
				try {
					Elements htmlblobElements = doc.select("div.gd-right").select("div.c39");
					if (htmlblobElements != null) {
						if (htmlblobElements.size() != htmlBlobIterator.getSize()) {
							sb.append("<li>Number of nodes("
									+ htmlBlobIterator.getSize()
									+ ") and elements("
									+ htmlblobElements.size()
									+ ") of html blob component doesn't match.</li>"); 
						}
						for (Element htmlblobElement : htmlblobElements) {
							String htmlblobtext = htmlblobElement.outerHtml();

							if (htmlBlobIterator != null && htmlBlobIterator.hasNext())
								htmlBlobNode = htmlBlobIterator.nextNode();
							if (htmlBlobNode != null) {
								if (StringUtils.isNotBlank(htmlblobtext)) {
									log.debug("Html blob component is "+htmlblobtext);
									htmlBlobNode.setProperty("html", htmlblobtext);
								} else {
									sb.append(Constants.HTMLBLOB_CONTENT_DOES_NOT_EXIST);
								}
							} else {
								sb.append(Constants.HTMLBLOB_NODE_DOES_NOT_EXIST);
							}
						}

					} else {
						sb.append(Constants.HTMLBLOB_ELEMENT_NOT_FOUND);
					}
				} catch (Exception e) {
					log.debug("Unable to update htmlblob component.",e);
				}
				//Var 12 modification - rohan - end
				// end set htmlblob component content.
				// ---------------------------------------------------------------------------------------------------------------------------------------

				//Start of second text component
				javax.jcr.Node text0Node = null;
				if (solutionListingParsysNode.hasNode("text_0")) {
					text0Node = solutionListingParsysNode.getNode("text_0");
				}
				String secondtext = "";
				try {
					Elements gdRightElements = doc.select("div.gd-right");
					if (gdRightElements != null) {
						Element gdRightElement = gdRightElements.first();
						if (gdRightElement != null) {
							if (gdRightElement.select("div.c00-pilot").size() > 0) {
								Elements textElements = gdRightElements.select("div.c00-pilot");
								if (textElements != null) {
									int count = 0;
									for (Element textElement :textElements) {
										if (count == 0) {
											count = count +1 ;
											continue;
										}
										secondtext = textElement.outerHtml();
									}
								}
							} else {

								Elements pElements = gdRightElement.getElementsByTag("p");
								Elements h3Elements = gdRightElement.getElementsByTag("h3");
								if (pElements != null) {
									int pCount = 0;

									for (Element pElement : pElements) {
										if (pCount == 0) {
											pCount = pCount + 1;
											continue;
										}
										if (pElement.hasClass("cta-link")) {
											continue;
										}
										secondtext = secondtext + pElement.outerHtml();
										int h3Count = 1;
										for (Element h3Element : h3Elements) {
											if (h3Count == pCount)
												secondtext = secondtext + h3Element.outerHtml();
											h3Count = h3Count + 1;
										}
										pCount = pCount + 1;
									}

								}
							}
						}
						if (text0Node != null) {
							if (StringUtils.isNotBlank(secondtext)) {
								text0Node.setProperty("text", secondtext);
								log.debug("Text component is*********** "+secondtext);
							} else {
								sb.append(Constants.TEXT_DOES_NOT_EXIST); // Text content is not available on the web publisher page.
							}
						} else {
							sb.append(Constants.TEXT_NODE_NOT_FOUND);
						}

					}
				} catch (Exception e) {
					log.debug("<li>Unable to update text component."+e+"</li>");
				}

				//End of second text component
				session.save();
			}
			else {
				sb.append(Constants.URL_CONNECTION_EXCEPTION);
			}

		} catch (Exception e) {
			log.debug("Exception ", e);
		}

		sb.append("</ul></td>");

		return sb.toString();
	}

}
