package com.agrotech; // (Asegúrate que sea tu paquete)

// --- IMPORTACIONES AÑADIDAS ---
import org.apache.camel.Header;
// --- FIN DE IMPORTACIONES ---

/**
 * Clase Java que simula el backend de AgroAnalyzer
 * para el patrón RPC. (VERSIÓN LIMPIA)
 */
public class ServicioAnalitica {

    /**
     * Método que será llamado por la ruta RPC del servidor.
     */
    public String getUltimoValor(@Header("id_sensor") String id) {

        String jsonRespuesta = String.format(
                "{\"id\":\"%s\",\"humedad\":48,\"temperatura\":26.7,\"fecha\":\"2025-0F-22T10:30:00Z\"}",
                id
        );

        return jsonRespuesta;
    }
}