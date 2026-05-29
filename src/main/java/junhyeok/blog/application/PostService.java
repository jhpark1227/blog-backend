package junhyeok.blog.application;

import junhyeok.blog.application.dto.response.GetPostResponse;
import junhyeok.blog.domain.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

    public Page<GetPostResponse> getPosts(int page, int size) {
        return postRepository.findAll(PageRequest.of(page, size))
                .map(GetPostResponse::from);
    }
}
