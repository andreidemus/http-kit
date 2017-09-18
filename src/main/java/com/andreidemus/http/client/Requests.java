package com.andreidemus.http.client;

import com.andreidemus.http.common.Request;
import com.andreidemus.http.common.Response;

/**
 * Performs HTTP requests
 */
public class Requests {
    private static RequestsClient requests = new RequestsClient(); // TODO lazy singleton

    /**
     * Performs GET request
     *
     * @param request the Request instance
     * @return the Response instance
     */
    public static Response get(Request request) {
        return requests.get(request);
    }

    /**
     * Performs POST request
     *
     * @param request the Request instance
     * @return the Response instance
     */
    public static Response post(Request request) {
        return requests.post(request);
    }

    /**
     * Performs PUT request
     *
     * @param request the Request instance
     * @return the Response instance
     */
    public static Response put(Request request) {
        return requests.put(request);
    }

    /**
     * Performs DELETE request
     *
     * @param request the Request instance
     * @return the Response instance
     */
    public static Response delete(Request request) {
        return requests.delete(request);
    }

    /**
     * Performs HEAD request
     *
     * @param request the Request instance
     * @return the Response instance
     */
    public static Response head(Request request) {
        return requests.head(request);
    }

    /**
     * Performs GET request
     *
     * @param url the url to be requested
     * @return the Response instance
     */
    public static Response get(String url) {
        return requests.get(new Request(url));
    }

    /**
     * Performs POST request
     *
     * @param url the url to be requested
     * @return the Response instance
     */
    public static Response post(String url) {
        return requests.post(new Request(url));
    }

    /**
     * Performs PUT request
     *
     * @param url the url to be requested
     * @return the Response instance
     */
    public static Response put(String url) {
        return requests.put(new Request(url));
    }

    /**
     * Performs DELETE request
     *
     * @param url the url to be requested
     * @return the Response instance
     */
    public static Response delete(String url) {
        return requests.delete(new Request(url));
    }

    /**
     * Performs HEAD request
     *
     * @param url the url to be requested
     * @return the Response instance
     */
    public static Response head(String url) {
        return requests.head(new Request(url));
    }
}
