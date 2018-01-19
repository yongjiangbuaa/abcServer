package com.geng.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbcServerHandler extends SimpleChannelInboundHandler {
    private  final Logger logger = LoggerFactory.getLogger(AbcServerHandler.class);
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf in = (ByteBuf)msg;
        try{
            while(in.isReadable()){
                logger.info("recieved data:{}",(char)in.readByte());
            }
        }finally {
            ReferenceCountUtil.release(msg);
        }
    }
}
