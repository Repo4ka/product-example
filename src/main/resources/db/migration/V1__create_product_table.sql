CREATE TABLE product (
    id          BIGSERIAL       PRIMARY KEY,
    name        VARCHAR(255)    NOT NULL,
    description VARCHAR(1000),
    price       DECIMAL(12, 2)  NOT NULL CHECK (price > 0),
    category    VARCHAR(20)     NOT NULL,
    created_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    deleted     BOOLEAN         NOT NULL DEFAULT FALSE,
    deleted_at  TIMESTAMPTZ
);

CREATE INDEX idx_product_category ON product(category) WHERE deleted = false;
CREATE INDEX idx_product_deleted ON product(deleted);
