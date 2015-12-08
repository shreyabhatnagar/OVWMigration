package com.cisco.dse.global.migration.productlisting;

import java.io.IOException;

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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.cisco.dse.global.migration.config.BaseAction;
import com.cisco.dse.global.migration.config.Constants;
public class ProductListing4 extends BaseAction{

	/**
	 * @param args
	 */

	Document doc;

	String title = null;

	StringBuilder sb = new StringBuilder(1024);

	String footerLinks = "{\"linktext\":\"<aaa>\",\"linkurl\":\"<bbb>\"}";

	static Logger log = Logger.getLogger(ProductListing4.class);

	public String translate(String host, String loc, String prod, String type,
			String catType, String locale, Session session) throws IOException,
			ValueFormatException, VersionException, LockException,
			ConstraintViolationException, RepositoryException {
		BasicConfigurator.configure();
		log.debug("In the translate method");
		log.debug("In the translate method, catType is :" + catType);

		// Repo node paths

		String productListingMid = "/content/<locale>/"
				+ catType
				+ "/<prod>/product-listing/jcr:content/content_parsys/products/layout-products/gd21v1/gd21v1-mid";


		String pageUrl = host + "/content/<locale>/" + catType
				+ "/<prod>/product-listing.html";

		pageUrl = pageUrl.replace("<locale>", locale).replace("<prod>", prod);

		sb.append("<td>" + "<a href=" + pageUrl + ">" + pageUrl + "</a>"
				+ "</td>");
		sb.append("<td>" + "<a href=" + loc + ">" + loc + "</a>" + "</td>");
		sb.append("<td><ul>");

		log.debug("SB value here is "+sb.toString());
		productListingMid = productListingMid.replace("<locale>", locale).replace("<prod>", prod);

		javax.jcr.Node productListingMidNode = null;

		try {
			productListingMidNode = session.getNode(productListingMid);
			log.debug("Path for node:" + productListingMidNode.getPath());
			try {
				doc = Jsoup.connect(loc).get();

				// start of text component
				try{
					setText(doc, productListingMidNode);
				}catch(Exception e){
					sb.append(Constants.EXCEPTION_TEXT_COMPONENT);
				}	
				// end of text component

				// start of HtmlBlob
				try {
					setHTMLBlob(doc, productListingMidNode);
				}
				catch (Exception e){
					sb.append(Constants.EXCEPTION_IN_HTMLBLOB);
				}
				// end of HtmlBlob

			} catch (Exception e) {
				getConnection(loc);
				sb.append(Constants.URL_CONNECTION_EXCEPTION);
			}

		}catch (Exception e) {
			sb.append(Constants.UNABLE_TO_UPDATE_PRODUCTLISTING);
		}
		sb.append("</ul></td>");
		session.save();
		log.debug("Msg returned is "+sb.toString());
		return sb.toString();
	}

	// Set Text Method
	public void setText(Document doc, Node productListingMidNode) throws RepositoryException{
		
		Elements textElements = doc.select("div.c00-pilot");
		if(textElements == null){
			sb.append(Constants.TEXT_ELEMENT_NOT_FOUND);
		}
		else{
			int eleSize = textElements.size();
			NodeIterator textNodeIterator = productListingMidNode.hasNode("text") ?productListingMidNode.getNodes("text*"):null;
			if(textNodeIterator != null){
				int nodeSize = (int)textNodeIterator.getSize();
				log.debug("node Size" + nodeSize + "ele Size" + eleSize);
				if(eleSize == nodeSize){
					for(Element ele : textElements){
						Node textNode = (Node)textNodeIterator.next();
						textNode.setProperty("text", ele.html());
					}
				}
				else if(nodeSize < eleSize){
					for(Element ele : textElements){
						if(textNodeIterator.hasNext()){
							Node textNode = (Node)textNodeIterator.next();
							textNode.setProperty("text", ele.html());
						}
						else{
							sb.append(Constants.TEXT_NODE_COUNT+nodeSize+Constants.TEXT_ELEMENT_COUNT+eleSize+"</li>");
						}
					}
				}
				else if(nodeSize > eleSize){
					for(Element ele : textElements){
						Node textNode = (Node)textNodeIterator.next();
						textNode.setProperty("text", ele.html());
					}
					sb.append(Constants.TEXT_NODE_COUNT+nodeSize+Constants.TEXT_ELEMENT_COUNT+eleSize+"</li>");
				}
			}
			else{
				sb.append(Constants.TEXT_NODE_NOT_FOUND);
			}
		}
	}
	//End of Text Method

	//Start of setHTMLBlob
	public void setHTMLBlob(Document doc, Node productListingMidNode) throws RepositoryException{
		Element htmlblobele = doc.getElementById("n21");
		if(htmlblobele != null && !htmlblobele.equals("")){
			Node blobNode = productListingMidNode.hasNode("htmlblob") ? productListingMidNode.getNode("htmlblob") : null;
			if(blobNode != null){
				blobNode.setProperty("html", htmlblobele.html());
			}else
			{
				sb.append(Constants.HTMLBLOB_ELEMENT_NOT_FOUND);
			}
		}else {
			sb.append(Constants.HTMLBLOB_NODE_NOT_FOUND);
		}

	}
	
	//End of setHTMLBlob
}

