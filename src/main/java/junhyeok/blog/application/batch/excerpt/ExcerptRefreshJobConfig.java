package junhyeok.blog.application.batch.excerpt;

import junhyeok.blog.domain.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.ChunkOrientedStepBuilder;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class ExcerptRefreshJobConfig {

    private static final int CHUNK_SIZE = 1;

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public Job excerptRefreshJob(Step excerptRefreshStep) {
        return new JobBuilder("excerptRefreshJob", jobRepository)
                .start(excerptRefreshStep)
                .build();
    }

    @Bean
    public Step excerptRefreshStep(
            ExcerptRefreshTargetReader reader,
            ExcerptRefreshTargetProcessor processor,
            ItemWriter<Post> postWriter
    ) {
        return new ChunkOrientedStepBuilder<Post, Post>(jobRepository, CHUNK_SIZE)
                .transactionManager(transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(postWriter)
                .build();
    }
}
