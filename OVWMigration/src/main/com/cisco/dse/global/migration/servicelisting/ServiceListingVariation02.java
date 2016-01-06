package com.cisco.dse.global.migration.servicelisting;

/* 
 * S.No		Name			Description of change
 * 1		Anudeep			Added the Java file to handle the migration of service listing variation 2 page.
 * 
 * */

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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

	StringBuilder sb = new StringBuilder(1024);

	String footerLinks = "{\"linktext\":\"<aaa>\",\"linkurl\":\"<bbb>\"}";

	static Logger log = Logger.getLogger(ServiceListingVariation02.class);

	public String translate(String host,String loc, String prod, String type,
			String catType, String locale, Session session,Map<String,String>urlMap) throws IOException,
			ValueFormatException, VersionException, LockException,
			ConstraintViolationException, RepositoryException {
		BasicConfigurator.configure();
		log.debug("In the translate method");
		log.debug("In the translate method, catType is :" + catType);

		// Repo node paths
		try {
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
			try{
			doc = Jsoup.connect(loc).get();
			if(doc != null){
				
				// ------------------------------------------------------------------------------------------------------------------------------------------
				// start set page properties.

				FrameworkUtils.setPageProperties(pageJcrNode, doc, session, sb);

				// end set page properties.
				// ------------------------------------------------------------------------------------------------------------------------------------------
				
				// start of text component
				try{
					setText(doc, serviceListingMidNode,locale,urlMap);
				}catch(Exception e){
					sb.append(Constants.EXCEPTION_TEXT_COMPONENT);
				}	
				// end of text component

				// start of spotlight component
				try{
					Elements spotLightEle = doc.select("div.c11-pilot");
					if(spotLightEle != null && !spotLightEle.isEmpty()){
						int spEleSize = spotLightEle.size();
						NodeIterator spotLightNodes = serviceListingMidNode.hasNodes()?serviceListingMidNode.getNodes("spotlight_large*"):null;
								if(spotLightNodes != null){
									int spNodeSize = (int)spotLightNodes.getSize();

									if(spEleSize == spNodeSize){
										for(Element ele : spotLightEle){
											Node spotLightComponentNode = (Node) spotLightNodes.next();
											setSpotlight(ele ,spotLightComponentNode,locale,urlMap);
										}
									} 
									else if(spEleSize > spNodeSize){
										for(Element ele : spotLightEle){
											if(spotLightNodes.hasNext()){
												Node spotLightComponentNode = (Node) spotLightNodes.next();
												setSpotlight(ele ,spotLightComponentNode,locale,urlMap);
											}
										}
										sb.append(Constants.SPOTLIGHT_NODE_COUNT+spNodeSize+Constants.SPOTLIGHT_ELEMENT_COUNT+spEleSize+"</li>");
									} 
									else if(spEleSize < spNodeSize){
										for(Element ele : spotLightEle){
											Node spotLightComponentNode = (Node) spotLightNodes.next();
											setSpotlight(ele ,spotLightComponentNode,locale,urlMap);
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
					sb.append(Constants.UNABLE_TO_UPDATE_SPOTLIGHT);
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
										setList(ele , listNode,urlMap);
									}
								}
							}else if(listEleSize > listNodeSize){
								for(Element ele : listElements){
									if(listNodeIterator.hasNext()){
										Node listNode = (Node)listNodeIterator.next();
										setList(ele , listNode,urlMap);
									}else{
										sb.append(Constants.MISMATCH_IN_LIST_ELEMENT+listEleSize+") "+ Constants.LIST_NODES_COUNT+listNodeSize+") </li>");
									}
								}

							}else if(listEleSize < listNodeSize){
								for(Element ele : listElements){
									Node listNode = (Node)listNodeIterator.next();
									setList(ele , listNode,urlMap);
								}

							}else{
								sb.append(Constants.MISMATCH_IN_LIST_NODES+listEleSize+Constants.LIST_NODES_COUNT+listNodeSize+") </li>");						
							}
						}else{
							sb.append(Constants.NO_LIST_NODES_FOUND);	
						}
					}
				}catch(Exception e){
					log.error(e);
					sb.append(Constants.UNABLE_TO_UPDATE_LIST);	
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
								sb.append(Constants.FOLLOWUS_TITLE_NOT_FOUND);	
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
									String aHref = a.absUrl("href");
									// Start extracting valid href
									log.debug("Before followus" + aHref + "\n");
									aHref = FrameworkUtils.getLocaleReference(aHref, urlMap);
									log.debug("after followus" + aHref + "\n");
									// End extracting valid href
									obj.put("linkurl",aHref);
									list.add(obj.toString());
								}
								followUsNode.setProperty("links", list.toArray(new String[list.size()]));
							}

						}else{
							sb.append(Constants.FOLLOWUS_NODE_NOT_FOUND);
						}
					}else{
						sb.append(Constants.FOLLOWUS_ELEMENT_NOT_FOUND);
					}
				}catch(Exception e){
					log.error(e);
					sb.append(Constants.UNABLE_TO_UPDATE_FOLLOWUS);
				}
				//end of follow us
			}else{
				sb.append(Constants.URL_CONNECTION_EXCEPTION);	
			}
			}catch(Exception e){
				log.error(e);
			}


		} catch (Exception e) {
			sb.append(Constants.URL_CONNECTION_EXCEPTION);
		}
		sb.append("</ul></td>");
		session.save();
		return sb.toString();
	}

	//This method is for text component 
	public void setText(Document doc, Node serviceListingMidNode,String locale, Map<String,String> urlMap)throws RepositoryException{
		Elements textElements = doc.select("div.c00-pilot");
		if(textElements.isEmpty()){
			sb.append(Constants.TEXT_ELEMENT_NOT_FOUND);
		}
		else{
			int eleSize = textElements.size();
			NodeIterator textNodeIterator = serviceListingMidNode.hasNode("text")? serviceListingMidNode.getNodes("text"):null;
			if(textNodeIterator != null){
				int nodeSize = (int)textNodeIterator.getSize();
				String text=null;
				if(eleSize == nodeSize){
					for(Element ele : textElements){
						if(textNodeIterator.hasNext()){
							Node textNode = (Node)textNodeIterator.next();
							text = FrameworkUtils.extractHtmlBlobContent(ele, "", locale, sb, urlMap);
							textNode.setProperty("text", text);	
						}
					}
				}
				else if(nodeSize < eleSize){
					for(Element ele : textElements){
						if(textNodeIterator.hasNext()){
							Node textNode = (Node)textNodeIterator.next();
							text = FrameworkUtils.extractHtmlBlobContent(ele, "", locale, sb, urlMap);
							textNode.setProperty("text", text);
						}
						else{
							sb.append(Constants.EXTRA_TEXT_ELEMENT_FOUND);
						}
					}
				}
				else if(nodeSize > eleSize){
					for(Element ele : textElements){
						if(textNodeIterator.hasNext()){
							Node textNode = (Node)textNodeIterator.next();
							text = FrameworkUtils.extractHtmlBlobContent(ele, "", locale, sb, urlMap);
							textNode.setProperty("text", text);							
						}
					}
					sb.append(Constants.EXTRA_TEXT_NODE_FOUND);
				}
			}
			else{
				sb.append(Constants.TEXT_NODE_NOT_FOUND);
			}
		}
	}

	//start setList
	void setList(Element ele , Node listNode,Map<String,String> urlMap){
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
				sb.append(Constants.NO_H2_ELEMENT_IN_LIST);
			}

			NodeIterator h3Nodes = listNode.hasNodes()?listNode.getNodes("element_subtitle*"):null;
			if(!h3Ele.isEmpty()){
				for(Element h3 : h3Ele){
					if(h3Nodes.hasNext()){
						Node h3Node = (Node)h3Nodes.next();
						h3Node.setProperty("subtitle",h3.text());
					}
				}
			}else{
				sb.append(Constants.NO_H3_ELEMENT_IN_LIST);
			}

			NodeIterator ulNodes = listNode.hasNodes()?listNode.getNodes("element_list*"):null;
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
						String aHref = a.absUrl("href");
						// Start extracting valid href
						log.debug("Before list" + aHref + "\n");
						aHref = FrameworkUtils.getLocaleReference(aHref, urlMap);
						log.debug("after list" + aHref + "\n");
						// End extracting valid href
						obj.put("linkurl",aHref);
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
					sb.append(Constants.NO_LIST_NODE_FOUND);
				}
			}

		}catch(Exception e){
			sb.append(Constants.UNABLE_TO_UPDATE_LIST);
		}
	}
	//end setList

	// This method to set content for spotlight
	public void setSpotlight(Element ele, Node spotLightComponentNode,String locale,Map<String,String> urlMap) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException{
		String title = "";
		String pText = "";
		String aText = "";
		Element h2Ele = ele.getElementsByTag("h2").first();
		if (h2Ele != null) {
			title = h2Ele.text();
			String tLink = h2Ele.getElementsByTag("a").first().absUrl("href");
			// Start extracting valid href
			log.debug("Before spotlight" + tLink + "\n");
			tLink = FrameworkUtils.getLocaleReference(tLink, urlMap);
			log.debug("after spotlight" + tLink + "\n");
			// End extracting valid href
			spotLightComponentNode.setProperty("title",title);
			Node titLink = spotLightComponentNode.hasNode("titlelink")?spotLightComponentNode.getNode("titlelink"):null;
			if(tLink!=null && !tLink.equals("")){
				if(titLink!=null){
					titLink.setProperty("url", tLink);	
				}else{
					sb.append(Constants.TITLE_LINK_NODE_NOT_AVAILABLE);
				}
			}else{
				sb.append(Constants.TITLE_DONOT_HAVE_LINK);
			}

		} else {
			sb.append(Constants.SPOTLIGHT_HEADING_TEXT_NOT_FOUND);
		}
		// start image
		String spotLightImage = FrameworkUtils.extractImagePath(ele, sb);
		log.debug("spotLightImage " + spotLightImage + "\n");

		if (spotLightComponentNode != null) {
			if (spotLightComponentNode.hasNode("image")) {
				Node spotLightImageNode = spotLightComponentNode.hasNode("image")?spotLightComponentNode.getNode("image"):null;
				String fileReference = spotLightImageNode.hasProperty("fileReference")?spotLightImageNode.getProperty("fileReference").getString():"";
				spotLightImage = FrameworkUtils.migrateDAMContent(spotLightImage, fileReference,  locale,sb);
				log.debug("spotLightImage " + spotLightImage + "\n");
				if (StringUtils.isNotBlank(spotLightImage)) {
					spotLightImageNode.setProperty("fileReference" , spotLightImage);
				}
			} else {
				sb.append(Constants.SPOTLIGHT_IMAGE_NODE_NOT_AVAILABLE);
			}
		}
		// end image
		Elements descriptionText = ele.getElementsByTag("p");
		String ulText = "";
		Element ulTextEle = ele.getElementsByTag("ul").first();
//		ulText = FrameworkUtils.extractHtmlBlobContent(descriptionText.first(), "", locale, sb, urlMap);
		if(ulTextEle!=null){
			String pTagText = descriptionText.text();
			ulText = FrameworkUtils.extractHtmlBlobContent(ulTextEle, "", locale, sb, urlMap);
			spotLightComponentNode.setProperty("description", pTagText+ulText);
		}
		else if (descriptionText != null && !descriptionText.isEmpty()) {
			for(Element descEle : descriptionText){
				pText = FrameworkUtils.extractHtmlBlobContent(descEle, "", locale, sb, urlMap);
			}
			spotLightComponentNode.setProperty("description", pText);
		}else{
			sb.append(Constants.SPOTLIGHT_DESCRIPTION_TEXT_NOT_FOUND);
		}

		Elements spotLightAnchor = ele.select("a.cta");
		if (spotLightAnchor != null && !spotLightAnchor.isEmpty()) {
			aText = spotLightAnchor.text();
			String linkUrl = spotLightAnchor.first().absUrl("href");
			// Start extracting valid href
			log.debug("Before spotlight" + linkUrl + "\n");
			linkUrl = FrameworkUtils.getLocaleReference(linkUrl, urlMap);
			log.debug("after spotlight" + linkUrl + "\n");
			// End extracting valid href
			spotLightComponentNode.setProperty("linktext", aText);
			javax.jcr.Node ctaNode = spotLightComponentNode.hasNode("cta")?spotLightComponentNode.getNode("cta"):null;
			if (ctaNode != null && linkUrl != null) {
				ctaNode.setProperty("url", linkUrl);
			}
		} else {
			sb.append(Constants.CTA_NOT_AVAILABLE);
		}
	}
}
