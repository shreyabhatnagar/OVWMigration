/* 
 * S.No		Name	Date		Description of change
 * 1		kiran   29-jan-16	Added the Java file to handle the migration of web about pages.
 * 
 * */



package com.cisco.dse.global.migration.productlisting;

import java.io.IOException;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.cisco.dse.global.migration.config.BaseAction;
import com.cisco.dse.global.migration.config.Constants;
import com.cisco.dse.global.migration.config.FrameworkUtils;

public class ProductListingVariation8 extends BaseAction {
	Document doc;

	String title = null;

	StringBuilder sb = new StringBuilder(1024);

	String footerLinks = "{\"linktext\":\"<aaa>\",\"linkurl\":\"<bbb>\"}";

	static Logger log = Logger.getLogger(ProductListingVariation8.class);

	public String translate(String host,String loc, String prod, String type,
			String catType, String locale, Session session, Map<String, String> urlMap) throws IOException,
			ValueFormatException, VersionException, LockException,
			ConstraintViolationException, RepositoryException {
		BasicConfigurator.configure();
		log.debug("In the translate method");
		log.debug("In the translate method, catType is :" + catType);

		// Repo node paths
		String pagePropertiesPath = "/content/<locale>/"+catType+"/<prod>/product-listing/jcr:content";
		String midGridNodePath = "/content/<locale>/"
				+ catType
				+ "/<prod>/product-listing/jcr:content/content_parsys/products/layout-products/gd21v1/gd21v1-mid";

		String pageUrl = host + "/content/<locale>/"
				+ catType + "/<prod>/product-listing.html";

		pageUrl = pageUrl.replace("<locale>", locale).replace("<prod>", prod);
		pagePropertiesPath = pagePropertiesPath.replace("<locale>", locale).replace("<prod>", prod);
		sb.append("<td>" + "<a href=" + pageUrl + ">" + pageUrl + "</a>"
				+ "</td>");
		sb.append("<td>" + "<a href=" + loc + ">" + loc + "</a>" + "</td>");
		sb.append("<td><ul>");

		midGridNodePath = midGridNodePath.replace("<locale>", locale).replace(
				"<prod>", prod);
		Node indexMidLeftNode = null;
		javax.jcr.Node pageJcrNode = null;
		try {
			indexMidLeftNode = session.getNode(midGridNodePath);
			pageJcrNode = session.getNode(pagePropertiesPath);
			try {
				doc = Jsoup.connect(loc).get();
				log.debug("Connected to the provided URL");
			} catch (Exception e) {
				doc = getConnection(loc);
			}

			if(doc != null){

				title = doc.title();
				
				// ------------------------------------------------------------------------------------------------------------------------------------------
				// start set page properties.

				FrameworkUtils.setPageProperties(pageJcrNode, doc, session, sb);

				// end set page properties.
				// ------------------------------------------------------------------------------------------------------------------------------------------
				
				// start set text component.
				try {
					String h2TagVal = "";
					String pTagVal = "";
					Node textNodeOne = null;
					Node textNodeTwo = null;

					if(indexMidLeftNode.hasNode("text")){
						textNodeOne = indexMidLeftNode.getNode("text");
					}else{
						sb.append("<li> Text Node not found</li>");

					}

					if(indexMidLeftNode.hasNode("text_0")){
						textNodeTwo = indexMidLeftNode.getNode("text_0");
					}else{
						sb.append("<li> Text Node not found</li>");
					}

					Elements textElements = doc.select("div.c00v1-pilot");
					if(textElements.isEmpty()){
						textElements = doc.select("div.c00-pilot");
					}
					if(textElements.isEmpty()){
						textElements = doc.select("div.cc00-pilot");
					}
					if(textElements.isEmpty()){
						textElements = doc.select("div.no-border");
					}

					if (textElements != null && !textElements.isEmpty()) {
						Elements hElements = !textElements.select("h2").isEmpty()?textElements.select("h2"):textElements.select("h1");
						Element ele = hElements.first();
						if (ele != null) {
							Element textProp = ele.getElementsByTag("h1").first()!=null?ele.getElementsByTag("h1").first():ele.getElementsByTag("h2").first();
							log.debug("text property!: " + textProp);
							if(textProp != null){
								h2TagVal = textProp.outerHtml();
								if(textNodeOne != null){
									textNodeOne.setProperty("text", h2TagVal);
								}
								log.debug("h2TagVal property!: " + h2TagVal);
							} else {
								sb.append(Constants.CHILD_TEXT_ELEMENT_NOT_FOUND);
							}
						}
					} else {
						sb.append(Constants.TEXT_ELEMENT_NOT_FOUND);
					}

					Elements pTagElements = doc.select("div.c00v0-pilotno-border,c100v1-pilot no-border");
					if(pTagElements.isEmpty()){
						pTagElements = doc.select("div.no-border");
					}
					if(pTagElements != null && !pTagElements.isEmpty()){

						Elements pElements = pTagElements.select("p");
						Element pTag = pElements.first();
						Element pTagText = pTag.getElementsByTag("p").first();
						//log.debug("pTagText property!: " + pTagText);
						if(pTagText != null){
							pTagVal = FrameworkUtils.extractHtmlBlobContent(pTagText, "", locale, sb, urlMap);	
							if(textNodeTwo != null){
								textNodeTwo.setProperty("text", pTagVal);
							}

						}else if(pTag.parent().hasClass("c00-pilot") || pTag.parent().hasClass("cc00-pilot")){
							pTagText = pTag.getElementsByTag("p").first();
							if(pTagText != null){
								pTagVal = FrameworkUtils.extractHtmlBlobContent(pTagText, "", locale, sb, urlMap);	
								if(textNodeTwo != null){
									textNodeTwo.setProperty("text", pTagVal);
								}
							}

						}else{

							sb.append(Constants.CHILD_TEXT_ELEMENT_NOT_FOUND);
						}
					}else{

						sb.append(Constants.CHILD_TEXT_ELEMENT_NOT_FOUND);
					}
					
				} catch (Exception e) {
					sb.append("<li>" + Constants.EXCEPTION_TEXT_COMPONENT
							+ e + "</li>");
				}
				session.save();
			}
			else {
				sb.append(Constants.URL_CONNECTION_EXCEPTION);
			}
		} catch (Exception e) {
			sb.append("<li>unable to migrate page "+e+"</li>");
			log.debug("Exception as url cannot be connected: "+ e);
		}
		sb.append("</ul></td>");

		return sb.toString();
	}

}
