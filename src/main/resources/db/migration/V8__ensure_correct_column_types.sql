-- Garantir que todos os tipos de coluna estão corretos para evitar erro bytea
-- Esta migração é idempotente e pode ser executada múltiplas vezes

-- Verificar e corrigir tipos de dados
DO $$
BEGIN
    -- Garantir que problema é VARCHAR
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_name = 'tech_debts' AND column_name = 'problema' 
               AND data_type != 'character varying') THEN
        ALTER TABLE tech_debts ALTER COLUMN problema TYPE VARCHAR(500);
    END IF;
    
    -- Garantir que descricao é TEXT
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_name = 'tech_debts' AND column_name = 'descricao' 
               AND data_type != 'text') THEN
        ALTER TABLE tech_debts ALTER COLUMN descricao TYPE TEXT;
    END IF;
    
    -- Garantir que task_number é VARCHAR
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_name = 'tech_debts' AND column_name = 'task_number' 
               AND data_type != 'character varying') THEN
        ALTER TABLE tech_debts ALTER COLUMN task_number TYPE VARCHAR(100);
    END IF;
    
    -- Garantir que task_url é TEXT
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_name = 'tech_debts' AND column_name = 'task_url' 
               AND data_type != 'text') THEN
        ALTER TABLE tech_debts ALTER COLUMN task_url TYPE TEXT;
    END IF;
    
    -- Garantir que criado_por é VARCHAR
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_name = 'tech_debts' AND column_name = 'criado_por' 
               AND data_type != 'character varying') THEN
        ALTER TABLE tech_debts ALTER COLUMN criado_por TYPE VARCHAR(255);
    END IF;
END $$;