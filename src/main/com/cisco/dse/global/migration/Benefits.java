package com.cisco.dse.global.migration;

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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.sling.commons.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Benefits {

	Document doc;

	String title = null;
	String fProductsTitle = null;

	Element fProduct = null;
	Elements fProdTitle = null;
	Elements fFooterLinks = null;
	Elements fSeries = null;
	Elements fProdSeries = null;
	Element fSubSeries = null;

	StringBuilder sb = new StringBuilder(1024);

	javax.jcr.Node hNode = null;
	javax.jcr.Node drawer = null;
	javax.jcr.Node subdrawer = null;
	javax.jcr.Node subdrawerpanel = null;

	String footerLinks = "{\"linktext\":\"<aaa>\",\"linkurl\":\"<bbb>\"}";

	
	static Logger log = Logger.getLogger(Benefits.class);

	public String translate(String loc, String prod, String type, String catType,
			String locale, Session session) throws IOException,
			ValueFormatException, VersionException, LockException,
			ConstraintViolationException, RepositoryException {
		BasicConfigurator.configure();
		log.debug("In the translate method");
		log.debug("In the translate method, catType is :"+ catType);

		// Repo node paths

		String benefitLeft = "/content/<locale>/"+catType+"/<prod>/benefit/jcr:content/content_parsys/benefits/layout-benefits/gd12v2/gd12v2-left";
		String benefitRight = "/content/<locale>/"+catType+"/<prod>/benefit/jcr:content/content_parsys/benefits/layout-benefits/gd12v2/gd12v2-right";

		String pageUrl = "http://chard.cisco.com:4502/content/<locale>/"+catType+"/<prod>/benefit.html";
		
		pageUrl = pageUrl.replace("<locale>", locale).replace("<prod>", prod);
		
		sb.append("<td>" + "<a href="+pageUrl+">"+pageUrl+"</a>"+"</td>");
		sb.append("<td>" + "<a href="+loc+">"+loc +"</a>"+ "</td>");
		sb.append("<td><ul>");


		benefitLeft = benefitLeft.replace("<locale>", locale).replace("<prod>", prod);
		benefitRight = benefitRight.replace("<locale>", locale).replace("<prod>", prod);

		javax.jcr.Node benefitLeftNode = null;
		javax.jcr.Node benefitRightNode = null;
		try {
			benefitLeftNode = session.getNode(benefitLeft);
			benefitRightNode = session.getNode(benefitRight);

			try {
				doc = Jsoup.connect(loc).get();
			} catch (Exception e) {
				sb.append("<li>Cannot Connect to given URL. \n"+loc+"</li>");
			}

			title = doc.title();
			// ------------------------------------------------------------------------------------------------------------------------------------------
			// start set benefit text content.
			try {
				Elements benefitTextElements = doc.select("div.c00-pilot");
				int eleSize = benefitTextElements.size();
				NodeIterator textNodeIterator = benefitLeftNode.getNodes("text*");
				int nodeSize = (int)textNodeIterator.getSize();
				
				if(eleSize == nodeSize){
					for(Element ele : benefitTextElements){
						textNodeIterator.hasNext();
						Node textNode = (Node)textNodeIterator.next();
						textNode.setProperty("text", ele.html());
					}
				}
				if(nodeSize < eleSize){
					String content = "";
					for(Element ele : benefitTextElements){
						content = content + benefitTextElements.first().getElementsByTag("h1,h2").outerHtml();
						content = content + ele.html();
					}
					textNodeIterator.hasNext();
					Node textNode = (Node)textNodeIterator.next();
					textNode.setProperty("text", content);
				}
			} catch (Exception e) {
				sb.append("<li>Unable to update benefits text component."+e+"</li>");
			}

			// end set benefit text.
			// ---------------------------------------------------------------------------------------------------------------------------------------
			// start set benefit list.
			try {
				Elements benefitListElem = doc.select("div.gd-left").select("div.n13-pilot");
				if(benefitListElem != null){
				for (Element benefitList : benefitListElem) {
					Element benefitTitle = benefitList.getElementsByTag("h3")
							.first();
					String benefitTitleText = benefitTitle != null ? benefitTitle.text() : "";
					
					if(StringUtils.isBlank(benefitTitleText)){
						benefitTitleText = benefitList.getElementsByTag("h3").first() != null ? benefitList.getElementsByTag("h3").first().text() : "";
					}
					
					Elements benefitAnchorTitle = benefitList.parent()
							.select("div.n13-pilot a").select("h2,h3");

					String benefitAnchorTitleText = benefitAnchorTitle != null ? benefitAnchorTitle.text() : "";

					Elements benefitUlList = benefitList.getElementsByTag("ul");

					int count = 0;

					javax.jcr.Node listNode = null;

					if (count == 0) {
						listNode = benefitLeftNode.getNode("list");
						if (listNode != null) {
							listNode.setProperty("title", benefitTitleText);
							log.debug("Updated title at " + listNode.getPath());
							count++;
						}
					} else {
						listNode = benefitLeftNode.getNode("list_"
								+ (count - 1));
						if (listNode != null) {
							listNode.setProperty("title",
									benefitAnchorTitleText);
							log.debug("Updated title at " + listNode.getPath());
							count++;
						}
					}

					
					NodeIterator elementList = listNode.getNodes("element_list*");
					
					for (Element ele : benefitUlList) {
						java.util.List<String> list = new ArrayList<String>();
						Elements benefitLiList = ele.getElementsByTag("li");

						for (Element li : benefitLiList) {
							JSONObject jsonObj = new JSONObject();
							Elements listItemAnchor = li.getElementsByTag("a");
							Elements listItemSpan = li.getElementsByTag("span");

							String anchorText = listItemAnchor != null ? listItemAnchor.text() : "";
							String anchorHref = listItemAnchor.attr("href");
							String anchorTarget = listItemAnchor.attr("target");
							String listIcon = listItemSpan.attr("class");
							String icon= li.ownText();

							jsonObj.put("linktext", anchorText);
							jsonObj.put("linkurl", anchorHref);
							jsonObj.put("icon", listIcon);
							jsonObj.put("size", "");// Need to get the size from the list element text.
							jsonObj.put("description", "");// Need to get the description from the list element text.
							if (StringUtils.isNotBlank(anchorTarget)) {
								jsonObj.put("openInNewWindow", true);
							}
							list.add(jsonObj.toString());

						}
						
						elementList.hasNext();
						Node eleNode = (Node)elementList.next();						
							if (eleNode != null ) {
								if(list.size()>1)
									eleNode.setProperty("listitems",
										list.toArray(new String[list.size()]));
								else
									eleNode.setProperty("listitems",
											list.get(0));
								log.debug("Updated listitems at " + eleNode.getPath());
							} else {								
								sb.append("<li>element_list node doesn't exists</li>");
							}
					}
				}
				}else{
					sb.append("<li>div class 'div.n13-pilot' not found in dom</li>");
				}
			} catch (Exception e) {
				sb.append("<li>Unable to update benefits list component.\n</li>");
				log.error("Exceptoin : ",e);
			}
			// end set benefit list.
			// --------------------------------------------------------------------------------------------------------------------------
			// start of benefit list right rail.

			try {

				Elements rightRail = doc.select("div.c23-pilot,div.cc23-pilot");
				int rightcount = 0;
				boolean entry = true;
				if(rightRail != null){
				if (rightRail.size() != benefitRightNode.getNodes("tile_bordered*").getSize()) {
                    sb.append("<li>Mis-Match in tilebordered Panels count/content.</li>"+rightRail.size()+"tile***"+benefitRightNode.getNodes("tile_bordered*").getSize());
				}
				if(rightRail.size()>0){
					
				NodeIterator titleBorderNodes = benefitRightNode.getNodes("tile_bordered*");
					
				for (Element ele : rightRail) {
					javax.jcr.Node rightRailNode = null;
					String title = ele.getElementsByTag("h2")!=null?ele.getElementsByTag("h2").text():"";
					if(StringUtils.isBlank(title)){
						title = ele.getElementsByTag("h3")!=null?ele.getElementsByTag("h3").text():"";
					}
					String desc = ele.getElementsByTag("p")!=null?ele.getElementsByTag("p").text():"";
					Elements anchor = ele.getElementsByTag("a");

					String anchorText = anchor!=null?anchor.text():"";
					String anchorHref = anchor.attr("href");
										
					titleBorderNodes.hasNext();
					rightRailNode = (Node)titleBorderNodes.next();
					
					if (rightRailNode != null) {
						if(title != null && title != "" && desc != null && desc != "" && anchorText != null && anchorText != ""){
						rightRailNode.setProperty("title", title);
						rightRailNode.setProperty("description", desc);
						rightRailNode.setProperty("linktext", anchorText);
						rightRailNode.setProperty("linkurl", anchorHref);
						log.debug("title, description, linktext and linkurl are created at "+rightRailNode.getPath());
						}else{
							sb.append("<li>Content miss match for "+ele.className()+"</li>");
						}
					}else{
						sb.append("<li>one of title_bordered node doesn't exist in node structure.</li>");
					}
				}
				}else{
					sb.append("<li>No Content with class 'c23-pilot or cc23-pilot' found</li>");
				}
				}else{
					sb.append("<li>div class c23-pilot not found in dom</li>");
				}
			} catch (Exception e) {
				sb.append("<li>Unable to update benefits tile_bordered component.\n</li>");
			}
			// End of benefit list right rail.
			// -----------------------------------------------------------------------------------------------------
			
			session.save();

		} catch (Exception e) {
			sb.append("<li>Exception "+e+"</li>");
		}
		
		sb.append("</ul></td>");
		
		return sb.toString();
	}
}
