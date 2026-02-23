package moment.arch;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(
    packages = "moment",
    importOptions = ImportOption.DoNotIncludeTests.class
)
class AdminLayerDependencyRuleTest extends AdminBaseArchTest {

    @ArchTest
    static final ArchRule AL_001_Admin_service는_presentation에_의존할_수_없다 =
        noClasses()
            .that().resideInAPackage("..admin.service..")
            .should().dependOnClassesThat()
            .resideInAPackage("..admin.presentation..")
            .because("[AL-001] Admin service 레이어는 presentation 레이어에 의존할 수 없습니다. " +
                     "수정 가이드: service -> presentation 방향의 의존성을 제거하세요.");

    @ArchTest
    static final ArchRule AL_002_Admin_presentation은_config와_listener에_직접_의존할_수_없다 =
        noClasses()
            .that().resideInAPackage("..admin.presentation..")
            .should().dependOnClassesThat()
            .resideInAnyPackage(
                "..admin.config..",
                "..admin.global.listener.."
            )
            .because("[AL-002] Admin presentation 레이어는 config/listener에 직접 의존할 수 없습니다. " +
                     "수정 가이드: presentation에서 불필요한 의존성을 제거하세요.");
}
