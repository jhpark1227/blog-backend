package junhyeok.blog.application;

import java.util.List;
import junhyeok.blog.application.dto.response.TagResponse;
import junhyeok.blog.domain.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    public List<TagResponse> getTags() {
        return tagRepository.findTagsOrderByPublishedPostCountDesc()
                .stream()
                .map(TagResponse::from)
                .toList();
    }
}
