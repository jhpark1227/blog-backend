package junhyeok.blog.infrastructure;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import junhyeok.blog.domain.TagData;

public record NotionDatasourceResponse(
        Properties properties
) {
    public List<TagData> getTagOptions() {
        return properties().tagProperty()
                .multiSelectInfo()
                .options()
                .stream()
                .map(o -> new TagData(o.id, o.name))
                .toList();
    }

    record Properties(
            @JsonProperty("태그")
            TagProperty tagProperty
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