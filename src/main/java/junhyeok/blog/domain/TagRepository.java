package junhyeok.blog.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface TagRepository extends JpaRepository<Tag, String> {

    @Modifying
    @Query("DELETE FROM Tag")
    void deleteAll();
}
