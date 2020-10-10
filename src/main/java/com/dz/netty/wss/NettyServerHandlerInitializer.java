package com.dz.netty.wss;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;

public class NettyServerHandlerInitializer extends ChannelInitializer<Channel> {

    // 证书中使用的密码，因为demo，不做配置，直接写死
    private String password = "123456";

    @Override
    protected void initChannel(Channel ch) throws Exception {
        ch.pipeline().addLast(new HttpServerCodec());
        ch.pipeline().addLast(new HttpObjectAggregator(65535));
        ch.pipeline().addLast(new ChunkedWriteHandler());
        // 对websocket url中的参数做解析处理的Handler
        ch.pipeline().addLast(new CustomUrlHandler());
        // 对websocket做支持，其路径为/ws
        ch.pipeline().addLast(new WebSocketServerProtocolHandler("/ws"));
        // 自定义业务逻辑处理的Handler
        ch.pipeline().addLast(new MyWebSocketHandler());

        // 以下为要支持wss所需处理
        KeyStore ks = KeyStore.getInstance("JKS");
        InputStream ksInputStream = new FileInputStream("/Users/zal/Documents/code/helloNetty/src/main/java/com/dz/netty/wss/demo.wsstest.com.keystore");
        ks.load(ksInputStream, password.toCharArray());
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, password.toCharArray());
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), null, null);
        SSLEngine sslEngine = sslContext.createSSLEngine();
        sslEngine.setUseClientMode(false);
        sslEngine.setNeedClientAuth(false);
        // 需把SslHandler添加在第一位
        ch.pipeline().addFirst("ssl", new SslHandler(sslEngine));
    }
}