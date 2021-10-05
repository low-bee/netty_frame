package com.xiaolong.netty.discard;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import io.netty.channel.socket.SocketChannel;

/**
 * @Description: Discards any incoming data.
 * @Author xiaolong
 * @Date 2021/10/5 3:49 下午
 */
public class MyDiscardServer {

    private int port ;

    public MyDiscardServer(int port) {
        this.port = port;
    }

    public MyDiscardServer() {
    }

    public void run() throws Exception {
        // 创建Reactor中的工作线程和接收相应线程
        EventLoopGroup bossEventLoopGroup = new NioEventLoopGroup();
        EventLoopGroup workEventLoopGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();

            serverBootstrap.group(bossEventLoopGroup, workEventLoopGroup)   // 设置group
                    .channel(NioServerSocketChannel.class)                  // 设置Channel
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new MyTimeProtocol());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            // 绑定端口开始接收信息
            ChannelFuture f = serverBootstrap.bind(this.port).sync();
            // gracefully close
            f.channel().closeFuture().sync();
        } finally {
            bossEventLoopGroup.shutdownGracefully();
            workEventLoopGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        int port = 8080;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }

        new MyDiscardServer(port).run();
    }
}
