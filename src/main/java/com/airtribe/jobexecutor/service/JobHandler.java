package com.airtribe.jobexecutor.service;


import com.airtribe.jobexecutor.dto.JobPayload;

public interface JobHandler {
    void execute(JobPayload payload);
}
