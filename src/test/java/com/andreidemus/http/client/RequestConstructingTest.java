package com.andreidemus.http.client;

import com.andreidemus.http.common.Request;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.util.*;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.core.Is.is;

public class RequestConstructingTest extends RequestsTest {
    @Test
    public void testHeaders() throws Exception {
        final String path = "/test-headers";

        final String url = getUrl(path);
        final Request req1 = new Request(url).header("header1", "val1-1")
                                             .header("header1", "val1-2")
                                             .header("header2", "val2");
        Requests.get(req1);

        // test reset headers by single method
        final Map<String, Set<String>> headers2 = new LinkedHashMap<>();
        headers2.put("header1", new LinkedHashSet<>(asList("val1-1", "val1-2")));
        headers2.put("header2", new LinkedHashSet<>(singletonList("val2")));
        final Request req2 = new Request(url).header("header3", "val3")
                                             .headers(headers2);

        Requests.get(req2);

        final Request request1 = server.requests().poll();

        assertThat(request1.headers(), hasEntry(is("header1"), contains("val1-1", "val1-2")));
        assertThat(request1.headers(), hasEntry(is("header2"), contains("val2")));
        assertThat(request1.path(), is(path));

        final Request request2 = server.requests().poll();
        assertThat(request2.headers(), hasEntry(is("header1"), contains("val1-1", "val1-2")));
        assertThat(request2.headers(), hasEntry(is("header2"), contains("val2")));
        assertThat(request2.headers(), CoreMatchers.not(hasKey("header3")));
        assertThat(request2.path(), is(path));
    }

    @Test
    public void testFormParams() throws Exception {
        final String path = "/test-form-params";

        final String url = getUrl(path);
        final Request req1 = new Request(url).formParam("param1", 1)
                                             .formParam("param1", 2)
                                             .formParam("param2", "val2");
        new RequestsClient().post(req1);

        // test reset params by single method
        final Map<String, Set<Object>> params2 = new LinkedHashMap<>();
        params2.put("param1", new LinkedHashSet<>(asList(1, 2)));
        params2.put("param2", new LinkedHashSet<>(singletonList("val2")));
        params2.put("param3", new LinkedHashSet<>());
        params2.put("param4", null);
        final Request req2 = new Request(url).formParam("param5", "val2")
                                             .formParams(params2);

        Requests.post(req2);

        final Request request1 = server.requests().poll();
        assertThat(request1.method(), is("POST"));
        assertThat(request1.bodyAsString(), is("param1=1&param1=2&param2=val2"));

        final Request request2 = server.requests().poll();
        assertThat(request2.method(), is("POST"));
        assertThat(request2.bodyAsString(), is("param1=1&param1=2&param2=val2"));
    }

    @Test
    public void testPathParams() throws Exception {
        final String path = "/test-path-params";

        final String url = getUrl(path);
        final Request req1 = new Request(url).pathParam("param1", 1)
                                             .pathParam("param1", 2)
                                             .pathParam("param2", "val2");
        Requests.post(req1);

        // test reset params by single method
        final Map<String, Set<Object>> params2 = new LinkedHashMap<>();
        params2.put("param1", new LinkedHashSet<>(asList(1, 2)));
        params2.put("param2", new LinkedHashSet<>(singletonList("val2")));
        params2.put("param3", new LinkedHashSet<>());
        params2.put("param4", null);
        final Request req2 = new Request(url).pathParams(params2);

        Requests.post(req2);

        final Request request1 = server.requests().poll();
        assertThat(request1.path(), is("/test-path-params?param1=1&param1=2&param2=val2"));

        final Request request2 = server.requests().poll();
        assertThat(request2.path(), is("/test-path-params?param1=1&param1=2&param2=val2"));
    }

    @Test
    public void testRequestBody() throws Exception {
        final String path = "/test-request-body";

        final Request req = new Request(getUrl(path))
                .header("Content-Type", "text/html")
                .body("request body");

        Requests.post(req);

        final Request request = server.requests().poll();
        assertThat(request.headers(), hasEntry(is("Content-Type"), contains("text/html")));
        assertThat(request.bodyAsString(), is("request body"));
    }

    @Test
    public void testDefaultContentTypeForTextBody() throws Exception {
        final String path = "/test-request-body";

        final Request req = new Request(getUrl(path)).body("request body");

        Requests.post(req);

        final Request request = server.requests().poll();
        assertThat(request.headers(), hasEntry(is("Content-Type"), contains("text/plain; UTF-8")));
        assertThat(request.headers(), hasEntry(is("Content-Length"), contains("12")));
        assertThat(request.bodyAsString(), is("request body"));
    }

    @Test
    public void testDefaultContentTypeForUrlencodedForm() throws Exception {
        final String path = "/test-request-body";

        final Request req = new Request(getUrl(path)).formParam("a_key", "a_val");

        Requests.post(req);

        final Request request = server.requests().poll();
        assertThat(request.headers(), hasEntry(is("Content-Type"), contains("application/x-www-form-urlencoded")));
        assertThat(request.headers(), hasEntry(is("Content-Length"), contains("11")));
        assertThat(request.bodyAsString(), is("a_key=a_val"));
    }

    @Test
    public void testRequestBodyOverridesFormParams() throws Exception {
        final String path = "/test-request-body";

        final Request req = new Request(getUrl(path)).formParam("param1", 1)
                                                     .body("request body")
                                                     .formParam("param2", 2)
                                                     .header("Content-Type", "text/plain");

        Requests.post(req);

        final Request request = server.requests().poll();
        assertThat(request.headers(), hasEntry(is("Content-Type"), contains("text/plain")));
        assertThat(request.headers(), hasEntry(is("Content-Length"), contains("12")));
        assertThat(request.bodyAsString(), is("request body"));
    }

    @Test
    public void testToString() throws Exception {
        final Request request = new Request("http://example.com").path("path1")
                                                                 .header("header1", "value1-1")
                                                                 .header("header1", "value1-2")
                                                                 .header("header2", "value2")
                                                                 .pathParam("a", 5)
                                                                 .pathParam("a", 7)
                                                                 .pathParam("b", "test")
                                                                 .body("request body");

        final String expectedOutput = "URL: http://example.com/path1?a=5&a=7&b=test\n" +
                "Headers:\n" +
                "{\n" +
                "  header1 : [value1-1, value1-2]\n" +
                "  header2 : [value2]\n" +
                "}\n" +
                "Body:\n" +
                "request body";

        assertThat(request.toString(), is(expectedOutput));
    }

    @Test
    public void testUserAgent() throws Exception {
        final String path = "/test-user-agent";

        Request req = new Request(getUrl(path)).header("User-Agent", "Custom User Agent");
        Requests.get(req);

        final Request request = server.requests().poll();
        assertThat(request.headers(), hasEntry(is("User-Agent"), contains("Custom User Agent")));
    }

    @Test
    public void testDefaultUserAgent() throws Exception {
        final String path = "/test-default-user-agent";

        Requests.get(getUrl(path));

        final Request request = server.requests().poll();
        assertThat(request.headers(), hasEntry(is("User-Agent"), contains("Java-Requests/0.0.1")));
    }

    @Test
    public void testPath() throws Exception {
        Request req = new Request(getUrl("")).path("test")
                                             .path("composite")
                                             .path("path");
        Requests.get(req);

        final Request request1 = server.requests().poll();
        assertThat(request1.path(), is("/test/composite/path"));
    }
}
