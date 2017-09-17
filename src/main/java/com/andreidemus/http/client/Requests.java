package com.andreidemus.http.client;

import com.andreidemus.http.common.Request;
import com.andreidemus.http.common.Response;

public class Requests {
    private static RequestsClient requests = new RequestsClient(); // TODO lazy singleton

    public static Response get(Request request) {
        return requests.get(request);
    }

    public static Response post(Request request) {
        return requests.post(request);
    }

    public static Response put(Request request) {
        return requests.put(request);
    }

    public static Response delete(Request request) {
        return requests.delete(request);
    }

    public static Response head(Request request) {
        return requests.head(request);
    }

    public static Response get(String url) {
        return requests.get(new Request(url));
    }

    public static Response post(String url) {
        return requests.post(new Request(url));
    }

    public static Response put(String url) {
        return requests.put(new Request(url));
    }

    public static Response delete(String url) {
        return requests.delete(new Request(url));
    }

    public static Response head(String url) {
        return requests.head(new Request(url));
    }

}
