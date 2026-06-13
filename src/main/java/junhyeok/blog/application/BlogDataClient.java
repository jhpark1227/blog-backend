package junhyeok.blog.application;

import java.util.List;
import junhyeok.blog.domain.Category;
import junhyeok.blog.domain.PostDataResult;
import junhyeok.blog.domain.Tag;

public interface BlogDataClient {

    PostDataResult getPostData(String cursor);

    List<Category> getCategories();

    List<Tag> getTags();

    String getPageContent(String pageId);
}
