package junhyeok.blog.infrastructure;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import junhyeok.blog.domain.Post;
import junhyeok.blog.domain.PostRepositoryCustom;
import junhyeok.blog.domain.PostStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepositoryCustom {

    private final EntityManager em;

    @Override
    public Page<Post> findPublishedBy(String categoryId, List<String> tagIds, Pageable pageable) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        List<Post> content = buildContentQuery(cb, categoryId, tagIds, pageable);
        long total = buildCountQuery(cb, categoryId, tagIds);

        return new PageImpl<>(content, pageable, total);
    }

    private List<Post> buildContentQuery(CriteriaBuilder cb, String categoryId, List<String> tagIds, Pageable pageable) {
        CriteriaQuery<Post> cq = cb.createQuery(Post.class);
        Root<Post> p = cq.from(Post.class);

        cq.select(p).distinct(true).where(predicates(cb, p, categoryId, tagIds));

        cq.orderBy(pageable.getSort().stream()
                .map(o -> o.isAscending() ? cb.asc(p.get(o.getProperty())) : cb.desc(p.get(o.getProperty())))
                .toList());

        TypedQuery<Post> query = em.createQuery(cq);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        return query.getResultList();
    }

    private long buildCountQuery(CriteriaBuilder cb, String categoryId, List<String> tagIds) {
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<Post> p = cq.from(Post.class);

        cq.select(cb.countDistinct(p)).where(predicates(cb, p, categoryId, tagIds));

        return em.createQuery(cq).getSingleResult();
    }

    private Predicate[] predicates(CriteriaBuilder cb, Root<Post> p, String categoryId, List<String> tagIds) {
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(p.get("status"), PostStatus.PUBLISHED));
        if (categoryId != null) {
            predicates.add(cb.equal(p.get("category").get("notionOptionId"), categoryId));
        }
        if (tagIds != null) {
            predicates.add(p.join("tags").get("notionOptionId").in(tagIds));
        }
        return predicates.toArray(new Predicate[0]);
    }
}