package com.geng.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import static io.netty.buffer.Unpooled.copiedBuffer;

public class AbcServerHandler extends ChannelInboundHandlerAdapter {
    private  final Logger logger = LoggerFactory.getLogger(AbcServerHandler.class);

//    @Override
//    public void channelActive(ChannelHandlerContext ctx){
//        ByteBuf time = ctx.alloc().buffer(4);
//        time.writeInt(Math.toIntExact(System.currentTimeMillis()/1000L));
//        final ChannelFuture f = ctx.writeAndFlush(time);
//        f.addListener(new ChannelFutureListener() {
//            @Override
//            public void operationComplete(ChannelFuture future) throws Exception {
//                    assert f == future;
//                    ctx.close();
//            }
//        });
//
//    }
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //业务逻辑开始
        if(msg instanceof FullHttpRequest){
             final FullHttpRequest request = (FullHttpRequest) msg;
             logger.error("HTTP");
//             if(!request.method().equals(HttpMethod.POST)){
//                throw new Exception("only accept post method");
//             }
             logger.error("HTTP METHOD: {}",request.method());
             logger.error("URI: {}",request.getUri());
            QueryStringDecoder queryDecoder = new QueryStringDecoder(request.getUri(),
                    true);
            Map<String, List<String>> parameters = queryDecoder.parameters();
            String deviceId = parameters.containsKey("device") ? parameters.get(
                    "device").get(0) : "";
             String data =request.content().toString(Charset.forName("UTF-8"));
            logger.error("content: {}",data);

             final String responseMessage = "gengyongjiang has got your http post!! data is deviceId:".concat(deviceId);
//            String content = request.content();
            FullHttpResponse response = new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1,
                    HttpResponseStatus.OK,
                    copiedBuffer(responseMessage.getBytes()));
            if (HttpHeaders.isKeepAlive(request))
            {
                response.headers().set(
                        HttpHeaders.Names.CONNECTION,
                        HttpHeaders.Values.KEEP_ALIVE
                );
            }
            response.headers().set(HttpHeaders.Names.CONTENT_TYPE,
                    "text/plain");
            response.headers().set(HttpHeaders.Names.CONTENT_LENGTH,
                    responseMessage.length());

            ctx.writeAndFlush(response);


        }else{
            super.channelRead(ctx,msg);
        }
//        ByteBuf in = (ByteBuf)msg;
//        logger.error("recieved data:{}",in.toString(CharsetUtil.UTF_8));
//        ctx.write(in);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx){

        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER)
                .addListener(ChannelFutureListener.CLOSE);

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx,Throwable cause){
        ctx.writeAndFlush(new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.INTERNAL_SERVER_ERROR,
                copiedBuffer(cause.getMessage().getBytes())
        ));
        cause.printStackTrace();
        logger.error(cause.getMessage());
        ctx.close();

    }
}
