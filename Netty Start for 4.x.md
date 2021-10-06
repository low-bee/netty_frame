> [https://netty.io/wiki/user-guide-for-4.x.html](https://netty.io/wiki/user-guide-for-4.x.html)

<a name="txijz"></a>
# 前言
<a name="ebc22"></a>
## 当前的问题
现如今，我们使用app或者一个lib来进行网络通行，例如我们经常会使用HTTP Client库来收发来自网络服务器的的信息或者通过web服务执行远程调用。但是，一些通用的协议还是不能做到一些需要特殊协议能做到的事。就好像我们不会使用HTTO协议去获取一个大文件，发送e-mail，处理一些实时的金融数据获去玩多人网络游戏。这些事需要一个高度优化的协议以便于我们能实现这种特殊的意图。比如说你可能想优化HTTP以便于它能够支持一个以AJAX为基础的聊天应用、流媒体、大文件传输等。你甚至想要自己设计实现一个协议的所有一个部分用来支持你应用的需要。另一个我们必须处理的情况是需要兼容过去遗留的老系统，以便于能够和其进行交互。这种情况下，我们的需求就是在不牺牲兼容性和性能的情况下尽可能快的进行迭代。
<a name="rUe4F"></a>
## 以上问题解决方案-netty
Netty是一个努力提供异步事件驱动Java网络通信应用框架，其为快速开发一个易于维护的高性能和高可用协议服务客户端提供一套工具。<br />换句话说，Netty是一个能够快速简单的进行开发像是协议或者客户端等网络程序的NIO客户端服务框架，它极大地简化和减少了类似TCP、UDP套接字服务器的网络编程开发。<br />“快速简单”不意味着当前程序的性能和可维护性等会出现问题。Netty已经被大量的顶尖协议像是FTP、SMTP、HTTP、各种各样的二进制和文本协议等证明它的设计实现的非常的优秀。从这个角度来考虑，Netty已经在开发、性能、稳定性、兼容性中取得了一个折中的结果。<br />一些用户可能已经发现一些网络框架声称他们有相同的优点，并且你可能会想问，是什么使得Netty如此的不同，答案是Netty的以用户使用的最舒适为中的哲学，Netty构建于这一套哲学至少，让你更容易的使用Netty
<a name="gn4sW"></a>
## Getting Started
这个章节围绕着Netty的核心结构让你快速的上手Netty。在这一个章节结束之后，你将有能力去写一个构建在Netty之上的客户端和服务器。<br />如果你更喜欢自上而下的学习方式，那么你可以从 [Chapter 2, Architectural Overview](https://netty.io/3.8/guide/#architecture)开始，然后再回来当前位置
<a name="Xz0zJ"></a>
## 准备
要运行当前章节的代码，我们的环境必须至少满足两个条件，最新版本的Netty框架和最低JDK1.6，最新版本的Netty在 [the project download page](https://netty.io/downloads.html) 下载，为了下载满足条件的JDK版本，你需要去相应的下载网站。<br />在你看当前章节的过程中，你可能会对当前章节的类产生许多的问题，如果你想要了解更更多请查看相应的API指引。所有的在这个章节出现的类名都有一个很方便的链接供你查看。并且，如果你发现有任何不正确的信息、语法错误或者排印错误，请不要犹豫将这个问题立刻将这个问题告知我们，如果你有一个想法让这个文档变得更好，也欢迎。
<a name="vvnOR"></a>
## 实现Discard Server
在协议领域，最简单的协议不是“hello world”， 而是 [DISCARD](https://tools.ietf.org/html/rfc863)。他是一个接受所有请求并直接丢弃的无响应协议。<br />为了实现DISCARD协议，唯一的你需要做的事是忽略所有收到的数据让我们直接从处理I/O时间的Netty handler的实现开始
```java
import io.netty.buffer.ByteBuf;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Handles a server-side channel.
 */
public class DiscardServerHandler extends ChannelInboundHandlerAdapter { // (1)

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) { // (2)
        // 释放当前的msg
        ((ByteBuf) msg).release(); // (3)
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) { // (4)
        // 当抛出一个异常时关闭连接
        cause.printStackTrace();
        ctx.close();
    }
}
```

1. DiscardServerHandler继承了一个实现了 `ChannelInboundHandler`和`ChannelInboundHandler`接口的`ChannelInboundHandlerAdapter`，提供了一些程序员能够覆写的事件处理方法。对于当前而言，仅仅继承`ChannelInboundHandlerAdapter`而不需要完成实现`ChannelInboundHandler`和`ChannelInboundHandler`接口就已经能够完成我们的目标了
1. 我们覆写了`channelRead()`事件处理方法，当收到信息的时候这个方法会被调用，每次收到来自客户端的新的数据的时候都会调用。在当前案例中，收到的信息是一个字节缓冲 [ByteBuf](https://netty.io/4.1/api/io/netty/buffer/ByteBuf.html)。
1. 为了实现DISCARD协议，处理器也不得不忽略收到的信息， [ByteBuf](https://netty.io/4.1/api/io/netty/buffer/ByteBuf.html)是一个引用计数器对象，我们必须通过release方法显示的将其释放，我们需要记住的是释放传递给处理程序的任何引用计数对象是程序的责任，一般而言，我们`channelRead`处理器方式的实现是下面这样的
```java
@Override
public void channelRead(ChannelHandlerContext ctx, Object msg) {
    try {
        // Do something with msg
    } finally {
        // 使用工具包显示的释放引用
        ReferenceCountUtil.release(msg);
    }
}
```

4. `exceptionCaught()`在Netty应为IO抛出异常或者处理器实现在处理数据是抛出异常的时候会被调用。在大多数情况下，导致异常的原因应用使用日志记录，并且和其连接的channel应该被关闭。尽管这个方法的实现依赖于你处理数据的不同的情况。例如，在关闭连接之前，你可能想要发送一个相应信息。

到目前为止，一切都挺完美的。我们实现了DISCARD服务的一半了，接下来，我们来编写一个main方法来执行这个实现了DISCARD协议的服务器
```java
package io.netty.example.discard;
    
import io.netty.bootstrap.ServerBootstrap;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
    
/**
 * Discards any incoming data.
 */
public class DiscardServer {
    
    private int port;
    
    public DiscardServer(int port) {
        this.port = port;
    }
    
    public void run() throws Exception {
        // reactor 主从模式
        // boss 用来接客人，并且将其分配给各个worker执行。
        EventLoopGroup bossGroup = new NioEventLoopGroup(); // (1)
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            // 一个服务器类，我们也可以通过channel，但是没必要
            ServerBootstrap b = new ServerBootstrap(); // (2)
            b.group(bossGroup, workerGroup)
             .channel(NioServerSocketChannel.class) // (3)
                // 此处传入我们的DisCardServerHandler()
             .childHandler(new ChannelInitializer<SocketChannel>() { // (4)
                 @Override
                 public void initChannel(SocketChannel ch) throws Exception {
                     ch.pipeline().addLast(new DiscardServerHandler());
                 }
             })
             .option(ChannelOption.SO_BACKLOG, 128)          // (5)
             .childOption(ChannelOption.SO_KEEPALIVE, true); // (6)
    
            // 绑定端口并且开始监听端口
            ChannelFuture f = b.bind(port).sync(); // (7)
    
            // 等待当前服务的socker关闭，在本案例中不会出现这样的情况
            // 但是你可以这样做，优雅的关闭你的服务
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
    
    public static void main(String[] args) throws Exception {
        int port = 8080;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }

        new DiscardServer(port).run();
    }
}
```

1. [NioEventLoopGroup](https://netty.io/4.1/api/io/netty/channel/nio/NioEventLoopGroup.html)是一个多线程循环事件IO处理器。Netty提供多种 [EventLoopGroup](https://netty.io/4.1/api/io/netty/channel/EventLoopGroup.html) 实现用于支持不同种类的运输层协议。我们实现一个服务器端的应用的样例中，有两个[NioEventLoopGroup](https://netty.io/4.1/api/io/netty/channel/nio/NioEventLoopGroup.html)被使用到，第一个，常被称为 “boss”，接受请求过来的连接，第二个，常被称为“woker”，一旦第一个boss接受到连接在接受连接并且将其注册之后worker就开始进行处理了。有多少线程和常见多少到[Channel](https://netty.io/4.1/api/io/netty/channel/Channel.html)的映射取决于[EventLoopGroup](https://netty.io/4.1/api/io/netty/channel/EventLoopGroup.html)的具体实现和通过构造器创建的配置。
1. [ServerBootstrap](https://netty.io/4.1/api/io/netty/bootstrap/ServerBootstrap.html)帮助我们创建一个服务，经过我们可以直接通过一个Channel来完成，但是这是一个枯燥而乏味的过程，在大多数情况下你都不需要这样做。
1. 在这里，我们定制的使用了[NioServerSocketChannel](https://netty.io/4.1/api/io/netty/channel/socket/nio/NioServerSocketChannel.html)类，这个类常常被用来实例化一个接受连接的Channel
1. 此处定制化的处理器将总是会评估接受的Channel， [ChannelInitializer](https://netty.io/4.1/api/io/netty/channel/ChannelInitializer.html) 是一个常常被用来帮助用户配置新的Channel的定制化处理器。绝大多数的情况下你是想通过增加一些像是DiscardServerHandler这样的为自己的网络应用自己实现的处理器配置一个新的Channel的 [ChannelPipeline](https://netty.io/4.1/api/io/netty/channel/ChannelPipeline.html) ，随着应用变得越来越复杂，很可能你向管道中添加更多的处理用，最后将这些匿名类放到顶级类中。 
1. 我们也可以去设置一些Channel实现的特殊的参数，我们被允许去设置一些像是tcpNoDelay或者keepAlice等TCP/IP的socket选项，请查看[ChannelOption](https://netty.io/4.1/api/io/netty/channel/ChannelOption.html) api文档并且定制化 [ChannelConfig](https://netty.io/4.1/api/io/netty/channel/ChannelConfig.html) 实现来得到一个覆写的ChannelOptions的实现
1. 不知道你有没有注意到到`option() childOption()`方法，第一个是boss，第二个for worker
1. 我么已经做好工作了，剩下的就是板顶端口号并且启动服务器。在此，我们绑定8080端口的所有网卡，我们能调用 `bind()`方法多次将应用绑定到任何你想要它绑定的端口上
<a name="lRmGo"></a>
### 查看收到的数据
现在我们已经编写好了我们的第一个服务器了，我们需要去测试它是否真的在工作，最简单的测试它的方式是使用_telnet_命令，例如你能在命令行键入 `telnet localhost 8080` 并且输入一些东西。<br />然而，我们能说这个服务在工作吗？我们不能有信息这样说，主要是因为这是一个discard服务。不知道有没有得到任何的response。为了确认这个服务真的在工作，让我们打印所有它收到的时间。<br />我们已经知道`channelRead()`方法在每次收到数据的时候都会被调用。让我们`channelRead()`变成下面这样的形式
```java
@Override
public void channelRead(ChannelHandlerContext ctx, Object msg) {
    ByteBuf in = (ByteBuf) msg;
    try {
        while (in.isReadable()) { // (1)
            System.out.print((char) in.readByte());
            System.out.flush();
        }
    } finally {
        ReferenceCountUtil.release(msg); // (2)
    }
}
```

1. 这个低效率的循环实际上可以转换为`System.out.println(in.toString(io.netty.util.CharsetUtil.US_ASCII)`
1. 除此之外，你可以调用`in.release()`​

当你再次运行telnet命令，就能显示出你收到的数据了，这篇文章的全部源码可以在`io.netty.example.discard`包中找到。
<a name="Ul4hu"></a>
## 实现Echo服务
到目前为止，我们一直在消费数据而不需要任何的响应。然而在服务器端，通常是需要支持请求的响应的。现在让我们通过实现一个ECHO协议来学习怎样去写一条信息给客户端。它会将任何它收到的消息发送回客户端。<br />这和我们前面章节中实现的discard服务其唯一的不同是他发送信息返回给客户端而不是将信息打印到控制台中。因此，只需要再次定义`channelRead()`方法即可
```java
@Override
public void channelRead(ChannelHandlerContext ctx, Object msg) {
    ctx.write(msg); // (1)
    ctx.flush(); // (2)
}
```

1. 一个[ChannelHandlerContext](https://netty.io/4.1/api/io/netty/channel/ChannelHandlerContext.html)对象是一个有能力响应各种各样IO操作事件的一个触发器，当前我们调用`write(object)`向外一字不差的写出收到的信息。请注意，当前不想在discard上那样释放了收到的信息，这是因为当它写出之后，Netty会隐式的释放。
1. `ctx.write(msg)`没有让这条信息被写出当前的writer，这是因为msg被缓存在内部，因此我们调用flush将信息写出。出了这样的方式之外，你也可以调用这个更加简洁的方法`ctx.writeAndFlash(msg)`。

如果现在你现在再次运行_telnet_程序，你将看到服务器返回了你发送给他的数据。这篇文章的全部源码可以在`io.netty.example.echo`包中找到。
<a name="njudU"></a>
### 实现一个时间服务
这一片文章的这个章节协议用来实现一个[TIME](https://tools.ietf.org/html/rfc868)协议。这个样例和我们之前学到的发送信息的协议不同，它的响应包含32位整数，不需要收到任何的请求，当发送信息后会关闭连接。在这个样例中，你将学到如何构建一个信息和如何发送一个信息和在完成信息发送之后关闭当前的连接。<br />因为我们除了在建立连接之后立即发送一条消息之外会忽略所有的请求信息，这次我们就不能使用`channelRead()`方法了，作为替代，我们应该覆写`channelActive`方法。像下面这样实现
```java
package io.netty.example.time;
// 依旧是继承ChannelInboundHandlerAdapter
public class TimeServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(final ChannelHandlerContext ctx) { // (1)
        final ByteBuf time = ctx.alloc().buffer(4); // (2)
        time.writeInt((int) (System.currentTimeMillis() / 1000L + 2208988800L));
        
        final ChannelFuture f = ctx.writeAndFlush(time); // (3)
        f.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) {
                assert f == future;
                ctx.close();
            }
        }); // (4)
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
```

1. 像我们解释的那样，方法`channelActive()`将在连接建立的时候被调用并且准备好读取来自客户端的数据流。然后写一个32位整数出去，相当于在这个方法中的实时的时间。
1. 为了发送一条新的消息，我们需要分配一个新的去存储信息的缓存。因为我们需要写一个32位的整数，因此我们的容量至少是四个字节。获取当前的 [ByteBufAllocator](https://netty.io/4.1/api/io/netty/buffer/ByteBufAllocator.html) （缓存分配器）通过调用ChannelHandlerContext.alloc()分配一个新的缓存。
1. 通常情况下，我们会写一个构造信息。

但是等一下，我们在NIO中发送一条信息之前不是会调用` java.nio.ByteBuffer.flip() `吗？`ByteBuf`没有这样的方法，因为他有两个指针，一个用来读数据，一个用来写数据。这样我们就不用flip了。当写数据到`ByteBuf`中时，写索引会增大但读索引不会改变。读索引和写索引各自代表字节流开始和结束的位置。<br />相比之下，NIO缓存没有提供一个明确的方法来判断内容数据的开始和结束的位置而不得不调用flip方法。当你忘记调用flip的时候，你会有很大的麻烦，它会发送不正确的数据。这样的错误不会发生在Netty中因为我们有不同的指针来处理这两种操作，当你习惯了它时，你会发现它会让你的生活变得更轻松——一种没有flip的生活！<br />另一点需要注意的是ChannelHandlerContext.write()会返回一个 [ChannelFuture](https://netty.io/4.1/api/io/netty/channel/ChannelFuture.html).  在Netty中，[ChannelFuture](https://netty.io/4.1/api/io/netty/channel/ChannelFuture.html).意味着一个这个IO操作不是直接发生的，它的含义是，任何的请求操作都可能不会真正实时操作，因为这是异步的。例如，下面的代码可能会在信息被发送之前关闭连接
```java
Channel ch = ...;
ch.writeAndFlush(message);
ch.close();
```
处于这个原因，你需要去调用close方法在 [ChannelFuture](https://netty.io/4.1/api/io/netty/channel/ChannelFuture.html) 完成之后。在writer完成之后会返回，他会通知监听器开始工作。请注意。close也不会立即关闭连接，因为他也会返回一个 [ChannelFuture](https://netty.io/4.1/api/io/netty/channel/ChannelFuture.html)。

4. 我们如何获取当写操作完成之后的通知呢？一个简单的样例是增加一个[ChannelFutureListener](https://netty.io/4.1/api/io/netty/channel/ChannelFutureListener.html)给返回的`ChannelFuture`，在此处我们创建一个新的匿名对象 [ChannelFutureListener](https://netty.io/4.1/api/io/netty/channel/ChannelFutureListener.html)，这个对象会在这个操作完成之后关闭这个Channel，出了这种方式以外，你应该可以通过使用一个预先定义好的监听器来简化代码
```java
f.addListener(ChannelFutureListener.CLOSE);
```
<a name="kR3XH"></a>
### 实现Time协议客户端
和DISCARD协或者ECHO服务器不同，我们需要编一写一个TIME协议的服务器，因为一个正常人很难将一个32位证书转变成为一个日期。在这个章节中，我们讨论了如何取确保服务器正常的工作，现在我们来学习怎样用Netty写一个客户端程序。<br />客户端和服务器之间最大的不同是实现[Bootstrap](https://netty.io/4.1/api/io/netty/bootstrap/Bootstrap.html) 和 [Channel](https://netty.io/4.1/api/io/netty/channel/Channel.html)的方式。请看下面的代码
```java
package io.netty.example.time;

public class TimeClient {
    public static void main(String[] args) throws Exception {
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        
        try {
            Bootstrap b = new Bootstrap(); // (1)
            b.group(workerGroup); // (2)
            b.channel(NioSocketChannel.class); // (3)
            b.option(ChannelOption.SO_KEEPALIVE, true); // (4)
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new TimeClientHandler());
                }
            });
            
            // Start the client.
            ChannelFuture f = b.connect(host, port).sync(); // (5)

            // Wait until the connection is closed.
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }
}

```

1. [Bootstrap](https://netty.io/4.1/api/io/netty/bootstrap/Bootstrap.html) 和 [ServerBootstrap](https://netty.io/4.1/api/io/netty/bootstrap/ServerBootstrap.html) 非常的相似除了他的服务channel为空之外，例如客户端或者无连接的Channel。
1. 如果你仅仅定义一个[EventLoopGroup](https://netty.io/4.1/api/io/netty/channel/EventLoopGroup.html)，他将作用于boss和worker两者。只不过boss这个角色没有被客户端使用到而已
1. 用 [NioSocketChannel](https://netty.io/4.1/api/io/netty/channel/socket/nio/NioSocketChannel.html) 代替 [NioServerSocketChannel](https://netty.io/4.1/api/io/netty/channel/socket/nio/NioServerSocketChannel.html)来常见一个客户端Channel
1. 注意我们在此不需要像在服务器中那样使用childOption，这是因为客户端的SockerChannel没有父Scokter
1. 应该调用`connect()`而不是`bind()`

如同你看到的这样，客户端的代码和服务器没有什么不同，那当前的 [ChannelHandler](https://netty.io/4.1/api/io/netty/channel/ChannelHandler.html) 怎么实现呢？他应该从服务器收到一个32位的整数并且将它转变成一个人类可以识别的格式。输出这个转化好的时间然后关闭当前的连接。
```java
package io.netty.example.time;

import java.util.Date;

public class TimeClientHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf m = (ByteBuf) msg; // (1)
        try {
            long currentTimeMillis = (m.readUnsignedInt() - 2208988800L) * 1000L;
            System.out.println(new Date(currentTimeMillis));
            ctx.close();
        } finally {
            m.release();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
```

1. 在TCP/IP协议中，Netty将对等方发送的数据读取到 [ByteBuf](https://netty.io/4.1/api/io/netty/buffer/ByteBuf.html) 中

这似乎看起来非常的简单并且和服务器端的代码没有任何的不同。然而，我们的handler有事却会抛出一个`IndexOutOfBoundsException`异常，在下一个章节我们来讨论一下这个情况出现的原因。
<a name="sb9oS"></a>
# 处理基于流的传输
<a name="CjyDt"></a>
## 在处理SocketBuffer中的一个小问题
在像是TCP/IP这种基于流的传输中，收到的数据被存入socket的接受缓冲区。但遗憾的是，一个基于流的缓冲区不是一个packet的队列二是一个字节的队列。这意味着，即使你发送了两条信息在两个独立的包中，操作系统不会以两个包的形式处理而是以一个一串字节数组形式来处理。因此，你的读和写所写出和接收到的数据，可能不是对等的。例如，现在我们假定操作系统的TCP/IP栈现在收到了下面这三个包<br />![image.png](https://cdn.nlark.com/yuque/0/2021/png/21990265/1633522768242-3ddab725-5b88-4262-ab25-758d29db0089.png#clientId=u962cf8f4-11ce-4&from=paste&height=89&id=u94de303b&margin=%5Bobject%20Object%5D&name=image.png&originHeight=89&originWidth=243&originalType=binary&ratio=1&size=2550&status=done&style=none&taskId=u4ca506ca-9e1c-4d40-a9b4-a02e9e48a14&width=243)<br />由于基于流协议的一般属性，很有可能读取到的数据是下面这种帧的形式<br />![image.png](https://cdn.nlark.com/yuque/0/2021/png/21990265/1633522752021-4c282176-e3e8-447f-b1ee-c96db33ec3ff.png#clientId=u962cf8f4-11ce-4&from=paste&height=90&id=u3c894cfd&margin=%5Bobject%20Object%5D&name=image.png&originHeight=90&originWidth=261&originalType=binary&ratio=1&size=2635&status=done&style=none&taskId=u1571e85c-ca1a-493c-9cb4-a8489c421ff&width=261)<br />因此，在无论是服务器还是客户端的接收端，都应该将数据接收到的片段变成一个可以很容易被app程序逻辑理解的有意义的帧。在上面的样例中，接收到的数据应该被封装为下面这样<br />![image.png](https://cdn.nlark.com/yuque/0/2021/png/21990265/1633522729279-f055e8ac-a117-46f5-a100-7e0fa0ac3a64.png#clientId=u962cf8f4-11ce-4&from=paste&height=87&id=ud24b178e&margin=%5Bobject%20Object%5D&name=image.png&originHeight=87&originWidth=261&originalType=binary&ratio=1&size=2638&status=done&style=none&taskId=ufacd1ebc-30aa-4237-af96-d07fb95e895&width=261)
<a name="KEul7"></a>
## 第一种解决方案
现在让我们回到TIMEclient的样例中，在这里我们也有这样的问题，也就是，一个32位的整数是一个非常小的数据，并且不大可能会碎片化。然而，这个问题是他可以被碎片化，并且当流增大碎片化的可能就会增加。一个最简单的解决方案是创建一个内部缓存区，只有当四个字节的数据被全部接收到的时候，才会将其放到缓冲区中。下面这种方案修复了这个问题
```java
package io.netty.example.time;

import java.util.Date;

public class TimeClientHandler extends ChannelInboundHandlerAdapter {
    private ByteBuf buf;
    
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        buf = ctx.alloc().buffer(4); // (1)
    }
    
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        buf.release(); // (1)
        buf = null;
    }
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf m = (ByteBuf) msg;
        buf.writeBytes(m); // (2)
        m.release();
        
        if (buf.readableBytes() >= 4) { // (3)
            long currentTimeMillis = (buf.readUnsignedInt() - 2208988800L) * 1000L;
            System.out.println(new Date(currentTimeMillis));
            ctx.close();
        }
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
```

1.  [ChannelHandler](https://netty.io/4.1/api/io/netty/channel/ChannelHandler.html) 有两个生命周期监听器方法`handlerAdded()` and `handlerRemoved()`，只要他们不阻塞非常长的时间，你可以任意的初始化一个任务。
1. 首先，收到的数据应该被增添到内部缓冲区`buf`中
1. 然后，当前的处理器必须检查到足够的数据，在这个样例中是四个字节，然后去处理计算逻辑。否则，Netty将会再次调用`channelRead()`方法当更多的数据到达的时候，知道所有的四个字节被增添进去。
<a name="DcW8C"></a>
## 第二种解决方案
尽管第一种方案已经解决了TIMEclient的问题，但是修改后的程序确显得非常的杂乱。想象一下这样一种复杂的协议的情况，发送过来的数据由许多字段组成，例如一个可变长度的字段。我们的 [ChannelInboundHandler](https://netty.io/4.1/api/io/netty/channel/ChannelInboundHandler.html) 将很快变得难以维护。<br />但是你可能注意到了，你能够增加更多的[ChannelHandler](https://netty.io/4.1/api/io/netty/channel/ChannelHandler.html) 到 [ChannelPipeline](https://netty.io/4.1/api/io/netty/channel/ChannelPipeline.html)中，并且你能将一个巨大的[ChannelHandler](https://netty.io/4.1/api/io/netty/channel/ChannelHandler.html)分割为多个组合来减少你应用的复杂度，例如，你能分割TimeClientHandler为两个处理器：

- TimeDecoder：用来处理碎片化问题（半包）
- TimeClientHandler：一个最初版本的TimeClientHandler

非常幸运的是，Netty提供了一个拓展类帮助你写一个开箱即用的程序。类似下面这样
```java
package io.netty.example.time;

public class TimeDecoder extends ByteToMessageDecoder { // (1)
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) { // (2)
        if (in.readableBytes() < 4) {
            return; // (3)
        }
        
        out.add(in.readBytes(4)); // (4)
    }
}
```

1. [ByteToMessageDecoder](https://netty.io/4.1/api/io/netty/handler/codec/ByteToMessageDecoder.html)是一个[ChannelInboundHandler](https://netty.io/4.1/api/io/netty/channel/ChannelInboundHandler.html)的实现，者可用更简单的处理碎片化问题。
1. 当收到新的数据的时候，[ByteToMessageDecoder](https://netty.io/4.1/api/io/netty/handler/codec/ByteToMessageDecoder.html)通过内部维护的一个累计缓冲区多次调用`decode()`方法
1. decode()方法能决定当没有足够的数据的时候什么也不会加入到我们的输出中，当有更多的数据到来的时候，会再次调用decode()方法，这样就解决了这个问题
1. 如果decode()方法增加了一个object到out中，这意味着解码器解码一条信息成功。 [ByteToMessageDecoder](https://netty.io/4.1/api/io/netty/handler/codec/ByteToMessageDecoder.html)将丢弃这个独到的部分的缓冲区。请一定要记住，你不需要多次的去调用解码信息。 [ByteToMessageDecoder](https://netty.io/4.1/api/io/netty/handler/codec/ByteToMessageDecoder.html)解码器将在不需要进行增添时什么也不会加入到out中

现在，我需要将另外一个Handler加入到[ChannelPipeline](https://netty.io/4.1/api/io/netty/channel/ChannelPipeline.html),我们应该在TimeClient实现 [ChannelInitializer](https://netty.io/4.1/api/io/netty/channel/ChannelInitializer.html) 
```java
b.handler(new ChannelInitializer<SocketChannel>() {
    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline().addLast(new TimeDecoder(), new TimeClientHandler());
    }
});
```
如果你是一个富有冒险精神的人，你可能想要尝试一下 [ReplayingDecoder](https://netty.io/4.1/api/io/netty/handler/codec/ReplayingDecoder.html) ，这个解码器能更加简单的处理这些事件。ni==你将需要去查询它的API咨询获取更多的信息。
```java
public class TimeDecoder extends ReplayingDecoder<Void> {
    @Override
    protected void decode(
            ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        out.add(in.readBytes(4));
    }
}
```
除此之外，Netty提供了一个开箱即用的解码器，确保你能非常简单的实现更多的协议，并且帮助你避免不可维护的绝大的处理器的实现。请参阅下面的软件包获取更多的细节。

- [io.netty.example.factorial](https://netty.io/4.1/xref/io/netty/example/factorial/package-summary.html) 能用来实现二进制协议
- [io.netty.example.telnet](https://netty.io/4.1/xref/io/netty/example/telnet/package-summary.html) 用来实现一个文本协议
<a name="tcJ7E"></a>
# 使用POJO代替ByteBuf
到目前为止，我们都是ByteBuf这个对象作为一个主要的协议的数据结构，在这一篇文章中，我们将使用POJO代替ByteBuf提高TIME协议的客户端。<br />使用在你的 [ChannelHandler](https://netty.io/4.1/api/io/netty/channel/ChannelHandler.html) 使用一个POJO的优势十分明显，当你从你的handler提取出ByteBuf的信息的时候，你的handler变得更加容易维护和重用。在这个TIME client和服务器的样例中，我们仅仅读一个32位的整数而不是直接使用ByteBuf。然而，你会发现在一个真实的协议中分割代码是一件十分必要的事情。<br />首先，让我们定义一个新的名为`UnixTime`
```java
package io.netty.example.time;

import java.util.Date;

public class UnixTime {

    private final long value;
    
    public UnixTime() {
        this(System.currentTimeMillis() / 1000L + 2208988800L);
    }
    
    public UnixTime(long value) {
        this.value = value;
    }
        
    public long value() {
        return value;
    }
        
    @Override
    public String toString() {
        return new Date((value() - 2208988800L) * 1000L).toString();
    }
}
```
我们现在能修改TimeDecode去生成一个UnixTime而不是一个ByteBuf
```java
@Override
protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
    // 当有足够的数据的时候，这个方法能将输入的ByteBuf的变成一个Integer
    if (in.readableBytes() < 4) {
        return;
    }

    out.add(new UnixTime(in.readUnsignedInt()));
}
```
现在更新解码器，`TimeClienHandler`现在再也不需要使用ByteBuf了
```java
@Override
public void channelRead(ChannelHandlerContext ctx, Object msg) {
    UnixTime m = (UnixTime) msg;
    System.out.println(m);
    ctx.close();
}
```
更加简单也更加优雅了对不对？这种相同的技术也能被用在服务端，让我们更新TimeServerHandler
```java
@Override
public void channelActive(ChannelHandlerContext ctx) {
    ChannelFuture f = ctx.writeAndFlush(new UnixTime());
    f.addListener(ChannelFutureListener.CLOSE);
}
```
现在，唯一的缺失就是实现了[ChannelOutboundHandler](https://netty.io/4.1/api/io/netty/channel/ChannelOutboundHandler.html) 的转换UnixTime为BetyBuf的解码器了。这比写编码器更加的简单因为不需要处理数据包的碎片化和组装编码信息。
```java
package io.netty.example.time;

public class TimeEncoder extends ChannelOutboundHandlerAdapter {
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        UnixTime m = (UnixTime) msg;
        ByteBuf encoded = ctx.alloc().buffer(4);
        encoded.writeInt((int)m.value());
        ctx.write(encoded, promise); // (1)
    }
}
```

1. 在这一行中有几个非常重要的的事。
   1. 首先我们传递一个原样的[ChannelPromise](https://netty.io/4.1/api/io/netty/channel/ChannelPromise.html)，以便于Netty在编码数据实际写入的链路中将其标记为成功或者失败。
   1. 我们没有调用`ctx.flush()`, 有一个单独的处理方法 `void flush(ChannelhandlerContext ctx)`已经覆盖了flush()方法（我的理解是flush已经被调用了？）

为了进一步简化事件，你可以使用[MessageToByteEncoder](https://netty.io/4.1/api/io/netty/handler/codec/MessageToByteEncoder.html):
```java
public class TimeEncoder extends MessageToByteEncoder<UnixTime> {
    @Override
    protected void encode(ChannelHandlerContext ctx, UnixTime msg, ByteBuf out) {
        out.writeInt((int)msg.value());
    }
}
```
最后的任务是将TimeEncoder放入服务器端的[ChannelPipeline](https://netty.io/4.1/api/io/netty/channel/ChannelPipeline.html)中，并且保证在TimeServerHandler，这是一个简单的练习
<a name="xqJXU"></a>
## 关闭你的应用
关闭应用时通常简单的通过`shutdownGracefully()`关闭所有的你创建的EventLoopGroup.这将在返回一个Future，它能在事件组真的的结束和完成并且所有的属于这个事件组的Channel被关闭之后通知你
