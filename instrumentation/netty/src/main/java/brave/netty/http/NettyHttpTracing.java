package brave.netty.http;

import brave.Span;
import brave.Tracer;
import brave.Tracing;
import brave.http.HttpServerHandler;
import brave.http.HttpTracing;
import brave.propagation.TraceContext;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.util.AttributeKey;

public final class NettyHttpTracing {
  public static NettyHttpTracing create(Tracing tracing) {
    return new NettyHttpTracing(HttpTracing.create(tracing));
  }

  public static NettyHttpTracing create(HttpTracing httpTracing) {
    return new NettyHttpTracing(httpTracing);
  }

  final Tracer tracer;
  final HttpServerHandler<HttpRequest, HttpResponse> handler;
  final TraceContext.Extractor<HttpHeaders> extractor;

  NettyHttpTracing(HttpTracing httpTracing) {
    tracer = httpTracing.tracing().tracer();
    handler = HttpServerHandler.create(httpTracing, new NettyHttpServerAdapter());
    extractor = httpTracing.tracing().propagation().extractor(HttpHeaders::get);
  }

  public ChannelInboundHandlerAdapter channelInboundHandler() {
    return new ChannelInboundHandlerAdapter() {
      @Override
      public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        HttpRequest httpRequest = (HttpRequest) msg;
        HttpHeaders httpHeaders = httpRequest.headers();

        Span span = handler.handleReceive(extractor, httpHeaders, httpRequest);
        ctx.channel().attr(AttributeKey.valueOf(Span.class.getName())).set(span);

        try (Tracer.SpanInScope ws = tracer.withSpanInScope(span)) {
          ctx.channel().attr(AttributeKey.valueOf(Tracer.SpanInScope.class.getName())).set(ws);
          super.channelRead(ctx, httpRequest);
        } catch (Exception | Error e) {
          throw e;
        }
      }
    };
  }

  public ChannelOutboundHandlerAdapter channelOutboundHandler() {

    return new ChannelOutboundHandlerAdapter() {
      @Override
      public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise)
          throws Exception {
        HttpResponse response = (HttpResponse) msg;
        Span span = (Span) ctx.channel().attr(AttributeKey.valueOf(Span.class.getName())).get();

        Throwable error = null;
        try (Tracer.SpanInScope ws = (Tracer.SpanInScope) ctx.channel()
            .attr(AttributeKey.valueOf(Tracer.SpanInScope.class.getName()))
            .get()) {
          ws.close();
          super.write(ctx, msg, promise);
        } catch (Exception | Error e) {
          error = e;
          throw e;
        } finally {
          handler.handleSend(response, error, span);
        }
      }
    };
  }
}
