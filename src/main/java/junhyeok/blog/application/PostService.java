package junhyeok.blog.application;

import java.util.List;
import junhyeok.blog.application.dto.response.PostDetailResponse;
import junhyeok.blog.application.dto.response.PostResponse;
import junhyeok.blog.domain.Post;
import junhyeok.blog.domain.PostRepository;
import junhyeok.blog.global.exception.CustomException;
import junhyeok.blog.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

    @Transactional(readOnly = true)
    public Page<PostResponse> getPosts(String categoryId, List<String> tagIds, Pageable pageable) {
        return postRepository.findPublishedBy(categoryId, tagIds, pageable)
                .map(PostResponse::from);
    }

    @Transactional(readOnly = true)
    public PostDetailResponse getPost(String postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
        return PostDetailResponse.from(post);
    }

    @Transactional(readOnly = true)
    public List<PostResponse> getPinnedPosts() {
        return postRepository.findPinnedPublishedPosts()
                .stream()
                .map(PostResponse::from)
                .toList();
    }

    public PostDetailResponse getPostAndIncreaseViewCount(String postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
        postRepository.increaseViewCountById(postId);
        return PostDetailResponse.from(post);
    }
}
