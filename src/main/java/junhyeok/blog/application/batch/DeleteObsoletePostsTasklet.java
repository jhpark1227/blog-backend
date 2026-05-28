package junhyeok.blog.application.batch;

import java.time.LocalDateTime;
import junhyeok.blog.domain.PostRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@StepScope
public class DeleteObsoletePostsTasklet implements Tasklet {

    private final LocalDateTime jobStartTime;

    private final PostRepository postRepository;

    public DeleteObsoletePostsTasklet(
            @Value("#{jobExecutionContext['" + PostSyncJobListener.JOB_START_TIME_KEY + "']}") LocalDateTime jobStartTime,
            PostRepository postRepository
    ) {
        this.jobStartTime = jobStartTime;
        this.postRepository = postRepository;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        int deletedCount = postRepository.softDeleteUnsyncedBefore(jobStartTime);
        log.info("Notion에서 삭제된 포스트 {}건 소프트딜리트 완료 (기준 시각: {})", deletedCount, jobStartTime);
        contribution.incrementWriteCount(deletedCount);
        return RepeatStatus.FINISHED;
    }
}