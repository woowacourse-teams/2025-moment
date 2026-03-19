package moment.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import java.util.List;
import moment.admin.domain.AdminGroupLog;
import moment.admin.domain.AdminGroupLogType;
import moment.admin.dto.response.AdminGroupLogListResponse;
import moment.admin.infrastructure.AdminGroupLogRepository;
import moment.admin.service.group.AdminGroupLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceUnderscores.class)
class AdminGroupLogServiceTest {

    @Mock
    private AdminGroupLogRepository adminGroupLogRepository;

    private AdminGroupLogService adminGroupLogService;

    @BeforeEach
    void setUp() {
        adminGroupLogService = new AdminGroupLogService(adminGroupLogRepository);
    }

    @Test
    void getGroupLogs_그룹별_로그_반환() {
        // given
        Long groupId = 1L;
        AdminGroupLog log1 = createLog(1L, groupId, AdminGroupLogType.GROUP_UPDATE);
        AdminGroupLog log2 = createLog(2L, groupId, AdminGroupLogType.MEMBER_KICK);
        Page<AdminGroupLog> page = new PageImpl<>(List.of(log1, log2));

        given(adminGroupLogRepository.findByGroupId(eq(groupId), any(Pageable.class)))
            .willReturn(page);

        // when
        AdminGroupLogListResponse response = adminGroupLogService.getGroupLogs(groupId, null, 0, 20);

        // then
        assertThat(response.content()).hasSize(2);
        assertThat(response.content()).allMatch(log -> log.groupId().equals(groupId));
    }

    @Test
    void getGroupLogs_페이지네이션_적용() {
        // given
        AdminGroupLog log1 = createLog(1L, 1L, AdminGroupLogType.GROUP_UPDATE);
        Page<AdminGroupLog> page = new PageImpl<>(List.of(log1),
            org.springframework.data.domain.PageRequest.of(0, 10), 25);

        given(adminGroupLogRepository.findAll(any(Pageable.class)))
            .willReturn(page);

        // when
        AdminGroupLogListResponse response = adminGroupLogService.getGroupLogs(null, null, 0, 10);

        // then
        assertThat(response.page()).isEqualTo(0);
        assertThat(response.size()).isEqualTo(10);
        assertThat(response.totalElements()).isEqualTo(25);
        assertThat(response.totalPages()).isEqualTo(3);
    }

    @Test
    void getGroupLogs_정렬_최신순() {
        // given
        AdminGroupLog log1 = createLog(1L, 1L, AdminGroupLogType.GROUP_UPDATE);
        AdminGroupLog log2 = createLog(2L, 1L, AdminGroupLogType.GROUP_DELETE);
        Page<AdminGroupLog> page = new PageImpl<>(List.of(log2, log1));

        given(adminGroupLogRepository.findAll(any(Pageable.class)))
            .willReturn(page);

        // when
        AdminGroupLogListResponse response = adminGroupLogService.getGroupLogs(null, null, 0, 20);

        // then
        assertThat(response.content().get(0).id()).isEqualTo(2L);
        assertThat(response.content().get(1).id()).isEqualTo(1L);
    }

    @Test
    void getGroupLogs_로그타입_필터_적용() {
        // given
        AdminGroupLogType type = AdminGroupLogType.GROUP_UPDATE;
        AdminGroupLog log = createLog(1L, 1L, type);
        Page<AdminGroupLog> page = new PageImpl<>(List.of(log));

        given(adminGroupLogRepository.findByType(eq(type), any(Pageable.class)))
            .willReturn(page);

        // when
        AdminGroupLogListResponse response = adminGroupLogService.getGroupLogs(null, type, 0, 20);

        // then
        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).type()).isEqualTo("GROUP_UPDATE");
    }

    @Test
    void getGroupLogs_그룹없어도_빈_리스트_반환() {
        // given
        Long nonExistentGroupId = 999L;
        Page<AdminGroupLog> emptyPage = new PageImpl<>(List.of());

        given(adminGroupLogRepository.findByGroupId(eq(nonExistentGroupId), any(Pageable.class)))
            .willReturn(emptyPage);

        // when
        AdminGroupLogListResponse response = adminGroupLogService.getGroupLogs(nonExistentGroupId, null, 0, 20);

        // then
        assertThat(response.content()).isEmpty();
        assertThat(response.totalElements()).isEqualTo(0);
    }

    private AdminGroupLog createLog(Long id, Long groupId, AdminGroupLogType type) {
        AdminGroupLog log = AdminGroupLog.builder()
            .adminId(1L)
            .adminEmail("admin@test.com")
            .type(type)
            .groupId(groupId)
            .description("테스트 로그")
            .build();
        ReflectionTestUtils.setField(log, "id", id);
        return log;
    }
}
