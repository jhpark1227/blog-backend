package junhyeok.blog.presentation;

import junhyeok.blog.global.exception.ErrorCode;

public record ErrorResponse(
        String errorCode
) {

    public static ErrorResponse from(ErrorCode errorCode) {
        return new ErrorResponse(errorCode.name());
    }
}
