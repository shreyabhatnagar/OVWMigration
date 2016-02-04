package com.cisco.dse.global.migration.benefit;

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
import org.apache.log4j.Logger;
import org.apache.sling.commons.json.JSONObject;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.cisco.dse.global.migration.config.BaseAction;
import com.cisco.dse.global.migration.config.Constants;
import com.cisco.dse.global.migration.config.FrameworkUtils;

public class BenefitsVariation1 extends BaseAction {
	Document doc;

	StringBuilder sb = new StringBuilder(1024);

	static Logger log = Logger.getLogger(BenefitsVariation1.class);

	boolean flag_enil = false;

	public String translate(String host, String loc, String prod, String type,
			String catType, String locale, Session session,
			Map<String, String> urlMap) throws IOException,
			ValueFormatException, VersionException, LockException,
			ConstraintViolationException, RepositoryException {

		log.debug("In the translate method of BenefitsVariation1");
		log.debug("In the translate method, catType is :" + catType);

		// Repo node paths
		String pagePropertiesPath = "/content/<locale>/" + catType
				+ "/<prod>/benefit/jcr:content";
		String pageUrl = host + "/content/<locale>/" + catType
				+ "/<prod>/benefit.html";
		pagePropertiesPath = pagePropertiesPath.replace("<locale>", locale)
				.replace("<prod>", prod);
		pageUrl = pageUrl.replace("<locale>", locale).replace("<prod>", prod);

		sb.append("<td>" + "<a href=" + pageUrl + ">" + pageUrl + "</a>"
				+ "</td>");
		sb.append("<td>" + "<a href=" + loc + ">" + loc + "</a>" + "</td>");
		sb.append("<td><ul>");

		String benefitsLeftNodePath = "/content/<locale>/"
				+ catType
				+ "/<prod>/benefit/jcr:content/content_parsys/benefits/layout-benefits/gd12v2/gd12v2-left";
		String benefitsRightNodePath = "/content/<locale>/"
				+ catType
				+ "/<prod>/benefit/jcr:content/content_parsys/benefits/layout-benefits/gd12v2/gd12v2-right";

		benefitsLeftNodePath = benefitsLeftNodePath.replace("<locale>", locale)
				.replace("<prod>", prod);
		benefitsRightNodePath = benefitsRightNodePath.replace("<locale>",
				locale).replace("<prod>", prod);

		javax.jcr.Node benefitsLeftNode = null;
		javax.jcr.Node benefitsRightNode = null;
		javax.jcr.Node pageJcrNode = null;
		try {
			benefitsLeftNode = session.getNode(benefitsLeftNodePath);
			benefitsRightNode = session.getNode(benefitsRightNodePath);
			pageJcrNode = session.getNode(pagePropertiesPath);
			try {
				doc = getConnection(loc);
			} catch (Exception e) {
				log.error("Exception ", e);
			}
			if (doc != null) {
				// start set page properties.
				log.debug("Started setting page properties");
				FrameworkUtils.setPageProperties(pageJcrNode, doc, session, sb);
				log.debug("Done with the setting page properties");
				// end set page properties.
				// -------------------------------------------------------------------------------------------------------------------
				// start of title text component
				try {
					String text = "";
					// getting data
					Element textEle = doc.select("div.gd-left")
							.select("div.c00-pilot,div.cc00-pilot").first();
					if(textEle.hasClass("cc00-pilot")){
						textEle = textEle.getElementsByTag("h1").first();
						flag_enil = true;
					}
					if (textEle != null) {
						text = FrameworkUtils.extractHtmlBlobContent(textEle,
								"", locale, sb, urlMap);
					} else {
						sb.append(Constants.TEXT_ELEMENT_NOT_FOUND);
					}
					// setting data
					Node textNode = benefitsLeftNode.hasNode("text") ? benefitsLeftNode
							.getNode("text") : null;
							if (textNode != null) {
								if (StringUtils.isNotEmpty(text) && text != null) {
									textNode.setProperty("text", text);
								}
							} else {
								sb.append(Constants.TEXT_NODE_NOT_FOUND);
							}

				} catch (Exception e) {
					sb.append(Constants.UNABLE_TO_MIGRATE_TEXT);
					log.error("Exception ", e);
				}
				// end of title text component
				// ------------------------------------------------------------------------------------------------------------
				// start of text and list component
				try {
					String text = "";
					String aText = "";
					String aHref = "";
					boolean flag = false;
					String listTitle = "";
					List<String> list = new ArrayList<String>();
					StringBuilder paraList = new StringBuilder();
					List<Element> ulList = new ArrayList<Element>();
					int childrenSize;
					// getting data of text component
					Element textEle = doc.select("div.gd-left")
							.select("div.c00-pilot,div.nn13-pilot").last();
					if (textEle != null) {
						childrenSize = textEle.children().size();
						for (int count = 0; count < childrenSize; count++) {
							Element child = textEle.child(count);
							if (child != null) {
								if ("h2".equalsIgnoreCase(child.tagName())) {
									flag = true;
								}
								if (flag == false) {
									paraList.append(FrameworkUtils
											.extractHtmlBlobContent(child, "",
													locale, sb, urlMap));
								} else {
									ulList.add(child);
								}
							}
						}
						text = paraList.toString();
						log.debug("Text elements:" + text);
						log.debug("List elements:" + ulList);
						if(flag_enil){
							Element cc00Ele = doc.select("div.cc00-pilot").first();
							cc00Ele.select("h1").remove();
							text = FrameworkUtils.extractHtmlBlobContent(cc00Ele, "",locale, sb, urlMap);
						}
					} else {
						sb.append(Constants.TEXT_ELEMENT_NOT_FOUND);
					}
					// setting data of text component
					Node textNode = benefitsLeftNode.hasNode("text_0") ? benefitsLeftNode
							.getNode("text_0") : null;
							if (textNode != null) {
								if (StringUtils.isNotEmpty(text) && text != null) {
									textNode.setProperty("text", text);
								}
							} else {
								sb.append(Constants.TEXT_NODE_NOT_FOUND);
							}
							if (!ulList.isEmpty()) {
								// getting data of list component
								listTitle = ulList.get(0).text();
								Elements listEle = ulList.get(1).select("a");
								for (Element ele : listEle) {
									JSONObject obj = new JSONObject();
									aText = ele.text();
									aHref = ele.absUrl("href");
									// Start extracting valid href
									log.debug("link ref before migration : " + aHref);
									aHref = FrameworkUtils.getLocaleReference(aHref,
											urlMap);
									log.debug("link ref after migration : " + aHref);
									// End extracting valid href
									obj.put("linktext", aText);
									obj.put("linkurl", aHref);
									obj.put("icon", "none");
									obj.put("size", "");
									obj.put("description", "");
									obj.put("openInNewWindow", false);
									list.add(obj.toString());
								}
							} else {
								sb.append(Constants.LIST_ELEMENT_NOT_FOUND);
							}
							// setting data of list component
							Node listNode = benefitsLeftNode.hasNode("list") ? benefitsLeftNode
									.getNode("list") : null;
									if (listNode != null) {
										if (StringUtils.isNotEmpty(listTitle)) {
											listNode.setProperty("title", listTitle);
										}
										Node eleListNode = listNode.hasNode("element_list_0") ? listNode
												.getNode("element_list_0") : null;
												if (eleListNode != null) {
													boolean multiple = eleListNode.getProperty(
															"listitems").isMultiple();
													if (multiple) {
														eleListNode.setProperty("listitems",
																list.toArray(new String[list.size()]));
													} else {
														eleListNode.setProperty("listitems",
																list.toString());
													}
												} else {
													sb.append(Constants.LIST_ELEMENT_LIST_NODE_NOT_FOUND);
												}
									} else {
										sb.append(Constants.LIST_ELEMENT_NOT_FOUND);
									}

				} catch (Exception e) {
					sb.append(Constants.UNABLE_TO_MIGRATE_TEXT);
					log.error("Exception ", e);
				}

				// end of text and list component
				// -------------------------------------------------------------------------------------------------------
				// start of right list component
				Elements listEle = doc.select("div.n13-pilot");
				if (listEle != null) {
					sb.append(Constants.LIST_NODE_NOT_FOUND);
				}
				// end of right list component
				// -------------------------------------------------------------------------------------------------------
				// start of tile bordered components
				try {
					String tileTitle = "";
					String tileDesc = "";
					String ctaText = "";
					String ctaLink = "";
					Elements tileEle = doc.select("div.gd-right").select(
							"div.c23-pilot,div.cc23-pilot");
					NodeIterator tileIterator = benefitsRightNode.hasNodes() ? benefitsRightNode
							.getNodes("tile*") : null;
							if (tileEle != null) {
								if (tileIterator != null) {
									int nodeCount = (int) tileIterator.getSize();
									int eleCount = tileEle.size();
									if (nodeCount != eleCount) {
										String message = Constants.TILE_BORDERED_ELEMENT_COUNT_MISMATCH;
										message = message.replace("(<ele>)",
												Integer.toString(eleCount));
										message = message.replace("(<node>)",
												Integer.toString(nodeCount));
										sb.append(message);
									}
									for (Element ele : tileEle) {
										Element tileTitleEle = ele.select("h2,h3")
												.first();
										if (tileTitleEle != null) {
											tileTitle = tileTitleEle.text();
										} else {
											sb.append(Constants.TILE_BORDERED_TITLE_NOT_FOUND);
										}
										Element tileDescEle = ele.getElementsByTag("p")
												.first();
										if (tileDescEle != null) {
											tileDesc = tileDescEle.text();
										} else {
											sb.append(Constants.TILE_BORDERED_DESCRIPTION_NOT_FOUND);
										}
										Element tileCta = ele.getElementsByTag("a")
												.first();
										if (tileCta != null) {
											ctaText = tileCta.text();
											ctaLink = tileCta.absUrl("href");
											if (StringUtil.isBlank(ctaLink)) {
												ctaLink = tileCta.attr("href");
											}
										} else {
											sb.append(Constants.TILE_BORDERED_ANCHOR_ELEMENTS_NOT_FOUND);
										}
										// Start extracting valid href
										log.debug("Before ctaLink" + ctaLink + "\n");
										ctaLink = FrameworkUtils.getLocaleReference(
												ctaLink, urlMap);

										ctaLink = ctaLink.replaceAll(" ", "").replaceAll("%20","");
										log.debug("after ctaLink" + ctaLink + "\n");
										// End extracting valid href
										if (tileIterator.hasNext()) {
											Node tileNode = (Node) tileIterator.next();
											if(tileNode.hasProperty("linktrigger")){
												String value = tileNode.getProperty("linktrigger").getString();
												if(value.equals("title")){
													if((int)tileIterator.getSize() != tileEle.size()){
														tileNode = tileIterator.hasNext() ? tileIterator.nextNode():null;
														if(tileNode == null){
															break;
														}
													}
												}
											}
											if (StringUtils.isNotEmpty(tileTitle)) {
												tileNode.setProperty("title", tileTitle);
											}
											if (StringUtils.isNotEmpty(tileDesc)) {
												tileNode.setProperty("description",
														tileDesc);
											}
											if (StringUtils.isNotEmpty(ctaText)) {
												tileNode.setProperty("linktext",
														ctaText);
											}
											if (StringUtils.isNotEmpty(ctaLink)) {
												tileNode.setProperty("linkurl", ctaLink);
											}
										}
									}
								} else {
									sb.append(Constants.TILE_BORDERED_NODES_NOT_FOUND);
								}
							} else {
								sb.append(Constants.TILE_BORDERED_ELEMENTS_NOT_FOUND);
							}
				} catch (Exception e) {
					sb.append(Constants.UNABLE_TO_MIGRATE_TILE_BORDERED_COMPONENTS);
					log.error("Exception ", e);
				}
				// end of tile bordered components

			} else {
				sb.append(Constants.URL_CONNECTION_EXCEPTION);
			}
		} catch (Exception e) {
			log.error("Exception ", e);
			sb.append(Constants.UNABLE_TO_MIGRATE_PAGE);
		}
		sb.append("</ul></td>");
		session.save();
		return sb.toString();
	}
}
