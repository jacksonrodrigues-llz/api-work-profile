--liquibase formatted sql
--changeset system:006-add-user-category

-- Adiciona a coluna de categoria à tabela de usuários
ALTER TABLE users ADD COLUMN IF NOT EXISTS category VARCHAR(50);