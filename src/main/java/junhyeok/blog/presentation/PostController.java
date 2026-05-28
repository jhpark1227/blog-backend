package junhyeok.blog.presentation;

import junhyeok.blog.application.PostService;
import junhyeok.blog.application.dto.GetPostCondition;
import junhyeok.blog.application.dto.response.GetPostResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping("/posts")
    public Page<GetPostResponse> getPosts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam int page,
            @RequestParam int size
    ) {
        return postService.getPosts(new GetPostCondition(categoryId, page, size));
    }
}
