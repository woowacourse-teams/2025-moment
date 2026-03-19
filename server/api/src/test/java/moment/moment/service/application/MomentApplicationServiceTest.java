package moment.moment.service.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDateTime;
import java.util.List;
import moment.block.domain.UserBlock;
import moment.block.infrastructure.UserBlockRepository;
import moment.comment.domain.Comment;
import moment.comment.infrastructure.CommentRepository;
import moment.config.TestTags;
import moment.fixture.CommentFixture;
import moment.fixture.GroupFixture;
import moment.fixture.MomentFixture;
import moment.fixture.UserFixture;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.global.page.Cursor;
import moment.global.page.PageSize;
import moment.group.domain.Group;
import moment.group.domain.GroupMember;
import moment.group.infrastructure.GroupMemberRepository;
import moment.group.infrastructure.GroupRepository;
import moment.moment.domain.Moment;
import moment.moment.domain.MomentImage;
import moment.moment.dto.request.MomentCreateRequest;
import moment.moment.dto.response.GroupMomentListResponse;
import moment.moment.dto.response.MomentCreateResponse;
import moment.moment.dto.response.tobe.MomentCompositions;
import moment.moment.infrastructure.MomentImageRepository;
import moment.moment.infrastructure.MomentRepository;
import moment.support.MomentCreatedAtHelper;
import moment.user.domain.User;
import moment.user.infrastructure.UserRepository;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@org.junit.jupiter.api.Tag(TestTags.INTEGRATION)
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@Transactional
@ActiveProfiles("test")
@DisplayNameGeneration(ReplaceUnderscores.class)
class MomentApplicationServiceTest {

    @Autowired
    private MomentApplicationService momentApplicationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MomentRepository momentRepository;

    @Autowired
    private MomentCreatedAtHelper momentCreatedAtHelper;

    @Autowired
    private MomentImageRepository momentImageRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    @Autowired
    private UserBlockRepository userBlockRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Test
    void 기본_모멘트를_작성한다() {
        // given
        User user = UserFixture.createUser();
        User momenter = userRepository.save(user);

        MomentCreateRequest request = new MomentCreateRequest(
                "안녕하세요 반갑습니다.",
                "https://test.com/image.jpg",
                "image.jpg"
        );

        // when
        MomentCreateResponse response = momentApplicationService.createBasicMoment(request, momenter.getId());

        // then
        assertAll(
                () -> assertThat(response.momenterId()).isNotNull(),
                () -> assertThat(response.content()).isEqualTo("안녕하세요 반갑습니다.")
        );
    }

    @Test
    void 추가_모멘트를_작성한다() {
        // given
        User user = UserFixture.createUser();
        User momenter = userRepository.save(user);

        MomentCreateRequest request = new MomentCreateRequest(
                "안녕하세요 반갑습니다.",
                "https://test.com/image.jpg",
                "image.jpg"
        );

        // when
        MomentCreateResponse response = momentApplicationService.createExtraMoment(request, momenter.getId());

        // then
        assertAll(
                () -> assertThat(response.id()).isNotNull(),
                () -> assertThat(response.content()).isEqualTo("안녕하세요 반갑습니다.")
        );
    }

    @Test
    void 나의_모멘트_조합을_조회한다() {
        // given
        User user = UserFixture.createUser();
        User momenter = userRepository.save(user);

        LocalDateTime start = LocalDateTime.of(2025, 1, 1, 0, 0);
        Moment basicmoment = momentCreatedAtHelper.saveMomentWithCreatedAt("1", momenter, start);
        Moment extraMoment1 = momentCreatedAtHelper.saveMomentWithCreatedAt("2", momenter, start.plusHours(1));
        Moment extraMoment2 = momentCreatedAtHelper.saveMomentWithCreatedAt("3", momenter, start.plusHours(2));
        Moment extraMoment3 = momentCreatedAtHelper.saveMomentWithCreatedAt("4", momenter, start.plusHours(3));

        String originalImageUrl = "https://test-bucket-1/test/images/photo2.png";
        momentImageRepository.save(new MomentImage(extraMoment2, originalImageUrl, "photo2.png"));

        Cursor cursor = new Cursor(extraMoment3.getCreatedAt().toString() + "_" + extraMoment3.getId());
        PageSize pageSize = new PageSize(2);

        // when
        MomentCompositions response = momentApplicationService.getMyMomentCompositions(cursor, pageSize,
                momenter.getId());

        // then
        String expectedResolvedUrl = "https://test-bucket-1/test/optimized-images/photo2.png";

        assertAll(
                () -> assertThat(response.momentCompositionInfo()).hasSize(2),
                () -> assertThat(response.nextCursor()).isNotNull(),
                () -> assertThat(response.hasNextPage()).isTrue(),
                () -> assertThat(response.momentCompositionInfo().getFirst().id()).isEqualTo(extraMoment2.getId()),
                () -> assertThat(response.momentCompositionInfo().getFirst().imageUrl()).isEqualTo(expectedResolvedUrl),
                () -> assertThat(response.momentCompositionInfo().getLast().id()).isEqualTo(extraMoment1.getId())
        );
    }

    @Test
    void 그룹_모멘트_조회_시_차단된_사용자의_모멘트가_필터링된다() {
        // given
        User owner = userRepository.save(UserFixture.createUser());
        User blockedUser = userRepository.save(UserFixture.createUser());
        Group group = groupRepository.save(GroupFixture.createGroup(owner));
        GroupMember ownerMember = groupMemberRepository.save(GroupMember.createOwner(group, owner, "오너"));
        GroupMember blockedMember = groupMemberRepository.save(GroupMember.createOwner(group, blockedUser, "차단유저"));

        momentRepository.save(MomentFixture.createMomentInGroup(owner, group, ownerMember));
        momentRepository.save(MomentFixture.createMomentInGroup(blockedUser, group, blockedMember));

        userBlockRepository.save(new UserBlock(owner, blockedUser));

        // when
        GroupMomentListResponse result = momentApplicationService.getGroupMoments(
                group.getId(), owner.getId(), null);

        // then
        assertThat(result.moments()).hasSize(1);
    }

    @Test
    void 차단된_사용자의_모멘트에_좋아요_토글_시_예외가_발생한다() {
        // given
        User owner = userRepository.save(UserFixture.createUser());
        User blockedUser = userRepository.save(UserFixture.createUser());
        Group group = groupRepository.save(GroupFixture.createGroup(owner));
        GroupMember ownerMember = groupMemberRepository.save(GroupMember.createOwner(group, owner, "오너"));
        GroupMember blockedMember = groupMemberRepository.save(GroupMember.createOwner(group, blockedUser, "차단유저"));

        Moment blockedMoment = momentRepository.save(
                MomentFixture.createMomentInGroup(blockedUser, group, blockedMember));

        userBlockRepository.save(new UserBlock(owner, blockedUser));

        // when & then
        assertThatThrownBy(() -> momentApplicationService.toggleMomentLike(
                group.getId(), blockedMoment.getId(), owner.getId()))
                .isInstanceOf(MomentException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BLOCKED_USER_INTERACTION);
    }
}
