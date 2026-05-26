# MetroSewa

MetroSewa is a simple Spring Boot microservices project for metro users, routes, and ticket booking.

## Services

### user-service

Handles user registration, login, profile lookup, password encryption, and JWT token creation.

Port: `8081`

Swagger: `http://localhost:8081/swagger-ui/index.html`

Database: `metro_user_db`

### route-service

Handles metro stations, metro lines, station order, station search, and route planning.

It calculates route details like total stations, distance, estimated time, fare, and interchange.

Port: `8082`

Swagger: `http://localhost:8082/swagger-ui/index.html`

Database: `metro_route_db`

Uses:
- MariaDB for station, line, and route data
- Redis for route-service cache support
- Elasticsearch for station search

### ticket-service

Handles ticket booking.

Before saving a ticket, it calls route-service to get route, fare, distance, time, and interchange details.

Port: `8083`

Swagger: `http://localhost:8083/swagger-ui/index.html`

Database: `metro_ticket_db`

## Docker Usage

Start the local dependencies from the project root:

```powershell
docker compose up -d mariadb route-redis route-elasticsearch
```

The MariaDB container runs on local port `3307`.

The init script creates these databases:
- `metro_user_db`
- `metro_route_db`
- `metro_ticket_db`

## Monitoring

The project has Docker config for:
- Prometheus: `http://localhost:9090`
- Grafana: `http://localhost:3000`

Start them with:

```powershell
docker compose up -d prometheus grafana
```

## Circuit Breaker

ticket-service uses Resilience4j circuit breaker when calling route-service.

If route-service is down, ticket-service returns a fallback response with status `ROUTE_SERVICE_UNAVAILABLE` instead of failing badly.

## Run Services

Run each service separately:

```powershell
cd user-service
.\mvnw.cmd spring-boot:run
```

```powershell
cd route-service
.\mvnw.cmd spring-boot:run
```

```powershell
cd ticket-service
.\mvnw.cmd spring-boot:run
```
