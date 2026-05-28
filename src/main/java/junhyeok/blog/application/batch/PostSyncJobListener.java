package junhyeok.blog.application.batch;

import java.time.LocalDateTime;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListener;
import org.springframework.stereotype.Component;

@Component
public class PostSyncJobListener implements JobExecutionListener {

    public static final String JOB_START_TIME_KEY = "jobStartTime";

    @Override
    public void beforeJob(JobExecution jobExecution) {
        jobExecution.getExecutionContext()
                .put(JOB_START_TIME_KEY, LocalDateTime.now());
    }
}