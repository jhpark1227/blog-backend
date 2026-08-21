package junhyeok.blog.application.batch;

import junhyeok.blog.domain.Post;
import junhyeok.blog.domain.PostRepository;
import org.springframework.batch.core.configuration.support.JdbcDefaultBatchConfiguration;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BatchConfig extends JdbcDefaultBatchConfiguration {

    @Bean
    public ItemWriter<Post> postWriter(PostRepository postRepository) {
        return new RepositoryItemWriterBuilder<Post>()
                .repository(postRepository)
                .build();
    }
}
