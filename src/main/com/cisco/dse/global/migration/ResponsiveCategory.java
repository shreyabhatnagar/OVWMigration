package com.cisco.dse.global.migration;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import javax.jcr.*;

import org.apache.jackrabbit.commons.JcrUtils;

import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.version.VersionException;

public class ResponsiveCategory {

	Document doc;
	String title = null;
	Element fProduct = null;
	Elements fProdTitle = null;
	String fProductsTitle = null;

	Elements fFooterLinks = null;

	Elements fSeries = null;

	Elements fProdSeries = null;

	Element fSubSeries = null;

	StringBuilder sb = new StringBuilder(1024);

	javax.jcr.Node hNode = null;
	javax.jcr.Node drawer = null;
	javax.jcr.Node subdrawer = null;
	javax.jcr.Node subdrawerpanel = null;

	String footerLinks = "{\"linktext\":\"<aaa>\",\"linkurl\":\"<bbb>\"}";

	// Repo node paths

	String fProds = "/content/<locale>/products/<prod>/index/jcr:content/Grid/category/layout-category/widenarrow_0/WN-Wide-1";
	String fHero = "/content/<locale>/products/<prod>/index/jcr:content/Grid/category/layout-category/widenarrow/WN-Wide-1/carousel/carouselContents";
	String pCta = "/content/<locale>/products/<prod>/index/jcr:content/Grid/category/layout-category/widenarrow/WN-Narrow-2/hero_tile";
	String rRail = "/content/<locale>/products/<prod>/index/jcr:content/Grid/category/layout-category/widenarrow_0/WN-Narrow-2";

	public String translate(String loc, String prod, String type,
			String locale, Session session) throws IOException,
			ValueFormatException, VersionException, LockException,
			ConstraintViolationException, RepositoryException {

		fProds = fProds.replace("<locale>", locale).replace("<prod>", prod);
		fHero = fHero.replace("<locale>", locale).replace("<prod>", prod);
		pCta = pCta.replace("<locale>", locale).replace("<prod>", prod);
		rRail = rRail.replace("<locale>", locale).replace("<prod>", prod);
		System.out
				.println(":::::::::::::::::::::::::::::::::::::::::::Featured Prod:::::::::"
						+ fProds);

		javax.jcr.Node pNode = null;
		try {

			pNode = session.getNode(fProds);
			hNode = session.getNode(fHero);

			System.out.println("Panels Length:::::::::"
					+ pNode.getNodes("container*").getSize());

			try {

				doc = Jsoup.connect(loc).get();
			} catch (Exception e) {
				sb.append("Cannot Connect to given URL. \n");
			}

			title = doc.title();

			// featured products title
			try {
				fProduct = doc.select("h2.compact").first();

				if (fProduct == null) {
					System.out.println("NULLLLLLLLLLLL");
				} else {
					System.out.println("Title:::::::::" + fProduct.text());
					fProductsTitle = fProduct.text();
					pNode.getNode("header")
							.setProperty("title", fProductsTitle);

				}
			} catch (Exception e) {
				sb.append("Unable to update Headers of Drawers.\n");
			}

			// Featured Products header links

			try {
				fFooterLinks = doc.select("p.footer");

				for (Element ele : fFooterLinks) {

					Elements titles = ele.getElementsByTag("a");
					// System.out.println("CCCC : " + ele.text());
					if (pNode.getNode("header").getNodes("item*").getSize() != titles
							.size())
						sb.append("Mis-match in header links.\n");
					int s = 1;
					for (Element tt : titles) {
						if (pNode.getNode("header").getNode("item_" + s) != null) {
							System.out.println("Text :" + tt.text());
							System.out.println("HREF :" + tt.attr("href"));

							pNode.getNode("header").getNode("item_" + s)
									.getNode("link")
									.setProperty("linktext", tt.text());
							pNode.getNode("header").getNode("item_" + s)
									.getNode("link")
									.setProperty("url", tt.attr("href"));
							s++;
						}
					}

				}

			} catch (Exception e) {
				sb.append("Unable to update Header Links of Drawers.\n");
			}

			// featured sub-categories

			try {
				//fProdSeries = doc.select("div.drawerspanel");

				fSeries = doc.select("div.series");

				int i = 0;

				// Value[] panels = pNode.getProperty("panelNodes").getValues();

				if (fSeries.size() != pNode.getNodes("container*").getSize())
					sb.append("Mismatch in drawer panels.\n");

				NodeIterator it = pNode.getNodes("container*");
				System.out.println(":::::No OF Drawers::::::::::"+it.getSize());
				for (Element ele : fSeries) {

					Element sDesc = ele.getElementsByTag("p").first();
					System.out.println("Desc : " + sDesc.text());

					Element stitle = ele.getElementsByTag("h3").first();
					Element aTitle = stitle.getElementsByTag("a").first();

					if (aTitle == null) {
						System.out.println("Series Title ::::::::::::"
								+ stitle.text());
					} else {
						System.out.println("Series Title ::::::::::::"
								+ aTitle.text());
						System.out.println("Series Title Anchor ::::::::::::"
								+ aTitle.attr("href"));
					}

					if (it.hasNext())
						drawer = it.nextNode();
					if (drawer != null) {
						if (aTitle == null) {
							drawer.setProperty("title", stitle.text());
							drawer.setProperty("description", sDesc.text());
						} else {
							drawer.setProperty("title", aTitle.text());
							drawer.setProperty("description", sDesc.text());
							if (drawer.getNode("overview") != null) {
								drawer.getNode("overview").setProperty(
										"linktext", aTitle.text());
								drawer.getNode("overview").setProperty("url",
										aTitle.attr("href"));
							}
						}
					}
					// featured series

					//fSubSeries = fProdSeries.select("div.parsys-drawers").get(i++);
					Elements SeriesColl = doc.select("ul.items");

					// subdrawer =
					// drawer.getNode("subdrawer_parsys").getNodes("content_product*");

					NodeIterator itr1 = drawer.getNode("subdrawer_parsys")
							.getNodes("content_product*");
					System.out.println(":::::No OF Drawer Panels::::::::::"+itr1.getSize());
					if (SeriesColl.size() != itr1.getSize())
						sb.append("Mis-Match on Series Panels.\n");
					for (Element ss : SeriesColl) {
						if (itr1.hasNext()) {
							subdrawerpanel = itr1.nextNode();
						}
						Elements subItems = ss.select("div.prodinfo");
						for (Element si : subItems) {
							Element siTitle = si.getElementsByTag("h4").first();
							Element siATitle = siTitle.getElementsByTag("a")
									.first();

							System.out.println("Sub Series Title:::::::::::::"
									+ siATitle.text());

							Elements indItems = si.select("ul.details").first()
									.getElementsByTag("li");
							List<String> list1 = new ArrayList<String>();
							for (Element indItem : indItems) {
								System.out
										.println("\t\t Feature Text :::::::::::::::"
												+ indItem.text());
								list1.add( indItem.text());
							}
							subdrawerpanel
									.setProperty("title", siATitle.text());
							
							subdrawerpanel.getNode("titlelink").setProperty("url", siATitle.attr("href"));
							subdrawerpanel.setProperty("description",
									list1.toArray(new String[list1.size()]));

							Elements rightItems = ss.select("ul.infolinks")
									.first().getElementsByTag("li");
							
							int n=1;
							for (Element rightItem : rightItems) {
								System.out
										.println("\t\t\t\t Right Text :::::::::::::::"
												+ rightItem.getElementsByTag(
														"a").text());
								
								if(subdrawerpanel.getNode("links").hasNode("item_"+n))
								{
									subdrawerpanel.getNode("links").getNode("item_"+n).getNode("link").setProperty("linktext", rightItem.getElementsByTag("a").text());
									subdrawerpanel.getNode("links").getNode("item_"+n).getNode("link").setProperty("url", rightItem.getElementsByTag("a").attr("href"));
									n++;
								}
							}

						}

					}

				}
			} catch (Exception e) {
				sb.append("Unable to update Drawer Panels.\n");
				e.printStackTrace();
			}

			// featured Hero
			try {
				//Value[] hPanels = hNode.getProperty("panelNodes").getValues();

				Elements fHeroes = doc.select("div.c50-pilot").first().select("div.frame");
				if (fHeroes.size() != hNode.getNodes("hero*").getSize())
					sb.append("Mis-Match in Hero Panels count/content.\n");
				NodeIterator itr2=hNode.getNodes("hero*");
				System.out.println("Hero Count:::::::::" + fHeroes.size());
				int j = 1;
				for (Element hero : fHeroes) {
					String desc = hero.getElementsByTag("p").first().text();
					String title = hero.getElementsByTag("h2").first().text();
					String ctaTitle = hero.getElementsByTag("b").first().text();
					javax.jcr.Node hPanel=null;
					
					if(itr2.hasNext())
					 hPanel= itr2.nextNode();

					if(hPanel!=null)
					{
					hPanel.setProperty("description", desc);
					hPanel.setProperty("title", title);
					hPanel.setProperty("linktext", ctaTitle);
					hPanel.getNode("cta").setProperty("url", hero.getElementsByTag("a")
							.first().attr("href"));

					
					}
				}
			} catch (Exception e) {
				sb.append("Unable to update Hero Panels.\n");
			}

			// Primary CTA
			try {
				Element cta = doc.select("div.c47-pilot").first();

				String desc = cta.getElementsByTag("p").first().text();
				//String title = cta.getElementsByTag("h3").first().text();
				String ctaTitle = cta.getElementsByTag("a").first().text();
				String ctaLink = cta.getElementsByTag("a").first().attr("href");

				javax.jcr.Node pCtaNode = session.getNode(pCta);
				pCtaNode.setProperty("title", title);
				pCtaNode.setProperty("description", desc);
				pCtaNode.setProperty("linktext", ctaTitle);
				pCtaNode.getNode("cta").setProperty("url", ctaLink);
			} catch (Exception e) {
				sb.append("Unable to update primary CTA.\n");
			}

			// Right Rail
			/*try {
				javax.jcr.Node rNode = session.getNode(rRail);
				NodeIterator itr1 = rNode.getNodes("tile*");

				System.out.println("Counttttttttttt::::::::" + itr1.getSize());

				Element rr = doc.select("div.gd-right").get(2);

				Elements sections = rr.select("div.poly");
				if (sections.size() == 0)
					sections = doc.select("div.gd-right").get(1)
							.select("div.poly");

				System.out.println("RRRR Count:::::::::" + sections.size());
				if (itr1.getSize() != sections.size())
					sb.append("Check tiles count/content.\n");
				int x = 0;
				while (itr1.hasNext() && x < sections.size()) {
					javax.jcr.Node secNode = itr1.nextNode();
					secNode.setProperty("title", sections.get(x)
							.getElementsByTag("h2").text());
					secNode.setProperty("description", sections.get(x)
							.getElementsByTag("p").text());
					secNode.setProperty("linktext", sections.get(x)
							.getElementsByTag("a").text());
					secNode.setProperty("linkurl", sections.get(x++)
							.getElementsByTag("a").attr("href"));
				}

			} catch (Exception e) {
				sb.append("Unable to update tiles in right rail.\n");
			}
*/
			session.save();

			System.out.println("PAGE TITLE:::::::::" + title);
		} catch (Exception e) {
			sb.append("UnKnown Error.\n");
		}
		return sb.toString();
	}

	public String updateURL(String url, javax.jcr.Node nd, String prop,
			int index, String locale) throws ValueFormatException,
			PathNotFoundException, RepositoryException {
		try {
			String u = null;
			if (url.contains("/c/en/us/"))
				return url;
			if (url.contains("/cisco/web/"))
				return url;
			else {
				if (index != -1) {
					// u=nd.getProperty(prop).getString().split("}")[index].split(",")[index+1].split(":")[index+1].replace("\"","");
					u = nd.getProperty(prop).getValues()[index].getString()
							.split(",")[1].split(":")[1].replace("\"", "");
					System.out
							.println("::::::::::::::GGGGGGGGGGGG::::::::::::::"
									+ u + "----index---" + index);
					if (u.contains("/en/us")) {
						u = u.replace("en/us", locale);
					}

				} else {
					u = nd.getProperty(prop).getString();
					if (u.contains("/en/us")) {
						u = u.replace("en/us", locale);
					}
				}
				System.out
						.println("::::::::::::::URLLLLLLLL::::::::::::::" + u);
				return u;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	public String updateImgURL(String url, javax.jcr.Node nd, String prop,
			String locale) throws ValueFormatException, PathNotFoundException,
			RepositoryException {
		String u = null;
		try {

			u = nd.getProperty(prop).getString()
					.replace("/assets/", "/global/" + locale + "/");
			return u;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return "";
	}

	public static void fileFromUrlToDam(String fAddress, String destination,
			Session mySession) {
		InputStream is = null;
		String mimeType = "";
		int fileLength = 0;
		try {
			System.out.println("::::::::::::::********URLLLLLLLL::::::::::::::"
					+ destination);

			URL Url = new URL(fAddress);
			URLConnection uCon = Url.openConnection();
			is = uCon.getInputStream();
			mimeType = uCon.getContentType();

			Binary binary = mySession.getValueFactory().createBinary(is);
			javax.jcr.Node root = mySession.getRootNode();

			String paths[] = destination.substring(1).split("/");
			int i = 0;
			String path = null;
			String pPath = null;
			while (i < paths.length) {
				System.out
						.println("::::::::::::::********URLLLLLLLL::::::::::::::"
								+ paths[i]);
				pPath = path;
				if (path == null)
					path = "/" + paths[i];
				else
					path = path + "/" + paths[i];

				System.out
						.println("::::::::::::::!!!!!!!!!!!!!URLLLLLLLL::::::::::::::"
								+ path);

				if (!mySession.nodeExists(path)) {
					if (i == (paths.length - 1)) {
						javax.jcr.Node myNewNode = mySession.getNode(pPath)
								.addNode(paths[i], "nt:file");
						javax.jcr.Node contentNode = myNewNode.addNode(
								"jcr:content", "nt:resource");
						// set the mandatory properties
						contentNode.setProperty("jcr:data", binary);
						contentNode.setProperty("jcr:lastModified",
								Calendar.getInstance());
						contentNode.setProperty("jcr:mimeType", mimeType);
					} else {

						javax.jcr.Node myNewNode = mySession.getNode(pPath)
								.addNode(paths[i], "nt:folder");
						/*
						 * if(myNewNode==null) { javax.jcr.Node nd=root.addNode(
						 * paths[i], "nt:folder"); root=nd; }
						 */
					}
				}
				i++;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
