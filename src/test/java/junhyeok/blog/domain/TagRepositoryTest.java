package junhyeok.blog.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import junhyeok.blog.global.config.JpaAuditingConfig;
import junhyeok.blog.util.PostBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(JpaAuditingConfig.class)
@AutoConfigureTestDatabase(replace = Replace.NONE)
class TagRepositoryTest {

    @Autowired
    TagRepository sut;

    @Autowired
    TestEntityManager em;

    @Test
    void 발행된_글이_많은_순서대로_태그를_조회한다() {
        Tag tag1 = em.persist(new Tag("tagId1", "태그1", "빨강", 1));
        Tag tag2 = em.persist(new Tag("tagId2", "태그2", "파랑", 2));
        Tag tag3 = em.persist(new Tag("tagId3", "태그3", "노랑", 3));
        em.persist(PostBuilder.create(1).tags(tag1).build());
        em.persist(PostBuilder.create(2).tags(tag2).build());
        em.persist(PostBuilder.create(3).tags(tag2).build());
        em.persist(PostBuilder.create(4).tags(tag3).build());
        em.persist(PostBuilder.create(5).tags(tag3).build());
        em.persist(PostBuilder.create(6).tags(tag3).build());
        em.flush();
        em.clear();

        List<Tag> tags = sut.findTagsOrderByPublishedPostCountDesc();

        assertThat(tags).extracting(Tag::getNotionOptionId).containsExactly("tagId3", "tagId2", "tagId1");
    }
}