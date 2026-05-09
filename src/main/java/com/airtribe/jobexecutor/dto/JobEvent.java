package com.airtribe.jobexecutor.dto;

// keep identical in both services (or extract common module)

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JobEvent {
    private String jobId;
    private String payload;
    private int retryCount;
}