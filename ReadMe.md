# Examen Práctico - Integración AgroTech Solutions S.A.

[cite_start]Este proyecto es la resolución del examen práctico para la integración de sistemas de la empresa AgroTech Solutions S.A.[cite: 2]. [cite_start]El objetivo es diseñar e implementar una solución funcional que conecte tres sistemas independientes (SensData, AgroAnalyzer y FieldControl) utilizando patrones clásicos de integración empresarial[cite: 9, 11].

## Stack Tecnológico

**Lenguaje:** Java 17 [cite: 23]
**Gestor de dependencias:** Apache Maven [cite: 24]
**Framework de Integración:** Apache Camel 4.x [cite: 25]
**Base de datos:** H2 (Embebida) [cite: 26]

## Patrones Aplicados

La integración se realiza implementando tres flujos principales que simulan la comunicación entre los sistemas de la empresa[cite: 11].

### 1. Transferencia de Archivos (SensData → AgroAnalyzer) [cite: 12]

* **Flujo:** Un componente `camel-file` monitorea la raíz del proyecto buscando el archivo `sensores.csv`.
* **Proceso:** Al encontrarlo, la ruta lee el archivo, utiliza `camel-csv` para convertirlo en objetos Java, y luego `camel-jackson` para serializar esos objetos a un *string* JSON.
* **Destino:** El string JSON se envía en memoria a través de un endpoint `direct:guardarEnDB` al siguiente módulo.

### 2. Base de Datos Compartida (AgroAnalyzer ↔ FieldControl) [cite: 13]

* **Flujo (Escritura):** La ruta `direct:guardarEnDB` (AgroAnalyzer) recibe el JSON, lo deserializa a una lista, y usa un `.split()` para procesar cada sensor individualmente.
* **Proceso:** Cada registro se transforma en una sentencia SQL `INSERT` y se ejecuta en la base de datos H2 usando `camel-jdbc`.
* **Flujo (Lectura):** Una ruta `timer:` (FieldControl) se dispara cada 10 segundos para consultar la base de datos con un `SELECT` y obtener la lectura más reciente.

### 3. Remote Procedure Call (RPC Simulado) (FieldControl → AgroAnalyzer) [cite: 14]

* [cite_start]**Flujo:** Se simula una llamada síncrona (bloqueante) entre dos rutas de Camel[cite: 49].
* **Cliente (`rpc-cliente`):** Una ruta `timer:` dispara un mensaje al endpoint `direct:solicitarLectura`. [cite_start]Esta ruta invoca al endpoint del servidor (`direct:rpc.obtenerUltimo`) y **espera** por una respuesta[cite: 50, 51, 55].
* **Servidor (`rpc-servidor`):** La ruta `direct:rpc.obtenerUltimo` recibe la solicitud, invoca a la clase Java `ServicioAnalitica.java` usando `camel-bean` para obtener una respuesta JSON simulada, y la retorna al cliente[cite: 57, 58, 60, 61].

## Pasos de Ejecución

Sigue estos pasos para ejecutar el proyecto:

### Prerrequisitos

* JDK 17 o superior.
* Apache Maven.
* Un IDE como IntelliJ IDEA o VS Code[cite: 26].

### Configuración

1.  Clona este repositorio.
2.  Asegúrate de que los siguientes archivos estén en las ubicaciones correctas:
    * `sensores.csv`: Debe estar en la **raíz** del proyecto (al mismo nivel que `pom.xml`).
    * `init.sql`: Debe estar en `src/main/resources/`.

### Ejecución

1.  Abre el proyecto en tu IDE (IntelliJ IDEA).
2.  Espera a que Maven descargue todas las dependencias definidas en el `pom.xml`.
3.  Localiza el archivo `src/main/java/com/agrotech/MainAgroTech.java`.
4.  Haz clic derecho sobre `MainAgroTech.java` y selecciona **"Run 'MainAgroTech.main()'"**.
5.  Observa la consola. Verás los logs de los 3 patrones ejecutándose:
    * `[SensData]` leyendo el archivo.
    * `[AgroAnalyzer]` guardando en la DB.
    * `[FieldControl-DB]` leyendo los datos de la DB.
    * `[CLIENTE]` y `[SERVIDOR]` ejecutando el RPC.
