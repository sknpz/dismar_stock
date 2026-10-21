-- Dismar Stock - esquema inicial (PostgreSQL)
-- Executado automaticamente pelo Flyway ao iniciar a API.

-- Categorias ficam em tabela própria para evitar variações do mesmo nome
-- ("Ferragens", "ferragens", "Ferragem") espalhadas pelos produtos.
CREATE TABLE category (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name        VARCHAR(80)  NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- Unicidade sem diferenciar maiúsculas/minúsculas
CREATE UNIQUE INDEX uq_category_name ON category (lower(name));

CREATE TABLE supplier (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name        VARCHAR(150) NOT NULL,
    -- Somente dígitos/letras, sem pontuação. Aceita o CNPJ alfanumérico
    -- (12 caracteres alfanuméricos + 2 dígitos verificadores) em vigor desde 07/2026.
    cnpj        CHAR(14)     UNIQUE,
    phone       VARCHAR(20),
    email       VARCHAR(150),
    address     VARCHAR(255),
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT ck_supplier_cnpj_format CHECK (cnpj ~ '^[0-9A-Z]{12}[0-9]{2}$')
);

CREATE TABLE product (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name            VARCHAR(150)  NOT NULL,
    sku             VARCHAR(50)   UNIQUE,
    category_id     BIGINT        REFERENCES category (id) ON DELETE SET NULL,
    -- RESTRICT: fornecedor com produtos vinculados não pode ser excluído
    supplier_id     BIGINT        REFERENCES supplier (id) ON DELETE RESTRICT,
    unit_price      NUMERIC(12,2) NOT NULL,
    quantity        INTEGER       NOT NULL DEFAULT 0,
    min_stock       INTEGER       NOT NULL DEFAULT 10,
    -- Exclusão lógica: o produto some das listagens, mas o histórico de
    -- movimentações continua apontando para ele.
    active          BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ   NOT NULL DEFAULT now(),

    CONSTRAINT ck_product_unit_price CHECK (unit_price >= 0),
    CONSTRAINT ck_product_quantity   CHECK (quantity >= 0),
    CONSTRAINT ck_product_min_stock  CHECK (min_stock >= 0)
);

CREATE INDEX idx_product_category ON product (category_id);
CREATE INDEX idx_product_supplier ON product (supplier_id);

CREATE TABLE stock_movement (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    product_id          BIGINT       NOT NULL REFERENCES product (id) ON DELETE RESTRICT,
    type                VARCHAR(10)  NOT NULL,
    quantity            INTEGER      NOT NULL,
    previous_quantity   INTEGER      NOT NULL,
    new_quantity        INTEGER      NOT NULL,
    notes               VARCHAR(255),
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT ck_movement_type     CHECK (type IN ('ENTRY', 'EXIT')),
    CONSTRAINT ck_movement_quantity CHECK (quantity > 0),
    CONSTRAINT ck_movement_new_quantity CHECK (new_quantity >= 0),
    -- Garante que o saldo registrado bate com o tipo da movimentação
    CONSTRAINT ck_movement_balance CHECK (
        (type = 'ENTRY' AND new_quantity = previous_quantity + quantity) OR
        (type = 'EXIT'  AND new_quantity = previous_quantity - quantity)
    )
);

CREATE INDEX idx_movement_product    ON stock_movement (product_id);
CREATE INDEX idx_movement_created_at ON stock_movement (created_at DESC);
