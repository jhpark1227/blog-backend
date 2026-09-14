package junhyeok.blog.application.batch.dataSync;

import java.util.ArrayList;
import java.util.List;
import junhyeok.blog.application.BlogDataClient;
import junhyeok.blog.domain.PostData;
import junhyeok.blog.domain.PostDataResult;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.batch.infrastructure.item.ItemStreamException;
import org.springframework.batch.infrastructure.item.ItemStreamSupport;
import org.springframework.stereotype.Component;

@Component
@StepScope
@RequiredArgsConstructor
public class PostDataItemReader extends ItemStreamSupport implements ItemReader<PostData> {

    private static final String ITEM_INDEX_KEY = "currentIndex";
    private static final String PAGE_CURSOR_KEY = "pageCursor";

    private final BlogDataClient blogDataClient;

    private List<PostData> currentBatch = new ArrayList<>();
    private int currentIndex = 0;
    private @Nullable String pageCursor = null;
    private @Nullable String nextCursor = null;
    private int pendingIndex = 0;
    private boolean hasMore = true;
    private boolean initialized = false;

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        String key = getExecutionContextKey(PAGE_CURSOR_KEY);
        this.nextCursor = executionContext.containsKey(key) ? executionContext.getString(key) : null;
        this.pendingIndex = executionContext.getInt(getExecutionContextKey(ITEM_INDEX_KEY), 0);
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        executionContext.putString(getExecutionContextKey(PAGE_CURSOR_KEY), pageCursor);
        executionContext.putInt(getExecutionContextKey(ITEM_INDEX_KEY), currentIndex);
    }

    @Override
    public @Nullable PostData read() {
        while (currentIndex >= currentBatch.size()) {
            if (initialized && !hasMore) {
                return null;
            }

            pageCursor = nextCursor;
            PostDataResult result = blogDataClient.getPostData(pageCursor);
            currentBatch = result.items();
            nextCursor = result.nextCursor();
            hasMore = result.hasMore();
            currentIndex = pendingIndex;
            pendingIndex = 0;
            initialized = true;

            if (currentBatch.isEmpty()) {
                return null;
            }
        }

        return currentBatch.get(currentIndex++);
    }
}