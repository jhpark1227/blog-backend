package junhyeok.blog.application.dto.response;

import junhyeok.blog.domain.Tag;

public record TagResponse(
        String tagId,
        String name,
        String color,
        int sortOrder
) {
    public static TagResponse from(Tag tag) {
        return new TagResponse(tag.getNotionOptionId(), tag.getName(), tag.getColor(), tag.getSortOrder());
    }
}
