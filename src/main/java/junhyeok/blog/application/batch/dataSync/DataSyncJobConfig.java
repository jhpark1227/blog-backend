package junhyeok.blog.application.batch.dataSync;

import junhyeok.blog.application.batch.dataSync.embedding.DeleteUnpublishedPostVectorsTasklet;
import junhyeok.blog.application.batch.dataSync.embedding.PostChunkingProcessor;
import junhyeok.blog.application.batch.dataSync.embedding.PostChunks;
import junhyeok.blog.application.batch.dataSync.embedding.PostEmbeddingTargetReader;
import junhyeok.blog.application.batch.dataSync.embedding.PostVectorWriter;
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
            Step deleteObsoleteMetadataStep,
            Step deleteUnpublishedPostVectorsStep,
            Step postEmbeddingStep
    ) {
        return new JobBuilder("dataSyncJob", jobRepository)
                .start(tagSyncStep)
                .next(postSyncStep)
                .next(deleteObsoletePostsStep)
                .next(deleteObsoleteMetadataStep)
                .next(deleteUnpublishedPostVectorsStep)
                .next(postEmbeddingStep)
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

    @Bean
    public Step postEmbeddingStep(
            PostEmbeddingTargetReader postEmbeddingTargetReader,
            PostChunkingProcessor postChunkingProcessor,
            PostVectorWriter postVectorWriter
    ) {
        return new ChunkOrientedStepBuilder<Post, PostChunks>(jobRepository, CHUNK_SIZE)
                .transactionManager(transactionManager)
                .reader(postEmbeddingTargetReader)
                .processor(postChunkingProcessor)
                .writer(postVectorWriter)
                .build();
    }

    @Bean
    public Step deleteUnpublishedPostVectorsStep(DeleteUnpublishedPostVectorsTasklet deleteUnpublishedPostVectorsTasklet) {
        return new StepBuilder("deleteUnpublishedPostVectorsStep", jobRepository)
                .tasklet(deleteUnpublishedPostVectorsTasklet, transactionManager)
                .build();
    }
}