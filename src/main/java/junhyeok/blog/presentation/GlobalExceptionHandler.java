package junhyeok.blog.presentation;

import java.util.Map;
import junhyeok.blog.global.exception.CustomException;
import junhyeok.blog.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final Map<ErrorCode, HttpStatus> errorCodeHttpStatusMapping = Map.of(
            ErrorCode.INVALID_CATEGORY_ID, HttpStatus.BAD_REQUEST,
            ErrorCode.CATEGORY_NOT_FOUND, HttpStatus.NOT_FOUND,
            ErrorCode.POST_NOT_FOUND, HttpStatus.NOT_FOUND,
            ErrorCode.NOTION_RESPONSE_INVALID, HttpStatus.INTERNAL_SERVER_ERROR
    );

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleApplicationException(CustomException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        log.info("[APPLICATION_EXCEPTION] {} - {}", errorCode.name(), errorCode.getMessage());
        return ResponseEntity.status(errorCodeHttpStatusMapping.getOrDefault(errorCode, HttpStatus.BAD_REQUEST))
                .body(ErrorResponse.from(errorCode));
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleHandlerMethodValidationException(HandlerMethodValidationException exception) {
        log.info("[HANDLER_METHOD_VALIDATION_EXCEPTION] - {}",
                exception.getParameterValidationResults().stream().flatMap(r -> r.getResolvableErrors().stream()).map(
                        MessageSourceResolvable::getDefaultMessage).toList());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("INVALID_REQUEST"));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFoundException(NoResourceFoundException exception) {
        log.debug("[NO_RESOURCE_FOUND] {}", exception.getResourcePath());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("NOT_FOUND"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception exception) {
        log.error("UNEXPECTED_EXCEPTION", exception);
        return ResponseEntity.internalServerError()
                .body(new ErrorResponse("INTERNAL_SERVER_ERROR"));
    }
}
