package br.edu.gamificacao;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

/**
 * Evidencia do ADR-001 (Monolito Modular): falha se um modulo acessar
 * pacotes internos de outro ou se surgir dependencia ciclica entre modulos.
 */
class ModularidadeTest {

    private final ApplicationModules modulos = ApplicationModules.of(EducacaoContinuadaGamificadaApplication.class);

    @Test
    void modulosRespeitamAsFronteiras() {
        modulos.verify();
    }

    @Test
    void geraDocumentacaoDosModulos() {
        new Documenter(modulos)
                .writeModulesAsPlantUml()
                .writeIndividualModulesAsPlantUml()
                .writeModuleCanvases();
    }
}
