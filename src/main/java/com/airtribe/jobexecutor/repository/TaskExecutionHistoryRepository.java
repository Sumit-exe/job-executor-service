package com.airtribe.jobexecutor.repository;
import com.airtribe.jobexecutor.entity.TaskExecutionHistory;
import org.springframework.data.cassandra.repository.CassandraRepository;

import java.util.List;
import java.util.UUID;

public interface TaskExecutionHistoryRepository extends CassandraRepository<TaskExecutionHistory, TaskExecutionHistory.Key> {

    List<TaskExecutionHistory> findByKeyJobId(UUID jobId);
}