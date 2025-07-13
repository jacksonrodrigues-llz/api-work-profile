-- Corrigir tipos dos campos task_number e task_url se necessário
ALTER TABLE tech_debts 
ALTER COLUMN task_number TYPE VARCHAR(100),
ALTER COLUMN task_url TYPE TEXT;