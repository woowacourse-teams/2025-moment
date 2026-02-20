package moment.arch;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.beans.factory.annotation.Autowired;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;

@AnalyzeClasses(
    packages = "moment",
    importOptions = ImportOption.DoNotIncludeTests.class
)
class AdminAnnotationRuleTest extends AdminBaseArchTest {

    @ArchTest
    static final ArchRule AA_001_Admin에서_Autowired_필드_주입은_금지한다 =
        noFields()
            .that().areDeclaredInClassesThat()
            .resideInAPackage("..admin..")
            .should().beAnnotatedWith(Autowired.class)
            .because("[AA-001] Admin 모듈에서 @Autowired 필드 주입은 금지됩니다. " +
                     "수정 가이드: @RequiredArgsConstructor + private final 필드로 " +
                     "생성자 주입을 사용하세요.");

    // AA-002 제거: Lombok @RequiredArgsConstructor는 @Retention(SOURCE)이므로
    // 바이트코드에 남지 않아 ArchUnit으로 검증 불가. AA-001(no @Autowired)로 대체 커버됨.
}
