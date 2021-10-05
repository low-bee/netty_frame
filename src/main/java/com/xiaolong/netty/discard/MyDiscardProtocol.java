package com.xiaolong.netty.discard;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

/**
 * @Description: 实现一个丢弃协议，该协议接收数据后不返回任何数据
 * @Author xiaolong
 * @Date 2021/10/5 3:37 下午
 */
public class MyDiscardProtocol extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ctx.write(msg);
        ByteBuf input = (ByteBuf) msg;
        try {
            while (input.isReadable()){
                System.out.print((char) input.readByte());
                System.out.flush();
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
