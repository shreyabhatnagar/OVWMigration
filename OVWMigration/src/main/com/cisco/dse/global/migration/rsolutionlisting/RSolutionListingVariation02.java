package com.cisco.dse.global.migration.rsolutionlisting;

import java.io.IOException;

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
			String catType, String locale, Session session) throws IOException,
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
				/*// start set page properties.

				FrameworkUtils.setPageProperties(pageJcrNode, doc, session, sb);

				// end set page properties.
*/
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
					migrateSpotLightContent(doc,solutionWideNode, locale);
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
		int childCount = 0;

		if(rightElements != null){
			c00Elements = rightElements.select("div.c00-pilot");
			cc00Elements = rightElements.select("div.cc00-pilot");
			if(c00Elements != null){
				c00 = c00Elements.first();
				if(c00 != null){
					h1Tags = c00.getElementsByTag("h1");
					if(h1Tags != null){
						setText= h1Tags.first();
					}
					childCount = c00.children().size();
				}
			}else if(cc00Elements != null){
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

	private void migrateSpotLightContent(Document doc, Node solutionWideNode, String locale) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
		// start set spotlight component.
		Elements spotLightElements = doc.select("div.c11-pilot");
		Elements spotLightTextElements = doc.select("div.cc00-pilot");

		if (solutionWideNode != null) {
			if(spotLightElements != null){
				int eleSize = spotLightElements.size();
				NodeIterator spoLightNodeIterator = solutionWideNode.hasNode("spotlight") ? solutionWideNode.getNodes("spotlight*") : null;
				int nodeSize = (int) spoLightNodeIterator.getSize();
				System.out.println("ele sizeee" + eleSize + "nodddd sizee" + nodeSize);
				if (eleSize == nodeSize) {
					for (Element ele : spotLightElements) {
						spoLightNodeIterator.hasNext();
						Node spotLightComponentNode = (Node) spoLightNodeIterator.next();
						setSpotLightContent(ele,spotLightComponentNode, locale);
					}
				}

				if (nodeSize < eleSize) {
					for (Element ele : spotLightElements) {
						if(spoLightNodeIterator.hasNext()){
						Node spotLightComponentNode = (Node) spoLightNodeIterator.next();
						setSpotLightContent(ele,spotLightComponentNode, locale);
						}
					}
					sb.append(Constants.SPOTLIGHT_NODE_COUNT_MISMATCH);
				}
				if (nodeSize > eleSize) {
					for (Element ele : spotLightElements) {
						spoLightNodeIterator.hasNext();
						Node spotLightComponentNode = (Node) spoLightNodeIterator.next();
						setSpotLightContent(ele,spotLightComponentNode, locale);
					}
					sb.append(Constants.SPOTLIGHT_NODE_COUNT_MISMATCH);
				}
			}
			else if(spotLightTextElements != null){
				Element cc00Last = spotLightTextElements.last();
				if(cc00Last != null){
					Elements spotLightChildrenElements = cc00Last.children();
					int eleSize = cc00Last.getElementsByTag("h3").size();
					NodeIterator spoLightNodeIterator = solutionWideNode.getNodes("spotlight*");
					int nodeSize = (int) spoLightNodeIterator.getSize();
					if (eleSize == nodeSize) {
						for (Element ele : spotLightChildrenElements) {
							spoLightNodeIterator.hasNext();
							Node spotLightComponentNode = (Node) spoLightNodeIterator.next();
							setSpotLightTextContent(ele,spotLightComponentNode, locale);
						}
					}

					if (nodeSize < eleSize) {
						for (Element ele : spotLightChildrenElements) {
							spoLightNodeIterator.hasNext();
							Node spotLightComponentNode = (Node) spoLightNodeIterator.next();
							setSpotLightTextContent(ele,spotLightComponentNode, locale);
						}
						sb.append(Constants.SPOTLIGHT_NODE_COUNT_MISMATCH);
					}
					if (nodeSize > eleSize) {
						for (Element ele : spotLightChildrenElements) {
							spoLightNodeIterator.hasNext();
							Node spotLightComponentNode = (Node) spoLightNodeIterator.next();
							setSpotLightTextContent(ele,spotLightComponentNode, locale);
						}
						sb.append(Constants.SPOTLIGHT_NODE_COUNT_MISMATCH);
					}
				}
			}
			else {
					System.out.println("error");
			}
		} else {
			sb.append("<li>Unable to update spotlight component as its respective div is not available.</li>");

		}
	}

	private void setSpotLightTextContent(Element ele,Node spotLightComponentNode, String locale) {
		
	}

	//Start Spotlight Set Method
	private void setSpotLightContent(Element ele, Node spotLightComponentNode,String locale) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
		String h2Text = "";
		String pText = "";
		String aText = "";
		String aHref = "";
		String h3Text = "";
		
		Elements h2TagText = ele.getElementsByTag("h2");
		Elements h3TagText = ele.getElementsByTag("h3");
		if (h2TagText != null) {
			h2Text = h2TagText.html();		
			spotLightComponentNode.setProperty("title",h2Text);
		}else if (h3TagText != null){
			h3Text = h3TagText.html();
			spotLightComponentNode.setProperty("title",h3Text);
		} 
		else {
			sb.append("<li>Spotlight Component Heading element not having any title in it ('h2' is blank)</li>");
		}

		Elements descriptionText = ele.getElementsByTag("p");
		if (descriptionText != null) {
			pText = descriptionText.html();
			spotLightComponentNode.setProperty("description", pText);
		} else {
			sb.append("<li>Spotlight Component description element not having any title in it ('p' is blank)</li>");
		}

		Elements anchorText = ele.getElementsByTag("a");
		if (anchorText != null) {
			aText = anchorText.text();
			spotLightComponentNode.setProperty("ctaText",aText);
		} else {
			sb.append("<li>Spotlight Component anchor tag not having any content in it ('<a>' is blank)</li>");
		}

		Elements anchorHref = ele.select("a[href]");
		Node spoLightNode = spotLightComponentNode.hasNode("cta") ? spotLightComponentNode.getNode("cta") : null;
		if (anchorHref != null) {
			aHref = anchorHref.attr("href");
			spoLightNode.setProperty("url",aHref);
		} else {
			sb.append("<li>Spotlight Component anchor tag not having any href content in it ('<a>' is blank)</li>");
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
				sb.append("<li>spotlight image node doesn't exist</li>");
			}
		}
		// end image
		
	}
	//End of SpotLight Set Method


}
// end set spotlight nodes




