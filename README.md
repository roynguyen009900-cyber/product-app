# Product Manager App

A Spring Boot application with HTMX, Thymeleaf, Web Awesome UI, Flyway, Postgres, and JdbcClient.

## Tech Stack

- **Backend**: Spring Boot 4.0.2, Java 25, Kotlin 2.3.0, Gradle 9.3.1
- **UI**: Thymeleaf + HTMX 2.0.4 + Web Awesome 3.2.1 (CDN)
- **Database**: PostgreSQL 17 + Flyway migrations
- **Data Access**: Spring JdbcClient (no JPA)
- **Scheduled Jobs**: Product import from [famme.no](https://famme.no/products.json)

## Quick Start

### 1. Start Postgres

```bash
docker compose up -d
```

### 2. Run the App

Ensure you have Java 25 installed.

```bash
./gradlew bootRun
```

Or if you need to point to a specific Java home:

```bash
JAVA_HOME=/path/to/java-25 ./gradlew bootRun
```

### 3. Open Browser

Navigate to [http://localhost:8080](http://localhost:8080)

## Features

- **Load Products** — Browse products in a responsive table.
- **Search Products** — Search by title or handle.
- **Add Product** — Form to add new products with variants.
- **Edit Product** — Update product details and prices.
- **Delete Product** — Remove products with confirmation dialog.
- **Product Variants** — Manage variants (add/remove) and toggle their availability directly from the list.
- **Scheduled Import** — Automatically fetches products from Famme API daily (5 sec after startup).

## Database Schema

```
products (id, external_id, title, handle, vendor, product_type, image_url, price, created_at)
    └── product_variants (id, product_id, external_id, title, sku, price, option1, option2, option3, available)
        (ON DELETE CASCADE)
```

## Project Structure

```
src/main/kotlin/com/example/productapp/
├── ProductAppApplication.kt     # Main entry point + @EnableScheduling
├── controller/
│   └── ProductController.kt     # HTMX-compatible endpoints
├── model/
│   ├── Product.kt               # Product domain model
│   └── ProductVariant.kt        # Variant domain model
├── repository/
│   └── ProductRepository.kt     # JdbcClient data access
└── service/
    ├── ProductService.kt        # Business logic
    └── ProductFetchService.kt   # Scheduled job + API fetcher

src/main/resources/
├── application.yml              # Configuration
├── db/migration/
│   └── V1__create_tables.sql    # Flyway migration
└── templates/
    ├── index.html               # Main page (HTMX + Web Awesome)
    ├── search.html              # Search page
    ├── edit-product.html        # Edit product page
    └── fragments/
        ├── product-table.html   # Product table fragment
        ├── delete-dialog.html   # Delete confirmation dialog
        └── variant-availability.html # Variant availability toggle
```
