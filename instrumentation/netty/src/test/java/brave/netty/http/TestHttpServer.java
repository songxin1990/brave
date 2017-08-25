/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package brave.netty.http;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class TestHttpServer extends Thread {
  private static final Logger logger = LoggerFactory.getLogger(TestHttpServer.class);

  private final int port;
  private ChannelInitializer<SocketChannel> channelInitializer;
  EventLoopGroup bossGroup = new NioEventLoopGroup();
  EventLoopGroup workerGroup = new NioEventLoopGroup();

  public TestHttpServer(int port, ChannelInitializer<SocketChannel> channelInitializer) {
    this.port = port;
    this.channelInitializer = channelInitializer;
  }

  @Override
  public void run() {
    try {
      ServerBootstrap b = new ServerBootstrap();
      b.group(bossGroup, workerGroup)
          .channel(NioServerSocketChannel.class)
          .childHandler(channelInitializer);

      Channel ch = b.bind(port).sync().channel();
      logger.info("netty httpserver start");
      ch.closeFuture().sync();
    } catch (InterruptedException e) {
      logger.info("netty httpserver interrupted");
      //Thread.currentThread().interrupt();
    } finally {
      shutdown();
    }
  }

  public void shutdown() {
    if (bossGroup != null) bossGroup.shutdownGracefully();
    if (workerGroup != null) workerGroup.shutdownGracefully();
  }
}
