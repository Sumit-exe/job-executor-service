package com.airtribe.jobexecutor.dto;

import lombok.Data;

import java.util.Map;

@Data
public class HttpJobData {
    private String method; // GET, POST, PUT, DELETE
    private String url;

    private Map<String, String> headers;
    private Map<String, String> queryParams;
    private Map<String, String> pathParams;

    private Object body;
}