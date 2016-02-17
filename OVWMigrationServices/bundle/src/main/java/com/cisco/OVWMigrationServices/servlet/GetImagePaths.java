package com.cisco.OVWMigrationServices.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Session;

import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.DamConstants;
import com.day.cq.dam.commons.util.AssetReferenceSearch;
import com.day.cq.search.QueryBuilder;
import com.day.jcr.vault.fs.api.PathFilterSet;
import com.day.jcr.vault.fs.api.ProgressTrackerListener;
import com.day.jcr.vault.fs.config.DefaultWorkspaceFilter;
import com.day.jcr.vault.packaging.JcrPackage;
import com.day.jcr.vault.packaging.JcrPackageDefinition;
import com.day.jcr.vault.packaging.JcrPackageManager;
import com.day.jcr.vault.packaging.PackagingService;
import com.day.jcr.vault.util.DefaultProgressListener;

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

	protected void doPost(SlingHttpServletRequest request,
			SlingHttpServletResponse response) throws IOException {
		
		String packageName = request.getParameter("name");
		if(StringUtils.isBlank(packageName)){
			java.util.Date date = new java.util.Date();
			packageName = "my package" + "-" + new Timestamp(date.getTime()).toString().replace(":", "-").replace(".", "-");
		}
		String packageGroupName = "OVW-Migration";

		PrintWriter printWritter = response.getWriter();
		ResourceResolver resourceResolver = null;
		Session session = null;
		XSSFWorkbook workbook = null;
		try {
			resourceResolver = resolverFactory
					.getAdministrativeResourceResolver(null);
			session = resourceResolver.adaptTo(Session.class);
			JcrPackageManager packageManager = (JcrPackageManager) PackagingService
					.getPackageManager(session);
			/*
			 * For 'create' method the parameter packageGroup is optional we can
			 * give group name under which the package should be created else it
			 * will take default, packageName is the name of the package and 1.0
			 * is the version of the package
			 */
			JcrPackage pack = packageManager.create(packageGroupName, packageName, "1.0");
			JcrPackageDefinition definition = pack.getDefinition();

			DefaultWorkspaceFilter filter = new DefaultWorkspaceFilter();
			/* nodePaths is the List containing the list of paths */
			
			
			Node ntFileNode = session.getNode("/content/dam/assets/OVWDEMO_DAM_ASSETS.xlsx/jcr:content/renditions/original/jcr:content"); 
			InputStream is = ntFileNode.getProperty("jcr:data").getBinary().getStream();
			workbook = new XSSFWorkbook(is);
			int count = 0;
			int totAssets = 0;
			for (XSSFSheet sheet : workbook) {
				for (Row tempRow : sheet) {
					if(count == 0){
					String pagePath = tempRow.getCell(0) != null ? tempRow
							.getCell(0).getStringCellValue() : "";
					printWritter.print("\n------------------------------------------");		
					printWritter.print("\npage path : " + pagePath);
					Resource r = resourceResolver.getResource(pagePath
							+ "/jcr:content");
					Node n = r.adaptTo(Node.class);
					AssetReferenceSearch ref = new AssetReferenceSearch(n,
							DamConstants.MOUNTPOINT_ASSETS, resourceResolver);
					Map<String, Asset> allref = new HashMap<String, Asset>();
					allref.putAll(ref.search());
					for (Map.Entry<String, Asset> entry : allref.entrySet()) {
						Asset asset = entry.getValue();
						printWritter.print("\nassets : " + asset.getPath());
						PathFilterSet pathFilterSet = new PathFilterSet();
						pathFilterSet.setRoot(asset.getPath());
						filter.add(pathFilterSet);
						totAssets++;
					}
					}
					if(count == 1){
						String pagePath = tempRow.getCell(0) != null ? tempRow
								.getCell(0).getStringCellValue() : "";
						PathFilterSet pathFilterSet = new PathFilterSet();
						pathFilterSet.setRoot(pagePath);
						filter.add(pathFilterSet);		
					}
				}
				count++;
			}
			definition.setFilter(filter, true);
			ProgressTrackerListener listener = new DefaultProgressListener();
			packageManager.assemble(pack, listener);
			printWritter.print("\nTotal assets : "+totAssets);
		} catch (Exception e) {
			printWritter.print("Exception : " + e);
		}
	}

	protected void doGet(SlingHttpServletRequest request,
			SlingHttpServletResponse response) throws IOException {
		doPost(request, response);
	}
}
