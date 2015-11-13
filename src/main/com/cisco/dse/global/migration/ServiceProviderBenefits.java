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
public class ServiceProviderBenefits {

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

	String benefitLeft = "/content/<locale>/solutions/<prod>/benefit/jcr:content/content_parsys/benefits/layout-benefits/gd12v2/gd12v2-left";
	String benefitRight = "/content/<locale>/solutions/<prod>/benefit/jcr:content/content_parsys/benefits/layout-benefits/gd12v2/gd12v2-right";
	
	public String translate(String loc, String prod, String type,
			String locale, Session session) throws IOException,
			ValueFormatException, VersionException, LockException,
			ConstraintViolationException, RepositoryException {

		benefitLeft = benefitLeft.replace("<locale>", locale).replace("<prod>", prod);
		benefitRight = benefitRight.replace("<locale>", locale).replace("<prod>", prod);
		
		javax.jcr.Node benefitLeftNode = null;
		javax.jcr.Node benefitRightNode = null;
		
		sb.append("<td>"+"url"+"</td>");
		sb.append("<td>"+loc+"</td>");
		sb.append("<td><ul>");
		
		try {

			benefitLeftNode = session.getNode(benefitLeft);
			benefitRightNode = session.getNode(benefitRight);
			
			try {
				doc = Jsoup.connect(loc).get();
				sb.append("<li>Connected to "+doc.baseUri()+"</li>");
			} catch (Exception e) {
				sb.append("<li>Cannot Connect to given URL. \n</li>");
			}

			title = doc.title();

			// start set unified computing benifit text properties.
			try {
				Element spTextElement = doc.select("div.c00-pilot").first();
//				System.out.println("");
				String spText = null;
				int count = 0;
					spText = spTextElement.getElementsByTag("h2").first().outerHtml(); //spTextEle.html();
					String spParaText = spTextElement.getElementsByTag("p").outerHtml(); 
					benefitLeftNode.getNode("text").setProperty("text", spText);
					benefitLeftNode.getNode("text_1").setProperty("text", spParaText);
				sb.append("<li>Updated text and text_1 at "+benefitLeftNode.getPath()+"<li>");
			} catch (Exception e) {
				sb.append("<li>Unable to update service provider benefits text component.\n</li>");
			}
			try {
				sb.append("<li>could not find spotlight_large_v2 component\n</li>");
				sb.append("<li>could not find spotlight_large_v2_0 component\n</li>");
				sb.append("<li>could not find htmlblob component\n</li>");
				sb.append("<li>could not find text_0 component\n</li>");
			} catch (Exception e) {
				sb.append("<li>Unable to update benefits text component.\n</li>");
			}
			try {
				if (doc.select("div.c50-pilot").select("div.frame").size() > 0) {
					sb.append("<li>Additional Hero component found\n</li>");
				}
				if (doc.select("div.gd23-pilot").size() > 0) {
					sb.append("<li>Additional list component found\n</li>");
				}
			} catch (Exception e) {
				sb.append("<li>Unable to update benefits text component.\n</li>");
			}
			// end set unified computing benifit text properties.
			//start set service provider tile_bordered properties.
			
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
						sb.append("<li>Updated title, description, linktext, linkurl at "+tileBorderedNode.getPath()+"</li>");
					}
				}

			} catch (Exception e) {
				sb.append("<li>Unable to update benefits tile_bordered component.\n</li>");
			}	
			//end set service provider tile_bordered properties.
			
			session.save();

		} catch (Exception e) {
			sb.append("<li>UnKnown Error.\n</li>");
		}
		
		sb.append("</ul></td>");
		return sb.toString();
	}
}
