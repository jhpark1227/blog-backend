package junhyeok.blog.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    INVALID_CATEGORY_ID("유효하지 않은 카테고리ID입니다."),
    CATEGORY_NOT_FOUND("존재하지 않는 카테고리입니다."),
    POST_NOT_FOUND("존재하지 않는 글입니다."),
    NOTION_RESPONSE_INVALID("Notion 응답을 파싱할 수 없습니다."),
    INVALID_EXCERPT_LENGTH("잘못된 요약문 길이입니다."),
    GEMINI_RESPONSE_INVALID("Gemini 응답을 파싱할 수 없습니다."),
    SYNCED_AT_IS_BEFORE("갱신 시간은 기존 값보다 이전일 수 없습니다."),
    EMBEDDED_AT_IS_BEFORE("임베딩 시간은 기존 값보다 이전일 수 없습니다."),
    ;

    private final String message;
}
