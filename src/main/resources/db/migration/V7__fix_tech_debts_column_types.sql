-- Corrigir tipos de colunas que podem estar causando erro bytea
-- Garantir que todos os campos de texto sejam VARCHAR ou TEXT

-- Adicionar campos que podem estar faltando
ALTER TABLE tech_debts 
ADD COLUMN IF NOT EXISTS criado_por_id BIGINT,
ADD COLUMN IF NOT EXISTS alterado_por_id BIGINT,
ADD COLUMN IF NOT EXISTS data_alteracao TIMESTAMP;

-- Garantir que os tipos estão corretos
ALTER TABLE tech_debts 
ALTER COLUMN problema TYPE VARCHAR(500),
ALTER COLUMN descricao TYPE TEXT,
ALTER COLUMN task_number TYPE VARCHAR(100),
ALTER COLUMN task_url TYPE TEXT,
ALTER COLUMN criado_por TYPE VARCHAR(255);

-- Recriar índices se necessário
DROP INDEX IF EXISTS idx_tech_debts_problema;
CREATE INDEX idx_tech_debts_problema ON tech_debts USING gin(to_tsvector('portuguese', problema));
CREATE INDEX IF NOT EXISTS idx_tech_debts_descricao ON tech_debts USING gin(to_tsvector('portuguese', descricao));