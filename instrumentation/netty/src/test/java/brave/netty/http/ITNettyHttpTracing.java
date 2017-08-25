package brave.netty.http;

import brave.http.ITHttpServer;
import org.junit.After;
import org.junit.AssumptionViolatedException;
import org.junit.ComparisonFailure;
import org.junit.Test;

public class ITNettyHttpTracing extends ITHttpServer {
  private int port = 4567;
  TestHttpServer httpSnoopServer = null;

  @Override
  @Test(expected = ComparisonFailure.class)
  public void reportsClientAddress() throws Exception {
    try {
      throw new AssumptionViolatedException("client address can get from channel not request");
    } finally {
      Thread.sleep(2000);
    }
  }

  @Override
  protected void init() throws Exception {
    stop();
    TestHttpInitializer initializer = new TestHttpInitializer(httpTracing);
    httpSnoopServer = new TestHttpServer(port, initializer);
    httpSnoopServer.start();
  }

  @Override
  protected String url(String path) {
    return "http://localhost:" + port + path;
  }

  @After
  public void stop() throws Exception {
    if (httpSnoopServer != null) {
      httpSnoopServer.interrupt();
      httpSnoopServer = null;
    }

  }
}
