--liquibase formatted sql
--changeset system:010-create-board-columns-table

-- Criação da tabela de colunas do quadro
CREATE TABLE IF NOT EXISTS board_columns (
    id BIGSERIAL PRIMARY KEY NOT NULL,
    name VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL,
    position INTEGER NOT NULL,
    icon_class VARCHAR(100),
    color_class VARCHAR(50),
    deletable BOOLEAN DEFAULT TRUE NOT NULL,
    user_id BIGINT NOT NULL
);

-- Adição da chave estrangeira (com tratamento de erro caso já exista)
--rollback ALTER TABLE board_columns DROP CONSTRAINT IF EXISTS fk_board_columns_user;
ALTER TABLE board_columns DROP CONSTRAINT IF EXISTS fk_board_columns_user;
ALTER TABLE board_columns ADD CONSTRAINT fk_board_columns_user FOREIGN KEY (user_id) REFERENCES users(id);

-- Criação de índice composto para usuário e posição
CREATE INDEX IF NOT EXISTS idx_board_columns_user_position ON board_columns(user_id, position);