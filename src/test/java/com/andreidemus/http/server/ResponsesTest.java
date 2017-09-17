package com.andreidemus.http.server;

import com.andreidemus.http.common.Request;
import com.andreidemus.http.client.Requests;
import com.andreidemus.http.common.Response;
import org.junit.Test;

import java.util.Queue;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertThat;

public class ResponsesTest {
    @Test
    public void testAccumulatingRequests() throws Exception {
        final Responses server = new Responses();
        final int port = server.start();

        final Request request1 = new Request("http://127.0.0.1:" + port).path("some_path")
                                                                        .header("Test Header", "test header value")
                                                                        .body("Some request body");
        final Request request2 = request1.body("Some other request body");

        Requests.post(request1);
        Requests.post(request2);

        final Queue<Request> requests = server.getRequests();
        assertThat(requests, hasSize(2));

        final Request serverRequest1 = requests.poll();
        final Request serverRequest2 = requests.poll();

        assertThat(serverRequest1.method(), is("POST"));
        assertThat(serverRequest1.path(), is("/some_path"));
        assertThat(serverRequest1.bodyAsString(), is("Some request body"));
        assertThat(serverRequest1.headers().get("test HeaDer"), hasItem("test header value"));

        assertThat(serverRequest2.bodyAsString(), is("Some other request body"));
    }

    @Test
    public void testStubbingResponse() throws Exception {
        final Responses server = new Responses();
        final int port = server.start();
        server.stubResponse(
                "HTTP/1.1 200 OK\n" +
                        "Server: Http Debug stubbed\n" +
                        "Content-Type: text/plain; charset=utf-8\n" +
                        "Content-Length: 21\n" +
                        "\n" +
                        "Stubbed response body"
        );

        final Response r = Requests.get("http://127.0.0.1:" + port);
        assertThat(r.status(), is(200));
        assertThat(r.reason(), is("OK"));
        assertThat(r.header("Server"), hasItem("Http Debug stubbed"));
        assertThat(r.header("Content-Type"), hasItem("text/plain; charset=utf-8"));
        assertThat(r.header("Content-Length"), hasItem("21"));
        assertThat(r.bodyAsString(), is("Stubbed response body"));
    }
}