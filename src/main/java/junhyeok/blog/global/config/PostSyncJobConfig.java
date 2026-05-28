package junhyeok.blog.global.config;

import junhyeok.blog.application.batch.DeleteObsoletePostsTasklet;
import junhyeok.blog.application.batch.PostItemProcessor;
import junhyeok.blog.application.batch.PostItemReader;
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
public class PostSyncJobConfig {

    private static final int CHUNK_SIZE = 1;

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public Job notionSyncJob(
            PostSyncJobListener postSyncJobListener,
            Step notionSyncStep,
            Step deleteObsoletePostsStep
    ) {
        return new JobBuilder("notionSyncJob", jobRepository)
                .listener(postSyncJobListener)   // beforeJob: jobStartTime을 ExecutionContext에 기록
                .start(notionSyncStep)           // Step 1: Notion → DB 동기화 (updatedAt 갱신)
                .next(deleteObsoletePostsStep)   // Step 2: updatedAt < jobStartTime → 소프트딜리트
                .build();
    }

    @Bean
    public Step notionSyncStep(
            PostItemReader postItemReader,
            PostItemProcessor postItemProcessor,
            PostItemWriter postItemWriter
    ) {
        return new ChunkOrientedStepBuilder<PostData, Post>(jobRepository, CHUNK_SIZE)
                .transactionManager(transactionManager)
                .reader(postItemReader)
                .processor(postItemProcessor)
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