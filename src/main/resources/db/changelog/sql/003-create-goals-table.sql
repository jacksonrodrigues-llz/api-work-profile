-- 003-create-goals-table.sql
-- Criação da tabela de metas

CREATE TABLE goals (
    id BIGSERIAL PRIMARY KEY,
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

-- Foreign Key
ALTER TABLE goals 
ADD CONSTRAINT fk_goals_user 
FOREIGN KEY (user_id) REFERENCES users(id);

-- Índices para otimização
CREATE INDEX idx_goals_user_id ON goals(user_id);
CREATE INDEX idx_goals_status ON goals(status);