# Product Manager App

A Spring Boot application with HTMX, Thymeleaf, Web Awesome UI, Flyway, Postgres, and JdbcClient.

## Tech Stack

- **Backend**: Spring Boot 3.4.2, Java 24, Gradle 9.3.1
- **UI**: Thymeleaf + HTMX 2.0 + Web Awesome 3.0 (CDN)
- **Database**: PostgreSQL 17 + Flyway migrations
- **Data Access**: Spring JdbcClient (no JPA)
- **Scheduled Jobs**: Product import from [famme.no](https://famme.no/products.json)

## Quick Start

### 1. Start Postgres

```bash
docker compose up -d
```

### 2. Run the App

```bash
JAVA_HOME=/Users/roy/Library/Java/JavaVirtualMachines/temurin-24.0.2/Contents/Home ./gradlew bootRun
```

### 3. Open Browser

Navigate to [http://localhost:8080](http://localhost:8080)

## Features

- **Load Products** — Fetches products from the database and displays in a table
- **Add Product** — Form to add new products (updates table via HTMX without page reload)
- **Fetch from Famme** — Imports up to 50 products from the Famme API
- **Scheduled Import** — Automatically fetches products daily (5 sec after startup)
- **Product Variants** — Each product's variants are shown inline in the table

## Database Schema

```
products (id, external_id, title, handle, vendor, product_type, image_url, price, created_at)
    └── product_variants (id, product_id, external_id, title, sku, price, option1, option2, option3, available)
```

## Project Structure

```
src/main/java/com/example/productapp/
├── ProductAppApplication.java       # Main entry point + @EnableScheduling
├── controller/
│   └── ProductController.java       # HTMX-compatible endpoints
├── model/
│   ├── Product.java                 # Product domain model
│   └── ProductVariant.java          # Variant domain model
├── repository/
│   └── ProductRepository.java       # JdbcClient data access
└── service/
    ├── ProductService.java          # Business logic
    └── ProductFetchService.java     # Scheduled job + API fetcher

src/main/resources/
├── application.yml                  # Configuration
├── db/migration/
│   └── V1__create_tables.sql        # Flyway migration
└── templates/
    ├── index.html                   # Main page (HTMX + Web Awesome)
    └── fragments/
        └── product-table.html       # Product table fragment
```
