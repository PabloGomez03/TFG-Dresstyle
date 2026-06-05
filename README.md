# Dresstyle

Dresstyle es una plataforma construida bajo una **arquitectura de microservicios**. Este proyecto forma parte de un Trabajo de Fin de Grado (TFG).

## 🏗️ Arquitectura

El ecosistema está compuesto por varios microservicios desarrollados con **Spring Boot** (Java 21) en el backend y una aplicación **Vue 3** en el frontend. Todo el entorno está contenerizado y orquestado mediante **Docker Compose**.

### Servicios Core (Infraestructura)
- **MongoDB**: Base de datos NoSQL. Cada microservicio que requiere persistencia se conecta a su propia base de datos lógica.
- **RabbitMQ**: *Message Broker* utilizado para la comunicación asíncrona y la arquitectura orientada a eventos entre microservicios (por ejemplo, para procesar notificaciones).
- **Discovery Server (Eureka)**: Servidor de registro y descubrimiento. Permite que los microservicios se encuentren entre sí dinámicamente (`Puerto 8761`).
- **API Gateway**: Puerta de enlace centralizada que enruta las peticiones de los clientes (Frontend) hacia los microservicios correspondientes (`Puerto 8080`).

### Microservicios de Negocio (Backend)
- **Auth Service** (`:8081`): Se encarga de la gestión de usuarios, registro y emisión de tokens JWT para la autenticación y autorización.
- **Catalog Service** (`:8082`): Gestión del catálogo de productos, prendas e inventario.
- **Notification Service** (`:8084`): Escucha eventos a través de RabbitMQ y gestiona el envío de correos electrónicos a los usuarios.
- **Order Service** (`:8085`): Gestión de los pedidos realizados por los usuarios.
- **Subscription Service** (`:8087`): Gestión de planes de suscripción de los clientes.

### Cliente (Frontend)
- **Web Frontend** (`:80`): Aplicación Single Page Application (SPA) desarrollada en Vue.js que consume la API a través del API Gateway.

---

## 🚀 Requisitos Previos

Para ejecutar el proyecto de forma local, es necesario contar con:
- [Docker Desktop](https://www.docker.com/products/docker-desktop/) (o Docker Engine + Docker Compose en Linux)
- Git

*Nota: No es estrictamente necesario tener Java, Maven o Node.js instalados en la máquina host, ya que el código se compila dentro de contenedores usando Docker multi-stage builds.*

---

## ⚙️ Configuración y Ejecución

1. **Clonar el repositorio:**
   ```bash
   git clone <URL_DEL_REPOSITORIO>
   cd TFG-Dresstyle
   ```

2. **Variables de entorno:**
   Copia el archivo de ejemplo para crear tu propio archivo `.env`:
   ```bash
   cp .env.example .env
   ```
   Abre el archivo `.env` y rellena/ajusta las variables necesarias (por ejemplo, las credenciales SMTP para que el *Notification Service* pueda enviar emails).

3. **Desplegar la aplicación:**
   Ejecuta el siguiente comando en la raíz del proyecto para construir las imágenes y levantar todos los contenedores en segundo plano:
   ```bash
   docker compose up -d --build
   ```
   *Atención: La primera vez que se ejecute, Docker descargará las imágenes de Maven, Node y JRE, y compilará cada uno de los microservicios. Este proceso puede tardar un poco.*

4. **Acceso a los servicios:**
   Una vez que todos los contenedores estén en estado *Running/Healthy*, puedes acceder a los siguientes paneles:
   - **Aplicación Web (Vue)**: [http://localhost](http://localhost)
   - **Eureka Dashboard**: [http://localhost:8761](http://localhost:8761)
   - **RabbitMQ Management**: [http://localhost:15672](http://localhost:15672) *(Usuario: `admin` / Contraseña: `admin123`)*
   - **API Gateway**: [http://localhost:8080](http://localhost:8080)

---

## 🛑 Detener la Aplicación

Para detener los contenedores sin borrar los datos (volúmenes):
```bash
docker compose stop
```

Para destruir los contenedores y la red de Docker (los datos de MongoDB se conservarán en el volumen `mongo-data`):
```bash
docker compose down
```

Para hacer un borrado total (incluyendo la base de datos):
```bash
docker compose down -v
```

---

## 📂 Estructura del Proyecto

```text
TFG-Dresstyle/
├── docker-compose.yml       # Orquestación de contenedores
├── .env                     # Variables de entorno secretas (no se sube a Git)
├── services/                # Código fuente del backend
│   ├── apigateway/          # Spring Cloud Gateway
│   ├── authservice/         # Microservicio de Autenticación
│   ├── catalogservice/      # Microservicio de Catálogo
│   ├── discoveryserver/     # Eureka Server
│   ├── notificationservice/ # Microservicio de Notificaciones
│   ├── orderservice/        # Microservicio de Pedidos
│   └── subscriptionservice/ # Microservicio de Suscripciones
└── vue-project/             # Código fuente del frontend (Vue 3)
```
