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
import org.springframework.web.client.HttpClientErrorException;

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
            if (request.getMethod() == HttpMethod.PATCH) {
                throw new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS, String.format("Unable to execute the request. Please try again in %ss.", retryAfterHeader));
            }
            if (retryAfterHeader != null) {
                try {
                    long seconds = Long.parseLong(retryAfterHeader.trim());
                    sleepMillis = Math.min(seconds * 1_000L, 60_000L);
                } catch (NumberFormatException ignored) {
                    logger.error("Retry-After header has invalid value: " + retryAfterHeader);
                }
            }
            try {
                TimeUnit.MILLISECONDS.sleep(sleepMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("API retry call interrupted.", e);
            } finally {
                response.close();
            }
            response = execution.execute(request, body);
        }
        return response;
    }
}