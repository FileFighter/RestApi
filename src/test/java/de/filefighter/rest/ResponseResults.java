package de.filefighter.rest;

import io.cucumber.messages.internal.com.google.common.io.CharStreams;
import lombok.Getter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

@Getter
public class ResponseResults {
    private final ClientHttpResponse theResponse;
    private final String body;
    private final HttpHeaders headers;

    ResponseResults(final ClientHttpResponse response) throws IOException {
        this.theResponse = response;
        this.headers = response.getHeaders();

        String text;
        try (Reader reader = new InputStreamReader(response.getBody())) {
            text = CharStreams.toString(reader);
        }
        body = text;
    }
}