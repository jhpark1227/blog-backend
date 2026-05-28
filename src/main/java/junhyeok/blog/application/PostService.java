package junhyeok.blog.application;

import junhyeok.blog.application.dto.GetPostCondition;
import junhyeok.blog.application.dto.response.GetPostResponse;
import junhyeok.blog.domain.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

    public Page<GetPostResponse> getPosts(GetPostCondition request) {
        return postRepository.find(request)
                .map(GetPostResponse::from);
    }
}
