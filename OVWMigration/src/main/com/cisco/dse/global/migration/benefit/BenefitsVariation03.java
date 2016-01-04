package com.cisco.dse.global.migration.benefit;

/* S.No			Name		Date		Description of change
 * 1			Bhavya		28-Dec-15	Added the Java file to handle the migration of benifits variation 3 with 3url.
 * 
 * */

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.sling.commons.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.cisco.dse.global.migration.config.BaseAction;
import com.cisco.dse.global.migration.config.Constants;
import com.cisco.dse.global.migration.config.FrameworkUtils;

public class BenefitsVariation03 extends BaseAction {

	Document doc = null;

	StringBuilder sb = new StringBuilder(1024);

	Logger log = Logger.getLogger(BenefitsVariation03.class);

	public String translate(String host, String loc, String prod, String type,
			String catType, String locale, Session session,  Map<String, String> urlMap) throws IOException,
			ValueFormatException, VersionException, LockException,
			ConstraintViolationException, RepositoryException {
		log.debug("In the translate method BenifitsVariation03");
		log.debug("In the translate method, catType is :" + catType);
		String pagePropertiesPath = "/content/<locale>/" + catType
				+ "/<prod>/benefit/jcr:content";
		String benefitLeft = "/content/<locale>/"
				+ catType
				+ "/<prod>/benefit/jcr:content/content_parsys/benefits/layout-benefits/gd12v2/gd12v2-left";
		String benefitRight = "/content/<locale>/"
				+ catType
				+ "/<prod>/benefit/jcr:content/content_parsys/benefits/layout-benefits/gd12v2/gd12v2-right";

		String pageUrl = host + "/content/<locale>/" + catType
				+ "/<prod>/benefit.html";
		pageUrl = pageUrl.replace("<locale>", locale).replace("<prod>", prod);
		pagePropertiesPath = pagePropertiesPath.replace("<locale>", locale)
				.replace("<prod>", prod);
		sb.append("<td>" + "<a href=" + pageUrl + ">" + pageUrl + "</a>"
				+ "</td>");
		sb.append("<td>" + "<a href=" + loc + ">" + loc + "</a>" + "</td>");
		sb.append("<td><ul>");

		benefitLeft = benefitLeft.replace("<locale>", locale).replace("<prod>",
				prod);
		benefitRight = benefitRight.replace("<locale>", locale).replace(
				"<prod>", prod);

		javax.jcr.Node benefitLeftNode = null;
		javax.jcr.Node benefitRightNode = null;
		javax.jcr.Node pageJcrNode = null;
		try {
			benefitLeftNode = session.getNode(benefitLeft);
			benefitRightNode = session.getNode(benefitRight);
			pageJcrNode = session.getNode(pagePropertiesPath);

			try {
				doc = getConnection(loc);
			} catch (Exception e) {
				log.error("Exception : ", e);
			}

			if (doc != null) {

				// start set page properties.

				FrameworkUtils.setPageProperties(pageJcrNode, doc, session, sb);

				// end set page properties.

				// start of text component
				try {
					migrateTextAndHtmlBlob(doc, benefitLeftNode,locale, urlMap);
				} catch (Exception e) {
					sb.append(Constants.EXCEPTION_TEXT_COMPONENT);
					log.error("Exception : ", e);
				}
				// end of text component

				// Start of List Component
				try {
					migratelistElements(doc, benefitLeftNode, session, prod, locale, urlMap);
				} catch (Exception e) {
					sb.append("Exception in List Component");
					log.error("Exception : ", e);
				}

				// End of List Component

				// Start of Right Rail
				try {
					migrateRightRailContent(doc, benefitRightNode, locale, urlMap);
				} catch (Exception e) {
					sb.append(Constants.UNABLE_TO_MIGRATE_TILE_BORDERED_COMPONENTS);
					log.error("Exception : ", e);
				}
				// End of Right Rail

			} else {
				sb.append(Constants.URL_CONNECTION_EXCEPTION);
			}
		} catch (Exception e) {
			sb.append(Constants.UNABLE_TO_MIGRATE_PAGE);
			log.error("Exception : ", e);
		}
		sb.append("</ul></td>");
		session.save();
		log.debug("Msg returned is " + sb.toString());
		return sb.toString();
	}

	//Start of migration of the Text and Text Description
	private void migrateTextAndHtmlBlob(Document doc, Node benefitLeftNode,String locale, Map<String, String> urlMap) throws PathNotFoundException, RepositoryException {
		Node firstTextNode = benefitLeftNode.hasNode("text") ? benefitLeftNode.getNode("text") : null;
		Node lastTextNode = benefitLeftNode.hasNode("text_0") ? benefitLeftNode.getNode("text_0") : null;
		log.debug("firstTextNode:"+firstTextNode);
		log.debug("lastTextNode:"+lastTextNode);
		Elements textElements = doc.select("div.c00-pilot");
		if (textElements.size() != 1) {
			migrateTextComponents(textElements.first(),firstTextNode, locale, urlMap);
			migrateTextDescriptionComponents(textElements.last(),lastTextNode, locale,urlMap);
		} else if (textElements.size() == 1) {
			Element titleEle = textElements.first();
			Element titleEl = titleEle.getElementsByTag("h1").first();
			if (titleEl == null) {
				titleEl = titleEle.getElementsByTag("h2").first();
			}
			migrateTextComponents(titleEl, lastTextNode, locale, urlMap);
			if(titleEl != null){
				titleEl.remove();
			}
			migrateTextDescriptionComponents(titleEle, firstTextNode,locale, urlMap);
		}
	}
	//End of migration of the Text and Text Description

	// Start of Text Content Migration
	private void migrateTextComponents(Element textElements, Node textNode,String locale, Map<String, String> urlMap) throws PathNotFoundException, RepositoryException {
		if (textNode != null) {
			log.debug("Text node path:"+textNode.getPath());
			if (textElements != null) {
				String html = FrameworkUtils.extractHtmlBlobContent(textElements, "",locale, sb, urlMap);
				textNode.setProperty("text", html);

			} else {
				sb.append(Constants.TEXT_DOES_NOT_EXIST);
			}
		} else {
			sb.append(Constants.TEXT_NODE_NOT_FOUND);
		}
	}

	// end of Text Content Migration

	// Start of text description Content Migration
	private void migrateTextDescriptionComponents(Element textElements, Node textNode,
			String locale , Map<String, String> urlMap) throws PathNotFoundException, RepositoryException {

		if (textNode != null) {
			log.debug("Text node path:"+textNode.getPath());
			if (textElements != null) {
				textNode.setProperty("text", FrameworkUtils.extractHtmlBlobContent(textElements, "", locale, sb , urlMap));
			} else {
				sb.append(Constants.TEXT_DESCRIPTION_NOT_FOUND);
			}

		} else {
			sb.append(Constants.TEXT_NODE_NOT_FOUND);
		}
	}

	// End of text description Content Migration

	// Start of Migrate List Elements method
	private void migratelistElements(Document doc, Node architectureLeftNode,Session session, String prod, String locale, Map<String, String> urlMap) throws RepositoryException {
		Elements listElements = doc.select("div.n13-pilot");

		if(listElements.isEmpty()){
			listElements = doc.select("div.nn13-pilot");
		}

		int eleSize = listElements.size();
		NodeIterator listNodeIterator = architectureLeftNode.hasNodes() ? architectureLeftNode.getNodes("list*") : null;
		if (listNodeIterator != null) {
			int nodeSize = (int) listNodeIterator.getSize();
			log.debug("node Size" + nodeSize + "ele Size" + eleSize);
			if (eleSize == nodeSize) {
				Node listNode = null;
				for (Element ele : listElements) {
					listNode = (Node) listNodeIterator.next();
					setListElements(ele, listNode, session,prod, locale, urlMap);
				}
			} else if (nodeSize < eleSize) {
				Node listNode;
				for (Element ele : listElements) {
					if (listNodeIterator.hasNext()) {
						listNode = (Node) listNodeIterator.next();
						setListElements(ele, listNode, session,prod, locale, urlMap);
					}
				}
				sb.append(Constants.MISMATCH_IN_LIST_NODES + eleSize
						+ Constants.LIST_NODES_COUNT + nodeSize);
			}

			else if (nodeSize > eleSize) {
				Node listNode;
				for (Element ele : listElements) {
					listNode = (Node) listNodeIterator.next();
					setListElements(ele, listNode, session,prod, locale, urlMap);
				}
				sb.append(Constants.MISMATCH_IN_LIST_NODES + eleSize
						+ Constants.LIST_NODES_COUNT + nodeSize);
			}
		} else {
			sb.append(Constants.LIST_NODE_NOT_FOUND);
		}

	}

	private void setListElements(Element ele, Node architectureListNode,Session session,String prod, String locale, Map<String, String> urlMap) {
		try {
			String ownPdfText = "";
			String pdfIcon = "";
			String pdfSize = "";
			Elements h2Ele = ele.getElementsByTag("h2");
			Elements h3Ele = ele.getElementsByTag("h3");
			Elements ulEle = ele.getElementsByTag("ul");
			int ulSize=0;
			// start of handling title and subtitle of list component
			if (h2Ele.size() == 3) {
				setListTitles(h2Ele, architectureListNode,prod, locale, urlMap);
			} else if (h3Ele.size() == 3) {
				setListTitles(h3Ele, architectureListNode,prod, locale, urlMap);
			} else {
				Elements titleElements = ele.select("h2,h3");
				setListTitles(titleElements, architectureListNode,prod, locale, urlMap);
			}
			// end of handling title and subtitle of list component

			// Flag to ignore the first Element List
			Elements titles = ele.select("h2,h3");
			boolean flagList = false;
			if(!titles.isEmpty() && !ulEle.isEmpty()){
				int titleSize = titles.size();
				ulSize = ulEle.size();
				if(titleSize == ulSize){
					flagList = true;
				}else if(titleSize < ulSize){
					flagList = true;
				}
			}else {
				flagList = false;
			}

			//Element List
			NodeIterator ulNodeIterator = architectureListNode.hasNode("element_list_0") ? architectureListNode.getNodes("element_list*") : null;
			if (ulNodeIterator != null) {
				Node ulnodeList;
				for (Element ulItr : ulEle) {
					if (ulNodeIterator.hasNext()) {
						ulnodeList = (Node) ulNodeIterator.next();
						log.debug("Flag of List Value" + flagList);
						if(!flagList){
							if (ulNodeIterator.hasNext()) {
								ulnodeList = (Node) ulNodeIterator.next();
								flagList = false;
								int nodeSize = (int) ulNodeIterator.getSize();
								sb.append(Constants.MISMATCH_IN_LIST_NODES+ ulSize+Constants.LIST_NODES_COUNT +nodeSize);
							}
						}
						Elements list = ulItr.getElementsByTag("li");
						List<String> listAdd = new ArrayList<String>();
						for (Element li : list) {
							pdfIcon = "";
							pdfSize = "";
							boolean openNewWindow = false;
							// pdf content
							try {
								ownPdfText = li.ownText();
								if (StringUtils.isNotEmpty(ownPdfText)) {
									log.debug("OWn text is:" + ownPdfText);
									if (ownPdfText.toLowerCase()
											.contains("pdf")
											|| ownPdfText.toLowerCase()
											.contains("video")) {
										pdfIcon = "pdf";
										if (ownPdfText.toLowerCase().contains(
												"video")) {
											pdfIcon = "video";
										}
										int i = 0;
										for (; i < ownPdfText.length(); i++) {
											char character = ownPdfText
													.charAt(i);
											boolean isDigit = Character
													.isDigit(character);
											if (isDigit) {
												break;
											}
										}
										pdfSize = ownPdfText.substring(i,
												ownPdfText.length() - 1);
										pdfSize = pdfSize.replace(")", "");
										pdfSize = pdfSize.trim();
									}
								}
							} catch (Exception e) {
								sb.append(Constants.Exception_BY_SPECIAL_CHARACTER);
								log.error("Exception : ", e);
							}
							if(!li.getElementsByTag("a").isEmpty()){
								Element a = li.getElementsByTag("a").first();
								// Start extracting valid href
								log.debug("Before anchorHref" + a.absUrl("href") + "\n");
								String anchorHref = FrameworkUtils.getLocaleReference(a.absUrl("href"), urlMap);
								log.debug("after anchorHref" + anchorHref + "\n");
								// End extracting valid href
								JSONObject obj = new JSONObject();
								obj.put("linktext", a.text());
								obj.put("linkurl", anchorHref);
								obj.put("icon", pdfIcon);
								obj.put("size", pdfSize);
								obj.put("description", "");
								obj.put("openInNewWindow", openNewWindow);
								listAdd.add(obj.toString());
							}
						}
						Property listitems = ulnodeList
								.getProperty("listitems");
						if (!listitems.isMultiple()) {
							listitems.remove();
							session.save();
						}
						ulnodeList.setProperty("listitems",
								listAdd.toArray(new String[listAdd.size()]));
					} else {
						sb.append(Constants.MISMATCH_IN_LIST_COUNT);
					}
				}
				if (ulNodeIterator.hasNext()) {
					sb.append(Constants.MISMATCH_IN_LIST_COUNT);
				}
			} else {
				sb.append(Constants.NO_LIST_NODES_FOUND);
			}
			// End of Element List
		} catch (Exception e) {
			sb.append(Constants.UNABLE_TO_MIGRATE_LIST_COMPONENT);
			log.error("Exception : ", e);
		}

	}

	// End of Migrate List Elements Method

	// start of list title setting
	private void setListTitles(Elements titleElements,Node architectureListNode, String prod, String locale, Map<String, String> urlMap) {
		try {
			int count = 1;
			Node subtitleLastNodeIterator = architectureListNode.hasNode("element_subtitle_0") ? architectureListNode.getNode("element_subtitle_0") : null;
			Node subtitleFirstNodeIterator = architectureListNode.hasNode("element_subtitle_1") ? architectureListNode.getNode("element_subtitle_1") : null;
			for (Element ele : titleElements) {
				String textEleHtml = null;
				if (count == 1) {
					textEleHtml = FrameworkUtils.extractHtmlBlobContent(ele, "",locale, sb,urlMap);
					architectureListNode.setProperty("title", textEleHtml);
				} else if (count == 2) {
					if (("unified-communications").equals(prod)) {
						textEleHtml = FrameworkUtils.extractHtmlBlobContent(ele, "",locale, sb,urlMap);
						subtitleFirstNodeIterator.setProperty("subtitle",
								textEleHtml);
					} else {
						textEleHtml = FrameworkUtils.extractHtmlBlobContent(ele, "",locale, sb,urlMap);
						subtitleLastNodeIterator.setProperty("subtitle",
								textEleHtml);
					}
				} else if (count == 3) {
					if (("unified-communications").equals(prod)) {
						textEleHtml = FrameworkUtils.extractHtmlBlobContent(ele, "",locale, sb,urlMap);
						subtitleLastNodeIterator.setProperty("subtitle",
								textEleHtml);
					} else {
						textEleHtml = FrameworkUtils.extractHtmlBlobContent(ele, "",locale, sb,urlMap);
						subtitleFirstNodeIterator.setProperty("subtitle",
								textEleHtml);
					}
				}
				count++;
			}
		} catch (Exception e) {
			log.error("Exception:", e);
		}

	}

	// end of list title setting

	// Start of Right rail migration
	private void migrateRightRailContent(Document doc,
			Node architectureRightNode, String locale, Map<String, String> urlMap) {
		try {
			boolean migrate = true;
			Elements rightRailList = doc.select("div.gd-right").select(
					"div.c23-pilot");

			if (!rightRailList.isEmpty() && rightRailList != null) {
				int eleSize = rightRailList.size();
				if (eleSize == 1) {
					Element rightListElem = rightRailList.first();
					if (rightListElem != null) {
						Elements ulElements = rightListElem
								.getElementsByTag("ul");
						if (ulElements.size() > 1) {
							sb.append(Constants.UNABLE_TO_MIGRATE_RIGHTRAIL);
							migrate = false;
						}
					}
				}
			}

			if (migrate) {
				if (rightRailList.isEmpty()) {
					log.debug("No right rail elements found with div class name.");
					sb.append(Constants.LIST_NOT_FOUND_IN_RIGHT_RAIL);
				} else {
					int eleSize = rightRailList.size();
					NodeIterator tileIterator = architectureRightNode
							.hasNodes() ? architectureRightNode
									.getNodes("tile_bordered*") : null;
									if (tileIterator != null) {
										int nodeSize = (int) tileIterator.getSize();
										Node listNode = null;
										if (eleSize == nodeSize) {
											for (Element rightListEle : rightRailList) {
												if (tileIterator.hasNext()) {
													listNode = (Node) tileIterator.next();
													setRightRailContent(listNode, rightListEle,locale, urlMap);
												} else {
													log.debug("Next node not found");
												}

											}
										} else if (eleSize > nodeSize) {
											for (Element rightListEle : rightRailList) {
												if (tileIterator.hasNext()) {
													listNode = (Node) tileIterator.next();
													setRightRailContent(listNode, rightListEle, locale, urlMap);
												} else {
													log.debug("Next node not found");
													sb.append(Constants.RIGHT_RAIL_NODE_COUNT
															+ nodeSize
															+ Constants.RIGHT_RAIL_ELEMENT_COUNT
															+ eleSize + "</li>");
													break;
												}

											}
										} else if (eleSize < nodeSize) {
											for (Element rightListEle : rightRailList) {
												if (tileIterator.hasNext()) {
													listNode = (Node) tileIterator.next();
													setRightRailContent(listNode, rightListEle, locale, urlMap );
												} else {
													log.debug("Next node not found");
												}
											}
											sb.append(Constants.RIGHT_RAIL_NODE_COUNT
													+ nodeSize
													+ Constants.RIGHT_RAIL_ELEMENT_COUNT
													+ eleSize + "</li>");
										}
									}
				}
			}

		} catch (Exception e) {
			sb.append(Constants.UNABLE_TO_MIGRATE_RIGHT_GRID);
		}
	}

	// End of right rail migration

	// Start of setting Right rail Content
	public void setRightRailContent(Node listNode, Element rightListEle, String locale, Map<String, String> urlMap) {
		try {
			Element title;
			Element description;
			Elements anchor = rightListEle.getElementsByTag("a");
			Elements headElements = rightListEle.getElementsByTag("h2");

			if (headElements.isEmpty()) {
				headElements = rightListEle.getElementsByTag("h3");
			}

			if (headElements.size() > 1) {
				title = headElements.last();
				description = rightListEle.getElementsByTag("p").last();
			} else {
				title = headElements.first();
				description = rightListEle.getElementsByTag("p").first();
			}
			String descriptionHtml = FrameworkUtils.extractHtmlBlobContent(description, "",locale, sb,urlMap);
			listNode.setProperty("title", title.text());
			listNode.setProperty("description", descriptionHtml);

			if(!anchor.isEmpty()){
				Element listtext = anchor.first();
				Element listurl = anchor.first();
				
				// Start extracting valid href
				log.debug("Before anchorHref" + listurl.absUrl("href") + "\n");
				String anchorHref = FrameworkUtils.getLocaleReference(listurl.absUrl("href"), urlMap);
				log.debug("after anchorHref" + anchorHref + "\n");
				// End extracting valid href
				listNode.setProperty("linktext", listtext.text());
				listNode.setProperty("linkurl", anchorHref);
			}
			log.debug("Updated title, descriptoin and linktext at "
					+ listNode.getPath());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// End of setting Right rail Content


}