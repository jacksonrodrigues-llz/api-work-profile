CREATE TABLE tech_debts (
    id BIGSERIAL PRIMARY KEY,
    problema VARCHAR(255) NOT NULL,
    descricao TEXT,
    prioridade INTEGER NOT NULL CHECK (prioridade BETWEEN 1 AND 4),
    status VARCHAR(20) DEFAULT 'TODO',
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_resolucao TIMESTAMP,
    criado_por VARCHAR(255),
    user_id BIGINT REFERENCES users(id)
);

CREATE TABLE tech_debt_tipos (
    tech_debt_id BIGINT REFERENCES tech_debts(id),
    tipos VARCHAR(20)
);

CREATE TABLE tech_debt_tags (
    tech_debt_id BIGINT REFERENCES tech_debts(id),
    tags VARCHAR(100)
);

CREATE INDEX idx_tech_debts_user_id ON tech_debts(user_id);
CREATE INDEX idx_tech_debts_status ON tech_debts(status);
CREATE INDEX idx_tech_debts_prioridade ON tech_debts(prioridade);
CREATE INDEX idx_tech_debts_data_criacao ON tech_debts(data_criacao);