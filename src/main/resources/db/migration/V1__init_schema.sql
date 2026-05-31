-- 초기 스키마. JPA 엔티티(ddl-auto=validate)와 컬럼/타입이 일치해야 한다.
-- LocalDateTime은 Hibernate가 DATETIME(6)으로 매핑하므로 정밀도(6)를 맞춘다.

CREATE TABLE product (
    id         BIGINT         NOT NULL AUTO_INCREMENT,
    name       VARCHAR(100)   NOT NULL,
    sku        VARCHAR(50)    NOT NULL,
    price      DECIMAL(12, 2) NOT NULL,
    category   VARCHAR(50),
    created_at DATETIME(6)    NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_product_sku (sku)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE inventory (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    product_id BIGINT       NOT NULL,
    quantity   INT          NOT NULL,
    location   VARCHAR(100),
    updated_at DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_inventory_product FOREIGN KEY (product_id) REFERENCES product (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE stock_log (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    product_id BIGINT       NOT NULL,
    type       VARCHAR(10)  NOT NULL,
    quantity   INT          NOT NULL,
    reason     VARCHAR(255),
    created_at DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_stock_log_product FOREIGN KEY (product_id) REFERENCES product (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE member (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    email      VARCHAR(100) NOT NULL,
    password   VARCHAR(255) NOT NULL,
    role       VARCHAR(20)  NOT NULL,
    created_at DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_member_email (email)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;