/* 
 * S.No		Name			Description of change
 * #1		Rohan 			Modified the html report related comments.
 * #2		Rohan			Added logic to get the title of tile bordered component when title is not in a h2 tag. 
 * 
 * */

package com.cisco.dse.global.migration.benefit;

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

import com.cisco.dse.global.migration.config.FrameworkUtils;
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

	static Logger log = Logger.getLogger(ServiceProviderBenefits.class);
	
	// Repo node paths

	String benefitLeft = "/content/<locale>/products/<prod>/benefit/jcr:content/content_parsys/benefits/layout-benefits/gd12v2/gd12v2-left";
	String benefitRight = "/content/<locale>/products/<prod>/benefit/jcr:content/content_parsys/benefits/layout-benefits/gd12v2/gd12v2-right";
		
	
	public String translate(String host,String loc, String prod, String type,
			String locale, Session session) throws IOException,
			ValueFormatException, VersionException, LockException,
			ConstraintViolationException, RepositoryException {
		String pagePropertiesPath = "/content/<locale>/products/<prod>/benefit/jcr:content";
		String pageUrl = host+"/content/<locale>/products/<prod>/benefit.html";
		benefitLeft = benefitLeft.replace("<locale>", locale).replace("<prod>", prod);
		benefitRight = benefitRight.replace("<locale>", locale).replace("<prod>", prod);
		pagePropertiesPath = pagePropertiesPath.replace("<locale>", locale).replace("<prod>", prod);
		pageUrl = pageUrl.replace("<locale>", locale).replace("<prod>", prod);
		
		sb.append("<td>" + "<a href="+pageUrl+">"+pageUrl+"</a>"+"</td>");
		sb.append("<td>" + "<a href="+loc+">"+loc +"</a>"+ "</td>");
		sb.append("<td><ul>");
		
		javax.jcr.Node benefitLeftNode = null;
		javax.jcr.Node benefitRightNode = null;
		javax.jcr.Node pageJcrNode = null;
		try {

			benefitLeftNode = session.getNode(benefitLeft);
			benefitRightNode = session.getNode(benefitRight);
			pageJcrNode = session.getNode(pagePropertiesPath);
			try {
							
				doc = Jsoup.connect(loc).get();
				log.debug("Connected to "+loc);
				if ("me_ar".equals(locale)) {
					sb.append("<li>The alignment of content for all arabic pages post migration will be from left to right by default. This has to be corrected manually.\n </li>");
				}
				
			} catch (Exception e) {
				sb.append("<li>Cannot Connect to given URL. \n</li>");
				log.error("Exception : "+e);
			}

			title = doc != null? doc.title() : "";

			// ------------------------------------------------------------------------------------------------------------------------------------------
			// start set page properties.
			
			FrameworkUtils.setPageProperties(pageJcrNode, doc, session, sb);
			
			// end set page properties.
			// ------------------------------------------------------------------------------------------------------------------------------------------
						
			// start set unified computing benifit text properties.
			try {
				NodeIterator textNodes  = benefitLeftNode.getNodes("text*");
				Elements benefitTextElements = doc.select("div.c00-pilot");
				javax.jcr.Node textNode = null;
				for (Element benefitTextElement:benefitTextElements) {
					String benefitText = benefitTextElement.html();
					if (textNodes.hasNext())
						textNode = textNodes.nextNode();
					if (textNode != null) {
						textNode.setProperty("text", benefitText);
					}
					log.debug("Updated text at "+textNode.getPath());
				}
				
			} catch (Exception e) {
				sb.append("<li>Unable to update unified computing benefits text component.</li>");
				log.error("Exception : "+e);
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
					log.debug("Updated title, description, linktext and linkurl at "+heroLargeNode.getPath());
				}
				}
			} catch (Exception e) {
				sb.append("<li>Unable to update unified computing benefits hero_large component.</li>");
				log.error("Exception : "+e);
			}
				//end set unified computing benefit hero properties.
				// #1 start - Commented the below 2 lines
				/* sb.append("<li>could not find c26v4_popup_cq component on left</li>");
				sb.append("<li>could not find c26v4_popup_cq component on right rail</li>"); */
				// #1 end
				
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
						String linkUrl = spotLightAnchor.attr("href");
						spotLightNode.setProperty("title", spotLightTitle.text());
						spotLightNode.setProperty("description", spotLightDescription.text());
						spotLightNode.setProperty("linktext", linkText);
						javax.jcr.Node ctaNode = spotLightNode.getNode("cta");
						ctaNode.setProperty("url", linkUrl);
						log.debug("Updated title, descriptoin and linktext at "+spotLightNode.getPath());
					}
				}
				} catch (Exception e) {
					sb.append("<li>Unable to update unified computing benefits spotlight_medium_v2 component.</li>");
					log.error("Exception : "+e);
				} 
				//end set unified computing benefit spotlight properties.
				//start set unified computing benefit tile_bordered properties.
				try {
					//
					//
					NodeIterator tileBorderedNodes  = benefitRightNode.getNodes("tile_bordered*");
					Elements rightRail = doc.select("div.c23-pilot");
					if (rightRail.size() != benefitRightNode.getNodes("tile_bordered*").getSize()) {
                        sb.append("<li>Mis-match in tile bordered panel count in the right rail.</li>");
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
							if (title.isEmpty()) {
								// #2 starts
								title = ele.getElementsByTag("h3").text();
								if (title.isEmpty()) {
									sb.append("<li>Title element for the right rail tile with node name "+tileBorderedNode.getName()+" is " +
											"not a h2 or h3 tag. Hence tile will not be migrated properly.</li>\n");
								}
								// #2 ends
							}
							tileBorderedNode.setProperty("title", title);
							tileBorderedNode.setProperty("description", desc);
							tileBorderedNode.setProperty("linktext", anchorText);
							tileBorderedNode.setProperty("linkurl", anchorHref);
							log.debug("Updated title, description, linktext and linkurl at "+tileBorderedNode.getPath());
						}
					}

				} catch (Exception e) {
					sb.append("<li>Unable to update benefits tile_bordered component.</li>");
					log.error("Exception : ",e);
				}
			//end set benefit list.
			// start of benefit list right rail.
				
				if (doc.select("div.c46-pilot").size() > 0) {
					sb.append("<li>Additional c46-pilot component found in Right Rail\n</li>");
				}
				sb.append("<li>The node 'c26v4_popup_cq' is not available in the right rail for the locale page.</li>"); //#1
			session.save();

		} catch (Exception e) {
			sb.append("<li>Could not create the content due to some error.</li>");
			log.error("Exception : ",e);
		}
		
		sb.append("</ul></td>");
		return sb.toString();
	}
}
