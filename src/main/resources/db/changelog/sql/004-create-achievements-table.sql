--liquibase formatted sql
--changeset system:004-create-achievements-table

-- Criação da tabela de conquistas
CREATE TABLE IF NOT EXISTS achievements (
    id BIGSERIAL PRIMARY KEY NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    type VARCHAR(50) DEFAULT 'MILESTONE',
    achieved_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    user_id BIGINT NOT NULL
);

-- Adição da chave estrangeira (com tratamento de erro caso já exista)
--rollback ALTER TABLE achievements DROP CONSTRAINT IF EXISTS fk_achievements_user;
ALTER TABLE achievements DROP CONSTRAINT IF EXISTS fk_achievements_user;
ALTER TABLE achievements ADD CONSTRAINT fk_achievements_user FOREIGN KEY (user_id) REFERENCES users(id);

-- Criação de índices
CREATE INDEX IF NOT EXISTS idx_achievements_user_id ON achievements(user_id);
CREATE INDEX IF NOT EXISTS idx_achievements_achieved_at ON achievements(achieved_at);