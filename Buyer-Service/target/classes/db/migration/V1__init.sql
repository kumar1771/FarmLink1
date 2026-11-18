CREATE TABLE IF NOT EXISTS buyers (
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id  VARCHAR(255) NOT NULL UNIQUE,
    name     VARCHAR(255),
    email    VARCHAR(255),
    phone    VARCHAR(20),
    location VARCHAR(255),
    INDEX idx_buyer_user_id (user_id)
);
