-- Dados de exemplo para desenvolvimento e demonstração.
-- Não faz parte das migrations: rode manualmente em um banco vazio.
-- CNPJs fictícios (formato válido, não pertencem a empresas reais).

-- Usuários de desenvolvimento. Senha dos dois: estoque123 (hash BCrypt abaixo).
-- Nunca use estes usuários em produção: lá o primeiro administrador vem de ADMIN_EMAIL/ADMIN_PASSWORD.
INSERT INTO app_user (name, email, password_hash, role) VALUES
    ('Administrador', 'admin@tana.local',    '{bcrypt}$2a$10$YLNVf1erNC4gBRgl7YI7/eb8zVNb44KBcA8OA2soiTQqktOlgbQbW', 'ADMIN'),
    ('Operador',      'operador@tana.local', '{bcrypt}$2a$10$YLNVf1erNC4gBRgl7YI7/eb8zVNb44KBcA8OA2soiTQqktOlgbQbW', 'USER');

INSERT INTO category (name) VALUES
    ('Ferragens'),
    ('Elétrica'),
    ('Hidráulica'),
    ('Ferramentas');

INSERT INTO supplier (name, cnpj, phone, email, address) VALUES
    ('Distribuidora Paraná Ferragens', '11222333000181', '(44) 3622-1000', 'vendas@pr-ferragens.example', 'Av. Brasil, 1500 - Umuarama/PR'),
    ('Elétrica Oeste Atacado',          '44555666000181', '(45) 3220-2000', 'contato@eletricaoeste.example', 'Rua Paraná, 820 - Cascavel/PR'),
    ('Hidro Sul Materiais',             '77888999000181', '(44) 3025-3000', 'comercial@hidrosul.example',   'Av. Colombo, 4300 - Maringá/PR');

INSERT INTO product (name, sku, category_id, supplier_id, unit_price, quantity, min_stock) VALUES
    ('Parafuso sextavado 1/4" (cento)', 'FER-001', 1, 1,  18.90, 120, 30),
    ('Dobradiça 3" zincada',            'FER-002', 1, 1,   7.50,  25, 30),
    ('Cabo flexível 2,5mm (rolo 100m)', 'ELE-001', 2, 2, 289.00,  14, 10),
    ('Disjuntor bipolar 32A',           'ELE-002', 2, 2,  42.90,   0,  5),
    ('Tubo PVC soldável 25mm (6m)',     'HID-001', 3, 3,  24.50,  60, 20),
    ('Registro de gaveta 3/4"',         'HID-002', 3, 3,  56.00,   8, 10),
    ('Furadeira de impacto 650W',       'TOO-001', 4, 1, 329.90,   6,  3);

-- O saldo atual de cada produto acima é o resultado destas movimentações
INSERT INTO stock_movement (product_id, type, quantity, previous_quantity, new_quantity, notes, user_id, created_at) VALUES
    (1, 'ENTRY', 150,   0, 150, 'Estoque inicial',         1, now() - interval '20 days'),
    (1, 'EXIT',   30, 150, 120, 'Venda balcão',            2, now() - interval '3 days'),
    (2, 'ENTRY',  40,   0,  40, 'Estoque inicial',         1, now() - interval '20 days'),
    (2, 'EXIT',   15,  40,  25, 'Pedido #1042',            2, now() - interval '2 days'),
    (3, 'ENTRY',  14,   0,  14, 'Estoque inicial',         1, now() - interval '18 days'),
    (4, 'ENTRY',  10,   0,  10, 'Estoque inicial',         1, now() - interval '18 days'),
    (4, 'EXIT',   10,  10,   0, 'Obra Residencial Jardim', 2, now() - interval '1 day'),
    (5, 'ENTRY',  60,   0,  60, 'Estoque inicial',         1, now() - interval '15 days'),
    (6, 'ENTRY',   8,   0,   8, 'Estoque inicial',         1, now() - interval '15 days'),
    (7, 'ENTRY',   6,   0,   6, 'Estoque inicial',         1, now() - interval '10 days');
