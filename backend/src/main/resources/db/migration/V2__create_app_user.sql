-- Dismar Stock - usuários e autenticação (fase 4)

-- "user" é palavra reservada no PostgreSQL, por isso app_user
CREATE TABLE app_user (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name            VARCHAR(150)  NOT NULL,
    email           VARCHAR(150)  NOT NULL,
    -- Hash BCrypt com prefixo do algoritmo ({bcrypt}...), nunca a senha em texto
    password_hash   VARCHAR(100)  NOT NULL,
    role            VARCHAR(20)   NOT NULL,
    -- Usuários são desativados, não excluídos: as movimentações continuam apontando para eles
    active          BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ   NOT NULL DEFAULT now(),

    CONSTRAINT ck_app_user_role CHECK (role IN ('ADMIN', 'USER'))
);

-- Login sem diferenciar maiúsculas/minúsculas no e-mail
CREATE UNIQUE INDEX uq_app_user_email ON app_user (lower(email));

-- Quem registrou cada movimentação. Nulo nas movimentações anteriores à autenticação.
ALTER TABLE stock_movement
    ADD COLUMN user_id BIGINT REFERENCES app_user (id) ON DELETE RESTRICT;

CREATE INDEX idx_movement_user ON stock_movement (user_id);
