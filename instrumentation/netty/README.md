# brave-instrumentation-netty

This module contains netty http handler.
The filters extract trace state from incoming requests. Then, they
reports Zipkin how long each request takes, along with relevant tags
like the http url. The exception handler ensures any errors are also
sent to Zipkin.

To enable tracing you need to add some code in ChannelInitializer inheritance in initChannel method.
For more details,just visit HttpSnoopyServerInitializer in test package.
```java

    NettyTracing nettyHttpTracing = NettyTracing.create(httpTracing);
    p.addLast("tracingOutbound", nettyHttpTracing.channelOutboundHandler());
    p.addLast("tracingInbound", nettyHttpTracing.channelInboundHandler());

    //p.addLast("exception", new ExceptionHandler());//Make sure this is the last line when init the pipeline.
    
```
