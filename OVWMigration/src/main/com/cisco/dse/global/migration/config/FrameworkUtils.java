/**
 * Copyright (C) 2014 Virtusa Corporation.
 * This file is proprietary and part of Virtusa LaunchPad.
 * LaunchPad code can not be copied and/or distributed without the express permission of Virtusa Corporation
 */

package com.cisco.dse.global.migration.config;


import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.ValueFormatException;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.log4j.Logger;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * The Class FrameworkUtils.
 * 
 * @author kumarip
 * 
 */

public class FrameworkUtils {

	/** The Constant LOG. */

	static Logger log = Logger.getLogger(FrameworkUtils.class);

	protected FrameworkUtils() {
		/**
		 * This is a constructor.
		 */
	}


	/**
	 * set page properties.
	 * @param jcr node 
	 * 			  the Node
	 * @param doc
	 *            the Document
	 * @param session
	 *            the Session
	 * @param sb
	 *       	  the StringBuilder
	 */
	public static void setPageProperties(javax.jcr.Node jcrNode, Document doc, Session session, StringBuilder sb) {
		System.out.println("inside setPageProperties");

		String description = "";
		Properties metaProp = new Properties();
		InputStream metaInput = null;
		try {
			// reading metadata.properties file
			String metafilename = "metadata.properties";
			metaInput = FrameworkUtils.class.getClassLoader().getResourceAsStream(
					metafilename);
			if (metaInput == null) {
				log.debug("input is null");
				return;
			}
			// load a properties file from class path, inside static method
			metaProp.load(metaInput);
			Enumeration<?> e = metaProp.propertyNames();
			while (e.hasMoreElements()) {
				String key = (String) e.nextElement();
				String value = metaProp.getProperty(key);
				System.out.println("Key : " + key + ", Value : " + value);
			}
			//getting html page property of meta data from properties file

			String descriptionHtmlProperty = StringUtils.isNotBlank(metaProp
					.getProperty("jcr:description")) ? metaProp
							.getProperty("jcr:description") : "";
							// getting meta data from html document
							Elements metas = doc.getElementsByTag("meta"); 
							if (metas != null) {
								for (Element meta : metas) { 
									if (meta.hasAttr("name") && meta.attr("name").equals(descriptionHtmlProperty)) { 
										description =  meta.attr("content"); 
										log.debug("description of document:::  " + description);
									}
								} 
							}

							// start check title of the page
							String pageTitle = doc.title();
							log.debug("pageTitle::::::: " + pageTitle);
							// end check title of the page

							//setting html meta data to as page properties
							if (jcrNode != null) {

								if (StringUtils.isNotBlank(description)) {
									jcrNode.setProperty("jcr:description", description);
								} else {
									sb.append("<li>meta data description doesn't exist </li>");
								}
								if (StringUtils.isNotBlank(pageTitle)) {
									pageTitle = pageTitle.substring(0,pageTitle.indexOf("- Cisco Systems"));
									if(pageTitle.trim().endsWith("- Cisco")){
                                        pageTitle = pageTitle.substring(0,pageTitle.lastIndexOf("- Cisco"));     
									}
									if (jcrNode.hasProperty("jcr:title")) {
										jcrNode.setProperty("jcr:title",pageTitle);
									}
									if (jcrNode.hasProperty("cisco:customHeadTitle")) {
										jcrNode.setProperty("cisco:customHeadTitle", pageTitle);
									}else{
										log.debug("Custom head title property not found");
									}
								} else {
									sb.append("<li>jcr title not set </li>");
								}
							} else {
								log.debug("jcr node doesn't exist");
							}
							session.save();
		} catch (Exception e) {
			log.debug("Unable to update metadata.", e);
		} finally {
			if (metaInput != null) {
				try {
					metaInput.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	public static String migrateDAMContent(String path, String imgRef,
			String locale, StringBuilder sb) {
		log.debug("In the migrateDAMContent to migrate : " + path);
		log.debug("Image path from the WEM node : " + imgRef);
		String newImagePath = null;
		Session session = getSession();

		try {
			
			//returning the image path if it have the current locale in it, since if the image is having locale then it is said to be already migrated.
			if(imgRef.indexOf(locale) != -1){
				return imgRef;
			}
			
			if (StringUtils.isNotBlank(path)) {
				//-----------------------------------------------------------------------------------------------
				//If the image path is having '/content/en/us' or  '/c/en/us' then if condition is satisfied to get the imgReference property from the current image path.
				if (path.indexOf("/content/en/us") != -1 || path.indexOf("/c/en/us") != -1) {
					log.debug("image path is being from /c/en/us : " + path);
					
					
				if(path.lastIndexOf(".img") != -1){	
					path = path.substring(0, path.lastIndexOf(".img"));//image path to get the node.
					//if the image path is having any domain then removing the domain from url.
					if (path.indexOf("http://") != -1 || path.indexOf("https://") != -1) {
						try {
							URL url = new URL(path);
							path = url.getPath();
						} catch (Exception e) {
							log.error("Excepiton : ", e);
						}
					}
					//'/c' is a short url, hence updating it with the long url to get the node structure.
					if (path.startsWith("/c/")) {
						path = path.replace("/c/", "/content/");
					}
					//image path should have 'jcr:content' instead of '_jcr_content'.
					if (path.indexOf("_jcr_content") != -1) {
						path = path.replace("_jcr_content", "jcr:content");
					}
					Node referenceNodePath = null;
					if(session.itemExists(path)){
						referenceNodePath = session.getNode(path);
					}else{
						log.debug("Path not found in the node structure : "+path);
					}
					if (referenceNodePath != null) {
						path = referenceNodePath.hasProperty("fileReference") ? referenceNodePath.getProperty("fileReference").getString() : "";
						log.debug("fileReference path for /c/en/us image path is : " + path);
					}else{
						log.debug("No fileReference path found in the path : ");
					}
				}
					
					
					log.debug("Hence retriving the file reference path is : " + path);
				}
				//--------------------------------------------------------------------------------------------------------
				//if the current image path is not having '/content/dam/en/us' or '/content/dam' then if block will be executed.
				if (StringUtils.isNotBlank(path)) {
				if (path.indexOf("/content/dam/en/us") == -1
						&& path.indexOf("/content/dam") == -1
						&& path.indexOf("/content/en/us") == -1
						&& path.indexOf("/c/dam/en/us") == -1
						&& path.indexOf("/c/dam") == -1
						&& path.indexOf("/c/en/us") == -1) {
					log.debug("Path of the image is not a wem image path.");
					//Adding the domain to the web image path, since to get the IO we need absolute url.
					if (path.indexOf("http:") == -1
							&& path.indexOf("https:") == -1) {
						log.debug("Adding domain to the image path.");
						path = path.trim();
						path = "http://www.cisco.com" + path;
					}
					//if there is fileReference property in the wem, then updating the path with 'assets' will be replaced with 'global/locale' and 'en/us' will be replaced with 'global/locale'.
					//if the fileReference property in the wem is blank then the web page image url, 'web' will be replaced with 'content/dam/global/locale', if there is no string 'web' image path then directly appending '/content/dam/global/locale' 
					if (StringUtils.isNotBlank(imgRef) && imgRef.indexOf(locale) == -1) {
						if(imgRef.indexOf("/en/us/")!=-1){
							imgRef = imgRef.replace("/en/us/", "/global/" + locale + "/");
						}else if(imgRef.indexOf("/en_us/")!=-1){
							imgRef = imgRef.replace("/en_us/", "/global/" + locale + "/");
						}else if(imgRef.indexOf("/assets/")!=-1){
							imgRef = imgRef.replace("/assets/", "/global/" + locale + "/");
						}
					} else {
						URL url = new URL(path);
						String imagePath = url.getPath();
						if (imagePath.startsWith("/web/")) {
							imgRef = imagePath.replace("/web/", "/content/dam/global/" + locale+"/");
						}else if(imagePath.toLowerCase().startsWith("/en/us/")){
							imgRef = imagePath.replace("/en/us/", "/content/dam/global/" + locale+"/");
							imgRef = imagePath.replace("/en/US/", "/content/dam/global/" + locale+"/");
						} else {
							imgRef = "/content/dam/global/" + locale + imagePath;
						}
					}
					//updating the name of the wem image path with the web image path.
					if (path.lastIndexOf("/") != -1 && imgRef.lastIndexOf("/") != -1) {
						String imageName = path.substring(path.lastIndexOf("/"), path.length());
						imgRef = imgRef.substring(0, imgRef.lastIndexOf("/")) + imageName;
					}
					newImagePath = setContentToDAM(path, imgRef, locale);//method to hit the service to migrate the image.
				}else if(path.startsWith("/c/en/us")||path.startsWith("/content/en/us")){
					return "";
				} else if (!path.equalsIgnoreCase(imgRef)) {//if the image path is form content dam and if the image paths of the wem and web are different when returning the web image path.
					log.debug("Path of the image is wem image path." + path);
					if (path.indexOf("http:") == -1 && path.indexOf("https:") == -1) {
						log.debug("Adding domain to the image path.");
						path = "http://www.cisco.com" + path;
					}
					URL url = new URL(path);
					path = url.getPath();
					log.debug("getPath : " + path);
					return path;
				} else {//if both the image paths are same in the wem and web then returning blank, which meaning to not update the fileReference.
					return "";
				}
			} else {
				log.debug("returning null : " + path);
				return null;
			}
			}
		} catch (Exception e) {
			log.error("Exception : ", e);
		}finally  {
			session.logout();
			session = null;
		}
		return newImagePath;
	}

	public static String setContentToDAM(String path, String imgPath, String locale) {
		log.debug("In the setContentToDAM method to migrate : " + path + " to " + imgPath);

		Properties prop = new Properties();
		InputStream input = null;
		String host = "";
		String domain = "";
		try{
			String filename = "config.properties";
			input = OVWMigration.class.getClassLoader().getResourceAsStream(filename);
			if (input == null) {
				log.debug("input is null");
			}
			// load a properties file from class path, inside static method
			prop.load(input);
			host = StringUtils.isNotBlank(prop.getProperty("serverurl")) ? prop.getProperty("serverurl") : "";
			domain = StringUtils.isNotBlank(prop.getProperty("domain")) ? prop.getProperty("domain") : "";
		}catch(Exception e){
			log.error("Exception : ",e);
		}

		HttpClient client = new HttpClient();
		HttpMethod method = new GetMethod(host + "/bin/services/DAMMigration?imgPath="
				+ path+"&imgRef="+imgPath+"&locale="+locale);
		Credentials defaultcreds = new UsernamePasswordCredentials("admin",
				"admin");
		AuthScope authscope = new AuthScope(domain, 4502,
				AuthScope.ANY_REALM);
		client.getState().setCredentials(authscope, defaultcreds);
		method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
				new DefaultHttpMethodRetryHandler(3, false));

		try {
			int statusCode = client.executeMethod(method);

			if (statusCode != HttpStatus.SC_OK) {
				log.debug("HTTP Method failed: " + method.getStatusLine());
			}
			byte[] responseBody = method.getResponseBody();
			String responseObj = new String(responseBody);
			log.debug("josn object from service respones.");
			log.debug(responseObj);
			JSONObject resObj = null;
			if(StringUtils.isNotBlank(responseObj)){
				resObj = new JSONObject(responseObj);
			}
			String newImagePath = "";
			String error = "";
			
			//code for connection timed out.
			for (int reconnection=0; reconnection<3; reconnection++) {
				log.debug("Count of reconnection" + reconnection);

				if(resObj != null && resObj.has("error") && !resObj.has("newImagePath")){
					error = (String) resObj.get("error");
					if(StringUtils.isNotBlank(error)){
						statusCode = client.executeMethod(method);

						if (statusCode != HttpStatus.SC_OK) {
							log.debug("HTTP Method failed: " + method.getStatusLine());
						}
						responseBody = method.getResponseBody();
						responseObj = new String(responseBody);
						log.debug("josn object from service respones.");
						log.debug(responseObj);
						resObj = null;
						if(StringUtils.isNotBlank(responseObj)){
							resObj = new JSONObject(responseObj);
						}
						error = "";
					}
				}else {
					break;
				}
			}
			
			if (resObj != null && resObj.has("newImagePath")) {
				newImagePath = (String) resObj.get("newImagePath");
				log.debug("Updated dam Image path : " + newImagePath);
			} else {
				log.debug("No 'newImagePath' found in service response.");
			}
			if (resObj != null && resObj.has("error")) {
				error = (String) resObj.get("error");
				if (StringUtils.isNotBlank(newImagePath)) {
					log.debug("Error in updating dam path : " + error);
				}
			} else {
				log.debug("No 'error' found in service response.");
			}
			return newImagePath;

		} catch (Exception e) {
			log.error("Exception : ", e);
		}
		return null;
	}

	/**
	 * extract image path.
	 * @param element 
	 * 		 the Element
	 * @param sb
	 *       the StringBuilder
	 */
	public static String extractImagePath(Element element, StringBuilder sb) {
		String imagePath = "";
		if (element != null) {
			Elements imageElements = element.getElementsByTag("img");
			if (imageElements != null) {
				Element imageElement = imageElements.first();
				if (imageElement != null) {
					imagePath =imageElement.absUrl("src");
				} 
			} 
		}
		log.debug("imagePath " + imagePath + "\n");
		return imagePath;
	}

	/**
	 * extract image path.
	 * @param htmlElement 
	 * 		 the Element
	 * @param sb
	 *       the StringBuilder
	 */
	/*	public static String extractHtmlBlobContent(Element htmlBlobElement, String fileReference, String locale, StringBuilder sb) {
		log.debug("In the extractHtmlBlobContent method.");
		String outeHtmlText = htmlBlobElement.outerHtml();
		String existingimagePath = "";
		String updatedImgPath = "";
		if (htmlBlobElement != null) {
			existingimagePath = extractImagePath(htmlBlobElement, sb);
			updatedImgPath = migrateDAMContent(existingimagePath, fileReference, locale,sb);

			log.debug(existingimagePath +" is updated to "+updatedImgPath);
			if(StringUtils.isNotBlank(existingimagePath) && StringUtils.isNotBlank(updatedImgPath)){
				outeHtmlText = outeHtmlText.replace(existingimagePath, updatedImgPath);
			}
		}
		return outeHtmlText;
	}*/

	//anudeep
	public static String extractHtmlBlobContent(Element htmlBlobElement, String fileReference, String locale, StringBuilder sb, Map<String, String> urlMap) {
		log.debug("In the extractHtmlBlobContent method.");
		String outeHtmlText = htmlBlobElement.outerHtml();
		List<String> existingimagePaths = null;
		String updatedImgPath = "";
		if (htmlBlobElement != null) {
			existingimagePaths = extractImagePaths(htmlBlobElement, sb);
			Iterator<String> iterator = existingimagePaths.iterator();
			while(iterator.hasNext()){
				String existingimagePath = iterator.next();
				updatedImgPath = migrateDAMContent(existingimagePath, fileReference, locale,sb);

				log.debug(existingimagePath +" is updated to "+updatedImgPath);
				if(StringUtils.isNotBlank(existingimagePath) && StringUtils.isNotBlank(updatedImgPath)){
					outeHtmlText = outeHtmlText.replace("\"" + existingimagePath + "\"", "\"" + updatedImgPath + "\"");
				}
			}
			Map<String, String> existingAnchorPaths = null;
			String updatedAnchorPath = "";
			existingAnchorPaths = extractAnchorLinks(htmlBlobElement, sb);
			Iterator anchorIterator = existingAnchorPaths.entrySet().iterator();
			while(anchorIterator.hasNext()){
				Map.Entry anchor = (Map.Entry)anchorIterator.next();
				String existingAnchorPath = anchor.getValue().toString();
				String existingAnchorAbsolutePath = anchor.getKey().toString();
				log.debug("Before anchorHref" + anchor.getKey().toString() + "\n");
				updatedAnchorPath = FrameworkUtils.getLocaleReference(anchor.getKey().toString(), urlMap, locale, sb);
				log.debug("after anchorHref" + updatedAnchorPath + "\n");

				log.debug(StringEscapeUtils.escapeXml(existingAnchorPath) +" is updated to "+updatedAnchorPath);
				if(StringUtils.isNotBlank(StringEscapeUtils.escapeXml(existingAnchorPath)) && StringUtils.isNotBlank(updatedAnchorPath)){
					outeHtmlText = outeHtmlText.replace("\"" +StringEscapeUtils.escapeXml(existingAnchorPath) + "\"", "\"" + updatedAnchorPath + "\"");
				}
				if(StringUtils.isNotBlank(StringEscapeUtils.escapeXml(existingAnchorAbsolutePath)) && StringUtils.isNotBlank(updatedAnchorPath)){
					outeHtmlText = outeHtmlText.replace("\"" +StringEscapeUtils.escapeXml(existingAnchorAbsolutePath) + "\"", "\"" + updatedAnchorPath + "\"");
				}
			}
		}
		return outeHtmlText;
	}

	private static Map<String, String> extractAnchorLinks(Element htmlBlobElement,
			StringBuilder sb) {
		Map<String, String> anchorPath =new HashMap<String, String>();
		if (htmlBlobElement != null) {
			Elements anchorElements = htmlBlobElement.getElementsByTag("a");
			log.debug("html outer html : " + htmlBlobElement.outerHtml());
			//			if (anchorElements != null) {
			if (!anchorElements.isEmpty()&&anchorElements!=null) {
				log.debug("anchorPath not null");
				for(Element anchorElement : anchorElements){
					log.debug("anchorPath::::" + anchorElement.attr("href"));
					String absAnchorPath = anchorElement.absUrl("href");
					log.debug("absolute anchorPath" + absAnchorPath);
					anchorPath.put(absAnchorPath, anchorElement.attr("href"));
				}
			} else {
				log.debug("anchorPath null");
			}
		}
		log.debug("anchorPath " + anchorPath + "\n");
		return anchorPath;
	}
	public static List<String> extractImagePaths(Element element, StringBuilder sb) {
		List<String> imagePath =new ArrayList<String>();
		if (element != null) {
			Elements imageElements = element.getElementsByTag("img");
			if (imageElements != null) {
				for(Element imgEle : imageElements){
					imagePath.add(imgEle.attr("src"));
				}
			} 
		}
		log.debug("imagePath " + imagePath + "\n");
		return imagePath;
	}
	//anudeep

	public static String getLocaleReference(String primaryCTALinkUrl, Map<String, String> urlMap, String locale, StringBuilder sb) {
		if (StringUtils.isNotBlank(primaryCTALinkUrl)) {
			String pdfPath = "";
			String query = "";
			if(primaryCTALinkUrl.endsWith(".pdf")|| primaryCTALinkUrl.endsWith(".PDF") || primaryCTALinkUrl.endsWith(".doc") || primaryCTALinkUrl.endsWith(".DOC") || primaryCTALinkUrl.endsWith(".docx") || primaryCTALinkUrl.endsWith(".DOCX") ){
				pdfPath = FrameworkUtils.migrateDAMContent(primaryCTALinkUrl, "", locale, sb);
				log.debug("pdf path after migraiton is: "+ pdfPath);
				return pdfPath;
				
			}
			if ((primaryCTALinkUrl.indexOf("?") != -1 || primaryCTALinkUrl.indexOf("#") != -1) && !primaryCTALinkUrl.endsWith(".html#top")) {
				String url = primaryCTALinkUrl;
				if (primaryCTALinkUrl.indexOf("?") != -1) {
					primaryCTALinkUrl = url.substring(0, url.indexOf("?"));
					query = url.substring(url.indexOf("?"));
				}
				if (primaryCTALinkUrl.indexOf("#") != -1) {
					primaryCTALinkUrl = url.substring(0, url.indexOf("#"));
					query = url.substring(url.indexOf("#"));
				}
				log.debug("primaryCTALinkUrl having query : "+ primaryCTALinkUrl);
				log.debug("query in primaryCTALinkUrl : "+ query);
			}
			if (urlMap.containsKey(primaryCTALinkUrl)) {
				if(primaryCTALinkUrl.endsWith(".html#top")){  //code to remove if #top is in provided url for "back to top" issue.
					log.debug("link with #top before trim : "+ primaryCTALinkUrl );
					primaryCTALinkUrl ="#top";
					log.debug("link with #top after trim : "+ primaryCTALinkUrl );
				}
				
				
				primaryCTALinkUrl = urlMap.get(primaryCTALinkUrl);
			}
			if (StringUtils.isNotBlank(query)) {
				primaryCTALinkUrl = primaryCTALinkUrl + query;
				log.debug("url with query:: " + primaryCTALinkUrl);
			}
			
		}
		return primaryCTALinkUrl;
	}

	public static Node getHeroPopUpNode(Node heroNode){
		try {
			log.debug("In the getPopUpNode method to get the pop up node for " + heroNode.getPath());
			String lightboxId = heroNode.hasProperty("lightboxid") ? heroNode.getProperty("lightboxid").getString() : "";
			Node heroLargeNodeParent = heroNode.getParent().getParent();
			NodeIterator heroPanelPopUpNodes = heroLargeNodeParent.getNodes("c26v4_popup_cq*");
			if(heroPanelPopUpNodes.getSize() == 0){
				heroPanelPopUpNodes = heroLargeNodeParent.getNodes("popup*");
			}
			if (heroPanelPopUpNodes.getSize() == 0 && heroLargeNodeParent != null) {
				Node heroLargeNodeSuperParent = heroLargeNodeParent.getParent().getParent();
				heroPanelPopUpNodes = heroLargeNodeSuperParent.getNodes("c26v4_popup_cq*");
				if(heroPanelPopUpNodes.getSize() == 0){
					heroPanelPopUpNodes = heroLargeNodeSuperParent.getNodes("popup*");
				}
			}
			if(heroPanelPopUpNodes.getSize() == 0){
				heroLargeNodeParent = heroNode.getParent();
				heroPanelPopUpNodes = heroLargeNodeParent.getNodes("c26v4_popup_cq*");
			}
			while (heroPanelPopUpNodes.hasNext()) {
				Node heroPanelPopUpNode = (Node) heroPanelPopUpNodes.next();
				String lightboxPopUpId = heroPanelPopUpNode.hasProperty("lightboxId") ? heroPanelPopUpNode.getProperty("lightboxId").getString() : "";
				log.debug("Hero Node lightboxId : "+lightboxId);
				log.debug("Hero PopUp Node lightboxId : "+lightboxPopUpId);
				if (StringUtils.isNotBlank(lightboxId) && StringUtils.isNotBlank(lightboxPopUpId)) {
					if (lightboxId.equals(lightboxPopUpId)) {
						return heroPanelPopUpNode;
					}
				}
			}
		}catch (ValueFormatException e) {
			log.error("ValueFormatException : ",e);
		} catch (PathNotFoundException e) {
			log.error("PathNotFoundException : ",e);
		} catch (RepositoryException e) {
			log.error("RepositoryException : ",e);
		}
		return null;
	}
	public static String UpdateHyperLinksInHtml(String htmlWEBContent,
			String htmlWEMContent, Document doc, StringBuilder sb) {
		log.debug("In the UpdateHyperLinksInHtml method");
		Map<String, String> map = new LinkedHashMap<String, String>();
		Element wemElement = null;
		if (doc != null) {
			//-------------------------------------------------------------------------------------------------------------------------------
			//start of Logic to retrieve all the hyper links text and url and save in a map.
			
			Element webElement = doc.html(htmlWEBContent);
			String title = "";
			Elements titleElements = webElement.select("h2.bdr-1");
			Elements titleLinks = webElement.select("div.c00v0-pilot");
			
			
			if(titleElements != null && !titleElements.isEmpty()){
				Element titleElement = titleElements.first();
				title = titleElement.ownText();
			}else{
				log.debug("No title found in web content with class 'bdr-1'");
			}
			
			String aHeadingText = "";
			String aHeadingLink = "";
			if(titleLinks != null && !titleLinks.isEmpty()){
				Element titleLink = titleLinks.first();
				Elements aElements = titleLink.getElementsByTag("a");
				if(aElements != null && !aElements.isEmpty()){
					Element aElement = aElements.first();
					aHeadingText = aElement.ownText();
					aHeadingLink = aElement.attr("href");
				}else{
					log.debug("No links found in the heading section.");
				}
			}else{
				log.debug("No links found in the heading section with in the class 'c00v0-pilot'.");
			}
			
			Elements liElements = webElement.getElementsByTag("li");
			if(liElements != null && !liElements.isEmpty()){
				for(Element ele : liElements){
					Elements aElements = ele.getElementsByTag("a");
					if(aElements != null && !aElements.isEmpty()){
						Element aElement = aElements.first();
						String atext = aElement.ownText();
						String aLink = aElement.attr("href");
						if(StringUtils.isNotBlank(atext)){
							if(StringUtils.isNotBlank(aLink)){
								map.put(atext, aLink);
							}else{
								log.debug("link href is blank in the element : "+aElement.html());
							}
						}else{
							log.debug("link text is blank in the element : "+aElement.html());
						}
					}else{
						log.debug("No a elements found in source html content.");
					}
				}
			}else{
				log.debug("No li elements found in source html content.");
			}
			
			//end of Logic to retrieve all the hyper links text and url and save in a map.
			//-------------------------------------------------------------------------------------------------------------------------------
			//start of logic to update all the links in the wem html content from the map.
			wemElement = doc.html(htmlWEMContent);
			Elements productContentElements = wemElement.select("div.product-content");
			if(productContentElements != null && productContentElements.isEmpty()){
				log.debug("'product-content' class not found, searching for li elements.");
				productContentElements = wemElement.getElementsByTag("li");
			}
			
			int i = 0;
			if(i<productContentElements.size()){
				Set<String> keys = map.keySet();
				for(String key : keys){
					String val = map.get(key);
					Element productElement = productContentElements.get(i);
					Elements anchorElements = productElement.getElementsByTag("a");
					
					if(anchorElements != null && !anchorElements.isEmpty()){
						Element anchorElement = anchorElements.first();
						anchorElement.attr("href", val);
						anchorElement.text(key);
					}else{
						log.debug("No Anchor element found in WEM Content.");
					}
					i++;
				}
			}else{
				log.debug("No elements found in the WEM Content with class 'product-content' or li tag.");
			}
			int webeleSize;
			int wemeleSize;
			if(( webeleSize = liElements.size())!=(wemeleSize = productContentElements.size())){
				sb.append("<li>size of links miss match "+webeleSize+" not equal to "+wemeleSize+".</li>");
			}
			//end of logic to update all the links in the wem html content from the map.
			//-------------------------------------------------------------------------------------------------------------------------------
			//Start of log to update the title of the content.
			
			log.debug("Title of the web page content : "+title);
			titleElements = wemElement.select("span.arrow");
			if(titleElements != null && !titleElements.isEmpty() && StringUtils.isNotBlank(title)){
				Element titleElement = titleElements.first();
				titleElement.parent().text(title);
			}else{
				log.debug("No title foundin wem content with class 'arrow'");
			}
			
			
			log.debug("Title link text of the web page content : "+aHeadingText);
			log.debug("Title link href of the web page content : "+aHeadingLink);
			
			Elements titleLinkElements = wemElement.select("span.view-all-link");
			if(titleLinkElements != null && !titleLinkElements.isEmpty()){
					Element titleLinkElement = titleLinkElements.first();
					Elements titleaElements = titleLinkElement.getElementsByTag("a");
					if(titleaElements != null && !titleaElements.isEmpty()){
						Element titleaElement = titleaElements.first();
						if(StringUtils.isNotBlank(aHeadingText)){
							titleaElement.text(aHeadingText);
						}else{
							log.debug("No heading link text found in web content.");
						}
						if(StringUtils.isNotBlank(aHeadingLink)){
							titleaElement.attr("href", aHeadingLink);
						}else{
							log.debug("No heading link found in web content.");
						}
						
						
					}else{
						log.debug("no anchor links found in the wem content.");
					}
			}else{
				log.debug("No element found with the class 'view-all-link' in wem content.");
			}
			//End of log to update the title of the content.
		} else {
			log.debug("doc is null.");
		}
		return wemElement.outerHtml();
	}
	
	public static String updateHtmlBlobContent(String htmlWEBContent,
			String htmlWEMContent, String loc, StringBuilder sb) throws JSONException{

		List<String> htmlList = new ArrayList<String>() {};
		Element wemElement = null;
		Document doc = null;
		
				try {
					doc = Jsoup.connect(loc).get();
				} catch (IOException e) {
				
				}
				log.debug("Connected to the provided URL");
			//-------------------------------------------------------------------------------------------------------------------------------
			//start of Logic to retrieve all the hyper links text and url and save in a map.
				try{
					if (doc != null) {
				
			Element webElement = doc.html(htmlWEBContent);
			String title = "";
			String paragraph = "";
			Elements titleElements = webElement.select("h3");
			Elements paragraphElements = webElement.select("p");
			if(titleElements != null && !titleElements.isEmpty()){
				Element titleElement = titleElements.first();
				title = titleElement.text();
				log.debug("title******* : "+title);
			}else{
				log.debug("No title found in web content with class 'bdr-1'");
			}
			if(paragraphElements != null && !paragraphElements.isEmpty()){
				Element pElement = paragraphElements.first();
				paragraph = pElement.text();
				log.debug("link href is blank in the element : "+paragraph);
			}else{
				log.debug("No p tag found in web content with class 'bdr-1'");
			}
			Elements liElements = webElement.select("li.clearfix");
			String subDrawerContentStr = "";
			if(liElements != null && !liElements.isEmpty()){
				for(Element ele : liElements){
					 subDrawerContentStr = ele.html();
							if(StringUtils.isNotBlank(subDrawerContentStr)){
						htmlList.add(subDrawerContentStr);
							}else{
								log.debug("link href is blank in the element : "+ele.html());
				}
				}
				//log.debug("subdrawer cotnendkeddsm"+ liElements.);
			}
			
			
			//end of Logic to retrieve all the hyper links text and url and save in a map.
			//-------------------------------------------------------------------------------------------------------------------------------
			//start of logic to update all the links in the wem html content from the map.
			wemElement = doc.html(htmlWEMContent);
			Elements dmcDrawerContentElements = wemElement.select("div.dmc-drawer-content");
			if(liElements != null && dmcDrawerContentElements != null){
				if(liElements.size() != dmcDrawerContentElements.size()){
					log.debug("liElements.size() content"+liElements.size()+ "::: "+dmcDrawerContentElements.size());
				sb.append("<li> Mismatch in the count of sub drawer panels for "+title+" drawer </li>");
				}}
			int ele=0; 
			for(Element dmcDrawerContentElement : dmcDrawerContentElements ){
				
				if(ele < htmlList.size()){
					log.debug("actual content"+htmlList.get(ele));
					dmcDrawerContentElement.html(htmlList.get(ele)+"<div style=\"clear:both;\"></div>");
					ele++;
				}
				
			}
		//	dmcDrawerContentElements.html(liElements.outerHtml());
			//end of logic to update all the links in the wem html content from the map.
			//-------------------------------------------------------------------------------------------------------------------------------
			//Start of log to update the title of the content.
			
			log.debug("Title of the web page content : "+title);
			titleElements = wemElement.select("h3");
			if(titleElements != null && !titleElements.isEmpty() && StringUtils.isNotBlank(title)){
				Element titleElement = titleElements.first();
				titleElement.text(title);
			}else{
				log.debug("No title foundin wem content with class 'arrow'");
			}
			
			paragraphElements = wemElement.select("p");
			if(paragraphElements != null && !paragraphElements.isEmpty() && StringUtils.isNotBlank(paragraph)){
				Element paraElement = paragraphElements.first();
				paraElement.text(paragraph);
			}else{
				log.debug("No title foundin wem content with class 'arrow'");
			}
			
			log.debug("Title text of the web page content : "+title);
			log.debug("Title p href of the web page content : "+paragraph);
			
			//End of log to update the title of the content.
			return wemElement.outerHtml();
		}else {
			log.debug("doc is null.");
		}
					}catch(Exception e){
						log.debug("doc is null.");
					}
	

		return null;
		}
		
	public static Session getSession() {
		Repository repository = null;
		Session session = null;
		Properties prop = new Properties();
		InputStream input = null;
		String host = null;
		String userId = null;
		String pwd = null;
		String workspace = null;
		String workbookpath = null;
		String reportspath = null;
		String repo = null;
		try {
			String filename = "config.properties";
			input = OVWMigration.class.getClassLoader().getResourceAsStream(filename);
			if (input == null) {
				log.debug("input is null");
				return null;
			}
			// load a properties file from class path, inside static method
			prop.load(input);
			host = StringUtils.isNotBlank(prop.getProperty("serverurl")) ? prop.getProperty("serverurl") : "";
			repo = host + "/crx/server";
			userId = StringUtils.isNotBlank(prop.getProperty("aemuser")) ? prop.getProperty("aemuser") : "";
			pwd = StringUtils.isNotBlank(prop.getProperty("aempassword")) ? prop.getProperty("aempassword") : "";
			workspace = StringUtils.isNotBlank(prop.getProperty("workspace")) ? prop.getProperty("workspace") : "";
			workbookpath = StringUtils.isNotBlank(prop.getProperty("workbookpath")) ? prop.getProperty("workbookpath") : "";
			reportspath = StringUtils.isNotBlank(prop.getProperty("reportspath")) ? prop.getProperty("reportspath") : "";

			log.debug("host : " + host);
			log.debug("userId : " + userId);
			log.debug("pwd : " + pwd);
			log.debug("workspace : " + workspace);
			log.debug("workbookpath : " + workbookpath);
			log.debug("reportspath : " + reportspath);

			if (host != "" && userId != "" && pwd != "" && workspace != "" && workbookpath != "" && reportspath != "") {
				repository = JcrUtils.getRepository(repo);
				session = repository.login(new SimpleCredentials(userId, pwd.toCharArray()), workspace);
			}

		} catch (IOException ex) {
			log.error("IOException : ", ex);
		} catch (Exception e) {
			log.error("Exception : ", e);
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return session;
	}
}
