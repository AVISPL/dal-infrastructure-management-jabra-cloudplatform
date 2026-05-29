/*
 * Copyright (c) 2025 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.data;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Interceptor for RestTemplate that checks for the response headers, important for proper handling of 429 responses.
 *
 * @author Maksym.Rossiytsev
 * @since 1.0.0
 */
public class JabraCloudRequestInterceptor implements ClientHttpRequestInterceptor {
    private final Log logger = LogFactory.getLog(this.getClass());

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        ClientHttpResponse response = execution.execute(request, body);
        if (response.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
            String retryAfterHeader = response.getHeaders().getFirst("Retry-After");
            long sleepMillis = 1_000L;
            if (retryAfterHeader != null) {
                try {
                    long seconds = Long.parseLong(retryAfterHeader.trim());
                    sleepMillis = Math.min(seconds * 1_000L, 60_000L);
                } catch (NumberFormatException ignored) {
                    logger.error("Retry-After header has invalid value: " + retryAfterHeader);
                }
            }
            // Drain the response body BEFORE closing. Pooled HTTP clients (e.g. Apache HttpClient
            // used by the parent container) only return a connection to the pool once its body is
            // fully consumed. Closing without draining leaves the connection marked "in use",
            // exhausting the pool and causing the retry execute() call to block indefinitely.
            drainAndClose(response);

            if (request.getMethod() == HttpMethod.PATCH || request.getMethod() == HttpMethod.POST) {
                throw new RuntimeException(String.format("Unable to execute the request: Too many requests. Please try again in %ss.", retryAfterHeader));
            }
            try {
                TimeUnit.MILLISECONDS.sleep(sleepMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("API retry call interrupted.", e);
            }
            response = execution.execute(request, body);
        }
        return response;
    }

    /**
     * Drains the response body so the underlying connection is returned to the pool,
     * then closes the response. Must be called before retrying on a pooled HTTP client.
     *
     * @param response entity to process and close
     */
    private void drainAndClose(ClientHttpResponse response) {
        try {
            StreamUtils.drain(response.getBody());
        } catch (IOException e) {
            logger.warn("Failed to drain 429 response body — connection may not be released cleanly: " + e.getMessage());
        } finally {
            response.close();
        }
    }
}