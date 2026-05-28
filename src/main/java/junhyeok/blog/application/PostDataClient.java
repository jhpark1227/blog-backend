package junhyeok.blog.application;

import junhyeok.blog.domain.PostDataResult;

public interface PostDataClient {

    PostDataResult get(String cursor);
}
