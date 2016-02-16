/* 
 * S.No		Name		Date			Description of change
 * 1		Anudeep		11-Feb-16		Added the Java file to handle the migration of midsize/find-solution Web url variation 4 page.
 * 
 * */

package com.cisco.dse.global.migration.web;

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

import org.apache.log4j.Logger;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.cisco.dse.global.migration.config.BaseAction;
import com.cisco.dse.global.migration.config.Constants;
import com.cisco.dse.global.migration.config.FrameworkUtils;

public class WebVariation16 extends BaseAction {

	Document doc;

	StringBuilder sb = new StringBuilder(1024);

	static Logger log = Logger.getLogger(WebVariation16.class);

	public String translate(String host, String loc, String prod, String type, String catType, String locale, Session session, Map<String, String> urlMap) throws IOException,
	ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {

		log.debug("In the translate method of WebVariation16");
		log.debug("In the translate method, catType is :" + catType);

		// Repo node paths
		String pagePropertiesPath = "/content/<locale>/buy/jcr:content/";
		String pageUrl = host + "/content/<locale>/buy.html";

		pagePropertiesPath = pagePropertiesPath.replace("<locale>", locale).replace("<prod>", prod);
		pageUrl = pageUrl.replace("<locale>", locale).replace("<prod>", prod);

		sb.append("<td>" + "<a href=" + pageUrl + ">" + pageUrl + "</a>" + "</td>");
		sb.append("<td>" + "<a href=" + loc + ">" + loc + "</a>" + "</td>");
		sb.append("<td><ul>");

		String parentNodePath = pagePropertiesPath+"Grid/how_to_buy/layout-how-to-buy";
		parentNodePath = parentNodePath.replace("<locale>", locale).replace("<prod>", prod);

		javax.jcr.Node parentNode = null;
		javax.jcr.Node pageJcrNode = null;

		try {
			parentNode = session.getNode(parentNodePath);
			pageJcrNode = session.getNode(pagePropertiesPath);

			//start getting nodes

			Node widenarrowNode = parentNode.hasNodes()?parentNode.getNode("widenarrow"):null;

			//end getting nodes

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

				//Page migration starts

				//start migrating left part
				try{
					log.debug("start migrating left part");

					Node halvesNode = null;
					log.debug("wide narrow node is : "+widenarrowNode.getName());
					Node wnWideNode = widenarrowNode.hasNode("WN-Wide-1")?widenarrowNode.getNode("WN-Wide-1"):null;
					if(wnWideNode!=null){
						halvesNode = wnWideNode.hasNode("halves")?wnWideNode.getNode("halves"):null;
					}
					log.debug("start migrating left top part");

					Element bizEle = doc.select("div.buy-biz").first();
					Element leftLowLeft = doc.select("div.low-l").first();

					Element cisEle = doc.select("div.buy-cisco").first();
					Element contEle = null;
					if(cisEle!=null){
						contEle = cisEle.select("div.s01-pilot").first();
					}
					Elements c00 = doc.select("div.gd-right").select("div.c00-pilot");
					if(c00!=null&&!c00.isEmpty()){
						sb.append("<li>Extra list component(s) found in right rail.</li>");
					}
					if(contEle!=null){
						sb.append("<li>Extra Contact Us element found in left rail.</li>");
					}
					Element leftLowRight = doc.select("div.low-r").first();
					if(bizEle!=null){
						Elements h3Ele = bizEle.getElementsByTag("h3");
						if(h3Ele!=null){
							for(Element e:h3Ele){
								Element a = e.getElementsByTag("a").first();
								if(a!=null){
									e.remove();
									sb.append("<li>Subtitle with link found extra in left rail.</li>");
								}
							}
							int h3Size = bizEle.getElementsByTag("h3").size();
							if(h3Size!=2){
								sb.append("<li>Mismatch of subtitles in left rail.</li>");
							}
						}

						Elements aEle = bizEle.getElementsByTag("a");
						if(aEle!=null){
							int aSize = aEle.size();
							if(aSize!=4){
								sb.append("<li>Mismatch of links in left rail.</li>");
							}
						}
					}else{
						sb.append("<li>Left part of left rail element not available.</li>");
					}
					if(cisEle!=null){
						Elements h3El = cisEle.getElementsByTag("h3");
						if(h3El!=null){
							int h3Size = h3El.size();
							if(h3Size!=2){
								sb.append("<li>Mismatch of subtitles in left rail.</li>");
							}
						}

						Elements aEl = cisEle.getElementsByTag("a");
						if(aEl!=null){
							int aSize = aEl.size();
							if(aSize!=6){
								sb.append("<li>Mismatch in links in left rail.</li>");
							}
						}
					}else{
						sb.append("<li>Right part of left rail not available.</li>");
					}
					if(halvesNode!=null){
						Node halfLeft = halvesNode.hasNode("H-Half-1")?halvesNode.getNode("H-Half-1"):null;
						if(bizEle!=null){
							setMain(halfLeft, bizEle, leftLowLeft,urlMap,locale);
						}
						Node halfLeftTwo = halvesNode.hasNode("H-Half-2")?halvesNode.getNode("H-Half-2"):null;
						if(cisEle!=null){
							setMain(halfLeftTwo, cisEle, leftLowRight,urlMap,locale);
						}
					}

					log.debug("end migrating left top part");

					log.debug("end migrating left part");

				}catch(Exception e){
					log.error("Exception in left rail : ",e);
				}
				//end migrating left part

				//start migration right part

				try{
					log.debug("start migrating right rail");
					Node wnNarrow = widenarrowNode.hasNode("WN-Narrow-2")?widenarrowNode.getNode("WN-Narrow-2"):null; 
					if(wnNarrow!=null){
						Node luHelpNode = wnNarrow.hasNode("letushelp_eot")?wnNarrow.getNode("letushelp_eot"):null;
						if(luHelpNode!=null){
							Element letUsHelp = doc.select("div.f-holder").first();
							if(letUsHelp!=null){
								Element h3Ele = letUsHelp.getElementsByTag("h3").first();
								String title=null;
								if(h3Ele!=null){
									title = h3Ele.text();
								}
								if(!StringUtil.isBlank(title)){
									luHelpNode.setProperty("title",title);
								}
							}else{
								sb.append("<li>no letus help found in web page.</li>");
							}
						}
					}
					Node listContainer = wnNarrow.hasNode("list_container")?wnNarrow.getNode("list_container"):null;
					NodeIterator listContentItr = null;
					if(listContainer!=null){
						Node listItemPsys = listContainer.hasNode("list_item_parsys")?listContainer.getNode("list_item_parsys"):null;
						if(listItemPsys!=null){
							listContentItr = listItemPsys.hasNodes()?listItemPsys.getNodes("list_content*"):null;
						}
					}
					Element listEle = doc.select("div.n13-pilot").first();
					if(listEle!=null && listContentItr!=null){
						Element titleEle = listEle.getElementsByTag("h2").first();
						String title = null;
						if(titleEle!=null){
							title = titleEle.text();
						}
						if(!StringUtil.isBlank(title)){
							listContainer.setProperty("title",title);
						}
						setLeftTopTwo(listEle,listContentItr,urlMap,locale);
					}
					if(listEle==null){
						sb.append("<li>List element not found in right rail.</li>");
					}
					log.debug("end migrating right rail");
				}catch(Exception e){
					log.error("Exception in right rail : ",e);
				}

				//end migration right part

				//Page migration end
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

	public void setLeftTopOne(Element bizEle,Node listContainer,NodeIterator listContentItr,Map<String,String> urlMap,String locale) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException{
		Element h2Ele = bizEle.getElementsByTag("h2").first();
		Element pEle = bizEle.getElementsByTag("p").first();
		String title = null;
		String intropara = null;
		if(h2Ele!=null){
			title = h2Ele.text();
			log.debug("Title is : "+title);
		}else{
			sb.append("<li>No title in left part of page.</li>");
		}
		if(pEle!=null){
			intropara = pEle.text();
			log.debug("intropara is : "+intropara);
		}else{
			sb.append("<li>No description in left part of page.</li>");
		}
		Node item = null;

		if(listContainer!=null){
			if(!StringUtil.isBlank(title)){
				listContainer.setProperty("title",title);
			}
			if(!StringUtil.isBlank(intropara)){
				listContainer.setProperty("intropara",intropara);
			}
			h2Ele.remove();
			pEle.remove();

		}
		Element comp = bizEle.select("p.compact").first();
		String linktext=null;
		String url = null;
		Elements aEle = null;
		if(comp!=null){
			aEle = comp.getElementsByTag("a");
		}
		if(aEle!=null){
			Node listContent = null;
			if(listContentItr.hasNext()){
				listContent = listContentItr.nextNode();
				Node listItems = listContent.hasNode("listitems")?listContent.getNode("listitems"):null;
				if(listItems!=null){
					item = listItems.hasNode("item_1")?listItems.getNode("item_1"):null;
					if(item!=null){
						Node linkData = item.hasNode("linkdata")?item.getNode("linkdata"):null;
						if(linkData!=null){
							Element a = aEle.first();
							linktext = a.text();
							url = a.absUrl("href");
							if(StringUtil.isBlank(url)){
								url = a.attr("href");
							}
							FrameworkUtils.getLocaleReference(url, urlMap, locale, sb);
							if(!StringUtil.isBlank(linktext)){
								linkData.setProperty("linktext",linktext);
							}
							if(!StringUtil.isBlank(url)){
								linkData.setProperty("url",url);
							}
							comp.remove();
							/*else{
								Element a = aEle.first();
								linktext = a.text();
								url = a.absUrl("href");
								if(StringUtil.isBlank(url)){
									url = a.attr("href");
								}
								FrameworkUtils.getLocaleReference(url, urlMap, locale, sb);
								linkData.setProperty("linktext",linktext);
								linkData.setProperty("url",url);
								comp.remove();
//								sb.append("<li>Mis-match of links in list element in left rail.</li>");
							}*/
						}
					}
				}
			}
		}
	}

	public void setLeftTopTwo(Element bizEle,NodeIterator listContentItr,Map<String,String> urlMap,String locale) throws PathNotFoundException, RepositoryException{
		Node listItems = null;
		Node listContent = null;
		if(bizEle.hasClass("n13-pilot") && bizEle.getElementsByTag("h3").size()<2){
			log.debug("call set list");
			sb.append("<li>No subtitle found for list in right rail.</li>");
			setList(bizEle,listContentItr,urlMap,locale);
		}else{
			while(listContentItr.hasNext()){
				listContent = listContentItr.nextNode();
				Element h3Ele = bizEle.getElementsByTag("h3").first();
				String subtitle = null;
				if(h3Ele!=null){
					subtitle = h3Ele.text();
					h3Ele.remove();
				}
				if(!StringUtil.isBlank(subtitle)){
					listContent.setProperty("subtitle",subtitle);
				}
				listItems = listContent.hasNode("listitems")?listContent.getNode("listitems"):null;
				NodeIterator itemsItr = null;
				if(listItems!=null){
					itemsItr = listItems.hasNodes()?listItems.getNodes("item*"):null;
				}
				Node item=null;
				while(itemsItr.hasNext()){
					item = itemsItr.nextNode();
					Node linkdata = item.hasNode("linkdata")?item.getNode("linkdata"):null;
					String linkText = null;
					String lUrl = null;
					Element aElet = null;
					if(linkdata!=null){
						aElet = bizEle.getElementsByTag("a").first();
						if(aElet!=null){
							linkText = aElet.text();
							lUrl = aElet.absUrl("href");
							if(StringUtil.isBlank(lUrl)){
								lUrl = aElet.attr("href");
							}
							aElet.remove();
						}
					}
					FrameworkUtils.getLocaleReference(lUrl, urlMap, locale, sb);
					if(!StringUtil.isBlank(linkText)){
						linkdata.setProperty("linktext",linkText);
					}
					if(!StringUtil.isBlank(lUrl)){
						linkdata.setProperty("url",lUrl);
					}
				}
			}
		}
	}
	public void setTile(Element leftLowLeft,Node tileNode,Map<String,String> urlMap,String locale) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException{
		Element titleEle = leftLowLeft.getElementsByTag("h2").first();
		Element descEle = leftLowLeft.getElementsByTag("p").first();
		Element anEle = leftLowLeft.getElementsByTag("a").first();
		String title = titleEle.text();
		String description = descEle.text();
		String linkText = anEle.text();
		String url = anEle.absUrl("href");
		if(StringUtil.isBlank(url)){
			url = anEle.attr("href");
		}
		FrameworkUtils.getLocaleReference(url, urlMap, locale, sb);
		if(!StringUtil.isBlank(title)){
			tileNode.setProperty("title",title);
		}
		if(!StringUtil.isBlank(description)){
			tileNode.setProperty("description",description);
		}
		Node ctaNode = tileNode.hasNode("cta")?tileNode.getNode("cta"):null;
		if(ctaNode!=null){
			if(!StringUtil.isBlank(linkText)){
				ctaNode.setProperty("linktext",linkText);
			}
			String linkType = ctaNode.getProperty("linktype").getString();
			if(linkType.equalsIgnoreCase("Url")){
				if(!StringUtil.isBlank(url)){
					ctaNode.setProperty("url",url);
				}
			}else{
				sb.append("<li>linktype property set to 'lightbox' instead of 'Url' for  </li>"+ title);
			}
		}
	}
	public void setMain(Node halfLeft,Element bizEle,Element leftLowLeft,Map<String,String> urlMap,String locale) throws PathNotFoundException, RepositoryException{
		Node listContainer = null;
		if(halfLeft!=null){
			listContainer = halfLeft.hasNode("list_container")?halfLeft.getNode("list_container"):null;
		}
		NodeIterator listContentItr = null;
		Node listItemPsys = listContainer.hasNode("list_item_parsys")?listContainer.getNode("list_item_parsys"):null;
		Element aButton = bizEle.select("a.a00v1").first();
		if(aButton!=null){
			Node bottonNode = listItemPsys.hasNode("button")?listItemPsys.getNode("button"):null;
			if(bottonNode!=null){
				Node ctanode = bottonNode.hasNode("cta")?bottonNode.getNode("cta"):null;
				if(ctanode!=null){
					String linktext = aButton.text();
					String url = aButton.absUrl("href");
					if(StringUtil.isBlank(url)){
						url = aButton.attr("href");
					}
					FrameworkUtils.getLocaleReference(url, urlMap, locale, sb);
					if(!StringUtil.isBlank(linktext)){
						ctanode.setProperty("linktext",linktext);
					}
					if(!StringUtil.isBlank(url)){
						ctanode.setProperty("url",url);
					}
					aButton.remove();
				}
			}
		}
		listContentItr = listItemPsys.hasNodes()?listItemPsys.getNodes("list_content*"):null;

		setLeftTopOne(bizEle,listContainer,listContentItr,urlMap,locale);

		setLeftTopTwo(bizEle,listContentItr,urlMap,locale);

		Node tileNode = halfLeft.hasNode("tile")?halfLeft.getNode("tile"):null;
		if(tileNode!=null){
			setTile(leftLowLeft,tileNode,urlMap,locale);
		}
	}
	public void setList(Element bizEle,NodeIterator listContentItr,Map<String,String> urlMap,String locale) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException{
		int count = 0;
		while(listContentItr.hasNext()){
			log.debug("listContentItr hasNext");
			Node listContent = listContentItr.nextNode();
			log.debug("node name : "+listContent.getName());
			log.debug("count const : "+count);
			if(count!=0){
				Element h3Ele = bizEle.getElementsByTag("h3").first();
				String subtitle = null;
				if(h3Ele!=null){
					subtitle = h3Ele.text();
					log.debug("h3 : "+subtitle);
				}
				log.debug("count const : "+count);
				if(!StringUtil.isBlank(subtitle)){
					listContent.setProperty("subtitle",subtitle);
				}
			}
			Node listItems = listContent.hasNode("listitems")?listContent.getNode("listitems"):null;
			log.debug(listItems);
			NodeIterator itemsItr = listItems.hasNodes()?listItems.getNodes("item*"):null;
			Element ul = bizEle.select("ul.no-bullets").first();
			int aSize = ul.getElementsByTag("a").size();
			int itemSize = (int)itemsItr.getSize();
			log.debug("aSize : "+aSize+" -- itemSize : "+itemSize);
			if(aSize!=itemSize){
				sb.append("<li>Mismatch of links in list component on right rail.</li>");
			}
			while(itemsItr.hasNext()){
				log.debug("item hasNext");
				Node item = itemsItr.nextNode();
				Node linkData = item.hasNode("linkdata")?item.getNode("linkdata"):null;
				Element aEle = ul.getElementsByTag("a").first();
				String linkText =null;
				String url = null;
				if(aEle!=null){
					linkText = aEle.text();
					log.debug("atext :"+linkText);
					url = aEle.absUrl("href");
					log.debug("url :"+url);
				}
				if(StringUtil.isBlank(url)){
					url = aEle.attr("href");
				}
				url = FrameworkUtils.getLocaleReference(url, urlMap, locale, sb);
				if(linkData!=null){
					log.debug("linkData!=null");
					if(!StringUtil.isBlank(linkText)){
						log.debug("link text not blank");
						linkData.setProperty("linktext",linkText);
					}
					if(!StringUtil.isBlank(url)){
						log.debug("link url not blank");
						linkData.setProperty("url",url);
					}
				}
				aEle.remove();
			}
			ul.remove();
			count++;
		}
	}
}