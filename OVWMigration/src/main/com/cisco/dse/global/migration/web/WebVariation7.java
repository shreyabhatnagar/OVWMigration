package com.cisco.dse.global.migration.web;

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

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.cisco.dse.global.migration.config.BaseAction;
import com.cisco.dse.global.migration.config.Constants;
import com.cisco.dse.global.migration.config.FrameworkUtils;

public class WebVariation7 extends BaseAction{
	Document doc;

	StringBuilder sb = new StringBuilder(1024);

	static Logger log = Logger.getLogger(WebVariation7.class);

	public String translate(String host, String loc, String prod, String type, String catType,
			String locale, Session session,Map<String,String> urlMap) throws IOException,
			ValueFormatException, VersionException, LockException,
			ConstraintViolationException, RepositoryException {
		BasicConfigurator.configure();

		// Repo node paths
		try {

			String pagePropertiesPath = "/content/<locale>/"+catType+"/<prod>";
			String pageUrl = host + "/content/<locale>/"+catType+"/<prod>/";
			if(type.equals("energy-webvar7")){
				log.debug("energy-webvar7");
				pagePropertiesPath = pagePropertiesPath+"/energy/jcr:content/";
				pageUrl = pageUrl +"energy.html";
			}else if(type.equals("retail-webvar7")){
				log.debug("retail-webvar7");
				pagePropertiesPath = pagePropertiesPath+"/retail/jcr:content/";
				pageUrl = pageUrl +"retail.html";
			}else if(type.equals("government-webvar7")){
				log.debug("government-webvar7");
				pagePropertiesPath = pagePropertiesPath+"/government/jcr:content/";
				pageUrl = pageUrl +"government.html";
			}else if(type.equals("manufacturing-webvar7")){
				log.debug("manufacturing-webvar7");
				pagePropertiesPath = pagePropertiesPath+"/manufacturing/jcr:content/";
				pageUrl = pageUrl +"manufacturing.html";
			}else if(type.equals("financial-services-webvar7")){
				log.debug("financial-services-webvar7");
				pagePropertiesPath = pagePropertiesPath+"/financial-services/jcr:content/";
				pageUrl = pageUrl +"financial-services.html";
			}else if(type.equals("iot-products-webvar7")){
				log.debug("iot-products-webvar7");
				pagePropertiesPath = pagePropertiesPath+"/iot-products/jcr:content/";
				pageUrl = pageUrl +"iot-products.html";
			}
			pageUrl = pageUrl.replace("<locale>", locale).replace("<prod>", prod);
			pagePropertiesPath = pagePropertiesPath.replace("<locale>", locale).replace("<prod>", prod);

			String indLeft = pagePropertiesPath+"content_parsys/solutions/layout-solutions/gd22v2/gd22v2-left";
			String indRight = pagePropertiesPath+"content_parsys/solutions/layout-solutions/gd22v2/gd22v2-right";

			log.debug("Path is "+indLeft);
			log.debug("Path is "+indRight);

			sb.append("<td>" + "<a href="+pageUrl+">"+pageUrl+"</a>"+"</td>");
			sb.append("<td>" + "<a href="+loc+">"+loc +"</a>"+ "</td>");
			sb.append("<td><ul>");

			indLeft = indLeft.replace("<locale>", locale).replace("<prod>", prod);
			indRight = indRight.replace("<locale>", locale).replace("<prod>", prod);

			javax.jcr.Node indLeftNode = null;
			javax.jcr.Node indRightNode = null;
			javax.jcr.Node pageJcrNode = null;

			indLeftNode = session.getNode(indLeft);
			indRightNode = session.getNode(indRight);
			pageJcrNode = session.getNode(pagePropertiesPath);

			try {
				doc = getConnection(loc);
				if(doc!=null){
					// ------------------------------------------------------------------------------------------------------------------------------------------
					// start set page properties.

					FrameworkUtils.setPageProperties(pageJcrNode, doc, session, sb);

					// end set page properties.
					// ------------------------------------------------------------------------------------------------------------------------------------------

					//------------------ (start gd-left) ---------------//
					if(type.equals("energy-webvar7") || type.equals("retail-webvar7") || type.equals("iot-products-webvar7")){
						log.debug("energy-webvar7 || retail-webvar7");
						try{
							log.debug("start of mid..$$");
							Element gdMidEle = null;
							/*Element gdMidEle = doc.select("div.gd-mid,div.gd-right").first();
							if(gdMidEle==null){
								log.debug("gdMidEle==null----S");
								gdMidEle = doc.select("div.sitecopy_hs").first();
								gdMidEle.select("c46-pilot").remove();
							}
							if(gdMidEle!=null){
								if(gdMidEle.hasClass("gd-right")){
									log.debug("hasClass...##");
									gdMidEle=gdMidEle.select("div.gd-left").last();
								}*/
							if(type.equals("iot-products-webvar7")){
								gdMidEle = doc.select("div.gd-right").first();
								gdMidEle = gdMidEle.select("div.gd-left").first();
							}else{
								gdMidEle = doc.select("div.gd-left,div.gd-mid").first();
								if(gdMidEle==null){
									log.debug("gdMidEle==null----S");
									gdMidEle = doc.select("div.sitecopy_hs").first();
									gdMidEle.select("c46-pilot").remove();
								}
							}
							log.debug("in gdMid");
							String gdMid = FrameworkUtils.extractHtmlBlobContent(gdMidEle, "",locale, sb, urlMap);
							if(!gdMid.equals("")&& gdMid!=null){
								Node leftBlob = indLeftNode.hasNode("htmlblob")?indLeftNode.getNode("htmlblob"):null;
								if(leftBlob!=null){
									
									/*Element migrateEle = doc.getElementsByTag("migrate").first();
									migrateEle.select("div.gd-mid").remove();
									if(migrateEle!=null){
										gdMid = migrateEle.outerHtml() + gdMid;
									}*/
									Element migrateEle = doc.getElementsByTag("migrtae").first();
									String c50JS = migrateEle != null ? migrateEle.getElementsByTag("script").last().outerHtml() : "";
									log.debug("hero java script is : "+c50JS);
									leftBlob.setProperty("html",c50JS+gdMid);
								}else{
									log.debug("html blob node not found.");
									sb.append("<li>html blob node not found.</li>");
								}
							}


							log.debug("end of mid..");
						}catch(Exception e){
							log.error("Unable to update hero : ",e);
						}
					}else if(type.equals("government-webvar7") || type.equals("manufacturing-webvar7")){
						log.debug("government-webvar7 || manufacturing-webvar7");
						try{
							log.debug("start of mid..@@");
							Element gdMidEle1 = doc.select("div.gd-mid").select("div.c50-pilot").first();
							NodeIterator leftBlobIterator = indLeftNode.hasNodes()?indLeftNode.getNodes("htmlblob*"):null;

							if(gdMidEle1!=null){
								log.debug("in gdMid@@@@");
								//							gdMidEle.html()
								String gdMid = FrameworkUtils.extractHtmlBlobContent(gdMidEle1, "",locale, sb, urlMap);
								log.debug("---------My log----------"+gdMid);

								if(!gdMid.equals("")&& gdMid!=null){
									Node leftBlob1 =null; 
									if(leftBlobIterator.hasNext()){
										leftBlob1 = (Node)leftBlobIterator.next();
									}
									if(leftBlob1!=null){
										log.debug("migrate miss spelt");
										/*Element migrateEle = doc.getElementsByTag("migrate").first();
										if(migrateEle!=null){
											log.debug("migrate !=null");
											gdMid = migrateEle.outerHtml() + gdMid;
										}*/
										Element migrateEle = doc.getElementsByTag("migrate").first();
										String c50JS = migrateEle.getElementsByTag("script").last().outerHtml();
										log.debug("hero java script is : "+c50JS);
										leftBlob1.setProperty("html", c50JS+gdMid);
									}else{
										log.debug("html blob node not found.");
										sb.append("<li>html blob node not found.</li>");
									}
								}

							}

							Element gdMidEle2 = doc.select("div.gd-mid").select("div.c00-pilot,div.n13-pilot").first();

							if(gdMidEle2!=null){
								//							gdMidEle.html()
								String gdMid = FrameworkUtils.extractHtmlBlobContent(gdMidEle2, "",locale, sb, urlMap);
								if(!gdMid.equals("")&& gdMid!=null){
									Node leftBlob2 =null; 
									if(leftBlobIterator.hasNext()){
										leftBlob2 = (Node)leftBlobIterator.next();
									}
									if(leftBlob2!=null){
										leftBlob2.setProperty("html", gdMid);
									}else{
										log.debug("html blob node not found.");
										sb.append("<li>html blob node not found.</li>");
									}
								}

							}	
							Elements gdMidElem = doc.select("div.gd-mid");
							Element gdMidEle3=null;
							gdMidEle3 = gdMidElem.select("div.c00-pilot").last();
							if(gdMidEle3==null){
								gdMidEle3 = doc.select("div.gd42-pilot").first();	
							}


							if(gdMidEle3!=null){
								//							gdMidEle.html()
								String gdMid = FrameworkUtils.extractHtmlBlobContent(gdMidEle3, "",locale, sb, urlMap);
								if(!gdMid.equals("")&& gdMid!=null){
									Node leftBlob3 =null; 
									if(leftBlobIterator.hasNext()){
										leftBlob3 = (Node)leftBlobIterator.next();
									}
									if(leftBlob3!=null){
										leftBlob3.setProperty("html", gdMid);
									}else{
										log.debug("html blob node not found.");
										sb.append("<li>html blob node not found.</li>");
									}
								}

							}
							log.debug("end of mid..");
						}catch(Exception e){
							log.error("Unable to update hero : ",e);
						}
					}else if(type.equals("financial-services-webvar7")){
						log.debug("financial-services-webvar7 else if");
						try{
							log.debug("start of mid..");
							Element gdMidEle1 = doc.select("div.gd-mid").select("div.c50-pilot").first();
							Element gdMidEle2 = doc.select("div.gd-mid").select("div.n13-pilot").first();
							NodeIterator leftBlobIterator = indLeftNode.hasNodes()?indLeftNode.getNodes("htmlblob*"):null;

							if(gdMidEle1!=null&&gdMidEle2!=null){
								log.debug("in gdMid");
								//							gdMidEle.html()
								String gdMid1 = FrameworkUtils.extractHtmlBlobContent(gdMidEle1, "",locale, sb, urlMap);
								String gdMid2 = FrameworkUtils.extractHtmlBlobContent(gdMidEle2, "",locale, sb, urlMap);
								String gdMid = gdMid1+gdMid2;
								if(!gdMid.equals("")&& gdMid!=null){
									Node leftBlob1 =null; 
									if(leftBlobIterator.hasNext()){
										leftBlob1 = (Node)leftBlobIterator.next();
									}
									if(leftBlob1!=null){
										leftBlob1.setProperty("html", gdMid);
									}else{
										log.debug("html blob node not found.");
										sb.append("<li>html blob node not found.</li>");
									}
								}

							}

							Element gdMidEle3 = doc.select("div.gd-mid").select("div.n13-pilot").last();
							if(gdMidEle3!=null){
								String gdMid3 = FrameworkUtils.extractHtmlBlobContent(gdMidEle3, "",locale, sb, urlMap);
								if(!gdMid3.equals("")){
									Node leftBlob1 =null; 
									if(leftBlobIterator.hasNext()){
										leftBlob1 = (Node)leftBlobIterator.next();
									}
									if(leftBlob1!=null){
										leftBlob1.setProperty("html", gdMid3);
									}else{
										log.debug("html blob node not found.");
										sb.append("<li>html blob node not found.</li>");
									}
								}
							}

							log.debug("end of mid..");
						}catch(Exception e){
							log.error("Unable to update hero : ",e);
						}
					}

					//------------------ (end gd-left) ---------------//

					//------------------ (start gd-right) ---------------//

					try{
						log.debug("start of right rail..");
						Element gdRightEle = doc.select("div.gd-right").last();

						if(gdRightEle==null){
							gdRightEle = doc.select("div#framework-content-right").first();
						}
						if(gdRightEle!=null){
							//							String gdRight = gdRightEle.html();
							Element gdParent = gdRightEle.parent();
							if(gdParent.className().equals("gd-mid")){
								gdRightEle=null;
							}
							if(gdRightEle==null){
								gdRightEle = doc.select("div#framework-content-right").first();
							}

							if(gdRightEle!=null){	
								String gdRight = FrameworkUtils.extractHtmlBlobContent(gdRightEle, "",locale, sb, urlMap);
								Node rightBlob = indRightNode.hasNode("htmlblob")?indRightNode.getNode("htmlblob"):null;
								if(rightBlob!=null){
									rightBlob.setProperty("html",gdRight.replaceAll("<br>", ""));
								}else{
									log.debug("html blob node not found.");
									sb.append("<li>html blob node not found.</li>");
								}
							}else{
								sb.append("<li>no gd-right content found</li>");
								log.debug("no gd-right content found");
							}
						}else{
							sb.append("<li>no gd-right content found</li>");
							log.debug("no gd-right content found");
						}
						log.debug("end of right rail..");
					}catch(Exception e){
						log.error("Exception in right rail : ",e);
					}

					//------------------ (end gd-right) ---------------//

				}else{
					sb.append(Constants.URL_CONNECTION_EXCEPTION);	
				}
			} catch (Exception e) {
				log.error(e);
				sb.append(Constants.URL_CONNECTION_EXCEPTION);
			}

		} catch (Exception e) {
			log.debug("Exception ", e);
		}
		sb.append("</ul></td>");
		session.save();
		return sb.toString();
	}
}