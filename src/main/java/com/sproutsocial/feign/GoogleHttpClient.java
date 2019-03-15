package com.sproutsocial.feign;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;

import feign.Client;
import feign.Request;
import feign.Response;
import feign.Util;

public class GoogleHttpClient implements Client {
    private final HttpTransport transport;
    private final HttpRequestFactory requestFactory;

    public GoogleHttpClient() {
        this(new NetHttpTransport());
    }

    public GoogleHttpClient(final HttpTransport transport) {
        this.transport = transport;
        this.requestFactory = transport.createRequestFactory();
    }

    @Override
    public final Response execute(final Request inputRequest,
                                  final Request.Options options) throws IOException {
        final HttpRequest request = convertRequest(inputRequest, options);
        final HttpResponse response = request.execute();

        return convertResponse(inputRequest, response);
    }

    private final Response convertResponse(final Request inputRequest,
                                           final HttpResponse inputResponse) throws IOException {
        return Response.builder()
            .status(inputResponse.getStatusCode())
            .reason(inputResponse.getStatusMessage())
            .headers(toMap(inputResponse.getHeaders()))
            .body(inputResponse.getContent(), inputResponse.getHeaders().getContentLength().intValue())
            .request(inputRequest)
            .build();
    }

    private final HttpRequest convertRequest(final Request inputRequest,
                                             final Request.Options options) throws IOException {
        // Setup request body
        HttpContent content = null;
        if (inputRequest.requestBody() != null) {
            final Collection<String> contentTypeValues = inputRequest.headers().get("Content-Type");
            String contentType = null;
            if (contentTypeValues != null && contentTypeValues.size() > 0) {
                contentType = contentTypeValues.iterator().next();
            } else {
                contentType = "application/octet-stream";
            }
            content = new ByteArrayContent(contentType, inputRequest.requestBody().asBytes());
        }

        // Build the request
        final HttpRequest request = requestFactory.buildRequest(inputRequest.httpMethod().name(),
                                                                new GenericUrl(inputRequest.url()),
                                                                content);

        // Setup headers
        final HttpHeaders headers = new HttpHeaders();
        for (final Map.Entry<String, Collection<String>> header : inputRequest.headers().entrySet()) {
            for (final String value : header.getValue()) {
                headers.set(header.getKey(), value);
            }
        }
        request.setHeaders(headers);

        // Setup request options
        request.setReadTimeout(options.readTimeoutMillis())
            .setConnectTimeout(options.connectTimeoutMillis())
            .setFollowRedirects(options.isFollowRedirects());
        
        return request;
    }

    private final Map<String, Collection<String>> toMap(final HttpHeaders headers) {
        final Map<String, Collection<String>> map = new HashMap<String, Collection<String>>();
        for (final String header : headers.keySet()) {
            map.put(header, headers.getHeaderStringValues(header));
        }
        return map;
    }
}
