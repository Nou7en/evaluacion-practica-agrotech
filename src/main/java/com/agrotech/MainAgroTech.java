package com.agrotech; // (Asegúrate que sea tu paquete)

// --- IMPORTACIONES AÑADIDAS ---
import org.apache.camel.main.Main;
import org.h2.jdbcx.JdbcDataSource;
// --- FIN DE IMPORTACIONES ---

/**
 * Clase principal que inicia la aplicación Apache Camel
 * y configura los servicios (como la Base de Datos).
 * (VERSIÓN LIMPIA)
 */
public class MainAgroTech {

    public static void main(String[] args) throws Exception {
        // 1. Crear el lanzador principal de Camel
        Main main = new Main();

        // 2. Configurar la Base de Datos H2
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:./database/agrotechdb;INIT=RUNSCRIPT FROM 'classpath:init.sql'");
        dataSource.setUser("sa");
        dataSource.setPassword("");

        // 3. Registrar la DB en Camel
        main.bind("dataSource", dataSource);

        // 4. Añadir nuestras rutas
        main.configure().addRoutesBuilder(new AgroTechRoutes());

        // 5. Ejecutar la aplicación
        System.out.println("Iniciando integración AgroTech... Presiona Ctrl+C para salir.");
        main.run();
    }
}