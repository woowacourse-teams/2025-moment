package moment.arch;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

@AnalyzeClasses(
    packages = "moment",
    importOptions = ImportOption.DoNotIncludeTests.class
)
class ModuleBoundaryRuleTest extends BaseArchTest {

    // auth→user, storage→user 의존은 핵심 인증/인가 흐름으로 허용
    private static final Map<String, Set<String>> ALLOWED_CROSS_DOMAIN_DEPS = Map.of(
        "auth", Set.of("user"),
        "storage", Set.of("user")
    );

    @ArchTest
    static final ArchRule M_003_도메인_간_서비스_의존은_Application_또는_Facade를_통해서만_가능하다 =
        classes()
            .that().areAnnotatedWith(Service.class)
            .and().resideInAPackage("..service.(*)..")
            .and().resideOutsideOfPackage("..service.application..")
            .and().resideOutsideOfPackage("..service.facade..")
            .and().resideOutsideOfPackage("..service.eventHandler..")
            .should(notDependOnOtherDomainServiceDirectly())
            .because("[M-003] 도메인 간 서비스 의존은 Application 또는 Facade를 통해서만 가능합니다. " +
                     "수정 가이드: 다른 도메인의 Domain Service를 직접 호출하지 말고, " +
                     "해당 도메인의 Application Service 또는 Facade Service를 통해 접근하세요. " +
                     "허용 예외: auth→user, storage→user (핵심 인증/인가 흐름).");

    // === 커스텀 ArchCondition ===

    private static ArchCondition<JavaClass> notDependOnOtherDomainServiceDirectly() {
        return new ArchCondition<>("not depend on other domain's Domain Service directly") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                String currentDomain = extractDomain(javaClass.getPackageName());
                if (currentDomain == null) return;

                javaClass.getDirectDependenciesFromSelf().stream()
                    .map(dep -> dep.getTargetClass())
                    .filter(target -> target.isAnnotatedWith(Service.class))
                    .filter(this::isDomainService)
                    .filter(target -> {
                        String targetDomain = extractDomain(target.getPackageName());
                        return targetDomain != null && !targetDomain.equals(currentDomain);
                    })
                    .filter(target -> {
                        String targetDomain = extractDomain(target.getPackageName());
                        Set<String> allowed = ALLOWED_CROSS_DOMAIN_DEPS.getOrDefault(
                            currentDomain, Set.of());
                        return !allowed.contains(targetDomain);
                    })
                    .forEach(target -> {
                        events.add(SimpleConditionEvent.violated(javaClass,
                            String.format("%s이(가) 다른 도메인의 Domain Service(%s)에 직접 의존합니다. " +
                                "Application 또는 Facade Service를 통해 접근하세요.",
                                javaClass.getSimpleName(),
                                target.getSimpleName())));
                    });
            }

            private String extractDomain(String packageName) {
                String[] parts = packageName.split("\\.");
                if (parts.length >= 2 && "moment".equals(parts[0])) {
                    return parts[1];
                }
                return null;
            }

            private boolean isDomainService(JavaClass javaClass) {
                String pkg = javaClass.getPackageName();
                return pkg.contains(".service.")
                    && !pkg.contains(".service.application")
                    && !pkg.contains(".service.facade")
                    && !pkg.contains(".service.eventHandler");
            }
        };
    }
}
