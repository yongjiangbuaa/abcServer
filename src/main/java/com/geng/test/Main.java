package com.geng.test;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.util.StatusPrinter;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

class Main {

	public static void main(String[] args){
		System.out.println("hello!");
		Logger logger = LoggerFactory.getLogger(Main.class);
		logger.info("this is logged by SLF4J");
		Object[] objectParam = {"first","second","third"};
		logger.info("objectParams are {},{},{} and {}",objectParam);
		LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
		StatusPrinter.print(lc);
		ByteBuffer m = ByteBuffer.allocate(1);
		m.flip();

		//单元测试 EmmbededChannel
		ByteBuf buff = Unpooled.buffer();
		for (int i=0;i<9;i++){
			buff.writeByte(i);
		}

		ByteBuf input = buff.duplicate();

		EmbeddedChannel channel = new EmbeddedChannel(new FixedLengthFrameDecoder(3));



	}


}
