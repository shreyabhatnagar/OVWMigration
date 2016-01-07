package com.cisco.dse.global.migration.config;

import org.apache.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public abstract class BaseAction {

	static Logger log = Logger.getLogger(BaseAction.class);

	int count = 0;

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
}
