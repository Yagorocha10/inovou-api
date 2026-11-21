package br.com.inovou.store;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "br.com.inovou.store", importOptions = ImportOption.DoNotIncludeTests.class)
class ArquiteturaTest {

    @ArchTest
    static final ArchRule dominioNaoDependeDeFrameworksNemDeOutrasCamadas = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("org.springframework..", "jakarta.persistence..", "..application..", "..adapter..", "..config..")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule applicationNaoDependeDeAdapters = noClasses()
            .that().resideInAPackage("..application..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("..adapter..", "jakarta.persistence..")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule adaptersDeEntradaNaoAcessamAdaptersDeSaida = noClasses()
            .that().resideInAPackage("..adapter.in..")
            .should().dependOnClassesThat()
            .resideInAPackage("..adapter.out..")
            .allowEmptyShould(true);
}
