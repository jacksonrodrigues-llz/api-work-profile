-- 002-create-activities-table.sql
-- Criação da tabela de atividades

CREATE TABLE activities (
    id BIGSERIAL PRIMARY KEY,
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

-- Foreign Key
ALTER TABLE activities 
ADD CONSTRAINT fk_activities_user 
FOREIGN KEY (user_id) REFERENCES users(id);

-- Índices para otimização
CREATE INDEX idx_activities_user_id ON activities(user_id);
CREATE INDEX idx_activities_status ON activities(status);
CREATE INDEX idx_activities_created_at ON activities(created_at);