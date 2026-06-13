package junhyeok.blog.application.batch;

import java.util.ArrayList;
import java.util.List;
import junhyeok.blog.application.BlogDataClient;
import junhyeok.blog.domain.PostData;
import junhyeok.blog.domain.PostDataResult;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.stereotype.Component;

@Component
@StepScope
@RequiredArgsConstructor
public class PostDataItemReader implements ItemReader<PostData> {

    private final BlogDataClient blogDataClient;

    private List<PostData> currentBatch = new ArrayList<>();
    private int currentIndex = 0;
    private String nextCursor = null;
    private boolean hasMore = true;
    private boolean initialized = false;

    @Override
    public PostData read() {
        if (currentIndex >= currentBatch.size()) {
            if (initialized && !hasMore) {
                return null;
            }

            PostDataResult result = blogDataClient.getPostData(nextCursor);
            currentBatch = result.items();
            nextCursor = result.nextCursor();
            hasMore = result.hasMore();
            currentIndex = 0;
            initialized = true;

            if (currentBatch.isEmpty()) {
                return null;
            }
        }

        return currentBatch.get(currentIndex++);
    }
}