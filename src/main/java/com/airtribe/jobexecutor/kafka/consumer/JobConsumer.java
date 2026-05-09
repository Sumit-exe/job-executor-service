package com.airtribe.jobexecutor.kafka.consumer;

import com.airtribe.jobexecutor.service.JobExecutorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobConsumer {

    private final JobExecutorService jobExecutorService;

    @KafkaListener(topics = "job-execution-topic", groupId = "job-group")
    public void consume(String message) {
        log.info("Received job: {}", message);
        jobExecutorService.process(message);
    }
}
