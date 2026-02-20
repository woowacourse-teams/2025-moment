package moment.arch;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import jakarta.persistence.Entity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

@AnalyzeClasses(
    packages = "moment",
    importOptions = ImportOption.DoNotIncludeTests.class
)
class PackageStructureRuleTest extends BaseArchTest {

    // === Phase 1: P-001 ~ P-004 ===

    @ArchTest
    static final ArchRule P_001_RestController_클래스는_presentation_패키지에_위치해야_한다 =
        classes()
            .that().areAnnotatedWith(RestController.class)
            .should().resideInAPackage("..presentation..")
            .because("[P-001] @RestController 클래스는 presentation 패키지에 위치해야 합니다. " +
                     "수정 가이드: 해당 클래스를 {도메인}.presentation 패키지로 이동하세요.");

    @ArchTest
    static final ArchRule P_002_Service_클래스는_service_패키지에_위치해야_한다 =
        classes()
            .that().areAnnotatedWith(Service.class)
            .should().resideInAPackage("..service..")
            .because("[P-002] @Service 클래스는 service 패키지에 위치해야 합니다. " +
                     "수정 가이드: 해당 클래스를 {도메인}.service.{하위패키지} 패키지로 이동하세요.");

    @ArchTest
    static final ArchRule P_003_Repository_인터페이스는_infrastructure_패키지에_위치해야_한다 =
        classes()
            .that().areAssignableTo(JpaRepository.class)
            .should().resideInAPackage("..infrastructure..")
            .because("[P-003] JpaRepository 확장 인터페이스는 infrastructure 패키지에 위치해야 합니다. " +
                     "수정 가이드: 해당 인터페이스를 {도메인}.infrastructure 패키지로 이동하세요.");

    @ArchTest
    static final ArchRule P_004_Entity_클래스는_domain_패키지에_위치해야_한다 =
        classes()
            .that().areAnnotatedWith(Entity.class)
            .should().resideInAPackage("..domain..")
            .because("[P-004] @Entity 클래스는 domain 패키지에 위치해야 합니다. " +
                     "수정 가이드: 해당 클래스를 {도메인}.domain 패키지로 이동하세요.");

    // === Phase 2: P-005 ~ P-006 ===

    @ArchTest
    static final ArchRule P_005_Request_DTO는_dto_request_패키지에_위치한다 =
        classes()
            .that().haveSimpleNameEndingWith("Request")
            .and().areAssignableTo(Record.class)
            .and().resideInAPackage("..dto..")
            .should().resideInAPackage("..dto.request..")
            .because("[P-005] Request DTO(record)는 dto/request 패키지에 위치해야 합니다. " +
                     "수정 가이드: 해당 클래스를 {도메인}.dto.request 패키지로 이동하세요.");

    @ArchTest
    static final ArchRule P_006_Response_DTO는_dto_response_패키지에_위치한다 =
        classes()
            .that().haveSimpleNameEndingWith("Response")
            .and().areAssignableTo(Record.class)
            .and().resideInAPackage("..dto..")
            .should().resideInAPackage("..dto.response..")
            .because("[P-006] Response DTO(record)는 dto/response 패키지에 위치해야 합니다. " +
                     "수정 가이드: 해당 클래스를 {도메인}.dto.response 패키지로 이동하세요.");
}
