package junhyeok.blog.presentation;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;
import junhyeok.blog.application.PostService;
import junhyeok.blog.application.dto.response.PostDetailResponse;
import junhyeok.blog.application.dto.response.PostResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping("/posts")
    public PageResponse<PostResponse> getPosts(
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) List<String> tagIds,
            @RequestParam(defaultValue = "0") @PositiveOrZero int page,
            @RequestParam(defaultValue = "5") @Positive @Max(100) int size,
            @RequestParam(defaultValue = "PUBLISHED_DATE") PostSortField sort,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction
    ) {
        return PageResponse.from(
                postService.getPosts(categoryId, tagIds, PageRequest.of(page, size, Sort.by(direction, sort.getProperty()))));
    }

    @GetMapping("/posts/pinned")
    public List<PostResponse> getPinnedPosts() {
        return postService.getPinnedPosts();
    }

    @GetMapping("/posts/{postId}")
    public PostDetailResponse getPost(@PathVariable("postId") String postId) {
        return postService.getPost(postId);
    }
}
