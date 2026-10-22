-- No Supabase, as tabelas do schema public também ficam acessíveis pela Data API (PostgREST)
-- para quem tiver a chave anônima do projeto. O Dismar Stock não usa essa API: ligar o RLS sem
-- nenhuma política bloqueia esse acesso. A API Java conecta como dona das tabelas e não é afetada.
-- Em um PostgreSQL comum (desenvolvimento e testes) não muda nada.
-- A flyway_schema_history fica de fora: o Flyway mantém essa tabela travada durante a migração.
ALTER TABLE category ENABLE ROW LEVEL SECURITY;
ALTER TABLE supplier ENABLE ROW LEVEL SECURITY;
ALTER TABLE product ENABLE ROW LEVEL SECURITY;
ALTER TABLE stock_movement ENABLE ROW LEVEL SECURITY;
ALTER TABLE app_user ENABLE ROW LEVEL SECURITY;
