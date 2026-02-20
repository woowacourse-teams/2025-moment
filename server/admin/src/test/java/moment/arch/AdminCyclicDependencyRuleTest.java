package moment.arch;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

@AnalyzeClasses(
    packages = "moment",
    importOptions = ImportOption.DoNotIncludeTests.class
)
class AdminCyclicDependencyRuleTest extends AdminBaseArchTest {

    @ArchTest
    static final ArchRule AC_001_Admin_패키지_간_순환_의존이_없어야_한다 =
        slices()
            .matching("moment.admin.(**)")
            .should().beFreeOfCycles()
            .because("[AC-001] Admin 모듈 내 패키지 간 순환 의존이 감지되었습니다. " +
                     "수정 가이드: 의존성 방향을 단방향으로 정리하세요. " +
                     "presentation -> service -> domain/dto 방향을 유지하세요.");
}
