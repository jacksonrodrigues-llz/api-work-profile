--liquibase formatted sql
--changeset system:009-create-daily-reports-table

-- Criação da tabela de relatórios diários
CREATE TABLE IF NOT EXISTS daily_reports (
    id BIGSERIAL PRIMARY KEY NOT NULL,
    report_date DATE NOT NULL,
    yesterday_tasks VARCHAR(1000) NOT NULL,
    today_tasks VARCHAR(1000) NOT NULL,
    impediments VARCHAR(1000),
    remote_work BOOLEAN DEFAULT FALSE,
    user_id BIGINT NOT NULL,
    CONSTRAINT fk_daily_report_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Criação de índice composto para usuário e data
CREATE INDEX IF NOT EXISTS idx_daily_report_user_date ON daily_reports(user_id, report_date);