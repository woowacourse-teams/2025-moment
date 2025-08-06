package moment.user.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import moment.matching.domain.Matching;
import moment.matching.infrastructure.MatchingRepository;
import moment.moment.domain.Moment;
import moment.moment.infrastructure.MomentRepository;
import moment.user.domain.ProviderType;
import moment.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@DataJpaTest
@DisplayNameGeneration(ReplaceUnderscores.class)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void 이메일을_가진_유저가_있다면_참을_반환한다() {
        // given
        String existedEmail = "mimi@icloud.com";
        userRepository.save(new User(existedEmail, "password", "mimi", ProviderType.EMAIL));

        // when & then
        assertThat(userRepository.existsByEmail(existedEmail)).isTrue();
    }

    @Test
    void 이메일을_가진_유저가_없다면_거짓을_반환한다() {
        // given
        String notExistedEmail = "mimi@icloud.com";
        userRepository.save(new User("hippo@gmail.com", "password", "hippo", ProviderType.EMAIL));

        // when & then
        assertThat(userRepository.existsByEmail(notExistedEmail)).isFalse();
    }

    @Test
    void 닉네임을_가진_유저가_있다면_참을_반환한다() {
        // given
        String existedNickname = "mimi";
        userRepository.save(new User("mimi@icloud.com", "password", existedNickname, ProviderType.EMAIL));

        // when & then
        assertThat(userRepository.existsByNickname(existedNickname)).isTrue();
    }

    @Test
    void 닉네임을_가진_유저가_없다면_거짓을_반환한다() {
        // given
        String notExistedNickname = "hippo";
        userRepository.save(new User("mimi@icloud.com", "password", "mimi", ProviderType.EMAIL));

        // when & then
        assertThat(userRepository.existsByNickname(notExistedNickname)).isFalse();
    }

    @Test
    void 이메일을_가진_유저를_찾는다() {
        // given
        userRepository.save(new User("mimi@icloud.com", "password", "mimi", ProviderType.EMAIL));

        // when
        Optional<User> user = userRepository.findByEmail("mimi@icloud.com");

        // then
        assertThat(user).isPresent();
    }

    @Test
    void 가입되지_않은_이메일은_찾을_수_없다() {
        // given
        userRepository.save(new User("mimi@icloud.com", "password", "mimi", ProviderType.EMAIL));

        // when
        Optional<User> user = userRepository.findByEmail("noUser@gmail.com");

        // then
        assertThat(user).isEmpty();
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public DateTimeProvider dateTimeProvider() {
            return new TestDateTimeProvider();
        }
    }

    @Autowired
    private DateTimeProvider dateTimeProvider;
    private TestDateTimeProvider testDateTimeProvider;

    @Autowired
    private MomentRepository momentRepository;

    @Autowired
    private MatchingRepository matchingRepository;

    @BeforeEach
    void setUp() {
        this.testDateTimeProvider = (TestDateTimeProvider) dateTimeProvider;
    }

    //TODO: 시간 값 비교 문제 해결
    @Disabled
    @Test
    void 오늘_날짜에_매칭_기록이_없는_사용자를_조회한다() {
        // given
        User momenter = userRepository.save(new User("mimi@icloud.com", "1234", "mimi", ProviderType.EMAIL));
        User yesterdayMatchedUser = userRepository.save(new User("hippo@gmail.com", "1234", "hippo", ProviderType.EMAIL));
        User todayMatchedUser = userRepository.save(new User("drago@gmail.com", "1234", "drago", ProviderType.EMAIL));
        User notMatchedUser = userRepository.save(new User("ama@gmail.com", "1234", "ama", ProviderType.EMAIL));

        Moment yesterdayMoment = momentRepository.save(new Moment("hu..", momenter));
        Moment todayMoment = momentRepository.save(new Moment("hu..ha..", momenter));

        LocalDateTime yesterday = LocalDate.now().minusDays(1).atStartOfDay();
        testDateTimeProvider.setNow(yesterday);
        Matching yesterdayMatching = matchingRepository.save(new Matching(yesterdayMoment, yesterdayMatchedUser));

        LocalDateTime today = LocalDate.now().atStartOfDay();
        testDateTimeProvider.setNow(today);
        matchingRepository.save(new Matching(todayMoment, todayMatchedUser));

        // when
        LocalDateTime endOfDay = LocalDate.now().plusDays(1).atStartOfDay();
        List<User> result = userRepository.findNotMatchedUsersToday(today, endOfDay, momenter);

        // then
        assertThat(result).hasSize(2);
    }
}
