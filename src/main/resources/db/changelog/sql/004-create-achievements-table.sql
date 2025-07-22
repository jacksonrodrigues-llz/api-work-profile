-- 004-create-achievements-table.sql
-- Criação da tabela de conquistas

CREATE TABLE achievements (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    type VARCHAR(50) DEFAULT 'MILESTONE',
    achieved_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    user_id BIGINT NOT NULL
);

-- Foreign Key
ALTER TABLE achievements 
ADD CONSTRAINT fk_achievements_user 
FOREIGN KEY (user_id) REFERENCES users(id);

-- Índices para otimização
CREATE INDEX idx_achievements_user_id ON achievements(user_id);
CREATE INDEX idx_achievements_achieved_at ON achievements(achieved_at);