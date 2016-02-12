package com.cisco.OVWMigrationServices.servlet;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.rmi.ServerException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.jcr.Session;
import javax.servlet.ServletOutputStream;

import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
//Sling Imports
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



//DAM API
import com.day.cq.dam.api.Asset;
import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
//QueryBuilder APIs
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.Hit;
import com.day.cq.search.result.SearchResult;
 
@SlingServlet(paths="/bin/myDownloadServlet", methods = "GET", metatype=true)
public class DownloadAssets extends org.apache.sling.api.servlets.SlingAllMethodsServlet{
     
     
    //Set up References
    /** Default log. */
    protected final Logger log = LoggerFactory.getLogger(this.getClass());
               
    private Session session;
                   
    //Inject a Sling ResourceResolverFactory
    @Reference
    private ResourceResolverFactory resolverFactory;
               
    @Reference
    private QueryBuilder builder;
     
     
    @Override
    //The GET Method uses the AEM QueryBuilder API to retrieve DAM Assets, places them in a ZIP and returns it
    //in the HTTP output stream
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServerException, IOException {
       
     try
     {
    	 
    	 String conPath = request.getParameter("contentPath");
        //Invoke the adaptTo method to create a Session 
        ResourceResolver resourceResolver = resolverFactory.getAdministrativeResourceResolver(null);
        session = resourceResolver.adaptTo(Session.class);
          
        // create query description as hash map (simplest way, same as form post)
        Map<String, String> map = new HashMap<String, String>();
      
        //set QueryBuilder search criteria                   
        map.put("type", "dam:Asset");
        map.put("path", conPath); 
        
        builder= resourceResolver.adaptTo(QueryBuilder.class);
         
        //INvoke the Search query
        Query query = builder.createQuery(PredicateGroup.create(map), session);
         
        SearchResult sr= query.getResult();
         
        //write out to the AEM Log file
         
        //Create a MAP to store results
        Map<String, InputStream> dataMap = new HashMap<String, InputStream>();
     
        // iterating over the results
        for (Hit hit : sr.getHits()) {
             
            //Convert the HIT to an asset - each asset will be placed into a ZIP for downloading
            String path = hit.getPath();
            Resource rs = resourceResolver.getResource(path);
            Asset asset = rs.adaptTo(Asset.class);   
               
            //We have the File Name and the inputstream
            InputStream data = asset.getOriginal().getStream();
            String name =asset.getName(); 
                         
           //Add to map
            dataMap.put(name, data); // key is fileName and value is inputStream - this will all be placed in ZIP file
       }
                    
        //ZIP up the AEM DAM Assets
        byte[] zip = zipFiles(dataMap);
         
        //
        // Sends the response back to the user / browser. The
        // content for zip file type is "application/zip". We
        // also set the content disposition as attachment for
        // the browser to show a dialog that will let user 
        // choose what action will he do to the sent content.
        //
         
        ServletOutputStream sos = response.getOutputStream();
         
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment;filename=dam.zip");
         
         
        // Write bytes to tmp file.
        sos.write(zip);
        sos.flush();    
     }
     catch(Exception e)
     {
     }
   }
     
     
     
    /**
     * Create the ZIP with AEM DAM Assets.
     */
    private byte[] zipFiles(Map data) throws IOException{
    	
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);
        byte bytes[] = new byte[2048];
        Iterator<Map.Entry<String, InputStream>> entries = data.entrySet().iterator();
         
        while (entries.hasNext()) {
            Map.Entry<String, InputStream> entry = entries.next();
             
            String fileName =(String) entry.getKey(); 
            InputStream is1 =(InputStream) entry.getValue(); 
             
            BufferedInputStream bis = new BufferedInputStream(is1);
 
            //populate the next entry of the ZIP with the AEM DAM asset
            zos.putNextEntry(new ZipEntry(fileName));
 
            int bytesRead;
            while ((bytesRead = bis.read(bytes)) != -1) {
                zos.write(bytes, 0, bytesRead);
                
            }
            zos.closeEntry();
            bis.close();
            is1.close();
        }
         
       zos.flush();
        baos.flush();
        zos.close();
        baos.close();
        return baos.toByteArray();
        
    }
 
}