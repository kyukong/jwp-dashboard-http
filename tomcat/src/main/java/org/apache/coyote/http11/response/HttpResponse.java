package org.apache.coyote.http11.response;

import org.apache.coyote.http11.common.ResourceType;
import org.apache.coyote.http11.common.HeaderKeys;
import org.apache.coyote.http11.common.HttpHeaders;
import org.apache.coyote.http11.common.HttpMessageDelimiter;
import org.apache.coyote.http11.request.HttpRequest;
import org.apache.coyote.http11.response.header.StatusCode;
import org.apache.coyote.http11.response.header.StatusLine;
import org.apache.coyote.http11.util.ResourceSearcher;

public class HttpResponse {

    private static final ResourceSearcher RESOURCE_SEARCHER = ResourceSearcher.getInstance();
    private static final String FILE_REGEX = "\\.";
    private static final int EXTENSION_LOCATION = 1;

    private static final String UNAUTHORIZED_RESOURCE = "/401.html";
    private static final String NOT_FOUNT_RESOURCE = "/404.html";

    private final StatusLine statusLine;
    private final HttpHeaders httpHeaders;
    private HttpResponseBody body;

    private HttpResponse(final StatusLine statusLine, final HttpHeaders httpHeaders, final HttpResponseBody body) {
        this.statusLine = statusLine;
        this.httpHeaders = httpHeaders;
        this.body = body;
    }

    public static HttpResponse init(final HttpRequest httpRequest) {
        final StatusLine statusLine = new StatusLine(httpRequest.getHttpVersion(), StatusCode.init());
        final HttpHeaders httpHeaders = HttpHeaders.init();
        final HttpResponseBody body = HttpResponseBody.init();

        return new HttpResponse(statusLine, httpHeaders, body);
    }

    public HttpResponse setStatusCode(final StatusCode statusCode) {
        statusLine.setStatusCode(statusCode);
        return this;
    }

    public HttpResponse setLocation(final String redirect) {
        httpHeaders.add(HeaderKeys.LOCATION, redirect);
        return this;
    }

    public HttpResponse setBody(final String resource) {
        final String contentType = selectContentType(resource);
        body = loadResourceContent(resource);

        setHttpHeaders(contentType, body.getValue());
        return this;
    }

    public HttpResponse generateSessionId() {
        httpHeaders.generateSessionId();
        return this;
    }

    public void unauthorized() {
        setStatusCode(StatusCode.UNAUTHORIZED)
            .setLocation(UNAUTHORIZED_RESOURCE)
            .setBody(UNAUTHORIZED_RESOURCE);
    }

    public void notFound() {
        setStatusCode(StatusCode.NOT_FOUND)
            .setLocation(NOT_FOUNT_RESOURCE)
            .setBody(NOT_FOUNT_RESOURCE);
    }

    private void setHttpHeaders(final String contentType, final String body) {
        int length = body.getBytes().length;

        httpHeaders.add(HeaderKeys.CONTENT_TYPE, contentType + ";charset=utf-8")
            .add(HeaderKeys.CONTENT_LENGTH, String.valueOf(length));
    }

    private String selectContentType(final String resource) {
        final String[] fileElements = resource.split(FILE_REGEX);

        return ResourceType.getContentType(fileElements[EXTENSION_LOCATION]);
    }

    private HttpResponseBody loadResourceContent(final String resource) {
        return HttpResponseBody.from(RESOURCE_SEARCHER.loadContent(resource));
    }

    public String toMessage() {
        return String.join(HttpMessageDelimiter.LINE.getValue(),
            statusLine.toMessage(),
            httpHeaders.toMessage(),
            HttpMessageDelimiter.HEADER_BODY.getValue(),
            body.getValue()
        );
    }

    public String getSessionId() {
        return httpHeaders.getSessionId();
    }
}
