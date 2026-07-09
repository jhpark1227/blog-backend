package junhyeok.blog.application;

import junhyeok.blog.domain.Excerpt;
import junhyeok.blog.domain.Post;

public interface ExcerptClient {

    Excerpt generateExcerpt(Post post);
}
