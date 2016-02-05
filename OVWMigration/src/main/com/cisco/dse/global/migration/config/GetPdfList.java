package com.cisco.dse.global.migration.config;

import java.io.FileInputStream;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class GetPdfList {
	public static void main(String[] args) {
		try {
			XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream(
					"c:/test/OVWDEMO_PDF.xlsx"));
			for (XSSFSheet sheet : workbook) {
				for (Row tempRow : sheet) {
					String gLink = tempRow.getCell(0) != null ? tempRow
							.getCell(0).getStringCellValue() : "";
					try {
						Document doc = getConnection(gLink);
						Elements links1 = doc.select("a[href]");
						System.out.println("--------------------------------------------------------");
						System.out.println("Url : " + gLink);
						for (Element ele : links1) {
							String link = ele.attr("href");
							if (link.endsWith(".pdf")) {
								System.out.println(link);
							}
						}
					} catch (Exception e) {
						System.out.println("Exception : " + e);
					}
				}
			}
		} catch (Exception e) {
			System.out.println("Exception" + e);
		}
	}
	public static Document getConnection(String loc) {
		Document doc = null;
		try {
			for (int retry = 0; retry < 10; retry++) {
				Connection connection = Jsoup.connect(loc).timeout(10000);
				if (connection != null) {
					try {
						doc = connection.get();
						break;
					} catch (Exception e) {
						System.out.println("Exception : " + e);
					} finally {
						if (connection != null) {
							connection = null;
						}
					}
				}
				Thread.sleep(3000);
			}
		} catch (Exception e) {
			System.out.println("Exception : " + e);
		}
		return doc;
	}
}
