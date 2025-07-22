-- 000-complete-database.sql
-- Script completo para criação do banco de dados
-- Portal de Progresso Profissional

-- =====================================================
-- 1. TABELA DE USUÁRIOS
-- =====================================================

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    github_username VARCHAR(255),
    github_token VARCHAR(500),
    company VARCHAR(255),
    position VARCHAR(255),
    profile_photo VARCHAR(500),
    avatar VARCHAR(500),
    password VARCHAR(255),
    enabled BOOLEAN DEFAULT true,
    role VARCHAR(20) DEFAULT 'USER',
    password_reset_token VARCHAR(255),
    token_expiration TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_github_username ON users(github_username);

-- =====================================================
-- 2. TABELA DE ATIVIDADES
-- =====================================================

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
    user_id BIGINT NOT NULL,
    CONSTRAINT fk_activities_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_activities_user_id ON activities(user_id);
CREATE INDEX idx_activities_status ON activities(status);
CREATE INDEX idx_activities_created_at ON activities(created_at);

-- =====================================================
-- 3. TABELA DE METAS
-- =====================================================

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
    user_id BIGINT NOT NULL,
    CONSTRAINT fk_goals_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_goals_user_id ON goals(user_id);
CREATE INDEX idx_goals_status ON goals(status);

-- =====================================================
-- 4. TABELA DE CONQUISTAS
-- =====================================================

CREATE TABLE achievements (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    type VARCHAR(50) DEFAULT 'MILESTONE',
    achieved_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    user_id BIGINT NOT NULL,
    CONSTRAINT fk_achievements_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_achievements_user_id ON achievements(user_id);
CREATE INDEX idx_achievements_achieved_at ON achievements(achieved_at);

-- =====================================================
-- 5. TABELA DE DÉBITOS TÉCNICOS
-- =====================================================

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
    user_id BIGINT NOT NULL,
    CONSTRAINT fk_tech_debts_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Tabelas auxiliares para relacionamentos many-to-many
CREATE TABLE tech_debt_tipos (
    tech_debt_id BIGINT NOT NULL,
    tipos VARCHAR(20),
    CONSTRAINT fk_tech_debt_tipos FOREIGN KEY (tech_debt_id) REFERENCES tech_debts(id) ON DELETE CASCADE
);

CREATE TABLE tech_debt_tags (
    tech_debt_id BIGINT NOT NULL,
    tags VARCHAR(100),
    CONSTRAINT fk_tech_debt_tags FOREIGN KEY (tech_debt_id) REFERENCES tech_debts(id) ON DELETE CASCADE
);

CREATE INDEX idx_tech_debts_user_id ON tech_debts(user_id);
CREATE INDEX idx_tech_debts_status ON tech_debts(status);
CREATE INDEX idx_tech_debts_prioridade ON tech_debts(prioridade);
CREATE INDEX idx_tech_debts_data_criacao ON tech_debts(data_criacao);
CREATE INDEX idx_tech_debts_task_number ON tech_debts(task_number);

-- =====================================================
-- 6. TABELA DE RELATÓRIOS DIÁRIOS
-- =====================================================

CREATE TABLE daily_reports (
    id BIGSERIAL PRIMARY KEY,
    report_date DATE NOT NULL,
    yesterday_tasks VARCHAR(1000) NOT NULL,
    today_tasks VARCHAR(1000) NOT NULL,
    impediments VARCHAR(1000),
    remote_work BOOLEAN DEFAULT false,
    user_id BIGINT NOT NULL,
    CONSTRAINT fk_daily_report_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_daily_report_user_date ON daily_reports(user_id, report_date);

-- =====================================================
-- DADOS INICIAIS (OPCIONAL)
-- =====================================================

-- Usuário administrador padrão (senha: admin123)
INSERT INTO users (email, name, password, role, enabled) 
VALUES ('admin@devportal.com', 'Administrador', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9tYLD7q5wjKlF6G', 'ADMIN', true);

-- Usuário sistema para webhooks
INSERT INTO users (email, name, password, role, enabled) 
VALUES ('system@webhook.internal', 'Sistema Webhook', 'N/A', 'ADMIN', true);