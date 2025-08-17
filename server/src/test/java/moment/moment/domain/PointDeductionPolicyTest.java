package moment.moment.domain;

import moment.user.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PointDeductionPolicyTest {

    @InjectMocks
    private PointDeductionPolicy pointDeductionPolicy;

    @Mock
    private User user;

    @Test
    void 포인트가_충분할_경우_추가_모멘트를_작성할_수_있다() {
        // given
        int writablePoint = 10;
        when(user.getAvailableStar()).thenReturn(writablePoint);

        // when
        boolean result = pointDeductionPolicy.canCreate(user);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void 포인트가_부족할_경우_추가_모멘트를_작성할_수_없다() {
        // given
        int unWritablePoint = 9;
        when(user.getAvailableStar()).thenReturn(unWritablePoint);

        // when
        boolean result = pointDeductionPolicy.canCreate(user);

        // then
        assertThat(result).isFalse();
    }
}
