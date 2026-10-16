# Base580 backend

API Spring Boot independiente del frontend y de infraestructura. Requiere Java 24
y MongoDB. Infraestructura administra los contenedores y la red.

## Desarrollo

Con MongoDB iniciado en el host:

```bash
./mvnw spring-boot:run
```

En Windows usa mvnw.cmd. API: http://localhost:8080/api.
Salud: http://localhost:8080/actuator/health.

MONGODB_URI cambia la conexion; por defecto mongodb://localhost:27017/base580.
Infraestructura proporciona mongodb://mongo:27017/base580 dentro de Docker.
Spring Boot 4 usa la propiedad spring.mongodb.uri.

## Build y despliegue

```bash
./mvnw verify
docker build -t base580-backend:latest .
```

La imagen ejecuta las pruebas durante el build. Al hacer push a main, el workflow
actualiza este clon en la VM y llama al script de base580-infra para construir
y reemplazar solo el backend. Nginx lo alcanza por backend:8080; el navegador
utiliza /api en el origen del frontend.

Consulta el README de base580-infra para preparar la VM y los secretos SSH.
