package junhyeok.blog.application.batch.dataSync;

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

    private final LocalDateTime runAt;

    private final PostRepository postRepository;

    public DeleteObsoletePostsTasklet(
            @Value("#{jobParameters['" + PostSyncScheduler.RUN_AT + "']}") LocalDateTime runAt,
            PostRepository postRepository
    ) {
        this.runAt = runAt;
        this.postRepository = postRepository;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        int deletedCount = postRepository.markDeletedWhenNotSyncedSince(runAt);
        log.info("Notion에서 삭제된 포스트 {}건 소프트딜리트 완료 (기준 시각: {})", deletedCount, runAt);
        contribution.incrementWriteCount(deletedCount);
        return RepeatStatus.FINISHED;
    }
}