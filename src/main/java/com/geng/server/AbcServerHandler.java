package com.geng.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbcServerHandler extends ChannelInboundHandlerAdapter {
    private  final Logger logger = LoggerFactory.getLogger(AbcServerHandler.class);
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)  {
        ByteBuf in = (ByteBuf)msg;
        logger.info("recieved data:{}",in.toString(CharsetUtil.UTF_8));
        ctx.write(in);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx){

        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER)
                .addListener(ChannelFutureListener.CLOSE);

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx,Throwable cause){
        cause.printStackTrace();
        logger.error(cause.getMessage());
        ctx.close();

    }
}
