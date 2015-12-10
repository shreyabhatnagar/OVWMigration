package com.cisco.dse.global.migration.productlanding;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
import org.apache.sling.commons.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.cisco.dse.global.migration.config.BaseAction;
import com.cisco.dse.global.migration.config.Constants;
import com.cisco.dse.global.migration.config.FrameworkUtils;

public class ProductLandingVariation5 extends BaseAction {

	Document doc;
	StringBuilder sb = new StringBuilder(1024);
	static Logger log = Logger.getLogger(ProductLandingVariation5.class);

	public String translate(String host, String loc, String prod, String type,
			String catType, String locale, Session session) throws IOException,
			ValueFormatException, VersionException, LockException,
			ConstraintViolationException, RepositoryException {
		BasicConfigurator.configure();
		log.debug("In the translate method of ProductLandingVariation2");
		log.debug("In the translate method, catType is :" + catType);

		// Repo node paths
		String pagePropertiesPath = "/content/<locale>/"+catType+"/<prod>/index/jcr:content";
		String layoutOverView = "/content/<locale>/"
				+ catType
				+ "/<prod>/index/jcr:content/content_parsys/overview_alt1/layout-overview-alt1";
		String pageUrl = host + "/content/<locale>/" + catType
				+ "/<prod>/index.html";

		pageUrl = pageUrl.replace("<locale>", locale).replace("<prod>", prod);
		pagePropertiesPath = pagePropertiesPath.replace("<locale>", locale).replace("<prod>", prod);
		sb.append("<td>" + "<a href=" + pageUrl + ">" + pageUrl + "</a>"
				+ "</td>");
		sb.append("<td>" + "<a href=" + loc + ">" + loc + "</a>" + "</td>");
		sb.append("<td><ul>");

		layoutOverView = layoutOverView.replace("<locale>", locale).replace(
				"<prod>", prod);
		layoutOverView = layoutOverView.replace("<locale>", locale).replace(
				"<prod>", prod);
		log.debug("layoutOverView : " + layoutOverView);
		javax.jcr.Node layoutOverViewNode = null;
		javax.jcr.Node pageJcrNode = null;
		try {
			layoutOverViewNode = session.getNode(layoutOverView);
			pageJcrNode = session.getNode(pagePropertiesPath);
			try {
				doc = Jsoup.connect(loc).get();
				log.debug("Connected to the provided URL");
			} catch (Exception e) {
				doc = getConnection(loc);
			}

			// ------------------------------------------------------------------------------------------------------------------------------------------
			// start set page properties.

			FrameworkUtils.setPageProperties(pageJcrNode, doc, session, sb);

			// end set page properties.
			// ------------------------------------------------------------------------------------------------------------------------------------------

			if (doc != null) {
				Elements gd_leftelements = doc.select("div.gd-left");
				Elements gd_rightElements = doc.select("div.gd-right");
				Elements c100_pilotElements = doc.select("div.c100-pilot");
				Elements gd14v1Elements = doc.select("div.gd14v1");
				Elements gd13v2Elements = doc.select("div.gd13v2");
				if ((gd_leftelements != null && !gd_leftelements.isEmpty())
						|| (gd_rightElements != null && !gd_rightElements.isEmpty())
						|| (c100_pilotElements != null && !c100_pilotElements.isEmpty())
						|| (gd14v1Elements != null && !gd14v1Elements.isEmpty())
						|| (gd13v2Elements != null && !gd13v2Elements.isEmpty())) {
					// ------------------------------------------------------------------------------------------------------------------------------------------
					// start of hero panel section.
					try {
						String h2Text = "";
						String pText = "";
						String aText = "";
						String aHref = "";
						String parbaseContent = "";
						Elements gridLeftElements = doc.select("div.gd-left");
						if (gridLeftElements != null) {
							Elements textElements = gridLeftElements
									.select("div.c50-text");
							if (textElements != null) {
								Element textElement = textElements.first();
								if (textElement != null) {
									Elements h2TagElements = textElement
											.getElementsByTag("h2");
									if (h2TagElements != null) {
										Element h2TagElement = h2TagElements
												.first();
										if (h2TagElement != null) {
											h2Text = h2TagElement.text();
										} else {
											sb.append("<li>Hero Panel Heding element not having any title in it ('h2' is blank)</li>");
										}
									} else {
										sb.append("<li>Hero Panel Heading element not found ('h2' tag not found in 'div.c50-text' div element)</li>");
									}
									Elements pTagElements = textElement
											.getElementsByTag("p");
									if (pTagElements != null) {
										Element pTagElement = pTagElements
												.first();
										if (pTagElement != null) {
											pText = pTagElement.text();
										} else {
											sb.append("<li>Hero Panel Paragraph element is not having any paragraph in it ('p' is blank)</li>");
										}
									} else {
										sb.append("<li>Hero Panel Paragraph element not found ('p' tag not found in 'div.c50-text' div element)</li>");
									}
									Elements aTagElements = textElement
											.getElementsByTag("a");
									if (aTagElements != null) {
										Element aTagElement = aTagElements
												.first();
										if (aTagElement != null) {
											aText = aTagElement.text();
											aHref = aTagElement.attr("href");
										} else {
											sb.append("<li>No anchor tag found in 'div.c50-text' div element</li>");
										}
									} else {
										sb.append("<li>No anchor tags found in 'div.c50-text' div element</li>");
									}

								} else {
									sb.append("<li>Hero Panel text element not found ('div.c50-text' elements exists but size of the elements is zero)</li>");
								}
							} else {
								sb.append("<li>Hero Panel text elements not found ('div.c50-text' class not found in the document)</li>");
							}
							Elements parbaseTextElements = gridLeftElements
									.select("div.parbase");
							if (parbaseTextElements != null) {
								parbaseTextElements = parbaseTextElements
										.select("div.c100-pilot");
								if (parbaseTextElements != null) {
									Element parbaseTextElement = parbaseTextElements
											.first();
									if (parbaseTextElement != null) {
										parbaseContent = parbaseTextElement
												.html();
									} else {
										sb.append("<li>No parbse text found.('div.parbase' element not found in 'div.gd-left')</li>");
									}
								} else {
									sb.append("<li>No parbse text found.('div.text parbase section' element not found in 'div.gd-left')</li>");
								}
							} else {
								sb.append("<li>No parbse text found.('div.text parbase section' element not found in 'div.gd-left')</li>");
							}

						} else {
							sb.append("<li>Hero panel not found. ('div.gd-left' class not found in the document)</li>");
						}

						Node gd12v2 = null;
						Node gd12v2_left = null;
						Node hero_large = null;
						Node heroPanelNode = null;
						if (layoutOverViewNode != null) {
							if (layoutOverViewNode.hasNode("gd12v2")) {
								gd12v2 = layoutOverViewNode.getNode("gd12v2");
								if (gd12v2.hasNode("gd12v2-left")) {
									gd12v2_left = gd12v2.getNode("gd12v2-left");
									if (gd12v2_left.hasNode("hero_large")) {
										hero_large = gd12v2_left
												.getNode("hero_large");
										NodeIterator heroPanelIterator = hero_large
												.getNodes("heropanel*");
										if (heroPanelIterator.hasNext()) {
											heroPanelNode = (Node) heroPanelIterator
													.next();
											if (StringUtils.isNotBlank(h2Text)) {
												heroPanelNode.setProperty(
														"title", h2Text);
											} else {
												sb.append("<li>h2 Text is blank in hero panel.</li>");
											}
											if (StringUtils.isNotBlank(pText)) {
												heroPanelNode.setProperty(
														"description", pText);
											} else {
												sb.append("<li>p Text is blank in hero panel.</li>");
											}
											if (StringUtils.isNotBlank(aText)) {
												heroPanelNode.setProperty(
														"linktext", aText);
											} else {
												sb.append("<li>p a href text is blank in hero panel.</li>");
											}
											if (StringUtils.isNotBlank(aHref)) {
												heroPanelNode.setProperty(
														"linkurl", aHref);
											} else {
												sb.append("<li>p a href ulr is blank in hero panel.</li>");
											}
										} else {
											sb.append("<li>Node with name 'heropanel*' doesn't exist under "
													+ hero_large.getPath()
													+ "</li>");
										}
									} else {
										sb.append("<li>Node with name 'hero_large' doesn't exist under "
												+ gd12v2_left.getPath()
												+ "</li>");
									}
									if (gd12v2_left.hasNode("text")) {
										Node textNode = gd12v2_left
												.getNode("text");
										if (StringUtils
												.isNotBlank(parbaseContent)) {
											textNode.setProperty("text",
													parbaseContent);
										} else {
											sb.append("<li>parbase content in left rail is blank.</li>");
										}
									} else {
										sb.append("<li>text node doesn't exist under : "
												+ gd12v2_left.getPath()
												+ "</li>");
									}
								} else {
									sb.append("<li>Node with name 'gd12v2-left' doesn't exist under "
											+ gd12v2.getPath() + "</li>");
								}
							} else {
								sb.append("<li>Node with name 'gd12v2' doesn't exist under "
										+ layoutOverView + "</li>");
							}
						} else {
							sb.append("<li>Node doesn't exist with path : "
									+ layoutOverView + "</li>");
						}

					} catch (Exception e) {
						log.error("Exception : ", e);
						sb.append("<li>Unable to update benefits text component."
								+ e + "</li>");
					}

					// end of hero panel.
					// ---------------------------------------------------------------------------------------------------------------------------------------
					// start of primary CTA section.
					try {
						String h3Text = "";
						String pText = "";
						String aText = "";
						String aHref = "";
						String h2Content = "";
						List<String> list = new ArrayList<String>();
						Elements gridRightElements = doc.select("div.gd-right");
						if (gridRightElements != null) {
							Elements primaryCTAElements = gridRightElements
									.select("div.c47-pilot");
							if (primaryCTAElements != null) {
								Element primaryCTAElement = primaryCTAElements
										.first();
								if (primaryCTAElement != null) {
									Elements h3TagElements = primaryCTAElement
											.getElementsByTag("h3");
									if (h3TagElements != null) {
										Element h3TagElement = h3TagElements
												.first();
										if (h3TagElement != null) {
											h3Text = h3TagElement.text();
										} else {
											sb.append("<li>Primary CTA Heding element not having any title in it ('h2' is blank)</li>");
										}
									} else {
										sb.append("<li>Primary CTA Heading element not found ('h2' tag not found in 'div.c50-text' div element)</li>");
									}
									Elements pTagElements = primaryCTAElement
											.getElementsByTag("p");
									if (pTagElements != null) {
										Element pTagElement = pTagElements
												.first();
										if (pTagElement != null) {
											pText = pTagElement.text();
										} else {
											sb.append("<li>Primary CTA Paragraph element is not having any paragraph in it ('p' is blank)</li>");
										}
									} else {
										sb.append("<li>Primary CTA Paragraph element not found ('p' tag not found in 'div.c50-text' div element)</li>");
									}
									Elements aTagElements = primaryCTAElement
											.getElementsByTag("a");
									if (aTagElements != null) {
										Element aTagElement = aTagElements
												.first();
										if (aTagElement != null) {
											aText = aTagElement.text();
											aHref = aTagElement.attr("href");
										} else {
											sb.append("<li>No anchor tag found in 'div. c47-pilot' div element</li>");
										}
									} else {
										sb.append("<li>No anchor tags found in 'div. c47-pilot' div element</li>");
									}

								} else {
									sb.append("<li>Hero Panel text element not found ('div.c50-text' elements exists but size of the elements is zero)</li>");
								}
							} else {
								sb.append("<li>Hero Panel text elements not found ('div.c50-text' class not found in the document)</li>");
							}
							Elements rightRailPilotElements = gridRightElements
									.select("div.s14-pilot");
							if (rightRailPilotElements != null) {
								Element rightRailPilotElement = rightRailPilotElements
										.first();
								if (rightRailPilotElement != null) {
									Elements h2Elements = rightRailPilotElement
											.getElementsByTag("h2");
									if (h2Elements != null) {
										Element h2Element = h2Elements.first();
										h2Content = h2Element.text();
									} else {
										sb.append("<li>h2 of right rail with class 'div.s14-pilot' is blank.</li>");
									}
									Elements liElements = rightRailPilotElement
											.getElementsByTag("li");
									for (Element ele : liElements) {
										JSONObject obj = new JSONObject();
										String icon = ele.attr("class");
										obj.put("icon", icon);
										Elements aElements = ele
												.getElementsByTag("a");
										if (aElements != null) {
											Element aElement = aElements
													.first();
											String title = aElement
													.attr("title");
											String href = aElement.attr("href");
											obj.put("linktext", title);
											obj.put("linkurl", href);
										} else {
											sb.append("<li>No anchor tag found in the right rail social links</li>");
										}
										list.add(obj.toString());
									}
								} else {
									sb.append("<li>right rail with class 'div.s14-pilot' is blank.</li>");
								}
							} else {
								sb.append("<li>No pilot found on right rail with class 'div.s14-pilot'</li>");
							}

						} else {
							sb.append("<li>Hero panel not found. ('div.gd-left' class not found in the document)</li>");
						}

						Node gd12v2 = null;
						Node gd12v2_right = null;
						Node primary_cta_v2 = null;
						if (layoutOverViewNode != null) {
							if (layoutOverViewNode.hasNode("gd12v2")) {
								gd12v2 = layoutOverViewNode.getNode("gd12v2");
								if (gd12v2.hasNode("gd12v2-right")) {
									gd12v2_right = gd12v2
											.getNode("gd12v2-right");
									if (gd12v2_right.hasNode("primary_cta_v2")) {
										primary_cta_v2 = gd12v2_right
												.getNode("primary_cta_v2");
										if (StringUtils.isNotBlank(h3Text)) {
											primary_cta_v2.setProperty("title",
													h3Text);
											log.debug(h3Text
													+ "is set to the property title at : "
													+ primary_cta_v2.getPath());
										} else {
											sb.append("<li>h3 text is blank in primary cta.</li>");
										}
										if (StringUtils.isNotBlank(pText)) {
											primary_cta_v2.setProperty(
													"description", pText);
											log.debug(pText
													+ "is set to the property title at : "
													+ primary_cta_v2.getPath());
										} else {
											sb.append("<li>p text is blank in primary cta.</li>");
										}
										if (StringUtils.isNotBlank(aText)) {
											primary_cta_v2.setProperty(
													"linktext", aText);
											log.debug(aText
													+ "is set to the property title at : "
													+ primary_cta_v2.getPath());
										} else {
											sb.append("<li>anchor text is blank in primary cta.</li>");
										}
										if (StringUtils.isNotBlank(aHref)) {
											if (primary_cta_v2
													.hasNode("linkurl")) {
												Node linkurlNode = primary_cta_v2
														.getNode("linkurl");
												linkurlNode.setProperty("url",
														aHref);
												log.debug(aHref
														+ "is set to the property title at : "
														+ primary_cta_v2
														.getPath());
											} else {
												sb.append("<li>linkurl node doesn't exists under : "
														+ primary_cta_v2
														.getPath()
														+ "</li>");
											}
										} else {
											sb.append("<li>anchor href is blank for primary cta.</li>");
										}
									} else {
										sb.append("<li>Node with name 'hero_large' doesn't exist under "
												+ gd12v2_right.getPath()
												+ "</li>");
									}
									if (gd12v2_right.hasNode("followus")) {
										Node followus = gd12v2_right
												.getNode("followus");
										if (StringUtils.isNotBlank(h2Content)) {
											followus.setProperty("title",
													h2Content);
										} else {
											sb.append("<li>No title found at right rail social media piolot.</li>");
										}

										if (list.size() > 1) {
											followus.setProperty("links", list
													.toArray(new String[list
													                    .size()]));
										}

									} else {
										sb.append("<li>No 'followus' node found under "
												+ gd12v2_right.getPath()
												+ "</li>");
									}

								} else {
									sb.append("<li>Node with name 'gd12v2-left' doesn't exist under "
											+ gd12v2.getPath() + "</li>");
								}
							} else {
								sb.append("<li>Node with name 'gd12v2' doesn't exist under "
										+ layoutOverView + "</li>");
							}
						} else {
							sb.append("<li>Node doesn't exist with path : "
									+ layoutOverView + "</li>");
						}
					} catch (Exception e) {
						sb.append("<li>Unable to update benefits list component.\n</li>");
						log.error("Exceptoin : ", e);
					}
					// end of primary CTA Section.
					// --------------------------------------------------------------------------------------------------------------------------
					// start of Grid one.
					try {
						Elements titleGrids = doc.select("div.c100-pilot");
						Elements titleElements = null;
						if (titleGrids != null) {
							titleElements = titleGrids.select("h2.bdr-1");
						} else {
							sb.append("<li>No title found center Grid with class 'bdr-1' </li>");
						}
						if (layoutOverViewNode != null) {
							if (layoutOverViewNode.hasNode("gd11v1_0")) {
								Node gd11v1_0 = layoutOverViewNode
										.getNode("gd11v1_0");
								if (gd11v1_0.hasNode("gd11v1-mid")) {
									Node gd11v1_mid = gd11v1_0
											.getNode("gd11v1-mid");
									if (gd11v1_mid.hasNode("text")) {
										Node textNode = gd11v1_mid
												.getNode("text");
										textNode.setProperty("text",
												titleElements.get(0)
												.outerHtml());
									} else {
										sb.append("<li>gd11v1-mid Node doesn't exist with path : "
												+ gd11v1_mid.getPath()
												+ "</li>");
									}
								} else {
									sb.append("<li>gd11v1-mid Node doesn't exist with path : "
											+ gd11v1_0.getPath() + "</li>");
								}
							} else {
								sb.append("<li>gd11v1_0 Node doesn't exist with path : "
										+ layoutOverViewNode.getPath()
										+ "</li>");
							}

						} else {
							sb.append("<li>Node doesn't exist with path : "
									+ layoutOverView + "</li>");
						}

						if (layoutOverViewNode != null) {
							if (layoutOverViewNode.hasNode("gd11v1")) {
								Node gd11v1 = layoutOverViewNode
										.getNode("gd11v1");
								if (gd11v1.hasNode("gd11v1-mid")) {
									Node gd11v1_mid = gd11v1
											.getNode("gd11v1-mid");
									if (gd11v1_mid.hasNode("text")) {
										Node textNode = gd11v1_mid
												.getNode("text");
										textNode.setProperty("text",
												titleElements.get(1)
												.outerHtml());
									} else {
										sb.append("<li>gd11v1-mid Node doesn't exist with path : "
												+ gd11v1_mid.getPath()
												+ "</li>");
									}
								} else {
									sb.append("<li>gd11v1-mid Node doesn't exist with path : "
											+ gd11v1.getPath() + "</li>");
								}
							} else {
								sb.append("<li>gd11v1_0 Node doesn't exist with path : "
										+ layoutOverViewNode.getPath()
										+ "</li>");
							}

						} else {
							sb.append("<li>Node doesn't exist with path : "
									+ layoutOverView + "</li>");
						}

						Elements gd14v1_lefts = null;
						Elements gd14v1_mid_lefts = null;
						Elements gd14v1_mid_rights = null;
						Elements gd14v1_rights = null;
						Elements grids = doc.select("div.gd14v1");
						if (grids != null) {
							Element grid = grids.first();
							if (grid != null) {
								Elements leftGridElements = grid
										.select("div.gd14v1-left");
								if (leftGridElements != null) {
									gd14v1_lefts = leftGridElements
											.select("div.c100-pilot");
								} else {
									sb.append("<li>Left grid is empty.</li>");
								}
								Elements midLeftGridElements = grid
										.select("div.gd14v1-mid-left");
								if (midLeftGridElements != null) {
									gd14v1_mid_lefts = midLeftGridElements
											.select("div.c100-pilot");
								} else {
									sb.append("<li>mid Left grid is empty.</li>");
								}
								Elements midRightGridElements = grid
										.select("div.gd14v1-mid-right");
								if (midRightGridElements != null) {
									gd14v1_mid_rights = midRightGridElements
											.select("div.c100-pilot");
								} else {
									sb.append("<li>mid Right grid is empty.</li>");
								}
								Elements rightGridElements = grid
										.select("div.gd14v1-right");
								if (rightGridElements != null) {
									gd14v1_rights = rightGridElements
											.select("div.c100-pilot");
								} else {
									sb.append("<li>Left grid is empty.</li>");
								}

							} else {
								sb.append("<li>Grids are emplty with class 'div.gd14v1' </li>");
							}
						} else {
							sb.append("<li>No grids found with class 'div.gd14v1' </li>");
						}

						if (layoutOverViewNode != null) {
							if (layoutOverViewNode.hasNode("gd14v1")) {
								Node gd14v1 = layoutOverViewNode
										.getNode("gd14v1");
								if (gd14v1.hasNode("gd14v1-left")) {
									Node gd14v1_left = gd14v1
											.getNode("gd14v1-left");
									NodeIterator textNodeIterator = gd14v1_left
											.getNodes("text*");
									if (textNodeIterator.getSize() == gd14v1_lefts
											.size()) {
										for (Element ele : gd14v1_lefts) {
											String leftGridHtml = ele.html();
											textNodeIterator.hasNext();
											Node textNode = (Node) textNodeIterator
													.next();
											textNode.setProperty("text",
													leftGridHtml);
										}
									} else {
										sb.append("<li>Number of nodes("
												+ textNodeIterator.getSize()
												+ ") and elements("
												+ gd14v1_lefts.size()
												+ ") doesn't match.</li>");
									}

								} else {
									sb.append("<li>gd14v1-left not found under : "
											+ layoutOverViewNode.getPath()
											+ "</li>");
								}

								if (gd14v1.hasNode("gd14v1-mid-left")) {
									Node gd14v1_mid_left = gd14v1
											.getNode("gd14v1-mid-left");
									NodeIterator textNodeIterator = gd14v1_mid_left
											.getNodes("text*");
									if (textNodeIterator.getSize() == gd14v1_mid_lefts
											.size()) {
										for (Element ele : gd14v1_mid_lefts) {
											String midLeftGridHtml = ele.html();
											textNodeIterator.hasNext();
											Node textNode = (Node) textNodeIterator
													.next();
											textNode.setProperty("text",
													midLeftGridHtml);
										}
									} else {
										sb.append("<li>Number of nodes("
												+ textNodeIterator.getSize()
												+ ") and elements("
												+ gd14v1_mid_lefts.size()
												+ ") doesn't match.</li>");
									}

								} else {
									sb.append("<li>gd14v1-left not found under : "
											+ layoutOverViewNode.getPath()
											+ "</li>");
								}

								if (gd14v1.hasNode("gd14v1-mid-right")) {
									Node gd14v1_mid_right = gd14v1
											.getNode("gd14v1-mid-right");
									NodeIterator textNodeIterator = gd14v1_mid_right
											.getNodes("text*");
									if (textNodeIterator.getSize() == gd14v1_mid_rights
											.size()) {
										for (Element ele : gd14v1_mid_rights) {
											String midRightGridHtml = ele
													.html();
											textNodeIterator.hasNext();
											Node textNode = (Node) textNodeIterator
													.next();
											textNode.setProperty("text",
													midRightGridHtml);
										}
									} else {
										sb.append("<li>Number of nodes("
												+ textNodeIterator.getSize()
												+ ") and elements("
												+ gd14v1_mid_rights.size()
												+ ") doesn't match.</li>");
									}

								} else {
									sb.append("<li>gd14v1-left not found under : "
											+ layoutOverViewNode.getPath()
											+ "</li>");
								}

								if (gd14v1.hasNode("gd14v1-right")) {
									Node gd14v1_right = gd14v1
											.getNode("gd14v1-right");
									NodeIterator textNodeIterator = gd14v1_right
											.getNodes("text*");
									if (textNodeIterator.getSize() == gd14v1_rights
											.size()) {
										for (Element ele : gd14v1_rights) {
											String rightGridHtml = ele.html();
											textNodeIterator.hasNext();
											Node textNode = (Node) textNodeIterator
													.next();
											textNode.setProperty("text",
													rightGridHtml);
										}
									} else {
										sb.append("<li>Number of nodes("
												+ textNodeIterator.getSize()
												+ ") and elements("
												+ gd14v1_rights.size()
												+ ") doesn't match.</li>");
									}

								} else {
									sb.append("<li>gd14v1-left not found under : "
											+ layoutOverViewNode.getPath()
											+ "</li>");
								}

							} else {
								sb.append("<li>gd14v1 Node doesn't exist with path : "
										+ layoutOverViewNode.getPath()
										+ "</li>");
							}

						} else {
							sb.append("<li>Node doesn't exist with path : "
									+ layoutOverView + "</li>");
						}

						Elements c100_pilot_left = null;
						Elements c100_pilot_mid = null;
						Elements c100_pilot_right = null;
						Elements grids1 = doc.select("div.gd13v2");
						if (grids1 != null) {
							Element grid = grids1.first();
							if (grid != null) {
								Elements leftGridElements = grid
										.select("div.gd13v2-left");
								if (leftGridElements != null) {
									Element leftGridElement = leftGridElements
											.first();
									if (leftGridElement != null) {
										c100_pilot_left = leftGridElement
												.select("div.c100-pilot");
									} else {
										sb.append("<li>Left grid not foundin the second grid block.</li>");
									}

								} else {
									sb.append("<li>Left grid not found in the second grid block.</li>");
								}
								Elements midGridElements = grid
										.select("div.gd13v2-mid");
								if (midGridElements != null) {
									Element midGridElement = midGridElements
											.first();
									if (midGridElement != null) {
										c100_pilot_mid = midGridElement
												.select("div.c100-pilot");
									} else {
										sb.append("<li>mid grid not foundin the second grid block.</li>");
									}

								} else {
									sb.append("<li>mid grid not found in the second grid block.</li>");
								}
								Elements rightGridElements = grid
										.select("div.gd13v2-right");
								if (rightGridElements != null) {
									Element rightGridElement = rightGridElements
											.first();
									if (rightGridElement != null) {
										c100_pilot_right = rightGridElement
												.select("div.c100-pilot");
									} else {
										sb.append("<li>Right grid not foundin the second grid block.</li>");
									}

								} else {
									sb.append("<li>Right grid not found in the second grid block.</li>");
								}
							} else {
								sb.append("<li>Grids are emplty with class 'div.gd14v1' </li>");
							}
						} else {
							sb.append("<li>No grids found with class 'div.gd14v1' </li>");
						}

						if (layoutOverViewNode != null) {
							if (layoutOverViewNode.hasNode("gd13v2")) {
								Node gd13v2 = layoutOverViewNode
										.getNode("gd13v2");
								if (gd13v2 != null) {
									Node gd13v2_left = gd13v2
											.getNode("gd13v2-left");
									if (gd13v2_left != null) {
										NodeIterator textNodes = gd13v2_left
												.getNodes("text*");
										if (textNodes.getSize() == c100_pilot_left
												.size()) {
											for (Element ele : c100_pilot_left) {
												String leftGridHTML = ele
														.html();
												textNodes.hasNext();
												Node textNode = (Node) textNodes
														.next();
												textNode.setProperty("text",
														leftGridHTML);
											}
										} else {
											sb.append("<li>Node size("
													+ textNodes.getSize()
													+ ") and Element size("
													+ c100_pilot_left.size()
													+ ") in the left-grid are not equal.</li>");
										}
									} else {
										sb.append("<li>Left Grid is blank in the second grid section.</li>");
									}
									Node gd13v2_mid = gd13v2
											.getNode("gd13v2-mid");
									if (gd13v2_mid != null) {
										NodeIterator textNodes = gd13v2_mid
												.getNodes("text*");
										if (textNodes.getSize() == c100_pilot_mid
												.size()) {
											for (Element ele : c100_pilot_mid) {
												String midGridHTML = ele.html();
												textNodes.hasNext();
												Node textNode = (Node) textNodes
														.next();
												textNode.setProperty("text",
														midGridHTML);
											}
										} else {
											sb.append("<li>Node size("
													+ textNodes.getSize()
													+ ") and Element size("
													+ c100_pilot_mid.size()
													+ ") in the mid-grid are not equal.</li>");
										}
									} else {
										sb.append("<li>Mid Grid is blank in the second grid section.</li>");
									}
									Node gd13v2_right = gd13v2
											.getNode("gd13v2-right");
									if (gd13v2_right != null) {
										NodeIterator textNodes = gd13v2_right
												.getNodes("text*");
										if (textNodes.getSize() == c100_pilot_right
												.size()) {
											for (Element ele : c100_pilot_right) {
												String rightGridHTML = ele
														.html();
												textNodes.hasNext();
												Node textNode = (Node) textNodes
														.next();
												textNode.setProperty("text",
														rightGridHTML);
											}
										} else {
											sb.append("<li>Node size("
													+ textNodes.getSize()
													+ ") and Element size("
													+ c100_pilot_right.size()
													+ ") in the right-grid are not equal.</li>");
										}
									} else {
										sb.append("<li>Right Grid is blank in the second grid section.</li>");
									}
								} else {
									sb.append("<li>Second Grid is blank.</li>");
								}
							} else {
								sb.append("<li>gd14v1 Node doesn't exist with path : "
										+ layoutOverViewNode.getPath()
										+ "</li>");
							}
						} else {
							sb.append("<li>Node doesn't exist with path : "
									+ layoutOverView + "</li>");
						}
					} catch (Exception e) {
						log.error("Exception : ", e);
						sb.append("<li>Unable to update grid component.\n</li>");
					}
					// End of Grid.
					// -----------------------------------------------------------------------------------------------------

					session.save();
				} else {
					sb.append(Constants.PAGE_IS_NOT_MIGRATED_HTML_STRUCTURE_IS_DIFFERENT);
				}
			} else {
				sb.append(Constants.URL_CONNECTION_EXCEPTION);
			}
		} catch (Exception e) {
			log.error("Exception : ", e);
			sb.append("<li>Exception " + e + "</li>");
		}

		sb.append("</ul></td>");

		return sb.toString();
	}
}
