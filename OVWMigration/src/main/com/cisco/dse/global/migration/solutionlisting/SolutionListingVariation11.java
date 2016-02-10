package com.cisco.dse.global.migration.solutionlisting;

import java.io.IOException;
import java.util.Map;

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
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.cisco.dse.global.migration.config.BaseAction;
import com.cisco.dse.global.migration.config.Constants;
import com.cisco.dse.global.migration.config.FrameworkUtils;

public class SolutionListingVariation11 extends BaseAction {

	Document doc;

	String title = null;

	StringBuilder sb = new StringBuilder(1024);

	String footerLinks = "{\"linktext\":\"<aaa>\",\"linkurl\":\"<bbb>\"}";

	static Logger log = Logger.getLogger(SolutionListingVariation11.class);

	public String translate(String host,String loc, String prod, String type,
			String catType, String locale, Session session, Map<String, String> urlMap) throws IOException,
			ValueFormatException, VersionException, LockException,
			ConstraintViolationException, RepositoryException {
		BasicConfigurator.configure();
		log.debug("In the translate method");
		log.debug("In the translate method, catType is :" + catType);

		// Repo node paths
		String pagePropertiesPath = "/content/<locale>/"+catType+"/<prod>/solution-listing/jcr:content";
		String indexMidLeft = "/content/<locale>/"
				+ catType
				+ "/<prod>/solution-listing/jcr:content/content_parsys/solutions/layout-solutions/gd21v1/gd21v1-mid";

		String pageUrl = host + "/content/<locale>/"
				+ catType + "/<prod>/solution-listing.html";

		pageUrl = pageUrl.replace("<locale>", locale).replace("<prod>", prod);
		pagePropertiesPath = pagePropertiesPath.replace("<locale>", locale).replace("<prod>", prod);
		sb.append("<td>" + "<a href=" + pageUrl + ">" + pageUrl + "</a>"
				+ "</td>");
		sb.append("<td>" + "<a href=" + loc + ">" + loc + "</a>" + "</td>");
		sb.append("<td><ul>");

		indexMidLeft = indexMidLeft.replace("<locale>", locale).replace(
				"<prod>", prod);
		Node indexMidLeftNode = null;
		javax.jcr.Node pageJcrNode = null;
		try {
			indexMidLeftNode = session.getNode(indexMidLeft);
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


				// start set text component.
				try {
					Elements textElements = doc.select("div.c00v0-alt1-pilot");
					if(textElements.isEmpty()){
						textElements = doc.select("div.c00v0-pilot,div.cc00v0-pilot");
					}
					if (!textElements.isEmpty()) {
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
										sb.append(Constants.TEXT_DOES_NOT_EXIST);
									}

								} else {
									sb.append(Constants.CHILD_TEXT_ELEMENT_NOT_FOUND);
								}

							}
						} else{
							sb.append(Constants.TEXT_NODE_NOT_FOUND);
						}
					} else {
						sb.append(Constants.TEXT_ELEMENT_NOT_FOUND);
					}

				} catch (Exception e) {
					sb.append("<li>" + Constants.EXCEPTION_TEXT_COMPONENT
							+ e + "</li>");
				}

				try {
					int count = 0;
					boolean paragraphExists = false;
					Elements textParbaseElements = doc.select("div.text, div.parbase");
					if (!textParbaseElements.isEmpty()) {
						for (Element ele : textParbaseElements) {
							if (ele != null) {
								Elements textProp = ele.getElementsByTag("h2");
								if(StringUtils.isNotBlank(textProp.toString())){
									count++;
								}
							} 
						}
					}
						if(count == 0){

							Elements cc00Elements = doc.select("div.cc00-pilot");
							if (cc00Elements != null) {
								for (Element ele : cc00Elements) {
									if (ele != null) {
										Elements textProp = ele.getElementsByTag("h2");
										Elements paragraphElem = ele.getElementsByTag("p");
										if(StringUtils.isNotBlank(textProp.toString())){
											count++;
										}
										if(StringUtils.isNotBlank(paragraphElem.toString())){
											paragraphExists = true;
										}
									} 
								}
								if(count != 0)
									sb.append(Constants.TEXT_NODE_NOT_FOUND.replace(".", " ") + "for " +count+" sub headings and hence element is not migrated.");
								if(paragraphExists){
									sb.append("<li>Extra description found on locale page.Hence element is not migrated.</li>");
								}
							}
						}else{
							count = count-1;
							if(count != 0)
								sb.append(Constants.TEXT_NODE_NOT_FOUND.replace(".", " ") + "for " +count+" sub headings and hence element is not migrated.");
						}
				} catch (Exception e) {
					sb.append("<li>" + Constants.EXCEPTION_TEXT_COMPONENT
							+ e + "</li>");
				}

				/*try{
					Element textEle = doc.select("div.c00v0-pilot").first();
					if(textEle != null){
						if(indexMidLeftNode.hasNode("text")){
							Node textNode = indexMidLeftNode.getNode("text");
							textNode.setProperty("text", textEle.html());
						}else{
							sb.append(Constants.TEXT_NODE_NOT_FOUND);
						}
					}else{
						sb.append(Constants.TEXT_ELEMENT_NOT_FOUND);
					}
				}catch(Exception e){
					sb.append(Constants.EXCEPTION_TEXT_COMPONENT);
					log.error("Unable to migrate text component."+e);
				}
*/
				// end set text
				// ---------------------------------------------------------------------------------------------------------------------------------------
				// start set spotlight component.
				try {
					String h2Text = "";
					String pText = "";
					int imageSrcEmptyCount = 0;
					boolean extraAnchorTagExists = false;
					Elements spotLightElements = doc.select("div.c11-pilot");
					Node spotLightComponentNode = null;				
					if (spotLightElements != null) {
						if (indexMidLeftNode != null) {

							NodeIterator spoLightNodeIterator = indexMidLeftNode
									.getNodes("spotlight_medium*");
							int eleSize = spotLightElements.size();

							int nodeSize = (int) spoLightNodeIterator.getSize();
							if (eleSize != nodeSize) {
								sb.append("<li>"+Constants.EXCEPTION_SPOTLIGHT_COMPONENT+ nodeSize+Constants.SPOTLIGHT_ELEMENT_COUNT+eleSize+"</li>");

							}

							for (Element ele : spotLightElements) {
								Element h2TagText = ele.getElementsByTag("h2").first();
								if (h2TagText != null) {
									h2Text = h2TagText.text();
								} else {
									sb.append(Constants.SPOTLIGHT_HEADING_ELEMENT_NOT_FOUND);
								}

								Elements descriptionText = ele.getElementsByTag("p");
								if (descriptionText != null) {
									pText = descriptionText.html();
								} else {
									sb.append(Constants.SPOTLIGHT_DESCRIPTION_ELEMENT_NOT_FOUND);
								}

								Element anchorText = ele.getElementsByTag("a").first();
								String ahref = "";
								if (anchorText != null) {
									ahref = anchorText.absUrl("href");
									if(StringUtil.isBlank(ahref)){
										ahref = anchorText.attr("href");
									}
									// Start extracting valid href
									log.debug("Before ahref" + ahref + "\n");
									ahref = FrameworkUtils.getLocaleReference(ahref, urlMap, locale, sb, catType, type);
									log.debug("after ahref" + ahref + "\n");
									// End extracting valid href
								} else {
									sb.append(Constants.SPOTLIGHT_ANCHOR_ELEMENT_NOT_FOUND);
								}

								Elements anchorTag = ele.getElementsByTag("a");
								if (anchorTag != null) {
									for(Element e:anchorTag){
										Element parElem = e.parent();
										if(!"h2".equals(parElem.tagName())){
											extraAnchorTagExists = true;
										}
									}

								}
								// start image
								String spotLightImage = FrameworkUtils.extractImagePath(ele, sb);
								log.debug("spotLightImage " + spotLightImage + "\n");
								
								// end image
								
								if(spoLightNodeIterator.hasNext()){
									spotLightComponentNode = (Node) spoLightNodeIterator.next();
									if(spotLightComponentNode != null){
										spotLightComponentNode.setProperty("title", h2Text);
										spotLightComponentNode.setProperty("description", pText);
										if (spotLightComponentNode.hasNode("image")) {
											Node spotLightImageNode = spotLightComponentNode.getNode("image");
											String fileReference = spotLightImageNode.hasProperty("fileReference")?spotLightImageNode.getProperty("fileReference").getString():"";
											spotLightImage = FrameworkUtils.migrateDAMContent(spotLightImage, fileReference, locale,sb, catType, type);
											log.debug("spotLightImage " + spotLightImage + "\n");
											if (StringUtils.isNotBlank(spotLightImage)) {
												spotLightImageNode.setProperty("fileReference" , spotLightImage);
											}else{
												imageSrcEmptyCount++;
											}
										} else {
											sb.append("<li>spotlight image node doesn't exist</li>");
										}
										String nodeName = spotLightComponentNode.getName();
										if(nodeName.contains("v2")){									
											if(spotLightComponentNode.hasNode("titlelink")){
												Node spotLightTitleLinkNode = spotLightComponentNode.getNode("titlelink");
												spotLightTitleLinkNode.setProperty("url", ahref);
											}else{
												sb.append(Constants.SPOTLIGHT_TITLELINK_NODE_NOT_FOUND);
											}
										}else{
											spotLightComponentNode.setProperty("title-linkurl", ahref);
										}

									}else{
										sb.append(Constants.SPOTLIGHT_NODE_NOT_FOUND);

									}

								}
							}
							if(imageSrcEmptyCount > 0){
								sb.append("<li> "+imageSrcEmptyCount+" image(s) are not found on spot light component of locale page. </li>");
							}
							if(extraAnchorTagExists){
								sb.append("<li>Extra link found in spotlight component on locale page. Hence cannot be migrated.</li>");
							}
						}else{
							sb.append(Constants.SPOTLIGHT_PARENT_NODE_NOT_FOUND);
						}

					} else {
						sb.append(Constants.SPOTLIGHT_PARENT_DIV_NOT_FOUND);
					}
				} catch (Exception e) {
					sb.append("<li>" + Constants.EXCEPTION_SPOTLIGHT_COMPONENT + e
							+ "</li>");
				}
				// end set spotlight nodes
				session.save();
			}
			else {
				sb.append(Constants.URL_CONNECTION_EXCEPTION);	
			}
		} catch (Exception e) {
			sb.append("<li>unable to migrate page"+e+"</li>");
			log.debug("Exception in page migration: "+ e);
		}

		sb.append("</ul></td>");

		return sb.toString();
	}
}
