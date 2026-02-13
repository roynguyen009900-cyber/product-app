CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    external_id BIGINT UNIQUE,
    title VARCHAR(500) NOT NULL,
    handle VARCHAR(500),
    vendor VARCHAR(255),
    product_type VARCHAR(255),
    image_url TEXT,
    price NUMERIC(10,2),
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE product_variants (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    external_id BIGINT UNIQUE,
    title VARCHAR(500),
    sku VARCHAR(255),
    price NUMERIC(10,2),
    option1 VARCHAR(255),
    option2 VARCHAR(255),
    option3 VARCHAR(255),
    available BOOLEAN DEFAULT true
);

CREATE INDEX idx_product_variants_product_id ON product_variants(product_id);
