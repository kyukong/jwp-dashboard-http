package org.apache.coyote.http11;

import nextstep.jwp.db.InMemoryUserRepository;
import nextstep.jwp.exception.UncheckedServletException;
import nextstep.jwp.model.User;

import org.apache.coyote.Processor;
import org.apache.coyote.http11.response.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Map;

public class Http11Processor implements Runnable, Processor {

    private static final Logger log = LoggerFactory.getLogger(Http11Processor.class);

    private final Socket connection;

    public Http11Processor(final Socket connection) {
        this.connection = connection;
    }

    @Override
    public void run() {
        process(connection);
    }

    @Override
    public void process(final Socket connection) {
        try (final var inputStream = connection.getInputStream();
             final var outputStream = connection.getOutputStream();
             final var inputStreamReader = new InputStreamReader(inputStream);
             final var bufferedReader = new BufferedReader(inputStreamReader)) {

            final HttpResponse httpResponse = HttpResponse.from(bufferedReader);
            final String responseMessage = httpResponse.createResponseMessage();

            printQueries(httpResponse);

            outputStream.write(responseMessage.getBytes());
            outputStream.flush();
        } catch (IOException | UncheckedServletException e) {
            log.error(e.getMessage(), e);
        }
    }

    private static void printQueries(final HttpResponse httpResponse) {
        final Map<String, String> queries = httpResponse.getQueries();
        if (queries.isEmpty()) {
            return;
        }
        User user = InMemoryUserRepository.findByAccount(queries.get("account"))
            .orElseThrow(() -> new IllegalArgumentException("아이디 또는 비밀번호를 잘못 입력하였습니다."));
        log.info("user : {}", user);
    }
}
