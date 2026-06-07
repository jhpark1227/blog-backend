package junhyeok.blog.application;

import java.util.List;
import junhyeok.blog.domain.PostDataResult;
import junhyeok.blog.domain.TagData;

public interface BlogDataClient {

    PostDataResult getPostData(String cursor);

    List<TagData> getTagData();
}
