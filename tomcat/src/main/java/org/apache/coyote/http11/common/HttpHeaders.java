package org.apache.coyote.http11.common;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HttpHeaders {

    private static final String KEY_VALUE_DELIMITER = ": ";
    private static final int KEY = 0;
    private static final int VALUE = 1;

    private final Map<HeaderKeys, String> headers;
    private final HttpCookie cookie;

    private HttpHeaders(final Map<HeaderKeys, String> headers, final HttpCookie cookie) {
        this.headers = headers;
        this.cookie = cookie;
    }

    public static HttpHeaders init() {
        return new HttpHeaders(new HashMap<>(), HttpCookie.empty());
    }

    public static HttpHeaders request(final List<String> messages) {
        final Map<HeaderKeys, String> headers = new HashMap<>();
        HttpCookie cookie = HttpCookie.empty();
        for (final String message : messages) {
            final String[] headerElement = message.split(KEY_VALUE_DELIMITER);
            if (HeaderKeys.isCookie(headerElement[KEY])) {
                cookie = HttpCookie.request(headerElement[VALUE]);
                continue;
            }
            headers.put(HeaderKeys.from(headerElement[KEY]), headerElement[VALUE]);
        }
        return new HttpHeaders(headers, cookie);
    }

    public HttpHeaders add(final HeaderKeys key, final String value) {
        this.headers.put(key, value);
        return this;
    }

    public HttpHeaders generateSessionId() {
        this.cookie.generateSessionId();
        return this;
    }

    public boolean hasSessionId() {
        return cookie.hasSessionId();
    }

    public String toMessage() {
        final StringBuilder message = new StringBuilder();
        for (Map.Entry<String, String> entry : getAllHeaders().entrySet()) {
            message.append(entry.getKey())
                .append(KEY_VALUE_DELIMITER)
                .append(entry.getValue())
                .append(HttpMessageDelimiter.WORD.getValue())
                .append(HttpMessageDelimiter.LINE.getValue());
        }
        excludeLastEmpty(message);
        return new String(message);
    }

    private Map<String, String> getAllHeaders() {
        final Map<String, String> allHeaders = convertStringFromObject();
        if (cookie.hasSessionId()) {
            allHeaders.put(HeaderKeys.SET_COOKIE.getName(), cookie.toMessage());
        }
        return sortHeaders(allHeaders);
    }

    private Map<String, String> convertStringFromObject() {
        return headers.entrySet().stream()
            .collect(Collectors.toMap(
                header -> header.getKey().getName(),
                Map.Entry::getValue
            ));
    }

    private Map<String, String> sortHeaders(final Map<String, String> headers) {
        return headers.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .collect(Collectors.toMap(
                Map.Entry::getKey, Map.Entry::getValue,
                (oldValue, newValue) -> oldValue,
                LinkedHashMap::new
            ));
    }

    private void excludeLastEmpty(final StringBuilder message) {
        message.deleteCharAt(message.length() - 1);
        message.deleteCharAt(message.length() - 1);
    }

    public int getContentLength() {
        final String contentLength = headers.get(HeaderKeys.CONTENT_LENGTH);
        if (contentLength == null) {
            return 0;
        }
        return Integer.parseInt(contentLength);
    }

    public String getSessionId() {
        return cookie.getSessionId();
    }
}
