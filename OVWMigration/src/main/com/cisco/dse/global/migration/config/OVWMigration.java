package com.cisco.dse.global.migration.config;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.jcr.Repository;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.cisco.dse.global.migration.benefit.BenefitsVariation03;
import com.cisco.dse.global.migration.benefit.BenifitsVariation2;
import com.cisco.dse.global.migration.buyersguide.BuyersGuideVariation01;
import com.cisco.dse.global.migration.architechture.ArchitechtureVariation1;
import com.cisco.dse.global.migration.architechture.ArchitechtureVariation3;
import com.cisco.dse.global.migration.architechture.ArchitectureVariation04;
import com.cisco.dse.global.migration.partner.PartnerVariation1;
import com.cisco.dse.global.migration.web.WebVariation3;
import com.cisco.dse.global.migration.productlisting.ProductListingVariation3;
import com.cisco.dse.global.migration.productlisting.ProductListingVariation4;
import com.cisco.dse.global.migration.productlisting.ProductListingVariation5;
import com.cisco.dse.global.migration.productlisting.ProductListingVariation6;
import com.cisco.dse.global.migration.productlisting.ProductListingVariation7;
import com.cisco.dse.global.migration.productlanding.ProductLandingVariation08;
import com.cisco.dse.global.migration.productlanding.ProductLandingVariation1;
import com.cisco.dse.global.migration.productlanding.ProductLandingVariation10;
import com.cisco.dse.global.migration.productlanding.ProductLandingVariation11;
import com.cisco.dse.global.migration.productlanding.ProductLandingVariation12;
import com.cisco.dse.global.migration.productlanding.ProductLandingVariation3;
import com.cisco.dse.global.migration.productlanding.ProductLandingVariation5;
import com.cisco.dse.global.migration.productlanding.ProductLandingVariation6;
import com.cisco.dse.global.migration.productlanding.ProductLandingVariation9;
import com.cisco.dse.global.migration.rroot.RProductVariation1;
import com.cisco.dse.global.migration.rroot.RSolutionIndex;
import com.cisco.dse.global.migration.rbenefit.RBenefitVariation1;
import com.cisco.dse.global.migration.rproductlanding.RProductLandingVariation1;
import com.cisco.dse.global.migration.rproductlanding.RProductLandingVariation2;
import com.cisco.dse.global.migration.rproductlisting.RProductListingVariation1;
import com.cisco.dse.global.migration.rproductlisting.RProductListingVariation2;
import com.cisco.dse.global.migration.rtechnology.RTechnologyVariation1;
import com.cisco.dse.global.migration.rservicelisting.RServiceListingVariation1;
import com.cisco.dse.global.migration.rservicelisting.RServiceListingVariation2;
import com.cisco.dse.global.migration.servicelisting.ServiceListingVariation01;
import com.cisco.dse.global.migration.servicelisting.ServiceListingVariation02;
import com.cisco.dse.global.migration.servicelisting.ServiceListingVariation03;
import com.cisco.dse.global.migration.solutionlisting.SolutionListingVariation08;
import com.cisco.dse.global.migration.solutionlisting.SolutionListingVariation09;
import com.cisco.dse.global.migration.solutionlisting.SolutionListingVariation11;
import com.cisco.dse.global.migration.solutionlisting.SolutionListingVariation12;
import com.cisco.dse.global.migration.solutionlisting.SolutionListingVariation2;
import com.cisco.dse.global.migration.technology.TechnologyVariation2;
import com.cisco.dse.global.migration.trainingevents.TrainingAndEventsVariation1;
import com.cisco.dse.global.migration.web.WebVariation5;
import com.cisco.dse.global.migration.web.WebVariation6;
import com.cisco.dse.global.migration.rsolutionlisting.RSolutionListingVariation01;
import com.cisco.dse.global.migration.rsolutionlisting.RSolutionListingVariation02;
import com.cisco.dse.global.migration.buyersguide.BuyersGuideVariation02;
import com.cisco.dse.global.migration.buyersguide.BuyersGuideVariation03;
import com.cisco.dse.global.migration.subcat.SubCatVariation1;
import com.cisco.dse.global.migration.subcat.SubCatVariation2;
import com.cisco.dse.global.migration.subcat.SubCatVariation3;
import com.cisco.dse.global.migration.web.WebVariation2;
import com.cisco.dse.global.migration.web.WebVariation1;
import com.cisco.dse.global.migration.web.WebVariation7;
import com.cisco.dse.global.migration.web.WebVariation8;
import com.cisco.dse.global.migration.web.WebVariation9;

public class OVWMigration {

	static Repository repository = null;
	static Session session = null;
	static Logger log = Logger.getLogger(OVWMigration.class);

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
			String filename = "config.properties";
			input = OVWMigration.class.getClassLoader().getResourceAsStream(
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

		log.debug("In the main method of OVWMigration");

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
					String sheetName = sheet.getSheetName();
					StringBuilder sb = new StringBuilder(1024);

					sb.append("<html><head><meta charset='UTF-16'></head><body><table  border='1'>");
					// Start Map
					Map<String, String> urlMap = new HashMap<String, String>();
					for (Row tempRow : sheet) {
						String gLink = tempRow.getCell(0) != null ? tempRow
								.getCell(0).getStringCellValue() : "";
								String prod = tempRow.getCell(1) != null ? tempRow
										.getCell(1).getStringCellValue() : "";
										String type = tempRow.getCell(2) != null ? tempRow
												.getCell(2).getStringCellValue() : "";
												String cattype = tempRow.getCell(3) != null ? tempRow
														.getCell(3).getStringCellValue() : "";
														if (StringUtils.isNotBlank(type) && type.indexOf("-") != -1) {
															String variationType =  type.substring(0, type.lastIndexOf("-"));
															String variation =  type.substring(type.lastIndexOf("-") + 1);
															log.debug("variation : " + variation);
															log.debug("actual type : " + variationType);
															String pageUrl = host + "/content/<locale>/"+ cattype + "/<prod>/"+ variationType + ".html";
															if (StringUtils.isNotBlank(variation) && variation.startsWith("Rroot")) {
																pageUrl = pageUrl.replace("<locale>", sheet.getSheetName()).replace("/<prod>", "");
															}else if(StringUtils.isNotBlank(variationType) && (variationType.equals("partners") || variationType.equals("training-events") || variationType.equals("about") || variationType.equals("industries"))){
																pageUrl = pageUrl.replace("<locale>", sheet.getSheetName()).replace("/<prod>/", "");
															} else if(StringUtils.isNotBlank(variation) && variation.startsWith("infrastructure")){
																pageUrl = pageUrl.replace("<locale>", sheet.getSheetName()).replace("<prod>", prod+"/network-infrastructure");
																log.debug("pageURL is: " + pageUrl);
															}else if(StringUtils.isNotBlank(variation) && variation.startsWith("internet")){
																pageUrl = pageUrl.replace("<locale>", sheet.getSheetName()).replace("<prod>", prod+"/mobile-internet");
																log.debug("pageURL is: " + pageUrl);
															}
															else if(StringUtils.isNotBlank(variation) && variation.startsWith("videoscape")){
																pageUrl = pageUrl.replace("<locale>", sheet.getSheetName()).replace("<prod>", prod+"/videoscape");
																log.debug("pageURL is: " + pageUrl);
															}else if(StringUtils.isNotBlank(variation) && variation.startsWith("ps")){
																pageUrl = pageUrl.replace("<locale>", sheet.getSheetName()).replace("<prod>", prod+"/professional-services");
															}else {
																pageUrl = pageUrl.replace("<locale>", sheet.getSheetName()).replace("<prod>", prod);
															}
															urlMap.put(gLink, pageUrl);
														}

					}

					for (String key : urlMap.keySet()) {
						log.debug("key:::" + key + "value::: " + urlMap.get(key));
					}
					// End Map
					for (Row tempRow : sheet) {

						String gLink = tempRow.getCell(0) != null ? tempRow
								.getCell(0).getStringCellValue() : "";
								String prod = tempRow.getCell(1) != null ? tempRow
										.getCell(1).getStringCellValue() : "";
										String type = tempRow.getCell(2) != null ? tempRow
												.getCell(2).getStringCellValue() : "";
												String cattype = tempRow.getCell(3) != null ? tempRow
														.getCell(3).getStringCellValue() : "";
														String check = tempRow.getCell(4) != null ? tempRow
																.getCell(4).getStringCellValue() : "";

																log.debug("gLink : " + gLink);
																log.debug("prod : " + prod);


																 if ("benefit-var3".equals(type)&&"YES".equalsIgnoreCase(check)) {
																	String msg3 = "";
																	msg3 = msg3 + "<tr>";
																	msg3 = msg3
																			+ new BenefitsVariation03().translate(host, gLink,
																					prod, type, cattype,
																					sheet.getSheetName(), session, urlMap);
																	msg3 = msg3 + "</tr>";

																	sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
																	sb.append(msg3);
																	sb.append("<tr><td colspan='3'>.</td></tr>");

																} else if ("index-var1".equals(type)&&"YES".equalsIgnoreCase(check)) {
																	String msg4 = "";
																	msg4 = msg4 + "<tr>";
																	msg4 = msg4
																			+ new ProductLandingVariation1().translate(
																					host, gLink, prod, type, cattype,
																					sheet.getSheetName(), session, urlMap);
																	msg4 = msg4 + "</tr>";

																	sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
																	sb.append(msg4);
																	sb.append("<tr><td colspan='3'>.</td></tr>");

																} else if ("index-var3".equals(type)&&"YES".equalsIgnoreCase(check)) {
																	String msg5 = "";
																	msg5 = msg5 + "<tr>";
																	msg5 = msg5
																			+ new ProductLandingVariation3().translate(
																					host, gLink, prod, type, cattype,
																					sheet.getSheetName(), session, urlMap);
																	msg5 = msg5 + "</tr>";

																	sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
																	sb.append(msg5);
																	sb.append("<tr><td colspan='3'>.</td></tr>");

																} else if ("index-var9".equals(type)&&"YES".equalsIgnoreCase(check)) {
																	String msg6 = "";
																	msg6 = msg6 + "<tr>";
																	msg6 = msg6
																			+ new ProductLandingVariation9().translate(
																					host, gLink, prod, type, cattype,
																					sheet.getSheetName(), session, urlMap);
																	msg6 = msg6 + "</tr>";

																	sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
																	sb.append(msg6);
																	sb.append("<tr><td colspan='3'>.</td></tr>");

																} else if ("index-var5".equals(type)&&"YES".equalsIgnoreCase(check)) {
																	String msg7 = "";
																	msg7 = msg7 + "<tr>";
																	msg7 = msg7
																			+ new ProductLandingVariation5().translate(
																					host, gLink, prod, type, cattype,
																					sheet.getSheetName(), session, urlMap);
																	msg7 = msg7 + "</tr>";

																	sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
																	sb.append(msg7);
																	sb.append("<tr><td colspan='3'>.</td></tr>");

																} else if ("index-var11".equals(type)&&"YES".equalsIgnoreCase(check)) {
																	String msg8 = "";
																	msg8 = msg8 + "<tr>";
																	msg8 = msg8
																			+ new ProductLandingVariation11()
																	.translate(host, gLink, prod, type,
																			cattype,
																			sheet.getSheetName(),
																			session, urlMap);
																	msg8 = msg8 + "</tr>";

																	sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
																	sb.append(msg8);
																	sb.append("<tr><td colspan='3'>.</td></tr>");

																}else if ("index-infrastructure".equals(type)&&"YES".equalsIgnoreCase(check)) {
																	String msg4 = "";
																	msg4 = msg4 + "<tr>";
																	msg4 = msg4
																			+ new SubCatVariation2().translate(
																					host, gLink, prod, type, cattype,
																					sheet.getSheetName(), session, urlMap);
																	msg4 = msg4 + "</tr>";

																	sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
																	sb.append(msg4);
																	sb.append("<tr><td colspan='3'>.</td></tr>");

																} 
																else if ("index-var10".equals(type)&&"YES".equalsIgnoreCase(check)) {
																	String msg9 = "";
																	msg9 = msg9 + "<tr>";
																	msg9 = msg9
																			+ new ProductLandingVariation10()
																	.translate(host, gLink, prod, type,
																			cattype,
																			sheet.getSheetName(),
																			session, urlMap);
																	msg9 = msg9 + "</tr>";

																	sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
																	sb.append(msg9);
																	sb.append("<tr><td colspan='3'>.</td></tr>");

																} else if ("index-var6".equals(type)&&"YES".equalsIgnoreCase(check)) {
																	String msg10 = "";
																	msg10 = msg10 + "<tr>";
																	msg10 = msg10
																			+ new ProductLandingVariation6().translate(
																					host, gLink, prod, type, cattype,
																					sheet.getSheetName(), session, urlMap);
																	msg10 = msg10 + "</tr>";

																	sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
																	sb.append(msg10);
																	sb.append("<tr><td colspan='3'>.</td></tr>");

																} else if ("index-var8".equals(type)&&"YES".equalsIgnoreCase(check)) {
																	String msg8 = "";
																	msg8 = msg8 + "<tr>";
																	msg8 = msg8
																			+ new ProductLandingVariation08()
																	.translate(host, gLink, prod, type,
																			cattype,
																			sheet.getSheetName(),
																			session, urlMap);
																	msg8 = msg8 + "</tr>";

																	sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
																	sb.append(msg8);
																	sb.append("<tr><td colspan='3'>.</td></tr>");


																} else if ("index-var12".equals(type)&&"YES".equalsIgnoreCase(check)) {
																	String msg8 = "";
																	msg8 = msg8 + "<tr>";
																	msg8 = msg8
																			+ new ProductLandingVariation12()
																	.translate(host, gLink, prod, type,
																			cattype,
																			sheet.getSheetName(),
																			session, urlMap);
																	msg8 = msg8 + "</tr>";

																	sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
																	sb.append(msg8);
																	sb.append("<tr><td colspan='3'>.</td></tr>");


																}else if ("solution-listing-var2".equals(type)&&"YES".equalsIgnoreCase(check)) {
																	String msg11 = "";
																	msg11 = msg11 + "<tr>";
																	msg11 = msg11
																			+ new SolutionListingVariation2().translate(
																					host, gLink, prod, type, cattype,
																					sheet.getSheetName(), session, urlMap);
																	msg11 = msg11 + "</tr>";

																	sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
																	sb.append(msg11);
																	sb.append("<tr><td colspan='3'>.</td></tr>");

																}
																else if ("solution-listing-var11".equals(type)&&"YES".equalsIgnoreCase(check)) {
																	String msg12 = "";
																	msg12 = msg12 + "<tr>";
																	msg12 = msg12
																			+ new SolutionListingVariation11().translate(
																					host, gLink, prod, type, cattype,
																					sheet.getSheetName(), session, urlMap);
																	msg12 = msg12 + "</tr>";

																	sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
																	sb.append(msg12);
																	sb.append("<tr><td colspan='3'>.</td></tr>");

																}
																else if ("solution-listing-var8".equals(type)&&"YES".equalsIgnoreCase(check)) {
																	String msg10 = "";
																	msg10 = msg10 + "<tr>";
																	msg10 = msg10
																			+ new SolutionListingVariation08().translate(
																					host, gLink, prod, type, cattype,
																					sheet.getSheetName(), session, urlMap);
																	msg10 = msg10 + "</tr>";

																	sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
																	sb.append(msg10);
																	sb.append("<tr><td colspan='3'>.</td></tr>");

																} else if ("solution-listing-var12".equals(type)&&"YES".equalsIgnoreCase(check)) {
																	String msg10 = "";
																	msg10 = msg10 + "<tr>";
																	msg10 = msg10
																			+ new SolutionListingVariation12().translate(
																					host, gLink, prod, type, cattype,
																					sheet.getSheetName(), session, urlMap);
																	msg10 = msg10 + "</tr>";

																	sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
																	sb.append(msg10);
																	sb.append("<tr><td colspan='3'>.</td></tr>");

																} else if ("product-listing-var4".equals(type)&&"YES".equalsIgnoreCase(check)) {
																	String msg14 = "";
																	msg14 = msg14 + "<tr>";
																	msg14 = msg14
																			+ new ProductListingVariation4().translate(
																					host, gLink, prod, type, cattype,
																					sheet.getSheetName(), session);
																	msg14 = msg14 + "</tr>";

																	sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
																	log.debug("Msg14 value is "+msg14);
																	sb.append(msg14);
																	sb.append("<tr><td colspan='3'>.</td></tr>");
																}else if ("product-listing-var3".equals(type)&&"YES".equalsIgnoreCase(check)) {
																	String msg10 = "";
																	msg10 = msg10 + "<tr>";
																	msg10 = msg10
																			+ new ProductListingVariation3().translate(
																					host, gLink, prod, type, cattype,
																					sheet.getSheetName(), session);
																	msg10 = msg10 + "</tr>";

																	sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
																	sb.append(msg10);
																	sb.append("<tr><td colspan='3'>.</td></tr>");

																}else if ("product-listing-var7".equals(type)&&"YES".equalsIgnoreCase(check)) {
																	String msg10 = "";
																	msg10 = msg10 + "<tr>";
																	msg10 = msg10
																			+ new ProductListingVariation7().translate(
																					host, gLink, prod, type, cattype,
																					sheet.getSheetName(), session, urlMap);
																	msg10 = msg10 + "</tr>";

																	sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
																	sb.append(msg10);
																	sb.append("<tr><td colspan='3'>.</td></tr>");

																}else if ("service-listing-var3".equals(type)&&"YES".equalsIgnoreCase(check)) {
																	String msg10 = "";
																	msg10 = msg10 + "<tr>";
																	msg10 = msg10
																			+ new ServiceListingVariation03().translate(
																					host, gLink, prod, type, cattype,
																					sheet.getSheetName(), session,urlMap);
																	msg10 = msg10 + "</tr>";

																	sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
																	sb.append(msg10);
																	sb.append("<tr><td colspan='3'>.</td></tr>");

																}else if ("service-listing-var1".equals(type)&&"YES".equalsIgnoreCase(check)) {
																	String msg10 = "";
																	msg10 = msg10 + "<tr>";
																	msg10 = msg10
																			+ new ServiceListingVariation01().translate(
																					host, gLink, prod, type, cattype,
																					sheet.getSheetName(), session, urlMap);
																	msg10 = msg10 + "</tr>";

																	sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
																	sb.append(msg10);
																	sb.append("<tr><td colspan='3'>.</td></tr>");
																}else if ("service-listing-var2".equals(type)&&"YES".equalsIgnoreCase(check)) {
																	String msg13 = "";
																	msg13 = msg13 + "<tr>";
																	msg13 = msg13
																			+ new ServiceListingVariation02().translate(
																					host, gLink, prod, type, cattype,
																					sheet.getSheetName(), session,urlMap);
																	msg13 = msg13 + "</tr>";

																	sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
																	sb.append(msg13);
																	sb.append("<tr><td colspan='3'>.</td></tr>");
																}

																else if ("product-listing-var6".equals(type)&&"YES".equalsIgnoreCase(check)) {
																	String msg15 = "";
																	msg15 = msg15 + "<tr>";
																	msg15 = msg15
																			+ new ProductListingVariation6().translate(
																					host, gLink, prod, type, cattype,
																					sheet.getSheetName(), session, urlMap);
																	msg15 = msg15 + "</tr>";

																	sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
																	sb.append(msg15);
																	sb.append("<tr><td colspan='3'>.</td></tr>");
																}
																else if ("product-listing-var5".equals(type)&&"YES".equalsIgnoreCase(check)) {
																	String msg15 = "";
																	msg15 = msg15 + "<tr>";
																	msg15 = msg15
																			+ new ProductListingVariation5().translate(
																					host, gLink, prod, type, cattype,
																					sheet.getSheetName(), session, urlMap);
																	msg15 = msg15 + "</tr>";

																	sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
																	sb.append(msg15);
																	sb.append("<tr><td colspan='3'>.</td></tr>");
																}else if ("technology-var6".equals(type)&&"YES".equalsIgnoreCase(check)) {
																	String msg19 = "";
																	msg19 = msg19 + "<tr>";
																	msg19 = msg19
																			+ new TechnologyVariation2().translate(
																					host, gLink, prod, type, cattype,
																					sheet.getSheetName(), session, urlMap);
																	msg19 = msg19 + "</tr>";

																	sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
																	sb.append(msg19);
																	sb.append("<tr><td colspan='3'>.</td></tr>");
																}
																else if ("solution-listing-var9".equals(type)&&"YES".equalsIgnoreCase(check)) {
																	String msg15 = "";
																	msg15 = msg15 + "<tr>";
																	msg15 = msg15
																			+ new SolutionListingVariation09().translate(
																					host, gLink, prod, type, cattype,
																					sheet.getSheetName(), session, urlMap);
																	msg15 = msg15 + "</tr>";

																	sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
																	sb.append(msg15);
																	sb.append("<tr><td colspan='3'>.</td></tr>");
																}else if ("architecture-var1".equals(type)&&"YES".equalsIgnoreCase(check)) {
																	String msg14 = "";
																	msg14 = msg14 + "<tr>";
																	msg14 = msg14
																			+ new ArchitechtureVariation1().translate(
																					host, gLink, prod, type, cattype,
																					sheet.getSheetName(), session, urlMap);
																	msg14 = msg14 + "</tr>";

																	sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
																	sb.append(msg14);
																	sb.append("<tr><td colspan='3'>.</td></tr>");
																}
																
																else if ("architecture-var3".equals(type)&&"YES".equalsIgnoreCase(check)) {
																	String msg16 = "";
																	msg16 = msg16 + "<tr>";
																	msg16 = msg16
																			+ new ArchitechtureVariation3().translate(
																					host, gLink, prod, type, cattype,
																					sheet.getSheetName(), session, urlMap);
																	msg16 = msg16 + "</tr>";

																	sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
																	sb.append(msg16);
																	sb.append("<tr><td colspan='3'>.</td></tr>");
																}
																else if ("architecture-var4".equals(type)&&"YES".equalsIgnoreCase(check)) {
																	String msg16 = "";
																	msg16 = msg16 + "<tr>";
																	msg16 = msg16
																			+ new ArchitectureVariation04().translate(
																					host, gLink, prod, type, cattype,
																					sheet.getSheetName(), session, urlMap);
																	msg16 = msg16 + "</tr>";

																	sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
																	sb.append(msg16);
																	sb.append("<tr><td colspan='3'>.</td></tr>");
																}else if ("product-listing-Rvar1".equals(type)&&"YES".equalsIgnoreCase(check)) {
																	String msg16 = "";
																	msg16 = msg16 + "<tr>";
																	msg16 = msg16
																			+ new RProductListingVariation1().translate(
																					host, gLink, prod, type, cattype,
																					sheet.getSheetName(), session, urlMap);
																	msg16 = msg16 + "</tr>";

																	sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
																	sb.append(msg16);
																	sb.append("<tr><td colspan='3'>.</td></tr>");
																}else if ("product-listing-Rvar2".equals(type)&&"YES".equalsIgnoreCase(check)) {
																	String msg16 = "";
																	msg16 = msg16 + "<tr>";
																	msg16 = msg16
																			+ new RProductListingVariation2().translate(
																					host, gLink, prod, type, cattype,
																					sheet.getSheetName(), session, urlMap);
																	msg16 = msg16 + "</tr>";

																	sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
																	sb.append(msg16);
																	sb.append("<tr><td colspan='3'>.</td></tr>");
																}else if ("index-Rrootvar1".equals(type)&&"YES".equalsIgnoreCase(check)) {
																	String msg20 = "";
																	msg20 = msg20 + "<tr>";
																	msg20 = msg20
																			+ new RProductVariation1().translate(
																					host, gLink, prod, type, cattype,
																					sheet.getSheetName(), session, urlMap);
																	msg20 = msg20 + "</tr>";

																	sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
																	sb.append(msg20);
																	sb.append("<tr><td colspan='3'>.</td></tr>");
																}


																else if ("buyersguide-var1".equals(type)&&"YES".equalsIgnoreCase(check)) {
																	String msg16 = "";
																	msg16 = msg16 + "<tr>";
																	msg16 = msg16
																			+ new BuyersGuideVariation01().translate(
																					host, gLink, prod, type, cattype,
																					sheet.getSheetName(), session, urlMap);
																	msg16 = msg16 + "</tr>";

																	sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
																	sb.append(msg16);
																	sb.append("<tr><td colspan='3'>.</td></tr>");
																}
																else if ("service-listing-Rvar1".equals(type)&&"YES".equalsIgnoreCase(check)) {
																	String msg16 = "";
																	msg16 = msg16 + "<tr>";
																	msg16 = msg16
																			+ new RServiceListingVariation1().translate(
																					host, gLink, prod, type, cattype,
																					sheet.getSheetName(), session,urlMap);
																	msg16 = msg16 + "</tr>";

																	sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
																	sb.append(msg16);
																	sb.append("<tr><td colspan='3'>.</td></tr>");
																}
																else if ("solution-listing-Rvar1".equals(type)&&"YES".equalsIgnoreCase(check)) {
																	String msg16 = "";
																	msg16 = msg16 + "<tr>";
																	msg16 = msg16
																			+ new RSolutionListingVariation01().translate(
																					host, gLink, prod, type, cattype,
																					sheet.getSheetName(), session, urlMap);
																	msg16 = msg16 + "</tr>";

																	sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
																	sb.append(msg16);
																	sb.append("<tr><td colspan='3'>.</td></tr>");
																}
																else if ("solution-listing-Rvar2".equals(type)&&"YES".equalsIgnoreCase(check)) {
																	String msg16 = "";
																	msg16 = msg16 + "<tr>";
																	msg16 = msg16
																			+ new RSolutionListingVariation02().translate(
																					host, gLink, prod, type, cattype,
																					sheet.getSheetName(), session, urlMap);
																	msg16 = msg16 + "</tr>";

																	sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
																	sb.append(msg16);
																	sb.append("<tr><td colspan='3'>.</td></tr>");
																}else if ("technology-rvar1".equals(type)&&"YES".equalsIgnoreCase(check)) {
																	String msg16 = "";
																	msg16 = msg16 + "<tr>";
																	msg16 = msg16
																			+ new RTechnologyVariation1().translate(
																					host, gLink, prod, type, cattype,
																					sheet.getSheetName(), session, urlMap);
																	msg16 = msg16 + "</tr>";

																	sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
																	sb.append(msg16);
																	sb.append("<tr><td colspan='3'>.</td></tr>");
																}
																else if ("index-Rrootvar2".equals(type)&&"YES".equalsIgnoreCase(check)) {
																	String msg16 = "";
																	msg16 = msg16 + "<tr>";
																	msg16 = msg16
																			+ new RSolutionIndex().translate(
																					host, gLink, prod, type, cattype,
																					sheet.getSheetName(), session);
																	msg16 = msg16 + "</tr>";

																	sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
																	sb.append(msg16);
																	sb.append("<tr><td colspan='3'>.</td></tr>");
																}
																else if ("index-Rvar1".equals(type)&&"YES".equalsIgnoreCase(check)) {
																	String msg16 = "";
																	msg16 = msg16 + "<tr>";
																	msg16 = msg16
																			+ new RProductLandingVariation1().translate(
																					host, gLink, prod, type, cattype,
																					sheet.getSheetName(), session,urlMap);
																	msg16 = msg16 + "</tr>";

																	sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
																	sb.append(msg16);
																	sb.append("<tr><td colspan='3'>.</td></tr>");
																}
																else if ("index-Rvar2".equals(type)&&"YES".equalsIgnoreCase(check)) {
																	String msg16 = "";
																	msg16 = msg16 + "<tr>";
																	msg16 = msg16
																			+ new RProductLandingVariation2().translate(
																					host, gLink, prod, type, cattype,
																					sheet.getSheetName(), session,urlMap);
																	msg16 = msg16 + "</tr>";

																	sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
																	sb.append(msg16);
																	sb.append("<tr><td colspan='3'>.</td></tr>");
																}
																else if ("benefit-var2".equals(type)&&"YES".equalsIgnoreCase(check)) {
																	String msg16 = "";
																	msg16 = msg16 + "<tr>";
																	msg16 = msg16
																			+ new BenifitsVariation2().translate(
																					host, gLink, prod, type, cattype,
																					sheet.getSheetName(), session,urlMap);
																	msg16 = msg16 + "</tr>";

																	sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
																	sb.append(msg16);
																	sb.append("<tr><td colspan='3'>.</td></tr>");
																}
																else if ("buyersguide-var2".equals(type)&&"YES".equalsIgnoreCase(check)) {
																	String msg16 = "";
																	msg16 = msg16 + "<tr>";
																	msg16 = msg16
																			+ new BuyersGuideVariation02().translate(
																					host, gLink, prod, type, cattype,
																					sheet.getSheetName(), session, urlMap);
																	msg16 = msg16 + "</tr>";

																	sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
																	sb.append(msg16);
																	sb.append("<tr><td colspan='3'>.</td></tr>");
																}
																else if ("buyersguide-var3".equals(type)&&"YES".equalsIgnoreCase(check)) {
																	String msg16 = "";
																	msg16 = msg16 + "<tr>";
																	msg16 = msg16
																			+ new BuyersGuideVariation03().translate(
																					host, gLink, prod, type, cattype,
																					sheet.getSheetName(), session, urlMap);
																	msg16 = msg16 + "</tr>";

																	sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
																	sb.append(msg16);
																	sb.append("<tr><td colspan='3'>.</td></tr>");
																}else if ("benefit-Rvar1".equals(type)&&"YES".equalsIgnoreCase(check)) {
																	String msg16 = "";
																	msg16 = msg16 + "<tr>";
																	msg16 = msg16
																			+ new RBenefitVariation1().translate(
																					host, gLink, prod, type, cattype,
																					sheet.getSheetName(), session, urlMap);
																	msg16 = msg16 + "</tr>";

																	sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
																	sb.append(msg16);
																	sb.append("<tr><td colspan='3'>.</td></tr>");
																}
																else if ("service-listing-Rvar2".equals(type)&&"YES".equalsIgnoreCase(check)) {
																	String msg16 = "";
																	msg16 = msg16 + "<tr>";
																	msg16 = msg16
																			+ new RServiceListingVariation2().translate(
																					host, gLink, prod, type, cattype,
																					sheet.getSheetName(), session, urlMap);
																	msg16 = msg16 + "</tr>";

																	sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
																	sb.append(msg16);
																	sb.append("<tr><td colspan='3'>.</td></tr>");
																}else if ("training-events-var1".equals(type)&&"YES".equalsIgnoreCase(check)) {
																	String msg16 = "";
																	msg16 = msg16 + "<tr>";
																	msg16 = msg16
																			+ new TrainingAndEventsVariation1().translate(
																					host, gLink, prod, type, cattype,
																					sheet.getSheetName(), session, urlMap);
																	msg16 = msg16 + "</tr>";

																	sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
																	sb.append(msg16);
																	sb.append("<tr><td colspan='3'>.</td></tr>");
																}else if ("partners-var1".equals(type)&&"YES".equalsIgnoreCase(check)) {
																	String msg16 = "";
																	msg16 = msg16 + "<tr>";
																	msg16 = msg16
																			+ new PartnerVariation1().translate(
																					host, gLink, prod, type, cattype,
																					sheet.getSheetName(), session, urlMap);
																	msg16 = msg16 + "</tr>";

																	sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
																	sb.append(msg16);
																	sb.append("<tr><td colspan='3'>.</td></tr>");
																}else if ("index-internetvar1".equals(type)&&"YES".equalsIgnoreCase(check)) {
																String msg16 = "";
																msg16 = msg16 + "<tr>";
																msg16 = msg16
																		+ new SubCatVariation1().translate(
																				host, gLink, prod, type, cattype,
																				sheet.getSheetName(), session, urlMap);
																msg16 = msg16 + "</tr>";

																sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
																sb.append(msg16);
																sb.append("<tr><td colspan='3'>.</td></tr>");
															}else if ("index-videoscapevar3".equals(type)&&"YES".equalsIgnoreCase(check)) {
																String msg16 = "";
																msg16 = msg16 + "<tr>";
																msg16 = msg16
																		+ new SubCatVariation3().translate(
																				host, gLink, prod, type, cattype,
																				sheet.getSheetName(), session, urlMap);
																msg16 = msg16 + "</tr>";

																sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
																sb.append(msg16);
																sb.append("<tr><td colspan='3'>.</td></tr>");
															} else if ("overview-WebVar3".equals(type) && "YES".equalsIgnoreCase(check)) {
																String msg17 = "";
																msg17 = msg17 + "<tr>";
																msg17 = msg17
																		+ new WebVariation3().translate(
																				host, gLink, prod, type, cattype,
																				sheet.getSheetName(), session, urlMap);
																msg17 = msg17 + "</tr>";

																sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
																sb.append(msg17);
																sb.append("<tr><td colspan='3'>.</td></tr>");
															}else if ("overview-webvar2".equals(type)&&"YES".equalsIgnoreCase(check)) {
																String msg16 = "";
																msg16 = msg16 + "<tr>";
																msg16 = msg16
																		+ new WebVariation2().translate(
																				host, gLink, prod, type, cattype,
																				sheet.getSheetName(), session, urlMap);
																msg16 = msg16 + "</tr>";

																sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
																sb.append(msg16);
																sb.append("<tr><td colspan='3'>.</td></tr>");
															}else if ("private-hybrid-solutions-webvar6".equals(type)&&"YES".equalsIgnoreCase(check)) {
																String msg16 = "";
																msg16 = msg16 + "<tr>";
																msg16 = msg16
																		+ new WebVariation6().translate(
																				host, gLink, prod, type, cattype,
																				sheet.getSheetName(), session, urlMap);
																msg16 = msg16 + "</tr>";

																sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
																sb.append(msg16);
																sb.append("<tr><td colspan='3'>.</td></tr>");
															}else if ("managed-cloud-services-pswebvar5".equals(type)&&"YES".equalsIgnoreCase(check)) {
																String msg16 = "";
																msg16 = msg16 + "<tr>";
																msg16 = msg16
																		+ new WebVariation5().translate(
																				host, gLink, prod, type, cattype,
																				sheet.getSheetName(), session, urlMap);
																msg16 = msg16 + "</tr>";

																sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
																sb.append(msg16);
																sb.append("<tr><td colspan='3'>.</td></tr>");
															}else if ("about-webvar1".equals(type)&&"YES".equalsIgnoreCase(check)) {
																String msg16 = "";
																msg16 = msg16 + "<tr>";
																msg16 = msg16
																		+ new WebVariation1().translate(
																				host, gLink, prod, type, cattype,
																				sheet.getSheetName(), session, urlMap);
																msg16 = msg16 + "</tr>";

																sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
																sb.append(msg16);
																sb.append("<tr><td colspan='3'>.</td></tr>");
															}
															else if ("featured-case-studies-webvar9".equals(type)&&"YES".equalsIgnoreCase(check)) {
																String msg16 = "";
																msg16 = msg16 + "<tr>";
																msg16 = msg16
																		+ new WebVariation9().translate(
																				host, gLink, prod, type, cattype,
																				sheet.getSheetName(), session, urlMap);
																msg16 = msg16 + "</tr>";

																sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
																sb.append(msg16);
																sb.append("<tr><td colspan='3'>.</td></tr>");
															}
															else if ("energy-webvar7".equals(type)&&"YES".equalsIgnoreCase(check)) {
																String msg16 = "";
																msg16 = msg16 + "<tr>";
																msg16 = msg16
																		+ new WebVariation7().translate(
																				host, gLink, prod, type, cattype,
																				sheet.getSheetName(), session, urlMap);
																msg16 = msg16 + "</tr>";

																sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
																sb.append(msg16);
																sb.append("<tr><td colspan='3'>.</td></tr>");
															}
															else if ("retail-webvar7".equals(type)&&"YES".equalsIgnoreCase(check)) {
																String msg16 = "";
																msg16 = msg16 + "<tr>";
																msg16 = msg16
																		+ new WebVariation7().translate(
																				host, gLink, prod, type, cattype,
																				sheet.getSheetName(), session, urlMap);
																msg16 = msg16 + "</tr>";

																sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
																sb.append(msg16);
																sb.append("<tr><td colspan='3'>.</td></tr>");
															}
															else if ("government-webvar7".equals(type)&&"YES".equalsIgnoreCase(check)) {
																String msg16 = "";
																msg16 = msg16 + "<tr>";
																msg16 = msg16
																		+ new WebVariation7().translate(
																				host, gLink, prod, type, cattype,
																				sheet.getSheetName(), session, urlMap);
																msg16 = msg16 + "</tr>";

																sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
																sb.append(msg16);
																sb.append("<tr><td colspan='3'>.</td></tr>");
															}
															else if ("manufacturing-webvar7".equals(type)&&"YES".equalsIgnoreCase(check)) {
																String msg16 = "";
																msg16 = msg16 + "<tr>";
																msg16 = msg16
																		+ new WebVariation7().translate(
																				host, gLink, prod, type, cattype,
																				sheet.getSheetName(), session, urlMap);
																msg16 = msg16 + "</tr>";

																sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
																sb.append(msg16);
																sb.append("<tr><td colspan='3'>.</td></tr>");
															}
															else if ("financial-services-webvar7".equals(type)&&"YES".equalsIgnoreCase(check)) {
																String msg16 = "";
																msg16 = msg16 + "<tr>";
																msg16 = msg16
																		+ new WebVariation7().translate(
																				host, gLink, prod, type, cattype,
																				sheet.getSheetName(), session, urlMap);
																msg16 = msg16 + "</tr>";

																sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
																sb.append(msg16);
																sb.append("<tr><td colspan='3'>.</td></tr>");
															}
															else if ("industries-webvar8".equals(type)&&"YES".equalsIgnoreCase(check)) {
																String msg16 = "";
																msg16 = msg16 + "<tr>";
																msg16 = msg16
																		+ new WebVariation8().translate(
																				host, gLink, prod, type, cattype,
																				sheet.getSheetName(), session, urlMap);
																msg16 = msg16 + "</tr>";

																sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
																sb.append(msg16);
																sb.append("<tr><td colspan='3'>.</td></tr>");
															}





					}

					sb.append("</table></body></html>");


					java.util.Date date = new java.util.Date();
					File file = new File(reportspath
							+ "/OVWMigrationReport_"
							+ sheetName
							+ "_"
							+ new Timestamp(date.getTime()).toString()
							.replace(":", "-").replace(".", "-")
							+ ".html");
					/*FileWriter fileWriter = new FileWriter(file);
					BufferedWriter bwr = new BufferedWriter(fileWriter);
					bwr.write(new String(sb.toString().getBytes("UTF-8")));
					bwr.flush();
					bwr.close();*/
					FileOutputStream fileOutputStream = new FileOutputStream( file );
					OutputStreamWriter outputStreamWriter = new OutputStreamWriter( fileOutputStream, "UTF-16" );
					BufferedWriter bufferedWriter = new BufferedWriter( outputStreamWriter );
					bufferedWriter.write(sb.toString());

					bufferedWriter.flush();
					bufferedWriter.close();

				}
				workbook.close();
				session.logout();
			} else {
				log.debug("config.properties file is not configured with 'serverurl' or 'aemuser' or 'aempassword' or 'workspace' or 'workbookpath' or 'reportspath'");
			}

		} catch (Exception e) {
			log.error("Exception in main of OVWMigration : ", e);
		}
	}
}
