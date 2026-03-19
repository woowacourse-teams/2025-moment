package moment.arch;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

@AnalyzeClasses(
    packages = "moment",
    importOptions = ImportOption.DoNotIncludeTests.class
)
class LayerDependencyRuleTest extends BaseArchTest {

    // === Phase 1: L-001 ~ L-004 ===

    @ArchTest
    static final ArchRule 레이어_의존성_규칙 = layeredArchitecture()
        .consideringOnlyDependenciesInLayers()
        .layer("Presentation").definedBy("..presentation..")
        .layer("Service").definedBy("..service..")
        .layer("Domain").definedBy("..domain..")
        .layer("Infrastructure").definedBy("..infrastructure..")
        .layer("Dto").definedBy("..dto..")
        .layer("Global").definedBy("..global..")

        // L-001: presentation은 service, dto, domain, global 의존 가능
        .whereLayer("Presentation").mayOnlyAccessLayers(
            "Service", "Dto", "Domain", "Global")

        // L-002: service는 presentation에 의존 불가
        .whereLayer("Service").mayOnlyAccessLayers(
            "Service", "Domain", "Infrastructure", "Dto", "Global")

        // L-003: domain은 다른 레이어에 의존하지 않음 (JPA 컨버터 참조는 ignoreDependency로 처리)
        .whereLayer("Domain").mayOnlyAccessLayers("Domain", "Global")

        // L-004: infrastructure는 domain, service(DIP 인터페이스), dto 의존 가능
        .whereLayer("Infrastructure").mayOnlyAccessLayers(
            "Domain", "Infrastructure", "Global", "Service", "Dto")

        // Domain → Infrastructure 예외: JPA @Convert 참조 (SourceDataConverter 등)
        .ignoreDependency(resideInAPackage("..domain.."), resideInAPackage("..infrastructure.."))

        .because("[L-001~L-004] 레이어 의존성은 presentation -> service -> domain 방향이어야 합니다. " +
                 "Infrastructure는 DIP를 위해 Service 인터페이스에 접근 가능합니다. " +
                 "수정 가이드: 역방향 의존이 발견되면 service 레이어를 통해 접근하도록 수정하세요.");

    // === Phase 2: L-005 ~ L-006 ===

    @ArchTest
    static final ArchRule L_005_dto_패키지는_domain에만_의존할_수_있다 =
        classes()
            .that().resideInAPackage("..dto..")
            .should().onlyDependOnClassesThat()
            .resideInAnyPackage(
                "..dto..",
                "..domain..",
                "..global..",
                "java..",
                "jakarta..",
                "lombok..",
                "io.swagger..",
                "com.fasterxml..",
                "org.springframework.."
            )
            .because("[L-005] dto 패키지는 domain 패키지에만 의존할 수 있습니다. " +
                     "수정 가이드: DTO에서 service/infrastructure/presentation 패키지의 " +
                     "클래스를 참조하지 마세요. DTO는 domain 엔티티를 변환하는 역할만 합니다.");

    @ArchTest
    static final ArchRule L_006_eventHandler는_facade_또는_application_서비스만_의존할_수_있다 =
        classes()
            .that().resideInAPackage("..service.eventHandler..")
            .should().onlyDependOnClassesThat()
            .resideInAnyPackage(
                "..service.eventHandler..",
                "..service.facade..",
                "..service.application..",
                "..dto..",
                "..domain..",
                "..global..",
                "java..",
                "org.springframework..",
                "org.slf4j..",
                "lombok..",
                "net.logstash.."
            )
            .because("[L-006] eventHandler는 facade 또는 application 서비스만 의존할 수 있습니다. " +
                     "수정 가이드: eventHandler에서 domain 서비스를 직접 호출하지 말고, " +
                     "facade 또는 application 서비스를 통해 호출하세요.");
}
