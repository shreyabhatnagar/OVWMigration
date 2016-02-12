package com.cisco.OVWMigrationServices.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;

import javax.jcr.Node;

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
import org.apache.sling.commons.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.AssetManager;
import com.day.jcr.vault.util.MimeTypes;

@SlingServlet(paths = { "/bin/services/DAMMigration" }, methods = { "GET" })
@Properties({ @Property(name = "service.pid", value = "com.cisco.OVWMigrationServices.servlet.DAMMigrationServlet") })
public class DAMMigrationServlet extends SlingAllMethodsServlet {

	/**
	 * serialVersionUID = 1L;
	 */
	private static final long serialVersionUID = 1L;

	/** The Constant LOG. */
	private static final Logger LOG = LoggerFactory.getLogger(DAMMigrationServlet.class);

	@Reference
	private ResourceResolverFactory resolverFactory;

	protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {

		LOG.debug("In the doPost method of OVWMigrationServlet");
		JSONObject jsonObj = new JSONObject();
		PrintWriter printWritter = response.getWriter();
		StringBuilder errorMsg = new StringBuilder();
		ResourceResolver resourceResolver = null;
		InputStream is = null;
		StringBuilder log = new StringBuilder();
		try {
			String newImagePath = "";
			String imgPath = request.getParameter("imgPath");
			String imgRef = request.getParameter("imgRef");
			String locale = request.getParameter("locale");

			String extension = imgPath.substring(imgPath.lastIndexOf(".") + 1, imgPath.length());
			if (extension.length() >= 4 || extension.lastIndexOf("?") != -1) {
				if (extension.lastIndexOf("?") != -1) {
					extension = extension.substring(0,
							extension.lastIndexOf("?"));
				}
			}

			URL url = new URL(imgPath);
			is = url.openStream();
			resourceResolver = resolverFactory.getAdministrativeResourceResolver(null);
			AssetManager assetMgr = resourceResolver.adaptTo(AssetManager.class);
			Asset imageAsset = null;
			if (is != null) {
				if (StringUtils.isNotBlank(extension)) {
					String mimeType = MimeTypes.getMimeType(extension.toLowerCase());
					if (StringUtils.isNotBlank(mimeType)) {
						com.adobe.granite.asset.api.AssetManager asserts = resourceResolver.adaptTo(com.adobe.granite.asset.api.AssetManager.class);
						boolean assetExists = asserts.assetExists(imgRef);
						if (!assetExists) {
							imageAsset = assetMgr.createAsset(imgRef, is, mimeType, true);
						} else {
							newImagePath = imgRef;
							log.append("~asset already exists");
						}
					} else {
						log.append("~No mime Type Found");
					}
				} else {
					log.append("~No extension Found in the url");
				}
			} else {
				log.append("~Asset not found : " + url.getPath());
			}
			if (imageAsset != null) {
				newImagePath = imageAsset.getPath();
				Node imgNode = imageAsset.adaptTo(Node.class);
				Node jcr_content = (imgNode != null && imgNode.hasNode("jcr:content")) ? imgNode.getNode("jcr:content") : null;
				Node metadata = (jcr_content != null && jcr_content.hasNode("metadata")) ? jcr_content.getNode("metadata") : null;
				log = log.append("~metadata NodePath : ").append(metadata.getPath());
				metadata.setProperty("jcr:language", locale);
				metadata.getSession().save();
			}
			jsonObj.put("newImagePath", newImagePath);
		} catch (Exception e) {
			errorMsg.append(e.getStackTrace());
		} finally {
			if (resourceResolver != null) {
				resourceResolver.close();
			}
			if (is != null) {
				is.close();
			}
		}
		try {
			jsonObj.put("error", errorMsg.toString());
			jsonObj.put("log", log.toString());
		} catch (Exception e) {
			LOG.error("Exception : ", e);
		}
		printWritter.print(jsonObj.toString());
	}
	protected void doGet(SlingHttpServletRequest request,
			SlingHttpServletResponse response) throws IOException {
		doPost(request, response);
	}
}
