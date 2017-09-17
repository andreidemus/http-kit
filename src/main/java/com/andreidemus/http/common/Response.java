package com.andreidemus.http.common;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.andreidemus.http.common.Utils.prettyPrintMap;

public class Response {
    private static final String CONTENT_TYPE = "Content-Type";
    private static final Pattern CHARSET_PATTERN = Pattern.compile("charset=([_\\-0-9a-zA-Z]+)(;|$)");

    private int status;
    private String reason;
    private Charset charset;
    private byte[] body;
    private Map<String, List<String>> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    public Response() {}

    public Response(int status, String reason, byte[] body, Map<String, List<String>> headers) {
        this.status = status;
        this.reason = reason;
        this.body = body;
        putHeaders(headers);
        this.charset = Optional.ofNullable(headers.get(CONTENT_TYPE))
                               .flatMap(this::parseCharset)
                               .orElse(StandardCharsets.UTF_8);
    }

    public Response(int status, String reason, Map<String, List<String>> headers) {
        this.status = status;
        this.reason = reason;
        this.headers = headers;
    }

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

    private void putHeaders(Map<String, List<String>> headers) {
        headers.entrySet()
               .stream()
               .filter(it -> it.getKey() != null && it.getValue() != null && !it.getValue().isEmpty())
               .forEach(it -> this.headers.put(it.getKey(), it.getValue()));
    }

    private Optional<Charset> parseCharset(List<String> headerValues) {
        return headerValues.stream()
                           .map(this::parseCharset)
                           .filter(Optional::isPresent)
                           .map(Optional::get)
                           .findFirst();
    }

    private Optional<Charset> parseCharset(String contentType) {
        try {
            final Matcher m = CHARSET_PATTERN.matcher(contentType);
            if (m.find()) {
                return Optional.of(Charset.forName(m.group(1)));
            }
        } catch (Exception e) {
            return Optional.empty();
        }
        return Optional.empty();
    }
}
