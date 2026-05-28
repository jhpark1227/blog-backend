package junhyeok.blog.infrastructure;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import junhyeok.blog.domain.PostData;

public record NotionDataSourceResponse(
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
                    properties.title.plainText(),
                    toKst(createdTime),
                    toKst(lastEditedTime)
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
            TitleProperty title
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
}