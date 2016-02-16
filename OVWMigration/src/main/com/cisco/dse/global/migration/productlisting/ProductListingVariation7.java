package com.cisco.dse.global.migration.productlisting;

/* S.No			Name		Date		Description of change
 * 1			Saroja		
 * 2			Bhavya		15-2-2015	Code changed after the live of es_mx,es_intl,en_cy,en_mt
 * */

import java.io.IOException;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.cisco.dse.global.migration.config.BaseAction;
import com.cisco.dse.global.migration.config.Constants;
import com.cisco.dse.global.migration.config.FrameworkUtils;

public class ProductListingVariation7 extends BaseAction {

	Document doc;

	String title = null;

	StringBuilder sb = new StringBuilder(1024);

	String footerLinks = "{\"linktext\":\"<aaa>\",\"linkurl\":\"<bbb>\"}";

	static Logger log = Logger.getLogger(ProductListingVariation7.class);

	public String translate(String host,String loc, String prod, String type,
			String catType, String locale, Session session, Map<String, String> urlMap) throws IOException,
			ValueFormatException, VersionException, LockException,
			ConstraintViolationException, RepositoryException {
		BasicConfigurator.configure();
		log.debug("In the translate method");
		log.debug("In the translate method, catType is :" + catType);

		// Repo node paths
		String pagePropertiesPath = "/content/<locale>/"+catType+"/<prod>/product-listing/jcr:content";
		String midGridNodePath = "/content/<locale>/"
				+ catType
				+ "/<prod>/product-listing/jcr:content/content_parsys/products/layout-products/gd21v1/gd21v1-mid";

		String pageUrl = host + "/content/<locale>/"
				+ catType + "/<prod>/product-listing.html";

		pageUrl = pageUrl.replace("<locale>", locale).replace("<prod>", prod);
		pagePropertiesPath = pagePropertiesPath.replace("<locale>", locale).replace("<prod>", prod);
		sb.append("<td>" + "<a href=" + pageUrl + ">" + pageUrl + "</a>"
				+ "</td>");
		sb.append("<td>" + "<a href=" + loc + ">" + loc + "</a>" + "</td>");
		sb.append("<td><ul>");

		midGridNodePath = midGridNodePath.replace("<locale>", locale).replace(
				"<prod>", prod);
		Node indexMidLeftNode = null;
		javax.jcr.Node pageJcrNode = null;
		try {
			indexMidLeftNode = session.getNode(midGridNodePath);
			pageJcrNode = session.getNode(pagePropertiesPath);
			try {
				doc = Jsoup.connect(loc).get();
				log.debug("Connected to the provided URL");
			} catch (Exception e) {
				doc = getConnection(loc);
			}

			if(doc != null){


				title = doc.title();
				
				// ------------------------------------------------------------------------------------------------------------------------------------------
				// start set page properties.

				FrameworkUtils.setPageProperties(pageJcrNode, doc, session, sb);

				// end set page properties.
				// ------------------------------------------------------------------------------------------------------------------------------------------

				
				// start set text component.
				try {
					Node textNodeOne = null;

					if(indexMidLeftNode.hasNode("text")){
						textNodeOne = indexMidLeftNode.getNode("text");
					}else{
						sb.append("<li> Text Node not found</li>");

					}
					String html = "";
					Elements textElements = doc.select("div.gd-right");
					if (textElements != null && !textElements.isEmpty()) {
						textElements = textElements.select("div.c00-pilot");
						if (textElements != null && !textElements.isEmpty()) {
							Element rightGridContent = textElements.first();
							if(rightGridContent != null){
								html = FrameworkUtils.extractHtmlBlobContent(rightGridContent, "",locale, sb, urlMap);
								if(textNodeOne !=null){
									textNodeOne.setProperty("text",html);
								}
							}
						} else {
							sb.append(Constants.TEXT_ELEMENT_NOT_FOUND);
						}
					} else {
						sb.append(Constants.TEXT_ELEMENT_NOT_FOUND);
					}
				} catch (Exception e) {
					sb.append("<li>" + Constants.EXCEPTION_TEXT_COMPONENT
							+ e + "</li>");
				}
				session.save();
			}
			else {
				sb.append(Constants.URL_CONNECTION_EXCEPTION);
			}
		} catch (Exception e) {
			log.debug("<li>unable to migrate page"+e+"</li>");
			log.debug("Exception as url cannot be connected: "+ e);
		}
		sb.append("</ul></td>");

		return sb.toString();
	}
}
