package com.cisco.dse.global.migration;

import java.io.IOException;

import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
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

	static Logger log = Logger.getLogger(ServiceProviderBenefits.class);
	
	String footerLinks = "{\"linktext\":\"<aaa>\",\"linkurl\":\"<bbb>\"}";

	// Repo node paths

	String benefitLeft = "/content/<locale>/solutions/<prod>/benefit/jcr:content/content_parsys/benefits/layout-benefits/gd12v2/gd12v2-left";
	String benefitRight = "/content/<locale>/solutions/<prod>/benefit/jcr:content/content_parsys/benefits/layout-benefits/gd12v2/gd12v2-right";
	String pageUrl = "http://chard.cisco.com:4502/content/<locale>/solutions/<prod>/benefit.html";
	
	public String translate(String loc, String prod, String type,
			String locale, Session session) throws IOException,
			ValueFormatException, VersionException, LockException,
			ConstraintViolationException, RepositoryException {

		benefitLeft = benefitLeft.replace("<locale>", locale).replace("<prod>", prod);
		benefitRight = benefitRight.replace("<locale>", locale).replace("<prod>", prod);
		pageUrl = pageUrl.replace("<locale>", locale).replace("<prod>", prod);
		
		javax.jcr.Node benefitLeftNode = null;
		javax.jcr.Node benefitRightNode = null;
		
		sb.append("<td>" + "<a href="+pageUrl+">"+pageUrl+"</a>"+"</td>");
		sb.append("<td>" + "<a href="+loc+">"+loc +"</a>"+ "</td>");
		sb.append("<td><ul>");
		
		
		try {

			benefitLeftNode = session.getNode(benefitLeft);
			benefitRightNode = session.getNode(benefitRight);

			
			try {
				doc = Jsoup.connect(loc).get();
				log.debug("Connected to "+doc.baseUri());
				if ("me_ar".equals(locale)) {
					sb.append("<li>The alignment of content for all arabic pages post migration will be from left to right by default. This has to be corrected manually.\n </li>");
				}
				
			} catch (Exception e) {
				sb.append("<li>Cannot Connect to given URL.</li>");
				log.error("Exception : ",e);
			}

			title = doc.title();

			// start set unified computing benifit text properties.
			try {
				NodeIterator textNodes  = benefitLeftNode.getNodes("text*");
				Elements spTextElements = doc.select("div.c00-pilot");
				javax.jcr.Node textNode = null;
				for (Element spTextElement:spTextElements) {
					String spText = spTextElement.getElementsByTag("h2").first().outerHtml(); 
					String spParaText = spTextElement.getElementsByTag("p").outerHtml();
					if (!spTextElement.parent().parent().hasClass("gd23-pilot")) {
						if (textNodes.hasNext())
							textNode = textNodes.nextNode();
						if (textNode != null) {
							textNode.setProperty("text", spText.concat(spParaText));
						}
						log.debug("Updated text at "+textNode.getPath());
					}
				}
				
			} catch (Exception e) {
				sb.append("<li>Unable to update service provider benefits text component.</li>");
				log.error("Exception : ",e);
			}
			
			try {
				if (doc.select("div.c50-pilot").select("div.frame").size() > 0) {
					sb.append("<li>Additional Hero component found\n</li>");
				}
				if (doc.select("div.gd23-pilot").size() > 0) {
					sb.append("<li>Additional list component found\n</li>");
				}
			} catch (Exception e) {
				sb.append("<li>Unable to update benefits text component.</li>");
				log.error("Exception : ",e);
			}
			// end set unified computing benifit text properties.
			// start set spotlight_large_v2 component properties
			try {
				//
				//
				NodeIterator spotLightLargeNodes  = benefitLeftNode.getNodes("spotlight_large_v2*");
				Elements benefitSpotLightElements  = doc.select("div.c11-pilot");
				javax.jcr.Node spotLightNode = null;
				if (doc.select("div.c11-pilot").size() == 0) {
					sb.append("<li>could not find spotlight_large_v2 component\n</li>");
				}
				if (doc.select("div.htmlblob").size() == 0) {
					sb.append("<li>could not find htmlblob component\n</li>");
				}
				for (Element benefitSpotLightEle : benefitSpotLightElements) {
					Element spotLightTitle = benefitSpotLightEle.getElementsByTag("h2").first();
					
					Element spotLightDescription = benefitSpotLightEle.getElementsByTag("p").first();
					Element spotLightAnchor = benefitSpotLightEle.getElementsByTag("a").first();
					String linkText = spotLightAnchor.text();
					System.out.println(linkText);
					String linkUrl = spotLightAnchor.attr("href");
					System.out.println(linkUrl);
					if (spotLightLargeNodes.hasNext())
						spotLightNode = spotLightLargeNodes.nextNode();
					if (spotLightNode != null) {
						spotLightNode.setProperty("title", spotLightTitle.text());
						spotLightNode.setProperty("description", spotLightDescription.text());
						spotLightNode.setProperty("linktext", linkText);
						javax.jcr.Node ctaNode = spotLightNode.getNode("cta");
						ctaNode.setProperty("url", linkUrl);
						log.debug("Updated title, descriptoin and linktext at "+spotLightNode.getPath());
					}
				}

			} catch (Exception e) {
				sb.append("<li>Unable to update benefits tile_bordered component.</li>");
				log.error("Exception : ",e);
			}	
			// end set spotlight_large_v2 component properties
			//start set service provider tile_bordered properties.
			
			try {
				//
				//
				NodeIterator tileBorderedNodes  = benefitRightNode.getNodes("tile_bordered*");
				Elements rightRail = doc.select("div.c23-pilot");
				if (rightRail.size() != benefitRightNode.getNodes("tile_bordered*").getSize()) {
                    sb.append("<li>Mis-Match in tilebordered Panels count/content.</li>");
				}
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
						log.debug("<li>Updated title, description, linktext, linkurl at "+tileBorderedNode.getPath()+"</li>");
					}
				}

			} catch (Exception e) {
				sb.append("<li>Unable to update benefits tile_bordered component.</li>");
				log.error("Exception : ",e);
			}	
			//end set service provider tile_bordered properties.
			
			session.save();

		} catch (Exception e) {
			sb.append("<li>Unable to migrate the content due to some error.</li>");
			log.error("Exception : ",e);
		}
		
		sb.append("</ul></td>");
		return sb.toString();
	}
}
