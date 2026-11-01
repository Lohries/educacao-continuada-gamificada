package br.edu.gamificacao.conquista;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Regras do ADR-002: dominio e porta nao conhecem framework, JPA nem o
 * modulo Usuario; o caso de uso so conhece a porta. Apenas o adapter toca
 * a infraestrutura.
 */
class HexagonalConquistaTest {

    private final JavaClasses classes = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("br.edu.gamificacao.conquista");

    @Test
    void dominioEPortaSaoJavaPuro() {
        noClasses()
                .that().haveSimpleName("ProgressoGamificado")
                .or().resideInAPackage("..conquista.porta..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("org.springframework..", "jakarta.persistence..", "br.edu.gamificacao.usuario..")
                .check(classes);
    }

    @Test
    void casoDeUsoNaoConheceAdapterNemUsuario() {
        noClasses()
                .that().haveSimpleName("ConquistaService")
                .should().dependOnClassesThat()
                .resideInAnyPackage("..conquista.adapter..", "br.edu.gamificacao.usuario..", "jakarta.persistence..")
                .check(classes);
    }
}
