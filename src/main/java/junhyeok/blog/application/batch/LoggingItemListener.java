package junhyeok.blog.application.batch;

import junhyeok.blog.application.batch.dataSync.embedding.PostChunks;
import junhyeok.blog.domain.Post;
import junhyeok.blog.domain.PostData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.listener.ItemProcessListener;
import org.springframework.batch.core.listener.SkipListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LoggingItemListener implements SkipListener<Object, Object>, ItemProcessListener<Object, Object> {

    @Override
    public void onProcessError(Object item, Exception e) {
        log.error("처리 실패: {}", describe(item), e);
    }

    @Override
    public void onSkipInRead(Throwable t) {
        log.warn("읽기 스킵", t);
    }

    @Override
    public void onSkipInProcess(Object item, Throwable t) {
        log.warn("처리 스킵: {}", describe(item), t);
    }

    @Override
    public void onSkipInWrite(Object item, Throwable t) {
        log.warn("쓰기 스킵: {}", describe(item), t);
    }

    private String describe(Object item) {
        return switch (item) {
            case Post post -> post.getNotionPageId();
            case PostData postData -> postData.pageId();
            case PostChunks postChunks -> postChunks.post().getNotionPageId();
            default -> String.valueOf(item);
        };
    }
}