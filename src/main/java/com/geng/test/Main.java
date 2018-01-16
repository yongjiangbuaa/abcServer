package com.geng.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Main {

	public static void main(String[] args){
		System.out.println("hello!");
		Logger logger = LoggerFactory.getLogger(Main.class);
		logger.info("this is logged by SLF4J");

	}


}
