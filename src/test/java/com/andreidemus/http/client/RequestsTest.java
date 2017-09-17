package com.andreidemus.http.client;

import com.xebialabs.restito.server.StubServer;
import org.junit.After;
import org.junit.Before;

public abstract class RequestsTest {
    StubServer server;

    @Before
    public void setUp() throws Exception {
        server = new StubServer().run();
    }

    @After
    public void tearDown() throws Exception {
        server.stop();
    }

    String getUrl(String path) {
        return "http://127.0.0.1:" + server.getPort() + path;
    }
}