package com.cisco.dse.global.migration.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class pagePropertiesCreation {


	static Repository repository = null;
	static Session session = null;
	static Logger log = Logger.getLogger(pagePropertiesCreation.class);

	public static void main(String s[]) throws FileNotFoundException,
			IOException {

		Properties prop = new Properties();
		InputStream input = null;

		String host = null;
		String userId = null;
		String pwd = null;
		String workspace = null;
		String workbookpath = null;
		String reportspath = null;
		String repo = null;

		try {
			String filename = "pageConfig.properties";
			input = pagePropertiesCreation.class.getClassLoader().getResourceAsStream(
					filename);
			if (input == null) {
				log.debug("input is null");
				return;
			}
			// load a properties file from class path, inside static method
			prop.load(input);

			host = StringUtils.isNotBlank(prop.getProperty("serverurl")) ? prop
					.getProperty("serverurl") : "";
			repo = host + "/crx/server";
			userId = StringUtils.isNotBlank(prop.getProperty("aemuser")) ? prop
					.getProperty("aemuser") : "";
			pwd = StringUtils.isNotBlank(prop.getProperty("aempassword")) ? prop
					.getProperty("aempassword") : "";
			workspace = StringUtils.isNotBlank(prop.getProperty("workspace")) ? prop
					.getProperty("workspace") : "";
			workbookpath = StringUtils.isNotBlank(prop
					.getProperty("workbookpath")) ? prop
					.getProperty("workbookpath") : "";
			reportspath = StringUtils.isNotBlank(prop
					.getProperty("reportspath")) ? prop
					.getProperty("reportspath") : "";
			log.debug("host : " + host);
			log.debug("userId : " + userId);
			log.debug("pwd : " + pwd);
			log.debug("workspace : " + workspace);
			log.debug("workbookpath : " + workbookpath);
			log.debug("reportspath : " + reportspath);
		} catch (IOException ex) {
			log.error("IOException : ", ex);
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		try {
			if (host != "" && userId != "" && pwd != "" && workspace != ""
					&& workbookpath != "" && reportspath != "") {
				repository = JcrUtils.getRepository(repo);
				session = repository.login(
						new SimpleCredentials(userId, pwd.toCharArray()),
						workspace);
				XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream(
						workbookpath));

				for (XSSFSheet sheet : workbook) {
					log.debug("Sheet name : " + sheet.getSheetName());

					XSSFRow firstRow = sheet.getRow(0);
					short cellnum1 = firstRow.getLastCellNum();
					List<String> propertyNamesList = new ArrayList<String>();
					List<String> propertyValuesList = new ArrayList<String>();
					HashMap<String, String> rowList = new HashMap<String, String>();
					log.debug("cell num" + cellnum1);
					for (int i = 0; i < cellnum1; i++) {
						if(firstRow.getCell(i) != null){
						String headingColumnValue = firstRow.getCell(i)
								.getStringCellValue();
						log.debug("headingColumnValue:"
								+ headingColumnValue);
						propertyNamesList.add(headingColumnValue);
						log.debug("prop name list size " + propertyNamesList.size());
						}
					}
					for (Row tempRow : sheet) {
						if(!tempRow.getCell(0).getStringCellValue().equals("URL")){
						
						String nodePath = tempRow.getCell(0) != null ? tempRow.getCell(0).getStringCellValue() : "";
						nodePath = nodePath.replace(".html", "");
						String pagePropertiesPath = nodePath+"/jcr:content";
						log.debug("node path" + pagePropertiesPath);
						javax.jcr.Node pageJcrNode = null;
					
						try{
						pageJcrNode = session.getNode(pagePropertiesPath);
						log.debug("Connected");
						}catch(Exception e){
							log.debug("exception");
						}
						
						if(pageJcrNode != null){
							log.debug("page jcr node");
						for (int i = 0; i < propertyNamesList.size(); i++) {
							String propertyValue = "";
							if(pageJcrNode.hasProperty(propertyNamesList.get(i))){
								propertyValue = pageJcrNode.getProperty(propertyNamesList.get(i)).getValue().getString();
							
								log.debug("propertyValue " + propertyValue);
							}else {
								if(propertyNamesList.get(i).equals("URL")){
									propertyValue ="Firstrow";
								}else{
								propertyValue = "";
								}
							}
							propertyValuesList.add(propertyValue);
							rowList.put(propertyNamesList.get(i),
									propertyValuesList.get(i));
							log.debug("row list" + rowList);
							
						}

						pagePropertiesCreate(propertyNamesList, rowList, tempRow, workbook);
						propertyValuesList.clear();
						rowList.clear();
						}else{
							log.debug("Node not found");
						}
						}
					}

					log.debug("In the main method of PagePropertiesUpdation");
					}
				workbook.close();
			} else {
				log.debug("config.properties file is not configured with 'serverurl' or 'aemuser' or 'aempassword' or 'workspace' or 'workbookpath' or 'reportspath'");
			}
		} catch (Exception e) {
			log.error("Exception in main of PagePropertiesUpdation : ", e);
		}
		finally {
			log.debug("Session completed");
			session.logout();
		}
	}

	private static void pagePropertiesCreate(List<String> propertyNamesList,
			HashMap<String, String> rowList, Row tempRow, XSSFWorkbook workbook) throws IOException {
		
			for(int i=0; i<propertyNamesList.size(); i++){
				String propName = propertyNamesList.get(i);
				String keyProp = rowList.get(propName) != null ? rowList.get(propName) : "";
				log.debug("key prop" + keyProp);
				if(!keyProp.equals("Firstrow")){
				tempRow.createCell(i).setCellValue(keyProp);
				log.debug("set value");
				FileOutputStream out = new FileOutputStream(new File("C:/test/OVWDEMOPage.xlsx"));
				workbook.write(out);
				out.close();
				System.out.println("Value wrote in excel");
				}
			}
		
	}


}
