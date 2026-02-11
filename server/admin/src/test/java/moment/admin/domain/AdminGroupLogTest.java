package moment.admin.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;

@DisplayNameGeneration(ReplaceUnderscores.class)
class AdminGroupLogTest {

    @Test
    void AdminGroupLog_생성_성공() {
        // given
        Long adminId = 1L;
        String adminEmail = "admin@test.com";
        AdminGroupLogType type = AdminGroupLogType.GROUP_UPDATE;
        Long groupId = 10L;
        Long targetId = 100L;
        String description = "그룹명 변경";

        // when
        AdminGroupLog log = AdminGroupLog.builder()
            .adminId(adminId)
            .adminEmail(adminEmail)
            .type(type)
            .groupId(groupId)
            .targetId(targetId)
            .description(description)
            .build();

        // then
        assertAll(
            () -> assertThat(log.getAdminId()).isEqualTo(adminId),
            () -> assertThat(log.getAdminEmail()).isEqualTo(adminEmail),
            () -> assertThat(log.getType()).isEqualTo(type),
            () -> assertThat(log.getGroupId()).isEqualTo(groupId),
            () -> assertThat(log.getTargetId()).isEqualTo(targetId),
            () -> assertThat(log.getDescription()).isEqualTo(description)
        );
    }

    @Test
    void AdminGroupLog_beforeValue_afterValue_JSON_저장() {
        // given
        String beforeValue = "{\"name\":\"이전 그룹명\",\"description\":\"이전 설명\"}";
        String afterValue = "{\"name\":\"새 그룹명\",\"description\":\"새 설명\"}";

        // when
        AdminGroupLog log = AdminGroupLog.builder()
            .adminId(1L)
            .adminEmail("admin@test.com")
            .type(AdminGroupLogType.GROUP_UPDATE)
            .groupId(10L)
            .beforeValue(beforeValue)
            .afterValue(afterValue)
            .build();

        // then
        assertAll(
            () -> assertThat(log.getBeforeValue()).isEqualTo(beforeValue),
            () -> assertThat(log.getAfterValue()).isEqualTo(afterValue)
        );
    }
}
