package moment.moment.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import moment.user.domain.User;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class PointDeductionPolicyTest {

    @InjectMocks
    private PointDeductionPolicy pointDeductionPolicy;

    @Mock
    private User user;

    @Test
    void 포인트가_충분할_경우_추가_모멘트를_작성할_수_있다() {
        // given
        when(user.canNotUseStars(any(Integer.class))).thenReturn(false);

        // when
        boolean result = pointDeductionPolicy.canNotCreate(user);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void 포인트가_부족할_경우_추가_모멘트를_작성할_수_없다() {
        // given
        when(user.canNotUseStars(any(Integer.class))).thenReturn(true);

        // when
        boolean result = pointDeductionPolicy.canNotCreate(user);

        // then
        assertThat(result).isTrue();
    }
}
