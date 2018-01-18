package com.geng.test;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FixedLengthFrameDecoderTests {
    @Test
    public void testWriteAndReadWithChannel(){
        ByteBuf buf = Unpooled.buffer();
        //单元测试 EmmbededChannel
        for (int i=0;i<9;i++){
            buf.writeByte(i);
        }
        ByteBuf input = buf.duplicate();
        EmbeddedChannel channel = new EmbeddedChannel(new FixedLengthFrameDecoder(3));

        //写入
        assertTrue(channel.writeInbound(input.retain()));
        assertTrue(channel.finish());

        //读取
        ByteBuf read =channel.readInbound();
        assertEquals(read,buf.readSlice(3));
        read.release();

        read = channel.readInbound();
        assertEquals(read,buf.readSlice(3));
        read.release();

        read = channel.readInbound();
        assertEquals(read,buf.readSlice(3));
        read.release();
        assertNull(channel.readInbound());

        buf.release();
    }
}
