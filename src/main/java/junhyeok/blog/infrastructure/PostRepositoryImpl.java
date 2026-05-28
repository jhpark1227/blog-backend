package junhyeok.blog.infrastructure;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import junhyeok.blog.application.dto.GetPostCondition;
import junhyeok.blog.domain.Post;
import junhyeok.blog.domain.PostRepositoryCustom;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepositoryCustom {

    private final EntityManager entityManager;

    @Override
    public Page<Post> find(GetPostCondition condition) {
        boolean hasCategoryFilter = condition.categoryId() != null;

        String baseJpql = "FROM Post p" + (hasCategoryFilter ? " WHERE p.category.id = :categoryId" : "");

        TypedQuery<Post> query = entityManager.createQuery("SELECT p " + baseJpql, Post.class);
        TypedQuery<Long> countQuery = entityManager.createQuery("SELECT COUNT(p) " + baseJpql, Long.class);

        if (hasCategoryFilter) {
            query.setParameter("categoryId", condition.categoryId());
            countQuery.setParameter("categoryId", condition.categoryId());
        }

        Pageable pageable = PageRequest.of(condition.page(), condition.size());
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<Post> posts = query.getResultList();
        long total = countQuery.getSingleResult();

        return new PageImpl<>(posts, pageable, total);
    }
}
