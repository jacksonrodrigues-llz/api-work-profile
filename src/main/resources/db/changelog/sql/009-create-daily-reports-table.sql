-- 009-create-daily-reports-table.sql
-- Criação da tabela de relatórios diários

CREATE TABLE daily_reports (
    id BIGSERIAL PRIMARY KEY,
    report_date DATE NOT NULL,
    yesterday_tasks VARCHAR(1000) NOT NULL,
    today_tasks VARCHAR(1000) NOT NULL,
    impediments VARCHAR(1000),
    remote_work BOOLEAN DEFAULT false,
    user_id BIGINT NOT NULL
);

-- Foreign Key
ALTER TABLE daily_reports 
ADD CONSTRAINT fk_daily_report_user 
FOREIGN KEY (user_id) REFERENCES users(id);

-- Índice composto para otimização
CREATE INDEX idx_daily_report_user_date ON daily_reports(user_id, report_date);