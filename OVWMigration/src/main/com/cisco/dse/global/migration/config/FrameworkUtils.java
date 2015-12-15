/**
 * Copyright (C) 2014 Virtusa Corporation.
 * This file is proprietary and part of Virtusa LaunchPad.
 * LaunchPad code can not be copied and/or distributed without the express permission of Virtusa Corporation
 */

package com.cisco.dse.global.migration.config;


import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;

import javax.jcr.Session;

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
 	    	   			String jcrTitle = "";
 	    	   			if (jcrNode.hasProperty("jcr:title")) {
 	    	   				jcrTitle = jcrNode.getProperty("jcr:title").getValue().getString();
 	    	   			}
 	    	   			 
 	    	   			log.debug("JCR Title in chard is "+jcrTitle);
 	    	   			if (StringUtils.isNotBlank(pageTitle) && (!pageTitle.trim().equalsIgnoreCase(jcrTitle))) {
 	    	   				log.debug("Page title and JCR title are not the same.");
 	    	   				jcrNode.setProperty("cisco:customHeadTitle", pageTitle);
 	    	   			} else {
 		    	   	    	sb.append("<li>custom head title not set </li>");
 		    	   	    }
     	   			} else {
     	   				sb.append("<li>custom head title not set </li>");
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
			String locale) {
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
					newImagePath = setContentToDAM(path, imgRef);
				} else if (!path.equalsIgnoreCase(imgRef)) {
					return path;
				} else {
					return "";
				}
			} else {
				log.debug("image path is blank.");
			}
		} catch (Exception e) {
			log.error("Exception : ", e);
		}
		return newImagePath;
	}

	public static String setContentToDAM(String path, String imgPath) {
		log.debug("In the setContentToDAM to migrate : " + path);

		HttpClient client = new HttpClient();
		HttpMethod method = new GetMethod(
				"http://chard.cisco.com:4502/bin/services/DAMMigration?imgPath="
						+ path+"&imgRef="+imgPath);
		Credentials defaultcreds = new UsernamePasswordCredentials("admin",
				"admin");
		AuthScope authscope = new AuthScope("chard.cisco.com", 4502,
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
			JSONObject resObj = new JSONObject(responseObj);
			String newImagePath = "";
			String error = "";
			if (resObj.has("newImagePath")) {
				newImagePath = (String) resObj.get("newImagePath");
				log.debug("Updated dam Image path : " + newImagePath);
			} else {
				log.debug("No 'newImagePath' found in service response.");
			}
			if (resObj.has("error")) {
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
					imagePath =imageElement.attr("src");
				} else {
					sb.append("<li>image doesn't exist </li>");
				}
			} else {
				sb.append("<li>image doesn't exist </li>");
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
    public static String extractHtmlBlobContent(Element htmlBlobElement, String fileReference, String locale, StringBuilder sb) {
    	String outeHtmlText = "";
    	String imagePath = "";
    	String imageOldPath = "";
    	if (htmlBlobElement != null) {
    		imagePath = extractImagePath(htmlBlobElement, sb);
    		imagePath = migrateDAMContent(imagePath, fileReference, locale);
    		Elements imageElements = htmlBlobElement.getElementsByTag("img");
			if (imageElements != null) {
				Element imageElement = imageElements.first();
				if (imageElement != null) {
					imageOldPath = imageElement.attr("src");
				} 
			}
			log.debug("old path and new path : "+ imageOldPath +" and "+imagePath);
			if(StringUtils.isNotBlank(imagePath)){
				outeHtmlText = htmlBlobElement.outerHtml().replace(imageOldPath, imagePath);
			}
			}
    	return outeHtmlText;
    }
   }
