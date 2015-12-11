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
import org.apache.log4j.Logger;
import org.apache.sling.commons.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.cisco.dse.global.migration.config.BaseAction;
import com.cisco.dse.global.migration.config.Constants;
import com.cisco.dse.global.migration.config.FrameworkUtils;

public class ProductLandingVariation10 extends BaseAction {

	/**
	 * @param args
	 */
	// TODO Auto-generated method stub
	Document doc;

	StringBuilder sb = new StringBuilder(1024);

	Logger log = Logger.getLogger(ProductLandingVariation10.class);

	public String translate(String host, String loc, String prod, String type,
			String catType, String locale, Session session) throws IOException,
			ValueFormatException, VersionException, LockException,
			ConstraintViolationException, RepositoryException {

		log.debug("In the translate method");
		log.debug("In the translate method, catType is :" + catType);
		String pagePropertiesPath = "/content/<locale>/"+catType+"/<prod>/index/jcr:content";
		String indexLeft = "/content/<locale>/"+ catType+ "/<prod>/index/jcr:content/content_parsys/overview/layout-overview/gd12v2/gd12v2-left";
		String indexRight = "/content/<locale>/"+ catType+ "/<prod>/index/jcr:content/content_parsys/overview/layout-overview/gd12v2/gd12v2-right";

		String pageUrl = host + "/content/<locale>/"+ catType + "/<prod>/index.html";
		pageUrl = pageUrl.replace("<locale>", locale).replace("<prod>", prod);
		pagePropertiesPath = pagePropertiesPath.replace("<locale>", locale).replace("<prod>", prod);
		sb.append("<td>" + "<a href=" + pageUrl + ">" + pageUrl + "</a>"+ "</td>");
		sb.append("<td>" + "<a href=" + loc + ">" + loc + "</a>" + "</td>");
		sb.append("<td><ul>");

		indexLeft = indexLeft.replace("<locale>", locale).replace("<prod>", prod);
		indexRight = indexRight.replace("<locale>", locale).replace("<prod>", prod);

		javax.jcr.Node indexLeftNode = null;
		javax.jcr.Node indexRightNode = null;
		javax.jcr.Node pageJcrNode = null;
		try {
			indexLeftNode = session.getNode(indexLeft);
			indexRightNode = session.getNode(indexRight);
			pageJcrNode = session.getNode(pagePropertiesPath);
			try {
				doc = Jsoup.connect(loc).get();
				log.debug("Connected to the provided URL");
			} catch (Exception e) {
				doc = getConnection(loc);
			}
			
			if(doc != null){
				// ------------------------------------------------------------------------------------------------------------------------------------------
				// start set page properties.
				
				FrameworkUtils.setPageProperties(pageJcrNode, doc, session, sb);
				
				// end set page properties.
				// ------------------------------------------------------------------------------------------------------------------------------------------
				
				// start set hero large component properties.			
				try {
					log.debug("Start of Hero component");
					Elements heroElements = doc.select("div.frame");				
					Node heroNode = indexLeftNode.hasNode("hero_large") ? indexLeftNode.getNode("hero_large") : null;

					if (heroNode != null) {
						log.debug("hero node found: "+ heroNode.getPath());
						if (heroElements.isEmpty()) {
							log.debug("No hero element found with div class name frame.");
							sb.append("<li>Hero component with class name 'frame' does not exist on locale page.</li>");
						}
						else {
							int eleSize = heroElements.size();
							log.debug("hero node element size: "+ eleSize);
							NodeIterator heroPanelNodeIterator = heroNode.getNodes("heropanel*");
							int nodeSize = (int)heroPanelNodeIterator.getSize();
							log.debug("hero node nodeSize : "+ nodeSize);
							if(eleSize == nodeSize){
								for(Element ele : heroElements){
									Node heroPanelNode;
									if (heroPanelNodeIterator.hasNext()) {
										heroPanelNode = (Node)heroPanelNodeIterator.next();
										heroPanelTranslate(heroPanelNode, ele, locale);
									}
									else {
										log.debug("Next node not found");								
									}
								}
							}
							else if(nodeSize < eleSize){
								for(Element ele : heroElements){
									Node heroPanelNode;
									if (heroPanelNodeIterator.hasNext()) {
										heroPanelNode = (Node)heroPanelNodeIterator.next();
										heroPanelTranslate(heroPanelNode, ele, locale);		
									}
									else {
										log.debug("Next node not found");
										sb.append("<li>Mismatch in the count of hero panels. Additional panel(s) found on locale page. Locale page has "+ eleSize +" panels and there are "+ nodeSize +" nodes.</li>");
										break;
									}
								}										
							}
							else if (nodeSize > eleSize) {
								for(Element ele : heroElements){
									Node heroPanelNode;
									if (heroPanelNodeIterator.hasNext()) {
										heroPanelNode = (Node)heroPanelNodeIterator.next();
										heroPanelTranslate(heroPanelNode, ele, locale);		
									}
									else {
										log.debug("Next node not found");								
									}
								}
								sb.append("<li>Mismatch in the count of hero panels. Additional node(s) found. Locale page has "+ eleSize +" panels and there are "+ nodeSize +" nodes.</li>");
							}
						}
					}
					else {
						log.debug("No hero node found at "+indexLeftNode);
						sb.append("<li>Node for hero large component does not exist.</li>");
					}
					log.debug("End of Hero component");

				} catch (Exception e) {
					sb.append("<li>Unable to update hero large component." + e
							+ "</li>");
				}		

				// end set Hero Large component properties.

				// start set selectorbar large component properties.				
				try {
					Elements selectorBarLargeElements;
					selectorBarLargeElements = doc.select("div.selectorbarpanel");
					if (selectorBarLargeElements.size() == 0) {
						selectorBarLargeElements = doc.select("div.c58-pilot").select("div.left,div.mid,div.right"); //("div.selectorbarpanel");
					}

					Node selectorBarNode = indexLeftNode.hasNode("selectorbarlarge_0") ? indexLeftNode.getNode("selectorbarlarge_0") : null;

					if (selectorBarNode != null) {
						if (selectorBarLargeElements.isEmpty()) {
							log.debug("No selector bar element found with div class name selectorbarpanel.");
							sb.append("<li>Selector bar component with class name 'selectorbarpanel' does not exist on locale page.</li>");
						}
						else {
							log.debug("selector bar node component found at : "+ indexLeftNode.getPath());
							int eleSize = selectorBarLargeElements.size();
							log.debug("selector component element size: "+ eleSize);
							NodeIterator selectorBarPanel = selectorBarNode.getNodes("selectorbarpanel*");
							int nodeSize = (int)selectorBarPanel.getSize();
							log.debug("selector component nodeSize : "+ nodeSize);
							if(eleSize == nodeSize){

								for(Element ele : selectorBarLargeElements){
									Node selectorBarPanelNode;
									if (selectorBarPanel.hasNext()) {
										selectorBarPanelNode = (Node)selectorBarPanel.next();
										selectorBarTranslate(selectorBarPanelNode, ele);
									}
									else {
										log.debug("Next node not found");								
									}
								}
							}
							else if(eleSize > nodeSize){
								for(Element ele : selectorBarLargeElements){
									Node selectorBarPanelNode;
									if (selectorBarPanel.hasNext()) {
										selectorBarPanelNode = (Node)selectorBarPanel.next();
										selectorBarTranslate(selectorBarPanelNode, ele);
									}
									else {
										log.debug("Next node not found");
										sb.append("<li>Mismatch in the count of selector bar panel. Additional panel(s) found on locale page. Locale page has "+ eleSize +" panels and there are "+ nodeSize +" nodes.</li>");
										break;
									}
								}

							}
							else if(eleSize < nodeSize){
								for(Element ele : selectorBarLargeElements){
									Node selectorBarPanelNode;
									if (selectorBarPanel.hasNext()) {
										selectorBarPanelNode = (Node)selectorBarPanel.next();
										selectorBarTranslate(selectorBarPanelNode, ele);
									}
								}
								sb.append("<li>Mismatch in the count of selector bar panels. Additional node(s) found. Locale page has "+ eleSize +" panels and there are "+ nodeSize +" nodes.</li>");						
							}
						}
					}
					else {
						log.debug("No selector bar node found at "+indexLeftNode);
						sb.append("<li>Node for selector bar component does not exist.</li>");
					}
				} catch (Exception e) {
					sb.append("<li>Unable to update Selecotr bar large component." + e
							+ "</li>");
				}		

				// end set Selector bar.

				// start of text component
				Node textNode2 = null;
				try {
					Elements textElements = doc.select("div.gd-left").select("div.c00-pilot");
					Node textNode1 =indexLeftNode.hasNode("gd22v2") ? indexLeftNode.getNode("gd22v2").getNode("gd22v2-left").getNode("text") : indexLeftNode.getNode("text"); 
					textNode2 = indexLeftNode.hasNode("gd22v2") ? indexLeftNode.getNode("gd22v2").getNode("gd22v2-right") : null;
					if (textNode1 != null) {
						if (textElements.isEmpty()) {
							sb.append("<li>The first text element is not available on the locale page.</li>");
						}
						else {
							if (textElements.first().html() != null) {
								textNode1.setProperty("text", textElements.first().html());
							}
							else {
								sb.append("<li>The first text element is not available on the locale page.</li>");
							}
						}
													
					}
					else {
						if (textElements.first().html() != null) {
							sb.append("<li>The first text node is not available on the locale page.</li>");
						}
					}
					
					if (textNode2 != null) {
						Node textChildNode = textNode2.getNode("text");
						if (textElements.size() >= 2) { 
							if (textElements.get(1).html() != null) { 
								textChildNode.setProperty("text", textElements.get(1).html());
							}
							else {
								sb.append("<li>The second text element is not available on the locale page.</li>");
							}
						}
						else {
							sb.append("<li>The second text element is not available on the locale page.</li>");
						}
												
					}
					else {
						if (textElements.size() >= 2) {
							if (textElements.get(1).html() != null) {
								sb.append("<li>The second text node is not available on the locale page.</li>");
							}
						}
					}

				} catch (Exception e) {
					log.error("Exception "+e);
					sb.append("<li>Unable to update text components.</li>");
				}
				// end of text component

				// Start of button
				try {
					Element a00v1CqElement = doc.select("div.a00v1-cq").first();
					if (textNode2 != null) {
						Node buttonNode = textNode2.getNode("a00v1_cq");
						if (buttonNode != null) {
							if (a00v1CqElement != null) {
								Elements cqAnchor = a00v1CqElement.getElementsByTag("a");
								String anchorText = cqAnchor != null ? cqAnchor.text() : "";
								String anchorHref = cqAnchor.attr("href");
								buttonNode.setProperty("linkText", anchorText);
								buttonNode.setProperty("linkUrl", anchorHref);
							}
							else {
								sb.append("<li>Button is not available on the locale page.</li>");
								log.debug("This button does not exist.");
							}
						}
						else {
							if (a00v1CqElement != null) {
								sb.append("<li>Additional button is available on the locale page.</li>");
							}
						}
					}
					else {
						if (a00v1CqElement != null) {
							sb.append("<li>Additional button is available on the locale page.</li>");
						}
					}
				}catch (Exception e) {
					sb.append("<li>Unable to update button component." + e + "</li>");
				}
				//End of button

				// start set spotlight component.
				try {
					Elements spotLightElements = doc.select("div.c11-pilot");

					if (indexLeftNode != null) {
						log.debug("Spotlight node found: "+ indexLeftNode.getPath());
						if (spotLightElements.isEmpty()) {
							log.debug("No spot light element found with div class name c11-pilot.");
							sb.append("<li>Spot light component with class name 'c11-pilot' does not exist on locale page.</li>");
						}
						else {
							int eleSize = spotLightElements.size();
							log.debug("Spotlight node element size: "+ eleSize);
							NodeIterator slNodeIterator = indexLeftNode.getNodes("spotlight_large_v2*");
							int nodeSize = (int)slNodeIterator.getSize();
							log.debug("Spotlight node nodeSize : "+ nodeSize);
							if(eleSize == nodeSize){
								for (Element spElement : spotLightElements) {
									Node slNode;
									if (slNodeIterator.hasNext()) {
										slNode = (Node)slNodeIterator.next();
										spotLightTranslate(slNode, spElement, locale);
									}
									else {
										log.debug("Next node not found");								
									}

								}
							}
							else if (nodeSize < eleSize) {
								for (Element spElement : spotLightElements) {
									Node slNode;
									if (slNodeIterator.hasNext()) {
										slNode = (Node)slNodeIterator.next();
										spotLightTranslate(slNode, spElement, locale);
									}
									else {
										log.debug("Next node not found");
										sb.append("<li>Mismatch in the count of spot light panels. Additional panel(s) found on locale page. Locale page has "+ eleSize +" panels and there are "+ nodeSize +" nodes.</li>");
										break;								
									}
								}

							}
							else if (nodeSize > eleSize) {
								for (Element spElement : spotLightElements) {
									Node slNode;
									if (slNodeIterator.hasNext()) {
										slNode = (Node)slNodeIterator.next();
										spotLightTranslate(slNode, spElement, locale);
									}
									else {
										log.debug("Next node not found");
									}
								}
								sb.append("<li>Mismatch in the count of spot light panels. Additional node(s) found. Locale page has "+ eleSize +" panels and there are "+ nodeSize +" nodes.</li>");
							}
						}

					}
					else {
						log.debug("No spot light node found at "+indexLeftNode);
						sb.append("<li>Node for spot light component does not exist.</li>");	
					}
				}
				catch (Exception e) {
					sb.append("<li>Unable to update Spot light component.</li>");
					log.error("Exception : "+e);
				} 
				//end set spotlight properties.

				//start right rail properties
				try {
					boolean migrate = true;
					Elements rightRailList = doc.select("div.gd-right").select("div.mlb-pilot").select("div.c00-pilot");
					
					if (rightRailList.isEmpty()) {
						rightRailList = doc.select("div.gd-right").select("div.n13-pilot");
						if (rightRailList != null) {
							int eleSize = rightRailList.size();
							if (eleSize == 1) {
								Element rightListElem =  rightRailList.first();
								if (rightListElem != null) {
									Elements ulElements = rightListElem.getElementsByTag("ul");
									if (ulElements.size() > 1) {
										sb.append("<li>The HTML structure for list component in right rail on the locale page is different and hence migration needs to be done manually.</li>");
										migrate = false;
									}
								}
							}
						}
					}
					
					if (migrate) {
						if (rightRailList.isEmpty()) {
							log.debug("No right rail elements found with div class name c00-pilot or n13-pilot.");
							sb.append("<li>Right rail component with class name 'c00-pilot' or 'n13-pilot' does not exist on locale page.</li>");
						}
						else {
							int eleSize = rightRailList.size();
							NodeIterator listIterator = indexRightNode.getNodes("list*");
							int nodeSize = (int)listIterator.getSize();

							if(eleSize == nodeSize){
								for (Element rightListEle : rightRailList) {
									Node listNode;
									if (listIterator.hasNext()) {
										listNode = (Node)listIterator.next();
										rightRailList(listNode, rightListEle);
									}
									else {
										log.debug("Next node not found");								
									}

								}
							}
							else if (eleSize > nodeSize) {
								for (Element rightListEle : rightRailList) {
									Node listNode;
									if (listIterator.hasNext()) {
										listNode = (Node)listIterator.next();
										rightRailList(listNode, rightListEle);						}
									else {
										log.debug("Next node not found");
										sb.append("<li>Mismatch in the count of list panels. Additional panel(s) found on locale page. Locale page has "+ eleSize +" panels and there are "+ nodeSize +" nodes.</li>");
										break;								
									}

								}
							}
							else if (eleSize < nodeSize) {
								for (Element rightListEle : rightRailList) {
									Node listNode;
									if (listIterator.hasNext()) {
										listNode = (Node)listIterator.next();
										rightRailList(listNode, rightListEle);						}
									else {
										log.debug("Next node not found");
									}
								}
								sb.append("<li>Mismatch in the count of list panels. Additional node(s) found. Locale page has "+ eleSize +" panels and there are "+ nodeSize +" nodes.</li>");
							}
						}
					}
					
				} catch (Exception e) {
					sb.append("<li>Unable to update benefits tile_bordered component.</li>");
					log.error("Exception : ",e);
				}
				//end set index list.

				// start of follow us component
				try {
					String h2Content = "";
					boolean followUsEle = true;
					boolean followUsNode = true;
					List<String> list = new ArrayList<String>();
					Elements rightRailPilotElements = doc.select("div.gd-right").select("div.s14-pilot");
					if (rightRailPilotElements != null) {
						Element rightRailPilotElement = rightRailPilotElements.first();
						if (rightRailPilotElement != null) {
							Elements h2Elements = rightRailPilotElement.getElementsByTag("h2");
							if (h2Elements != null) {
								Element h2Element = h2Elements.first();
								h2Content = h2Element.text();
							} else {
								sb.append("<li>h2 of right rail with class 'div.s14-pilot' is blank.</li>");
							}
							Elements liElements = rightRailPilotElement.getElementsByTag("li");
							for (Element ele : liElements) {
								JSONObject obj = new JSONObject();
								String icon = ele.attr("class");
								obj.put("icon", icon);
								Elements aElements = ele.getElementsByTag("a");
								if (aElements != null) {
									Element aElement = aElements.first();
									String title = aElement.attr("title");
									String href = aElement.attr("href");
									obj.put("linktext", title);
									obj.put("linkurl", href);
								} else {
									sb.append("<li>No anchor tag found in the right rail social links</li>");
								}
								list.add(obj.toString());
							}
						} else {
							followUsEle = false; 
						}
					} else {
						followUsEle = false; 
					}

					if (indexRightNode.hasNode("followus")) {
						Node followus = indexRightNode.getNode("followus");
						if (StringUtils.isNotBlank(h2Content)) {
							followus.setProperty("title", h2Content);
						} 

						if (list.size() > 1) {
							followus.setProperty("links", list.toArray(new String[list.size()]));
						}

					} else {
						followUsNode = false;
					}

					if (followUsEle) {
						if (followUsNode) {
							log.debug("Follow us element and node are available.");
						}
						else {
							sb.append("<li>Additional follow us element found in right rail on the locale page.</li>");
						}
					}
					else {
						if (followUsNode) {
							sb.append("<li>No follow us element found in right rail on the locale page.</li>");

						}
						else {
							log.debug("Follow us element and node are not available.");
						} 
					}

				}
				catch (Exception e) {
					sb.append("<li>Unable to update follow us component.</li>");
					log.error("Exception : ",e);
				}

				// end of follow us component

			} else {
				sb.append(Constants.URL_CONNECTION_EXCEPTION);
			}

		} catch (Exception e) {
			sb.append("<li>Exception " + e + "</li>");
		}

		sb.append("</ul></td>");
		session.save();
		return sb.toString();
	}


	public void heroPanelTranslate(Node heroPanelNode, Element ele, String locale) {

		try {			
			String title = ele.getElementsByTag("h2")!=null?ele.getElementsByTag("h2").text():"";
			String desc = ele.getElementsByTag("p")!=null?ele.getElementsByTag("p").first().text():"";

			Elements anchor = ele.getElementsByTag("a");		
			String anchorText = anchor!=null?anchor.text():"";
			String anchorHref = anchor.attr("href");

			// start image
			String heroImage = FrameworkUtils.extractImagePath(ele, sb);
			log.debug("heroImage before migration : " + heroImage + "\n");
			if (heroPanelNode != null) {
				if (heroPanelNode.hasNode("image")) {
					Node imageNode = heroPanelNode.getNode("image");
					String fileReference = imageNode.hasProperty("fileReference")?imageNode.getProperty("fileReference").getString():"";
					heroImage = FrameworkUtils.migrateDAMContent(heroImage, fileReference, locale);
					log.debug("heroImage after migration : " + heroImage + "\n");
					if (StringUtils.isNotBlank(heroImage)) {
						imageNode.setProperty("fileReference" , heroImage);
					} else {
						sb.append("<li>hero image doesn't exist</li>");
					}
				} else {
					sb.append("<li>hero image node doesn't exist</li>");
				}
			}
			// end image
			
			heroPanelNode.setProperty("title", title);
			heroPanelNode.setProperty("description", desc);
			heroPanelNode.setProperty("linktext", anchorText);
			heroPanelNode.setProperty("linkurl", anchorHref);
		}catch (Exception e) {
			e.printStackTrace();
		}

	}	

	public void selectorBarTranslate(Node selectorBarPanelNode, Element ele) {

		try {
			String title = (ele!=null?ele.getElementsByTag("a").first().text():"");
			String titleUrl = ele.getElementsByTag("a").first().attr("href");
			log.debug("selector component titleUrl: "+ titleUrl);							
			selectorBarPanelNode.setProperty("title", title);
			selectorBarPanelNode.setProperty("titleurl", titleUrl);
			if (ele.childNodeSize() >= 2) {
				log.debug("Child node size is greater than 1.");
				if (ele.select("div.menu").isEmpty()) {
					log.debug("Menu is not available.");
					sb.append("<li>Selector bar drop down menu elements does not exist on the locale page.</li>");
				}
				else {
					log.debug("Menu is available.");
					Element menuEle = ele.child(1);
					if (menuEle != null) {
						log.debug("selector component menuEle: "+ menuEle.toString());
						Element anchor = menuEle.getElementsByTag("a").last();
						String allLinkText = anchor!=null? anchor.text():"";
						String allLinkUrl = anchor!=null?anchor.attr("href"):"";
						selectorBarPanelNode.setProperty("alllinktext", allLinkText);
						selectorBarPanelNode.setProperty("alllinkurl", allLinkUrl);

						Elements menuUlList = menuEle.getElementsByTag("ul");
						System.out.println("/////////"+menuUlList.size());
						for (Element element : menuUlList) {
							java.util.List<String> list = new ArrayList<String>();
							Elements menuLiList = element.getElementsByTag("li");
							System.out.println(menuLiList.size());

							for (Element li : menuLiList) {
								JSONObject jsonObj = new JSONObject();
								Elements listItemAnchor = li.getElementsByTag("a");
								String anchorText = listItemAnchor != null ? listItemAnchor.text() : "";
								String anchorHref = listItemAnchor.attr("href");

								jsonObj.put("linktext", anchorText);
								jsonObj.put("linkurl", anchorHref);
								jsonObj.put("size", "");
								list.add(jsonObj.toString());
							}

							selectorBarPanelNode.setProperty("panelitems", list.toArray(new String[list.size()]));	
						}
					}
					else {
						sb.append("<li>Selector bar drop down menu elements does not exist on the locale page.</li>");
					}
				}
			}
			else {
				sb.append("<li>Selector bar drop down menu elements does not exist on the locale page.</li>");
			}
						
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void spotLightTranslate (Node slNode, Element spElement, String locale) {
		try {
			Element spotLightTitle = spElement.getElementsByTag("h2").first();
			Element spotLightDescription = spElement.getElementsByTag("p").first();
			Elements spotLightAnchorElements = spElement.getElementsByTag("a");
			Element spotLightAnchor = spotLightAnchorElements.first();
			
			// start image
			String spotLightImage = FrameworkUtils.extractImagePath(spElement, sb);
			log.debug("spotLightImage " + spotLightImage + "\n");
			if (slNode != null) {
				if (slNode.hasNode("image")) {
					Node spotLightImageNode = slNode.getNode("image");
					String fileReference = spotLightImageNode.hasProperty("fileReference")?spotLightImageNode.getProperty("fileReference").getString():"";
					spotLightImage = FrameworkUtils.migrateDAMContent(spotLightImage, fileReference, locale);
					log.debug("spotLightImage " + spotLightImage + "\n");
					if (StringUtils.isNotBlank(spotLightImage)) {
						spotLightImageNode.setProperty("fileReference" , spotLightImage);
					} else {
						sb.append("<li>spotlight image doesn't exist</li>");
					}
				} else {
					sb.append("<li>spotlight image node doesn't exist</li>");
				}
			}
			// end image
			
			if(spotLightDescription.getElementsByTag("a")!=null && !spotLightDescription.getElementsByTag("a").isEmpty()){
				slNode.setProperty("description", spotLightDescription.html());
				// start
				if (spotLightAnchorElements.size() > 1) {
					spotLightAnchor = spotLightAnchorElements.get(1);
				}
				else {
					spotLightAnchor = null;
					sb.append("<li>Link is not found on locale page for the spotlight component. This needs to be deleted manually.</li>");
				}
				//end
			}
			else {
				slNode.setProperty("description", spotLightDescription.text());
			}
			
			if (spotLightAnchor != null) {
				String linkText = spotLightAnchor.text();
				String linkUrl = spotLightAnchor.attr("href");
				slNode.setProperty("linktext", linkText);
				javax.jcr.Node ctaNode = slNode.getNode("cta");
				if (ctaNode != null) {
					if (linkUrl != null) {
//						ctaNode.setProperty("linktype", "Url");
						ctaNode.setProperty("url", linkUrl);
					}
				}
			}
			
			if (spotLightTitle != null) {
				Elements spotLightLink = spotLightTitle.getElementsByTag("a");
				if (spotLightLink.isEmpty()) {
					slNode.setProperty("title", spotLightTitle.text());
				}
				else {
					Element spotLightLinkEle = spotLightLink.first();
					String slLinkText = spotLightLinkEle.text();
					String slLinkUrl = spotLightLinkEle.attr("href");
					slNode.setProperty("title", slLinkText);
					javax.jcr.Node titleLinkNode = slNode.getNode("titlelink");
					if (titleLinkNode != null) {
						if (slLinkUrl != null) {
							titleLinkNode.setProperty("linktype", "Url");
							titleLinkNode.setProperty("url", slLinkUrl);
						}
					}
				}
			}
			log.debug("Updated title, descriptoin and linktext at "+slNode.getPath());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void rightRailList (Node listNode, Element rightListEle) {
		try {
			Element title;
			Element description;
			Elements headElements = rightListEle.getElementsByTag("h2");
			if (headElements.size() > 1) {
				title = rightListEle.getElementsByTag("h2").last();
				description = rightListEle.getElementsByTag("p").last();
				sb.append("<li>Mismatch in count of list panel component in right rail.</li>");
			}
			else {
				title = rightListEle.getElementsByTag("h2").first();
				description = rightListEle.getElementsByTag("p").first();
			}
			listNode.setProperty("title", title.text());
			javax.jcr.Node introNode = listNode.getNode("intro");
			introNode.setProperty("paragraph_rte", description.text());
			javax.jcr.Node eleListNode = listNode.getNode("element_list_0");

			Elements ulList = rightListEle.getElementsByTag("ul");
			for (Element element : ulList) {
				java.util.List<String> list = new ArrayList<String>();
				Elements menuLiList = element.getElementsByTag("li"); 

				for (Element li : menuLiList) {
					JSONObject jsonObjrr = new JSONObject();
					Elements listItemAnchor = li.getElementsByTag("a");
					String anchorText = listItemAnchor != null ? listItemAnchor.text() : "";
					String anchorHref = listItemAnchor.attr("href");

					jsonObjrr.put("linktext", anchorText);
					jsonObjrr.put("linkurl", anchorHref);
					jsonObjrr.put("icon", "none");
					jsonObjrr.put("size", "");
					jsonObjrr.put("description", "");
					jsonObjrr.put("openInNewWindow", "false");
					list.add(jsonObjrr.toString());
				}
				eleListNode.setProperty("listitems", list.toArray(new String[list.size()]));	
			}
			log.debug("Updated title, descriptoin and linktext at "+listNode.getPath());
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}

}
