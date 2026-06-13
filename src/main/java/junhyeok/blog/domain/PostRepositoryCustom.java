package junhyeok.blog.domain;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostRepositoryCustom {

    Page<Post> findPublishedBy(String categoryId, List<String> tagIds, Pageable pageable);
}
