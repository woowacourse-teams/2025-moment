package moment.arch;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

@AnalyzeClasses(
    packages = "moment",
    importOptions = ImportOption.DoNotIncludeTests.class
)
class NamingConventionRuleTest extends BaseArchTest {

    // === Phase 1: N-001 ~ N-003 ===

    @ArchTest
    static final ArchRule N_001_Service_어노테이션_클래스는_Service로_끝나야_한다 =
        classes()
            .that().areAnnotatedWith(Service.class)
            .should().haveSimpleNameEndingWith("Service")
            .because("[N-001] @Service 어노테이션이 붙은 클래스는 'Service'로 끝나야 합니다. " +
                     "수정 가이드: 클래스명을 {도메인명}Service로 변경하세요.");

    @ArchTest
    static final ArchRule N_002_RestController_어노테이션_클래스는_Controller로_끝나야_한다 =
        classes()
            .that().areAnnotatedWith(RestController.class)
            .should().haveSimpleNameEndingWith("Controller")
            .because("[N-002] @RestController 어노테이션이 붙은 클래스는 'Controller'로 끝나야 합니다. " +
                     "수정 가이드: 클래스명을 {리소스명}Controller로 변경하세요.");

    @ArchTest
    static final ArchRule N_003_JpaRepository_확장_인터페이스는_Repository로_끝나야_한다 =
        classes()
            .that().areAssignableTo(JpaRepository.class)
            .should().haveSimpleNameEndingWith("Repository")
            .because("[N-003] JpaRepository를 확장하는 인터페이스는 'Repository'로 끝나야 합니다. " +
                     "수정 가이드: 인터페이스명을 {엔티티명}Repository로 변경하세요.");

    // === Phase 2: N-004 ~ N-006 ===

    @ArchTest
    static final ArchRule N_004_EventHandler_클래스는_EventHandler로_끝나야_한다 =
        classes()
            .that().resideInAPackage("..service.eventHandler..")
            .should().haveSimpleNameEndingWith("EventHandler")
            .because("[N-004] eventHandler 패키지의 클래스는 'EventHandler'로 끝나야 합니다. " +
                     "수정 가이드: 클래스명을 {도메인}EventHandler로 변경하세요.");

    @ArchTest
    static final ArchRule N_005_ApplicationService는_ApplicationService로_끝나야_한다 =
        classes()
            .that().resideInAPackage("..service.application..")
            .and().areAnnotatedWith(Service.class)
            .and().haveSimpleNameNotEndingWith("AuthService")
            .should().haveSimpleNameEndingWith("ApplicationService")
            .because("[N-005] application 패키지의 @Service 클래스는 'ApplicationService'로 끝나야 합니다. " +
                     "예외: OAuth 제공자 서비스 (AppleAuthService, GoogleAuthService). " +
                     "수정 가이드: 클래스명을 {도메인}ApplicationService로 변경하세요.");

    @ArchTest
    static final ArchRule N_006_FacadeService는_FacadeService로_끝나야_한다 =
        classes()
            .that().resideInAPackage("..service.facade..")
            .and().areAnnotatedWith(Service.class)
            .should().haveSimpleNameEndingWith("FacadeService")
            .because("[N-006] facade 패키지의 @Service 클래스는 'FacadeService'로 끝나야 합니다. " +
                     "수정 가이드: 클래스명을 {도메인}{액션}FacadeService로 변경하세요.");
}
