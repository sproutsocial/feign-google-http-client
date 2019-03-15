package com.sproutsocial.feign;

import feign.Feign;
import feign.Feign.Builder;
import feign.client.AbstractClientTest;

public class GoogleHttpClientTest extends AbstractClientTest {
    @Override
    public Builder newBuilder() {
        return Feign.builder()
            .client(new GoogleHttpClient());
    }
}
