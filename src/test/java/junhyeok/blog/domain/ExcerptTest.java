package junhyeok.blog.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import junhyeok.blog.global.exception.CustomException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ExcerptTest {

    @Test
    void 요약이_null_이면_예외가_발생한다() {
        assertThatThrownBy(() -> new Excerpt(null))
                .isInstanceOf(CustomException.class)
                .hasMessage("잘못된 요약문 길이입니다.");
    }

    @Test
    void 글자수제한을_초과하면_예외가_발생한다() {
        assertThatThrownBy(() -> new Excerpt("a".repeat(151)))
                .isInstanceOf(CustomException.class)
                .hasMessage("잘못된 요약문 길이입니다.");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "  "})
    void 빈값이면_예외가_발생한다(String value) {
        assertThatThrownBy(() -> new Excerpt(value))
                .isInstanceOf(CustomException.class)
                .hasMessage("잘못된 요약문 길이입니다.");
    }
}
