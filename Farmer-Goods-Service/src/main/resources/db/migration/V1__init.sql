CREATE TABLE IF NOT EXISTS products (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    farmer_id           VARCHAR(255) NOT NULL,
    product_name        VARCHAR(255) NOT NULL,
    category            VARCHAR(100),
    type                VARCHAR(100),
    farmer_price        DOUBLE,
    market_price        DOUBLE,
    quantity_available  INT,
    unit                VARCHAR(50),
    location            VARCHAR(255),
    harvest_date        DATE,
    available           BOOLEAN DEFAULT TRUE,
    INDEX idx_product_farmer_id (farmer_id)
);
