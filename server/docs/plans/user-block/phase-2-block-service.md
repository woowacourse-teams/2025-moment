# Phase 2: Block 서비스 레이어

- **Status**: DRAFT
- **Created**: 2026-02-09
- **Parent Plan**: [user-block-plan.md](../user-block-plan.md)
- **Depends On**: Phase 1

---

## 목표

UserBlockService (도메인 서비스), UserBlockApplicationService (애플리케이션 서비스), DTO, Controller를 구현하여 차단/해제/목록 API를 완성한다.

---

## 현재 상태 분석

### 서비스 계층 패턴

**도메인 서비스 패턴** (`MomentService.java`):
```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MomentService {
    private final MomentRepository momentRepository;

    @Transactional
    public Moment create(...) { ... }

    public Moment getMomentBy(Long momentId) {
        return momentRepository.findById(momentId)
                .orElseThrow(() -> new MomentException(ErrorCode.MOMENT_NOT_FOUND));
    }
}
```

**애플리케이션 서비스 패턴** (`MomentApplicationService.java`):
```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MomentApplicationService {
    private final UserService userService;
    private final MomentService momentService;

    @Transactional
    public MomentCreateResponse createBasicMoment(MomentCreateRequest request, Long momenterId) {
        User momenter = userService.getUserBy(momenterId);
        Moment savedMoment = momentService.create(request.content(), momenter);
        return MomentCreateResponse.of(savedMoment);
    }
}
```

### 컨트롤러 패턴 (`MomentController.java`)

```java
@Tag(name = "Moment API", description = "모멘트 관련 API 명세")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/moments")
public class MomentController {
    @Operation(summary = "...", description = "...")
    @ApiResponses({...})
    @PostMapping
    public ResponseEntity<SuccessResponse<MomentCreateResponse>> createBasicMoment(
            @Valid @RequestBody MomentCreateRequest request,
            @AuthenticationPrincipal Authentication authentication
    ) {
        ...
        HttpStatus status = HttpStatus.CREATED;
        return ResponseEntity.status(status).body(SuccessResponse.of(status, response));
    }
}
```

### 인증 패턴

- `@AuthenticationPrincipal Authentication authentication` - JWT 쿠키에서 추출
- `authentication.id()` - 현재 로그인한 사용자 ID

---

## TDD 테스트 목록

### 2-1. UserBlockService 테스트

| # | 테스트명 | 설명 |
|---|---------|------|
| T1 | `사용자를_차단한다` | 새로운 차단 관계 생성 |
| T2 | `자기_자신을_차단하면_예외가_발생한다` | BL-001 |
| T3 | `이미_차단된_사용자를_다시_차단하면_예외가_발생한다` | BL-002 |
| T4 | `soft_delete된_차단을_재차단하면_restore한다` | restore() 호출 확인 |
| T5 | `차단을_해제한다` | soft delete 확인 |
| T6 | `존재하지_않는_차단을_해제하면_예외가_발생한다` | BL-003 |
| T7 | `양방향_차단된_사용자_ID_목록을_반환한다` | UNION ALL 쿼리 결과 |
| T8 | `차단_목록이_비어있으면_빈_리스트를_반환한다` | 빈 결과 처리 |
| T9 | `양방향_차단_여부를_확인한다_차단된_경우` | isBlocked true |
| T10 | `양방향_차단_여부를_확인한다_차단되지_않은_경우` | isBlocked false |
| T11 | `내가_차단한_사용자_목록을_반환한다` | 차단 목록 조회 |

### 2-2. UserBlockApplicationService 테스트

| # | 테스트명 | 설명 |
|---|---------|------|
| T1 | `사용자를_차단하고_응답을_반환한다` | UserBlockResponse 확인 |
| T2 | `차단을_해제한다` | unblock 위임 확인 |
| T3 | `차단된_사용자_ID_목록을_반환한다` | 위임 확인 |
| T4 | `차단_목록을_반환한다` | UserBlockListResponse 목록 |

---

## 구현 단계

### Step 1: UserBlockService 구현

**파일 생성**: `src/main/java/moment/block/service/block/UserBlockService.java`

```java
package moment.block.service.block;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import moment.block.domain.UserBlock;
import moment.block.infrastructure.UserBlockRepository;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserBlockService {

    private final UserBlockRepository userBlockRepository;

    @Transactional
    public UserBlock block(User blocker, User blockedUser) {
        validateNotSelf(blocker, blockedUser);
        validateNotAlreadyBlocked(blocker, blockedUser);

        Optional<UserBlock> deletedBlock = userBlockRepository
                .findByBlockerAndBlockedUserIncludeDeleted(blocker.getId(), blockedUser.getId());

        if (deletedBlock.isPresent()) {
            UserBlock existing = deletedBlock.get();
            existing.restore();
            return existing;
        }

        return userBlockRepository.save(new UserBlock(blocker, blockedUser));
    }

    @Transactional
    public void unblock(User blocker, User blockedUser) {
        UserBlock userBlock = userBlockRepository.findByBlockerAndBlockedUser(blocker, blockedUser)
                .orElseThrow(() -> new MomentException(ErrorCode.BLOCK_NOT_FOUND));
        userBlockRepository.delete(userBlock);
    }

    public List<Long> getBlockedUserIds(Long userId) {
        List<Long> blockedUserIds = userBlockRepository.findBlockedUserIds(userId);
        if (blockedUserIds == null || blockedUserIds.isEmpty()) {
            return Collections.emptyList();
        }
        return blockedUserIds;
    }

    public boolean isBlocked(Long userId1, Long userId2) {
        return userBlockRepository.existsBidirectionalBlock(userId1, userId2);
    }

    public List<UserBlock> getBlockedUsers(User blocker) {
        return userBlockRepository.findAllByBlocker(blocker);
    }

    private void validateNotSelf(User blocker, User blockedUser) {
        if (blocker.getId().equals(blockedUser.getId())) {
            throw new MomentException(ErrorCode.BLOCK_SELF);
        }
    }

    private void validateNotAlreadyBlocked(User blocker, User blockedUser) {
        if (userBlockRepository.existsByBlockerAndBlockedUser(blocker, blockedUser)) {
            throw new MomentException(ErrorCode.BLOCK_ALREADY_EXISTS);
        }
    }
}
```

### Step 2: UserBlockApplicationService 구현

**파일 생성**: `src/main/java/moment/block/service/application/UserBlockApplicationService.java`

```java
package moment.block.service.application;

import java.util.List;
import lombok.RequiredArgsConstructor;
import moment.block.domain.UserBlock;
import moment.block.dto.response.UserBlockListResponse;
import moment.block.dto.response.UserBlockResponse;
import moment.block.service.block.UserBlockService;
import moment.user.domain.User;
import moment.user.service.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserBlockApplicationService {

    private final UserService userService;
    private final UserBlockService userBlockService;

    @Transactional
    public UserBlockResponse blockUser(Long blockerId, Long blockedUserId) {
        User blocker = userService.getUserBy(blockerId);
        User blockedUser = userService.getUserBy(blockedUserId);
        UserBlock userBlock = userBlockService.block(blocker, blockedUser);
        return UserBlockResponse.from(userBlock);
    }

    @Transactional
    public void unblockUser(Long blockerId, Long blockedUserId) {
        User blocker = userService.getUserBy(blockerId);
        User blockedUser = userService.getUserBy(blockedUserId);
        userBlockService.unblock(blocker, blockedUser);
    }

    public List<Long> getBlockedUserIds(Long userId) {
        return userBlockService.getBlockedUserIds(userId);
    }

    public boolean isBlocked(Long userId1, Long userId2) {
        return userBlockService.isBlocked(userId1, userId2);
    }

    public List<UserBlockListResponse> getBlockedUsers(Long userId) {
        User blocker = userService.getUserBy(userId);
        List<UserBlock> blocks = userBlockService.getBlockedUsers(blocker);
        return blocks.stream()
                .map(UserBlockListResponse::from)
                .toList();
    }
}
```

### Step 3: Response DTO 생성

**파일 생성**: `src/main/java/moment/block/dto/response/UserBlockResponse.java`

```java
package moment.block.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import moment.block.domain.UserBlock;

@Schema(description = "사용자 차단 응답")
public record UserBlockResponse(
        @Schema(description = "차단된 사용자 ID", example = "2")
        Long blockedUserId,

        @Schema(description = "차단 시간", example = "2025-07-14T16:24:34")
        LocalDateTime createdAt
) {
    public static UserBlockResponse from(UserBlock userBlock) {
        return new UserBlockResponse(
                userBlock.getBlockedUser().getId(),
                userBlock.getCreatedAt()
        );
    }
}
```

**파일 생성**: `src/main/java/moment/block/dto/response/UserBlockListResponse.java`

```java
package moment.block.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import moment.block.domain.UserBlock;

@Schema(description = "차단 목록 응답")
public record UserBlockListResponse(
        @Schema(description = "차단된 사용자 ID", example = "2")
        Long blockedUserId,

        @Schema(description = "차단된 사용자 닉네임", example = "mimi")
        String nickname,

        @Schema(description = "차단 시간", example = "2025-07-14T16:24:34")
        LocalDateTime createdAt
) {
    public static UserBlockListResponse from(UserBlock userBlock) {
        return new UserBlockListResponse(
                userBlock.getBlockedUser().getId(),
                userBlock.getBlockedUser().getNickname(),
                userBlock.getCreatedAt()
        );
    }
}
```

### Step 4: Controller 구현

**파일 생성**: `src/main/java/moment/block/presentation/UserBlockController.java`

```java
package moment.block.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import moment.auth.presentation.AuthenticationPrincipal;
import moment.block.dto.response.UserBlockListResponse;
import moment.block.dto.response.UserBlockResponse;
import moment.block.service.application.UserBlockApplicationService;
import moment.global.dto.response.ErrorResponse;
import moment.global.dto.response.SuccessResponse;
import moment.user.dto.request.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User Block API", description = "사용자 차단 관련 API 명세")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/users")
public class UserBlockController {

    private final UserBlockApplicationService userBlockApplicationService;

    @Operation(summary = "사용자 차단", description = "특정 사용자를 차단합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "차단 성공"),
            @ApiResponse(responseCode = "400", description = "[BL-001] 자기 자신을 차단할 수 없습니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "[U-009] 존재하지 않는 사용자입니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "[BL-002] 이미 차단된 사용자입니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{userId}/blocks")
    public ResponseEntity<SuccessResponse<UserBlockResponse>> blockUser(
            @PathVariable Long userId,
            @AuthenticationPrincipal Authentication authentication
    ) {
        UserBlockResponse response = userBlockApplicationService.blockUser(authentication.id(), userId);
        HttpStatus status = HttpStatus.CREATED;
        return ResponseEntity.status(status).body(SuccessResponse.of(status, response));
    }

    @Operation(summary = "사용자 차단 해제", description = "차단된 사용자를 차단 해제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "차단 해제 성공"),
            @ApiResponse(responseCode = "404", description = "[BL-003] 차단 관계가 존재하지 않습니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{userId}/blocks")
    public ResponseEntity<Void> unblockUser(
            @PathVariable Long userId,
            @AuthenticationPrincipal Authentication authentication
    ) {
        userBlockApplicationService.unblockUser(authentication.id(), userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "차단 목록 조회", description = "내가 차단한 사용자 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/blocks")
    public ResponseEntity<SuccessResponse<List<UserBlockListResponse>>> getBlockedUsers(
            @AuthenticationPrincipal Authentication authentication
    ) {
        List<UserBlockListResponse> response = userBlockApplicationService.getBlockedUsers(authentication.id());
        HttpStatus status = HttpStatus.OK;
        return ResponseEntity.status(status).body(SuccessResponse.of(status, response));
    }
}
```

### Step 5: 서비스 테스트 작성

**파일 생성**: `src/test/java/moment/block/service/block/UserBlockServiceTest.java`

Mockito 기반 단위 테스트. T1~T11 테스트 메서드를 TDD 순서대로 작성.

**파일 생성**: `src/test/java/moment/block/service/application/UserBlockApplicationServiceTest.java`

Mockito 기반 단위 테스트. T1~T4 테스트 메서드를 TDD 순서대로 작성.

---

## 생성/수정 파일 목록

| 작업 | 파일 경로 |
|------|----------|
| 생성 | `src/main/java/moment/block/service/block/UserBlockService.java` |
| 생성 | `src/main/java/moment/block/service/application/UserBlockApplicationService.java` |
| 생성 | `src/main/java/moment/block/dto/response/UserBlockResponse.java` |
| 생성 | `src/main/java/moment/block/dto/response/UserBlockListResponse.java` |
| 생성 | `src/main/java/moment/block/presentation/UserBlockController.java` |
| 생성 | `src/test/java/moment/block/service/block/UserBlockServiceTest.java` |
| 생성 | `src/test/java/moment/block/service/application/UserBlockApplicationServiceTest.java` |

## 의존성

- Phase 1 완료 필수 (UserBlock, UserBlockRepository, ErrorCode)
- `UserService.getUserBy(Long id)` 사용 (기존 코드)
- `Authentication` record 사용 (기존 코드)

## 주의사항

- `unblock()`은 `userBlockRepository.delete(userBlock)`를 호출하면 `@SQLDelete`에 의해 soft delete 처리됨
- `findByBlockerAndBlockedUserIncludeDeleted`는 native query이므로 soft delete된 레코드도 포함
- `findByBlockerAndBlockedUser`는 `@SQLRestriction` 적용으로 활성 차단만 조회
