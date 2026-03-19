package moment.arch;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

@AnalyzeClasses(
    packages = "moment",
    importOptions = ImportOption.DoNotIncludeTests.class
)
class AdminNamingConventionRuleTest extends AdminBaseArchTest {

    @ArchTest
    static final ArchRule AN_001_Admin_Controller는_ApiController로_끝나야_한다 =
        classes()
            .that().areAnnotatedWith(RestController.class)
            .and().resideInAPackage("..admin.presentation..")
            .should().haveSimpleNameEndingWith("ApiController")
            .because("[AN-001] Admin @RestController 클래스는 'ApiController'로 끝나야 합니다. " +
                     "수정 가이드: 클래스명을 Admin{리소스명}ApiController로 변경하세요. " +
                     "예: AdminUserApiController, AdminGroupApiController");

    @ArchTest
    static final ArchRule AN_002_Admin_Controller는_Admin으로_시작해야_한다 =
        classes()
            .that().areAnnotatedWith(RestController.class)
            .and().resideInAPackage("..admin.presentation..")
            .should().haveSimpleNameStartingWith("Admin")
            .because("[AN-002] Admin @RestController 클래스는 'Admin'으로 시작해야 합니다. " +
                     "수정 가이드: 클래스명 앞에 Admin 접두사를 추가하세요.");

    @ArchTest
    static final ArchRule AN_003_Admin_Service는_Service로_끝나야_한다 =
        classes()
            .that().areAnnotatedWith(Service.class)
            .and().resideInAPackage("..admin.service..")
            .should().haveSimpleNameEndingWith("Service")
            .because("[AN-003] Admin @Service 클래스는 'Service'로 끝나야 합니다. " +
                     "수정 가이드: 클래스명을 Admin{도메인}Service로 변경하세요.");
}
