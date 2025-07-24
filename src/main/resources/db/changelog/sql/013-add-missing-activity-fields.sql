-- liquibase formatted sql

-- changeset system:add-missing-activity-fields

-- Adicionar campo task_type faltante na tabela activities
ALTER TABLE activities ADD COLUMN IF NOT EXISTS task_type VARCHAR(20) DEFAULT 'CORE';

-- Criar índice para task_type
CREATE INDEX IF NOT EXISTS idx_activities_task_type ON activities(task_type);