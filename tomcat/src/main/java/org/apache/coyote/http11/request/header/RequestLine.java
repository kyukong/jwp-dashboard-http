package org.apache.coyote.http11.request.header;

import org.apache.coyote.http11.common.HttpMessageDelimiter;

public class RequestLine {

    private static final int METHOD = 0;
    private static final int RESOURCE = 1;
    private static final int HTTP_VERSION = 2;

    private final Method method;
    private final Resource resource;
    private final String httpVersion;

    private RequestLine(final Method method, final Resource resource, final String httpVersion) {
        this.method = method;
        this.resource = resource;
        this.httpVersion = httpVersion;
    }

    public static RequestLine from(final String requestLine) {
        final String[] requestLineElement = requestLine.split(HttpMessageDelimiter.WORD.getValue());

        return new RequestLine(
            Method.from(requestLineElement[METHOD]),
            Resource.from(requestLineElement[RESOURCE]),
            requestLineElement[HTTP_VERSION]
        );
    }

    public Method getMethod() {
        return method;
    }

    public String getUrl() {
        return resource.getUrl();
    }

    public String getHttpVersion() {
        return httpVersion;
    }
}
