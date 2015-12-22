package com.cisco.dse.global.migration.config;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
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

import com.cisco.dse.global.migration.benefit.Benefits;
import com.cisco.dse.global.migration.benefit.BenifitsVariation4;
import com.cisco.dse.global.migration.benefit.ServiceProviderBenefits;
import com.cisco.dse.global.migration.benefit.UnifiedComputingBenefits;
import com.cisco.dse.global.migration.buyersguide.BuyersGuideVariation01;
import com.cisco.dse.global.migration.buyersguide.BuyersGuideVariation02;
import com.cisco.dse.global.migration.buyersguide.BuyersGuideVariation03;
import com.cisco.dse.global.migration.architechture.ArchitechtureVariation1;
import com.cisco.dse.global.migration.architechture.ArchitechtureVariation3;
import com.cisco.dse.global.migration.architechture.ArchitectureVariation04;
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
import com.cisco.dse.global.migration.rservicelisting.RServiceListingVariation1;
import com.cisco.dse.global.migration.servicelisting.ServiceListingVariation01;
import com.cisco.dse.global.migration.servicelisting.ServiceListingVariation02;
import com.cisco.dse.global.migration.servicelisting.ServiceListingVariation03;
import com.cisco.dse.global.migration.solutionlisting.SolutionListingVariation08;
import com.cisco.dse.global.migration.solutionlisting.SolutionListingVariation09;
import com.cisco.dse.global.migration.solutionlisting.SolutionListingVariation11;
import com.cisco.dse.global.migration.solutionlisting.SolutionListingVariation12;
import com.cisco.dse.global.migration.solutionlisting.SolutionListingVariation2;
import com.cisco.dse.global.migration.technology.TechnologyVariation2;

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

					sb.append("<html><head><meta charset='UTF-8'></head><body><table  border='1'>");

					for (Row tempRow : sheet) {

						String gLink = tempRow.getCell(0) != null ? tempRow
								.getCell(0).getStringCellValue() : "";
								String prod = tempRow.getCell(1) != null ? tempRow
										.getCell(1).getStringCellValue() : "";
										String type = tempRow.getCell(2) != null ? tempRow
												.getCell(2).getStringCellValue() : "";
												String cattype = tempRow.getCell(3) != null ? tempRow
														.getCell(3).getStringCellValue() : "";

														log.debug("gLink : " + gLink);
														log.debug("prod : " + prod);


														if ("benefit-var1".equalsIgnoreCase(prod)) {
															String msg1 = "";
															msg1 = msg1 + "<tr>";
															msg1 = msg1
																	+ new UnifiedComputingBenefits().translate(
																			host, gLink, prod, type,
																			sheet.getSheetName(), session);
															msg1 = msg1 + "</tr>";

															sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
															sb.append(msg1);
															sb.append("<tr><td colspan='3'>.</td></tr>");

														} else if ("benefit-var2".equalsIgnoreCase(prod)) {
															String msg2 = "";
															msg2 = msg2 + "<tr>";
															msg2 = msg2
																	+ new ServiceProviderBenefits().translate(
																			host, gLink, prod, type,
																			sheet.getSheetName(), session);
															msg2 = msg2 + "</tr>";

															sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
															sb.append(msg2);
															sb.append("<tr><td colspan='3'>.</td></tr>");

														} else if ("benefit-var3".equals(type)) {
															String msg3 = "";
															msg3 = msg3 + "<tr>";
															msg3 = msg3
																	+ new Benefits().translate(host, gLink,
																			prod, type, cattype,
																			sheet.getSheetName(), session);
															msg3 = msg3 + "</tr>";

															sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
															sb.append(msg3);
															sb.append("<tr><td colspan='3'>.</td></tr>");

														} else if ("index-var1".equals(type)) {
															String msg4 = "";
															msg4 = msg4 + "<tr>";
															msg4 = msg4
																	+ new ProductLandingVariation1().translate(
																			host, gLink, prod, type, cattype,
																			sheet.getSheetName(), session);
															msg4 = msg4 + "</tr>";

															sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
															sb.append(msg4);
															sb.append("<tr><td colspan='3'>.</td></tr>");

														} else if ("index-var3".equals(type)) {
															String msg5 = "";
															msg5 = msg5 + "<tr>";
															msg5 = msg5
																	+ new ProductLandingVariation3().translate(
																			host, gLink, prod, type, cattype,
																			sheet.getSheetName(), session);
															msg5 = msg5 + "</tr>";

															sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
															sb.append(msg5);
															sb.append("<tr><td colspan='3'>.</td></tr>");

														} else if ("index-var9".equals(type)) {
															String msg6 = "";
															msg6 = msg6 + "<tr>";
															msg6 = msg6
																	+ new ProductLandingVariation9().translate(
																			host, gLink, prod, type, cattype,
																			sheet.getSheetName(), session);
															msg6 = msg6 + "</tr>";

															sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
															sb.append(msg6);
															sb.append("<tr><td colspan='3'>.</td></tr>");

														} else if ("index-var5".equals(type)) {
															String msg7 = "";
															msg7 = msg7 + "<tr>";
															msg7 = msg7
																	+ new ProductLandingVariation5().translate(
																			host, gLink, prod, type, cattype,
																			sheet.getSheetName(), session);
															msg7 = msg7 + "</tr>";

															sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
															sb.append(msg7);
															sb.append("<tr><td colspan='3'>.</td></tr>");

														} else if ("index-var11".equals(type)) {
															String msg8 = "";
															msg8 = msg8 + "<tr>";
															msg8 = msg8
																	+ new ProductLandingVariation11()
															.translate(host, gLink, prod, type,
																	cattype,
																	sheet.getSheetName(),
																	session);
															msg8 = msg8 + "</tr>";

															sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
															sb.append(msg8);
															sb.append("<tr><td colspan='3'>.</td></tr>");

														} else if ("index-var10".equals(type)) {
															String msg9 = "";
															msg9 = msg9 + "<tr>";
															msg9 = msg9
																	+ new ProductLandingVariation10()
															.translate(host, gLink, prod, type,
																	cattype,
																	sheet.getSheetName(),
																	session);
															msg9 = msg9 + "</tr>";

															sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
															sb.append(msg9);
															sb.append("<tr><td colspan='3'>.</td></tr>");

														} else if ("index-var6".equals(type)) {
															String msg10 = "";
															msg10 = msg10 + "<tr>";
															msg10 = msg10
																	+ new ProductLandingVariation6().translate(
																			host, gLink, prod, type, cattype,
																			sheet.getSheetName(), session);
															msg10 = msg10 + "</tr>";

															sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
															sb.append(msg10);
															sb.append("<tr><td colspan='3'>.</td></tr>");

														} else if ("index-var8".equals(type)) {
																String msg8 = "";
																msg8 = msg8 + "<tr>";
																msg8 = msg8
																		+ new ProductLandingVariation08()
																.translate(host, gLink, prod, type,
																		cattype,
																		sheet.getSheetName(),
																		session);
																msg8 = msg8 + "</tr>";

																sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
																sb.append(msg8);
																sb.append("<tr><td colspan='3'>.</td></tr>");

															
														} else if ("index-var12".equals(type)) {
															String msg8 = "";
															msg8 = msg8 + "<tr>";
															msg8 = msg8
																	+ new ProductLandingVariation12()
															.translate(host, gLink, prod, type,
																	cattype,
																	sheet.getSheetName(),
																	session);
															msg8 = msg8 + "</tr>";

															sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
															sb.append(msg8);
															sb.append("<tr><td colspan='3'>.</td></tr>");

														
													}else if ("solution-listing-var2".equals(type)) {
															String msg11 = "";
															msg11 = msg11 + "<tr>";
															msg11 = msg11
																	+ new SolutionListingVariation2().translate(
																			host, gLink, prod, type, cattype,
																			sheet.getSheetName(), session);
															msg11 = msg11 + "</tr>";

															sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
															sb.append(msg11);
															sb.append("<tr><td colspan='3'>.</td></tr>");

														}
														else if ("solution-listing-var11".equals(type)) {
															String msg12 = "";
															msg12 = msg12 + "<tr>";
															msg12 = msg12
																	+ new SolutionListingVariation11().translate(
																			host, gLink, prod, type, cattype,
																			sheet.getSheetName(), session);
															msg12 = msg12 + "</tr>";

															sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
															sb.append(msg12);
															sb.append("<tr><td colspan='3'>.</td></tr>");

														}
														else if ("solution-listing-var8".equals(type)) {
															String msg10 = "";
															msg10 = msg10 + "<tr>";
															msg10 = msg10
																	+ new SolutionListingVariation08().translate(
																			host, gLink, prod, type, cattype,
																			sheet.getSheetName(), session);
															msg10 = msg10 + "</tr>";

															sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
															sb.append(msg10);
															sb.append("<tr><td colspan='3'>.</td></tr>");

														} else if ("solution-listing-var12".equals(type)) {
															String msg10 = "";
															msg10 = msg10 + "<tr>";
															msg10 = msg10
																	+ new SolutionListingVariation12().translate(
																			host, gLink, prod, type, cattype,
																			sheet.getSheetName(), session);
															msg10 = msg10 + "</tr>";

															sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
															sb.append(msg10);
															sb.append("<tr><td colspan='3'>.</td></tr>");

														} else if ("product-listing-var4".equals(type)) {
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
														}else if ("product-listing-var3".equals(type)) {
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

														}else if ("product-listing-var7".equals(type)) {
															String msg10 = "";
															msg10 = msg10 + "<tr>";
															msg10 = msg10
																	+ new ProductListingVariation7().translate(
																			host, gLink, prod, type, cattype,
																			sheet.getSheetName(), session);
															msg10 = msg10 + "</tr>";

															sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
															sb.append(msg10);
															sb.append("<tr><td colspan='3'>.</td></tr>");

														}else if ("service-listing-var3".equals(type)) {
															String msg10 = "";
															msg10 = msg10 + "<tr>";
															msg10 = msg10
																	+ new ServiceListingVariation03().translate(
																			host, gLink, prod, type, cattype,
																			sheet.getSheetName(), session);
															msg10 = msg10 + "</tr>";

															sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
															sb.append(msg10);
															sb.append("<tr><td colspan='3'>.</td></tr>");

														}else if ("service-listing-var1".equals(type)) {
															String msg10 = "";
															msg10 = msg10 + "<tr>";
															msg10 = msg10
																	+ new ServiceListingVariation01().translate(
																			host, gLink, prod, type, cattype,
																			sheet.getSheetName(), session);
															msg10 = msg10 + "</tr>";

															sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
															sb.append(msg10);
															sb.append("<tr><td colspan='3'>.</td></tr>");
														}else if ("service-listing-var2".equals(type)) {
															String msg13 = "";
															msg13 = msg13 + "<tr>";
															msg13 = msg13
																	+ new ServiceListingVariation02().translate(
																			host, gLink, prod, type, cattype,
																			sheet.getSheetName(), session);
															msg13 = msg13 + "</tr>";

															sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
															sb.append(msg13);
															sb.append("<tr><td colspan='3'>.</td></tr>");
														}

														else if ("product-listing-var6".equals(type)) {
															String msg15 = "";
															msg15 = msg15 + "<tr>";
															msg15 = msg15
																	+ new ProductListingVariation6().translate(
																			host, gLink, prod, type, cattype,
																			sheet.getSheetName(), session);
															msg15 = msg15 + "</tr>";

															sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
															sb.append(msg15);
															sb.append("<tr><td colspan='3'>.</td></tr>");
														}
														else if ("product-listing-var5".equals(type)) {
															String msg15 = "";
															msg15 = msg15 + "<tr>";
															msg15 = msg15
																	+ new ProductListingVariation5().translate(
																			host, gLink, prod, type, cattype,
																			sheet.getSheetName(), session);
															msg15 = msg15 + "</tr>";

															sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
															sb.append(msg15);
															sb.append("<tr><td colspan='3'>.</td></tr>");
														}else if ("technology-var6".equals(type)) {
															String msg19 = "";
															msg19 = msg19 + "<tr>";
															msg19 = msg19
																	+ new TechnologyVariation2().translate(
																			host, gLink, prod, type, cattype,
																			sheet.getSheetName(), session);
															msg19 = msg19 + "</tr>";

															sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
															sb.append(msg19);
															sb.append("<tr><td colspan='3'>.</td></tr>");
														}
														else if ("solutions-listing-var9".equals(type)) {
															String msg15 = "";
															msg15 = msg15 + "<tr>";
															msg15 = msg15
																	+ new SolutionListingVariation09().translate(
																			host, gLink, prod, type, cattype,
																			sheet.getSheetName(), session);
															msg15 = msg15 + "</tr>";

															sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
															sb.append(msg15);
															sb.append("<tr><td colspan='3'>.</td></tr>");
														}else if ("collaboration-architecture-var1".equals(type)) {
															String msg14 = "";
															msg14 = msg14 + "<tr>";
															msg14 = msg14
																	+ new ArchitechtureVariation1().translate(
																			host, gLink, prod, type, cattype,
																			sheet.getSheetName(), session);
															msg14 = msg14 + "</tr>";

															sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
															sb.append(msg14);
															sb.append("<tr><td colspan='3'>.</td></tr>");
														}
														else if ("benefits-var4".equals(type)) {
															String msg20 = "";
															msg20 = msg20 + "<tr>";
															msg20 = msg20
																	+ new BenifitsVariation4().translate(
																			host, gLink, prod, type, cattype,
																			sheet.getSheetName(), session);
															msg20 = msg20 + "</tr>";

															sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
															sb.append(msg20);
															sb.append("<tr><td colspan='3'>.</td></tr>");
														}
														else if ("collaboration-architecture-var3".equals(type)) {
															String msg16 = "";
															msg16 = msg16 + "<tr>";
															msg16 = msg16
																	+ new ArchitechtureVariation3().translate(
																			host, gLink, prod, type, cattype,
																			sheet.getSheetName(), session);
															msg16 = msg16 + "</tr>";

															sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
															sb.append(msg16);
															sb.append("<tr><td colspan='3'>.</td></tr>");
														}
														else if ("collaboration-architecture-var4".equals(type)) {
															String msg16 = "";
															msg16 = msg16 + "<tr>";
															msg16 = msg16
																	+ new ArchitectureVariation04().translate(
																			host, gLink, prod, type, cattype,
																			sheet.getSheetName(), session);
															msg16 = msg16 + "</tr>";

															sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
															sb.append(msg16);
															sb.append("<tr><td colspan='3'>.</td></tr>");
														}
														else if ("buyersguide-var1".equals(type)) {
															String msg16 = "";
															msg16 = msg16 + "<tr>";
															msg16 = msg16
																	+ new BuyersGuideVariation01().translate(
																			host, gLink, prod, type, cattype,
																			sheet.getSheetName(), session);
															msg16 = msg16 + "</tr>";

															sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
															sb.append(msg16);
															sb.append("<tr><td colspan='3'>.</td></tr>");
														}
														else if ("buyersguide-var3".equals(type)) {
															String msg16 = "";
															msg16 = msg16 + "<tr>";
															msg16 = msg16
																	+ new BuyersGuideVariation03().translate(
																			host, gLink, prod, type, cattype,
																			sheet.getSheetName(), session);
															msg16 = msg16 + "</tr>";

															sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
															sb.append(msg16);
															sb.append("<tr><td colspan='3'>.</td></tr>");
														}
														else if ("buyersguide-var2".equals(type)) {
															String msg16 = "";
															msg16 = msg16 + "<tr>";
															msg16 = msg16
																	+ new BuyersGuideVariation02().translate(
																			host, gLink, prod, type, cattype,
																			sheet.getSheetName(), session);
															msg16 = msg16 + "</tr>";

															sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
															sb.append(msg16);
															sb.append("<tr><td colspan='3'>.</td></tr>");
														}else if ("rservice-listing-var1".equals(type)) {
															String msg16 = "";
															msg16 = msg16 + "<tr>";
															msg16 = msg16
																	+ new RServiceListingVariation1().translate(
																			host, gLink, prod, type, cattype,
																			sheet.getSheetName(), session);
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
					FileWriter fileWriter = new FileWriter(file);
					BufferedWriter bwr = new BufferedWriter(fileWriter);
					bwr.write(new String(sb.toString().getBytes("UTF-8")));
					bwr.flush();
					bwr.close();

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
