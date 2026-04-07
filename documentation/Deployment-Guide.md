# Guía de Despliegue - StreamVault API

## Tabla de Contenidos

1. [Requisitos Previos](#1-requisitos-previos)
2. [Método Automático (Script)](#2-método-automático-script)
3. [Método Manual](#3-método-manual)
4. [Configuración de Variables de Entorno](#4-configuración-de-variables-de-entorno)
5. [Verificación del Despliegue](#5-verificación-del-despliegue)
6. [Comandos Útiles](#6-comandos-útiles)
7. [Solución de Problemas](#7-solución-de-problemas)

---

## 1. Requisitos Previos

Para ejecutar StreamVault API necesitas:

| Requisito | Descripción |
|-----------|-------------|
| **Sistema Operativo** | Ubuntu 20.04+, Debian 11+, CentOS 8+, Fedora 35+ |
| **Permisos** | Acceso root o usuario con permisos sudo |
| **Servicios Externos** | PostgreSQL, SMTP (Mail), MinIO |
| **Red** | Acceso a internet para descargar la imagen |

### Servicios Externos Requeridos

| Servicio | Hostname DNS | Puerto | Propósito |
|----------|-------------|--------|-----------|
| PostgreSQL | `srv-db.streamvault.local` | 5432 | Base de datos |
| SMTP/Mail | `srv-mail.streamvault.local` | 587 | Envío de emails |
| MinIO/S3 | `srv-minio.streamvault.local` | 9000 | Almacenamiento de archivos |

---

## 2. Método Automático (Script)

El script `deploy-streamvault.sh` hace todo automáticamente:

### 2.1 Descargar el Script

```bash
# Opción 1: Descargar del repositorio
curl -O https://raw.githubusercontent.com/ManuelBth/streamvault-api/main/scripts/deploy-streamvault.sh

# Opción 2: Si ya tienes el repositorio local
cp scripts/deploy-streamvault.sh ~/
```

### 2.2 Ejecutar el Script

```bash
# Dar permisos de ejecución
chmod +x deploy-streamvault.sh

# Ejecutar
./deploy-streamvault.sh
```

### 2.3 Lo que hace el Script

```
┌─────────────────────────────────────────────────────────────────────┐
│ QUÉ HACE EL SCRIPT automáticamente                                 │
└─────────────────────────────────────────────────────────────────────┘

1. Verifica si Docker está instalado
   └→ Si no está: lo instala automáticamente

2. Descarga la imagen desde GHCR
   └→ ghcr.io/manuelbth/streamvault-api:latest

3. Crea archivo ~/streamvault/.env con variables
   └→ Edita este archivo con tus contraseñas reales

4. Crea red Docker (streamvault-backend)

5. Levanta el contenedor
   └→ Puerto 8080 expuesto
   └→ Restart automático enabled

6. Muestra información de acceso
```

---

## 3. Método Manual

Si prefieres hacerlo paso a paso:

### 3.1 Instalar Docker

```bash
# Ubuntu/Debian
sudo apt-get update
sudo apt-get install -y docker.io

# CentOS/RHEL
sudo yum install -y docker

# Iniciar Docker
sudo systemctl start docker
sudo systemctl enable docker
```

### 3.2 Descargar Imagen

```bash
# Descargar la imagen desde GitHub Container Registry
docker pull ghcr.io/manuelbth/streamvault-api:latest
```

### 3.3 Crear Archivo de Variables

```bash
mkdir -p ~/streamvault
nano ~/streamvault/.env
```

### 3.4 Levantar Contenedor

```bash
# Crear red
docker network create streamvault-backend

# Ejecutar contenedor
docker run -d \
  --name streamvault-api \
  --network streamvault-backend \
  -p 8080:8080 \
  --env-file ~/streamvault/.env \
  --restart unless-stopped \
  ghcr.io/manuelbth/streamvault-api:latest
```

---

## 4. Configuración de Variables de Entorno

Edita el archivo `~/streamvault/.env` con tus valores reales:

```bash
# ==============================================================================
# Base de datos (PostgreSQL) - IMPORTANTE: Cambia la contraseña
# ==============================================================================
DB_HOST=srv-db.streamvault.local
DB_PORT=5432
DB_NAME=streamvault
DB_USER=streamvault_user
DB_PASSWORD=TU_PASSWORD_AQUI

# ==============================================================================
# Mail/SMTP - IMPORTANTE: Cambia la contraseña
# ==============================================================================
MAIL_HOST=srv-mail.streamvault.local
MAIL_PORT=587
MAIL_FROM=noreply@streamvault.local
MAIL_PASSWORD=TU_PASSWORD_AQUI

# ==============================================================================
# JWT - Claves del proyecto (NO cambiar)
# ==============================================================================
JWT_PUBLIC_KEY=-----BEGIN PUBLIC KEY-----\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAmSs6GLLdy/TcAZP+9MV2\n...
JWT_PRIVATE_KEY=-----BEGIN PRIVATE KEY-----\nMIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQCZKzoYst3L9NwB\n...

# ==============================================================================
# MinIO - Ajusta según tu configuración
# ==============================================================================
MINIO_URL=http://srv-minio.streamvault.local:9000
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=minioadmin
MINIO_BUCKET_VIDEOS=streamvault-videos
MINIO_BUCKET_THUMBNAILS=streamvault-thumbnails

# ==============================================================================
# CORS - Dominio del frontend
# ==============================================================================
CORS_ALLOWED_ORIGINS=https://app.streamvault.local
```

---

## 5. Verificación del Despliegue

### 5.1 Verificar que el Contenedor está Corriendo

```bash
docker ps
```

Deberías ver:
```
CONTAINER ID   IMAGE                               STATUS
abc123456789   ghcr.io/manuelbth/streamvault-api   Up 2 minutes
```

### 5.2 Verificar Salud de la API

```bash
curl http://localhost:8080/actuator/health
```

Respuesta esperada:
```json
{
  "status": "UP",
  "components": {
    "db": { "status": "UP" },
    "mail": { "status": "UP" }
  }
}
```

### 5.3 Ver Logs

```bash
# Ver logs en tiempo real
docker logs -f streamvault-api

# Ver últimos 50 líneas
docker logs --tail 50 streamvault-api
```

---

## 6. Comandos Útiles

| Comando | Descripción |
|---------|-------------|
| `docker ps` | Ver contenedores en ejecución |
| `docker logs -f streamvault-api` | Ver logs en tiempo real |
| `docker restart streamvault-api` | Reiniciar el contenedor |
| `docker stop streamvault-api` | Detener el contenedor |
| `docker start streamvault-api` | Iniciar el contenedor |
| `docker exec -it streamvault-api /bin/sh` | Entrar al contenedor |
| `docker inspect streamvault-api` | Ver detalles del contenedor |
| `docker system prune` | Limpiar recursos no usados |

---

## 7. Solución de Problemas

### 7.1 El Contenedor no Inicia

```bash
# Ver logs de errores
docker logs streamvault-api

# Verificar variables de entorno
docker exec streamvault-api env
```

**Problema común**: Servicios externos no disponibles.
- Verifica que PostgreSQL esté corriendo
- Verifica que MinIO esté corriendo
- Verifica la configuración de red/DNS

### 7.2 Error de Conexión a Base de Datos

```
Unable to open JDBC Connection
```

**Solución**: Verifica que `DB_HOST` sea correcto y que el servidor PostgreSQL esté accesible.

### 7.3 Error de Permisos de Docker

```
permission denied while trying to connect to the Docker daemon
```

**Solución**:
```bash
# Agregar usuario al grupo docker
sudo usermod -aG docker $USER

# O usar sudo
sudo docker ps
```

### 7.4 Verificar Salud de los Servicios Externos

```bash
# Test PostgreSQL
nc -zv srv-db.streamvault.local 5432

# Test SMTP
nc -zv srv-mail.streamvault.local 587

# Test MinIO
curl -I http://srv-minio.streamvault.local:9000/minio/health/live
```

---

## 📋 Checklist Post-Instalación

- [ ] Docker instalado y corriendo
- [ ] Imagen descargada correctamente
- [ ] Archivo .env configurado con contraseñas reales
- [ ] Contenedor iniciado y corriendo
- [ ] Health endpoint respondiendo
- [ ] Logs sin errores críticos

---

## 📞 Soporte

Si tienes problemas:

1. Revisa los logs: `docker logs streamvault-api`
2. Verifica la configuración: `docker exec streamvault-api env`
3. Revisa la documentación completa en: `documentation/Docker-StreamVault.md`

---

*Documento creado: Abril 2026*
*Versión: 1.0*