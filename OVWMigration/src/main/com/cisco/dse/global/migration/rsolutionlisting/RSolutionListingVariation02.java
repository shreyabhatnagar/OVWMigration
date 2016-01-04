package com.cisco.dse.global.migration.rsolutionlisting;

import java.io.IOException;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.cisco.dse.global.migration.config.BaseAction;
import com.cisco.dse.global.migration.config.Constants;
import com.cisco.dse.global.migration.config.FrameworkUtils;

public class RSolutionListingVariation02 extends BaseAction{
	Document doc = null;

	StringBuilder sb = new StringBuilder(1024);

	Logger log = Logger.getLogger(RSolutionListingVariation02.class);

	public String translate(String host, String loc, String prod, String type,
			String catType, String locale, Session session, Map<String, String> urlMap) throws IOException,
			ValueFormatException, VersionException, LockException,
			ConstraintViolationException, RepositoryException {

		log.debug("In the translate method");
		log.debug("In the translate method, catType is :" + catType);

		String pagePropertiesPath = "/content/<locale>/"+catType+"/<prod>/solution-listing/jcr:content";
		String solutionRight = "/content/<locale>/"+ catType+ "/<prod>/solution-listing/jcr:content/Grid/category/layout-category/narrowwide/NW-Wide-2";

		String pageUrl = host + "/content/<locale>/"+ catType + "/<prod>/solution-listing.html";
		pageUrl = pageUrl.replace("<locale>", locale).replace("<prod>", prod);
		pagePropertiesPath = pagePropertiesPath.replace("<locale>", locale).replace("<prod>", prod);
		sb.append("<td>" + "<a href=" + pageUrl + ">" + pageUrl + "</a>"+ "</td>");
		sb.append("<td>" + "<a href=" + loc + ">" + loc + "</a>" + "</td>");
		sb.append("<td><ul>");

		solutionRight = solutionRight.replace("<locale>", locale).replace("<prod>", prod);

		javax.jcr.Node solutionWideNode = null;
		javax.jcr.Node pageJcrNode = null;

		try {
			solutionWideNode = session.getNode(solutionRight);
			pageJcrNode = session.getNode(pagePropertiesPath);

			try {
				doc = getConnection(loc);
				log.debug("Connected to the provided URL");
			} catch (Exception e) {
				sb.append(Constants.URL_CONNECTION_EXCEPTION);
			}

			if(doc != null){
				// start set page properties.

				FrameworkUtils.setPageProperties(pageJcrNode, doc, session, sb);

				// end set page properties.

				//Start of text Element
				try {
					migrateTextContent(doc,solutionWideNode );
				}
				catch(Exception e){
					sb.append(Constants.UNABLE_TO_MIGRATE_TEXT);
				}
				//End of text Element

				//Start of Spotlight
				try {
					migrateSpotLightContent(doc,solutionWideNode, locale, urlMap);
				}
				catch(Exception e){
					sb.append(Constants.UNABLE_TO_UPDATE_SPOTLIGHT);
				}
				//End of Spotlight


			}else{
				sb.append(Constants.URL_CONNECTION_EXCEPTION);
			}
		}catch(Exception e){
			sb.append(Constants.URL_CONNECTION_EXCEPTION);
		}
		sb.append("</ul></td>");
		session.save();
		log.debug("Msg returned is "+sb.toString());
		return sb.toString();
	}

	private void migrateTextContent(Document doc, Node solutionWideNode) throws PathNotFoundException, RepositoryException {

		Elements rightElements = doc.select("div.gd-right");
		Element setText = null;
		Elements c00Elements = null;
		Elements cc00Elements = null;
		Element c00 = null;
		Element cc00 = null;
		Elements h1Tags = null;
		Elements h2Tags= null;
		int childCount = 0;

		if(rightElements != null){
			c00Elements = rightElements.select("div.c00-pilot,div.c11-pilot");
			cc00Elements = rightElements.select("div.cc00-pilot");
			if(!c00Elements.isEmpty()){
				c00 = c00Elements.first();
				if(c00 != null){
					h1Tags = c00.getElementsByTag("h1");
					h2Tags = c00.getElementsByTag("h2");
					if(!h1Tags.isEmpty()){
						setText= h1Tags.first();
					}
					else if(!h2Tags.isEmpty()){
						setText= h2Tags.first();
					}
					childCount = c00.children().size();
				}
			}else if(!cc00Elements.isEmpty()){
				cc00 = cc00Elements.first();
				if(cc00 != null){
					h1Tags = cc00.getElementsByTag("h1");
					if(h1Tags != null){
						setText= h1Tags.first();
					}
					childCount = cc00.children().size();
				}
			}else {
				sb.append(Constants.TEXT_DOES_NOT_EXIST);
			}
		}

		Node textNode = solutionWideNode.hasNode("header") ? solutionWideNode.getNode("header"):null;
		if(textNode != null){
			if(setText != null){
				textNode.setProperty("title", setText.text());
				if(childCount > 1){
					sb.append(Constants.EXTRA_TEXT_ELEMENT_FOUND);
				}
			}else {
				if(childCount > 0){
					sb.append(Constants.EXTRA_TEXT_ELEMENT_FOUND);
				}
				sb.append(Constants.TEXT_DOES_NOT_EXIST);
			}
		}else{
			sb.append(Constants.TEXT_NODE_NOT_FOUND);
		}

	}

	private void migrateSpotLightContent(Document doc, Node solutionWideNode, String locale, Map<String, String> urlMap) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
		// start set spotlight component.
		Elements spotLightElements = doc.select("div.c11-pilot");
		Elements spotLightTextElements = doc.select("div.cc00-pilot");

		if (solutionWideNode != null) {
			if(!spotLightElements.isEmpty()){
				int eleSize = spotLightElements.size();
				NodeIterator spoLightNodeIterator = solutionWideNode.hasNode("spotlight") ? solutionWideNode.getNodes("spotlight*") : null;
				int nodeSize = (int) spoLightNodeIterator.getSize();
				if (eleSize == nodeSize) {
					for (Element ele : spotLightElements) {
						spoLightNodeIterator.hasNext();
						Node spotLightComponentNode = (Node) spoLightNodeIterator.next();
						setSpotLightContent(ele,spotLightComponentNode, locale, urlMap);
					}
				}

				if (nodeSize < eleSize) {
					for (Element ele : spotLightElements) {
						if(spoLightNodeIterator.hasNext()){
							Node spotLightComponentNode = (Node) spoLightNodeIterator.next();
							setSpotLightContent(ele,spotLightComponentNode, locale, urlMap);
						}
					}
					sb.append(Constants.SPOTLIGHT_NODE_COUNT + nodeSize + Constants.SPOTLIGHT_ELEMENT_COUNT + eleSize);
				}
				if (nodeSize > eleSize) {
					for (Element ele : spotLightElements) {
						spoLightNodeIterator.hasNext();
						Node spotLightComponentNode = (Node) spoLightNodeIterator.next();
						setSpotLightContent(ele,spotLightComponentNode, locale, urlMap);
					}
					sb.append(Constants.SPOTLIGHT_NODE_COUNT + nodeSize + Constants.SPOTLIGHT_ELEMENT_COUNT + eleSize);
				}
			}
			else if(!spotLightTextElements.isEmpty()){
				Element cc00Last = spotLightTextElements.last();
				if(cc00Last != null){
					Elements h3Tag = cc00Last.getElementsByTag("h3");
					Elements pTag = cc00Last.getElementsByTag("p");
					Elements aTag = cc00Last.getElementsByTag("a");

					/*	Elements descriptionLinkText = cc00Last.select("p[class]");
					String ctaLink = null;
					boolean ctaLinkExists = false;
					if(!descriptionLinkText.isEmpty()){ 
						ctaLink = descriptionLinkText.attr("class");
						if(!ctaLink.isEmpty()){
							if(ctaLink.equals("cta-link")){
								ctaLinkExists = true;
							}
							else {
								ctaLinkExists = false;
							}
						}
					}*/

					int eleSize = h3Tag.size();
					NodeIterator spoLightNodeIterator = solutionWideNode.hasNode("spotlight") ? solutionWideNode.getNodes("spotlight*") : null;
					NodeIterator spoLightNodePTagIterator = solutionWideNode.hasNode("spotlight") ? solutionWideNode.getNodes("spotlight*") : null;
					NodeIterator spoLightNodeLinkIterator = solutionWideNode.hasNode("spotlight") ? solutionWideNode.getNodes("spotlight*") : null;

					int nodeSize = (int) spoLightNodeIterator.getSize();
					if (eleSize == nodeSize) {
						for (Element ele : h3Tag) {
							spoLightNodeIterator.hasNext();
							Node spotLightComponentNode = (Node) spoLightNodeIterator.next();
							setSpotLightTitleContent(ele,spotLightComponentNode, locale, urlMap);
						}
						for (Element ele : pTag) {
							spoLightNodePTagIterator.hasNext();
							Node spotLightComponentNode = (Node) spoLightNodePTagIterator.next();
							setSpotLightTextContent(ele,spotLightComponentNode, locale, urlMap);
						}
						for (Element ele : aTag) {
							spoLightNodeLinkIterator.hasNext();
							Node spotLightComponentNode = (Node) spoLightNodeLinkIterator.next();
							setSpotLightAnchorContent(ele,spotLightComponentNode, locale, urlMap);
						}
					}

					if (nodeSize < eleSize) {
						for (Element ele : h3Tag) {
							if(spoLightNodeIterator.hasNext()){
							Node spotLightComponentNode = (Node) spoLightNodeIterator.next();
							setSpotLightTitleContent(ele,spotLightComponentNode, locale, urlMap);
							}
						}
						for (Element ele : pTag) {
							if(spoLightNodePTagIterator.hasNext()){
							Node spotLightComponentNode = (Node) spoLightNodePTagIterator.next();
							setSpotLightTextContent(ele,spotLightComponentNode, locale, urlMap);
							}
						}
						for (Element ele : aTag) {
							if(spoLightNodeLinkIterator.hasNext()){
							Node spotLightComponentNode = (Node) spoLightNodeLinkIterator.next();
							setSpotLightAnchorContent(ele,spotLightComponentNode, locale, urlMap);
							}
						}
						sb.append(Constants.SPOTLIGHT_NODE_COUNT + nodeSize + Constants.SPOTLIGHT_ELEMENT_COUNT + eleSize);
					}
					if (nodeSize > eleSize) {
						for (Element ele : h3Tag) {
							spoLightNodeIterator.hasNext();
							Node spotLightComponentNode = (Node) spoLightNodeIterator.next();
							setSpotLightTitleContent(ele,spotLightComponentNode, locale, urlMap);
						}
						for (Element ele : pTag) {
							spoLightNodePTagIterator.hasNext();
							Node spotLightComponentNode = (Node) spoLightNodePTagIterator.next();
							setSpotLightTextContent(ele,spotLightComponentNode, locale, urlMap);
						}
						for (Element ele : aTag) {
							spoLightNodePTagIterator.hasNext();
							Node spotLightComponentNode = (Node) spoLightNodePTagIterator.next();
							setSpotLightAnchorContent(ele,spotLightComponentNode, locale, urlMap);
						}
						sb.append(Constants.SPOTLIGHT_NODE_COUNT + nodeSize + Constants.SPOTLIGHT_ELEMENT_COUNT + eleSize);
					}
				}
			}
			else {
				sb.append(Constants.SPOTLIGHT_PARENT_DIV_NOT_FOUND);
			}
		} else {
			sb.append(Constants.SPOTLIGHT_NODE_NOT_FOUND);

		}
	}

	private void setSpotLightAnchorContent(Element ele,Node spotLightComponentNode, String locale, Map<String, String> urlMap) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {

		Element parentEle = ele.parent();
		if(parentEle != null){
			Elements h3Parent = parentEle.getElementsByTag("h3");
			if(h3Parent.isEmpty()){
				
				String aText = "";
				String aHref = "";
			
				Elements anchorText = ele.getElementsByTag("a");
				if (anchorText != null) {
					aText = anchorText.text();
					spotLightComponentNode.setProperty("ctaText",aText);
				} else {
					sb.append(Constants.SPOTLIGHT_ANCHOR_TEXT_NOT_FOUND);
				}

				Elements anchorHref = ele.select("a[href]");
				Node spoLightNode = spotLightComponentNode.hasNode("cta") ? spotLightComponentNode.getNode("cta") : null;
				if (anchorHref != null) {
					aHref = anchorHref.attr("href");
					// Start extracting valid href
					log.debug("Before aHref" + aHref + "\n");
					aHref = FrameworkUtils.getLocaleReference(aHref, urlMap);
					log.debug("after aHref" + aHref + "\n");
					// End extracting valid href
					spoLightNode.setProperty("url",aHref);
				} else {
					sb.append(Constants.SPOTLIGHT_ANCHOR_LINK_NOT_FOUND);
				}
				
			}else {
				sb.append(Constants.SPOTLIGHT_ANCHOR_TEXT_NOT_FOUND);
				sb.append(Constants.SPOTLIGHT_ANCHOR_LINK_NOT_FOUND);
			}
		}
	}

	//Start Spotlight Set Method
	private void setSpotLightContent(Element ele, Node spotLightComponentNode,String locale, Map<String, String> urlMap) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
		String h2Text = "";
		String pText = "";
		String aText = "";
		String aHref = "";
		String h3Text = "";

		Elements h2TagText = ele.getElementsByTag("h2");
		Elements h3TagText = ele.getElementsByTag("h3");
		if (!h2TagText.isEmpty()) {
			h2Text = h2TagText.html();		
			spotLightComponentNode.setProperty("title",h2Text);
		}else if (h3TagText != null){
			h3Text = h3TagText.html();
			spotLightComponentNode.setProperty("title",h3Text);
		} 
		else {
			sb.append(Constants.SPOTLIGHT_HEADING_TEXT_NOT_FOUND);
		}

		Elements descriptionText = ele.getElementsByTag("p");
		if (descriptionText != null) {
			pText = descriptionText.html();
			spotLightComponentNode.setProperty("description", pText);
		} else {
			sb.append(Constants.SPOTLIGHT_DESCRIPTION_TEXT_NOT_FOUND);
		}

		Elements anchorText = ele.getElementsByTag("a");
		if (anchorText != null) {
			Element lastAnchorText = ele.getElementsByTag("a").last();
			if(lastAnchorText != null){
			aText = lastAnchorText.text();
			spotLightComponentNode.setProperty("ctaText",aText);
			}
		} else {
			sb.append(Constants.SPOTLIGHT_ANCHOR_TEXT_NOT_FOUND);
		}

		Elements anchorHref = ele.select("a[href]");
		Node spoLightNode = spotLightComponentNode.hasNode("cta") ? spotLightComponentNode.getNode("cta") : null;
		if (anchorHref != null) {
			Element lastAnchorHref = ele.select("a[href]").last();
			if(lastAnchorHref != null){
			aHref = lastAnchorHref.attr("href");
			// Start extracting valid href
			log.debug("Before aHref" + aHref + "\n");
			aHref = FrameworkUtils.getLocaleReference(aHref, urlMap);
			log.debug("after aHref" + aHref + "\n");
			// End extracting valid href
			spoLightNode.setProperty("url",aHref);
			}
		} else {
			sb.append(Constants.SPOTLIGHT_ANCHOR_LINK_NOT_FOUND);
		}

		// start image
		String spotLightImage = FrameworkUtils.extractImagePath(ele, sb);
		log.debug("spotLightImage befor migration : " + spotLightImage + "\n");
		if (spotLightComponentNode != null) {
			if (spotLightComponentNode.hasNode("image")) {
				Node spotLightImageNode = spotLightComponentNode.getNode("image");
				String fileReference = spotLightImageNode.hasProperty("fileReference")?spotLightImageNode.getProperty("fileReference").getString():"";
				spotLightImage = FrameworkUtils.migrateDAMContent(spotLightImage, fileReference, locale, sb);
				log.debug("spotLightImage after migration : " + spotLightImage + "\n");
				if (StringUtils.isNotBlank(spotLightImage)) {
					spotLightImageNode.setProperty("fileReference" , spotLightImage);
				}
			} else {
				sb.append(Constants.SPOTLIGHT_IMAGE_NOT_AVAILABLE);
			}
		}
		// end image

	}
	//End of SpotLight Set Method

	//Start of Spot Light text Content into Spotlight
	private void setSpotLightTextContent(Element ele,Node spotLightComponentNode, String locale, Map<String, String> urlMap) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
		String pText = "";
		Elements descriptionText = ele.getElementsByTag("p");
		Elements descriptionLinkText = ele.select("p[class]");
		String aText = "";
		String aHref = "";

		if(!descriptionLinkText.isEmpty()){
			Elements anchorText = ele.getElementsByTag("a");
			if (anchorText != null) {
				aText = anchorText.text();
				spotLightComponentNode.setProperty("ctaText",aText);
			} else {
				sb.append(Constants.SPOTLIGHT_ANCHOR_ELEMENT_NOT_FOUND);
			}

			Elements anchorHref = ele.select("a[href]");
			Node spoLightNode = spotLightComponentNode.hasNode("cta") ? spotLightComponentNode.getNode("cta") : null;
			if (anchorHref != null) {
				aHref = anchorHref.attr("href");
				// Start extracting valid href
				log.debug("Before aHref" + aHref + "\n");
				aHref = FrameworkUtils.getLocaleReference(aHref, urlMap);
				log.debug("after aHref" + aHref + "\n");
				// End extracting valid href
				spoLightNode.setProperty("url",aHref);
			} else {
				sb.append(Constants.SPOTLIGHT_ANCHOR_LINK_NOT_FOUND);
			}

		}
		else if (!descriptionText.isEmpty()) {
			pText = descriptionText.html();
			spotLightComponentNode.setProperty("description", pText);
		} else {
			sb.append(Constants.EXTRA_TEXT_ELEMENT_FOUND);
		}
	}
	//End of Spot Light text Content into Spotlight

	//Start of Spot Light title Content into Spotlight
	private void setSpotLightTitleContent(Element ele,Node spotLightComponentNode, String locale, Map<String, String> urlMap) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
		String aText = "";

		Elements anchorText = ele.getElementsByTag("a");
		Elements h3Text = ele.getElementsByTag("h3");
		if(!h3Text.isEmpty()){
			Element h3First = h3Text.first();
			if(h3First != null){
				String h3OwnText = h3First.ownText();
				if(!h3OwnText.isEmpty()){
					spotLightComponentNode.setProperty("title",h3OwnText);
				}
				else if (!anchorText.isEmpty()){
					aText = anchorText.text();
					spotLightComponentNode.setProperty("title",aText);
					Node titleLink = spotLightComponentNode.hasNode("titleLink") ? spotLightComponentNode.getNode("titleLink") : null;
					if(titleLink != null){
						Elements aHref = anchorText.select("a[href]");
						if (aHref != null) {
							String anchorHref = aHref.attr("href");
							// Start extracting valid href
							log.debug("Before anchorHref" + anchorHref + "\n");
							anchorHref = FrameworkUtils.getLocaleReference(anchorHref, urlMap);
							log.debug("after anchorHref" + anchorHref + "\n");
							// End extracting valid href
							titleLink.setProperty("linktype", "Url");
							titleLink.setProperty("url",anchorHref);
						} 
					}else {
						sb.append(Constants.TITLE_LINK_NODE_NOT_AVAILABLE);
					}
				}
			}
		}

		// start image
		String spotLightImage = FrameworkUtils.extractImagePath(ele, sb);
		log.debug("spotLightImage befor migration : " + spotLightImage + "\n");
		if (spotLightComponentNode != null) {
			if (spotLightComponentNode.hasNode("image")) {
				Node spotLightImageNode = spotLightComponentNode.getNode("image");
				String fileReference = spotLightImageNode.hasProperty("fileReference")?spotLightImageNode.getProperty("fileReference").getString():"";
				spotLightImage = FrameworkUtils.migrateDAMContent(spotLightImage, fileReference, locale, sb);
				log.debug("spotLightImage after migration : " + spotLightImage + "\n");
				if (StringUtils.isNotBlank(spotLightImage)) {
					spotLightImageNode.setProperty("fileReference" , spotLightImage);
				}
			} else {
				sb.append(Constants.SPOTLIGHT_IMAGE_NOT_AVAILABLE);
			}
		}
		// end image
	}
	//End of Spot Light title Content into Spotlight

}
// end set spotlight nodes