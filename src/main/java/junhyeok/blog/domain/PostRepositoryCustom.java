package junhyeok.blog.domain;

import junhyeok.blog.application.dto.GetPostCondition;
import org.springframework.data.domain.Page;

public interface PostRepositoryCustom {

    Page<Post> find(GetPostCondition request);
}
