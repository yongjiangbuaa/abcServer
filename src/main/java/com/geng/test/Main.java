package com.geng.test;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.util.StatusPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Main {

	public static void main(String[] args){
		System.out.println("hello!");
		Logger logger = LoggerFactory.getLogger(Main.class);
		logger.info("this is logged by SLF4J");
		LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
		StatusPrinter.print(lc);

	}


}
