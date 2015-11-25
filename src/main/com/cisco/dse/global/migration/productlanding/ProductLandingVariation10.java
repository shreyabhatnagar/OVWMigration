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

public class ProductLandingVariation10 {

	/**
	 * @param args
	 */
	// TODO Auto-generated method stub
	Document doc;
	
	StringBuilder sb = new StringBuilder(1024);
	
	Logger log = Logger.getLogger(ProductLandingVariation10.class);
	
	public String translate(String loc, String prod, String type,
			String catType, String locale, Session session) throws IOException,
			ValueFormatException, VersionException, LockException,
			ConstraintViolationException, RepositoryException {
		
		log.debug("In the translate method");
		log.debug("In the translate method, catType is :" + catType);
		
		String indexLeft = "/content/<locale>/"+ catType+ "/<prod>/index/jcr:content/content_parsys/overview/layout-overview/gd12v2/gd12v2-left";
		String indexRight = "/content/<locale>/"+ catType+ "/<prod>/index/jcr:content/content_parsys/overview/layout-overview/gd12v2/gd12v2-right";
		
		String pageUrl = "http://chard.cisco.com:4502/content/<locale>/"+ catType + "/<prod>/index.html";
		pageUrl = pageUrl.replace("<locale>", locale).replace("<prod>", prod);
		
		sb.append("<td>" + "<a href=" + pageUrl + ">" + pageUrl + "</a>"+ "</td>");
		sb.append("<td>" + "<a href=" + loc + ">" + loc + "</a>" + "</td>");
		sb.append("<td><ul>");

		indexLeft = indexLeft.replace("<locale>", locale).replace("<prod>", prod);
		indexRight = indexRight.replace("<locale>", locale).replace("<prod>", prod);
		
		javax.jcr.Node indexLeftNode = null;
		javax.jcr.Node indexRightNode = null;
		
		try {
			indexLeftNode = session.getNode(indexLeft);
			indexRightNode = session.getNode(indexRight);
			
			try {
				doc = Jsoup.connect(loc).get();
				log.debug("Connected to the provided URL");
			} catch (Exception e) {
				sb.append("<li>Cannot Connect to given URL. \n" + loc + "</li>");
			}
			
			// start set hero large component properties.			
			try {
				log.debug("Start of Hero component");
				Elements heroElements = doc.select("div.frame");				
				Node heroNode = indexLeftNode.hasNode("hero_large") ? indexLeftNode.getNode("hero_large") : null;
						
				if (heroNode != null) {
					log.debug("hero node found: "+ heroNode.getPath());
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
								heroPanelTranslate(heroPanelNode, ele);
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
								heroPanelTranslate(heroPanelNode, ele);		
							}
							else {
								log.debug("Next node not found");
								sb.append("<li>Mismatch in the count of hero panels. Additional panel(s) found on locale page.</li>");
								break;
							}
						}										
					}
					else if (nodeSize > eleSize) {
						for(Element ele : heroElements){
							Node heroPanelNode;
							if (heroPanelNodeIterator.hasNext()) {
								heroPanelNode = (Node)heroPanelNodeIterator.next();
								heroPanelTranslate(heroPanelNode, ele);		
							}
							else {
								log.debug("Next node not found");								
							}
						}
						sb.append("<li>Mismatch in the count of hero panels. Additional node(s) found.</li>");
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
								sb.append("<li>Mismatch in the count of selector bar panel. Additional panel(s) found on locale page.</li>");
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
						sb.append("<li>Mismatch in the count of selector bar panels. Additional node(s) found.</li>");						
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
				textNode1.setProperty("text", textElements.first().html());
				
				if (textNode2 != null) {
					Node textChildNode = textNode2.getNode("text");
					textChildNode.setProperty("text", textElements.get(1).html());						
				}
				else {
					sb.append("<li>The second text node is not available on the locale page.</li>");
				}
				
			} catch (Exception e) {
				sb.append("<li>Unable to update text components." + e + "</li>");
			}
			// end of text component
			
			// Start of button
			try {
				if (textNode2 != null) {
					Node buttonNode = textNode2.getNode("a00v1_cq");
					Element a00v1CqElement = doc.select("div.a00v1-cq").first();
					
					if (a00v1CqElement != null) {
						Elements cqAnchor = a00v1CqElement.getElementsByTag("a");
						String anchorText = cqAnchor != null ? cqAnchor.text() : "";
						String anchorHref = cqAnchor.attr("href");
						buttonNode.setProperty("linkText", anchorText);
						buttonNode.setProperty("linkUrl", anchorHref);
					}
					else {
						sb.append("<li>Button is not available on the locale page.</li>");
					}
					
				}
				else {
					log.debug("This button does not exist.");
					sb.append("<li>Button is not available on the locale page.</li>");
				}
			}catch (Exception e) {
				sb.append("<li>Unable to update button component." + e + "</li>");
			}
			//End of button
			
			// start set spotlight component.
			try {
				Elements spotLightElements = doc.select("div.c11-pilot");
//				Node spotLightNode = indexLeftNode.hasNode("spotlight_large_v2*") ? indexLeftNode
//						.getNode("spotlight_large_v2*") : null;
						
				if (indexLeftNode != null) {
					log.debug("Spotlight node found: "+ indexLeftNode.getPath());
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
								spotLightTranslate(slNode, spElement);
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
								spotLightTranslate(slNode, spElement);
							}
							else {
								log.debug("Next node not found");
								sb.append("<li>Mismatch in the count of spot light panels. Additional panel(s) found on locale page.</li>");
								break;								
							}
						}
					
					}
					else if (nodeSize > eleSize) {
						for (Element spElement : spotLightElements) {
							Node slNode;
							if (slNodeIterator.hasNext()) {
								slNode = (Node)slNodeIterator.next();
								spotLightTranslate(slNode, spElement);
							}
							else {
								log.debug("Next node not found");
							}
						}
						sb.append("<li>Mismatch in the count of spot light panels. Additional node(s) found.</li>");
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
				Elements rightRailList = doc.select("div.gd-right").select("div.mlb-pilot").select("div.c00-pilot");				
				if (rightRailList.isEmpty()) {
					rightRailList = doc.select("div.gd-right").select("div.n13-pilot");
				}
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
							sb.append("<li>Mismatch in the count of list panels. Additional panel(s) found on locale page.</li>");
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
					sb.append("<li>Mismatch in the count of list panels. Additional node(s) found.</li>");
				}
				

			} catch (Exception e) {
				sb.append("<li>Unable to update benefits tile_bordered component.</li>");
				log.error("Exception : ",e);
			}
		//end set index list.
			
			// start of follow us component
			String h2Content = "";
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
					sb.append("<li>right rail with class 'div.s14-pilot' is blank.</li>");
				}
			} else {
				sb.append("<li>No pilot found on right rail with class 'div.s14-pilot'</li>");
			}
			
			if (indexRightNode.hasNode("followus")) {
				Node followus = indexRightNode.getNode("followus");
				if (StringUtils.isNotBlank(h2Content)) {
					followus.setProperty("title", h2Content);
				} else {
					sb.append("<li>No title found at right rail social media pilot.</li>");
				}

				if (list.size() > 1) {
					followus.setProperty("links", list.toArray(new String[list.size()]));
				}

			} else {
				sb.append("<li>No 'followus' node found under "
						+ indexRightNode.getPath() + "</li>");
			}
		/*	try {
				Element followUs = doc.select("div.gd-right").select("div.s14-pilot").first();
				if (followUs != null) {
					sb.append("<li>Additional 'Follow Us' component found in the right rail on locale page.</li>");
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}*/
			// end of follow us component
				
		} catch (Exception e) {
			sb.append("<li>Exception " + e + "</li>");
		}
		
		sb.append("</ul></td>");
		session.save();
		return sb.toString();
	}
		
		
	public void heroPanelTranslate(Node heroPanelNode, Element ele) {
		
		try {			
			String title = ele.getElementsByTag("h2")!=null?ele.getElementsByTag("h2").text():"";
			String desc = ele.getElementsByTag("p")!=null?ele.getElementsByTag("p").first().text():"";
			
			Elements anchor = ele.getElementsByTag("a");		
			String anchorText = anchor!=null?anchor.text():"";
			String anchorHref = anchor.attr("href");
			
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
			Element menuEle = ele.child(1);
			log.debug("selector component menuEle: "+ menuEle.toString());
			Element anchor = menuEle.getElementsByTag("a").last();
			String allLinkText = anchor!=null? anchor.text():"";
			String allLinkUrl = anchor!=null?anchor.attr("href"):"";
			selectorBarPanelNode.setProperty("alllinktext", allLinkText);
			selectorBarPanelNode.setProperty("alllinkurl", allLinkUrl);
			
			Elements menuUlList = menuEle.getElementsByTag("ul");
			for (Element element : menuUlList) {
				java.util.List<String> list = new ArrayList<String>();
				Elements menuLiList = element.getElementsByTag("li");

				for (Element li : menuLiList) {
					JSONObject jsonObj = new JSONObject();
					Elements listItemAnchor = li.getElementsByTag("a");
					String anchorText = listItemAnchor != null ? listItemAnchor.text() : "";
					String anchorHref = listItemAnchor.attr("href");
					
					jsonObj.put("linktext", anchorText);
					jsonObj.put("linkurl", anchorHref);
					list.add(jsonObj.toString());
				}
				
				selectorBarPanelNode.setProperty("panelitems", list.toArray(new String[list.size()]));	
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void spotLightTranslate (Node slNode, Element spElement) {
		try {
			Element spotLightTitle = spElement.getElementsByTag("h2").first();
			Element spotLightDescription = spElement.getElementsByTag("p").first();
			Element spotLightAnchor = spElement.getElementsByTag("a").first();
			String linkText = spotLightAnchor.text();
			String linkUrl = spotLightAnchor.attr("href");
			slNode.setProperty("title", spotLightTitle.text());
			slNode.setProperty("description", spotLightDescription.text());
			slNode.setProperty("linktext", linkText);
			javax.jcr.Node ctaNode = slNode.getNode("cta");
			ctaNode.setProperty("url", linkUrl);
			log.debug("Updated title, descriptoin and linktext at "+slNode.getPath());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void rightRailList (Node listNode, Element rightListEle) {
		try {
			Element title = rightListEle.getElementsByTag("h2").first();
			listNode.setProperty("title", title.text());
			Element description = rightListEle.getElementsByTag("p").first();
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



