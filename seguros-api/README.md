# seguros-api

API Spring Boot de gestión de pólizas (individual y colectiva). Capas **controller**, **service** y **repository**, H2 en memoria, mock del CORE y seguridad por `x-api-key`.

## Requisitos

- **JDK** 17 o superior (`javac` disponible; no basta solo con JRE)
- Maven 3.9+

## Configuración

En `src/main/resources/application.properties`:

| Propiedad    | Descripción                          | Valor por defecto |
|-------------|--------------------------------------|-------------------|
| `app.api-key` | Clave obligatoria en cada request  | `123456`          |
| `app.ipc`     | IPC decimal aplicado al renovar (5 % = `0.05`) | `0.05` |
| `server.port` | Puerto HTTP                          | `8082`            |

## Ejecución

```bash
cd seguros-api
mvn spring-boot:run
```

Los datos de ejemplo se cargan desde `data.sql` al arrancar (H2 en memoria; se pierden al detener la app).

### Datos iniciales

| id | tipo        | estado  | Notas                          |
|----|-------------|---------|--------------------------------|
| 1  | INDIVIDUAL  | VIGENTE | 1 riesgo activo                |
| 2  | COLECTIVA   | VIGENTE | 2 riesgos activos              |

## Seguridad

Todas las rutas exigen el header:

```http
x-api-key: 123456
```

## Endpoints

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/polizas?tipo=&estado=` | Lista pólizas (filtros opcionales: `INDIVIDUAL`/`COLECTIVA`, `VIGENTE`/`RENOVADA`/`CANCELADA`) |
| GET | `/polizas/{id}/riesgos` | Riesgos de la póliza |
| POST | `/polizas/{id}/renovar` | Aplica IPC a canon y prima; estado `RENOVADA` |
| POST | `/polizas/{id}/cancelar` | Cancela póliza y todos sus riesgos |
| POST | `/polizas/{id}/riesgos` | Alta de riesgo (solo colectiva) |
| POST | `/riesgos/{id}/cancelar` | Cancela un riesgo |
| POST | `/core-mock/evento` | Mock CORE (registra en log) |

## Ejemplos curl

```bash
API_KEY="x-api-key: 123456"

curl -s -H "$API_KEY" "http://localhost:8082/polizas?tipo=COLECTIVA"
curl -s -H "$API_KEY" "http://localhost:8082/polizas/2/riesgos"
curl -s -X POST -H "$API_KEY" "http://localhost:8082/polizas/1/renovar"
curl -s -X POST -H "$API_KEY" "http://localhost:8082/polizas/2/cancelar"
curl -s -X POST -H "$API_KEY" -H "Content-Type: application/json" \
  -d '{"canonMensual":800000,"identificadorInmueble":"INM-103","direccion":"Calle 1 #2-3"}' \
  "http://localhost:8082/polizas/2/riesgos"
curl -s -X POST -H "$API_KEY" "http://localhost:8082/riesgos/2/cancelar"
curl -s -X POST -H "$API_KEY" -H "Content-Type: application/json" -d '{"evento":"ACTUALIZACION","polizaId":555}' "http://localhost:8082/core-mock/evento"
```

Se debe ver JSON con `"status":"OK"` en la terminal del **curl**. El log aparece en la terminal donde corre **`mvn spring-boot:run`**, no en la del curl:

`[CORE-MOCK] Intento de envio al CORE: evento=ACTUALIZACION polizaId=555`

> Si partes el comando con `\`, no dejes espacios después de la barra; si no, curl no envía el body y puede fallar en silencio con `-s`.

Tras renovar, cancelar o mutar riesgos, la misma línea `[CORE-MOCK]` debe salir en la consola de Spring Boot.

## Reglas de negocio

1. Una póliza **individual** no admite `POST /polizas/{id}/riesgos`.
2. No se puede **renovar** una póliza **cancelada**.
3. **Cancelar** una póliza pasa todos sus riesgos a `CANCELADO`.
4. Agregar riesgo valida que la póliza sea **colectiva** (y recalcula canon/prima de la póliza).

## Estructura

```
src/main/java/com/seguros/api/
├── controller/
├── service/
├── repository/
├── entity/
├── domain/
├── dto/
├── config/
└── exception/
```
