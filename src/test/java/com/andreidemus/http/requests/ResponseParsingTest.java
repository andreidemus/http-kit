package com.andreidemus.http.requests;

import org.glassfish.grizzly.http.Method;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.junit.Test;

import static com.xebialabs.restito.builder.stub.StubHttp.whenHttp;
import static com.xebialabs.restito.semantics.Action.*;
import static com.xebialabs.restito.semantics.Condition.method;
import static com.xebialabs.restito.semantics.Condition.uri;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.Matchers.*;

public class ResponseParsingTest extends RequestsTest {
    @Test
    public void responseBodyParsedCorrectly() throws Exception {
        final String path = "/body-parsed-correctly";

        whenHttp(server).match(
                method(Method.GET),
                uri(path)
        ).then(ok(), stringContent("©©©test body їїїєєє"));

        final Response resp = Requests.get(getUrl(path));

        assertThat(resp.status(), is(200));
        assertThat(resp.text(), is("©©©test body їїїєєє"));
    }

    @Test
    public void errorResponseParsedCorrectly() throws Exception {
        final String path = "/error-response-parsed-correctly";

        whenHttp(server).match(
                method(Method.GET),
                uri(path)
        ).then(status(HttpStatus.UNAUTHORIZED_401), stringContent("error body"));

        final Response resp = Requests.get(getUrl(path));
        assertThat(resp.status(), is(401));
        assertThat(resp.hasBody(), is(true));
        assertThat(resp.text(), is("error body"));
    }

    @Test
    public void headersParsedCorrectly() throws Exception {
        final String path = "/headers-parsed-correctly";

        whenHttp(server).match(
                method(Method.GET),
                uri(path)
        ).then(
                ok(),
                header("Header1", "value1"),
                header("Header2", "value2")
        );

        final Response resp = Requests.get(getUrl(path));
        assertThat(resp.header("Header1"), contains("value1"));
        assertThat(resp.header("header2"), contains("value2"));
    }
}
