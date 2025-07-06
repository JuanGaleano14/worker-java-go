# Sistema de Procesamiento de Órdenes - Worker Java y Go

**Autor:** Juan Pablo Galeano Salguero

## Descripción

Este proyecto implementa un sistema de procesamiento de órdenes distribuido que consiste en dos aplicaciones principales:

1. **API Go** - Servicio de enriquecimiento de datos que proporciona información de clientes y productos
2. **Worker Java** - Procesador reactivo de órdenes que consume mensajes de Kafka, enriquece los datos consultando la API Go, y almacena las órdenes procesadas en MongoDB

## Estructura del Proyecto

```
worker-java-go/
├── api-go/                          # API de enriquecimiento en Go
│   ├── data/                        # Datos JSON
│   │   ├── customers.json           # Información de clientes
│   │   └── products.json            # Información de productos
│   ├── handlers/                    # Controladores HTTP
│   ├── models/                      # Modelos de datos
│   ├── utils/                       # Utilidades
│   └── main.go                      # Punto de entrada
├── worker/                          # Worker de procesamiento en Java
│   ├── src/main/java/com/global/worker/
│   │   ├── application/             # Capa de aplicación
│   │   │   └── service/             # Servicios
│   │   ├── config/                  # Configuraciones
│   │   ├── domain/                  # Modelos y puertos de dominio
│   │   │   ├── model/               # Entidades
│   │   │   └── port/                # Interfaces
│   │   └── infrastructure/          # Adaptadores de infraestructura
│   │       ├── goapi/               # Cliente para API Go
│   │       ├── kafka/               # Consumer de Kafka
│   │       ├── mongo/               # Repositorio MongoDB
│   │       └── redis/               # Cache y control de reintentos
│   └── docker-compose.yml           # Servicios de infraestructura
└── README.md
```

## Tecnologías

### API Go

- **Framework:** Gin
- **Lenguaje:** Go 1.24.4
- **Almacenamiento:** Archivos JSON locales
- **Puerto:** 8080

### Worker Java

- **Framework:** Spring Boot 3.5.3 (WebFlux - Reactive)
- **Lenguaje:** Java 21
- **Base de datos:** MongoDB (Reactive)
- **Message Broker:** Apache Kafka
- **Cache:** Redis
- **Puerto:** 8181

### Infraestructura

- **MongoDB:** Puerto 27018
- **Kafka:** Puerto 9092
- **Zookeeper:** Puerto 2181
- **Redis:** Puerto 6379

## Requisitos Previos

- **Java 21** o superior
- **Go 1.24.4** o superior
- **Docker** y **Docker Compose**
- **Git**

## Instalación y Configuración

### 1. Clonar el repositorio

```bash
git clone https://github.com/JuanGaleano14/worker-java-go.git
cd worker-java-go
```

### 2. Levantar todo el sistema con Docker Compose (**recomendado**)

```bash
docker compose up -d
```

Este comando levanta todos los servicios:

- API Go (puerto 8080)
- Worker Java (puerto 8181)
- MongoDB (puerto 27018)
- Kafka (puerto 9092)
- Zookeeper (puerto 2181)
- Redis (puerto 6379)

---

### Ejecución manual de servicios (opcional)

Si se desea ejecutar los servicios de forma individual en local:

#### 1. Levantar solo la infraestructura (MongoDB, Kafka, Zookeeper, Redis):

```bash
cd worker
# Solo infraestructura
docker compose up -d mongo kafka zookeeper redis
```

#### 2. API Go localmente:

```bash
cd api-go
go mod tidy
go run main.go
```

URL API Go `http://localhost:8080`

#### 3. Worker Java localmente:

```bash
cd worker
./gradlew bootRun
```

URL worker `http://localhost:8181`

## Uso del Sistema

### Endpoints de la API Go

- **GET** `/customers/:id` - Obtiene información de un cliente
- **GET** `/products/:id` - Obtiene información de un producto

**Ejemplo:**

```bash
curl http://localhost:8080/customers/customer-001
curl http://localhost:8080/products/product-001
```

### Envío de Mensajes a Kafka

1. **Conectarse al contenedor de Kafka:**

```bash
docker exec -it kafka bash
```

2. **Crear un productor de mensajes:**

```bash
kafka-console-producer.sh --bootstrap-server localhost:9092 --topic orders
```

3. **Enviar un mensaje de orden (JSON):**

```json
{
  "orderId": "order-1",
  "customerId": "customer-001",
  "products": ["product-001", "product-002"]
}
```

### Consulta de Base de Datos MongoDB

1. **Conectarse a MongoDB:**

```bash
docker exec -it mongo mongosh
```

2. **Seleccionar la base de datos:**

```javascript
use orderdb
```

3. **Ver las colecciones disponibles:**

```javascript
show collections
```

4. **Consultar las órdenes almacenadas:**

```javascript
db.orders.find().pretty();
```

5. **Consultar una orden específica:**

```javascript
db.orders.findOne({ orderId: "order-1" });
```

6. **Contar total de órdenes:**

```javascript
db.orders.countDocuments();
```

## Flujo

1. **Recepción:** El Worker Java consume mensajes del tópico `orders` de Kafka
2. **Validación:** Verifica que el cliente esté activo consultando la API Go
3. **Enriquecimiento:** Obtiene información detallada de los productos desde la API Go
4. **Persistencia:** Almacena la orden enriquecida en MongoDB
5. **Control de Errores:** Implementa retry con backoff exponencial usando Redis
6. **Concurrencia:** Utiliza locks distribuidos en Redis para evitar procesamiento duplicado

## Características

### Sistema de Reintentos

- **Máximo de reintentos:** 5
- **Estrategia:** Backoff exponencial (2^retry segundos)
- **Control:** Redis para tracking de reintentos y locks

### Programación Reactiva

- **Framework:** Spring WebFlux
- **Patrones:** Mono y Flux para composición asíncrona

### Arquitectura Hexagonal

- **Puertos:** Interfaces que definen contratos
- **Adaptadores:** Implementaciones concretas de infraestructura
- **Dominio:** Lógica de negocio pura e independiente

## Testing

### API Go

```bash
cd api-go
go test -cover ./...
```

### Worker Java

```bash
cd worker
./gradlew test
```

### Ver logs de los contenedores

```bash
docker logs kafka
docker logs mongo
docker logs redis
```

## Datos de Ejemplo

### Clientes Disponibles

- **customer-001:** Ana (activo)
- **customer-002:** Pablo (inactivo)

### Productos Disponibles

- **product-001:** Laptop Asus ($1,200.00)
- **product-002:** Mouse Logitech ($25.50)

## Posibles errores al ejecutar

### El Worker no puede conectarse a la API Go

- Verificar que la API Go esté ejecutándose en el puerto 8080
- Revisar la configuración en `application.properties`

### Kafka no recibe mensajes

- Verificar que el tópico `orders` existe
- Confirmar que Kafka esté ejecutándose en el puerto 9092

### MongoDB no almacena las órdenes

- Verificar conexión a MongoDB en el puerto 27018
- Validar que no exista otra instancia local de MongoDB con el mismo puerto
- Revisar logs del Worker para errores de base de datos

### Cliente inactivo

- Solo los clientes con `active: true` pueden procesar órdenes
- Verificar el estado del cliente en `api-go/data/customers.json`

## Comandos

### Detener todos los servicios

```bash
cd worker
docker compose down
```

### Limpiar datos de MongoDBs

```bash
docker exec -it mongo mongosh
use orderdb
db.orders.deleteMany({})
```

### Ver tópicos de Kafka

```bash
docker exec -it kafka kafka-topics.sh --bootstrap-server localhost:9092 --list
```

---

**Desarrollado por Juan Pablo Galeano Salguero**
