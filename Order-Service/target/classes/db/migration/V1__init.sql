CREATE TABLE IF NOT EXISTS orders (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    buyer_id         VARCHAR(255) NOT NULL,
    farmer_id        VARCHAR(255) NOT NULL,
    product_id       BIGINT       NOT NULL,
    product_name     VARCHAR(255),
    quantity         INT,
    price_per_unit   DOUBLE,
    total_price      DOUBLE,
    order_status     VARCHAR(50),
    delivery_address VARCHAR(500),
    order_date       DATETIME,
    version          BIGINT DEFAULT 0,
    INDEX idx_order_buyer_id  (buyer_id),
    INDEX idx_order_farmer_id (farmer_id)
);
