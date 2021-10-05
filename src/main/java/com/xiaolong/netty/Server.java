package com.xiaolong.netty;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;



/**
 * @Description: 阻塞Io服务器的样子
 * @Author xiaolong
 * @Date 2021/10/4 9:25 下午
 */
public class Server implements Runnable{
    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(8808);
            while (!Thread.interrupted()){
                Socket accept = serverSocket.accept();
                new Thread(new Handler(accept)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class Handler implements Runnable{

        private final Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                byte[] input = new byte[Integer.MAX_VALUE / 10000];
                socket.getInputStream().read(input);
                byte[] output = process(input);
                socket.getOutputStream().write(output);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private byte[] process(byte[] input) {
            return "hello world".getBytes(StandardCharsets.UTF_8);
        }
    }

}
