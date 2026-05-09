package com.airtribe.jobexecutor.service;

import com.airtribe.jobexecutor.dto.JobEvent;
import com.airtribe.jobexecutor.dto.JobPayload;
import com.airtribe.jobexecutor.entity.Job;
import com.airtribe.jobexecutor.entity.TaskExecutionHistory;
import com.airtribe.jobexecutor.entity.TaskSchedule;
import com.airtribe.jobexecutor.factory.JobHandlerFactory;
import com.airtribe.jobexecutor.repository.JobRepository;
import com.airtribe.jobexecutor.repository.TaskExecutionHistoryRepository;
import com.airtribe.jobexecutor.repository.TaskScheduleRepository;
import com.airtribe.jobexecutor.util.BucketUtil;
import com.airtribe.jobexecutor.util.enums.ExecutionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.*;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JobExecutorService {

    private final ObjectMapper objectMapper;
    private final TaskExecutionHistoryRepository historyRepository;
    private final TaskScheduleRepository taskScheduleRepository;
    private final JobHandlerFactory factory;
    private final JobRepository jobRepository;

    public void process(String message) {

        JobEvent event;

        try {

            event = objectMapper.readValue(
                    message,
                    JobEvent.class
            );

        } catch (Exception e) {
            return;
        }

        UUID jobId = UUID.fromString(event.getJobId());

        Job job = jobRepository.findByKeyJobId(jobId)
                .orElseThrow(() ->
                        new RuntimeException("Job not found")
                );

        Instant executionTime = Instant.now();

        try {

            JobPayload payload =
                    objectMapper.readValue(
                            event.getPayload(),
                            JobPayload.class
                    );

            factory.getHandler(payload.getType())
                    .execute(payload);

            saveHistory(
                    jobId,
                    executionTime,
                    "SUCCESS",
                    event.getRetryCount(),
                    null
            );

            deleteCurrentSchedule(jobId, executionTime);

            rescheduleJob(job, executionTime);

        } catch (Exception ex) {

            saveHistory(
                    jobId,
                    executionTime,
                    "FAILED",
                    event.getRetryCount(),
                    ex.getMessage()
            );

            retry(job, event);
        }
    }

    private void retry(Job job, JobEvent event) {

        int nextRetry = event.getRetryCount() + 1;

        if (nextRetry > job.getMaxRetryCount()) {
            return;
        }

        Instant nextExecution =
                Instant.now().plusSeconds(60);

        createTaskSchedule(
                job,
                nextExecution
        );
    }


    private void execute(TaskSchedule job) {

        if (job.getPayload() == null) {
            throw new RuntimeException("Payload missing");
        }

        try {

            JobPayload payload =
                    objectMapper.readValue(job.getPayload(), JobPayload.class);

            JobHandler handler = factory.getHandler(payload.getType());

            handler.execute(payload);

        } catch (Exception e) {
            throw new RuntimeException("Execution failed", e);
        }
    }

    private void saveHistory(UUID jobId,
                             Instant executionTime,
                             String status,
                             int retryCount,
                             String error) {

        TaskExecutionHistory history = new TaskExecutionHistory();

        TaskExecutionHistory.Key key = new TaskExecutionHistory.Key();
        key.setJobId(jobId);
        key.setExecutionTime(executionTime);

        history.setKey(key);
        history.setStatus(ExecutionStatus.valueOf(status));
        history.setRetryCount(retryCount);
        history.setLastUpdateTime(Instant.now());
        history.setErrorMessage(error);

        historyRepository.save(history);
    }


    private void rescheduleJob(
            Job job,
            Instant baseTime
    ) {

        if (!Boolean.TRUE.equals(job.getIsRecurring())) {
            return;
        }

        if (!Boolean.TRUE.equals(job.getIsActive())) {
            return;
        }

        Instant nextExecution =
                calculateNextExecution(
                        job,
                        baseTime
                );

        createTaskSchedule(
                job,
                nextExecution
        );
    }

    private void createTaskSchedule(
            Job job,
            Instant nextExecution
    ) {

        String bucket =
                BucketUtil.getBucket(
                        LocalDateTime.ofInstant(
                                nextExecution,
                                ZoneOffset.UTC
                        )
                );

        TaskSchedule schedule =
                new TaskSchedule();

        TaskSchedule.Key key =
                new TaskSchedule.Key();

        key.setExecutionBucket(bucket);
        key.setJobId(job.getKey().getJobId());

        schedule.setKey(key);
        schedule.setNextExecutionTime(nextExecution);
        schedule.setStatus("SCHEDULED");
        schedule.setPayload(job.getPayload());
        schedule.setIsActive(job.getIsActive());

        taskScheduleRepository.save(schedule);
    }

    private void deleteCurrentSchedule(
            UUID jobId,
            Instant executionTime
    ) {

        String bucket =
                BucketUtil.getBucket(
                        LocalDateTime.ofInstant(
                                executionTime,
                                ZoneOffset.UTC
                        )
                );

        TaskSchedule.Key key =
                new TaskSchedule.Key();

        key.setExecutionBucket(bucket);
        key.setJobId(jobId);

        taskScheduleRepository.deleteById(key);
    }

    private Instant calculateNextExecution(
            Job job,
            Instant baseTime
    ){
        ZoneId zoneId =
                ZoneId.of(job.getTimezone().getZoneId());

        ZonedDateTime base =
                ZonedDateTime.ofInstant(baseTime, zoneId);
        return switch (job.getTimeUnit()) {

            case MINUTES ->
                    base.plusMinutes(job.getFrequency())
                            .toInstant();

            case HOURS ->
                    base.plusHours(job.getFrequency())
                            .toInstant();

            case DAYS ->
                    base.plusDays(job.getFrequency())
                            .toInstant();

            case WEEKS ->
                    base.plusWeeks(job.getFrequency())
                            .toInstant();

            case MONTHS ->
                    base.plusMonths(job.getFrequency())
                            .toInstant();

            case YEARS ->
                    base.plusYears(job.getFrequency())
                            .toInstant();
        };
    }

}