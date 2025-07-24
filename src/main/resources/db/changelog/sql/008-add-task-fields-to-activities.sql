--liquibase formatted sql
--changeset system:008-add-task-fields-to-activities

-- Adiciona campos de tarefa à tabela de atividades
ALTER TABLE activities ADD COLUMN IF NOT EXISTS task_number VARCHAR(100);
ALTER TABLE activities ADD COLUMN IF NOT EXISTS task_url TEXT;

-- Cria índice para o número da tarefa
CREATE INDEX IF NOT EXISTS idx_activities_task_number ON activities(task_number);