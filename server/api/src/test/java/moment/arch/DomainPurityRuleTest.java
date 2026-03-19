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
class DomainPurityRuleTest extends BaseArchTest {

    @ArchTest
    static final ArchRule DS_001_domain_패키지는_Spring_프레임워크에_의존하지_않는다 =
        noClasses()
            .that().resideInAPackage("..domain..")
            .and().doNotHaveSimpleName("BaseEntity")
            .and().haveSimpleNameNotEndingWith("NicknameGenerator")
            .should().dependOnClassesThat()
            .resideInAPackage("org.springframework..")
            .because("[DS-001] domain 패키지는 Spring 프레임워크에 의존하지 않아야 합니다. " +
                     "허용: JPA(jakarta.persistence), Hibernate(@SQLDelete, @SQLRestriction), " +
                     "Lombok, Java 표준 라이브러리. " +
                     "예외: BaseEntity (Spring Auditing), NicknameGenerator (전략 패턴 빈). " +
                     "수정 가이드: Spring 어노테이션(@Service, @Component, @Transactional 등)을 " +
                     "domain 패키지에서 제거하세요.");

    @ArchTest
    static final ArchRule DS_002_domain_패키지는_상위_레이어에_의존하지_않는다 =
        noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat(
                com.tngtech.archunit.base.DescribedPredicate.describe(
                    "reside in upper layers (excluding JPA converters)",
                    target -> {
                        String pkg = target.getPackageName();
                        String name = target.getSimpleName();
                        if (name.endsWith("Converter") && pkg.contains(".infrastructure")) {
                            return false;
                        }
                        return pkg.contains(".infrastructure")
                            || pkg.contains(".service.")
                            || pkg.contains(".presentation")
                            || pkg.contains(".dto.");
                    }
                )
            )
            .because("[DS-002] domain 패키지는 상위 레이어에 의존하지 않아야 합니다. " +
                     "예외: JPA @Convert 참조 (인프라 컨버터). " +
                     "수정 가이드: domain -> infrastructure/service/presentation/dto 방향의 " +
                     "의존성을 제거하세요. 필요한 경우 인터페이스를 domain에 정의하고 " +
                     "infrastructure에서 구현하세요.");
}
