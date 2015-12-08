/**
 * Copyright (C) 2014 Virtusa Corporation.
 * This file is proprietary and part of Virtusa LaunchPad.
 * LaunchPad code can not be copied and/or distributed without the express permission of Virtusa Corporation
 */

package com.cisco.dse.global.migration.config;


import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

import javax.jcr.Session;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
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
      
    	   String title = "";
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
    			String titleHtmlProperty = StringUtils.isNotBlank(metaProp
    					.getProperty("jcr:title")) ? metaProp
    					.getProperty("jcr:title") : "";
				String descriptionHtmlProperty = StringUtils.isNotBlank(metaProp
    					.getProperty("jcr:description")) ? metaProp
    					.getProperty("jcr:description") : "";
				// getting meta data from html document
    	   		Elements metas = doc.getElementsByTag("meta"); 
    	   		if (metas != null) {
	    	   	   for (Element meta : metas) { 
	    	   	      if (meta.hasAttr("name") && meta.attr("name").equals(titleHtmlProperty)) { 
	    	   	         title =  meta.attr("content"); 
	    	   	         log.debug("title of document:::  " + title);
	    	   	      } else {
	    	   	    	sb.append("<li>meta data title doesn't exist </li>");
	    	   	      }
	    	   	      if (meta.hasAttr("name") && meta.attr("name").equals(descriptionHtmlProperty)) { 
	    	   	    	 description =  meta.attr("content"); 
	    	   	         log.debug("description of document:::  " + description);
	    	   	      } else {
	    	   	    	sb.append("<li>meta data description doesn't exist </li>");
	    	   	      }
	    	   	   } 
    	   		}
    	   		//setting html meta data to as page properties
	    	   	if (jcrNode != null) {
	    	   		if (StringUtils.isNotBlank(title)) {
	    	   			jcrNode.setProperty("jcr:title", title);
	    	   		}
	    	   		if (StringUtils.isNotBlank(description)) {
	    	   			jcrNode.setProperty("jcr:description", description);
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

   }
