package junhyeok.blog.global.config;

import junhyeok.blog.application.batch.DeleteObsoletePostsTasklet;
import junhyeok.blog.application.batch.MetadataSyncTasklet;
import junhyeok.blog.application.batch.PostDataItemProcessor;
import junhyeok.blog.application.batch.PostDataItemReader;
import junhyeok.blog.application.batch.PostItemWriter;
import junhyeok.blog.application.batch.PostSyncJobListener;
import junhyeok.blog.domain.Post;
import junhyeok.blog.domain.PostData;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.ChunkOrientedStepBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class DataSyncJobConfig {

    private static final int CHUNK_SIZE = 1;

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public Job DataSyncJob(
            Step tagSyncStep,
            PostSyncJobListener postSyncJobListener,
            Step postSyncStep,
            Step deleteObsoletePostsStep
    ) {
        return new JobBuilder("dataSyncJob", jobRepository)
                .listener(postSyncJobListener)
                .start(tagSyncStep)
                .next(postSyncStep)
                .next(deleteObsoletePostsStep)
                .build();
    }

    @Bean
    public Step tagSyncStep(MetadataSyncTasklet metadataSyncTasklet) {
        return new StepBuilder("tagSyncStep", jobRepository)
                .tasklet(metadataSyncTasklet, transactionManager)
                .build();
    }

    @Bean
    public Step postSyncStep(
            PostDataItemReader postDataItemReader,
            PostDataItemProcessor postDataItemProcessor,
            PostItemWriter postItemWriter
    ) {
        return new ChunkOrientedStepBuilder<PostData, Post>(jobRepository, CHUNK_SIZE)
                .transactionManager(transactionManager)
                .reader(postDataItemReader)
                .processor(postDataItemProcessor)
                .writer(postItemWriter)
                .build();
    }

    @Bean
    public Step deleteObsoletePostsStep(DeleteObsoletePostsTasklet deleteObsoletePostsTasklet) {
        return new StepBuilder("deleteObsoletePostsStep", jobRepository)
                .tasklet(deleteObsoletePostsTasklet, transactionManager)
                .build();
    }
}