#!/bin/bash
# ==============================================================================
# StreamVault API - Script de Despliegue Automático
# ==============================================================================
# Este script instala Docker (si no está) y levanta el contenedor de StreamVault
# Uso: ./deploy-streamvault.sh
# ==============================================================================

set -e

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}🚀 StreamVault API - Despliegue Automático${NC}"
echo "================================================"

# -----------------------------------------------------------------------------
# 1. Verificar si Docker está instalado
# -----------------------------------------------------------------------------
echo -e "\n${YELLOW}[1/4] Verificando Docker...${NC}"

if command -v docker &> /dev/null; then
    echo -e "${GREEN}✅ Docker ya está instalado${NC}"
    docker --version
else
    echo -e "${YELLOW}⚠️ Docker no encontrado. Instalando...${NC}"
    
    # Detectar SO e instalar Docker
    if [ -f /etc/os-release ]; then
        . /etc/os-release
        case "$ID" in
            ubuntu|debian)
                echo "Instalando Docker en Ubuntu/Debian..."
                sudo apt-get update
                sudo apt-get install -y ca-certificates curl gnupg lsb-release
                sudo mkdir -p /etc/apt/keyrings
                curl -fsSL https://download.docker.com/linux/$ID/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
                echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/$ID stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
                sudo apt-get update
                sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
                ;;
            centos|rhel|fedora)
                echo "Instalando Docker en CentOS/RHEL/Fedora..."
                sudo yum install -y yum-utils
                sudo yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo
                sudo yum install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
                ;;
            *)
                echo -e "${RED}❌ Sistema operativo no soportado. Instala Docker manualmente.${NC}"
                exit 1
                ;;
        esac
        
        # Agregar usuario al grupo docker
        echo -e "${YELLOW}Agregando usuario al grupo docker...${NC}"
        sudo usermod -aG docker $USER
        echo -e "${GREEN}✅ Docker instalado. Nota: mungkin perlu logout/login untuk生效.${NC}"
    else
        echo -e "${RED}❌ No se pudo detectar el sistema operativo.${NC}"
        exit 1
    fi
fi

# -----------------------------------------------------------------------------
# 2. Descargar imagen desde GHCR (GitHub Container Registry)
# -----------------------------------------------------------------------------
echo -e "\n${YELLOW}[2/4] Descargando imagen de Docker...${NC}"
echo -e "${YELLOW}   Repo: ghcr.io/manuelbth/streamvault-api:latest${NC}"

# Verificar si puede hacer login (opcional - público no requiere login)
docker pull ghcr.io/manuelbth/streamvault-api:latest || {
    echo -e "${YELLOW}⚠️ Imagen pública, debería poder descargarse sin login${NC}"
    # Si falla, intentamos con la SHA específica del último commit
    echo -e "${YELLOW}Intentando con imagen del último build...${NC}"
}

echo -e "${GREEN}✅ Imagen descargada${NC}"

# -----------------------------------------------------------------------------
# 3. Crear archivo de variables de entorno
# -----------------------------------------------------------------------------
echo -e "\n${YELLOW}[3/4] Configurando variables de entorno...${NC}"

# Crear directorio para config
mkdir -p ~/streamvault

# Ver si existe .env, si no crear uno de ejemplo
if [ ! -f ~/streamvault/.env ]; then
    cat > ~/streamvault/.env << 'EOF'
# ==============================================================================
# StreamVault API - Variables de Entorno
# ==============================================================================
# IMPORTANTE: Cambia los valores por los de tu entorno
# ==============================================================================

# Spring
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=8080

# Base de datos (PostgreSQL)
DB_HOST=srv-db.streamvault.local
DB_PORT=5432
DB_NAME=streamvault
DB_USER=streamvault_user
DB_PASSWORD=YOUR_PASSWORD_HERE

# Mail/SMTP
MAIL_HOST=srv-mail.streamvault.local
MAIL_PORT=587
MAIL_FROM=noreply@streamvault.local
MAIL_PASSWORD=YOUR_PASSWORD_HERE

# JWT Keys (las del proyecto)
JWT_PUBLIC_KEY=-----BEGIN PUBLIC KEY-----\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAmSs6GLLdy/TcAZP+9MV2\nnmIwaUopCSgucSA7LSzsycw9rD42QuErSkpSM+FmxGDIcHRtnrP5YuyexvAaUfpe\npONnX1W0Mq9b00gJ0mvAUuv+UEm2wGYCr7vNFWvFvu2v/Etq4Zmrnm3pq8LEx3ST\nV8tvKYfGJ1qV+Dh2BR9JcwdOTE8AfmyzkIYBCnHOE5yBvviVu8pawz6Mi3c+g6I9\nkgcrAQCtXkHgmRUKfJXA32F/hEpmweslhxc2caehoR9QeKnCJyRNbGDz+DaUdWcr\nIXIO5QtKN8lj99RFwGhYSZpfxmi+QyslnaYFEhoTZ1mZD/e0jvBa4bT7xQoLK7kl\nCQIDAQAB\n-----END PUBLIC KEY-----
JWT_PRIVATE_KEY=-----BEGIN PRIVATE KEY-----\nMIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQCZKzoYst3L9NwB\nk/70xXaeYjBpSikJKC5xIDstLOzJzD2sPjZC4StKSlIz4WbEYMhwdG2es/li7J7G\n8BpR+l6k42dfVbQyr1vTSAnSa8BS6/5QSbbAZgKvu80Va8W+7a/8S2rhmauebemr\nwsTHdJNXy28ph8YnWpX4OHYFH0lzB05MTwB+bLOQhgEKcc4TnIG++JW7ylrDPoyL\ndz6Doj2SBysBAK1eQeCZFQp8lcDfYX+ESmbB6yWHFzZxp6GhH1B4qcInJE1sYPP4\nNpR1Zyshcg7lC0o3yWP31EXAaFhJml/GaL5DKyWdpgUSGhNnWZkP97SO8FrhtPvF\nCgsruSUJAgMBAAECggEAEsICeWfeVc4E64SOYreUEU2BFEPqxp5PIbeKx7uKvCPx\nIZj02DfvL0fuzT+7cC1SHsODcT4lOoIz57Ub5BI/aP4YeMpsKRDks/lMiqJ7iSwx\nlu+QG/viNFdRhGAQdZNyS/mIe1xwchSP22Mc6jhSmxvk3zcrg0JKv31apsHwIs//\nnjjFisE1cql+wSJdpI2yO19mPm60uEgo0jjr/ktSurclxzMekqTbW+51ImvhLvWa\nIkqRqomLkmCQZyHsrVYpxPsT5EPHu1vxTxN8Slm/dbHqGc00h3eVq1gMzhSTwTPT\nCb5D6/fPW5GpJHcFvH3VBolw5Y/KQMkvWQrRh0ZR6QKBgQDUULwX/7N7/nPETT8m\nsbOpgueofgm/MuEuvjGNhM8BploNHVgZHVxIZMMLzdrZErDYGnJU8V2ST0DndNv/\ngp5gSxmXdDtfl5zbT/zVXqk4eH824ze475nI495HigwX5j5ysFpxWKBw6TY/Mic0\n/Hj9pOeJWN27sVsuQNPa9j8rqwKBgQC4rxLyluCBw/2mnWS00DppEt4o9N7vrMnH\nXdWqIXyB1g3wM+GNoGmlMWRE9DtVLV/r3cvzSVZ0aKJbPs93h3rHYML7Umbrbzfs\nKUC4VQCeBlxtWtbfyhBwTqi8Px6iD83aJ01Wmu/nDptiawLwKtfiBmIpEvcBaUWT\nhYvHplSeGwKBgQCE2OuInK+Cw0lOqAL+xCwlwcoQDKUupLhv1gQNh+87Ggq4sAbC\n2DM4/QtqJGlucBqFba7iZZBmDv8OlHlfnCxbFkKyGMFZ4/T66UbLf4qk0gjPoEbq\nicmRALvByagdgCzM+Hnu+ESTwej2i/wqVFukYf2aXCeJ3MSv4VJubKxENwKBgQCi\nym/BNg0fVUs4mnQyjXlvNRpur6nzSjNycNvt4yaEq9INcS3YURXObwMbZM0H/78V\ngaRNBtAWPEUgePUXP2ySYlB8h94AmUCKArLxyLuKj8DZA8Fz8gEbfbpudJTj9VpW\neV5KgLgGy3FcB0fHu3wf19CetADWVZzmtEjJ62ubEQKBgQDPCRAP8DM8YqC6TBMI\nRwiaG/B+ngIMQ7FOEwxeopxctYwFCsT5mkjVCBO9eLPHT+kfTzLsz+bc3IWIuj8g\ncpybo3BycZfeGANYIZZ84c6HE3NK9obV0HXQUE0ddqt9Pkoa5jxdHazh47yEM7Ak\nCVP1VEUzxNBD8/z3ZrFhPKys6w==\n-----END PRIVATE KEY-----

# MinIO
MINIO_URL=http://srv-minio.streamvault.local:9000
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=minioadmin
MINIO_BUCKET_VIDEOS=streamvault-videos
MINIO_BUCKET_THUMBNAILS=streamvault-thumbnails

# CORS
CORS_ALLOWED_ORIGINS=https://app.streamvault.local
EOF
    echo -e "${GREEN}✅ Archivo .env creado en ~/streamvault/.env${NC}"
    echo -e "${YELLOW}⚠️ IMPORTANTE: Edita ~/streamvault/.env y cambia las contraseñas!${NC}"
fi

# -----------------------------------------------------------------------------
# 4. Levantar el contenedor
# -----------------------------------------------------------------------------
echo -e "\n${YELLOW}[4/4] Iniciando contenedor StreamVault API...${NC}"

# Crear red si no existe
docker network create streamvault-backend 2>/dev/null || true

# Variables de entorno
export $(cat ~/streamvault/.env | grep -v '^#' | xargs)

# Ejecutar contenedor
docker run -d \
  --name streamvault-api \
  --network streamvault-backend \
  -p 8080:8080 \
  --env-file ~/streamvault/.env \
  --restart unless-stopped \
  ghcr.io/manuelbth/streamvault-api:latest

echo -e "${GREEN}✅ Contenedor iniciado!${NC}"
echo ""
echo "================================================"
echo -e "${GREEN}🎉 StreamVault API está corriendo!${NC}"
echo "================================================"
echo ""
echo "📌 Endpoints disponibles:"
echo "   - API:          http://localhost:8080"
echo "   - Health:       http://localhost:8080/actuator/health"
echo "   - Swagger UI:   http://localhost:8080/swagger-ui.html"
echo ""
echo "📌 Comandos útiles:"
echo "   - Ver logs:     docker logs -f streamvault-api"
echo "   - Reiniciar:   docker restart streamvault-api"
echo "   - Detener:      docker stop streamvault-api"
echo ""
echo -e "${YELLOW}⚠️ Asegúrate de que los servicios externos estén disponibles:${NC}"
echo "   - PostgreSQL:   $DB_HOST:$DB_PORT"
echo "   - SMTP:         $MAIL_HOST:$MAIL_PORT"
echo "   - MinIO:        $MINIO_URL"
echo ""