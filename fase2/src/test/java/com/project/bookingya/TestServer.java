package com.project.bookingya;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * FASE 2 - Servidor de Pruebas Embebido
 * 
 * Inicia la aplicación Spring Boot en segundo plano para que los tests de Serenity/Cucumber
 * puedan hacer llamadas REST a http://localhost:8080/api
 * 
 * @author Equipo BDD - Fase 2
 */
public class TestServer {
    
    private static ConfigurableApplicationContext applicationContext;
    private static final Object LOCK = new Object();
    private static boolean started = false;

    /**
     * Inicia la aplicación Spring Boot con perfil de H2 para tests.
     * Thread-safe: solo se inicia una vez aunque se llame múltiples veces.
     */
    public static void start() {
        synchronized (LOCK) {
            if (started) {
                System.out.println("✓ Test Server ya está corriendo en puerto 8080");
                return;
            }

            try {
                System.out.println("▶ Iniciando Test Server en puerto 8080...");
                
                // Cargar la clase BookingyaApplication dinámicamente
                Class<?> appClass = Class.forName("com.project.bookingya.BookingyaApplication");
                
                SpringApplication app = new SpringApplication(appClass);
                app.setAdditionalProfiles("h2test");
                
                applicationContext = app.run();
                
                started = true;
                System.out.println("✓ Test Server iniciado correctamente en http://localhost:8080/api");
                
                // Pequeña pausa para asegurar que el servidor está listo
                Thread.sleep(2000);
                
            } catch (Exception e) {
                System.err.println("✗ Error iniciando Test Server: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("No se pudo iniciar el Test Server", e);
            }
        }
    }

    /**
     * Detiene la aplicación Spring Boot
     */
    public static void stop() {
        synchronized (LOCK) {
            if (applicationContext != null && started) {
                System.out.println("◀ Deteniendo Test Server...");
                SpringApplication.exit(applicationContext);
                started = false;
                System.out.println("✓ Test Server detenido");
            }
        }
    }

    /**
     * Obtiene el contexto de la aplicación
     */
    public static ConfigurableApplicationContext getContext() {
        return applicationContext;
    }

    /**
     * Verifica si el servidor ya está iniciado
     */
    public static boolean isStarted() {
        return started;
    }
}

