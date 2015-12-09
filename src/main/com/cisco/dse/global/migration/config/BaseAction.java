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
			log.debug("in the getConnection method.");
			Connection connection = Jsoup.connect(loc);
			if (connection != null) {
				doc = connection.get();
			} else if (count < 10) {
				count++;
				log.debug("Trying to reconnect count : " + count);
				getConnection(loc);
			}
		} catch (Exception e) {
			log.error("Exception : ", e);
		}

		return doc;
	}

}
