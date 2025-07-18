#!/bin/bash

echo "🧹 Limpando banco para nova migração com Liquibase..."

# Parar containers
docker compose down

# Remover volumes (dados do banco)
docker compose down -v

# Remover tabelas do Flyway se existirem
docker compose up -d db
sleep 5

docker exec -i api-work-profile-db-1 psql -U postgres -d careertracker << 'EOF' 2>/dev/null || true
DROP TABLE IF EXISTS flyway_schema_history CASCADE;
DROP TABLE IF EXISTS tech_debt_tags CASCADE;
DROP TABLE IF EXISTS tech_debt_tipos CASCADE;
DROP TABLE IF EXISTS tech_debts CASCADE;
DROP TABLE IF EXISTS achievements CASCADE;
DROP TABLE IF EXISTS goals CASCADE;
DROP TABLE IF EXISTS activities CASCADE;
DROP TABLE IF EXISTS users CASCADE;
EOF

echo "✅ Banco limpo! Agora o Liquibase criará as tabelas do zero."
echo "🚀 Execute: docker compose up --build"