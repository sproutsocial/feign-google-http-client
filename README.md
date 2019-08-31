# Google Http Client - Feign Client

# DEPRECATED: THIS IS IN [FEIGN CORE](https://github.com/OpenFeign/feign/tree/master/googlehttpclient) AS OF VERSION 10.4.0

This library is a feign [Client](https://github.com/OpenFeign/feign/blob/master/core/src/main/java/feign/Client.java) to use the java [Google Http Client](https://github.com/googleapis/google-http-java-client).

To use this, add to your classpath (via maven, or otherwise). Then cofigure Feign to use the GoogleHttpClient:

```java
GitHub github = Feign.builder()
                     .client(new GoogleHttpClient())
                     .target(GitHub.class, "https://api.github.com");
```

This client is used in [Sprout Social's](http://sproutsocial.com) production environment.
