package com.andreidemus.http.client;

import com.andreidemus.http.server.Responses;
import org.junit.Before;

public abstract class RequestsTest {
    Responses server;

    @Before
    public void setUp() throws Exception {
        server = new Responses();
        server.start();
    }

    String getUrl(String path) {
        return "http://127.0.0.1:" + server.port() + path;
    }
}