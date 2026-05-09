package com.airtribe.jobexecutor.factory;

import com.airtribe.jobexecutor.service.JobHandler;
import com.airtribe.jobexecutor.service.impl.HttpJobHandler;
import com.airtribe.jobexecutor.service.impl.InternalJobHandler;
import com.airtribe.jobexecutor.util.enums.JobType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JobHandlerFactory {

    private final HttpJobHandler httpHandler;
    private final InternalJobHandler internalHandler;

    public JobHandler getHandler(JobType type) {
        return switch (type) {
            case HTTP -> httpHandler;
            case INTERNAL -> internalHandler;
            default -> throw new RuntimeException("Unsupported type");
        };
    }
}