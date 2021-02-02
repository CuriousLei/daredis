package nettyTest;

import cn.buptleida.netty.FixedLengthFrameEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.CountDownLatch;


public class Client {
    private static CountDownLatch latch = new CountDownLatch(10000);
    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(workerGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        // 对服务端发送的消息进行粘包和拆包处理，由于服务端发送的消息已经进行了空格补全，
                        // 并且长度为20，因而这里指定的长度也为20
                        socketChannel.pipeline().addLast(new FixedLengthFrameDecoder(20));
                        // 将粘包和拆包处理得到的消息转换为字符串
                        socketChannel.pipeline().addLast(new StringDecoder());
                        // 对客户端发送的消息进行空格补全，保证其长度为20
                        socketChannel.pipeline().addLast(new FixedLengthFrameEncoder(20));
                        // 客户端发送消息给服务端，并且处理服务端响应的消息
                        socketChannel.pipeline().addLast(new ClientHandler(latch));
                    }
                });
        ChannelFuture future = bootstrap.connect("127.0.0.1", 8008).sync();

        double start,end;
        try {
            write(future);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // start = System.currentTimeMillis();
        // for(int i=0;i<10;++i){
        //     String msg = "HSET myHt key"+i+" "+i;
        //     //future.channel().writeAndFlush(msg);
        //     //future.channel().write(Unpooled.copiedBuffer(msg.getBytes()));
        //     future.channel().writeAndFlush(msg.getBytes());
        // }
        // end = System.currentTimeMillis();
        // System.out.println(end-start);
        // start = System.currentTimeMillis();
        // for(int i=0;i<1000;++i){
        //     String msg = "HGET myHt key"+i;
        //     future.channel().writeAndFlush(msg);
        // }
        // end = System.currentTimeMillis();
        // System.out.println(end-start);
        //future.channel().writeAndFlush(Unpooled.copiedBuffer("777".getBytes()));
        //future.channel().close();
        future.channel().closeFuture().sync();

        workerGroup.shutdownGracefully();
    }

    /**
     * 输出流方法
     */
    private static void write(ChannelFuture future) throws IOException {
        //构建键盘输入流
        InputStream in = System.in;
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));


        for(;;){
            String str = bufferedReader.readLine();//从键盘输入获取内容
            double start,end;
            //future.channel().writeAndFlush(str);
            if(str.equalsIgnoreCase("exit")){
                break;
            }
            if(str.equalsIgnoreCase("test")){
                start = System.currentTimeMillis();
                for(int i=0;i<10000;++i){
                    future.channel().writeAndFlush("GET age");
                }
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                end = System.currentTimeMillis();
                System.out.println("查询耗时："+(end-start));
                System.out.println("QPS："+10000/((float)(end-start)/1000));
            }
            //future.channel().write(Unpooled.copiedBuffer(str.getBytes()));
        }
    }

}
