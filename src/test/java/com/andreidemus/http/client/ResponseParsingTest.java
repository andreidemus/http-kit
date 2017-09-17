package com.andreidemus.http.client;

import com.andreidemus.http.common.Response;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.Matchers.*;

public class ResponseParsingTest extends RequestsTest {
    @Test
    public void responseBodyParsedCorrectly() throws Exception {
        final String path = "/body-parsed-correctly";

        server.stubResponse("HTTP/1.1 200 OK\n" +
                "Server: Http Debug stubbed\n" +
                "Content-Type: text/plain; charset=utf-8\n" +
                "Content-Length: 28\n" +
                "\n" +
                "©©©test body їїїєєє");

        final Response resp = Requests.get(getUrl(path));

        assertThat(resp.status(), is(200));
        assertThat(resp.text(), is("©©©test body їїїєєє"));
    }

    @Test
    public void errorResponseParsedCorrectly() throws Exception {
        final String path = "/error-response-parsed-correctly";

        server.stubResponse("HTTP/1.1 401 Unauthorized\n" +
                "Server: Http Debug stubbed\n" +
                "Content-Type: text/plain; charset=utf-8\n" +
                "Content-Length: 10\n" +
                "\n" +
                "error body");

        final Response resp = Requests.get(getUrl(path));
        assertThat(resp.status(), is(401));
        assertThat(resp.hasBody(), is(true));
        assertThat(resp.text(), is("error body"));
    }

    @Test
    public void headersParsedCorrectly() throws Exception {
        final String path = "/headers-parsed-correctly";

        server.stubResponse("HTTP/1.1 200 OK\n" +
                "Server: Http Debug stubbed\n" +
                "Header1: value1\n" +
                "Header2: value2\n");

        final Response resp = Requests.get(getUrl(path));
        System.out.println(resp);
        assertThat(resp.header("Header1"), contains("value1"));
        assertThat(resp.header("header2"), contains("value2"));
    }
}
