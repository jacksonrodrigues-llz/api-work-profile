--liquibase formatted sql
--changeset system:003-create-goals-table

-- Criação da tabela de metas
CREATE TABLE IF NOT EXISTS goals (
    id BIGSERIAL PRIMARY KEY NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(50),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    progress_percentage INTEGER DEFAULT 0,
    target_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    user_id BIGINT NOT NULL
);

-- Adição da chave estrangeira (com tratamento de erro caso já exista)
--rollback ALTER TABLE goals DROP CONSTRAINT IF EXISTS fk_goals_user;
ALTER TABLE goals DROP CONSTRAINT IF EXISTS fk_goals_user;
ALTER TABLE goals ADD CONSTRAINT fk_goals_user FOREIGN KEY (user_id) REFERENCES users(id);

-- Criação de índices
CREATE INDEX IF NOT EXISTS idx_goals_user_id ON goals(user_id);
CREATE INDEX IF NOT EXISTS idx_goals_status ON goals(status);