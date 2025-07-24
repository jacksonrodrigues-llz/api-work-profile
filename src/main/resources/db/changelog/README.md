# Estrutura de Migrações Liquibase

Este projeto utiliza o Liquibase para gerenciar migrações de banco de dados usando scripts SQL.

## Estrutura de Arquivos

- `db.changelog-master.xml`: Arquivo principal que inclui todos os scripts SQL
- `sql/`: Diretório contendo todos os scripts SQL de migração
  - `001-create-users-table.sql`: Criação da tabela de usuários
  - `002-create-activities-table.sql`: Criação da tabela de atividades
  - `003-create-goals-table.sql`: Criação da tabela de metas
  - `004-create-achievements-table.sql`: Criação da tabela de conquistas
  - `005-create-tech-debts-table.sql`: Criação da tabela de débitos técnicos
  - `006-add-user-category.sql`: Adição da coluna de categoria à tabela de usuários
  - `007-add-tech-debts-constraints.sql`: Adição de restrições à tabela de débitos técnicos
  - `008-add-task-fields-to-activities.sql`: Adição de campos de tarefa à tabela de atividades
  - `009-create-daily-reports-table.sql`: Criação da tabela de relatórios diários
  - `010-create-board-columns-table.sql`: Criação da tabela de colunas do quadro

## Como Adicionar Novas Migrações

Para adicionar uma nova migração:

1. Crie um novo arquivo SQL no diretório `sql/` seguindo o padrão de nomenclatura `NNN-descricao-da-migracao.sql`
2. Adicione o cabeçalho do Liquibase no início do arquivo:

```sql
--liquibase formatted sql
--changeset autor:id-da-migracao

-- Seu SQL aqui
```

3. O Liquibase detectará automaticamente o novo arquivo e o executará na próxima inicialização da aplicação.

## Formato dos Scripts SQL

Cada script SQL deve seguir o formato:

```sql
--liquibase formatted sql
--changeset autor:id-da-migracao

-- SQL para criar/alterar tabelas
CREATE TABLE IF NOT EXISTS exemplo (
    id BIGSERIAL PRIMARY KEY NOT NULL,
    nome VARCHAR(255) NOT NULL
);

-- Adicionar índices, chaves estrangeiras, etc.
CREATE INDEX IF NOT EXISTS idx_exemplo_nome ON exemplo(nome);

-- Para adicionar constraints com segurança (removendo primeiro se existir)
--rollback ALTER TABLE exemplo DROP CONSTRAINT IF EXISTS fk_exemplo_referencia;
ALTER TABLE exemplo DROP CONSTRAINT IF EXISTS fk_exemplo_referencia;
ALTER TABLE exemplo ADD CONSTRAINT fk_exemplo_referencia FOREIGN KEY (referencia_id) REFERENCES referencia(id);
```

### Tratamento de Constraints

Para evitar erros ao adicionar constraints que já existem, sempre use o padrão:

```sql
-- Primeiro remova a constraint se ela existir
ALTER TABLE tabela DROP CONSTRAINT IF EXISTS nome_constraint;
-- Depois adicione a constraint
ALTER TABLE tabela ADD CONSTRAINT nome_constraint ...
```

## Execução Manual

Para executar as migrações manualmente:

```bash
mvn liquibase:update
```

Para reverter a última migração:

```bash
mvn liquibase:rollback -Dliquibase.rollbackCount=1
```