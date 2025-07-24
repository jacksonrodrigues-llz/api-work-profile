--liquibase formatted sql
--changeset system:005-create-tech-debts-table

-- Criação da tabela de débitos técnicos
CREATE TABLE IF NOT EXISTS tech_debts (
    id BIGSERIAL PRIMARY KEY NOT NULL,
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

-- Adição da chave estrangeira (com tratamento de erro caso já exista)
--rollback ALTER TABLE tech_debts DROP CONSTRAINT IF EXISTS fk_tech_debts_user;
ALTER TABLE tech_debts DROP CONSTRAINT IF EXISTS fk_tech_debts_user;
ALTER TABLE tech_debts ADD CONSTRAINT fk_tech_debts_user FOREIGN KEY (user_id) REFERENCES users(id);

-- Criação da tabela de tipos de débitos técnicos
CREATE TABLE IF NOT EXISTS tech_debt_tipos (
    tech_debt_id BIGINT NOT NULL,
    tipos VARCHAR(20)
);

-- Adição da chave estrangeira para tipos (com tratamento de erro caso já exista)
--rollback ALTER TABLE tech_debt_tipos DROP CONSTRAINT IF EXISTS fk_tech_debt_tipos;
ALTER TABLE tech_debt_tipos DROP CONSTRAINT IF EXISTS fk_tech_debt_tipos;
ALTER TABLE tech_debt_tipos ADD CONSTRAINT fk_tech_debt_tipos FOREIGN KEY (tech_debt_id) REFERENCES tech_debts(id) ON DELETE CASCADE;

-- Criação da tabela de tags de débitos técnicos
CREATE TABLE IF NOT EXISTS tech_debt_tags (
    tech_debt_id BIGINT NOT NULL,
    tags VARCHAR(100)
);

-- Adição da chave estrangeira para tags (com tratamento de erro caso já exista)
--rollback ALTER TABLE tech_debt_tags DROP CONSTRAINT IF EXISTS fk_tech_debt_tags;
ALTER TABLE tech_debt_tags DROP CONSTRAINT IF EXISTS fk_tech_debt_tags;
ALTER TABLE tech_debt_tags ADD CONSTRAINT fk_tech_debt_tags FOREIGN KEY (tech_debt_id) REFERENCES tech_debts(id) ON DELETE CASCADE;

-- Criação de índices
CREATE INDEX IF NOT EXISTS idx_tech_debts_user_id ON tech_debts(user_id);
CREATE INDEX IF NOT EXISTS idx_tech_debts_status ON tech_debts(status);
CREATE INDEX IF NOT EXISTS idx_tech_debts_prioridade ON tech_debts(prioridade);
CREATE INDEX IF NOT EXISTS idx_tech_debts_data_criacao ON tech_debts(data_criacao);
CREATE INDEX IF NOT EXISTS idx_tech_debts_task_number ON tech_debts(task_number);