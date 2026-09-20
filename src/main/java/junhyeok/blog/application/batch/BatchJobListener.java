package junhyeok.blog.application.batch;

import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BatchJobListener implements JobExecutionListener {

    private static final String JOB_EXECUTION_ID = "jobExecutionId";

    @Override
    public void beforeJob(JobExecution jobExecution) {
        MDC.put(JOB_EXECUTION_ID, String.valueOf(jobExecution.getId()));
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        String jobName = jobExecution.getJobInstance().getJobName();
        Duration jobDuration = Duration.between(jobExecution.getStartTime(), jobExecution.getEndTime());

        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            log.info("잡 완료: {} ({}초)", jobName, jobDuration.toSeconds());
        } else {
            log.error("잡 실패: {} status={} ({}초)", jobName, jobExecution.getStatus(), jobDuration.toSeconds());
            jobExecution.getAllFailureExceptions()
                    .forEach(e -> log.error("실패 원인", e));
        }

        jobExecution.getStepExecutions()
                .forEach(step -> log.info("{}", step.getSummary()));
        MDC.remove(JOB_EXECUTION_ID);
    }
}
