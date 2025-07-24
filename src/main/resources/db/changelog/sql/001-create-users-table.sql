--liquibase formatted sql
--changeset system:001-create-users-table

-- Verifica se a tabela não existe antes de criar
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    github_username VARCHAR(255),
    github_token VARCHAR(500),
    company VARCHAR(255),
    position VARCHAR(255),
    profile_photo VARCHAR(500),
    avatar VARCHAR(500),
    password VARCHAR(255),
    enabled BOOLEAN DEFAULT TRUE,
    role VARCHAR(20) DEFAULT 'USER',
    password_reset_token VARCHAR(255),
    token_expiration TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Criação de índices
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_github_username ON users(github_username);