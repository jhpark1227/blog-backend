package junhyeok.blog.application.batch.dataSync;

import junhyeok.blog.domain.Post;
import junhyeok.blog.domain.PostData;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.ChunkOrientedStepBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemWriter;
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
    public Job dataSyncJob(
            Step tagSyncStep,
            Step postSyncStep,
            Step deleteObsoletePostsStep,
            Step deleteObsoleteMetadataStep
    ) {
        return new JobBuilder("dataSyncJob", jobRepository)
                .start(tagSyncStep)
                .next(postSyncStep)
                .next(deleteObsoletePostsStep)
                .next(deleteObsoleteMetadataStep)
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
            ItemWriter<Post> postWriter
    ) {
        return new ChunkOrientedStepBuilder<PostData, Post>(jobRepository, CHUNK_SIZE)
                .transactionManager(transactionManager)
                .reader(postDataItemReader)
                .processor(postDataItemProcessor)
                .writer(postWriter)
                .build();
    }

    @Bean
    public Step deleteObsoletePostsStep(DeleteObsoletePostsTasklet deleteObsoletePostsTasklet) {
        return new StepBuilder("deleteObsoletePostsStep", jobRepository)
                .tasklet(deleteObsoletePostsTasklet, transactionManager)
                .build();
    }

    @Bean
    public Step deleteObsoleteMetadataStep(DeleteObsoleteMetadataTasklet deleteObsoleteMetadataTasklet) {
        return new StepBuilder("deleteObsoleteMetadataStep", jobRepository)
                .tasklet(deleteObsoleteMetadataTasklet, transactionManager)
                .build();
    }
}