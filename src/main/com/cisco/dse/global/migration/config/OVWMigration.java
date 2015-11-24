package com.cisco.dse.global.migration.config;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;

import javax.jcr.Repository;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.cisco.dse.global.migration.benefit.Benefits;
import com.cisco.dse.global.migration.benefit.ServiceProviderBenefits;
import com.cisco.dse.global.migration.benefit.UnifiedComputingBenefits;
import com.cisco.dse.global.migration.productlanding.ProductLandingVariation2;
import com.cisco.dse.global.migration.productlanding.ProductLandingVariation9;


public class OVWMigration {
	
		
	static Repository repository = null;
	static Session session = null;
	static Logger log = Logger.getLogger(OVWMigration.class);
	public static void main(String s[]) throws FileNotFoundException,
			IOException {
		
		log.debug("---------------------inside main--------------------------");	
		String REPO = "http://chard.cisco.com:4502/crx/server";
		String WORKSPACE = "crx.default";
		// OVWMigrator mig=null;

		try {			
			repository = JcrUtils.getRepository(REPO);			
			
			session = repository.login(
					new SimpleCredentials("admin", "admin".toCharArray()),
					WORKSPACE);
		    XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream(
				"c:/test/OVWDEMO.xlsx"));

			for (XSSFSheet sheet : workbook) {
				// int sheetIndex = 0;
				// XSSFSheet sheet = workbook.getSheetAt(sheetIndex);
				log.debug("SHEET NAME:::::::::::" + sheet.getSheetName());
				String sheetName = sheet.getSheetName();
				String msg = "";
				String msg1="";
				String msg2="";
				String msg3="";
				String msg4="";
				String msg5="";
				String msg6="";
				StringBuilder sb = new StringBuilder(1024);
				
				for (Row tempRow : sheet) {
					
					String gLink = tempRow.getCell(0)!=null?tempRow.getCell(0).getStringCellValue():"";
					String prod = tempRow.getCell(1)!=null?tempRow.getCell(1).getStringCellValue():"";
					String type = tempRow.getCell(2)!=null?tempRow.getCell(2).getStringCellValue():"";
					String cattype = tempRow.getCell(3)!=null?tempRow.getCell(3).getStringCellValue():"";
					
					log.debug("gLink : "+gLink);
														
					System.out.println("gLink : "+gLink);
					if ("servers-unified-computing1".equalsIgnoreCase(prod)) {
						msg1 = msg1+"<tr>";
						msg1 = msg1 + new UnifiedComputingBenefits().translate(gLink, prod, type,
							sheet.getSheetName(), session);
						 msg1 = msg1+"</tr>";
					} else if ("service-provider1".equalsIgnoreCase(prod)) {
						msg2 = msg2+"<tr>";
						 msg2 = msg2 + new ServiceProviderBenefits().translate(gLink, prod, type,
							sheet.getSheetName(), session);
						 msg2 = msg2+"</tr>";
					} else if("var1".equals(type)){
						msg3 = msg3 + "<tr>";
						msg3 = msg3 + new Benefits().translate(gLink, prod, type,cattype,
							sheet.getSheetName(), session);
						msg3 = msg3 + "</tr>";
//					} else if ("index-var1".equals(type)) {
//						msg4 = msg4 + "<tr>";
//						msg4 = msg4 + new ProductLandingVariation1().translate(gLink, prod, type,cattype,
//							sheet.getSheetName(), session);
//						msg4 = msg4 + "</tr>";
					}
					else if ("index-var2".equals(type)) {
						msg5 = msg5 + "<tr>";
						msg5 = msg5 + new ProductLandingVariation2().translate(gLink, prod, type,cattype,
							sheet.getSheetName(), session);
						msg5 = msg5 + "</tr>";
					}
					else if("index-var9".equals(type)){
					msg6 = msg6 + "<tr>";
					msg6 = msg6 + new ProductLandingVariation9().translate(gLink, prod, type,cattype,
						sheet.getSheetName(), session);
					msg6 = msg6 + "</tr>";
				}
					/*
					Cell conceptCell = tempRow.createCell(3);
					conceptCell.setCellValue(msg);*/
				}
				
				//sb.append("<style>td {width: 400px;} table{widht: 400px;}</style>");
				
				
				sb.append("<table widht='500' border='1'>");
				sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
				sb.append(msg1);
				sb.append("<tr><td colspan='3'>.</td></tr>");
				sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
				sb.append(msg2);
				sb.append("<tr><td colspan='3'>.</td></tr>");
				sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
				sb.append(msg3);
				sb.append("<tr><td colspan='3'>.</td></tr>");
				sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
				sb.append(msg4);
				sb.append("<tr><td colspan='3'>.</td></tr>");
				sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
				sb.append(msg5);
				sb.append("<tr><td colspan='3'>.</td></tr>");
				sb.append("<tr bgcolor='#888888'><th style='width:500px'>WEM url</th><th style='width:500px'>Web Publisher url</th><th style='width:500px'>Comments</th></tr>");
				sb.append(msg6);
				sb.append("<tr><td colspan='3'>.</td></tr>");

				sb.append("</table>");
				
				
				java.util.Date date = new java.util.Date();
				File file = new File("c:/test/OVWMigrationReport_"
						+ sheetName
						+ "_"
						+ new Timestamp(date.getTime()).toString()
								.replace(":", "-").replace(".", "-") + ".html");
				FileWriter fileWriter = new FileWriter(file);
				BufferedWriter bwr = new BufferedWriter(fileWriter);
				bwr.write(sb.toString());
				bwr.flush();
				bwr.close();
				
			}
			/*FileOutputStream outFile = new FileOutputStream(new File("c:/test/OVWDEMO.xlsx"));
			workbook.write(outFile);
			outFile.close();*/
			workbook.close();
			session.logout();		
			
		} catch (Exception e) {
			log.error("inside main",e);
			
		}
	}
}
