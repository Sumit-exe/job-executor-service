package com.airtribe.jobexecutor.service.impl;

import com.airtribe.jobexecutor.dto.HttpJobData;
import com.airtribe.jobexecutor.dto.JobPayload;
import com.airtribe.jobexecutor.service.JobHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class HttpJobHandler implements JobHandler {

    private final WebClient webClient;

    @Override
    public void execute(JobPayload payload) {

        HttpJobData data = payload.getData();

        String url = buildUrl(data);

        WebClient.RequestBodySpec request = webClient
                .method(HttpMethod.valueOf(data.getMethod()))
                .uri(url);

        // Headers
        if (data.getHeaders() != null) {
            request.headers(h -> data.getHeaders().forEach(h::set));
        }

        WebClient.ResponseSpec response;

        // Body (only for POST/PUT/PATCH)
        if (requiresBody(data.getMethod())) {
            response = request
                    .bodyValue(data.getBody() != null ? data.getBody() : "")
                    .retrieve();
        } else {
            response = request.retrieve();
        }

        // Execute (block since your system is not reactive end-to-end)
        String result = response
                .bodyToMono(String.class)
                .block();

        // Optional logging
        System.out.println("Response: " + result);
    }

    private String buildUrl(HttpJobData data) {

        String url = data.getUrl();

        // Path params
        if (data.getPathParams() != null) {
            for (Map.Entry<String, String> entry : data.getPathParams().entrySet()) {
                url = url.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }

        // Query params
        if (data.getQueryParams() != null && !data.getQueryParams().isEmpty()) {
            StringBuilder sb = new StringBuilder(url).append("?");

            data.getQueryParams().forEach((k, v) ->
                    sb.append(k).append("=").append(v).append("&"));

            url = sb.substring(0, sb.length() - 1);
        }

        return url;
    }

    private boolean requiresBody(String method) {
        return method.equalsIgnoreCase("POST")
                || method.equalsIgnoreCase("PUT")
                || method.equalsIgnoreCase("PATCH");
    }
}