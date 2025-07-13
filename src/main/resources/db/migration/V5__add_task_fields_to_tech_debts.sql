-- Adicionar campos para integração com ferramentas externas
ALTER TABLE tech_debts 
ADD COLUMN task_number VARCHAR(100) NULL,
ADD COLUMN task_url TEXT NULL;

-- Criar índices para melhorar performance das buscas
CREATE INDEX IF NOT EXISTS idx_tech_debts_task_number ON tech_debts(task_number);
CREATE INDEX IF NOT EXISTS idx_tech_debts_data_criacao ON tech_debts(data_criacao);
CREATE INDEX IF NOT EXISTS idx_tech_debts_problema ON tech_debts(problema);