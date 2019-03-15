/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2019 Sprout Social
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
*SOFTWARE.
*/


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

    private final HttpRequest convertRequest(final Request inputRequest,
                                             final Request.Options options) throws IOException {
        // Setup the request body
        HttpContent content = null;
        if (inputRequest.requestBody().length() > 0) {
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
            headers.set(header.getKey(), header.getValue());
        }
        // Some servers don't do well with no Accept header
        if (inputRequest.headers().get("Accept") == null) {
            headers.setAccept("*/*");
        }
        request.setHeaders(headers);

        // Setup request options
        request.setReadTimeout(options.readTimeoutMillis())
            .setConnectTimeout(options.connectTimeoutMillis())
            .setFollowRedirects(options.isFollowRedirects())
            .setThrowExceptionOnExecuteError(false);
        return request;
    }

    private final Response convertResponse(final Request inputRequest,
                                           final HttpResponse inputResponse) throws IOException {
        final HttpHeaders headers = inputResponse.getHeaders();
        Integer contentLength = null;
        if (headers.getContentLength() != null && headers.getContentLength() <= Integer.MAX_VALUE) {
            contentLength = inputResponse.getHeaders().getContentLength().intValue();
        }
        return Response.builder()
            .body(inputResponse.getContent(), contentLength)
            .status(inputResponse.getStatusCode())
            .reason(inputResponse.getStatusMessage())
            .headers(toMap(inputResponse.getHeaders()))
            .request(inputRequest)
            .build();
    }

    private final Map<String, Collection<String>> toMap(final HttpHeaders headers) {
        final Map<String, Collection<String>> map = new HashMap<String, Collection<String>>();
        for (final String header : headers.keySet()) {
            map.put(header, headers.getHeaderStringValues(header));
        }
        return map;
    }
}
