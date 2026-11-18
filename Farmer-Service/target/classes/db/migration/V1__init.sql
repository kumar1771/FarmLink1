CREATE TABLE IF NOT EXISTS farmer_profiles (
    id          CHAR(36)      NOT NULL PRIMARY KEY,
    user_id     VARCHAR(255)  NOT NULL UNIQUE,
    farm_name   VARCHAR(255)  NOT NULL,
    farm_type   VARCHAR(100),
    location    VARCHAR(255),
    description VARCHAR(1000),
    rating      DOUBLE        DEFAULT 0.0,
    verified    BOOLEAN       DEFAULT FALSE,
    created_at  DATETIME
);
