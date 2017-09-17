package com.andreidemus.http.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;

public class Responses {
    private final int numThreads;
    private final ExecutorService requestsExecutor;
    private final ExecutorService mainExecutor;
    private final AtomicInteger count = new AtomicInteger();
    private final List<ServerRequest> requests = Collections.synchronizedList(new LinkedList<>());
    private String stubbedResponse = "HTTP/1.1 200 OK\n" +
            "Server: Http Debug\n" +
            "Content-Type: text/plain; charset=utf-8\n" +
            "Content-Length: 21\n" +
            "\n" +
            "This is response body";

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

        return socket.getLocalPort();
    }


    public void stop() {
        throw new RuntimeException("Not implemented."); // TODO implement
    }

    public Queue<ServerRequest> getRequests() {
        final Queue<ServerRequest> requests = new LinkedList<>();
        requests.addAll(this.requests);
        return requests;
    }

    public void stubResponse(String stubbedResponse) {
        this.stubbedResponse = stubbedResponse;
    }

    private void handleConnection(Socket connection) {
        requestsExecutor.execute(() -> {
            try {
                System.out.println("Connection #" + count.addAndGet(1));
                final ServerRequest request = readRequest(connection.getInputStream());
                requests.add(request);
                dumpRequest(request);

                OutputStream out = connection.getOutputStream();
                out.write(stubbedResponse.getBytes());
                out.flush();

                connection.close();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    private ServerRequest readRequest(InputStream in) {
        final ServerRequest request = new ServerRequest();
        try {
            final String startLine = readStartLine(in);
            final String[] parsedStartLine = parseStartLine(startLine);
            request.rawStartLine = startLine;
            request.method = parsedStartLine[0];
            request.url = parsedStartLine[1];
            request.protocol = parsedStartLine[2];

            final List<String> headers = readHeaders(in);
            request.rawHeaders = String.join("\n", headers);
            request.headers = parseHeaders(headers);

            int available = in.available();
            if (available > 0) {
                byte[] bytes = new byte[available];
                int actuallyRead = in.read(bytes);
                if (actuallyRead != available) {
                    throw new IOException("Request body was not read completely.");
                }
                request.body = bytes;
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return request;
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

    private Map<String, List<String>> parseHeaders(List<String> headers) {
        return headers.stream()
                      .map(it -> it.split(":"))
                      .filter(it -> it.length == 2)
                      .collect(Collectors.toMap(
                              it -> it[0],
                              it -> singletonList(it[1].trim()),
                              (a, b) -> { // TODO improve performance
                                  List<String> result = new LinkedList<>();
                                  result.addAll(a);
                                  result.addAll(b);
                                  return result;
                              },
                              () -> new TreeMap<>(String.CASE_INSENSITIVE_ORDER)
                      ));
    }

    private void dumpRequest(ServerRequest request) { // TODO use logger
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
