package com.cisco.OVWMigrationServices.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;

import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.commons.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.AssetManager;

@SlingServlet(paths = { "/bin/services/DAMMigration" }, methods = { "GET" })
@Properties({ @Property(name = "service.pid", value = "com.cisco.OVWMigrationServices.servlet.DAMMigrationServlet") })
public class DAMMigrationServlet extends SlingAllMethodsServlet{

	/**
	 * serialVersionUID = 1L;
	 */
	private static final long serialVersionUID = 1L;

	/** The Constant LOG. */
	private static final Logger LOG = LoggerFactory
			.getLogger(DAMMigrationServlet.class);

	@Reference
	private ResourceResolverFactory resolverFactory;

	protected void doPost(SlingHttpServletRequest request,
			SlingHttpServletResponse response) throws IOException {

		LOG.debug("In the doPost method of OVWMigrationServlet");
		JSONObject jsonObj = new JSONObject();
		PrintWriter printWritter = response.getWriter();
		String errorMsg = "";
		try {
			String newImagePath = "";
			String imgPath = request.getParameter("imgPath");
			String locale = request.getParameter("locale");
			URL url = new URL(imgPath);
			String path = url.getPath();
			String imageName = path.substring(path.lastIndexOf("/") + 1,
					path.length());
			String imagePath = path.substring(0, path.lastIndexOf("/"));
			InputStream is = url.openStream();
			ResourceResolver resourceResolver = resolverFactory
					.getAdministrativeResourceResolver(null);

			AssetManager assetMgr = resourceResolver
					.adaptTo(AssetManager.class);
			String newFile = "/content/dam/global/"+ locale + imagePath + "/" + imageName;
			Asset imageAsset = assetMgr.createAsset(newFile, is, "image/jpeg",
					true);
			if (imageAsset != null) {
				newImagePath = imageAsset.getPath();
			}
			jsonObj.put("newImagePath", newImagePath);
		} catch (Exception e) {
			errorMsg = e.getMessage();
		}
		try{
			jsonObj.put("error", errorMsg);
		}catch(Exception e){
			LOG.error("Exception : ",e);
		}
		printWritter.print(jsonObj.toString());
	}

	protected void doGet(SlingHttpServletRequest request,
			SlingHttpServletResponse response) throws IOException {
		doPost(request, response);
	}

}
