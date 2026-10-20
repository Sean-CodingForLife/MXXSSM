package com.mxxssm;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "com.mxxssm", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    @ArchTest
    static final ArchRule controllersMustNotUseRepositories = noClasses()
            .that().haveSimpleNameEndingWith("Controller")
            .should().dependOnClassesThat().haveSimpleNameEndingWith("Repository");

    @ArchTest
    static final ArchRule repositoriesMustNotUseServices = noClasses()
            .that().haveSimpleNameEndingWith("Repository")
            .should().dependOnClassesThat().haveSimpleNameEndingWith("Service");

    @ArchTest
    static final ArchRule servicesMustNotUseControllers = noClasses()
            .that().haveSimpleNameEndingWith("Service")
            .should().dependOnClassesThat().haveSimpleNameEndingWith("Controller");

    @ArchTest
    static final ArchRule commonCodeMustNotUseControllers = noClasses()
            .that().resideInAPackage("com.mxxssm.common..")
            .should().dependOnClassesThat().haveSimpleNameEndingWith("Controller");
}
