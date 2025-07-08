# Documentación Técnica - Práctica 4

## Cambios introducidos en la aplicación (Versión 1.3.0)

En la versión 1.3.0 de la aplicación ToDoList se han realizado los siguientes cambios:

- Actualización de la página Acerca de:
    - Se ha modificado el texto de la versión, pasando de "1.3.0-SNAPSHOT" a "1.3.0".
    - Se ha añadido la fecha de publicación.
- Actualización del archivo `pom.xml`:
    - Se ha cambiado el número de versión a `1.3.0`.
- Generación del nuevo esquema de base de datos PostgreSQL para la versión 1.3.0.
- Creación de un script de migración para actualizar la base de datos de producción de la versión 1.2.0 a la 1.3.0.
- Se han realizado pruebas en entorno de producción para validar la migración y asegurar el correcto funcionamiento de la aplicación con los datos existentes.

---

## Detalles del despliegue de producción

### Contenedores Docker

La aplicación ToDoList en producción se despliega usando Docker, mediante dos contenedores:

- Contenedor de base de datos PostgreSQL.
- Contenedor de la aplicación Spring Boot.

Ambos contenedores se encuentran conectados en la misma red de Docker para permitir la comunicación entre ellos.

---

### Perfil de producción

La aplicación utiliza un perfil específico de Spring Boot denominado `postgres-prod`, en el cual se establece la siguiente propiedad:
spring.jpa.hibernate.ddl-auto=validate

Con esta configuración, la aplicación únicamente valida que el esquema de la base de datos coincida con las entidades JPA definidas en el código, sin realizar ninguna modificación automática sobre la base de datos. Esto permite proteger la base de datos de producción frente a cambios accidentales.

### Esquemas de datos

#### Esquema versión 1.2.0

El esquema de la base de datos correspondiente a la versión 1.2.0 se encuentra guardado en: sql/schema-1.2.0.sql

Este archivo contiene únicamente las instrucciones de creación de las tablas y estructuras de la base de datos hasta la versión 1.2.0.

---

#### Esquema versión 1.3.0

El esquema actualizado de la base de datos para la versión 1.3.0 está guardado en: sql/schema-1.3.0.sql

Incluye las nuevas estructuras y cambios realizados en la base de datos en esta versión.

---

### Script de migración

Para actualizar la base de datos de producción de la versión 1.2.0 a la 1.3.0, se ha creado el siguiente script: sql/schema-1.2.0-1.3.0.sql
Este script contiene únicamente las instrucciones necesarias para transformar el esquema anterior al nuevo, garantizando la conservación de los datos existentes.

### Proceso de Migración 
Para realizar la migración en producción se siguen los siguientes pasos:
- Arrancar la base de datos PostgreSQL vacía en un contenedor.
- Restaurar la copia de seguridad existente correspondiente a la versión 1.2.0.
- Ejecutar el script sql/schema-1.2.0-1.3.0.sql para aplicar los cambios de esquema.
- Lanzar la aplicación ToDoList con el perfil postgres-prod.
- Verificar que los datos previos se mantienen y que la aplicación funciona correctamente.
- Realizar una nueva copia de seguridad de la base de datos tras la migración. Esta copia se guarda en el directorio src/ con la fecha de creación, por ejemplo: src/backup-2025-07-06.sql
- Hacer commit de la nueva copia de seguridad en el repositorio.

## Imágen Docker de la aplicación Spring Boot
La imagen Docker generada para la versión 1.3.0 se encuentra publicada en Docker Hub y se puede acceder mediante la siguiente URL:

https://hub.docker.com/repository/docker/jeremyyugsi5/epn-todolist-equipo4/general

## Imágen Docker para la Base de datos Postgres
https://hub.docker.com/repository/docker/jeremyyugsi5/epn-todolist-db/general
