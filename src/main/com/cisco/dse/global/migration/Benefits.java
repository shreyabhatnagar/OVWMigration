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

	static Logger log = Logger.getLogger(Benefits.class);

	public String translate(String loc, String prod, String type,
			String locale, Session session) throws IOException,
			ValueFormatException, VersionException, LockException,
			ConstraintViolationException, RepositoryException {
		BasicConfigurator.configure();
		log.debug("In the translate method");
		
		sb.append("<td>" + "url" + "</td>");
		sb.append("<td>" + loc + "</td>");
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
				sb.append("<li> Connected to : " + doc.baseUri() + "</li>");
			} catch (Exception e) {
				sb.append("<li>Cannot Connect to given URL. \n</li>");
			}

			title = doc.title();
			// ------------------------------------------------------------------------------------------------------------------------------------------
			// start set benefit text content.
			try {
				Elements benefitTextElements = doc.select("div.c00-pilot,div.cc00-pilot");
				
				if(benefitTextElements != null && benefitTextElements.size()>0){
				String benefitText = null;
				String benefitText0 = null;
				for (Element benefitTextEle : benefitTextElements) {

					benefitText = benefitTextEle.getElementsByTag("h2").first().outerHtml();
					
					Elements migrate = benefitTextEle.getElementsByTag("migrate");
					if(migrate.size()>0){
					benefitText0 = migrate.first().html();
					}else{
					benefitText0 = benefitTextEle.html();
					}
				
					Node textNode = benefitLeftNode.getNode("text");
					if(textNode != null){
						if(migrate.size()>0){
						textNode.setProperty("text", benefitText);
						}else{
							textNode.setProperty("text", "");
						}
						sb.append("<li>Updated text at " + textNode.getPath() + "</li>");
					}else{
						sb.append("<li>'text' node does not exists at "+benefitLeftNode.getPath()+"</li>");
					}
					
					
					Node text_0_Node = benefitLeftNode.getNode("text_0");
					if(text_0_Node != null){
						text_0_Node.setProperty("text", benefitText0);
						sb.append("<li>Updated text at " + text_0_Node.getPath() + "</li>");
					}else{
						sb.append("<li>'text_0' node doesn't exists at "+benefitLeftNode.getPath()+"</li>");
					}
				}
				}else{
					sb.append("<li style='color:red'>'div.c00-pilot' div class doesn't exists.</li>");
				}
			} catch (Exception e) {
				sb.append("<li>Unable to update benefits text component.\n"+e+"</li>");
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
								sb.append("<li>Updated title at " + listNode.getPath() + "</li>");
								count++;
							}
						} else {
							listNode = benefitLeftNode.getNode("list_"
									+ (count - 1));
							if (listNode != null) {
								listNode.setProperty("title",
										benefitAnchorTitleText);
								sb.append("<li>Updated title at " + listNode.getPath() + "</li>");
								count++;
							}
						}

						if (listNode != null) {
							Node elementList = listNode
									.getNode("element_list_0");
							if (elementList != null) {
								elementList.setProperty("listitems",
										list.toArray(new String[list.size()]));
								sb.append("<li>Updated listitesm at " + elementList.getPath() + "</li>");
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
				sb.append("<li>Unable to update benefits list component.\n" + e +"</li>");
				log.error(e);
			}
			// end set benefit list.
			// --------------------------------------------------------------------------------------------------------------------------
			// start of benefit list right rail.

			try {

				Elements rightRail = doc.select("div.c23-pilot");
				javax.jcr.Node rightRailNode = null;
				int rightcount = 0;
				boolean entry = true;
				if(rightRail != null){
				for (Element ele : rightRail) {

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
						rightRailNode.setProperty("title", title);
						rightRailNode.setProperty("description", desc);
						rightRailNode.setProperty("linktext", anchorText);
						rightRailNode.setProperty("linkurl", anchorHref);
						sb.append("<li>title, description, linktext and linkurl are created at "+rightRailNode.getPath()+"</li>");
					}else{
						sb.append("<li>title_bordered node doesn't exist in aem.</li>");
					}
				}
				}else{
					sb.append("<li>div class c23-pilot not found in dom</li>");
				}
			} catch (Exception e) {
				sb.append("<li>Unable to update benefits tile_bordered component.\n</li>");
				e.printStackTrace();
			}
			// End of benefit list right rail.
			// -----------------------------------------------------------------------------------------------------
			
			session.save();

		} catch (Exception e) {
			sb.append("<li>UnKnown Error.\n</li>");
		}
		
		sb.append("</ul></td>");
		
		return sb.toString();
	}
}
