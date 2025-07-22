-- 005-create-tech-debts-table.sql
-- Criação da tabela de débitos técnicos

CREATE TABLE tech_debts (
    id BIGSERIAL PRIMARY KEY,
    problema VARCHAR(500) NOT NULL,
    descricao TEXT,
    prioridade INTEGER NOT NULL,
    status VARCHAR(20) DEFAULT 'TODO',
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_resolucao TIMESTAMP,
    criado_por VARCHAR(255),
    criado_por_id BIGINT,
    alterado_por_id BIGINT,
    data_alteracao TIMESTAMP,
    task_number VARCHAR(100),
    task_url TEXT,
    user_id BIGINT NOT NULL
);

-- Foreign Key
ALTER TABLE tech_debts 
ADD CONSTRAINT fk_tech_debts_user 
FOREIGN KEY (user_id) REFERENCES users(id);

-- Tabela para tipos de débito técnico (relacionamento many-to-many)
CREATE TABLE tech_debt_tipos (
    tech_debt_id BIGINT NOT NULL,
    tipos VARCHAR(20)
);

ALTER TABLE tech_debt_tipos 
ADD CONSTRAINT fk_tech_debt_tipos 
FOREIGN KEY (tech_debt_id) REFERENCES tech_debts(id) ON DELETE CASCADE;

-- Tabela para tags de débito técnico (relacionamento many-to-many)
CREATE TABLE tech_debt_tags (
    tech_debt_id BIGINT NOT NULL,
    tags VARCHAR(100)
);

ALTER TABLE tech_debt_tags 
ADD CONSTRAINT fk_tech_debt_tags 
FOREIGN KEY (tech_debt_id) REFERENCES tech_debts(id) ON DELETE CASCADE;

-- Índices para otimização
CREATE INDEX idx_tech_debts_user_id ON tech_debts(user_id);
CREATE INDEX idx_tech_debts_status ON tech_debts(status);
CREATE INDEX idx_tech_debts_prioridade ON tech_debts(prioridade);
CREATE INDEX idx_tech_debts_data_criacao ON tech_debts(data_criacao);
CREATE INDEX idx_tech_debts_task_number ON tech_debts(task_number);