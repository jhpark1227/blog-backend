package junhyeok.blog.application.batch.excerpt;

import junhyeok.blog.application.batch.BatchJobListener;
import junhyeok.blog.application.batch.LoggingItemListener;
import junhyeok.blog.domain.Post;
import junhyeok.blog.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.ChunkOrientedStepBuilder;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ExcerptRefreshJobConfig {

    private static final int CHUNK_SIZE = 1;

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public Job excerptRefreshJob(Step excerptRefreshStep, BatchJobListener batchJobListener) {
        return new JobBuilder("excerptRefreshJob", jobRepository)
                .listener(batchJobListener)
                .start(excerptRefreshStep)
                .build();
    }

    @Bean
    public Step excerptRefreshStep(
            ExcerptRefreshTargetReader reader,
            ExcerptRefreshTargetProcessor processor,
            ItemWriter<Post> postWriter,
            LoggingItemListener loggingItemListener
    ) {
        return new ChunkOrientedStepBuilder<Post, Post>(jobRepository, CHUNK_SIZE)
                .transactionManager(transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(postWriter)
                .faultTolerant()
                .retryLimit(3)
                .retry(ResourceAccessException.class,
                        HttpServerErrorException.class,
                        HttpClientErrorException.TooManyRequests.class)
                .skip(CustomException.class)
                .skipLimit(5)
                .listener(loggingItemListener)
                .build();
    }
}
