package com.project.bookingya.steps;

import io.cucumber.java.Before;
import io.cucumber.java.After;
import com.project.bookingya.TestServer;

/**
 * FASE 2 - Hooks de Cucumber para gestionar el ciclo de vida del servidor de pruebas
 * 
 * Before: Se ejecuta ANTES del primer escenario → inicia el servidor
 * After:  Se ejecuta DESPUÉS del último escenario → detiene el servidor
 * 
 * @author Equipo BDD - Fase 2
 */
public class TestHooks {

    private static boolean serverInitialized = false;

    /**
     * Hook Before: Inicializa el servidor de pruebas una sola vez
     */
    @Before(order = 0) // order = 0 significa que se ejecuta primero
    public void initializeTestServer() {
        if (!serverInitialized) {
            TestServer.start();
            serverInitialized = true;
        }
    }

    /**
     * Hook After: Detiene el servidor después de todos los tests
     */
    @After(order = 1000) // order alto significa que se ejecuta al final
    public void teardownTestServer() {
        // No detener aquí para mantener el servidor durante toda la ejecución
        // Descomentar si deseas detener después de cada escenario
        // TestServer.stop();
    }
}
