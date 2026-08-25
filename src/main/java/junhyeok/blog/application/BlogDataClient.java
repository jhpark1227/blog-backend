package junhyeok.blog.application;

import java.time.LocalDateTime;
import java.util.List;
import junhyeok.blog.domain.Category;
import junhyeok.blog.domain.PostDataResult;
import junhyeok.blog.domain.Tag;

public interface BlogDataClient {

    PostDataResult getPostData(String cursor);

    List<Category> getCategories(LocalDateTime syncedAt);

    List<Tag> getTags(LocalDateTime syncedAt);

    String getPageContent(String pageId);
}
