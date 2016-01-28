package com.cisco.dse.global.migration.productlanding;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.sling.commons.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.cisco.dse.global.migration.config.BaseAction;
import com.cisco.dse.global.migration.config.Constants;
import com.cisco.dse.global.migration.config.FrameworkUtils;

public class ProductLandingVariation6 extends BaseAction {

	Document doc;
	StringBuilder sb = new StringBuilder(1024);
	static Logger log = Logger.getLogger(ProductLandingVariation6.class);

	public String translate(String host,String loc, String prod, String type,
			String catType, String locale, Session session,Map<String,String>urlMap) throws IOException,
			ValueFormatException, VersionException, LockException,
			ConstraintViolationException, RepositoryException {
		BasicConfigurator.configure();
		log.debug("In the translate method of ProductLandingVariation6");
		log.debug("In the translate method, catType is :" + catType);

		// Repo node paths
		String pagePropertiesPath = "/content/<locale>/"+catType+"/<prod>/index/jcr:content";
		String layoutOverView = "/content/<locale>/"
				+ catType
				+ "/<prod>/index/jcr:content/content_parsys/overview/layout-overview";
		String pageUrl = host+"/content/<locale>/"
				+ catType + "/<prod>/index.html";

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
				// ------------------------------------------------------------------------------------------------------------------------------------------
				// start of hero panel section.
				try {
					Elements frameElements = null;
					Elements heropanelElements = doc.select("div.c50-pilot");
					if (heropanelElements != null) {
						Element heropanelElement = heropanelElements.first();
						if (heropanelElement != null) {
							frameElements = heropanelElement
									.select("div.frame");
						} else {
							sb.append("<li>Hero Panel is empty.</li>");
						}
					} else {
						sb.append("<li>No Hero Panel found.</li>");
					}

					String parbaseContent = "";
					Elements gridLeftElements = doc.select("div.gd12v2-left");
					Elements parbaseTextElements = gridLeftElements
							.select("div.parbase");
					if (parbaseTextElements != null) {
						parbaseTextElements = parbaseTextElements
								.select("div.c00v0-pilot");
						if (parbaseTextElements != null) {
							Element parbaseTextElement = parbaseTextElements
									.first();
							if (parbaseTextElement != null) {
								parbaseContent = parbaseTextElement.html();
							} else {
								sb.append("<li>No parbse text found.('div.parbase' element not found in 'div.gd-left')</li>");
							}
						} else {
							sb.append("<li>No parbse text found.('div.text parbase section' element not found in 'div.gd-left')</li>");
						}
					} else {
						sb.append("<li>No parbse text found.('div.text parbase section' element not found in 'div.gd-left')</li>");
					}

					Value[] panelPropertiest = null;
					if (layoutOverViewNode != null) {
						if (layoutOverViewNode.hasNode("gd12v2_0")) {
							Node gd12v2_0 = layoutOverViewNode
									.getNode("gd12v2_0");
							if (gd12v2_0.hasNode("gd12v2-left")) {
								Node gd12v2_left = gd12v2_0
										.getNode("gd12v2-left");
								if (gd12v2_left.hasNode("hero_large")) {
									Node hero_large = gd12v2_left.getNode("hero_large");

									Property panelNodesProperty = hero_large.hasProperty("panelNodes")?hero_large.getProperty("panelNodes"):null;
									if(panelNodesProperty.isMultiple()){
										panelPropertiest = panelNodesProperty.getValues();
									}

									if (frameElements != null) {
										if (frameElements.size()>0) {
											int i = 0;
											for (Element ele : frameElements) {
												Node heropanelNode = null;
												if(panelPropertiest != null && i<=panelPropertiest.length){
													String propertyVal = panelPropertiest[i].getString();
													if(StringUtils.isNotBlank(propertyVal)){
														JSONObject jsonObj = new JSONObject(propertyVal);
														if(jsonObj.has("panelnode")){
															String panelNodeProperty = jsonObj.get("panelnode").toString();
															heropanelNode = hero_large.hasNode(panelNodeProperty)?hero_large.getNode(panelNodeProperty):null;
														}
													}
													i++;
												}else{
													sb.append("<li>No heropanel Node found.</li>");
												}

												Node heroPanelPopUpNode = null;
												Elements lightBoxElements = ele.select("div.c50-image").select("a.c26v4-lightbox");
												if(lightBoxElements != null && !lightBoxElements.isEmpty()){
													Element lightBoxElement = lightBoxElements.first();
													heroPanelPopUpNode = FrameworkUtils.getHeroPopUpNode(heropanelNode);
												}

												Elements h2Elements = ele.getElementsByTag("h2");
												if (h2Elements != null) {
													Element h2element = h2Elements.first();
													if (h2element != null) {
														String h2 = h2element.text();
														heropanelNode.setProperty("title", h2);
														if(heroPanelPopUpNode != null){
															heroPanelPopUpNode.setProperty("popupHeader", h2);
														}else{
															if(lightBoxElements != null && !lightBoxElements.isEmpty()){
																sb.append("<li>Hero content video pop up node not found.</li>");
															}
														}
													} else {
														sb.append("<li>No heading foundin hero panel.</li>");
													}
												} else {
													sb.append("<li>No heading found in hero panel.</li>");
												}
												Elements pElements = ele
														.getElementsByTag("p");
												if (pElements != null) {
													Element pElement = pElements.first();
													if (pElement != null) {
														String p = pElement.text();
														heropanelNode.setProperty("description", p);
													} else {
														sb.append("<li>No description found in hero panel.</li>");
													}
												} else {
													sb.append("<li>No description found in hero panel.</li>");
												}
												Elements aElements = ele.getElementsByTag("a");
												if (aElements != null) {
													Element aElement = aElements.first();
													if (aElement != null) {
														String aText = aElement.text();
														String ahref = aElement.absUrl("href");
														if(StringUtil.isBlank(ahref)){
															ahref = aElement.attr("href");
														}
														// Start extracting valid href
														log.debug("Before heroPanelLinkUrl" + ahref + "\n");
														ahref = FrameworkUtils.getLocaleReference(ahref, urlMap);
														log.debug("after heroPanelLinkUrl" + ahref + "\n");
														// End extracting valid href
														heropanelNode.setProperty("linktext", aText);
														heropanelNode.setProperty("linkurl", ahref);
													} else {
														sb.append("<li>No anchor link found in hero panel.</li>");
													}
												} else {
													sb.append("<li>No anchor link found in hero panel.</li>");
												}
												// start image
												String heroImage = FrameworkUtils.extractImagePath(ele, sb);
												log.debug("heroImage " + heroImage + "\n");
												if (heropanelNode != null) {
													if (heropanelNode.hasNode("image")) {
														Node imageNode = heropanelNode.getNode("image");
														String fileReference = imageNode.hasProperty("fileReference")?imageNode.getProperty("fileReference").getString():"";
														heroImage = FrameworkUtils.migrateDAMContent(heroImage, fileReference, locale,sb);
														log.debug("heroImage " + heroImage + "\n");
														if (StringUtils.isNotBlank(heroImage)) {
															imageNode.setProperty("fileReference" , heroImage);
														}else{
															sb.append("<li> image is not found on locale page's hero element. </li>");
														}
													} else {
														sb.append("<li>hero image node doesn't exist</li>");
													}
												}
												// end image
											}
										} else {
											sb.append("<li>No hero frames found.</li>");
										}
									} else {
										sb.append("<li>No Frames found inside hero panel.</li>");
									}
									if (gd12v2_left.hasNode("text")) {
										Node textNode = gd12v2_left
												.getNode("text");
										if (StringUtils
												.isNotBlank(parbaseContent)) {
											textNode.setProperty("text",
													parbaseContent);
										} else {
											sb.append("<li>parbase sectoin content not found.</li>");
										}
									} else {
										sb.append("<li>'text' node doesn't exist.</li>");
									}
								} else {
									sb.append("<li>'hero_large' Node doesn't exists.</li>");
								}
							} else {
								sb.append("<li>'gd12v2-left' Node doesn't exists.</li>");
							}
						} else {
							sb.append("<li>'gd12v2_0' Node doesn't exists.</li>");
						}
					} else {
						sb.append("<li>Content path doesn't exist in WEM</li>");
					}
				} catch (Exception e) {
					log.error("Exception : ", e);
					sb.append("<li>Unable to update Hero panel component.</li>");
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
									Element pTagElement = pTagElements.first();
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
									Element aTagElement = aTagElements.first();
									if (aTagElement != null) {
										aText = aTagElement.text();
										aHref = aTagElement.absUrl("href");
										if(StringUtil.isBlank(aHref)){
											aHref = aTagElement.attr("href");
										}
										// Start extracting valid href
										log.debug("Before primaryCTALinkUrl" + aHref + "\n");
										aHref = FrameworkUtils.getLocaleReference(aHref, urlMap);
										log.debug("after primaryCTALinkUrl" + aHref + "\n");
										// End extracting valid href
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
										Element aElement = aElements.first();
										String title = aElement.attr("title");
										String href = aElement.absUrl("href");
										if(StringUtil.isBlank(href)){
											href = aElement.attr("href");
										}
										// Start extracting valid href
										log.debug("Before rightrailsocialLinkUrl" + href + "\n");
										href = FrameworkUtils.getLocaleReference(href, urlMap);
										log.debug("after rightrailsocialLinkUrl" + href + "\n");
										// End extracting valid href
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
						if (layoutOverViewNode.hasNode("gd12v2_0")) {
							gd12v2 = layoutOverViewNode.getNode("gd12v2_0");
							if (gd12v2.hasNode("gd12v2-right")) {
								gd12v2_right = gd12v2.getNode("gd12v2-right");
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
										primary_cta_v2.setProperty("linktext",
												aText);
										log.debug(aText
												+ "is set to the property title at : "
												+ primary_cta_v2.getPath());
									} else {
										sb.append("<li>anchor text is blank in primary cta.</li>");
									}
									if (StringUtils.isNotBlank(aHref)) {
										if (primary_cta_v2.hasNode("linkurl")) {
											Node linkurlNode = primary_cta_v2
													.getNode("linkurl");
											linkurlNode.setProperty("url",
													aHref);
											log.debug(aHref
													+ "is set to the property title at : "
													+ primary_cta_v2.getPath());
										} else {
											sb.append("<li>linkurl node doesn't exists under : "
													+ primary_cta_v2.getPath()
													+ "</li>");
										}
									} else {
										sb.append("<li>anchor href is blank for primary cta.</li>");
									}
								} else {
									sb.append("<li>Node with name 'hero_large' doesn't exist under "
											+ gd12v2_right.getPath() + "</li>");
								}
								if (gd12v2_right.hasNode("followus")) {
									Node followus = gd12v2_right
											.getNode("followus");
									if (StringUtils.isNotBlank(h2Content)) {
										followus.setProperty("title", h2Content);
									} else {
										sb.append("<li>No title found at right rail social media piolot.</li>");
									}

									if (list.size() > 1) {
										followus.setProperty("links",
												list.toArray(new String[list
												                        .size()]));
									}

								} else {
									sb.append("<li>No 'followus' node found under "
											+ gd12v2_right.getPath() + "</li>");
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
					Elements gd11v1 = doc.select("div.gd11v1-mid");
					int i = 0;
					if (gd11v1 != null) {
						if (layoutOverViewNode != null) {
							if (layoutOverViewNode.hasNode("gd11v1")) {
								Node gd11v1_Nodes = layoutOverViewNode
										.getNode("gd11v1");
								if (gd11v1_Nodes.hasNode("gd11v1-mid")) {
									Node gd11v1_midNode = gd11v1_Nodes
											.getNode("gd11v1-mid");
									if (gd11v1_midNode.hasNode("htmlblob_0")) {
										Node htmlblob_0 = gd11v1_midNode
												.getNode("htmlblob_0");
										Element gd11v1_mid = gd11v1.get(i).getElementsByClass("c00-pilot").first();
										if (gd11v1_mid != null) {
											htmlblob_0.setProperty("html", FrameworkUtils.extractHtmlBlobContent(gd11v1_mid, "", locale, sb, urlMap));
										} else {
											sb.append("<li>Heading of the grid is blank.</li>");
										}
									} else {
										sb.append("<li>'htmlblob_0' node doesn't exists.</li>");
									}
								} else {
									sb.append("<li>'gd11v1-mid' node doesn't exists.</li>");
								}
							} else {
								sb.append("<li>'gd11v1' Node doesn't exist.</li>");
							}
							i++;
							if (layoutOverViewNode.hasNode("gd11v1_0")) {
								Node gd11v1_0_Nodes = layoutOverViewNode
										.getNode("gd11v1_0");
								if (gd11v1_0_Nodes.hasNode("gd11v1-mid")) {
									Node gd11v1_midNode = gd11v1_0_Nodes
											.getNode("gd11v1-mid");
									if (gd11v1_midNode.hasNode("htmlblob")) {
										Node htmlblob = gd11v1_midNode
												.getNode("htmlblob");
										Element gd11v1_mid = gd11v1.get(i).getElementsByClass("c00-pilot").first();
										if (gd11v1_mid != null ) {
											htmlblob.setProperty("html", FrameworkUtils.extractHtmlBlobContent(gd11v1_mid, "", locale, sb, urlMap));
										} else {
											sb.append("<li>Heading of the grid is blank.</li>");
										}
									} else {
										sb.append("<li>'htmlblob' node doesn't exists.</li>");
									}
								} else {
									sb.append("<li>'gd11v1-mid' node doesn't exists.</li>");
								}
							} else {
								sb.append("<li>'gd11v1_0' Node doesn't exist.</li>");
							}
							i++;
							if (layoutOverViewNode.hasNode("gd11v1_1")) {
								Node gd11v1_1_Nodes = layoutOverViewNode
										.getNode("gd11v1_1");
								if (gd11v1_1_Nodes.hasNode("gd11v1-mid")) {
									Node gd11v1_midNode = gd11v1_1_Nodes
											.getNode("gd11v1-mid");
									if (gd11v1_midNode.hasNode("text")) {
										Node text = gd11v1_midNode.getNode("text");
										Element gd11v1_mid = gd11v1.get(i).getElementsByClass("c00-pilot").first();
										if (gd11v1_mid != null) {
											text.setProperty("text", gd11v1_mid.html());
										} else {
											sb.append("<li>Heading of the grid is blank.</li>");
										}
									} else {
										sb.append("<li>'text' node doesn't exists.</li>");
									}
								} else {
									sb.append("<li>'gd11v1-mid' node doesn't exists.</li>");
								}
							} else {
								sb.append("<li>'gd11v1_1' Node doesn't exist.</li>");
							}
						} else {
							sb.append("<li>Node doesn't exist with path : "
									+ layoutOverView + "</li>");
						}
							
					} else {
						sb.append("<li>Html blob element not found.</li>");
					}

					
				} catch (Exception e) {
					log.error("Exception : ", e);
					sb.append("<li>Unable to update grid component.\n</li>");
				}
				// End of Grid.
				// -----------------------------------------------------------------------------------------------------
				// Start of the grid one elements.
				try {
					NodeIterator gd14v1_Iterator = null;
					if (layoutOverViewNode != null) {
						if (layoutOverViewNode.hasNode("gd14v1")) {
							Node gd14v1 = layoutOverViewNode.getNode("gd14v1");
							gd14v1_Iterator = gd14v1.getNodes("gd14v1-*");
						} else {
							sb.append("<li>'gd14v1' Node doesn't exists.</li>");
						}

					} else {
						sb.append("<li>Node doesn't exist with path : "
								+ layoutOverView + "</li>");
					}

					Elements gd14v1_left_Elements = doc
							.select("div.gd14v1-pilot");
					if (gd14v1_left_Elements != null) {
						Elements list_Elements = gd14v1_left_Elements
								.select("div.list");
						if (list_Elements != null) {

							for (Element list_Element : list_Elements) {

								String title = "";
								String paragraph = "";
								List<String> list = new ArrayList<String>();

								if (list_Element != null) {
									Elements h2Tags = list_Element
											.getElementsByTag("h2");
									if (h2Tags != null) {
										Element h2Tag = h2Tags.first();
										if (h2Tag != null) {
											title = h2Tag.text();
										} else {
											sb.append("<li>No header foundi the left grid.</li>");
										}
									} else {
										sb.append("<li>No header found in the left grid.</li>");
									}

									Elements pElements = list_Element
											.getElementsByTag("p");
									if (pElements != null) {
										Element pElement = pElements.first();
										if (pElement != null) {
											paragraph = pElement.outerHtml();
										} else {
											sb.append("<li>No header foundi the left grid.</li>");
										}
									} else {
										sb.append("<li>No header found in the left grid.</li>");
									}

									Elements ulElements = list_Element
											.getElementsByTag("ul");
									if (ulElements != null) {
										Element ulElement = ulElements.first();
										if (ulElement != null) {
											Elements aTagElements = ulElement
													.getElementsByTag("a");
											for (Element ele : aTagElements) {
												JSONObject obj = new JSONObject();
												String aText = ele.text();
												String aLink = ele.absUrl("href");
												if(StringUtil.isBlank(aLink)){
													aLink = ele.attr("href");
												}
												// Start extracting valid href
												log.debug("Before listLinkUrl" + aLink + "\n");
												aLink = FrameworkUtils.getLocaleReference(aLink, urlMap);
												log.debug("after listLinkUrl" + aLink + "\n");
												// End extracting valid href
												obj.put("linktext", aText);
												obj.put("linkurl", aLink);
												obj.put("icon", "");
												obj.put("size", "");
												obj.put("description", "");
												obj.put("openInNewWindow",
														false);
												list.add(obj.toString());
											}
										} else {
											sb.append("<li>No list found the left grid.</li>");
										}
									} else {
										sb.append("<li>No list found in the left grid.</li>");
									}

									if (gd14v1_Iterator != null) {
										if (gd14v1_Iterator.hasNext()) {
											Node gd14v1 = (Node) gd14v1_Iterator
													.next();
											if (gd14v1.hasNode("list")) {
												Node listNode = gd14v1
														.getNode("list");

												if (StringUtils
														.isNotBlank(title)) {
													listNode.setProperty(
															"title", title);
												} else {
													sb.append("<li>No title found in the grid.</li>");
												}

												if (listNode.hasNode("intro")) {
													Node introNode = listNode
															.getNode("intro");

													if (StringUtils
															.isNotBlank(paragraph)) {
														introNode
														.setProperty(
																"paragraph_rte",
																paragraph);
													} else {
														sb.append("<li>no paragraph found in the grid.</li>");
													}

												} else {
													sb.append("<li>'intro' node doesn't exist.</li>");
												}

												NodeIterator element_list_Iterator = listNode
														.getNodes("element_list*");

												if (element_list_Iterator
														.hasNext()) {
													Node element_list = (Node) element_list_Iterator
															.next();

													if (element_list
															.hasProperty("listitems")) {
														Property listitems = element_list
																.getProperty("listitems");
														if (!listitems
																.isMultiple()) {
															listitems.remove();
															session.save();
														}
													}
													if (list.size() > 0) {
														element_list
														.setProperty(
																"listitems",
																list.toArray(new String[list
																                        .size()]));
													} else {
														sb.append("<li>No list elements found.</li>");
													}
												} else {
												}
											} else {
												sb.append("<li>'list' doesn't exists.</li>");
											}
										} else {
											sb.append("<li>'gd14v1-*' Node does not exists.</li>");
										}
									} else {
										sb.append("<li>'gd14v1-*' Node does not exists.</li>");
									}
								} else {
									sb.append("<li>left grid element is blank.</li>");
								}
							}
						} else {
							sb.append("<li>left grid not foundin the first section.</li>");
						}
					} else {
						sb.append("<li>left grid not found in the first section.</li>");
					}
				} catch (Exception e) {
					log.error("Exception : ", e);
					sb.append("<li>Unable to update grid component." + e + "</li>");
				}
				// End of grid one elements.
				// -------------------------------------------------------------------------------------
				// Start of grid two elements.
				try {
					NodeIterator gd14v1_Iterator = null;
					if (layoutOverViewNode != null) {
						if (layoutOverViewNode.hasNode("gd14v1_0")) {
							Node gd14v1 = layoutOverViewNode
									.getNode("gd14v1_0");
							gd14v1_Iterator = gd14v1.getNodes("gd14v1-*");
						} else {
							sb.append("<li>'gd14v1' Node doesn't exists.</li>");
						}

					} else {
						sb.append("<li>Node doesn't exist with path : "
								+ layoutOverView + "</li>");
					}

					Elements gd14v1_left_Elements = doc
							.select("div.gd14v1-pilot");
					if (gd14v1_left_Elements != null) {
						Elements slp_Elements = gd14v1_left_Elements
								.select("div.tile_slp_small");
						int imageSrcEmptyCount = 0;
						if (slp_Elements != null) {

							for (Element slp_Element : slp_Elements) {
								String h2Text = "";
								String pText = "";
								Elements h2Elements = slp_Element
										.getElementsByTag("h2");
								if (h2Elements != null) {
									Element h2Element = h2Elements.first();
									h2Text = h2Element.text();
								} else {
									sb.append("<li>No heading found in grid two.</li>");
								}
								Elements pElements = slp_Element
										.getElementsByTag("p");
								if (pElements != null) {
									Element pElement = pElements.first();
									pText = pElement.text();
								} else {
									sb.append("<li>No description found in grid two.</li>");
								}

								String aHref = "";
								Elements aElements = slp_Element
										.getElementsByTag("a");
								if (aElements != null) {
									Element aElement = aElements.first();
									aHref = aElement.absUrl("href");
									if(StringUtil.isBlank(aHref)){
										aHref = aElement.attr("href");
									}
									// Start extracting valid href
									log.debug("Before gridtwoLinkUrl" + aHref + "\n");
									aHref = FrameworkUtils.getLocaleReference(aHref, urlMap);
									log.debug("after gridtwoLinkUrl" + aHref + "\n");
									// End extracting valid href
								} else {
									sb.append("<li>No anchors found in grid two.</li>");
								}
								// start image
								String spotLightImage = FrameworkUtils.extractImagePath(slp_Element, sb);
								log.debug("spotLightImage " + spotLightImage + "\n");
								// end image
								if (gd14v1_Iterator.hasNext()) {
									Node gd14v1_Node = (Node) gd14v1_Iterator
											.next();
									if (gd14v1_Node.hasNode("tile_slp_small")) {
										Node tile_slp_small = gd14v1_Node
												.getNode("tile_slp_small");
										if (StringUtils.isNotBlank(h2Text)) {
											tile_slp_small.setProperty("title",
													h2Text);
										} else {
											sb.append("<li>No heading text found in the grid two.</li>");
										}
										if (StringUtils.isNotBlank(pText)) {
											tile_slp_small.setProperty(
													"description", pText);
										} else {
											sb.append("<li>No description text found in the grid two.</li>");
										}
										if (StringUtils.isNotBlank(aHref)) {
											tile_slp_small.setProperty(
													"linkurl", aHref);
										} else {
											sb.append("<li>No anchor tag found in the grid two.</li>");
										}
										if (tile_slp_small != null) {
											if (tile_slp_small.hasNode("image")) {
												Node imageNode = tile_slp_small.getNode("image");
												String fileReference = imageNode.hasProperty("fileReference")?imageNode.getProperty("fileReference").getString():"";
												spotLightImage = FrameworkUtils.migrateDAMContent(spotLightImage, fileReference, locale,sb);
												log.debug("spotLightImage " + spotLightImage + "\n");
												if (StringUtils.isNotBlank(spotLightImage)) {
													imageNode.setProperty("fileReference" , spotLightImage);
												}else{
													imageSrcEmptyCount++;
												}
											} else {
												sb.append("<li>hero image node doesn't exist</li>");
											}
										}
									} else {
										sb.append("<li>'tile_slp_small' Node doesn't exist.</li>");
									}
								}
							}if(imageSrcEmptyCount > 0){
								sb.append("<li> "+imageSrcEmptyCount+" image(s) are not found on spot light component of locale page. </li>");
							}

						} else {
							sb.append("<li>Second grid element not found.</li>");
						}
					} else {
						sb.append("<li>Node doesn't exist with path : "
								+ layoutOverView + "</li>");
					}
				} catch (Exception e) {
					log.error("Exception : ", e);
					sb.append("<li>Unable to update grid two component.\n</li>");
				}
				// End of grid two elements.

				// Start of grid three.
				try {
					String h2Text = "";
					String pText = "";
					String aText = "";
					String spotLightImage = "";
					String aHref = "";
					Elements c11v5_alt1_pilot = doc
							.select("div.c11v5-alt1-pilot");
					if (c11v5_alt1_pilot != null) {
						Element spotLightContent = c11v5_alt1_pilot.first();
						if (spotLightContent != null) {
							Elements h2Elements = spotLightContent
									.getElementsByTag("h2");
							if (h2Elements != null) {
								Element h2Element = h2Elements.first();
								if (h2Element != null) {
									h2Text = h2Element.text();
								} else {
									sb.append("<li>No Heading found in spotlight block.</li>");
								}
							} else {
								sb.append("<li>No header found in spotlight block.</li>");
							}
							Elements pElements = spotLightContent
									.getElementsByTag("p");
							if (pElements != null) {
								Element pElement = pElements.first();
								if (pElement != null) {
									pText = pElement.text();
								} else {
									sb.append("<li>No Description found in spotlight block.</li>");
								}
							} else {
								sb.append("<li>No Description found in spotlight block.</li>");
							}
							Elements aElements = spotLightContent
									.getElementsByTag("a");
							if (aElements != null) {
								Element aElement = aElements.first();
								if (aElement != null) {
									aText = aElement.text();
									aHref = aElement.absUrl("href");
									if(StringUtil.isBlank(aHref)){
										aHref = aElement.attr("href");
									}
									// Start extracting valid href
									log.debug("Before gridThreeLinkUrl" + aHref + "\n");
									aHref = FrameworkUtils.getLocaleReference(aHref, urlMap);
									log.debug("after gridThreeLinkUrl" + aHref + "\n");
									// End extracting valid href
								} else {
									sb.append("<li>No Anchor found in spotlight block.</li>");
								}
							} else {
								sb.append("<li>No Anchor found in spotlight block.</li>");
							}
							// start image
							spotLightImage = FrameworkUtils.extractImagePath(spotLightContent, sb);
							log.debug("spotLightImage " + spotLightImage + "\n");
							// end image
						} else {
							sb.append("<li>spotLight content is blank.</li>");
						}
					} else {
						sb.append("<li>No spotlight content found.</li>");
					}
					NodeIterator gd14v1_Iterator = null;
					if (layoutOverViewNode != null) {
						if (layoutOverViewNode.hasNode("gd11v1_1")) {
							Node gd14v1 = layoutOverViewNode.getNode("gd11v1_1");
							if (gd14v1.hasNode("gd11v1-mid")) {
								Node gd11v1_mid = gd14v1.getNode("gd11v1-mid");
								if (gd11v1_mid.hasNode("spotlight_large_v2")) {
									Node spotlight_large_v2 = gd11v1_mid
											.getNode("spotlight_large_v2");
									if (StringUtils.isNotBlank(h2Text)) {
										spotlight_large_v2.setProperty("title",
												h2Text);
									} else {
										sb.append("<li>No heading found in spotlight.</li>");
									}
									if (StringUtils.isNotBlank(pText)) {
										spotlight_large_v2.setProperty(
												"description", pText);
									} else {
										sb.append("<li>No description found in spotlight.</li>");
									}
									if (StringUtils.isNotBlank(aText)) {
										spotlight_large_v2.setProperty("linktext",
												aText);
									} else {
										sb.append("<li>No anchor text found in spotlight.</li>");
									}
									if (spotlight_large_v2.hasNode("cta")) {
										Node ctaNode = spotlight_large_v2
												.getNode("cta");
										if (StringUtils.isNotBlank(aHref)) {
											ctaNode.setProperty("url", aHref);
										} else {
											sb.append("<li>href link is blank.</li>");
										}
									} else {
										sb.append("<li>'cta' Node doesn't exist.</li>");
									}
									if (spotlight_large_v2 != null) {
										if (spotlight_large_v2.hasNode("image")) {
											Node imageNode = spotlight_large_v2.getNode("image");
											String fileReference = imageNode.hasProperty("fileReference")?imageNode.getProperty("fileReference").getString():"";
											spotLightImage = FrameworkUtils.migrateDAMContent(spotLightImage, fileReference, locale,sb);
											log.debug("spotLightImage " + spotLightImage + "\n");
											if (StringUtils.isNotBlank(spotLightImage)) {
												imageNode.setProperty("fileReference" , spotLightImage);
											}
										} else {
											sb.append("<li>hero image node doesn't exist</li>");
										}
									}
								} else {
									sb.append("<li>'spotlight_large_v2' Node doesn't exist.</li>");
								}

							} else {
								sb.append("<li>'gd11v1-mid' Node doesn't exist.</li>");
							}
						}
					}

				} catch (Exception e) {
					log.error("Exception : ", e);
					sb.append("<li>Unable to update grid two component.\n</li>");
				}
				// End of grid three.
				// -----------------------------------------------------------------------------
				// Start of grid four.

				try {
					NodeIterator gd14v1_Iterator = null;
					if (layoutOverViewNode != null) {
						if (layoutOverViewNode.hasNode("gd14v1_1")) {
							Node gd14v1 = layoutOverViewNode.getNode("gd14v1_1");
							gd14v1_Iterator = gd14v1.getNodes("gd14v1-*");
						} else {
							sb.append("<li>'gd14v1' Node doesn't exists.</li>");
						}

					} else {
						sb.append("<li>Node doesn't exist with path : "
								+ layoutOverView + "</li>");
					}
					Elements gd14v1_pilots = doc.select("div.gd14v1-pilot");
					if (gd14v1_pilots != null) {
						Elements c23v2_pilots = gd14v1_pilots
								.select("div.c23v2-pilot");
						for (Element ele : c23v2_pilots) {
							String h2Text = "";
							String pText = "";
							String aText = "";
							String aHref = "";
							Elements h2Elements = ele.getElementsByTag("h2");
							if (h2Elements != null) {
								Element h2Element = h2Elements.first();
								if (h2Element != null) {
									h2Text = h2Element.text();
								} else {
									sb.append("<li>No heading found in fouth grid.</li>");
								}
							} else {
								sb.append("<li>No heading found in fouth grid.</li>");
							}
							Elements pElements = ele.getElementsByTag("p");
							if (pElements != null) {
								Element pElement = pElements.first();
								if (pElement != null) {
									pText = pElement.text();
								} else {
									sb.append("<li>No description found in fouth grid.</li>");
								}
							} else {
								sb.append("<li>No description found in fouth grid.</li>");
							}
							Elements aElements = ele.getElementsByTag("a");
							if (aElements != null) {
								Element aElement = aElements.first();
								if (aElement != null) {
									aText = aElement.text();
									aHref = aElement.absUrl("href");
									if(StringUtil.isBlank(aHref)){
										aHref = aElement.attr("href");
									}
									// Start extracting valid href
									log.debug("Before gridFourLinkUrl" + aHref + "\n");
									aHref = FrameworkUtils.getLocaleReference(aHref, urlMap);
									log.debug("after gridFourLinkUrl" + aHref + "\n");
									// End extracting valid href
								} else {
									sb.append("<li>No anchor found in fouth grid.</li>");
								}
							} else {
								sb.append("<li>No anchor found in fouth grid.</li>");
							}
							if (gd14v1_Iterator.hasNext()) {
								Node gd14v1Node = (Node) gd14v1_Iterator.next();
								if (gd14v1Node.hasNode("tile_bordered")) {
									Node tile_bordered = gd14v1Node
											.getNode("tile_bordered");

									if (StringUtils.isNotBlank(h2Text)) {
										tile_bordered.setProperty("title",
												h2Text);
									} else {
										sb.append("<li>No Header found in the fourth grids.</li>");
									}

									if (StringUtils.isNotBlank(pText)) {
										tile_bordered.setProperty(
												"description", pText);
									} else {
										sb.append("<li>No description found in the fourth grids.</li>");
									}

									if (StringUtils.isNotBlank(aText)) {
										tile_bordered.setProperty("linktext",
												aText);
									} else {
										sb.append("<li>No link text found in the fourth grids.</li>");
									}

									if (StringUtils.isNotBlank(aHref)) {
										tile_bordered.setProperty("linkurl",
												aHref);
									} else {
										sb.append("<li>No linkurl found in the fourth grids.</li>");
									}
								} else {
									sb.append("<li>'tile_bordered' Node doesn't exist.</li>");
								}

							} else {
								sb.append("<li>'gd14v1*' Node doesn't exists.</li>");
							}
						}

						Elements mbwtiles = gd14v1_pilots.select("div.mbwtile");
						String h3Text = "";
						String pText = "";
						String aText = "";
						String aHref = "";
						if (mbwtiles != null) {
							Element mbwtile = mbwtiles.first();
							if (mbwtile != null) {
								Elements h3Elements = mbwtile
										.getElementsByTag("h3");
								if (h3Elements != null) {
									Element h3Element = h3Elements.first();
									if (h3Element != null) {
										h3Text = h3Element.text();
									} else {
										sb.append("<li>No heading found in right grid.</li>");
									}

								} else {
									sb.append("<li>No heading found in the right grid.</li>");
								}

								Elements pElements = mbwtile
										.getElementsByTag("p");
								if (pElements != null) {
									Element pElement = pElements.first();
									if (pElement != null) {
										pText = pElement.text();
									} else {
										sb.append("<li>No heading found in right grid.</li>");
									}

								} else {
									sb.append("<li>No heading found in the right grid.</li>");
								}

								Elements aElements = mbwtile
										.getElementsByTag("a");
								if (aElements != null) {
									Element aElement = aElements.first();
									if (aElement != null) {
										aText = aElement.text();
										aHref = aElement.absUrl("href");
										if(StringUtil.isBlank(aHref)){
											aHref = aElement.attr("href");
										}
										// Start extracting valid href
										log.debug("Before rightGridLinkUrl" + aHref + "\n");
										aHref = FrameworkUtils.getLocaleReference(aHref, urlMap);
										log.debug("after rightGridLinkUrl" + aHref + "\n");
										// End extracting valid href
									} else {
										sb.append("<li>No heading found in right grid.</li>");
									}

								} else {
									sb.append("<li>No heading found in the right grid.</li>");
								}

								if (gd14v1_Iterator.hasNext()) {
									Node gd14v1Node = (Node) gd14v1_Iterator
											.next();
									if (gd14v1Node.hasNode("tile_bordered")) {

										Node tile_bordered = gd14v1Node
												.getNode("tile_bordered");

										if (StringUtils.isNotBlank(h3Text)) {
											tile_bordered.setProperty("title",
													h3Text);
										} else {
											sb.append("<li>heading is blank in last grid.</li>");
										}

										if (StringUtils.isNotBlank(pText)) {
											tile_bordered.setProperty(
													"description", pText);
										} else {
											sb.append("<li>heading is blank in last grid.</li>");
										}

										if (StringUtils.isNotBlank(aText)) {
											tile_bordered.setProperty(
													"linktext", aText);
										} else {
											sb.append("<li>heading is blank in last grid.</li>");
										}

										if (StringUtils.isNotBlank(aHref)) {
											tile_bordered.setProperty(
													"linkurl", aHref);
										} else {
											sb.append("<li>heading is blank in last grid.</li>");
										}

									} else {
										sb.append("<li>'tile_bordered' Node doesn't exist.</li>");
									}

								}

							} else {
								sb.append("<li>right most 'mbox' grid is missing.</li>");
							}

						} else {
							sb.append("<li>right grid element not found in the fourth grid.('mbwtile' element doesn't exists.)</li>");
						}

					} else {
						sb.append("<li>Grid Four title border section not found. </li>");
					}
				} catch (Exception e) {
					log.error("Exception : ", e);
					sb.append("<li>Unable to update grid two component.\n</li>");
				}
				// End of grid four.
			}else{
				sb.append(Constants.URL_CONNECTION_EXCEPTION);
			}
			session.save();
		} catch (Exception e) {
			log.error("Exception : ", e);
			sb.append("<li>Exception " + e + "</li>");
		}
		sb.append("</ul></td>");
		return sb.toString();
	}
}
