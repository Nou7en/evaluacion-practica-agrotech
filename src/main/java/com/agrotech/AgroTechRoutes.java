package com.agrotech; // (Asegúrate que sea tu paquete)

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;

/**
 * Define todas las rutas de Apache Camel para el caso AgroTech.
 * (VERSIÓN FINAL + Creación de archivo JSON)
 */
public class AgroTechRoutes extends RouteBuilder {

    @Override
    public void configure() {


        // PATRÓN 1: File Transfer (SensData -> AgroAnalyzer)

        from("file:./?fileName=sensores.csv&noop=true&initialDelay=1000")
                .routeId("file-transfer")
                .log("[SensData] Leyendo archivo ${file:name}")
                .unmarshal().csv()
                .log("[SensData] Datos CSV deserializados: ${body}")
                .marshal().json(JsonLibrary.Jackson)
                .log("[SensData] Datos convertidos a JSON")

                // --- INICIO DE LA MODIFICACIÓN ---
                // Usamos multicast para enviar el JSON a dos lugares al mismo tiempo:
                // 1. Al archivo físico 'sensores.json'
                // 2. A la ruta de la base de datos 'direct:guardarEnDB'
                .multicast()
                .to("file:./?fileName=sensores.json") // <-- ¡NUEVA LÍNEA!
                .to("direct:guardarEnDB") // <-- La ruta de antes
                .end(); // Fin del multicast



        // PATRÓN 2: Shared Database (AgroAnalyzer -> DB)


        from("direct:guardarEnDB")
                .routeId("db-writer")
                .log("[AgroAnalyzer] Recibiendo datos para guardar en DB")
                .unmarshal().json(JsonLibrary.Jackson, java.util.List.class)
                .split(body())
                .filter(simple("${exchangeProperty.CamelSplitIndex} > 0"))
                .process(exchange -> {
                    @SuppressWarnings("unchecked")
                    java.util.List<String> row = exchange.getIn().getBody(java.util.List.class);

                    String id = row.get(0);
                    String fecha = row.get(1);
                    double humedad = Double.parseDouble(row.get(2));
                    double temp = Double.parseDouble(row.get(3));

                    String sql = String.format(
                            "INSERT INTO lecturas (id_sensor, fecha, humedad, temperatura) VALUES ('%s', '%s', %f, %f)",
                            id, fecha, humedad, temp
                    );

                    exchange.getIn().setBody(sql);
                })
                .to("jdbc:dataSource")
                .log("[AgroAnalyzer] Lectura de sensor guardada en DB");

        // Simulación de FieldControl leyendo la DB
        from("timer:db-reader?delay=5s&period=10s")
                .routeId("db-reader")
                .setBody(constant("SELECT * FROM lecturas ORDER BY fecha DESC LIMIT 1"))
                .to("jdbc:dataSource")
                .log("[FieldControl-DB] Lectura más reciente de la DB: ${body}");


        // PATRÓN 3: RPC Simulado (FieldControl -> AgroAnalyzer)


        from("direct:rpc.obtenerUltimo")
                .routeId("rpc-servidor")
                .log("[SERVIDOR] Solicitud recibida para sensor ${header.id_sensor}")
                .bean(ServicioAnalitica.class, "getUltimoValor");


        from("direct:solicitarLectura")
                .routeId("rpc-cliente")
                .setHeader("id_sensor", simple("${body}"))
                .log("[CLIENTE] Solicitando lectura del sensor ${header.id_sensor}")
                .toD("direct:rpc.obtenerUltimo?timeout=2000")
                .log("[CLIENTE] Respuesta recibida: ${body}");


        from("timer:rpc-trigger?delay=7s&repeatCount=1")
                .routeId("rpc-trigger")
                .setBody(constant("S003"))
                .to("direct:solicitarLectura");
    }
}