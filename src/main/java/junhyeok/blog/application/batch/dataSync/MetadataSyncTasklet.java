package junhyeok.blog.application.batch.dataSync;

import java.util.List;
import junhyeok.blog.application.BlogDataClient;
import junhyeok.blog.domain.Category;
import junhyeok.blog.domain.CategoryRepository;
import junhyeok.blog.domain.Tag;
import junhyeok.blog.domain.TagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@StepScope
@RequiredArgsConstructor
public class MetadataSyncTasklet implements Tasklet {

    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final BlogDataClient blogDataClient;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        categoryRepository.deleteAll();
        tagRepository.deleteAll();
        List<Category> allCategories = blogDataClient.getCategories();
        List<Tag> allTags = blogDataClient.getTags();
        categoryRepository.saveAll(allCategories);
        tagRepository.saveAll(allTags);
        return RepeatStatus.FINISHED;
    }
}