-- Adiciona a coluna category à tabela users
ALTER TABLE users ADD COLUMN IF NOT EXISTS category VARCHAR(50);