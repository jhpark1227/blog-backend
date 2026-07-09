package junhyeok.blog.application.batch.dataSync;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostSyncScheduler {

    private final JobOperator jobOperator;
    private final Job dataSyncJob;

    @Scheduled(cron = "${batch.post-sync.cron}")
    public void run() {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addLocalDateTime("runAt", LocalDateTime.now())
                    .toJobParameters();

            jobOperator.start(dataSyncJob, params);
        } catch (Exception e) {
            log.error("Post sync batch failed", e);
        }
    }
}