package moment.arch;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import jakarta.persistence.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;

@AnalyzeClasses(
    packages = "moment",
    importOptions = ImportOption.DoNotIncludeTests.class
)
class DtoRuleTest extends BaseArchTest {

    // === Phase 2: D-001 ===

    @ArchTest
    static final ArchRule D_001_dto_request와_response_패키지의_클래스는_record여야_한다 =
        classes()
            .that().resideInAnyPackage("..dto.request..", "..dto.response..")
            .should().beAssignableTo(Record.class)
            .because("[D-001] dto/request, dto/response 패키지의 클래스는 Java record여야 합니다. " +
                     "수정 가이드: class를 record로 변경하세요. " +
                     "예: public record UserCreateRequest(String email, String password) {}");

    // === Phase 1: D-002 ~ D-004 ===

    @ArchTest
    static final ArchRule D_002_Controller는_Entity를_직접_반환하지_않는다 =
        classes()
            .that().areAnnotatedWith(RestController.class)
            .should(notReturnEntityFromPublicMethods())
            .because("[D-002] Controller는 Entity를 직접 반환할 수 없습니다. " +
                     "수정 가이드: 반환타입을 Response DTO(record)로 변경하고, " +
                     "DTO.from(entity) 또는 DTO.of(entity)로 변환하세요.");

    @ArchTest
    static final ArchRule D_003_Autowired_필드_주입은_프로덕션_코드에서_금지한다 =
        noFields()
            .that().areDeclaredInClassesThat()
            .resideOutsideOfPackage("..support..")
            .should().beAnnotatedWith(Autowired.class)
            .because("[D-003] 프로덕션 코드에서 @Autowired 필드 주입은 금지됩니다. " +
                     "수정 가이드: @RequiredArgsConstructor + private final 필드로 생성자 주입을 사용하세요.");

    @ArchTest
    static final ArchRule D_004_Application_Service와_Facade_Service는_Entity를_직접_반환하지_않는다 =
        classes()
            .that().resideInAnyPackage("..service.application..", "..service.facade..")
            .and().areAnnotatedWith(Service.class)
            .should(notReturnEntityFromActionMethods())
            .because("[D-004] Application Service와 Facade Service의 액션 메서드는 Entity를 직접 반환할 수 없습니다. " +
                     "get 접두사 메서드(내부 조회용)는 허용됩니다. " +
                     "수정 가이드: 반환타입을 Response DTO(record)로 변경하고, " +
                     "DTO.from(entity) 또는 DTO.of(entity)로 변환하세요.");

    // === 커스텀 ArchCondition ===

    private static ArchCondition<JavaClass> notReturnEntityFromPublicMethods() {
        return new ArchCondition<>("not return @Entity classes from public methods") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                for (JavaMethod method : javaClass.getMethods()) {
                    if (!method.getModifiers().contains(JavaModifier.PUBLIC)) {
                        continue;
                    }
                    checkEntityReturn(javaClass, method, events);
                }
            }
        };
    }

    private static ArchCondition<JavaClass> notReturnEntityFromActionMethods() {
        return new ArchCondition<>("not return @Entity classes from action methods") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                for (JavaMethod method : javaClass.getMethods()) {
                    if (!method.getModifiers().contains(JavaModifier.PUBLIC)) {
                        continue;
                    }
                    String methodName = method.getName();
                    if (methodName.startsWith("get") || methodName.startsWith("find")
                        || methodName.startsWith("create")) {
                        continue;
                    }
                    checkEntityReturn(javaClass, method, events);
                }
            }
        };
    }

    private static void checkEntityReturn(JavaClass javaClass, JavaMethod method, ConditionEvents events) {
        JavaClass returnType = method.getRawReturnType();

        if (isEntityClass(returnType)) {
            events.add(SimpleConditionEvent.violated(javaClass,
                String.format("%s.%s()이(가) Entity(%s)를 직접 반환합니다",
                    javaClass.getSimpleName(),
                    method.getName(),
                    returnType.getSimpleName())));
        }

        method.getReturnType().getAllInvolvedRawTypes().stream()
            .filter(type -> !type.equals(returnType))
            .filter(DtoRuleTest::isEntityClass)
            .forEach(entityType ->
                events.add(SimpleConditionEvent.violated(javaClass,
                    String.format("%s.%s()이(가) 컬렉션으로 Entity(%s)를 반환합니다",
                        javaClass.getSimpleName(),
                        method.getName(),
                        entityType.getSimpleName()))));
    }

    private static boolean isEntityClass(JavaClass javaClass) {
        return javaClass.isAnnotatedWith(Entity.class);
    }
}
