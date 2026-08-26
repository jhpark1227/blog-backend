package junhyeok.blog.presentation;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;
import junhyeok.blog.application.PostService;
import junhyeok.blog.application.dto.response.PostDetailResponse;
import junhyeok.blog.application.dto.response.PostResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.server.Cookie.SameSite;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PostController {

    private static final String VIEWED_POST_IDS_COOKIE_NAME = "VIEWED-POST-IDS";
    private static final int MAX_VIEWED_IDS = 50;

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
    public ResponseEntity<PostDetailResponse> getPost(
            @CookieValue(name = VIEWED_POST_IDS_COOKIE_NAME, defaultValue = "") String rawIds,
            @PathVariable("postId") String postId
    ) {
        List<String> ids = rawIds.isBlank() ? List.of() : List.of(rawIds.split("\\."));

        if (ids.contains(postId)) {
            return ResponseEntity.ok(postService.getPost(postId));
        }

        PostDetailResponse response = postService.getPostAndIncreaseViewCount(postId);
        String updatedCookie = append(ids, postId);
        ResponseCookie cookie = ResponseCookie.from(VIEWED_POST_IDS_COOKIE_NAME, updatedCookie)
                .maxAge(Duration.between(LocalDateTime.now(), LocalDate.now().plusDays(1).atStartOfDay()).toSeconds())
                .path("/")
                .httpOnly(true)
                .secure(true)
                .sameSite(SameSite.NONE.attributeValue())
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(response);
    }

    private String append(List<String> viewedIds, String postId) {
        List<String> appended = Stream.concat(viewedIds.stream(), Stream.of(postId)).toList();
        List<String> truncated = appended.size() <= MAX_VIEWED_IDS
                ? appended
                : appended.subList(appended.size() - MAX_VIEWED_IDS, appended.size());
        return String.join(".", truncated);
    }
}
