package moment.arch;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

@AnalyzeClasses(
    packages = "moment",
    importOptions = ImportOption.DoNotIncludeTests.class
)
class CyclicDependencyRuleTest extends BaseArchTest {

    // 모듈러 모놀리스에서 모듈 간 순환은 M-003에서 검증하므로,
    // 여기서는 각 도메인 내부 패키지 간 순환만 검사합니다.
    // DIP(infrastructure→service) 및 JPA(@Convert domain→infrastructure) 의존은 제외합니다.

    @ArchTest
    static final ArchRule C_001_auth_패키지_간_순환_의존이_없어야_한다 =
        intraModuleCycleRule("moment.auth.(**)", "auth");

    @ArchTest
    static final ArchRule C_001_user_패키지_간_순환_의존이_없어야_한다 =
        intraModuleCycleRule("moment.user.(**)", "user");

    @ArchTest
    static final ArchRule C_001_moment_패키지_간_순환_의존이_없어야_한다 =
        intraModuleCycleRule("moment.moment.(**)", "moment");

    @ArchTest
    static final ArchRule C_001_comment_패키지_간_순환_의존이_없어야_한다 =
        intraModuleCycleRule("moment.comment.(**)", "comment");

    @ArchTest
    static final ArchRule C_001_group_패키지_간_순환_의존이_없어야_한다 =
        intraModuleCycleRule("moment.group.(**)", "group");

    @ArchTest
    static final ArchRule C_001_like_패키지_간_순환_의존이_없어야_한다 =
        intraModuleCycleRule("moment.like.(**)", "like");

    @ArchTest
    static final ArchRule C_001_notification_패키지_간_순환_의존이_없어야_한다 =
        intraModuleCycleRule("moment.notification.(**)", "notification");

    private static ArchRule intraModuleCycleRule(String pattern, String moduleName) {
        return slices()
            .matching(pattern)
            .should().beFreeOfCycles()
            // DIP 패턴: infrastructure가 service 인터페이스를 구현하면서 생기는 순환 제외
            .ignoreDependency(resideInAPackage("..infrastructure.."), resideInAPackage("..service.."))
            // JPA @Convert: domain이 infrastructure 컨버터를 참조하면서 생기는 순환 제외
            .ignoreDependency(resideInAPackage("..domain.."), resideInAPackage("..infrastructure.."))
            .because("[C-001] " + moduleName + " 모듈 내 패키지 간 순환 의존이 감지되었습니다. " +
                     "수정 가이드: 의존성 방향을 단방향으로 정리하세요. " +
                     "참고: DIP(infrastructure→service)와 JPA @Convert 참조는 예외입니다.");
    }
}
