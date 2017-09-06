package com.andreidemus.requests;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RequestsClient {

    private static final Pattern CHARSET_PATTERN = Pattern.compile("charset=([_\\-0-9a-zA-Z]+)(;|$)");

    public Response get(Request request) {
        return process("GET", request);
    }

    public Response post(Request request) {
        return process("POST", request);
    }

    public Response put(Request request) {
        return process("PUT", request);
    }

    public Response delete(Request request) {
        return process("DELETE", request);
    }

    public Response head(Request request) {
        return process("HEAD", request);
    }

    //TODO CONNECT, OPTIONS, TRACE, PATCH

    private Response process(String method, Request request) {
        try {
            final HttpURLConnection conn = constructRequest(method, request);
            final Response response = parseResponse(conn);
            conn.disconnect();
            return response;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private HttpURLConnection constructRequest(String method, Request request) throws IOException {
        String urlStr = request.url();
        if (request.hasPathParams()) {
            urlStr += "?" + request.pathParamsAsString();
        }
        final URL url = new URL(urlStr);
        final HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod(method);

        request.headers().forEach((key, vals) -> {
            vals.forEach(val -> conn.addRequestProperty(key, val));
        });

        if (request.hasBody()) {
            if (!request.headers().containsKey("Content-Type")) {
                conn.addRequestProperty("Content-Type", "text/plain; " + request.charset().name());
            }
            final byte[] bytes = request.body();
            conn.addRequestProperty("Content-Length", String.valueOf(bytes.length));
            writeRequestBody(conn, bytes);
        } else if (request.hasFormParams()) {
            if (!request.headers().containsKey("Content-Type")) {
                conn.addRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            }
            final byte[] bytes = request.formParamsAsString().getBytes(request.charset());
            conn.addRequestProperty("Content-Length", String.valueOf(bytes.length));
            writeRequestBody(conn, bytes);
        }

        return conn;
    }

    private void writeRequestBody(HttpURLConnection conn, byte[] bytes) throws IOException {
        conn.setDoOutput(true);
        try (OutputStream out = conn.getOutputStream()) {
            out.write(bytes);
            out.flush();
        }
    }

    private Response parseResponse(HttpURLConnection conn) throws IOException {
        final Response response = new Response();
        response.status = conn.getResponseCode();
        response.reason = conn.getResponseMessage();

        conn.getHeaderFields()
            .entrySet()
            .stream()
            .filter(it -> it.getKey() != null && it.getValue() != null && !it.getValue().isEmpty())
            .forEach(it -> response.headers.put(it.getKey(), it.getValue()));

        response.charset = Optional.ofNullable(response.headers.get("Content-Type"))
                                   .flatMap(this::parseCharset)
                                   .orElse(StandardCharsets.UTF_8);
        response.body = readBody(conn);

        return response;
    }

    private byte[] readBody(HttpURLConnection conn) {
        try (InputStream in = getInputStream(conn)) {
            if (in == null) {
                return new byte[]{};
            }
            byte[] bytes = new byte[in.available()];
            in.read(bytes);
            return bytes;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private InputStream getInputStream(HttpURLConnection conn) {
        try {
            if (conn.getResponseCode() < 400) {
                return conn.getInputStream();
            } else {
                return conn.getErrorStream();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    Optional<Charset> parseCharset(List<String> headerValues) {
        return headerValues.stream()
                           .map(this::parseCharset)
                           .filter(Optional::isPresent)
                           .map(Optional::get)
                           .findFirst();
    }

    Optional<Charset> parseCharset(String contentType) {
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
