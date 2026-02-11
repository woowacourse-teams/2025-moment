package moment.group.service.group;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import moment.fixture.GroupFixture;
import moment.fixture.UserFixture;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.group.domain.Group;
import moment.group.infrastructure.GroupRepository;
import moment.user.domain.User;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceUnderscores.class)
class GroupServiceTest {

    @Mock
    private GroupRepository groupRepository;

    @InjectMocks
    private GroupService groupService;

    @Test
    void 그룹_생성_성공() {
        // Given
        User owner = UserFixture.createUser();
        when(groupRepository.save(any(Group.class))).thenAnswer(i -> i.getArgument(0));

        // When
        Group group = groupService.create("테스트 그룹", "설명", owner);

        // Then
        assertThat(group.getName()).isEqualTo("테스트 그룹");
        assertThat(group.getDescription()).isEqualTo("설명");
        assertThat(group.getOwner()).isEqualTo(owner);
        verify(groupRepository).save(any(Group.class));
    }

    @Test
    void 그룹_조회_성공() {
        // Given
        User owner = UserFixture.createUser();
        Group group = GroupFixture.createGroup(owner);
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));

        // When
        Group result = groupService.getById(1L);

        // Then
        assertThat(result).isEqualTo(group);
    }

    @Test
    void 그룹_조회_존재하지_않음() {
        // Given
        when(groupRepository.findById(1L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> groupService.getById(1L))
            .isInstanceOf(MomentException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.GROUP_NOT_FOUND);
    }

    @Test
    void 그룹_수정_성공() {
        // Given
        User owner = UserFixture.createUserWithId(1L);
        Group group = GroupFixture.createGroup(owner);
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));

        // When
        groupService.update(1L, "새이름", "새설명", owner);

        // Then
        assertThat(group.getName()).isEqualTo("새이름");
        assertThat(group.getDescription()).isEqualTo("새설명");
    }

    @Test
    void 그룹_수정_소유자가_아님() {
        // Given
        User owner = UserFixture.createUserWithId(1L);
        User other = UserFixture.createUserWithId(2L);
        Group group = GroupFixture.createGroup(owner);
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));

        // When/Then
        assertThatThrownBy(() -> groupService.update(1L, "새이름", "새설명", other))
            .isInstanceOf(MomentException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_GROUP_OWNER);
    }

    @Test
    void 그룹_삭제_성공() {
        // Given
        User owner = UserFixture.createUserWithId(1L);
        Group group = GroupFixture.createGroup(owner);
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));

        // When
        groupService.delete(1L, owner);

        // Then
        verify(groupRepository).delete(group);
    }

    @Test
    void 그룹_삭제_소유자가_아님() {
        // Given
        User owner = UserFixture.createUserWithId(1L);
        User other = UserFixture.createUserWithId(2L);
        Group group = GroupFixture.createGroup(owner);
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));

        // When/Then
        assertThatThrownBy(() -> groupService.delete(1L, other))
            .isInstanceOf(MomentException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_GROUP_OWNER);
    }
}
