package com.cisco.dse.global.migration.servicelisting;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
import org.apache.sling.commons.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.cisco.dse.global.migration.config.BaseAction;
import com.cisco.dse.global.migration.config.Constants;
import com.cisco.dse.global.migration.config.FrameworkUtils;


public class ServiceListingVariation02 extends BaseAction {
	Document doc;

	String title = null;

	StringBuilder sb = new StringBuilder(1024);

	String footerLinks = "{\"linktext\":\"<aaa>\",\"linkurl\":\"<bbb>\"}";

	static Logger log = Logger.getLogger(ServiceListingVariation02.class);

	public String translate(String host,String loc, String prod, String type,
			String catType, String locale, Session session) throws IOException,
			ValueFormatException, VersionException, LockException,
			ConstraintViolationException, RepositoryException {
		BasicConfigurator.configure();
		log.debug("In the translate method");
		log.debug("In the translate method, catType is :" + catType);

		// Repo node paths

		String pagePropertiesPath = "/content/<locale>/"+catType+"/<prod>/service-listing/jcr:content";
		String serviceListingMid = "/content/<locale>/"+ catType+ "/<prod>/service-listing/jcr:content/content_parsys/services/layout-services/gd21v1/gd21v1-mid";
		String serviceListingMidLeft = "/content/<locale>/"+ catType+ "/<prod>/service-listing/jcr:content/content_parsys/services/layout-services/gd22v2/gd22v2-left";
		String serviceListingMidRight = "/content/<locale>/"+ catType+ "/<prod>/service-listing/jcr:content/content_parsys/services/layout-services/gd22v2/gd22v2-right";

		String pageUrl = host+"/content/<locale>/"+ catType + "/<prod>/service-listing.html";

		pageUrl = pageUrl.replace("<locale>", locale).replace("<prod>", prod);
		pagePropertiesPath = pagePropertiesPath.replace("<locale>", locale).replace("<prod>", prod);
		sb.append("<td>" + "<a href=" + pageUrl + ">" + pageUrl + "</a>"
				+ "</td>");
		sb.append("<td>" + "<a href=" + loc + ">" + loc + "</a>" + "</td>");
		sb.append("<td><ul>");



		serviceListingMid = serviceListingMid.replace("<locale>", locale).replace(
				"<prod>", prod);
		serviceListingMidLeft = serviceListingMidLeft.replace("<locale>", locale).replace(
				"<prod>", prod);
		serviceListingMidRight = serviceListingMidRight.replace("<locale>", locale).replace(
				"<prod>", prod);


		javax.jcr.Node serviceListingMidNode = null;
		serviceListingMidNode = session.getNode(serviceListingMid);

		javax.jcr.Node serviceListingMidLeftNode = null;
		serviceListingMidLeftNode = session.getNode(serviceListingMidLeft);

		javax.jcr.Node serviceListingMidRightNode = null;
		serviceListingMidRightNode = session.getNode(serviceListingMidRight);

		javax.jcr.Node pageJcrNode = null;
		pageJcrNode = session.getNode(pagePropertiesPath);
		try {
			try{
				doc = Jsoup.connect(loc).get();
			}
			catch(Exception e){
				doc = getConnection(loc);
			}

			// ------------------------------------------------------------------------------------------------------------------------------------------
			// start set page properties.

			FrameworkUtils.setPageProperties(pageJcrNode, doc, session, sb);

			// end set page properties.
			// ------------------------------------------------------------------------------------------------------------------------------------------
			if(doc != null){
				// start of text component
				try{
					setText(doc, serviceListingMidNode);
				}catch(Exception e){
					sb.append(Constants.EXCEPTION_TEXT_COMPONENT);
				}	
				// end of text component

				// start of spotlight component
				try{
					Elements spotLightEle = doc.select("div.c11-pilot");
					if(spotLightEle != null && !spotLightEle.isEmpty()){
						int spEleSize = spotLightEle.size();
						NodeIterator spotLightNodes = serviceListingMidNode.hasNodes()?serviceListingMidNode
								.getNodes("spotlight_large*"):null;
								if(spotLightNodes != null){
									int spNodeSize = (int)spotLightNodes.getSize();

									if(spEleSize == spNodeSize){
										for(Element ele : spotLightEle){
											Node spotLightComponentNode = (Node) spotLightNodes.next();
											setSpotlight(ele ,spotLightComponentNode,locale);
										}
									} 
									else if(spEleSize > spNodeSize){
										for(Element ele : spotLightEle){
											if(spotLightNodes.hasNext()){
												Node spotLightComponentNode = (Node) spotLightNodes.next();
												setSpotlight(ele ,spotLightComponentNode,locale);
											}
										}
										sb.append(Constants.SPOTLIGHT_NODE_COUNT+spNodeSize+Constants.SPOTLIGHT_ELEMENT_COUNT+spEleSize+"</li>");
									} 
									else if(spEleSize < spNodeSize){
										for(Element ele : spotLightEle){
											Node spotLightComponentNode = (Node) spotLightNodes.next();
											setSpotlight(ele ,spotLightComponentNode,locale);
										}
										sb.append(Constants.SPOTLIGHT_ELEMENT_COUNT+spEleSize+Constants.SPOTLIGHT_NODE_COUNT+spNodeSize+"</li>");
									}
								}
								else{
									sb.append(Constants.SPOTLIGHT_NODE_NOT_FOUND);
								}
					}
					else{
						sb.append(Constants.SPOTLIGHT_ELEMENT_NOT_FOUND);
					}
				}catch(Exception e){
					log.error(e);
					sb.append("<li>Unable to Update Spotlight.</li>");
				}
				// End of spotlight component

				// Start of List Component
				try{
					Elements listElements = doc.select("div.n13-pilot");
					if(listElements != null && !listElements.isEmpty()){
						int listEleSize = listElements.size();
						NodeIterator listNodeIterator = serviceListingMidLeftNode.hasNode("list")? serviceListingMidLeftNode.getNodes("list"):null;
						if(listNodeIterator!=null){
							int listNodeSize = (int)listNodeIterator.getSize();
							if(listEleSize == listNodeSize){
								for(Element ele : listElements){
									if(listNodeIterator.hasNext()){
										Node listNode = (Node)listNodeIterator.next();
										setList(ele , listNode);
									}
								}
							}else if(listEleSize > listNodeSize){
								for(Element ele : listElements){
									if(listNodeIterator.hasNext()){
										Node listNode = (Node)listNodeIterator.next();
										setList(ele , listNode);
									}else{
										sb.append("<li>MisMatch in list components. Additional Elements found in Locale Page. Locale page has (" +listEleSize+") Elements and Available nodes are ("+listNodeSize+") </li>");
									}
								}

							}else if(listEleSize < listNodeSize){
								for(Element ele : listElements){
									Node listNode = (Node)listNodeIterator.next();
									setList(ele , listNode);
								}

							}else{
								sb.append("<li>MisMatch in list components. Additional Nodes found. Locale page has (" +listEleSize+") Elements and Available nodes are ("+listNodeSize+") </li>");						
							}
						}else{
							sb.append("<li>No List Elements Found.</li>");	
						}
					}
				}catch(Exception e){
					log.error(e);
					sb.append("<li>Unable to update List.</li>");	
				}
				// End of List Component
				//start of follow us
				try{
					Element followUsEle = doc.select("div.s14-pilot").first();
					Node followUsNode = serviceListingMidRightNode.hasNode("followus")?serviceListingMidRightNode.getNode("followus"):null;
					if(followUsEle!=null){
						Element followUsTitle = followUsEle.getElementsByTag("h2").first();
						if(followUsNode!=null){
							if(!followUsTitle.equals("")&& followUsTitle.hasText()){
								followUsNode.setProperty("title",followUsTitle.text());
							}else{
								sb.append("<li>No Title found for FollowUs.</li>");	
							}

							Elements ulEle = followUsEle.getElementsByTag("ul");
							List<String> list = new ArrayList<String>();

							for(Element ul : ulEle){
								Elements liEle = ul.getElementsByTag("li");
								for(Element li : liEle){
									JSONObject obj = new JSONObject();
									obj.put("icon",li.attr("class"));
									Element a = li.getElementsByTag("a").first();
									obj.put("linktext",a.attr("title"));
									obj.put("linkurl",a.attr("href"));
									list.add(obj.toString());
								}
								followUsNode.setProperty("links", list.toArray(new String[list.size()]));
							}

						}else{
							sb.append("<li>FollowUs Node not Found.</li>");
						}
					}else{
						sb.append("<li>FollowUs Element not Found in locale page.</li>");
					}
				}catch(Exception e){
					log.error(e);
					sb.append("<li>Unable to update followus.</li>");
				}
				//end of follow us
			}else{
				sb.append(Constants.URL_CONNECTION_EXCEPTION);	
				}
			} catch (Exception e) {
				sb.append("<li>Cannot Connect to given URL. \n" + loc + "</li>");
			}
			sb.append("</ul></td>");
			session.save();
			return sb.toString();
		}

		//This method is for text component 
		public void setText(Document doc, Node serviceListingMidNode)throws RepositoryException{
			Elements textElements = doc.select("div.c00-pilot");
			if(textElements.isEmpty()){
				sb.append(Constants.TEXT_ELEMENT_NOT_FOUND);
			}
			else{
				int eleSize = textElements.size();
				NodeIterator textNodeIterator = serviceListingMidNode.hasNode("text")? serviceListingMidNode.getNodes("text"):null;
				if(textNodeIterator != null){
					int nodeSize = (int)textNodeIterator.getSize();
					if(eleSize == nodeSize){
						for(Element ele : textElements){
							if(textNodeIterator.hasNext()){
								Node textNode = (Node)textNodeIterator.next();
								textNode.setProperty("text", ele.html());	
							}
						}
					}
					else if(nodeSize < eleSize){
						for(Element ele : textElements){
							if(textNodeIterator.hasNext()){
								Node textNode = (Node)textNodeIterator.next();
								textNode.setProperty("text", ele.html());
							}
							else{
								sb.append("<li>Found extra text element in locale page</li>");
							}
						}
					}
					else if(nodeSize > eleSize){
						for(Element ele : textElements){
							if(textNodeIterator.hasNext()){
								Node textNode = (Node)textNodeIterator.next();
								textNode.setProperty("text", ele.html());							
							}
						}
						sb.append("<li>Extra text node exist</li>");
					}
				}
				else{
					sb.append("<li>No text Node found.</li>");
				}
			}
		}

		//start setList
		void setList(Element ele , Node listNode){
			try{
				Elements h2Ele = ele.getElementsByTag("h2");
				Elements h3Ele = ele.getElementsByTag("h3");
				Elements ulEle = ele.getElementsByTag("ul");

				if(!h2Ele.isEmpty()){
					for(Element h2 : h2Ele){
						log.debug("h2 of list");
						listNode.setProperty("title", h2.text());
					}
				}else{
					sb.append("<li>No h2 elements in list.</li>");
				}

				NodeIterator h3Nodes = listNode.getNodes("element_subtitle*");
				if(!h3Ele.isEmpty()){
					for(Element h3 : h3Ele){
						if(h3Nodes.hasNext()){
							Node h3Node = (Node)h3Nodes.next();
							h3Node.setProperty("subtitle",h3.text());
						}
					}
				}else{
					sb.append("<li>No h3 elements in list.</li>");
				}

				NodeIterator ulNodes = listNode.getNodes("element_list*");
				for(Element ul : ulEle){
					if(ulNodes.hasNext()){
						Elements aEle = ul.getElementsByTag("a");
						Node eleList = (Node)ulNodes.next();
						List<String> list = new ArrayList<String>();
						String nodeName = eleList.getName();
						for(Element a : aEle){
							JSONObject obj = new JSONObject();
							String linkText = a.text();
							obj.put("linktext", linkText);
							obj.put("linkurl",a.attr("href"));
							obj.put("icon","none");
							obj.put("size","");
							obj.put("description","");
							obj.put("openInNewWindow",true);
							/*	if(nodeName.equals("element_list_1")){
							log.debug("element_list_1");
							obj.put("openInNewWindow",false);
						}else{
							log.debug("element_list_0/2");
							obj.put("openInNewWindow",true);
						} */
							list.add(obj.toString());
						}
						try{
							//	eleList.setProperty("listitems", list.toArray(new String[list.size()]));
							if (nodeName.equals("element_list_0")) {
								eleList.setProperty("listitems", list.toArray(new String[list.size()]));
							}
							else {
								eleList.setProperty("listitems", list.get(0));
							} 

						}catch(Exception e){
							log.debug("setProperty"+e);
						}
					}else{
						sb.append("<li>No List node(s) found.</li>");
					}
				}

			}catch(Exception e){
				sb.append("<li>Unable to update List Component...</li>");
			}
		}
		//end setList

		// This method to set content for spotlight
		public void setSpotlight(Element ele, Node spotLightComponentNode,String locale) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException{
			String title = "";
			String pText = "";
			String aText = "";
			Element h2Ele = ele.getElementsByTag("h2").first();
			if (h2Ele != null) {
				title = h2Ele.text();
				String tLink = h2Ele.getElementsByTag("a").attr("href");
				spotLightComponentNode.setProperty("title",title);
				Node titLink = spotLightComponentNode.getNode("titlelink");
				if(tLink!=null && !tLink.equals("")){
					if(titLink!=null){
						titLink.setProperty("url", tLink);	
					}else{
						sb.append("<li>Title link node not available</li>");
					}
				}else{
					sb.append("<li>Title does not have link in local page.</li>");
				}

			} else {
				sb.append("<li>Spotlight Component not having any title.</li>");
			}
			// start image
			String spotLightImage = FrameworkUtils.extractImagePath(ele, sb);
			log.debug("spotLightImage " + spotLightImage + "\n");
			
			if (spotLightComponentNode != null) {
				if (spotLightComponentNode.hasNode("image")) {
					Node spotLightImageNode = spotLightComponentNode.getNode("image");
					String fileReference = spotLightImageNode.hasProperty("fileReference")?spotLightImageNode.getProperty("fileReference").getString():"";
					spotLightImage = FrameworkUtils.migrateDAMContent(spotLightImage, fileReference,  locale);
					log.debug("spotLightImage " + spotLightImage + "\n");
					if (StringUtils.isNotBlank(spotLightImage)) {
						spotLightImageNode.setProperty("fileReference" , spotLightImage);
					} else {
						sb.append("<li>spotlight image doesn't exist</li>");
					}
				} else {
					sb.append("<li>spotlight image node doesn't exist</li>");
				}
			}
			// end image
			Elements descriptionText = ele.getElementsByTag("p");
			String ulText = ele.getElementsByTag("ul").outerHtml();

			if(!ulText.equals("") && ulText!=null){
				String pTagText = descriptionText.text();
				spotLightComponentNode.setProperty("description", pTagText+ulText);
			}
			else if (descriptionText != null && !descriptionText.isEmpty()) {
				pText = descriptionText.html();
				spotLightComponentNode.setProperty("description", pText);
			}else{
				sb.append("<li>Spotlight Component not having any description.</li>");
			}

			Elements spotLightAnchor = ele.select("a.cta");
			if (spotLightAnchor != null && !spotLightAnchor.isEmpty()) {
				aText = spotLightAnchor.text();
				String linkUrl = spotLightAnchor.attr("href");
				spotLightComponentNode.setProperty("linktext", aText);
				javax.jcr.Node ctaNode = spotLightComponentNode.getNode("cta");
				if (ctaNode != null && linkUrl != null) {
					ctaNode.setProperty("url", linkUrl);
				}
			} else {
				sb.append("No cta link available in locale page.</li>");
			}
		}
	}
