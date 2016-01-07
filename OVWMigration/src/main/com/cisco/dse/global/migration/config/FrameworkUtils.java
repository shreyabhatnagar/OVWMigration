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
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
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
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.sling.commons.json.JSONObject;
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
									if (jcrNode.hasProperty("jcr:title")) {
										jcrNode.setProperty("jcr:title",pageTitle);
										if (jcrNode.hasProperty("cisco:customHeadTitle")) {
											jcrNode.setProperty("cisco:customHeadTitle", "");
										}else{
											log.debug("Custom head title property not found");
										}
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
		String newImagePath = "";

		try {
			if (StringUtils.isNotBlank(path)) {
				if (path.indexOf("/content/en/us") == -1
						&& path.indexOf("/content/dam") == -1
						&& path.indexOf("/c/en/us") == -1
						&& path.indexOf("/c/dam") == -1) {
					if (path.indexOf("http:") == -1
							&& path.indexOf("https:") == -1) {
						log.debug("Adding domain to the image path.");
						path = "http://www.cisco.com" + path;
					}
					if (StringUtils.isNotBlank(imgRef)) {
						imgRef = imgRef.replace("/assets", "/global/" + locale);
						imgRef = imgRef.replace("/en/us", "/global/" + locale);
					} else {
						URL url = new URL(path);
						String imagePath = url.getPath();
						if (imagePath.indexOf("/web") != -1) {
							imgRef = imagePath.replace("/web", "/content/dam/global/" + locale);
						} else {
							imgRef = "/content/dam/global/" + locale + imagePath;
						}
					}
					if(path.lastIndexOf("/")!=-1 && imgRef.lastIndexOf("/")!=-1){
						String imageName = path.substring(path.lastIndexOf("/"), path.length());
						imgRef = imgRef.substring(0, imgRef.lastIndexOf("/"))+imageName;
					}
					newImagePath = setContentToDAM(path, imgRef);
				} else if (!path.equalsIgnoreCase(imgRef)) {
					return path;
				} else {
					return "";
				}
			} else {
				sb.append(Constants.IMAGE_NOT_FOUND_IN_LOCALE_PAGE);
				log.debug("image path is blank.");
			}
		} catch (Exception e) {
			log.error("Exception : ", e);
		}
		return newImagePath;
	}

	public static String setContentToDAM(String path, String imgPath) {
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
				+ path+"&imgRef="+imgPath);
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
				log.debug("Before anchorHref" + anchor.getKey().toString() + "\n");
				updatedAnchorPath = FrameworkUtils.getLocaleReference(anchor.getKey().toString(), urlMap);
				log.debug("after anchorHref" + updatedAnchorPath + "\n");

				log.debug(anchor.getKey().toString() +" is updated to "+updatedAnchorPath);
				if(StringUtils.isNotBlank(existingAnchorPath) && StringUtils.isNotBlank(updatedAnchorPath)){
					outeHtmlText = outeHtmlText.replace("\"" +existingAnchorPath + "\"", "\"" + updatedAnchorPath + "\"");
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
			log.debug("&&&&&&&&&&&& null" + htmlBlobElement.outerHtml());
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

	public static String getLocaleReference(String primaryCTALinkUrl, Map<String, String> urlMap) {
		if (StringUtils.isNotBlank(primaryCTALinkUrl)) {
			if (urlMap.containsKey(primaryCTALinkUrl)) {
				primaryCTALinkUrl = urlMap.get(primaryCTALinkUrl);
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

}
