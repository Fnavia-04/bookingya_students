package com.project.bookingya;

import io.cucumber.junit.CucumberOptions;
import net.serenitybdd.cucumber.CucumberWithSerenity;
import org.junit.runner.RunWith;

/**
 * FASE 2 — BDD
 * Runner de Cucumber que usa el motor de Serenity para generar:
 *  - Reporte HTML interactivo en target/site/serenity/
 *  - Insignia de estado (passing/failing) para el README
 *
 * Ejecución:
 *   mvn verify                    → corre tests + genera reporte
 *   mvn serenity:aggregate        → regenera solo el reporte
 */
@RunWith(CucumberWithSerenity.class)
@CucumberOptions(
    features  = "src/test/resources/features",
    glue      = {"com.project.bookingya.steps", "com.project.bookingya"},
    plugin    = {
        "pretty",
        "html:target/cucumber-reports/cucumber.html",
        "json:target/cucumber-reports/cucumber.json"
    },
    tags = "not @wip and not @integration"
)
public class CucumberRunnerIT {
    // Clase vacía — solo actúa como punto de entrada para el runner
}
