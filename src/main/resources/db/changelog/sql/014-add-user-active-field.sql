-- liquibase formatted sql

-- changeset system:add-user-active-field

-- Adicionar campo active faltante na tabela users
ALTER TABLE users ADD COLUMN IF NOT EXISTS active BOOLEAN DEFAULT TRUE;