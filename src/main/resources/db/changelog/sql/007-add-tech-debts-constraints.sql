--liquibase formatted sql
--changeset system:007-add-tech-debts-constraints

-- Adiciona restrição de verificação para a prioridade dos débitos técnicos
--rollback ALTER TABLE tech_debts DROP CONSTRAINT IF EXISTS chk_tech_debts_prioridade;
ALTER TABLE tech_debts DROP CONSTRAINT IF EXISTS chk_tech_debts_prioridade;
ALTER TABLE tech_debts ADD CONSTRAINT chk_tech_debts_prioridade CHECK (prioridade BETWEEN 1 AND 4);