package de.filefighter.rest;

import java.io.*;

import io.cucumber.messages.internal.com.google.common.io.CharStreams;
import org.springframework.http.client.ClientHttpResponse;

public class ResponseResults {
    private final ClientHttpResponse theResponse;
    private final String body;

    ResponseResults(final ClientHttpResponse response) throws IOException {
        this.theResponse = response;

        String text;
        try (Reader reader = new InputStreamReader(response.getBody())) {
            text = CharStreams.toString(reader);
        }
        body = text;
    }

    public ClientHttpResponse getTheResponse() {
        return theResponse;
    }

    public String getBody() {
        return body;
    }
}