package junhyeok.blog.infrastructure;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import junhyeok.blog.domain.PostData;
import junhyeok.blog.domain.PostStatus;

public record NotionDataSourceQueryResponse(
        List<PageResult> results,

        @JsonProperty("next_cursor")
        String nextCursor,

        @JsonProperty("has_more")
        boolean hasMore
) {

    public record PageResult(
            String id,

            @JsonProperty("created_time")
            String createdTime,

            @JsonProperty("last_edited_time")
            String lastEditedTime,

            Properties properties
    ) {
        public PostData convertToPostData() {
            return new PostData(
                    id,
                    properties.titleProperty
                            .plainText(),
                    PostStatus.valueOf(
                            properties.statusProperty
                                    .select
                                    .name
                                    .toUpperCase()
                    ),
                    properties.publishedDateProperty
                            .getPublishedDate(),
                    toKst(createdTime),
                    toKst(lastEditedTime),
                    properties.categoryProperty.select.id,
                    properties().tagProperty()
                            .multiSelects()
                            .stream()
                            .map(o -> o.id)
                            .toList(),
                    properties.fixedProperty
                            .checkbox
            );
        }

        private LocalDateTime toKst(String isoDateTime) {
            return OffsetDateTime.parse(isoDateTime)
                    .atZoneSameInstant(ZoneId.of("Asia/Seoul"))
                    .toLocalDateTime();
        }
    }

    public record Properties(
            @JsonProperty("제목")
            TitleProperty titleProperty,
            @JsonProperty("분류")
            CategoryProperty categoryProperty,
            @JsonProperty("태그")
            TagProperty tagProperty,
            @JsonProperty("상태")
            StatusProperty statusProperty,
            @JsonProperty("날짜")
            PublishedDateProperty publishedDateProperty,
            @JsonProperty("고정")
            FixedProperty fixedProperty
    ) {
    }

    public record TitleProperty(
            @JsonProperty("title")
            List<TitleDetail> titleDetails
    ) {
        public String plainText() {
            if (titleDetails == null || titleDetails.isEmpty()) {
                return "";
            }
            return titleDetails.get(0).plainText();
        }
    }

    public record TitleDetail(
            @JsonProperty("plain_text")
            String plainText
    ) {
    }

    private record CategoryProperty(
            String id,
            String type,
            Select select
    ) {
    }

    private record TagProperty(
            @JsonProperty("multi_select")
            List<MultiSelect> multiSelects
    ) {
    }

    private record MultiSelect(
            String id,
            String name,
            String color
    ) {
    }

    private record StatusProperty(
            Select select
    ) {
    }

    private record Select(
            String id,
            String name,
            String color
    ) {
    }

    private record PublishedDateProperty(
            String id,
            String type,
            Date date
    ) {
        private LocalDate getPublishedDate() {
            if (date == null) {
                return null;
            }
            return date.start;
        }
    }

    private record Date(
            LocalDate start,
            LocalDate end,
            String timeZone
    ) {
    }

    private record FixedProperty(
            String id,
            String type,
            boolean checkbox
    ) {
    }
}