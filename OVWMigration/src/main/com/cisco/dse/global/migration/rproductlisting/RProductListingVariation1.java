package com.cisco.dse.global.migration.rproductlisting;

/* 
 * S.No     	Name                 Date                    Description of change
 *  #1         Saroja            21-Dec-15           Added the Java file to handle the migration of product listing responsive page(s).
 * 
 * */

import java.io.IOException;

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

public class RProductListingVariation1 extends BaseAction{
	
	Document doc;


	StringBuilder sb = new StringBuilder(1024);

	String footerLinks = "{\"linktext\":\"<aaa>\",\"linkurl\":\"<bbb>\"}";

	static Logger log = Logger.getLogger(RProductListingVariation1.class);

	public String translate(String host,String loc, String prod, String type,
			String catType, String locale, Session session) throws IOException,
			ValueFormatException, VersionException, LockException,
			ConstraintViolationException, RepositoryException {
		BasicConfigurator.configure();
		log.debug("In the translate method");
		log.debug("In the translate method, catType is :" + catType);

		// Repo node paths
		String pagePropertiesPath = "/content/<locale>/"+catType+"/<prod>/product-listing/jcr:content";
		String gridFullNodePath = "/content/<locale>/"
				+ catType
				+ "/<prod>/product-listing/jcr:content/Grid/category/layout-category/full/Full";
		
		String gridNarrowWideNodePath = "/content/<locale>/"
				+ catType
				+ "/<prod>/product-listing/jcr:content/Grid/category/layout-category/narrowwide/NW-Wide-2";


		String pageUrl = host + "/content/<locale>/"
				+ catType + "/<prod>/product-listing.html";

		pageUrl = pageUrl.replace("<locale>", locale).replace("<prod>", prod);
		pagePropertiesPath = pagePropertiesPath.replace("<locale>", locale).replace("<prod>", prod);
		sb.append("<td>" + "<a href=" + pageUrl + ">" + pageUrl + "</a>"
				+ "</td>");
		sb.append("<td>" + "<a href=" + loc + ">" + loc + "</a>" + "</td>");
		sb.append("<td><ul>");

		gridFullNodePath = gridFullNodePath.replace("<locale>", locale).replace(
				"<prod>", prod);
		
		gridNarrowWideNodePath = gridNarrowWideNodePath.replace("<locale>", locale).replace(
				"<prod>", prod);
		Node gridNarrowWideNode = null;
		Node pageJcrNode = null;
		try {
			gridNarrowWideNode = session.getNode(gridNarrowWideNodePath);
			pageJcrNode = session.getNode(pagePropertiesPath);
			try {
				doc = Jsoup.connect(loc).get();
				log.debug("Connected to the provided URL");
			} catch (Exception e) {
				doc = getConnection(loc);
			}

			if(doc != null){

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

					if(gridNarrowWideNode.hasNode("text")){
						textNodeOne = gridNarrowWideNode.getNode("text");
					}else{
						sb.append(Constants.TEXT_NODE_NOT_FOUND);

					}

					if(gridNarrowWideNode.hasNode("text_0")){
						textNodeTwo = gridNarrowWideNode.getNode("text_0");
					}else{
						sb.append(Constants.TEXT_NODE_NOT_FOUND);
					}
					
					Element firstTextElement = doc.select("div.c00-pilot").first();
					if(firstTextElement == null){
						firstTextElement = doc.select("div.c00v1-pilot").first();
					}
					
					if(firstTextElement == null){
						firstTextElement = doc.select("div.cc00v1-pilot").first();
					}
					
					if (firstTextElement != null) {
						//Element eleh1 = 
						Element ele = firstTextElement.getElementsByTag("h1").first()!=null?firstTextElement.getElementsByTag("h1").first():firstTextElement.getElementsByTag("h2").first();
					//	Element ele = hElements.first();
						if (ele != null) {
							log.debug("text property element is !: " + ele);
								h2TagVal = ele.html();
								if(textNodeOne != null){
									textNodeOne.setProperty("text", h2TagVal);
								log.debug("h2TagVal property!: " + h2TagVal);
							} else {
								sb.append(Constants.CHILD_TEXT_ELEMENT_NOT_FOUND);
							}
						}
					} else {
						sb.append(Constants.TEXT_ELEMENT_NOT_FOUND);
					}

					Element pTagElements = doc.select("div.c100v1-pilot").first();
					
					if(pTagElements == null){
						pTagElements = doc.select("div.c00-pilot").last();
					}
					
					if(pTagElements == null){
						pTagElements = doc.select("div.cc00v1-pilot").first();
					}
					
					if(pTagElements == null){
						pTagElements = doc.select("div.c00v0-pilot").first();
					}
					
					if(pTagElements != null){

						Elements pElements = pTagElements.select("p");
						Element pTag = pElements.last();
						Element pTagText = pTag.getElementsByTag("p").last();
						log.debug("pTagText property!: " + pTagText);
						if(pTagText != null){
							pTagVal = pTagText.outerHtml();	
							if(textNodeTwo != null){
								textNodeTwo.setProperty("text", pTagVal);
							}

						}else{
							sb.append(Constants.CHILD_TEXT_ELEMENT_NOT_FOUND);
						}

						}else{

							sb.append(Constants.CHILD_TEXT_ELEMENT_NOT_FOUND);
						}
					
				} catch (Exception e) {
					sb.append(Constants.EXCEPTION_TEXT_COMPONENT
							);
					log.error("Exception in updating text component: ", e);
				}
				
				session.save();
			}
			else {
				sb.append(Constants.URL_CONNECTION_EXCEPTION);
			}
		} catch (Exception e) {
			sb.append(Constants.URL_CONNECTION_EXCEPTION);
			log.debug("Exception as url cannot be connected: ", e);
		}
		sb.append("</ul></td>");

		return sb.toString();
	}
}
