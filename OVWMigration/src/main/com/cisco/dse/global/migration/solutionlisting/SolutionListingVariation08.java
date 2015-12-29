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
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.cisco.dse.global.migration.config.BaseAction;
import com.cisco.dse.global.migration.config.Constants;
import com.cisco.dse.global.migration.config.FrameworkUtils;

public class SolutionListingVariation08 extends BaseAction {
	Document doc;

	String title = null;

	StringBuilder sb = new StringBuilder(1024);

	String footerLinks = "{\"linktext\":\"<aaa>\",\"linkurl\":\"<bbb>\"}";

	static Logger log = Logger.getLogger(SolutionListingVariation08.class);

	public String translate(String host, String loc, String prod, String type,
			String catType, String locale, Session session, Map<String, String> urlMap) throws IOException,
			ValueFormatException, VersionException, LockException,
			ConstraintViolationException, RepositoryException {
		BasicConfigurator.configure();
		log.debug("In the translate method");
		log.debug("In the translate method, catType is :" + catType);

		// Repo node paths
		String pagePropertiesPath = "/content/<locale>/"+catType+"/<prod>/solution-listing/jcr:content";
		String indexMid = "/content/<locale>/"
				+ catType
				+ "/<prod>/solution-listing/jcr:content/content_parsys/solutions/layout-solutions/gd21v1/gd21v1-mid";

		String pageUrl = host + "/content/<locale>/" + catType
				+ "/<prod>/solution-listing.html";

		pageUrl = pageUrl.replace("<locale>", locale).replace("<prod>", prod);

		sb.append("<td>" + "<a href=" + pageUrl + ">" + pageUrl + "</a>"
				+ "</td>");
		sb.append("<td>" + "<a href=" + loc + ">" + loc + "</a>" + "</td>");
		sb.append("<td><ul>");

		indexMid = indexMid.replace("<locale>", locale).replace("<prod>", prod);
		pagePropertiesPath = pagePropertiesPath.replace("<locale>", locale).replace("<prod>", prod);
		javax.jcr.Node indexMidNode = null;
		javax.jcr.Node pageJcrNode = null;
		boolean isHero = true;
		boolean isHtml = true;

		try {
			indexMidNode = session.getNode(indexMid);
			pageJcrNode = session.getNode(pagePropertiesPath);
			log.debug("Path for node:" + indexMidNode.getPath());
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

				// ---------------------------------------------------------------------------------------------------------------------------------------
				// start set text component.
				try {
					Elements textElements = doc.select("div.c00-pilot");
					if (textElements != null && !textElements.isEmpty()) {
						NodeIterator textNodeIterator = indexMidNode
								.getNodes("text*");
						if (textNodeIterator != null) {
							for (Element ele : textElements) {
								if (textNodeIterator.hasNext()) {
									Node textNode = (Node) textNodeIterator.next();
									String textProp = ele.html();
									if (StringUtils.isNotBlank(textProp)) {
										textNode.setProperty("text", textProp);
									} else {
										sb.append(Constants.TEXT_DOES_NOT_EXIST);
									}
								}
							}
						} else {
							sb.append(Constants.TEXT_NODE_NOT_FOUND);
						}
					} else {
						sb.append(Constants.TEXT_ELEMENT_NOT_FOUND);
					}

				} catch (Exception e) {
					log.error("Exception : ", e);
					sb.append(Constants.EXCEPTION_TEXT_COMPONENT);
				}

				// end set text
				// ---------------------------------------------------------------------------------
				// start set hero large component properties.

				try {
					String h2Text = "";
					String pText = "";
					String aText = "";
					String aHref = "";

					Elements heroElements = doc.select("div.frame");
					//heroElements = heroElements.select("div.c50-text");
					//Elements heroImageElements = doc.select("div.c50-image");
					Node heroNode = indexMidNode.hasNode("hero_large") ? indexMidNode
							.getNode("hero_large") : null;
							if (heroElements != null && !heroElements.isEmpty()) {
								if (heroNode != null) {
									int eleSize = heroElements.size();
									NodeIterator heroPanelNodeIterator = heroNode
											.getNodes("heropanel*");
									int nodeSize = (int) heroPanelNodeIterator.getSize();
									if (eleSize == nodeSize) {
										for (Element ele : heroElements) {
											if (heroPanelNodeIterator.hasNext()) {
												Node heroPanelNode = (Node) heroPanelNodeIterator
														.next();
												// start image
												
												String heroImage = FrameworkUtils.extractImagePath(ele, sb);
												log.debug("heroImage " + heroImage + "\n");
												if (heroPanelNode != null) {
													if (heroPanelNode.hasNode("image")) {
														Node imageNode = heroPanelNode.getNode("image");
														String fileReference = imageNode.hasProperty("fileReference")?imageNode.getProperty("fileReference").getString():"";
														heroImage = FrameworkUtils.migrateDAMContent(heroImage, fileReference, locale,sb);
														log.debug("heroImage " + heroImage + "\n");
														if (StringUtils.isNotBlank(heroImage)) {
															imageNode.setProperty("fileReference" , heroImage);
														}
													} else {
														sb.append("<li>hero image node doesn't exist</li>");
													}
												}
												
												// end image
												Elements h2TagText = ele
														.getElementsByTag("h2");
												if (h2TagText != null) {
													h2Text = h2TagText.html();
												} else {
													sb.append(Constants.HERO_CONTENT_HEADING_ELEMENT_DOESNOT_EXISTS);
												}

												Elements descriptionText = ele
														.getElementsByTag("p");
												if (descriptionText != null) {
													pText = descriptionText.first().text();
												} else {
													sb.append(Constants.HERO_CONTENT_DESCRIPTION_ELEMENT_DOESNOT_EXISTS);
												}

												Elements anchorText = ele
														.getElementsByTag("a");
												if (anchorText != null) {
													aText = anchorText.text();
													aHref = anchorText.attr("href");
												} else {
													sb.append(Constants.HERO_CONTENT_ANCHOR_ELEMENT_DOESNOT_EXISTS);
												}
												if (StringUtils.isNotBlank(h2Text)) {
													heroPanelNode.setProperty("title",
															h2Text);
												} else {
													sb.append(Constants.HERO_CONTENT_HEADING_IS_BLANK);
												}
												if (StringUtils.isNotBlank(pText)) {
													heroPanelNode.setProperty(
															"description", pText);
												} else {
													sb.append(Constants.HERO_CONTENT_DESCRIPTION_IS_BLANK);
												}

												if (StringUtils.isNotBlank(aText)) {
													heroPanelNode.setProperty("linktext",
															aText);
												} else {
													sb.append(Constants.HERO_CONTENT_ANCHOR_TEXT_IS_BLANK);
												}
												if (StringUtils.isNotBlank(aHref)) {
													heroPanelNode.setProperty("linkurl",
															aHref);
												} else {
													sb.append(Constants.HERO_CONTENT_ANCHOR_LINK_IS_BLANK);
												}
											} else {
												sb.append(Constants.HERO_CONTENT_PANEL_ELEMENT_NOT_FOUND);
											}
										}
										} else {
										log.debug("Hero component node count mismatch!");
										sb.append(Constants.HERO_CONTENT_COUNT_MISMATCH.replace("<ele>",  Integer.toString(eleSize)).replace("<node>", Integer.toString(nodeSize)));
									}
								} else {
									isHero = false;//No Hero Content Node Found.
									//sb.append(Constants.HERO_CONTENT_NODE_NOT_FOUND);
								}

							} else {
								isHero = false;//No Hero Content Node Found.
								//sb.append(Constants.HERO_CONTENT_PANEL_ELEMENT_NOT_FOUND);

							}
				} catch (Exception e) {
					log.debug("Exception : ",e);
					sb.append(Constants.EXCEPTOIN_IN_UPDATING_HERO_CONTENT);
				}

				// end set Hero Large component's title, description, link
				// text,linkurl.
				// ---------------------------------------------------------------------------------
				// start set html blob properties

				try {
					Elements htmlblobElements = doc.select("div.c50-pilot");
					String htmlBlobContent = "";
					if (htmlblobElements != null && !htmlblobElements.isEmpty()) {
						for(Element ele: htmlblobElements){
							log.debug("html blob content: "+ ele);
							htmlBlobContent = FrameworkUtils.extractHtmlBlobContent(ele, "", locale, sb, urlMap);
						}
					} else {
						isHtml = false;//No Html node content node found.
						//sb.append(Constants.HTMLBLOB_ELEMENT_NOT_FOUND);
					}

					if (indexMidNode.hasNode("htmlblob")) {
						Node htmlblobNode = indexMidNode.getNode("htmlblob");
						if (StringUtils.isNotBlank(htmlBlobContent)) {
							htmlblobNode.setProperty("html", htmlBlobContent);
						} else {
							sb.append(Constants.HTMLBLOB_CONTENT_DOES_NOT_EXIST);
						}
					} else {
						isHtml = false;//No Html node content node found.
						//sb.append(Constants.HTMLBLOB_NODE_NOT_FOUND);
					}

					if(!isHero && !isHtml){
						sb.append(Constants.NO_HERO_OR_HTMLBLOB_NOT_FOUND);
					}

				} catch (Exception e) {
					log.error("Exception : ", e);
					sb.append(Constants.EXCEPTION_IN_HTMLBLOB);
				}
				// end of set html blob properties
				//-----------------------------------------------------------------------------------------------------
				// start set spotlight medium component properties.

				try {
					Elements spotLightElements = doc.select("div.c11-pilot");
					if (spotLightElements == null || spotLightElements.isEmpty()) {
						Elements ulSpotLightElements = doc.select("div.nn12-pilot");
						for (Element ele : ulSpotLightElements) {
							Elements liElements = ele.getElementsByTag("li");
							spotLightElements.addAll(liElements);
						}
					}
					if (spotLightElements != null) {
						int eleSize = spotLightElements.size();
						NodeIterator spotLightNodeIterator = indexMidNode
								.getNodes("spotlight_medium*");
						int nodeSize = (int) spotLightNodeIterator.getSize();
						if (spotLightNodeIterator != null) {
							for (Element ele : spotLightElements) {
								String pText = "";
								String aText = "";
								String aHref = "";
								if (spotLightNodeIterator.hasNext()) {
									Node heroPanelNode = (Node) spotLightNodeIterator
											.next();
									Elements aElements = ele.getElementsByTag("a");
									if (aElements != null) {
										Element aElement = aElements.first();
										if (aElement != null) {
											aText = aElement.text();
											aHref = aElement.attr("href");
										} else {
											sb.append(Constants.SPOTLIGHT_ANCHOR_ELEMENT_NOT_FOUND);
										}
									} else {
										sb.append(Constants.SPOTLIGHT_ANCHOR_ELEMENT_NOT_FOUND);
									}
									Elements descriptionText = ele
											.getElementsByTag("p");
									if (descriptionText == null
											|| descriptionText.isEmpty()) {
										ele.getElementsByTag("h2").remove();
										descriptionText.add(ele);
									}
									if (descriptionText != null) {
										pText = descriptionText.text();
									} else{
										sb.append(Constants.SPOTLIGHT_DESCRIPTION_ELEMENT_NOT_FOUND);
									}
									// start image
									String spotLightImage = FrameworkUtils.extractImagePath(ele, sb);
									log.debug("spotLightImage " + spotLightImage + "\n");
									if (heroPanelNode.hasNode("image")) {
										Node spotLightImageNode = heroPanelNode.getNode("image");
										String fileReference = spotLightImageNode.hasProperty("fileReference")?spotLightImageNode.getProperty("fileReference").getString():"";
										spotLightImage = FrameworkUtils.migrateDAMContent(spotLightImage, fileReference, locale,sb);
										log.debug("spotLightImage " + spotLightImage + "\n");
										if (StringUtils.isNotBlank(spotLightImage)) {
											spotLightImageNode.setProperty("fileReference" , spotLightImage);
										}
									} else {
										sb.append("<li>spotlight image node doesn't exist</li>");
									}
									// end image
									if (StringUtils.isNotBlank(aText)) {
										heroPanelNode.setProperty("title", aText);
									} else {
										sb.append(Constants.SPOTLIGHT_ANCHOR_TEXT_NOT_FOUND);
									}
									if (StringUtils.isNotBlank(aHref)) {
										heroPanelNode.setProperty("title-linkurl",
												aHref);
									} else {
										sb.append(Constants.SPOTLIGHT_ANCHOR_LINK_NOT_FOUND);
									}
									if (StringUtils.isNotBlank(pText)) {
										heroPanelNode.setProperty("description",
												pText);
									} else {
										sb.append(Constants.SPOTLIGHT_DESCRIPTION_TEXT_NOT_FOUND);
									}
								} else {
									sb.append(Constants.SPOTLIGHT_NODE_NOT_FOUND);
								}
							}
						}
						if (eleSize != nodeSize) {
							log.debug("Spotlight component node count mismatch!");
							sb.append(Constants.SPOTLIGHT_NODE_COUNT
									+ nodeSize
									+ Constants.SPOTLIGHT_ELEMENT_COUNT
									+ eleSize
									+ "</li>");
						}
					} else {
						sb.append(Constants.SPOTLIGHT_ELEMENT_NOT_FOUND);
					}
				} catch (Exception e) {
					log.error("Exception : ",e);
					sb.append(Constants.EXCEPTION_SPOTLIGHT_COMPONENT);
				}
				// end set Spotlight medium component's title, description.
			}
			else{
				sb.append(Constants.URL_CONNECTION_EXCEPTION);
			}
		}
		catch (Exception e) {
			log.debug("Exception : ",e);
			sb.append(Constants.EXCEPTION_IN_SOLUTION_LISTING_CONTENT_UPDATE);
		}
		session.save();
		sb.append("</ul></td>");

		return sb.toString();
	}
}
