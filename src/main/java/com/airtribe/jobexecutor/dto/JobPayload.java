package com.airtribe.jobexecutor.dto;

import com.airtribe.jobexecutor.util.enums.JobType;
import lombok.Data;

@Data
public class JobPayload {
    private JobType type;
    private HttpJobData data;
}