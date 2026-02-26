[![CI](https://github.com/Repo4ka/product-example/actions/workflows/ci.yml/badge.svg)](https://github.com/Repo4ka/product-example/actions/workflows/ci.yml)

# Product Demo API

Spring Boot REST API for managing a product catalog with category-based discount pricing.

## Tech Stack

- Java 21, Spring Boot 3.5
- PostgreSQL with Flyway migrations
- MapStruct for DTO mapping, Lombok for boilerplate reduction
- Testcontainers for integration tests
- SpringDoc OpenAPI (Swagger UI)

## Prerequisites

- Java 21+
- PostgreSQL running on localhost:5432
- Docker (required for tests)

## Build & Run

```bash
# Create the database
createdb product_demo

# Compile
./mvnw clean compile

# Run the application (starts on http://localhost:8080)
./mvnw spring-boot:run

# Run tests
./mvnw clean test

# Build executable JAR
./mvnw clean package
```

Database connection defaults to `localhost:5432/product_demo` with `postgres/postgres`. Override with environment variables:

```bash
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=product_demo
export DB_USERNAME=postgres
export DB_PASSWORD=postgres
```

## API Documentation

Swagger UI: http://localhost:8080/swagger-ui.html
OpenAPI JSON: http://localhost:8080/v3/api-docs

## Endpoints

| Method | Path              | Description                          | Status Codes   |
|--------|-------------------|--------------------------------------|----------------|
| POST   | `/products`       | Create a product                     | 201, 400       |
| GET    | `/products`       | List products (paginated, filterable)| 200            |
| GET    | `/products/{id}`  | Get a product by ID                  | 200, 404       |
| PUT    | `/products/{id}`  | Update a product (full replacement)  | 200, 400, 404  |
| DELETE | `/products/{id}`  | Soft-delete a product                | 200, 404       |

## Example Requests

### Create a product

```bash
curl -X POST http://localhost:8080/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Clean Code",
    "description": "A handbook of agile software craftsmanship",
    "price": 39.99,
    "category": "BOOKS"
  }'
```

Response (201):
```json
{
  "id": 1,
  "name": "Clean Code",
  "description": "A handbook of agile software craftsmanship",
  "price": 39.99,
  "discountedPrice": 35.99,
  "category": "BOOKS",
  "createdAt": "2026-02-24T10:30:00Z",
  "updatedAt": "2026-02-24T10:30:00Z"
}
```

### Get a product by ID

```bash
curl http://localhost:8080/products/1
```

### Update a product

```bash
curl -X PUT http://localhost:8080/products/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Clean Code (2nd Edition)",
    "description": null,
    "price": 44.99,
    "category": "BOOKS"
  }'
```

### Delete a product

```bash
curl -X DELETE http://localhost:8080/products/1
```

### List products with pagination

```bash
curl "http://localhost:8080/products?page=0&size=10"
```

### Filter by category

```bash
curl "http://localhost:8080/products?category=BOOKS&page=0&size=10"
```

## Request Validation

| Field       | Rules                                              |
|-------------|----------------------------------------------------|
| name        | Required, non-blank, max 255 characters            |
| description | Optional, max 1000 characters                      |
| price       | Required, positive, max 2 decimal places           |
| category    | Required, one of: ELECTRONICS, BOOKS, FOOD, OTHER  |

Validation errors return 400 with field-level details:

```json
{
  "timestamp": "2026-02-24T10:35:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "fieldErrors": [
    { "field": "name", "message": "Name is required" },
    { "field": "price", "message": "Price must be positive" }
  ]
}
```

## Discount Rules

Each product category defines a discount rate applied at response time (not stored in the database):

| Category    | Discount |
|-------------|----------|
| BOOKS       | 10%      |
| ELECTRONICS | 0%       |
| FOOD        | 0%       |
| OTHER       | 0%       |

The `discountedPrice` field is included in all product responses alongside the original `price`.

## Soft Delete

Products are never physically removed from the database. `DELETE /products/{id}` marks the product as deleted and records a deletion timestamp. Soft-deleted products are excluded from all retrieval and listing operations.
