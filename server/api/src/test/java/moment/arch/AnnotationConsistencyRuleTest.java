package moment.arch;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import jakarta.persistence.Entity;
import moment.global.domain.BaseEntity;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(
    packages = "moment",
    importOptions = ImportOption.DoNotIncludeTests.class
)
class AnnotationConsistencyRuleTest extends BaseArchTest {

    // === Phase 1: A-001 ~ A-005 ===

    @ArchTest
    static final ArchRule A_001_Transactional은_service_패키지에서만_사용한다 =
        noClasses()
            .that().resideOutsideOfPackage("..service..")
            .should().beAnnotatedWith(Transactional.class)
            .because("[A-001] @Transactional은 service 패키지에서만 사용해야 합니다. " +
                     "수정 가이드: @Transactional을 Controller/Repository에서 제거하고, " +
                     "트랜잭션 경계를 Service 레이어로 이동하세요.");

    @ArchTest
    static final ArchRule A_003_Entity는_SQLDelete와_SQLRestriction을_사용한다 =
        classes()
            .that().areAnnotatedWith(Entity.class)
            .and().doNotHaveSimpleName("AdminGroupLog")
            .and().doNotHaveSimpleName("RefreshToken")
            .should().beAnnotatedWith(SQLDelete.class)
            .andShould().beAnnotatedWith(SQLRestriction.class)
            .because("[A-003] Entity는 Soft Delete 패턴(@SQLDelete, @SQLRestriction)을 적용해야 합니다. " +
                     "예외: AdminGroupLog (감사 로그), RefreshToken (만료 시 삭제). " +
                     "수정 가이드: @SQLDelete(sql = \"UPDATE {table} SET deleted_at = NOW() WHERE id = ?\")와 " +
                     "@SQLRestriction(\"deleted_at IS NULL\")을 추가하세요.");

    @ArchTest
    static final ArchRule A_004_Entity는_BaseEntity를_확장한다 =
        classes()
            .that().areAnnotatedWith(Entity.class)
            .should().beAssignableTo(BaseEntity.class)
            .because("[A-004] @Entity 클래스는 BaseEntity를 확장해야 합니다. " +
                     "수정 가이드: 클래스 선언에 'extends BaseEntity'를 추가하세요. " +
                     "BaseEntity는 @CreatedDate를 제공합니다.");

    // A-005 제거: Lombok @RequiredArgsConstructor는 @Retention(SOURCE)이므로
    // 바이트코드에 남지 않아 ArchUnit으로 검증 불가. D-003(no @Autowired)로 대체 커버됨.

    // === Phase 2: A-006 ~ A-007 ===

    @ArchTest
    static final ArchRule A_006_Controller의_RequestBody에_Valid를_사용한다 =
        classes()
            .that().areAnnotatedWith(RestController.class)
            .should(useValidWithRequestBody())
            .because("[A-006] Controller에서 @RequestBody를 사용할 때 @Valid를 함께 사용해야 합니다. " +
                     "수정 가이드: @RequestBody 파라미터 앞에 @Valid를 추가하세요.");

    @ArchTest
    static final ArchRule A_007_Service_클래스는_클래스_레벨_Transactional_readOnly_true를_적용한다 =
        classes()
            .that().areAnnotatedWith(Service.class)
            .should(haveTransactionalReadOnlyTrue())
            .because("[A-007] @Service 클래스는 클래스 레벨에 @Transactional(readOnly = true)를 적용해야 합니다. " +
                     "수정 가이드: 클래스에 @Transactional(readOnly = true)를 추가하세요. " +
                     "쓰기 작업 메서드에는 별도로 @Transactional을 오버라이드하세요.");

    // === Phase 3: AU-001 ===

    @ArchTest
    static final ArchRule AU_001_User_API_Controller는_AuthenticationPrincipal을_사용한다 =
        classes()
            .that().areAnnotatedWith(RestController.class)
            .and().resideOutsideOfPackage("..admin..")
            .should(useAuthenticationPrincipal())
            .because("[AU-001] User API Controller는 @AuthenticationPrincipal을 사용해야 합니다. " +
                     "수정 가이드: 인증이 필요한 엔드포인트에 " +
                     "@AuthenticationPrincipal Authentication authentication 파라미터를 추가하세요.");

    // === 커스텀 ArchCondition ===

    private static ArchCondition<JavaClass> useValidWithRequestBody() {
        return new ArchCondition<>("use @Valid with @RequestBody") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                javaClass.getMethods().stream()
                    .filter(method -> method.getModifiers().contains(JavaModifier.PUBLIC))
                    .forEach(method -> {
                        method.getParameterAnnotations().forEach(annotations -> {
                            boolean hasRequestBody = annotations.stream()
                                .anyMatch(a -> a.getRawType().getName().equals(
                                    "org.springframework.web.bind.annotation.RequestBody"));
                            boolean hasValid = annotations.stream()
                                .anyMatch(a -> a.getRawType().getName().equals(
                                    "jakarta.validation.Valid"));

                            if (hasRequestBody && !hasValid) {
                                events.add(SimpleConditionEvent.violated(javaClass,
                                    String.format("[A-006] %s.%s()에서 @RequestBody에 @Valid가 누락되었습니다",
                                        javaClass.getSimpleName(), method.getName())));
                            }
                        });
                    });
            }
        };
    }

    private static ArchCondition<JavaClass> haveTransactionalReadOnlyTrue() {
        return new ArchCondition<>("have @Transactional(readOnly = true) at class level") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                boolean hasReadOnlyTrue = javaClass.getAnnotations().stream()
                    .filter(a -> a.getRawType().getName().equals(
                        "org.springframework.transaction.annotation.Transactional"))
                    .anyMatch(a -> {
                        Object readOnly = a.getProperties().get("readOnly");
                        return Boolean.TRUE.equals(readOnly);
                    });

                if (!hasReadOnlyTrue) {
                    events.add(SimpleConditionEvent.violated(javaClass,
                        String.format("[A-007] %s에 @Transactional(readOnly = true)가 " +
                            "클래스 레벨에 없습니다",
                            javaClass.getSimpleName())));
                }
            }
        };
    }

    private static ArchCondition<JavaClass> useAuthenticationPrincipal() {
        return new ArchCondition<>("use @AuthenticationPrincipal in at least one method") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                String simpleName = javaClass.getSimpleName();
                if (simpleName.contains("Auth") || simpleName.equals("HealthCheckController")) {
                    return;
                }

                boolean hasAuthPrincipal = javaClass.getMethods().stream()
                    .flatMap(method -> method.getParameterAnnotations().stream())
                    .flatMap(java.util.Collection::stream)
                    .anyMatch(annotation ->
                        annotation.getRawType().getSimpleName()
                            .equals("AuthenticationPrincipal"));

                if (!hasAuthPrincipal) {
                    events.add(SimpleConditionEvent.violated(javaClass,
                        String.format("[AU-001] %s에 @AuthenticationPrincipal을 사용하는 메서드가 없습니다",
                            javaClass.getSimpleName())));
                }
            }
        };
    }
}
