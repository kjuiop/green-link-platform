package com.greenlink.admin.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.greenlink.admin", importOptions = ImportOption.DoNotIncludeTests.class)
class AdminApiArchitectureTest {

    @ArchTest
    static final ArchRule 입구_어댑터는_출구_어댑터를_참조하지_않는다 =
            noClasses().that().resideInAPackage("..admin.adapter.in..")
                    .should().dependOnClassesThat().resideInAPackage("..admin.adapter.out..");

    @ArchTest
    static final ArchRule 출구_어댑터는_입구_어댑터를_참조하지_않는다 =
            noClasses().that().resideInAPackage("..admin.adapter.out..")
                    .should().dependOnClassesThat().resideInAPackage("..admin.adapter.in..");

    @ArchTest
    static final ArchRule 입구_어댑터는_JPA와_QueryDSL을_직접_참조하지_않는다 =
            noClasses().that().resideInAPackage("..admin.adapter.in..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            "jakarta.persistence..", "org.springframework.data.jpa..", "com.querydsl..");

    @ArchTest
    static final ArchRule 어댑터는_infrastructure를_참조하지_않는다 =
            noClasses().that().resideInAPackage("..admin.adapter..")
                    .should().dependOnClassesThat().resideInAPackage("..admin.infrastructure..");
}
