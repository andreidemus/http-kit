package com.andreidemus.requests;

import java.nio.charset.Charset;
import java.util.*;

import static com.andreidemus.requests.Utils.prettyPrintMap;

public class Response {
    int status;
    String reason;
    Charset charset;
    byte[] body;
    Map<String, List<String>> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    public int status() {
        return status;
    }

    public String reason() {
        return reason;
    }

    public String text() {
        return bodyAsString();
    }

    public String bodyAsString() {
        return new String(body, charset);
    }

    public Charset charset() {
        return charset;
    }

    public boolean hasBody() {
        return body != null && body.length > 0;
    }

    public byte[] body() {
        return body;
    }

    public Map<String, List<String>> headers() {
        return headers;
    }

    public List<String> header(String header) {
        final List<String> hs = headers.get(header);
        if (hs != null) {
            return hs;
        } else {
            return Collections.emptyList();
        }
    }

    public Optional<String> firstHeader(String header) {
        return Optional.ofNullable(headers.get(header))
                       .filter(it -> !it.isEmpty())
                       .map(it -> it.get(0));
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Status: ")
          .append(status())
          .append("\nReason: ")
          .append(reason());
        if (!headers().isEmpty()) {
            sb.append("\nHeaders:\n")
              .append(prettyPrintMap(headers()));
        }
        if (hasBody()) {
            sb.append("\nBody:\n")
              .append(text());
        }

        return sb.toString();
    }
}
