package com.andreidemus.http.server;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ServerRequest {
    String rawStartLine;
    String rawHeaders;

    byte[] body;
    String method;
    String url;
    String protocol;
    Charset charset = StandardCharsets.UTF_8; // TODO should be get from request headers

    Map<String, List<String>> headers;// = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    public String rawStartLine() {
        return rawStartLine;
    }

    public String rawHeaders() {
        return rawHeaders;
    }

    public byte[] body() {
        return Arrays.copyOf(this.body, this.body.length);
    }

    public String bodyAsString() {
        return new String(body, charset);
    }

    public String method() {
        return method;
    }

    public String url() {
        return url;
    }

    public String protocol() {
        return protocol;
    }

    public Charset charset() {
        return charset;
    }

    public Map<String, List<String>> headers() {
        Map<String, List<String>> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        headers.putAll(this.headers);
        return headers;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(rawStartLine)
          .append("\n")
          .append(rawHeaders);
        if (body != null && body.length > 0) {
            sb.append("\n\n")
              .append(new String(body, charset));
        }

        return sb.toString();
    }
}
