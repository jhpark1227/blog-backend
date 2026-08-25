package junhyeok.blog.infrastructure;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import junhyeok.blog.domain.Category;
import junhyeok.blog.domain.Tag;

public record NotionDatasourceResponse(
        Properties properties
) {
    public List<Category> getCategories(LocalDateTime syncedAt) {
        List<SelectOption> options = properties.categoryProperty.selectInfo.options;
        List<Category> categories = new ArrayList<>();
        for (int i = 0; i < options.size(); i++) {
            SelectOption o = options.get(i);
            categories.add(new Category(o.id, o.name, o.color, i, syncedAt));
        }
        return categories;
    }

    public List<Tag> getTags(LocalDateTime syncedAt) {
        List<MultiSelectOption> options = properties.tagProperty.multiSelectInfo.options;
        List<Tag> tags = new ArrayList<>();
        for (int i = 0; i < options.size(); i++) {
            MultiSelectOption o = options.get(i);
            tags.add(new Tag(o.id, o.name, o.color, i, syncedAt));
        }
        return tags;
    }

    record Properties(
            @JsonProperty("태그")
            TagProperty tagProperty,
            @JsonProperty("분류")
            CategoryProperty categoryProperty
    ) {
    }

    private record CategoryProperty(
            String id,
            String name,
            String description,
            String type,
            @JsonProperty("select")
            SelectInfo selectInfo
    ) {
    }

    private record SelectInfo(
            List<SelectOption> options
    ) {
    }

    private record SelectOption(
            String id,
            String name,
            String color,
            String description
    ) {
    }

    private record TagProperty(
            @JsonProperty("multi_select")
            MultiSelectInfo multiSelectInfo
    ) {
    }

    private record MultiSelectInfo(
            List<MultiSelectOption> options
    ) {
    }

    private record MultiSelectOption(
            String id,
            String name,
            String color,
            String description
    ) {
    }
}