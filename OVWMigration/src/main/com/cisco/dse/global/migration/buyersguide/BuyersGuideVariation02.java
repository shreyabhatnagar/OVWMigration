/* 
 * S.No		Name	Date		Description of change
 * 1	    saikiran	22-Dec-15   	     Added the Java file to handle the migration of solution listing variation 9 page.
 * 
 * */



package com.cisco.dse.global.migration.buyersguide;

import java.io.IOException;
import java.util.Map;

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
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.cisco.dse.global.migration.config.BaseAction;
import com.cisco.dse.global.migration.config.Constants;
import com.cisco.dse.global.migration.config.FrameworkUtils;

public class BuyersGuideVariation02 extends BaseAction{
	Document doc;
	StringBuilder sb = new StringBuilder(1024);
	static Logger log = Logger.getLogger(BuyersGuideVariation02.class);
	
	public String translate(String host, String loc, String prod, String type,
			String catType, String locale, Session session, Map<String, String> urlMap) throws IOException,
			ValueFormatException, VersionException, LockException,
			ConstraintViolationException, RepositoryException {

		BasicConfigurator.configure();
		log.debug("In the translate method");
		log.debug("In the translate method, catType is :" + catType);
		String pagePropertiesPath = "/content/<locale>/" + catType
				+ "/<prod>/buyers-guide/jcr:content";
		

		// Repo node paths
		
		String buyersTop = "/content/<locale>/"
				+ catType
				+ "/<prod>/buyers-guide/jcr:content/content_parsys/comparison/layout-comparison/gd01v1/gd01v1-mid/c17v1_cq/c17v1-parsys-forTabs";
		
		
		String pageUrl = host + "/content/<locale>/" + catType
				+ "/<prod>/buyers-guide.html";
		

		pageUrl = pageUrl.replace("<locale>", locale).replace("<prod>", prod);
		pagePropertiesPath = pagePropertiesPath.replace("<locale>", locale)
				.replace("<prod>", prod);
	
		buyersTop = buyersTop.replace("<locale>", locale).replace("<prod>",prod);
		
		sb.append("<td>" + "<a href=" + pageUrl + ">" + pageUrl + "</a>"
				+ "</td>");
		sb.append("<td>" + "<a href=" + loc + ">" + loc + "</a>" + "</td>");
		sb.append("<td><ul>");
		
		Node buyersTopNode = null;
		
		Node pageJcrNode = null;
		
		  try {
				  	
	        	buyersTopNode = session.getNode(buyersTop);
	        	
	        	log.debug("buyersTopNode 22222222 : "+buyersTopNode.getPath());
	        	
	        	
	        	pageJcrNode = session.getNode(pagePropertiesPath);
	        
	        	log.debug("pageJcrNode : 2222222 : "+pageJcrNode.getPath());
	        	
	        	doc = getConnection(loc);
	        	 if (doc != null) {
	 				
	 				// ------------------------------------------------------------------------------------------------------------------------------------------
	 				// start set page properties.

	 				FrameworkUtils.setPageProperties(pageJcrNode, doc, session, sb);

	 				// end set page properties.
	 				// ------------------------------------------------------------------------------------------------------------------------------------------
	 			    // start of htmlblob component
	 				// start of get content
					try {
						String htmlContent="";
						
						
					   
						
						
						log.debug("Started migrating HtmlBlob content.");
						Element htmlBlobElements = doc.select("div.c17v1").first();
						
						
						
						
						
						Elements tabSetElements = htmlBlobElements.select("div.tab");
					
						Elements elemts = htmlBlobElements.select("div.n12-pilot,div.item");
						Element textDesc= htmlBlobElements.select("div.c00-pilot").last();
						
						int htmlEleSize = tabSetElements.size();
						NodeIterator htmlNodeIterator= null;
						if(buyersTopNode != null){
							htmlNodeIterator = buyersTopNode.getNodes("c17_item_cq*");
							
							log.debug("htmlNodeIterator : "+htmlNodeIterator.getSize());
						}
						log.debug("Outside loop");
						if(htmlNodeIterator!=null){
						log.debug("Inside it");
						int htmlNodeSize = (int)htmlNodeIterator.getSize();	
						log.debug("Element "+htmlEleSize);
						log.debug("Node "+htmlNodeSize);
						//Node htmlNode = null;
						if(htmlEleSize==htmlNodeSize){
							log.debug("Next one");
							int i=0;
							for(Element ele : tabSetElements){
								
								Element aEle = ele.getElementsByTag("a").first();
								Elements pEle = textDesc.getElementsByTag("p");
								
								//String tabId = ele.getElementsByTag("td").attr("id");
								Node htmlNode = null;
								if(htmlNodeIterator.hasNext()){
									htmlNode = (Node)htmlNodeIterator.next();
								
									htmlNode.setProperty("tabTitle",aEle.text());
									//htmlNode.setProperty("tabID",tabId);
									
									
									
									Node parsysNode = htmlNode.hasNode("c17v1-parsys-forTabContent")?htmlNode.getNode("c17v1-parsys-forTabContent"):null;
									if(parsysNode!=null){
										log.debug("inside second loop");
										Node htmlblobNode = parsysNode.hasNode("htmlblob")?parsysNode.getNode("htmlblob"):null;
										if(htmlblobNode!=null){
											
											
											String rawHtml = FrameworkUtils.extractHtmlBlobContent(elemts.get(i), "", locale, sb, urlMap, catType, type);
										   // log.debug("rawhtml:"+elemts.get(2).html());
											htmlblobNode.setProperty("html",rawHtml );
											
											i++;
										}
										Node textNode = parsysNode.hasNode("text")?parsysNode.getNode("text"):null;
										if(textNode!=null){
											
											textNode.setProperty("text",pEle.text());
										}
									}
								}
							}
													log.debug("After setBlob");
						}
						else{
							log.debug("Next one");
							int i=0;
							for(Element ele : tabSetElements){
								
								Element aEle = ele.getElementsByTag("a").first();
							//	Elements pEle = textDesc.getElementsByTag("p");
								
								//String tabId = ele.getElementsByTag("td").attr("id");
								Node htmlNode = null;
								if(htmlNodeIterator.hasNext()){
									htmlNode = (Node)htmlNodeIterator.next();
								
									htmlNode.setProperty("tabTitle",aEle.text());
									//htmlNode.setProperty("tabID",tabId);
									
									
									
									Node parsysNode = htmlNode.hasNode("c17v1-parsys-forTabContent")?htmlNode.getNode("c17v1-parsys-forTabContent"):null;
									if(parsysNode!=null){
										log.debug("inside second loop");
										Node htmlblobNode = parsysNode.hasNode("htmlblob")?parsysNode.getNode("htmlblob"):null;
										if(htmlblobNode!=null){
											
											
											String rawHtml = FrameworkUtils.extractHtmlBlobContent(elemts.get(i), "", locale, sb, urlMap, catType, type);
											
											htmlblobNode.setProperty("html",rawHtml );
											
											i++;
										}
										/*Node textNode = parsysNode.hasNode("text")?parsysNode.getNode("text"):null;
										if(textNode!=null){
											textNode.setProperty("text",pEle.text());
										}*/
									}
								}
							}
													
							sb.append("mismatch as Html blob size is:"+htmlEleSize +"and node size is:" +htmlNodeSize);
							log.debug("After setBlob");
						}
						}
						
						
						
						
						// End get content.
						
					} catch (Exception e) {
						log.error("Exception : ", e);
						sb.append(Constants.EXCEPTION_IN_HTMLBLOB);
					}
					// end htmlblob component.
					// ------------------------------------------------------------------------------------------------------------------------------------------
					session.save();
	        	 }else {
	  				sb.append(Constants.URL_CONNECTION_EXCEPTION);
	  			}
	        	 
	  		} catch (Exception e) {
	  			sb.append(Constants.URL_CONNECTION_EXCEPTION);
	  			log.error("--------------------start-------------------------");
	  			log.error("Exception as url cannot be connected: " ,e);
	  			log.error("----------------------end-----------------------------");
	  		}

	  		sb.append("</ul></td>");
	  		
	  		return sb.toString();
	}
}
	
	