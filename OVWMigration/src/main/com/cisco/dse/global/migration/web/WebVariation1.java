/* 
 * S.No		Name	Date		Description of change
 * 1		kiran   11-jan-16	Added the Java file to handle the migration of web about pages.
 * 
 * */

package com.cisco.dse.global.migration.web;

import java.io.IOException;
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
import org.apache.log4j.Logger;
import org.apache.sling.commons.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.cisco.dse.global.migration.config.BaseAction;
import com.cisco.dse.global.migration.config.Constants;
import com.cisco.dse.global.migration.config.FrameworkUtils;



public class WebVariation1 extends BaseAction{
	Document doc;
	StringBuilder sb = new StringBuilder(1024);
	static Logger log = Logger.getLogger(WebVariation1.class);
	
	int noImageCount = 0;
	
	public String translate(String host, String loc, String prod, String type,
			String catType, String locale, Session session, Map<String, String> urlMap) throws IOException,
			ValueFormatException, VersionException, LockException,
			ConstraintViolationException, RepositoryException {
		log.debug("In the translate method of WebVariation1");
		log.debug("In the translate method, catType is :" + catType);

		String pagePropertiesPath = "/content/<locale>/" + catType
				+ "/jcr:content";
		String pageUrl = host + "/content/<locale>/" + catType
				+ ".html";
		pagePropertiesPath = pagePropertiesPath.replace("<locale>", locale)
				.replace("<prod>", prod);
		pageUrl = pageUrl.replace("<locale>", locale).replace("<prod>", prod);
		
		sb.append("<td>" + "<a href=" + pageUrl + ">" + pageUrl + "</a>"
				+ "</td>");
		sb.append("<td>" + "<a href=" + loc + ">" + loc + "</a>" + "</td>");
		sb.append("<td><ul>");
		
		String webNodePath = "/content/<locale>/"
				+ catType
				+ "/jcr:content/content_parsys/generic/layout-generic/gd12v2";
		//String webBottomNodePath = "/content/<locale>/"
				//+ catType
			//	+ "/<prod>//content/en/us/solutions/test-wp-on-wem/about/jcr:content/content_parsys/generic/layout-generic/gd12v2/gd12v2-right";

		webNodePath = webNodePath.replace("<locale>",
				locale).replace("<prod>", prod);
		/*webBottomNodePath = webBottomNodePath.replace("<locale>",
				locale).replace("<prod>", prod);*/
		javax.jcr.Node webNode = null;
	//	javax.jcr.Node webBottomNode = null;
		javax.jcr.Node pageJcrNode = null;
		try {

			webNode = session.getNode(webNodePath);
		//	webBottomNode = session.getNode(webBottomNodePath);
			pageJcrNode = session.getNode(pagePropertiesPath);
			try {
				doc = getConnection(loc);
			} catch (Exception e) {
				log.error("Exception ", e);
			}
			if (doc != null) {
				// ------------------------------------------------------------------------------------------------------------------------------------------
				// start set page properties.

				FrameworkUtils.setPageProperties(pageJcrNode, doc, session, sb);

				// end set page properties.
				// ------------------------------------------------------------------------------------------------------------------------------------------
				// start set hero medium component properties.			
				try {
					log.debug("Start of Hero component");
					Elements heroElements = doc.select("div.c50-pilot");
					int eleSize = heroElements.select("div.frame").size();
					Node heroNode = webNode.hasNode("gd12v2-left/hero_medium") ? webNode.getNode("gd12v2-left/hero_medium") : null;

					if (heroNode != null) {
						log.debug("hero node found: "+ heroNode.getPath());
						if (heroElements.isEmpty()) {
							log.debug("No hero element found with div class name frame.");
							sb.append("<li>Hero component with class name 'frame' does not exist on locale page.</li>");
						}
						else {
//							int eleSize = heroElements.size();
							log.debug("hero node element size: "+ eleSize);
							NodeIterator heroPanelNodeIterator = heroNode.getNodes("heropanel*");
							int nodeSize = (int)heroPanelNodeIterator.getSize();
							log.debug("hero node nodeSize : "+ nodeSize);
							if(eleSize == nodeSize){
								setForHero(heroElements,heroNode,locale,urlMap);
							}
							else if(nodeSize < eleSize){
								setForHero(heroElements,heroNode,locale,urlMap);
								sb.append("<li>Mismatch in the count of hero panels. Additional panel(s) found on locale page. Locale page has "+ eleSize +" panels and there are "+ nodeSize +" nodes.</li>");
							}
							else if (nodeSize > eleSize) {
								setForHero(heroElements,heroNode,locale,urlMap);
								sb.append("<li>Mismatch in the count of hero panels. Additional node(s) found. Locale page has "+ eleSize +" panels and there are "+ nodeSize +" nodes.</li>");
							}
						}
					}
					else {
						log.debug("No hero node found at "+webNode);
						sb.append("<li>Node for hero large component does not exist.</li>");
					}
					log.debug("End of Hero component");

				} catch (Exception e) {
					sb.append("<li>Unable to update hero large component.</li>");
				}		

				// end set Hero Large component properties
				//-------------------------------------------------------------
				// start of htmlblob component
				try {
					String htmlBlobContent = "";
					StringBuilder oldImage = new StringBuilder();
					
					log.debug("Started migrating HtmlBlob content.");
					// Start get content.
					Elements htmlBlobElements = doc.select("div.gd-left").select("div.c00-pilot");
					if (htmlBlobElements != null) {
						for(Element ele : htmlBlobElements )
						{
						htmlBlobContent = FrameworkUtils.extractHtmlBlobContent(ele, "", locale, sb, urlMap);
						oldImage.append(htmlBlobContent);
						}
					}
					//End of getContent
					//Start of set content
					if (webNode.hasNode("gd12v2-left/htmlblob")) {
						Node htmlBlobNode = webNode.getNode("gd12v2-left/htmlblob");
						if (!StringUtils.isEmpty(htmlBlobContent)) {
							htmlBlobNode.setProperty("html",oldImage.toString());
							log.debug("HtmlBlob Content migrated is done.");
						}
					} else {
						sb.append(Constants.HTMLBLOB_NODE_NOT_FOUND);
					}
				}	
				catch (Exception e) {
					sb.append(Constants.EXCEPTION_IN_HTMLBLOB);
					log.error("Exception " , e);
				}
				

				// End get content.
			//End of htmlblob Component
			//-------------------------------------------------------------------------------------
			//start of right-grid Component
			//start of htmlblob Component
				try {
					String htmlBlobContent = "";
					StringBuilder oldImage = new StringBuilder();
					
					log.debug("Started migrating HtmlBlob content.");
					// Start get content.
					Elements htmlBlobElements = doc.select("div.gd-right");
					if (htmlBlobElements != null) {
						for(Element ele : htmlBlobElements )
						{
							htmlBlobContent = FrameworkUtils.extractHtmlBlobContent(ele, "", locale, sb, urlMap);
							oldImage.append(htmlBlobContent);
						}
					}
					//End of getContent
					//Start of set content
					if (webNode.hasNode("gd12v2-right/htmlblob")) {
						Node htmlBlobNode = webNode.getNode("gd12v2-right/htmlblob");
						if (!StringUtils.isEmpty(htmlBlobContent)) {
							htmlBlobNode.setProperty("html",oldImage.toString());
							log.debug("HtmlBlob Content migrated is done.");
						}
					} else {
						sb.append(Constants.HTMLBLOB_NODE_NOT_FOUND);
					}
				}

				
				
				
				
				catch (Exception e) {
					sb.append(Constants.EXCEPTION_IN_HTMLBLOB);
					log.error("Exception " , e);
				}
				// end htmlblob component.
				
				session.save();
			} else {
				sb.append(Constants.URL_CONNECTION_EXCEPTION);
			}
		} catch (Exception e) {
			sb.append(Constants.UNABLE_TO_MIGRATE_PAGE);
			log.debug("Exception as url cannot be connected: " + e);
			log.error("Exception " , e);
		}

		sb.append("</ul></td>");

		return sb.toString();

	}
	//start setting of heropanel
		public void heroPanelTranslate(Node heroPanelNode, Element ele, String locale,Map<String,String> urlMap) {

			try {			
				String title = ele.getElementsByTag("h2")!=null?ele.getElementsByTag("h2").first().text():"";
				String desc = ele.getElementsByTag("p")!=null?ele.getElementsByTag("p").first().text():"";

				Element anchor = ele.getElementsByTag("a").first();		
				String anchorText = anchor!=null?anchor.text():"";
				String anchorHref = anchor.attr("href");
				// Start extracting valid href
				log.debug("Before heroPanelLinkUrl" + anchorHref + "\n");
				anchorHref = FrameworkUtils.getLocaleReference(anchorHref, urlMap);
				log.debug("after heroPanelLinkUrl" + anchorHref + "\n");
				// End extracting valid href

				// start image
				String heroImage = FrameworkUtils.extractImagePath(ele, sb);
				log.debug("heroImage before migration : " + heroImage + "\n");
				if (heroPanelNode != null) {
					Node heroPanelPopUpNode = null;
					Elements lightBoxElements = ele.select("div.c50-image").select("a.c26v4-lightbox");
					if(lightBoxElements != null && !lightBoxElements.isEmpty()){
						Element lightBoxElement = lightBoxElements.first();
						heroPanelPopUpNode = FrameworkUtils.getHeroPopUpNode(heroPanelNode);
					}
					if (heroPanelNode.hasNode("image")) {
						Node imageNode = heroPanelNode.getNode("image");
						String fileReference = imageNode.hasProperty("fileReference")?imageNode.getProperty("fileReference").getString():"";
						heroImage = FrameworkUtils.migrateDAMContent(heroImage, fileReference, locale,sb);
						log.debug("heroImage after migration : " + heroImage + "\n");
						if (StringUtils.isNotBlank(heroImage)) {
							imageNode.setProperty("fileReference" , heroImage);
						}
					} else {
						sb.append("<li>hero image node doesn't exist</li>");
					}
					
					if(heroPanelPopUpNode != null){
						heroPanelPopUpNode.setProperty("popupHeader", title);
					}else{
						sb.append("<li>Hero content video pop up node not found.</li>");
					}
					
					heroPanelNode.setProperty("title", title);
					heroPanelNode.setProperty("description", desc);
					heroPanelNode.setProperty("linktext", anchorText);
					heroPanelNode.setProperty("linkurl", anchorHref);
				}
				// end image
				
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		public void setForHero(Elements heroElements, Node heroPanelMedium, String locale, Map<String, String> urlMap) {
			try {
				Value[] panelPropertiest = null;
				Property panelNodesProperty = heroPanelMedium.hasProperty("panelNodes") ? heroPanelMedium.getProperty("panelNodes") : null;
				if (panelNodesProperty.isMultiple()) {
					panelPropertiest = panelNodesProperty.getValues();
				}
				int i = 0;
				Node heroPanelNode = null;
				for (Element ele : heroElements) {
					if (panelPropertiest != null && i <= panelPropertiest.length) {
						String propertyVal = panelPropertiest[i].getString();
						if (StringUtils.isNotBlank(propertyVal)) {
							JSONObject jsonObj = new JSONObject(propertyVal);
							if (jsonObj.has("panelnode")) {
								String panelNodeProperty = jsonObj.get("panelnode").toString();
								heroPanelNode = heroPanelMedium.hasNode(panelNodeProperty) ? heroPanelMedium.getNode(panelNodeProperty) : null;
							}
						}
						i++;
					} else {
						sb.append("<li>No heropanel Node found.</li>");
					}
					heroPanelTranslate(heroPanelNode, ele, locale, urlMap);
				}
			} catch (Exception e) {
			}
		}
		
		//end setting of heropanel
		
}

					
						
					
