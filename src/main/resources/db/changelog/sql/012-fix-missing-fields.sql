-- liquibase formatted sql

-- changeset system:fix-missing-fields-and-tables

-- Corrigir tabela users - adicionar campos faltantes
ALTER TABLE users ADD COLUMN IF NOT EXISTS active BOOLEAN DEFAULT TRUE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS avatar_url VARCHAR(500);
ALTER TABLE users ADD COLUMN IF NOT EXISTS last_login TIMESTAMP;
ALTER TABLE users ADD COLUMN IF NOT EXISTS category VARCHAR(50);

-- Corrigir tabela activities - adicionar campos faltantes
ALTER TABLE activities ADD COLUMN IF NOT EXISTS task_type VARCHAR(20) DEFAULT 'CORE';
ALTER TABLE activities ADD COLUMN IF NOT EXISTS task_number VARCHAR(100);
ALTER TABLE activities ADD COLUMN IF NOT EXISTS task_url TEXT;

-- Criar tabela goals se não existir
CREATE TABLE IF NOT EXISTS goals (
    id BIGSERIAL PRIMARY KEY NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    target_date DATE,
    progress_percentage INTEGER DEFAULT 0,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    category VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    user_id BIGINT NOT NULL
);

-- Adicionar FK para goals
ALTER TABLE goals DROP CONSTRAINT IF EXISTS fk_goals_user;
ALTER TABLE goals ADD CONSTRAINT fk_goals_user FOREIGN KEY (user_id) REFERENCES users(id);

-- Criar tabela achievements se não existir
CREATE TABLE IF NOT EXISTS achievements (
    id BIGSERIAL PRIMARY KEY NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    type VARCHAR(50),
    impact VARCHAR(500),
    recognition VARCHAR(500),
    achieved_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    user_id BIGINT NOT NULL
);

-- Adicionar FK para achievements
ALTER TABLE achievements DROP CONSTRAINT IF EXISTS fk_achievements_user;
ALTER TABLE achievements ADD CONSTRAINT fk_achievements_user FOREIGN KEY (user_id) REFERENCES users(id);

-- Criar tabela daily_reports se não existir
CREATE TABLE IF NOT EXISTS daily_reports (
    id BIGSERIAL PRIMARY KEY NOT NULL,
    report_date DATE NOT NULL,
    yesterday_tasks VARCHAR(1000) NOT NULL,
    today_tasks VARCHAR(1000) NOT NULL,
    impediments VARCHAR(1000),
    remote_work BOOLEAN,
    user_id BIGINT NOT NULL
);

-- Adicionar FK para daily_reports
ALTER TABLE daily_reports DROP CONSTRAINT IF EXISTS fk_daily_reports_user;
ALTER TABLE daily_reports ADD CONSTRAINT fk_daily_reports_user FOREIGN KEY (user_id) REFERENCES users(id);

-- Criar tabela board_columns se não existir
CREATE TABLE IF NOT EXISTS board_columns (
    id BIGSERIAL PRIMARY KEY NOT NULL,
    name VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL,
    position INTEGER NOT NULL,
    icon_class VARCHAR(100),
    color_class VARCHAR(100),
    deletable BOOLEAN DEFAULT TRUE,
    user_id BIGINT
);

-- Adicionar FK para board_columns
ALTER TABLE board_columns DROP CONSTRAINT IF EXISTS fk_board_columns_user;
ALTER TABLE board_columns ADD CONSTRAINT fk_board_columns_user FOREIGN KEY (user_id) REFERENCES users(id);

-- Criar índices se não existirem
CREATE INDEX IF NOT EXISTS idx_goals_user_id ON goals(user_id);
CREATE INDEX IF NOT EXISTS idx_goals_status ON goals(status);
CREATE INDEX IF NOT EXISTS idx_achievements_user_id ON achievements(user_id);
CREATE INDEX IF NOT EXISTS idx_achievements_type ON achievements(type);
CREATE INDEX IF NOT EXISTS idx_daily_reports_user_id ON daily_reports(user_id);
CREATE INDEX IF NOT EXISTS idx_daily_reports_date ON daily_reports(report_date);
CREATE INDEX IF NOT EXISTS idx_board_columns_user_id ON board_columns(user_id);
CREATE INDEX IF NOT EXISTS idx_board_columns_position ON board_columns(position);