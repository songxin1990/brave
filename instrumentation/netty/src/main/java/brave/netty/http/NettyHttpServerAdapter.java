package brave.netty.http;

import brave.http.HttpServerAdapter;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.util.internal.StringUtil;

class NettyHttpServerAdapter extends HttpServerAdapter<HttpRequest, HttpResponse> {
  @Override
  public String method(HttpRequest request) {
    return request.method().name();
  }

  /**
   * no url returned if header "HOST" is null.
   * @param request
   * @return
   */
  @Override
  public String url(HttpRequest request) {
    String host = request.headers().get("HOST");
    if(StringUtil.isNullOrEmpty(host)) {
      return null;
    }else {
      StringBuffer url = new StringBuffer("http://");
      url.append(host);
      url.append(request.uri());
      return url.toString();
    }
  }

  @Override
  public String requestHeader(HttpRequest request, String name) {
    return request.headers().get(name);
  }

  @Override
  public Integer statusCode(HttpResponse response) {
    return response.status().code();
  }
}
