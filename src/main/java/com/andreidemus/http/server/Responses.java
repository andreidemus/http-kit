package com.andreidemus.http.server;

import com.andreidemus.http.common.Request;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;

public class Responses {
    private final int numThreads;
    private final ExecutorService requestsExecutor;
    private final ExecutorService mainExecutor;
    private final AtomicInteger count = new AtomicInteger();
    private final List<Request> requests = Collections.synchronizedList(new LinkedList<>());
    private volatile AtomicReference<String> stubbedResponse = new AtomicReference<>("HTTP/1.1 200 OK\n" +
            "Server: Http Debug1\n" +
            "Content-Type: text/plain; charset=utf-8\n" +
            "Content-Length: 21\n" +
            "\n" +
            "This is response body");
    private int port;

    public Responses() {
        numThreads = 5;
        requestsExecutor = Executors.newFixedThreadPool(numThreads);
        mainExecutor = Executors.newSingleThreadExecutor();
    }

    public int start() throws IOException {
        return start(7070);
    }

    public int start(int port) throws IOException {
        final ServerSocket socket = openSocket(port);
        System.out.println("Started server on port " + socket.getLocalPort()); // TODO use logger

        mainExecutor.submit(() -> {
            while (true) {
                try {
                    handleConnection(socket.accept());
                } catch (IOException e) {
                    e.printStackTrace(); // TODO use logger
                }
            }
        });

        this.port = socket.getLocalPort();

        return this.port;
    }


    public void stop() {
        throw new RuntimeException("Not implemented."); // TODO implement
    }

    public Queue<Request> requests() {
        final Queue<Request> requests = new LinkedList<>();
        requests.addAll(this.requests);
        return requests;
    }

    public void stubResponse(String newStubbedResponse) {
        this.stubbedResponse.set(newStubbedResponse);
    }

    public int port() {
        return port;
    }

    private void handleConnection(Socket connection) {
        requestsExecutor.execute(() -> {
            try {
                System.out.println(port + " : Connection #" + count.addAndGet(1));
                final Request request = readRequest(connection.getInputStream());
                requests.add(request);
                dumpRequest(request);

                OutputStream out = connection.getOutputStream();
                out.write(stubbedResponse.get().getBytes());
                out.flush();

                connection.close();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    private Request readRequest(InputStream in) {
        try {
            final String startLine = readStartLine(in);
            final String[] parsedStartLine = parseStartLine(startLine);
            final String method = parsedStartLine[0];
            final String path = parsedStartLine[1];
//            final String protocol = parsedStartLine[2]; // TODO add field to Request

            final List<String> unparsedHeaders = readHeaders(in);
            final Map<String, Set<String>> headers = parseHeaders(unparsedHeaders);

            final byte[] body;
            int available = in.available();
            if (available > 0) {
                byte[] bytes = new byte[available];
                int actuallyRead = in.read(bytes);
                if (actuallyRead != available) {
                    throw new IOException("Request body was not read completely.");
                }
                body = bytes;
            } else {
                body = new byte[]{};
            }
            return new Request(method, "", path, headers, body);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private String readStartLine(InputStream in) throws IOException {
        final DataInput dataInput = new DataInputStream(in);

        String line = dataInput.readLine();
        if (line == null)
            throw new IOException("Request does not have a start line.");

        /*
        From the HTTP/1.1 spec:
        "In the interest of robustness, servers SHOULD ignore any empty line(s) received where
         a Request-Line is expected. In other words, if the server is reading the protocol stream
         at the beginning of a message and receives a CRLF first, it should ignore the CRLF"
         */
        while (line.trim().isEmpty()) {
            line = dataInput.readLine();
            if (line == null)
                throw new IOException("Request does not have a start line.");
        }

        return line;
    }

    private String[] parseStartLine(String startLine) {
        String[] parsed = startLine.split("[ ]+");

        // Validate
        if (parsed.length != 3) {
            throw new HttpServerException("Start line is invalid. Start line:" + startLine);
        }
        //TODO check request method and protocol

        return parsed;
    }

    private List<String> readHeaders(InputStream in) throws IOException {
        List<String> headers = new ArrayList<>();

        final DataInput dataInput = new DataInputStream(in);

        // TODO replace with Stream for Java9 (use `takeWhile`)
        for (String line = dataInput.readLine(); line != null && !line.trim().isEmpty(); line = dataInput.readLine()) {
            headers.add(line);
        }

        return headers;
    }

    private Map<String, Set<String>> parseHeaders(List<String> headers) {
        return headers.stream()
                      .map(it -> it.split(":"))
                      .filter(it -> it.length == 2)
                      .collect(Collectors.toMap(
                              it -> it[0].trim(),
                              it -> new HashSet<>(singletonList(it[1].trim())),
                              (a, b) -> { // TODO improve performance
                                  Set<String> result = new HashSet<>();
                                  result.addAll(a);
                                  result.addAll(b);
                                  return result;
                              },
                              () -> new TreeMap<>(String.CASE_INSENSITIVE_ORDER)
                      ));
    }

    private void dumpRequest(Request request) { // TODO use logger
        System.out.println(request.toString() + "\n");
        System.out.flush();
    }

    private ServerSocket openSocket(int port) {
        try {
            return new ServerSocket(port);
        } catch (IOException e) {
            return openSocket(port + 1);
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        new Responses().start();
    }
}
