package com.cisco.dse.global.migration;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import javax.jcr.*;

import org.apache.jackrabbit.commons.JcrUtils;

import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.version.VersionException;


import org.apache.sling.commons.json.JSONObject;
import org.apache.commons.lang.StringUtils; 
public class UnifiedComputingBenefits {

	Document doc;
	String title = null;
	Element fProduct = null;
	Elements fProdTitle = null;
	String fProductsTitle = null;

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
	
	public String translate(String loc, String prod, String type,
			String locale, Session session) throws IOException,
			ValueFormatException, VersionException, LockException,
			ConstraintViolationException, RepositoryException {

		benefitLeft = benefitLeft.replace("<locale>", locale).replace("<prod>", prod);
		benefitRight = benefitRight.replace("<locale>", locale).replace("<prod>", prod);

		
		sb.append("<td>"+"url"+"</td>");
		sb.append("<td>"+loc+"</td>");
		sb.append("<td><ul>");

		
		javax.jcr.Node benefitLeftNode = null;
		javax.jcr.Node benefitRightNode = null;
		try {

			benefitLeftNode = session.getNode(benefitLeft);
			benefitRightNode = session.getNode(benefitRight);
			
			try {

				doc = Jsoup.connect(loc).get();
				sb.append("<li>"+doc.baseUri()+"</li>");
			} catch (Exception e) {
				sb.append("<li>Cannot Connect to given URL. \n</li>");
			}

			title = doc.title();

			// start set unified computing benifit text properties.
			try {
				Elements benefitTextElements = doc.select("div.c00-pilot");
				
				String benefitText = null;
				int count = 0;
				for (Element benefitTextEle : benefitTextElements) {
					benefitText = benefitTextEle.html();
					if (count == 0) {
						benefitLeftNode.getNode("text").setProperty("text", benefitText);
						count++;
					} else {
						benefitLeftNode.getNode("text_"+(count-1)).setProperty("text", benefitText);
						count++;
					}
					
				}
				
			} catch (Exception e) {
				sb.append("<li>Unable to update unified computing benefits text component.\n</li>");
			}

			// end set unified computing benifit text properties.
			
			
			try {
				//start set unified computing benifit hero properties.
				Elements benefitHeroPanelElements = doc.select("div.c50-pilot").select("div.heropanel");
				javax.jcr.Node heroLargeNode = null;
				if (benefitLeftNode.hasNode("hero_large")) {
					heroLargeNode = benefitLeftNode.getNode("hero_large");
				}
				if (heroLargeNode != null) {
				for (Element benefitHeroPanelEle : benefitHeroPanelElements) {
					Element textDiv= benefitHeroPanelEle.select("div.c50-text").first();
					String heroPanelTitle = textDiv.getElementsByTag("h2").first().text();
					String heroPanelDescription = textDiv.getElementsByTag("p").first().text();
					Element ctaLink = textDiv.select("p.cta-link").first();
					Element ctaAnchor = ctaLink.getElementsByTag("a").first();
					String linkText = ctaAnchor.text();
					String linkUrl = ctaAnchor.attr("href");
					heroLargeNode.getNode("heropanel_0").setProperty("title", heroPanelTitle);
					heroLargeNode.getNode("heropanel_0").setProperty("description", heroPanelDescription);
					heroLargeNode.getNode("heropanel_0").setProperty("linktext", linkText);
					heroLargeNode.getNode("heropanel_0").setProperty("linkurl", linkUrl);
					sb.append("<li>Updated title, description, linktext and linkurl at "+heroLargeNode.getPath()+"</li>");
				}
				}
			} catch (Exception e) {
				sb.append("<li>Unable to update unified computing benefits hero_large component..\n"+e+"</li>");
			}
				//end set unified computing benefit hero properties.
				try {
					sb.append("<li>could not find c26v4_popup_cq component\n</li>");
					sb.append("<li>could not find c26v4_popup_cq component\n</li>");
					sb.append("<li>could not find htmlblob component\n</li>");
					sb.append("<li>could not find text_0 component\n</li>");
				} catch (Exception e) {
					sb.append("<li>Unable to find components.\n</li>");
				}
				//start set unified computing benefit spotlight properties.
				try {
				Elements benefitSpotLightElements = doc.select("div.c11-pilot");
				javax.jcr.Node spotLightNode = null;
				if (benefitLeftNode.hasNode("spotlight_medium_v2")) {
					spotLightNode = benefitLeftNode.getNode("spotlight_medium_v2");
				}
				if (spotLightNode != null) {
					for (Element benefitSpotLightEle : benefitSpotLightElements) {
						Element spotLightTitle = benefitSpotLightEle.getElementsByTag("h2").first();
						
						Element spotLightDescription = benefitSpotLightEle.getElementsByTag("p").first();
						Element spotLightAnchor = benefitSpotLightEle.getElementsByTag("a").first();
						String linkText = spotLightAnchor.text();
						System.out.println(linkText);
						String linkUrl = spotLightAnchor.attr("href");
						System.out.println(linkUrl);
						spotLightNode.setProperty("title", spotLightTitle.text());
						spotLightNode.setProperty("description", spotLightDescription.text());
						spotLightNode.setProperty("linktext", linkText);
						javax.jcr.Node ctaNode = spotLightNode.getNode("cta");
						ctaNode.setProperty("url", linkUrl);
						sb.append("<li>Updated title, descriptoin and linktext at "+spotLightNode.getPath()+"</li>");
					}
				}
				} catch (Exception e) {
					sb.append("<li>Unable to update unified computing benefits spotlight_medium_v2 component..\n"+e+"</li>");
				} 
				//end set unified computing benefit spotlight properties.
				//start set unified computing benefit tile_bordered properties.
				try {
					//
					//
					NodeIterator tileBorderedNodes  = benefitRightNode.getNodes("tile_bordered*");
					Elements rightRail = doc.select("div.c23-pilot");
					javax.jcr.Node tileBorderedNode = null;

					for (Element ele : rightRail) {

						String title = ele.getElementsByTag("h2").text();
						String desc = ele.getElementsByTag("p").text();
						Elements anchor = ele.getElementsByTag("a");

						String anchorText = anchor.text();
						String anchorHref = anchor.attr("href");
						if (tileBorderedNodes.hasNext())
							tileBorderedNode = tileBorderedNodes.nextNode();
						if (tileBorderedNode != null) {
							tileBorderedNode.setProperty("title", title);
							tileBorderedNode.setProperty("description", desc);
							tileBorderedNode.setProperty("linktext", anchorText);
							tileBorderedNode.setProperty("linkurl", anchorHref);
							sb.append("<li>Updated title, description, linktext and linkurl at "+tileBorderedNode.getPath()+"</li>");
						}
					}

				} catch (Exception e) {
					sb.append("<li>Unable to update benefits tile_bordered component.\n"+e+"</li>");
				}
			//end set benefit list.
			// start of benefit list right rail.

			session.save();
			
		} catch (Exception e) {
			sb.append("<li>UnKnown Error.\n"+e+"</li>");
		}
		
		sb.append("</ul></td>");
		
		return sb.toString();
	}
}
