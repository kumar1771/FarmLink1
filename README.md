# UrbanFleet 🌾

A microservices-based platform that connects **Farmers** directly with **Buyers**. Farmers can list products and manage stock; buyers can browse, place orders, and track them. Built with Spring Boot 3, Spring Cloud, and Spring AI.

---

## Architecture

```
                        ┌─────────────────┐
                        │  Config Server  │  :8888
                        │  (GitHub Repo)  │
                        └────────┬────────┘
                                 │
                        ┌────────▼────────┐
                        │  Eureka Server  │  :8761
                        │ (Service Disco) │  Basic Auth Protected
                        └────────┬────────┘
                                 │
                        ┌────────▼────────┐
                        │   API Gateway   │  :9090  ← Only public port
                        │ JWT + API Key   │
                        │ Rate Limiting   │
                        │ Circuit Breaker │
                        └────────┬────────┘
                                 │
        ┌──────────┬─────────────┼─────────────┬──────────────┐
        │          │             │             │              │
   ┌────▼───┐ ┌────▼────┐ ┌─────▼──────┐ ┌───▼─────┐ ┌──────▼───────┐
   │  Auth  │ │ Farmer  │ │   Farmer   │ │  Buyer  │ │    Order     │
   │Service │ │Service  │ │   Goods    │ │Service  │ │   Service    │
   │ :8081  │ │ :7070   │ │  Service   │ │ :7072   │ │   :8080      │
   └────────┘ └─────────┘ │  :7071     │ └─────────┘ └──────────────┘
                           └────────────┘
                                 │
                    ┌────────────┴────────────┐
                    │                         │
             ┌──────▼──────┐         ┌────────▼──────┐
             │   Payment   │         │   AI Service  │
             │   Service   │         │    :8085      │
             │   :8090     │         └───────────────┘
             └─────────────┘

   Infrastructure:  MySQL :3306 │ Redis :6379 │ Kafka :9092 │ Zookeeper :2181
```

---

## Services

| Service | Port | Description |
|---|---|---|
| Config Server | 8888 | Centralized config pulled from GitHub |
| Eureka Server | 8761 | Service discovery — protected with Basic Auth |
| API Gateway | 9090 | Single entry point — JWT, API Key, Rate Limiting |
| Auth Service | 8081 | User registration, login, JWT token management |
| Farmer Service | 7070 | Farmer profile management |
| Farmer Goods Service | 7071 | Product listing and stock management |
| Buyer Service | 7072 | Buyer profile management |
| Order Service | 8080 | Order placement, tracking, status transitions |
| Payment Service | 8090 | Payment processing (stub) |
| AI Service | 8085 | OpenAI-powered chat assistant |

---

## Tech Stack

| Category | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3 |
| API Gateway | Spring Cloud Gateway |
| Service Discovery | Spring Cloud Netflix Eureka |
| Config Management | Spring Cloud Config |
| Inter-service Calls | Spring Cloud OpenFeign |
| Authentication | Spring Security + JWT (jjwt) |
| Persistence | Spring Data JPA + MySQL 8 |
| Caching / Rate Limiting | Redis 7 |
| Messaging | Apache Kafka |
| Resilience | Resilience4j Circuit Breaker |
| Database Migrations | Flyway |
| AI Integration | Spring AI + OpenAI |
| Containerization | Docker + Docker Compose |
| CI/CD | GitHub Actions |

---

## Key Features

- **JWT Authentication** — tokens expire after 10 hours, validated at the gateway
- **API Key Guard** — all requests require `X-Api-Key` header
- **Rate Limiting** — Redis-backed: 5 req/s on auth endpoints, 2 req/s on AI
- **Circuit Breaker** — Resilience4j on Order → Farmer Goods Feign calls
- **Optimistic Locking** — `@Version` on Order entity prevents race conditions
- **Pagination** — all list endpoints support `page` and `size` query params
- **Flyway Migrations** — schema versioned, `ddl-auto=validate` in all services
- **Security Headers** — X-Content-Type-Options, X-Frame-Options, X-XSS-Protection on all responses
- **Non-root Containers** — all Dockerfiles run as unprivileged `appuser`
- **Eureka Basic Auth** — service registry protected, credentials via environment variables

---

## Prerequisites

- Java 17+
- Maven 3.8+
- Docker + Docker Compose

---

## Quick Start (Docker)

**1. Copy and fill in the environment file:**

```bash
cp .env.example .env
```

Edit `.env` with your values (see Environment Variables section below).

**2. Start everything:**

```bash
docker compose up --build
```

**3. API is available at:** `http://localhost:9090`

> Services start in the correct order automatically via `depends_on` healthchecks.

---

## Running Locally (Without Docker)

**1. Start infrastructure first** (MySQL, Redis, Kafka must be running):

```bash
# Only start infrastructure containers, not the app services
docker compose up mysql redis zookeeper kafka
```

**2. Set environment variables** in your IDE run configurations (or export in terminal):

```bash
DB_USERNAME=root
DB_PASSWORD=your_mysql_password
JWT_SECRET=your_secret_key_minimum_32_characters_long
apikey=your_api_key_value
OPENAI_API_KEY=sk-...
REDIS_PASSWORD=your_redis_password
EUREKA_USERNAME=eureka
EUREKA_PASSWORD=your_eureka_password
```

> In IntelliJ: Run → Edit Configurations → Environment Variables for each service.

**3. Start services in this exact order**:

1. **Config Server** `:8888`
2. **Eureka Server** `:8761`
3. **Auth Service** `:8081`
4. **Farmer Service** `:7070`
5. **Farmer Goods Service** `:7071`
6. **Buyer Service** `:7072`
7. **Order Service** `:8080`
8. **Payment Service** `:8090`
9. **AI Service** `:8085`
10. **API Gateway** `:9090` — must start last

**Services that need DB variables:** Auth, Farmer, Farmer-Goods, Buyer, Order

**Services that need all variables:** API Gateway

**Services that need only Eureka variables:** Payment, AI

---

## Database Setup

All services share one MySQL database. Create it before running locally:

```sql
CREATE DATABASE AuthuService;
```

Tables are created automatically by Flyway migrations on first run.

---

## Environment Variables

Create a `.env` file in the project root:

```env
DB_USERNAME=root
DB_PASSWORD=your_mysql_password

JWT_SECRET=your_secret_key_minimum_32_characters_long
apikey=your_api_key_value

OPENAI_API_KEY=sk-...

REDIS_PASSWORD=your_redis_password

EUREKA_USERNAME=eureka
EUREKA_PASSWORD=your_eureka_password

CORS_ALLOWED_ORIGINS=http://localhost:3000
```

| Variable | Used By | Description |
|---|---|---|
| `DB_USERNAME` | Auth, Farmer, FarmerGoods, Buyer, Order | MySQL username |
| `DB_PASSWORD` | Auth, Farmer, FarmerGoods, Buyer, Order | MySQL password |
| `JWT_SECRET` | Auth, API Gateway | JWT signing key (min 32 chars) |
| `apikey` | API Gateway | API key for all requests |
| `OPENAI_API_KEY` | AI Service | OpenAI API key |
| `REDIS_PASSWORD` | API Gateway, Redis | Redis auth password |
| `EUREKA_USERNAME` | All services, Eureka | Eureka basic auth username |
| `EUREKA_PASSWORD` | All services, Eureka | Eureka basic auth password |
| `CORS_ALLOWED_ORIGINS` | API Gateway | Allowed frontend origins |

---

## API Endpoints

All requests go through the **API Gateway at `http://localhost:9090`**.

### Required Headers (all protected endpoints)

| Header | Value |
|---|---|
| `Authorization` | `Bearer <jwt_token>` |
| `X-Api-Key` | `<your_api_key>` |

---

### Auth — `/Users` (Public, API Key required)

| Method | Endpoint | Description |
|---|---|---|
| POST | `/Users/signup` | Register a new user |
| POST | `/Users/login` | Login and receive JWT |

**Signup:**
```json
{
  "username": "john",
  "email": "john@example.com",
  "password": "password123",
  "role": "FARMER",
  "phone": "1234567890"
}
```

**Login:**
```json
{
  "email": "john@example.com",
  "password": "password123"
}
```

**Login response:**
```json
{
  "accessToken": "<jwt_token>",
  "role": "FARMER",
  "name": "john",
  "status": "Success"
}
```

---

### Farmer Profile — `/farmer` (Role: `FARMER`)

| Method | Endpoint | Description |
|---|---|---|
| POST | `/farmer/profile` | Create farmer profile |
| GET | `/farmer/profile/me` | Get my profile |
| PUT | `/farmer/profile/me` | Update my profile |
| DELETE | `/farmer/profile/me` | Delete my profile |

---

### Products — `/products`

| Method | Endpoint | Role | Description |
|---|---|---|---|
| POST | `/products` | FARMER | Add a new product |
| GET | `/products?page=0&size=20` | FARMER, BUYER | Get all products (paginated) |
| GET | `/products/my` | FARMER | Get my products |
| GET | `/products/{id}` | FARMER, BUYER | Get product by ID |
| PUT | `/products/{id}` | FARMER | Update product |
| DELETE | `/products/{id}` | FARMER | Delete product |

---

### Buyer Profile — `/buyer` (Role: `BUYER`)

| Method | Endpoint | Description |
|---|---|---|
| POST | `/buyer/profile` | Create buyer profile |
| GET | `/buyer/profile/me` | Get my profile |
| PUT | `/buyer/profile/me` | Update my profile |
| DELETE | `/buyer/profile/me` | Delete my profile |

---

### Orders — `/orders` (Role: `FARMER` + `BUYER`)

| Method | Endpoint | Description |
|---|---|---|
| POST | `/orders` | Place an order |
| GET | `/orders/buyer?page=0&size=20` | Get my orders as buyer (paginated) |
| GET | `/orders/farmer?page=0&size=20` | Get orders for my products (paginated) |
| PUT | `/orders/{id}/status` | Update order status |

**Order status transitions:**

| Role | Allowed Transitions |
|---|---|
| FARMER | `PENDING` → `CONFIRMED`, `CONFIRMED` → `SHIPPED` |
| BUYER | `SHIPPED` → `DELIVERED`, any → `CANCELLED` |

---

### AI Assistant — `/Ai` (Role: `FARMER` + `BUYER`)

| Method | Endpoint | Description |
|---|---|---|
| POST | `/Ai/openAi` | Ask the AI assistant |

**Request body:**
```json
{
  "query": "What crops are in season right now?"
}
```

---

## User Roles

| Role | Access |
|---|---|
| `FARMER` | Farmer profile, manage products, view/update orders, AI |
| `BUYER` | Buyer profile, browse products, place orders, AI |
| `ADMIN` | Full access |
| `DELIVERY` | Delivery-related access |

---

## Security

- All requests validated at the API Gateway — internal services are not publicly exposed
- JWT tokens signed with HMAC-SHA256, expire after **10 hours**
- API Key required on every request
- Passwords hashed with **BCrypt (strength 14)**
- Eureka dashboard protected with HTTP Basic Auth
- Redis rate limiting: auth endpoints 5 req/s, AI endpoint 2 req/s
- Security response headers on all responses
- `/products/reduce-stock/**` blocked at gateway (internal use only)
- No secrets in source code — all via environment variables

---

## Config Repository

Centralized configuration is stored at:
`https://github.com/kumar1771/UrbenFleet-ConfigRepo`

Contains config for: API Gateway routes, Eureka, Payment Service.

---

## Project Structure

```
UrbanFleet-main/
├── .github/
│   └── workflows/
│       └── ci.yml              # GitHub Actions CI — builds & pushes all images
├── Config-Server/
├── eureka-server/
├── API-GateWay/
├── Auth-service/
├── Farmer-Service/
├── Farmer-Goods-Service/
├── Buyer-Service/
├── Order-Service/
├── Payment-Service/
├── Ai-Service/
├── docker-compose.yml          # Full stack — all services + MySQL + Redis + Kafka
├── .env                        # Environment variables (not committed)
└── README.md
```

Each service follows the same internal structure:
```
<Service>/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── ...
│   │   │       ├── Controller/
│   │   │       ├── Service/
│   │   │       ├── Repository/
│   │   │       ├── Model/
│   │   │       ├── Dto/
│   │   │       ├── Exception/       # Typed exceptions + GlobalExceptionHandler
│   │   │       └── Config/
│   │   └── resources/
│   │       ├── application.properties
│   │       └── db/migration/        # Flyway SQL migrations
│   └── test/
├── Dockerfile
└── pom.xml
```

---

## CI/CD

GitHub Actions workflow (`.github/workflows/ci.yml`) runs on every push to `main`:

1. Builds all 9 service JARs with Maven
2. Runs unit tests — build fails if any test fails
3. Builds Docker images
4. Pushes to Docker Hub with two tags: `:latest` and `:<git-sha>`
