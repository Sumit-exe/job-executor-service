package com.airtribe.jobexecutor.service.impl;

import com.airtribe.jobexecutor.dto.JobPayload;
import com.airtribe.jobexecutor.service.JobHandler;
import org.springframework.stereotype.Service;

@Service
public class InternalJobHandler implements JobHandler {

    @Override
    public void execute(JobPayload payload) {
//        String action = (String) payload.getData().get("action");
//
//        if ("GENERATE_REPORT".equals(action)) {
//            System.out.println("Generating report...");
//        }
    }
}