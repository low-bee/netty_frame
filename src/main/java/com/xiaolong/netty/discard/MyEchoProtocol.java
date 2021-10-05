package com.xiaolong.netty.discard;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @Description: 写出数据
 * @Author xiaolong
 * @Date 2021/10/5 4:48 下午
 */
public class MyEchoProtocol extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf in = (ByteBuf) msg;
        ctx.writeAndFlush(in);
    }
}
