package com.cisco.OVWMigrationServices.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.Value;

import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.SearchResult;

@SlingServlet(paths = { "/bin/services/GetimagePaths" }, methods = { "GET" })
@Properties({ @Property(name = "service.pid", value = "com.cisco.OVWMigrationServices.servlet.GetImagePaths") })
public class GetImagePaths extends SlingAllMethodsServlet {

	/**
	 * serialVersionUID = 1L;
	 */
	private static final long serialVersionUID = 1L;

	/** The Constant LOG. */
	private static final Logger LOG = LoggerFactory.getLogger(DAMMigrationServlet.class);

	@Reference
	private ResourceResolverFactory resolverFactory;

	@Reference
	private QueryBuilder builder;

	protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {

		PrintWriter printWritter = response.getWriter();
		ResourceResolver resourceResolver = null;
		JSONArray ja = new JSONArray();
		Session session = null;
		String pagePath = request.getParameter("pagePath");
		Map<String, String> queryMap = new HashMap<String, String>();
		Set<String> set = new HashSet<String>();

		try {
			resourceResolver = resolverFactory.getAdministrativeResourceResolver(null);
			session = resourceResolver.adaptTo(Session.class);
			queryMap.put("path", pagePath);
			queryMap.put("type", "nt:unstructured");
			queryMap.put("property", "fileReference");
			queryMap.put("property.operation", "exists");
			queryMap.put("property.value", "true");
			Query query = builder.createQuery(PredicateGroup.create(queryMap), session);
			SearchResult result = query.getResult();
			Iterator<Node> groupNodes = result.getNodes();
			Node groupNode = null;
			while (groupNodes.hasNext()) {
				groupNode = groupNodes.next();
				String fileReference = groupNode.hasProperty("fileReference") ? groupNode.getProperty("fileReference").getString() : "pathNotFound";
				set.add(fileReference);
			}
		} catch (Exception e) {
			printWritter.print("Exception : " + e);
		}
		try {
			queryMap = new HashMap<String, String>();
			queryMap.put("path", pagePath);
			queryMap.put("type", "nt:unstructured");
			queryMap.put("property", "listitems");
			queryMap.put("property.operation", "exists");
			queryMap.put("property.value", "true");
			Query query = builder.createQuery(PredicateGroup.create(queryMap), session);
			SearchResult result = query.getResult();
			Iterator<Node> groupNodes = result.getNodes();
			Node groupNode = null;
			while (groupNodes.hasNext()) {
				groupNode = groupNodes.next();
				javax.jcr.Property listitems = groupNode.hasProperty("listitems") ? groupNode.getProperty("listitems") : null;
				if(listitems.isMultiple()){
					Value[] values = listitems.getValues();
					for(Value val : values){
						String data = val.getString();
						if(StringUtils.isNotBlank(data)){
							JSONObject obj = new JSONObject(data);
							String url = (String)(obj.has("linkurl")?obj.get("linkurl"):"");
							if(url.endsWith(".pdf") || url.endsWith(".doc") || url.endsWith(".docx") || url.endsWith(".PDF") || url.endsWith(".DOC") || url.endsWith(".DOCX")){
								if(resourceResolver.getResource(url) != null){
									set.add(url);
								}
							}
						}
					}
				}else{
					String data = listitems.getValue().getString();
					if(StringUtils.isNotBlank(data)){
						JSONObject obj = new JSONObject(data);
						String url = (String)(obj.has("linkurl")?obj.get("linkurl"):"");
						if(url.endsWith(".pdf") || url.endsWith(".doc") || url.endsWith(".docx") || url.endsWith(".PDF") || url.endsWith(".DOC") || url.endsWith(".DOCX")){
							if(resourceResolver.getResource(url) != null){
								set.add(url);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			printWritter.print("Exception : " + e);
		}
		
		
		set.addAll(getImagePathsFromHtml("html", pagePath, session, resourceResolver));// get the html content from the html property to get the image paths.

		set.addAll(getImagePathsFromHtml("text", pagePath, session, resourceResolver));// get the html content from the text property to get the image paths.

		try {
			for (String val : set) {
				ja.put(val);
			}
		} catch (Exception e) {
			printWritter.print("Exception : " + e);
		}
		printWritter.print(ja.toString());
	}

	Set<String> getImagePathsFromHtml(String propName, String pagePath,
			Session session, ResourceResolver resourceResolver) {

		Map<String, String> queryMap = new HashMap<String, String>();
		Set<String> set = new HashSet<String>();
		try {
			queryMap.put("path", pagePath);
			queryMap.put("type", "nt:unstructured");
			queryMap.put("property", propName);
			queryMap.put("property.operation", "exists");
			queryMap.put("property.value", "true");

			Query query = builder.createQuery(PredicateGroup.create(queryMap), session);
			SearchResult result = query.getResult();
			Iterator<Node> groupNodes = result.getNodes();
			Node groupNode = null;
			while (groupNodes.hasNext()) {
				groupNode = groupNodes.next();
				String html = groupNode.hasProperty(propName) ? groupNode
						.getProperty(propName).getString() : "pathNotFound";

				Document document = Jsoup.parse(html);
				for (Element element : document.getElementsByTag("img")) {
					String src = element.attr("src");
					set.add(src);
				}
				
				for (Element element : document.getElementsByTag("a")) {
					String url = element.attr("href");
					if(url.endsWith(".pdf") || url.endsWith(".doc") || url.endsWith(".docx") || url.endsWith(".PDF") || url.endsWith(".DOC") || url.endsWith(".DOCX")){
						if(resourceResolver.getResource(url) != null){
							set.add(url);
						}
					}
				}
			}
		} catch (Exception e) {
			set.add(e.getMessage());
		}
		return set;
	}

	protected void doGet(SlingHttpServletRequest request,
			SlingHttpServletResponse response) throws IOException {
		doPost(request, response);
	}
}
