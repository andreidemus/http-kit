package com.andreidemus.http.client;

import com.andreidemus.http.common.Request;
import com.xebialabs.restito.semantics.Condition;
import org.glassfish.grizzly.http.Method;
import org.junit.Test;

import java.util.*;

import static com.xebialabs.restito.builder.verify.VerifyHttp.verifyHttp;
import static com.xebialabs.restito.semantics.Condition.*;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
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

        verifyHttp(server).times(
                2,
                method(Method.GET),
                uri(path),
                withMultiHeader("header1", "val1-1", "val1-2"),
                withHeader("header2", "val2"),
                Condition.not(withHeader("header0"))
        );
    }

    @Test
    public void testFormParams() throws Exception {
        final String path = "/test-form-params";

        final String url = getUrl(path);
        final Request req1 = new Request(url).formParam("param1", 1)
                                             .formParam("param1", 2)
                                             .formParam("param2", "val2")
                                             .header("Content-Type", "text/plain"); // hack restito
        new RequestsClient().post(req1);

        // test reset params by single method
        final Map<String, Set<Object>> params2 = new LinkedHashMap<>();
        params2.put("param1", new LinkedHashSet<>(asList(1, 2)));
        params2.put("param2", new LinkedHashSet<>(singletonList("val2")));
        params2.put("param3", new LinkedHashSet<>());
        params2.put("param4", null);
        final Request req2 = new Request(url).formParam("param5", "val2")
                                             .formParams(params2)
                                             .header("Content-Type", "text/plain"); // hack restito

        Requests.post(req2);

        server.getCalls().forEach(it -> System.out.println(it.getPostBody()));

        verifyHttp(server).times(
                2,
                method(Method.POST),
                uri(path),
                withPostBodyContaining("param1=1&param1=2&param2=val2")
        );
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

        verifyHttp(server).times(
                2,
                method(Method.POST),
                uri(path),
                parameter("param1", "1", "2"),
                parameter("param2", "val2"),
                not(hasParameter("param3")),
                not(hasParameter("param4"))
        );
    }

    @Test
    public void testRequestBody() throws Exception {
        final String path = "/test-request-body";

        final Request request = new Request(getUrl(path))
                .header("Content-Type", "text/html")
                .body("request body");

        Requests.post(request);

        verifyHttp(server).once(
                method(Method.POST),
                uri(path),
                withHeader("Content-Type", "text/html"),
                withPostBodyContaining("request body")
        );
    }

    @Test
    public void testDefaultContentTypeForTextBody() throws Exception {
        final String path = "/test-request-body";

        final Request request = new Request(getUrl(path)).body("request body");

        Requests.post(request);

        server.getCalls().forEach(it -> System.out.println(it.getHeaders() + "\n" + it.getParameters()));

        verifyHttp(server).once(
                method(Method.POST),
                uri(path),
                withHeader("Content-Type", "text/plain; UTF-8"),
                withHeader("Content-Length", "12"),
                withPostBodyContaining("request body")
        );
    }

    @Test
    public void testDefaultContentTypeForUrlencodedForm() throws Exception {
        final String path = "/test-request-body";

        final Request request = new Request(getUrl(path)).formParam("a_key", "a_val");

        Requests.post(request);

        verifyHttp(server).once(
                method(Method.POST),
                uri(path),
                parameter("a_key", "a_val"),
                withHeader("Content-Type", "application/x-www-form-urlencoded"),
                withHeader("Content-Length", "11")
        );
    }

    @Test
    public void testRequestBodyOverridesFormParams() throws Exception {
        final String path = "/test-request-body";

        final Request request = new Request(getUrl(path)).formParam("param1", 1)
                                                         .body("request body")
                                                         .formParam("param2", 2)
                                                         .header("Content-Type", "text/plain");

        System.out.println(request);
        Requests.post(request);

        verifyHttp(server).once(
                method(Method.POST),
                uri(path),
                withPostBodyContaining("request body")
        );
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

        Request r = new Request(getUrl(path)).header("User-Agent", "Custom User Agent");
        Requests.get(r);

        server.getCalls().forEach(it -> System.out.println(it.getHeaders()));

        verifyHttp(server).once(
                method(Method.GET),
                uri(path),
                withHeader("User-Agent", "Custom User Agent")
        );
    }

    @Test
    public void testDefaultUserAgent() throws Exception {
        final String path = "/test-default-user-agent";

        Requests.get(getUrl(path));

        verifyHttp(server).once(
                method(Method.GET),
                uri(path),
                withHeader("User-Agent", "Java-Requests/0.0.1")
        );
    }

    @Test
    public void testPath() throws Exception {
        Request r = new Request(getUrl("")).path("test")
                                           .path("composite")
                                           .path("path");
        Requests.get(r);

        verifyHttp(server).once(
                method(Method.GET),
                uri("/test/composite/path"),
                withHeader("User-Agent", "Java-Requests/0.0.1")
        );
    }
}
