#!/bin/bash

echo "🧪 Testando migração para Liquibase..."

# Verificar se o banco está rodando
echo "📊 Verificando status do banco..."
docker ps | grep postgres || {
    echo "❌ Banco não está rodando. Iniciando..."
    docker compose up -d db
    sleep 5
}

# Verificar estrutura dos changesets
echo "📋 Verificando estrutura dos changesets..."
find src/main/resources/db/changelog -name "*.xml" | sort

# Verificar se não há referências ao Flyway
echo "🔍 Verificando se não há referências ao Flyway..."
if grep -r -i "flyway" src/ pom.xml; then
    echo "⚠️  Ainda há referências ao Flyway!"
    exit 1
else
    echo "✅ Nenhuma referência ao Flyway encontrada"
fi

# Verificar dependências no pom.xml
echo "📦 Verificando dependências..."
if grep -q "liquibase-core" pom.xml; then
    echo "✅ Dependência do Liquibase encontrada"
else
    echo "❌ Dependência do Liquibase não encontrada!"
    exit 1
fi

if grep -q "flyway" pom.xml; then
    echo "❌ Ainda há dependências do Flyway!"
    exit 1
else
    echo "✅ Nenhuma dependência do Flyway encontrada"
fi

echo "🎉 Migração para Liquibase concluída com sucesso!"
echo "🚀 Agora você pode iniciar a aplicação normalmente"