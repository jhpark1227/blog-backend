package junhyeok.blog.application;

import java.util.List;
import junhyeok.blog.application.dto.response.TagResponse;
import junhyeok.blog.domain.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class TagService {

    private final PostRepository postRepository;

    public List<TagResponse> getTags() {
        return postRepository.findAllTags()
                .stream()
                .map(TagResponse::from)
                .toList();
    }
}
