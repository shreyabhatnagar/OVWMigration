package com.cisco.dse.global.migration.config;

import java.util.Map;

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
			Connection connection = null;
			for (int retry=0; retry<10; retry++) {
				log.debug("Trying to establish connection to "+loc+". Connection retry count is : "+retry);
				connection = Jsoup.connect(loc);
				if (connection != null) {
					try {
						log.debug("Connection established!!!");
						doc = connection.get();
						break;
					}
					catch (Exception e) {
						log.error("Exception : ",e);
						log.debug("As document is not retrieved due to above exception, connection retry loop will continue.");
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
		Document doc = null;
		try {			
			log.debug("Inside the getSecuredConnection method to connect to : "+loc);
			Connection connection = null;
			Map<String, String> cookies = null;
			
			String loginUrl = "https://sso.cisco.com/autho/login/loginaction.html";
			
			for (int retry=0; retry<10; retry++) {
				log.debug("Trying to login to "+loginUrl+". Connection retry count is : "+retry);
				Connection.Response res = Jsoup.connect(loginUrl)
					    .data("userid", "sbasta", "password", "Qwer1234$")
					    .method(Method.POST)
					    .execute();
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
		}catch (Exception e) {
			log.error("Exception : ", e);
		}

		return doc;
	}
}
