package junhyeok.blog.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import junhyeok.blog.application.dto.response.TagResponse;
import junhyeok.blog.domain.PostRepository;
import junhyeok.blog.domain.PostStatus;
import junhyeok.blog.domain.Tag;
import junhyeok.blog.domain.TagRepository;
import junhyeok.blog.util.PostBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class TagServiceTest {

    @Autowired
    TagService sut;

    @Autowired
    TagRepository tagRepository;

    @Autowired
    PostRepository postRepository;

    @AfterEach
    void tearDown() {
        postRepository.deleteAllInBatch();
        tagRepository.deleteAllInBatch();
    }

    @Test
    void 태그를_발행된_글_개수_내림차순_정렬순서_오름차순으로_조회한다() {
        Tag tag1 = tagRepository.save(new Tag("tagId1", "태그1", "빨강", 1));
        Tag tag2 = tagRepository.save(new Tag("tagId2", "태그2", "파랑", 2));
        Tag tag3 = tagRepository.save(new Tag("tagId3", "태그3", "노랑", 3));
        postRepository.save(PostBuilder.create(1)
                .tags(tag1)
                .build());
        postRepository.save(PostBuilder.create(2)
                .tags(tag1, tag2, tag3)
                .build());
        postRepository.save(PostBuilder.create(3)
                .tags(tag1, tag2, tag3)
                .build());
        postRepository.save(PostBuilder.create(4)
                .status(PostStatus.PENDING)
                .tags(tag3)
                .build());

        List<TagResponse> tags = sut.getTags();

        assertThat(tags).extracting(TagResponse::tagId).containsExactly("tagId1", "tagId2", "tagId3");
    }
}