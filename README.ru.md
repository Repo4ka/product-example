[![CI](https://github.com/Repo4ka/product-example/actions/workflows/ci.yml/badge.svg)](https://github.com/Repo4ka/product-example/actions/workflows/ci.yml)

# Product Demo API

Spring Boot REST API для управления каталогом товаров с системой скидок по категориям.

## Стек технологий

- Java 21, Spring Boot 3.5
- PostgreSQL с миграциями Flyway
- MapStruct для маппинга DTO, Lombok для сокращения шаблонного кода
- Testcontainers для интеграционных тестов
- SpringDoc OpenAPI (Swagger UI)

## Требования

- Java 21+
- PostgreSQL на localhost:5432
- Docker (необходим для запуска тестов)

## Сборка и запуск

```bash
# Создать базу данных
createdb product_demo

# Компиляция
./mvnw clean compile

# Запуск приложения (стартует на http://localhost:8080)
./mvnw spring-boot:run

# Запуск тестов
./mvnw clean test

# Сборка исполняемого JAR
./mvnw clean package
```

Подключение к БД по умолчанию: `localhost:5432/product_demo`, пользователь `postgres/postgres`. Переопределяется через переменные окружения:

```bash
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=product_demo
export DB_USERNAME=postgres
export DB_PASSWORD=postgres
```

## Документация API

Swagger UI: http://localhost:8080/swagger-ui.html
OpenAPI JSON: http://localhost:8080/v3/api-docs

## Эндпоинты

| Метод  | Путь              | Описание                                    | Коды ответа    |
|--------|-------------------|---------------------------------------------|----------------|
| POST   | `/products`       | Создать товар                               | 201, 400       |
| GET    | `/products`       | Список товаров (с пагинацией и фильтрацией) | 200            |
| GET    | `/products/{id}`  | Получить товар по ID                        | 200, 404       |
| PUT    | `/products/{id}`  | Обновить товар (полная замена)              | 200, 400, 404  |
| DELETE | `/products/{id}`  | Удаление товара (soft delete)               | 200, 404       |

## Примеры запросов

### Создание товара

```bash
curl -X POST http://localhost:8080/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Чистый код",
    "description": "Справочник по гибкой разработке программного обеспечения",
    "price": 39.99,
    "category": "BOOKS"
  }'
```

Ответ (201):
```json
{
  "id": 1,
  "name": "Чистый код",
  "description": "Справочник по гибкой разработке программного обеспечения",
  "price": 39.99,
  "discountedPrice": 35.99,
  "category": "BOOKS",
  "createdAt": "2026-02-24T10:30:00Z",
  "updatedAt": "2026-02-24T10:30:00Z"
}
```

### Получение товара по ID

```bash
curl http://localhost:8080/products/1
```

### Обновление товара

```bash
curl -X PUT http://localhost:8080/products/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Чистый код (2-е издание)",
    "description": null,
    "price": 44.99,
    "category": "BOOKS"
  }'
```

### Удаление товара

```bash
curl -X DELETE http://localhost:8080/products/1
```

### Список товаров с пагинацией

```bash
curl "http://localhost:8080/products?page=0&size=10"
```

### Фильтрация по категории

```bash
curl "http://localhost:8080/products?category=BOOKS&page=0&size=10"
```

## Валидация запросов

| Поле        | Правила                                             |
|-------------|-----------------------------------------------------|
| name        | Обязательное, непустое, максимум 255 символов       |
| description | Необязательное, максимум 1000 символов              |
| price       | Обязательное, положительное, максимум 2 знака после запятой |
| category    | Обязательное, одно из: ELECTRONICS, BOOKS, FOOD, OTHER |

Ошибки валидации возвращают 400 с детализацией по полям:

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

## Правила скидок

Каждая категория товаров определяет свою ставку скидки, которая применяется при формировании ответа (не хранится в базе данных):

| Категория   | Скидка |
|-------------|--------|
| BOOKS       | 10%    |
| ELECTRONICS | 0%     |
| FOOD        | 0%     |
| OTHER       | 0%     |

Поле `discountedPrice` включается во все ответы с товарами наряду с оригинальной ценой `price`.

## Удаление

Товары никогда не удаляются физически из базы данных. `DELETE /products/{id}` помечает товар как удалённый и фиксирует время удаления. Удалённые (soft delete) товары исключаются из всех операций получения и листинга.

## Открытые вопросы по заданию

Задание выполнено исходя с рядом допущений. В рабочем процессе я бы задала ряд вопросов, например:
1. Уверены ли мы, что хотим использовать скидку по категориям товаров?
2. Хотим ли мы хардкодить скидку в категории, или нам нужна возможность менять ставку скидки категории используя API?
3. Я предположила, что DELETE товара должен работать как soft delete, так как по опыту товар обычно не удаляется полностью из БД. Это тоже требует уточнения.
4. Как скидка должна округляться? На данный момент я предположила стандартный вариант с округлением в большую сторону.
5. И ещё множество других вопросов, связанных с ролями доступа, количеством хранимых товаров, сценариями использования, валютами и пр.


