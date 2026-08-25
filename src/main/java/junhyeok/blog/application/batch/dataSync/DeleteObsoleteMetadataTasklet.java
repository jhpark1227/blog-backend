package junhyeok.blog.application.batch.dataSync;

import java.time.LocalDateTime;
import junhyeok.blog.domain.CategoryRepository;
import junhyeok.blog.domain.TagRepository;
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
public class DeleteObsoleteMetadataTasklet implements Tasklet {

    private final LocalDateTime runAt;

    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;

    public DeleteObsoleteMetadataTasklet(
            @Value("#{jobParameters['" + PostSyncScheduler.RUN_AT + "']}") LocalDateTime runAt,
            CategoryRepository categoryRepository,
            TagRepository tagRepository
    ) {
        this.runAt = runAt;
        this.categoryRepository = categoryRepository;
        this.tagRepository = tagRepository;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        int deletedTagCount = tagRepository.deleteWhenNotSyncedSince(runAt);
        int deletedCategoryCount = categoryRepository.deleteWhenNotSyncedSince(runAt);
        log.info("Notion에서 삭제된 카테고리 {}건, 태그 {}건 삭제 완료 (기준 시각: {})", deletedCategoryCount, deletedTagCount, runAt);
        contribution.incrementWriteCount(deletedCategoryCount + deletedTagCount);
        return RepeatStatus.FINISHED;
    }
}