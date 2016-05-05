package com.cisco.dse.global.migration.config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class PagePropertiesUpdation {

	static Repository repository = null;
	static Session session = null;
	static Logger log = Logger.getLogger(PagePropertiesUpdation.class);

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
			input = PagePropertiesUpdation.class.getClassLoader()
					.getResourceAsStream(filename);
			if (input == null) {
				log.debug("Input is null");
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
					for (int i = 0; i < cellnum1; i++) {
						String headingColumnValue = firstRow.getCell(i)
								.getStringCellValue();
						propertyNamesList.add(headingColumnValue);
					}
					sheet.removeRow(firstRow);
					for (Row tempRow : sheet) {
						propertyValuesList.clear();
						for (int i = 0; i < cellnum1; i++) {
							String propertyValue = tempRow.getCell(i) != null ? tempRow
									.getCell(i).getStringCellValue() : "";
							propertyValuesList.add(propertyValue);
							rowList.put(propertyNamesList.get(i),
									propertyValuesList.get(i));
						}

						pagePropertiesUpdation(propertyNamesList, rowList);
						rowList.clear();
					}
				}
				workbook.close();
				session.logout();
			} else {
				log.debug("config.properties file is not configured with 'serverurl' or 'aemuser' or 'aempassword' or 'workspace' or 'workbookpath' or 'reportspath'");
			}
		} catch (Exception e) {
			log.error("Exception in main of PagePropertiesUpdation : ", e);
		}
	}

	private static void pagePropertiesUpdation(List<String> propertyNamesList,
			HashMap<String, String> rowList) {
		String propMulVal[] = new String[1000];
		String pageUrl = rowList.get("URL");
		log.debug("----------Stated updating page properties of " + pageUrl
				+ "----------");
		if (pageUrl.indexOf("/content") != -1)
			pageUrl = pageUrl.substring(pageUrl.indexOf("/content"),
					pageUrl.length());
		if (pageUrl.contains(".html")) {
			pageUrl = pageUrl.substring(0, pageUrl.lastIndexOf("."));
		}
		log.debug("Page path is " + pageUrl);

		try {
			Node pageNode = session.getNode(pageUrl + "/jcr:content");
			if (pageNode != null) {
				for (String propertyName : propertyNamesList) {
					String prpValue = rowList.get(propertyName);

					if (prpValue.equals("yes")) {
						prpValue = "true";
					} else if (prpValue.equals("no")) {
						prpValue = "false";
					}
					if (StringUtils.isNotBlank(propertyName)
							&& pageNode.hasProperty(propertyName)) {
						boolean isMulti = pageNode.getProperty(propertyName)
								.isMultiple();

						if (!isMulti) {
							log.debug("Property "
									+ propertyName
									+ " already exists, it's old value is: "
									+ pageNode.getProperty(propertyName)
											.getString());
							pageNode.setProperty(propertyName,
									rowList.get(propertyName));
						} else {
							log.debug("Property " + propertyName
									+ " already exists ");
							propMulVal = rowList.get(propertyName).split(":,");
							pageNode.setProperty(propertyName, propMulVal);
						}

						log.debug(propertyName + " is set to " + prpValue);
					} else {
						if (StringUtils.isNotBlank(propertyName)
								&& !"URL".equals(propertyName)
								&& StringUtils.isNotBlank(prpValue)) {
							pageNode.setProperty(propertyName, prpValue);
							log.debug(propertyName + " is set to " + prpValue);

						}
					}
					session.save();
				}
			}
			log.debug("----------Done with the updation of page properties "
					+ pageUrl + "----------");
		} catch (PathNotFoundException e) {
			log.debug("PathNotFoundException :", e);
		} catch (RepositoryException e) {
			log.debug("RepositoryException :", e);
		}
	}

}
