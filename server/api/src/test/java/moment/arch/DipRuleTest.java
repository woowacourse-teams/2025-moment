package moment.arch;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.springframework.context.annotation.Configuration;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(
    packages = "moment",
    importOptions = ImportOption.DoNotIncludeTests.class
)
class DipRuleTest extends BaseArchTest {

    @ArchTest
    static final ArchRule DIP_001_infrastructure의_외부_클라이언트는_service의_인터페이스를_구현해야_한다 =
        classes()
            .that().resideInAPackage("..infrastructure..")
            .and().areNotInterfaces()
            .and().haveSimpleNameNotEndingWith("Repository")
            .and().areNotAnnotatedWith(Configuration.class)
            .and(areNotInfrastructureInternalClasses())
            .should(implementInterfaceInServicePackage())
            .because("[DIP-001] infrastructure의 외부 클라이언트 구현체는 " +
                     "service 패키지의 인터페이스를 구현해야 합니다. " +
                     "수정 가이드: service 패키지에 인터페이스를 정의하고, " +
                     "infrastructure 클래스가 이를 구현하도록 변경하세요.");

    @ArchTest
    static final ArchRule DIP_002_service는_infrastructure_구체_클래스에_직접_의존하지_않는다 =
        noClasses()
            .that().resideInAPackage("..service..")
            .should().dependOnClassesThat(
                DescribedPredicate.describe(
                    "are concrete classes in infrastructure package (excluding Emitters and Repositories)",
                    javaClass -> javaClass.getPackageName().contains(".infrastructure")
                        && !javaClass.isInterface()
                        && !javaClass.getSimpleName().equals("Emitters")
                )
            )
            .because("[DIP-002] service 패키지는 infrastructure의 구체 클래스에 직접 의존할 수 없습니다. " +
                     "Repository 인터페이스와 Emitters(SSE 인프라)는 허용됩니다. " +
                     "수정 가이드: service 패키지의 인터페이스를 통해 의존하세요.");

    // === Helper Methods ===

    private static DescribedPredicate<JavaClass> areNotInfrastructureInternalClasses() {
        return DescribedPredicate.describe(
            "are not infrastructure internal classes",
            javaClass -> {
                String fullName = javaClass.getName();
                String simpleName = javaClass.getSimpleName();
                String packageName = javaClass.getPackageName();
                return !simpleName.endsWith("Converter")
                    && !simpleName.endsWith("Listener")
                    && !simpleName.endsWith("Initializer")
                    && !simpleName.endsWith("Exception")
                    && !simpleName.endsWith("Message")
                    && !simpleName.endsWith("Receipt")
                    && !simpleName.endsWith("Response")
                    && !fullName.contains("$")
                    && !simpleName.equals("Emitters")
                    && !packageName.contains(".expo");
            }
        );
    }

    private static ArchCondition<JavaClass> implementInterfaceInServicePackage() {
        return new ArchCondition<>("implement an interface from service package") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                boolean implementsServiceInterface = javaClass.getRawInterfaces().stream()
                    .anyMatch(iface -> iface.getPackageName().contains(".service."));

                if (!implementsServiceInterface) {
                    events.add(SimpleConditionEvent.violated(javaClass,
                        String.format("%s이(가) service 패키지의 인터페이스를 구현하지 않습니다",
                            javaClass.getName())));
                }
            }
        };
    }
}
