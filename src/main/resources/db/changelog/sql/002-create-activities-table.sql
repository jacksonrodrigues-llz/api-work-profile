--liquibase formatted sql
--changeset system:002-create-activities-table

-- Criação da tabela de atividades
CREATE TABLE IF NOT EXISTS activities (
    id BIGSERIAL PRIMARY KEY NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(20) DEFAULT 'TODO',
    priority VARCHAR(20) DEFAULT 'MEDIUM',
    project VARCHAR(255),
    skills VARCHAR(500),
    estimated_hours INTEGER,
    actual_hours INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    user_id BIGINT NOT NULL
);

-- Adição da chave estrangeira (com tratamento de erro caso já exista)
--rollback ALTER TABLE activities DROP CONSTRAINT IF EXISTS fk_activities_user;
ALTER TABLE activities DROP CONSTRAINT IF EXISTS fk_activities_user;
ALTER TABLE activities ADD CONSTRAINT fk_activities_user FOREIGN KEY (user_id) REFERENCES users(id);

-- Criação de índices
CREATE INDEX IF NOT EXISTS idx_activities_user_id ON activities(user_id);
CREATE INDEX IF NOT EXISTS idx_activities_status ON activities(status);
CREATE INDEX IF NOT EXISTS idx_activities_created_at ON activities(created_at);