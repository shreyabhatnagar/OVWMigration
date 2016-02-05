package com.cisco.dse.global.migration.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public abstract class BaseAction {

	static Logger log = Logger.getLogger(BaseAction.class);
	protected Document getConnection(String loc) {
		Document doc = null;
		try {			
			log.debug("Inside the getConnection method.");
			for (int retry=0; retry<10; retry++) {
				log.debug("Trying to establish connection to "+loc+". Connection retry count is : "+retry);
				Connection connection = Jsoup.connect(loc).timeout(10000);
				if (connection != null) {
					try {
						doc = connection.get();
						log.debug("Connection established!!!");
						break;
					} catch (Exception e) {
						log.error("Exception : ",e);
						log.debug("As document is not retrieved due to above exception, connection retry loop will continue.");
					} finally{
						if(connection != null){
							connection = null;
						}
					}
				}
				Thread.sleep(3000);
			}
		}catch (Exception e) {
			log.error("Exception : ", e);
		}
		return doc;
	}
	
	protected Document getSecuredConnection(String loc) {
		
		Properties prop = new Properties();
		InputStream input = null;
		String ciscoid = null;
		String ciscopwd = null;
		try {
			String filename = "config.properties";
			input = OVWMigration.class.getClassLoader().getResourceAsStream(filename);
			if (input == null) {
				log.debug("input is null");
			}
			// load a properties file from class path, inside static method
			prop.load(input);

			ciscoid = StringUtils.isNotBlank(prop.getProperty("ciscoid")) ? prop.getProperty("ciscoid") : "";
			ciscopwd = StringUtils.isNotBlank(prop.getProperty("ciscopwd")) ? prop.getProperty("ciscopwd") : "";
			log.debug("ciscoid : "+ciscoid);
			log.debug("ciscopwd : "+ciscopwd);
			
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
		
		
		Document doc = null;
		try {			
			log.debug("Inside the getSecuredConnection method to connect to : "+loc);
			Connection connection = null;
			Map<String, String> cookies = null;
			
			String loginUrl = "https://sso.cisco.com/autho/login/loginaction.html";
			if(StringUtils.isNotBlank(ciscoid) && StringUtils.isNotBlank(ciscopwd)){
			for (int retry=0; retry<10; retry++) {
				Connection.Response res = null;
				log.debug("Trying to login to "+loginUrl+". Connection retry count is : "+retry);
				try{
					log.debug("trying to connect with credentials");
					res = Jsoup.connect(loginUrl)
							.timeout(10000)
							.data("userid", ciscoid, "password", ciscopwd)
							.method(Method.POST)
							.execute();
				}
				catch(Exception e){
					log.error("Exception : ",e);
					log.error("Unable to login trying again.");
				}
				if (res != null) {
					try {
						log.debug("Connection established!!!");
						cookies = res.cookies();      
						break;
					}
					catch (Exception e) {
						log.error("Exception : ",e);
						log.debug("Unable to login trying again.");
					}
				}
				Thread.sleep(3000);
			}
			for (int retry=0; retry<10; retry++) {
				log.debug("Trying to establish connection to "+loc+". Connection retry count is : "+retry);
				connection = Jsoup.connect(loc).cookies(cookies);
				if (connection != null) {
					try {
						log.debug("Connection established!!!");
						doc = connection.get();
						break;
					}
					catch (Exception e) {
						log.error("Exception : ",e);
						log.debug("Unable to connect to the doc.");
					}
				}
				Thread.sleep(3000);
			}
			}else{
				log.debug("please configure ciscoid and ciscoppwd");
			}
		}catch (Exception e) {
			log.error("Exception : ", e);
		}
		return doc;
	}
}
