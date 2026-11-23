package com.greenlink.shopcore.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.greenlink.shopcore", importOptions = ImportOption.DoNotIncludeTests.class)
class ShopCoreArchitectureTest {

    @ArchTest
    static final ArchRule domain은_application을_참조하지_않는다 =
            noClasses().that().resideInAPackage("..shopcore.domain..")
                    .should().dependOnClassesThat().resideInAPackage("..shopcore.application..")
                    .allowEmptyShould(true);

    @ArchTest
    static final ArchRule domain과_application은_JPA와_웹_기술을_참조하지_않는다 =
            noClasses().that().resideInAnyPackage("..shopcore.domain..", "..shopcore.application..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            "jakarta.persistence..", "org.springframework.data.jpa..", "org.springframework.web..")
                    .allowEmptyShould(true);

    @ArchTest
    static final ArchRule shop_core는_온우림_core를_참조하지_않는다 =
            noClasses().that().resideInAPackage("com.greenlink.shopcore..")
                    .should().dependOnClassesThat().resideInAPackage("com.greenlink.core..")
                    .allowEmptyShould(true);
}
