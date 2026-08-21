package junhyeok.blog.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import junhyeok.blog.application.dto.response.CategoryResponse;
import junhyeok.blog.domain.Category;
import junhyeok.blog.domain.CategoryRepository;
import junhyeok.blog.domain.Post;
import junhyeok.blog.domain.PostRepository;
import junhyeok.blog.util.PostBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CategoryServiceTest {

    @Autowired
    CategoryService sut;

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    PostRepository postRepository;

    @AfterEach
    void tearDown() {
        postRepository.deleteAllInBatch();
        categoryRepository.deleteAllInBatch();
    }

    @Test
    void 모든_카테고리와_글_개수를_함께_조회한다() {
        Category category1 = categoryRepository.save(new Category("id1", "카테고리1", "red", 1));
        Category category2 = categoryRepository.save(new Category("id2", "카테고리2", "blue", 2));
        Category category3 = categoryRepository.save(new Category("id3", "카테고리3", "green", 3));
        Post post1 = PostBuilder.create(1)
                .build();
        Post post2 = PostBuilder.create(2)
                .category(category2)
                .build();
        Post post3 = PostBuilder.create(3)
                .category(category3)
                .build();
        Post post4 = PostBuilder.create(4)
                .category(category3)
                .build();
        postRepository.save(post1);
        postRepository.save(post2);
        postRepository.save(post3);
        postRepository.save(post4);

        List<CategoryResponse> allCategories = sut.getCategoriesWithPublishedPostCount();

        assertThat(allCategories).containsExactly(
                new CategoryResponse("id1", "카테고리1", 0L),
                new CategoryResponse("id2", "카테고리2", 1L),
                new CategoryResponse("id3", "카테고리3", 2L)
        );
    }
}
