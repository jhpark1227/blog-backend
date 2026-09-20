package junhyeok.blog.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ChatRequest(
        @Pattern(regexp = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$",
                message = "잘못된 대화 ID 형식입니다.")
        String conversationId,

        @NotBlank(message = "질문을 입력해주세요.")
        @Size(max = 1000, message = "질문은 1000자를 넘을 수 없습니다.")
        String prompt
) {
}