package com.cisco.dse.global.migration;

import java.io.IOException;
import java.util.ArrayList;

import javax.jcr.Node;
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

	// Repo node paths

	String benefitLeft = "/content/<locale>/products/<prod>/benefit/jcr:content/content_parsys/benefits/layout-benefits/gd12v2/gd12v2-left";
	String benefitRight = "/content/<locale>/products/<prod>/benefit/jcr:content/content_parsys/benefits/layout-benefits/gd12v2/gd12v2-right";

	String pageUrl = "http://chard.cisco.com:4502/content/<locale>/products/<prod>/benefit.html";
	
	static Logger log = Logger.getLogger(Benefits.class);

	public String translate(String loc, String prod, String type,
			String locale, Session session) throws IOException,
			ValueFormatException, VersionException, LockException,
			ConstraintViolationException, RepositoryException {
		BasicConfigurator.configure();
		log.debug("In the translate method");
		
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
				Elements benefitTextElements = doc.select("div.c00-pilot,div.cc00-pilot");
				
				if(benefitTextElements != null && benefitTextElements.size()>0){
				String benefitText = null;
				String benefitText0 = null;
				
				String h2Text = doc.select("h2.header-1").first().outerHtml();
				
								
				for (Element benefitTextEle : benefitTextElements) {

					Elements h2Ele = benefitTextEle.getElementsByTag("h2");
					
					if(h2Ele.size()>0){
						benefitText = h2Ele.first().outerHtml();
					}
					
					Elements migrate = benefitTextEle.getElementsByTag("migrate");
					if(migrate.size()>0){
					benefitText0 = migrate.first().html();
					}else{
					benefitText0 = benefitTextEle.html();
					}
				
					Node textNode = benefitLeftNode.getNode("text");
					if(textNode != null){
						textNode.setProperty("text", h2Text);
						log.debug("Updated text at " + textNode.getPath());
					}else{
						sb.append("<li>'text' node does not exists at "+benefitLeftNode.getPath()+"</li>");
					}
					
					
					Node text_0_Node = benefitLeftNode.getNode("text_0");
					if(text_0_Node != null){
						text_0_Node.setProperty("text", benefitText0);
						log.debug("Updated text at " + text_0_Node.getPath());
					}else{
						sb.append("<li>'text_0' node doesn't exists at "+benefitLeftNode.getPath()+"</li>");
					}
				}
				}else{
					sb.append("<li>'div.c00-pilot' div class doesn't exists.</li>");
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
					Element benefitTitle = benefitList.getElementsByTag("h2")
							.first();
					String benefitTitleText = benefitTitle.text();
					Elements benefitAnchorTitle = benefitList.parent()
							.select("div.n13-pilot a").select("h2");

					String benefitAnchorTitleText = benefitAnchorTitle.text();

					Elements benefitUlList = benefitList.getElementsByTag("ul");

					int count = 0;

					for (Element ele : benefitUlList) {
						java.util.List<String> list = new ArrayList<String>();
						Elements benefitLiList = ele.getElementsByTag("li");

						for (Element li : benefitLiList) {
							JSONObject jsonObj = new JSONObject();
							Elements listItemAnchor = li.getElementsByTag("a");
							Elements listItemSpan = li.getElementsByTag("span");

							String anchorText = listItemAnchor.text();
							String anchorHref = listItemAnchor.attr("href");
							String anchorTarget = listItemAnchor.attr("target");
							String listIcon = listItemSpan.attr("class");

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

						if (listNode != null) {
							Node elementList = listNode
									.getNode("element_list_0");
							if (elementList != null) {
								elementList.setProperty("listitems",
										list.toArray(new String[list.size()]));
								log.debug("Updated listitesm at " + elementList.getPath());
							} else {
								sb.append("<li>element_list_0 node doesn't exists</li>");
							}
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
                    sb.append("<li>Mis-Match in tilebordered Panels count/content.</li>");
				}
				if(rightRail.size()>0){
				for (Element ele : rightRail) {
					javax.jcr.Node rightRailNode = null;
					String title = ele.getElementsByTag("h2").text();
					String desc = ele.getElementsByTag("p").text();
					Elements anchor = ele.getElementsByTag("a");

					String anchorText = anchor.text();
					String anchorHref = anchor.attr("href");
										
						if(benefitRightNode.hasNode("tile_bordered") && entry == true){
							rightRailNode = benefitRightNode.getNode("tile_bordered");
							entry = false;
						}else{
							boolean hasNode = false;
							while(!hasNode && rightcount<20){
								hasNode = benefitRightNode.hasNode("tile_bordered_"+rightcount);
								if(hasNode){
									rightRailNode = benefitRightNode.getNode("tile_bordered_"+rightcount);
								}
								rightcount++;
							}
						}
					
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
