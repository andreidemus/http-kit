package com.andreidemus.http.common;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static com.andreidemus.http.common.Utils.prettyPrintMap;

// TODO improve immutability
public class Request {
    private static final String PATH_DELIMITER = "/";

    private final String url;
    private final byte[] body;
    private final Map<String, Set<Object>> pathParams;
    private final Map<String, Set<Object>> formParams;
    private final Map<String, Set<String>> headers;
    private final Charset charset;

    private Request(String url,
                    byte[] body,
                    Map<String, Set<Object>> pathParams,
                    Map<String, Set<Object>> formParams,
                    Map<String, Set<String>> headers,
                    Charset charset) {
        this.url = url;
        this.body = body;
        this.pathParams = pathParams;
        this.formParams = formParams;
        this.headers = headers;
        this.charset = charset;
    }

    public Request(String url) {
        this(
                url,
                new byte[]{},
                new LinkedHashMap<>(),
                new LinkedHashMap<>(),
                newHeaders(),
                StandardCharsets.UTF_8
        );
    }

    public Request(String url,
                   Map<String, Set<String>> headers,
                   byte[] body) {
        this.url = url;
        this.body = body;
        this.pathParams = new LinkedHashMap<>();
        this.formParams = new LinkedHashMap<>();
        this.headers = newHeaders();
        headers.entrySet()
               .stream()
               .filter(it -> it.getValue() != null && !it.getValue().isEmpty())
               .forEach(it -> this.headers.put(it.getKey(), it.getValue()));
        this.charset = StandardCharsets.UTF_8; // TODO get from headers
    }

    public String url() {
        return url;
    }

    public Request path(String path) {
        return new Request(url + PATH_DELIMITER + urlEncode(path), body, pathParams, formParams, headers, charset);
    }

    public Request formParam(String name, Object value) {
        if (value == null) {
            return this;
        }
        final Map<String, Set<Object>> formParams = new LinkedHashMap<>();
        formParams.putAll(this.formParams);
        Set<Object> vals = formParams.get(name);
        if (vals == null) {
            vals = new LinkedHashSet<>();
        }
        vals.add(value);
        formParams.put(name, vals);

        return new Request(url, body, pathParams, formParams, headers, charset);
    }

    public Request formParam(String name, Set<Object> values) {
        if (values == null) {
            return this;
        }
        final Map<String, Set<Object>> formParams = new LinkedHashMap<>();
        formParams.putAll(this.formParams);
        formParams.put(name, values);

        return new Request(url, body, pathParams, formParams, headers, charset);
    }

    public Request formParams(Map<String, Set<Object>> formParams) {
        formParams = formParams.entrySet()
                               .stream()
                               .filter(it -> it.getValue() != null && !it.getValue().isEmpty())
                               .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return new Request(url, body, pathParams, formParams, headers, charset);
    }

    public boolean hasFormParams() {
        return !formParams.isEmpty();
    }

    public Map<String, Set<Object>> formParams() {
        return formParams;
    }

    public String formParamsAsString() {
        return prepareParams(formParams);
    }

    public Request pathParam(String name, Object value) {
        if (value == null) {
            return this;
        }
        final Map<String, Set<Object>> pathParams = new LinkedHashMap<>();
        pathParams.putAll(this.pathParams);
        Set<Object> vals = pathParams.get(name);
        if (vals == null) {
            vals = new LinkedHashSet<>();
        }
        vals.add(value);
        pathParams.put(name, vals);

        return new Request(url, body, pathParams, formParams, headers, charset);
    }

    public Request pathParam(String name, Set<Object> values) {
        if (values == null) {
            return this;
        }
        final Map<String, Set<Object>> pathParams = new LinkedHashMap<>();
        pathParams.putAll(this.pathParams);
        pathParams.put(name, values);

        return new Request(url, body, pathParams, formParams, headers, charset);
    }

    public Request pathParams(Map<String, Set<Object>> pathParams) {
        pathParams = pathParams.entrySet()
                               .stream()
                               .filter(it -> it.getValue() != null && !it.getValue().isEmpty())
                               .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return new Request(url, body, pathParams, formParams, headers, charset);
    }

    public boolean hasPathParams() {
        return !pathParams.isEmpty();
    }

    public Map<String, Set<Object>> pathParams() {
        return pathParams;
    }

    public String pathParamsAsString() {
        return prepareParams(pathParams);
    }

    public Request headers(Map<String, Set<String>> headers) {
        final Map<String, Set<String>> newHeaders = newHeaders();
        headers.entrySet()
               .stream()
               .filter(it -> it.getValue() != null && !it.getValue().isEmpty())
               .forEach(it -> newHeaders.put(it.getKey(), it.getValue()));

        return new Request(url, body, pathParams, formParams, newHeaders, charset);
    }

    public Request header(String name, String value) {
        if (value == null) {
            return this;
        }
        final Map<String, Set<String>> headers = newHeaders();
        headers.putAll(this.headers);
        Set<String> vals = headers.get(name);
        if (vals == null) {
            vals = new LinkedHashSet<>();
        }
        vals.add(value);
        headers.putIfAbsent(name, vals);

        return new Request(url, body, pathParams, formParams, headers, charset);
    }

    public Map<String, Set<String>> headers() {
        return headers;
    }

    public boolean hasBody() {
        return body.length > 0;
    }

    public Request body(byte[] body) {
        if (body == null) {
            return this;
        }
        return new Request(url, body, pathParams, formParams, headers, charset);
    }

    public Request body(byte[] body, Charset charset) {
        if (body == null) {
            return this;
        }
        return new Request(url, body, pathParams, formParams, headers, charset);
    }

    public Request body(String body) {
        if (body == null) {
            return this;
        }
        return new Request(url, body.getBytes(charset), pathParams, formParams, headers, charset);
    }

    public byte[] body() {
        return body;
    }

    public String bodyAsString() {
        return new String(body, charset);
    }

    public Charset charset() {
        return charset;
    }

    public Request charset(Charset charset) {
        return new Request(url, body, pathParams, formParams, headers, charset);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("URL: ")
          .append(url());
        if (hasPathParams()) {
            sb.append("?")
              .append(pathParamsAsString());
        }
        if (!headers().isEmpty()) {
            sb.append("\nHeaders:\n")
              .append(prettyPrintMap(headers()));
        }
        if (hasBody()) {
            sb.append("\nBody:\n")
              .append(bodyAsString());
        }

        return sb.toString();
    }

    private String prepareParams(Map<String, Set<Object>> params) {
        return params.entrySet()
                     .stream()
                     .flatMap(entry -> entry.getValue()
                                            .stream()
                                            .map(val -> urlEncode(entry.getKey()) + "=" + urlEncode(val.toString())))
                     .collect(Collectors.joining("&"));
    }

    private static String urlEncode(String str) {
        try {
            return URLEncoder.encode(str, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private static Map<String, Set<String>> newHeaders() {
        return new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    }
}
