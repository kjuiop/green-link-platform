package com.greenlink.shop.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.greenlink.shop", importOptions = ImportOption.DoNotIncludeTests.class)
class ShopApiArchitectureTest {

    @ArchTest
    static final ArchRule 입구_어댑터는_출구_어댑터를_참조하지_않는다 =
            noClasses().that().resideInAPackage("..shop.adapter.in..")
                    .should().dependOnClassesThat().resideInAPackage("..shop.adapter.out..")
                    .allowEmptyShould(true);

    @ArchTest
    static final ArchRule 출구_어댑터는_입구_어댑터를_참조하지_않는다 =
            noClasses().that().resideInAPackage("..shop.adapter.out..")
                    .should().dependOnClassesThat().resideInAPackage("..shop.adapter.in..")
                    .allowEmptyShould(true);

    @ArchTest
    static final ArchRule 입구_어댑터는_JPA를_직접_참조하지_않는다 =
            noClasses().that().resideInAPackage("..shop.adapter.in..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            "jakarta.persistence..", "org.springframework.data.jpa..")
                    .allowEmptyShould(true);

    @ArchTest
    static final ArchRule 어댑터는_infrastructure를_참조하지_않는다 =
            noClasses().that().resideInAPackage("..shop.adapter..")
                    .should().dependOnClassesThat().resideInAPackage("..shop.infrastructure..")
                    .allowEmptyShould(true);

    @ArchTest
    static final ArchRule shop_api는_온우림_core를_참조하지_않는다 =
            noClasses().that().resideInAPackage("com.greenlink.shop..")
                    .should().dependOnClassesThat().resideInAPackage("com.greenlink.core..")
                    .allowEmptyShould(true);
}
