package com.greenlink.core.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.greenlink.core", importOptions = ImportOption.DoNotIncludeTests.class)
class CoreArchitectureTest {

    @ArchTest
    static final ArchRule domain은_application을_참조하지_않는다 =
            noClasses().that().resideInAPackage("..core.domain..")
                    .should().dependOnClassesThat().resideInAPackage("..core.application..");

    @ArchTest
    static final ArchRule domain은_JPA와_웹_기술을_참조하지_않는다 =
            noClasses().that().resideInAPackage("..core.domain..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            "jakarta.persistence..", "org.springframework.data.jpa..", "org.springframework.web..",
                            "com.querydsl..", "software.amazon..");

    @ArchTest
    static final ArchRule application은_JPA와_웹_기술을_참조하지_않는다 =
            noClasses().that().resideInAPackage("..core.application..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            "jakarta.persistence..", "org.springframework.data.jpa..", "org.springframework.web..",
                            "com.querydsl..", "software.amazon..");
}
