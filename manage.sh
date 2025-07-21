#!/bin/bash
#
# CAREER TRACKER - Script de Gerenciamento
# =======================================
#
# ATENÇÃO: Este é o ÚNICO script necessário para gerenciar a aplicação.
# Todos os outros scripts foram consolidados aqui.
#
# USO: ./manage.sh [comando]
#
# COMANDOS:
# - setup    : Configuração inicial (primeira vez apenas)
# - start    : Iniciar a aplicação
# - update   : Atualizar a aplicação (preserva o banco de dados)
# - clean    : Limpar banco de dados (CUIDADO: apaga todos os dados)
# - help     : Mostrar ajuda
#
# NOTA: Os testes foram removidos e serão implementados no futuro.
#
# ORDEM DE EXECUÇÃO:
# 1. ./manage.sh setup  (apenas na primeira vez)
# 2. ./manage.sh start  (para iniciar)
# 3. ./manage.sh update (para atualizações futuras)
#

# Cores para melhor visualização
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Função para exibir o cabeçalho
show_header() {
    echo -e "${BLUE}"
    echo "╔════════════════════════════════════════════════════════╗"
    echo "║                   CAREER TRACKER                       ║"
    echo "║            Portal de Progresso Profissional            ║"
    echo "╚════════════════════════════════════════════════════════╝"
    echo -e "${NC}"
}

# Função para exibir o menu de ajuda
show_help() {
    echo -e "${YELLOW}Uso:${NC} $0 [comando]"
    echo
    echo "Comandos disponíveis:"
    echo -e "  ${GREEN}setup${NC}    - Configuração inicial (executar apenas uma vez)"
    echo -e "  ${GREEN}start${NC}    - Iniciar a aplicação"
    echo -e "  ${GREEN}stop${NC}     - Parar a aplicação"
    echo -e "  ${GREEN}update${NC}   - Atualizar a aplicação preservando o banco de dados"
    echo -e "  ${GREEN}logs${NC}     - Visualizar logs da aplicação"
    echo -e "  ${GREEN}status${NC}   - Verificar status dos containers"
    echo -e "  ${GREEN}clean${NC}    - Limpar banco de dados (⚠️ CUIDADO: apaga todos os dados)"
    echo -e "  ${GREEN}help${NC}     - Exibir esta ajuda"
    echo
    echo -e "${YELLOW}Ordem de execução recomendada:${NC}"
    echo "1. ./manage.sh setup    (primeira vez apenas)"
    echo "2. ./manage.sh start    (iniciar a aplicação)"
    echo "3. ./manage.sh update   (para atualizações futuras)"
    echo
}

# Função para configuração inicial
setup() {
    echo -e "${BLUE}🔍 Verificando se o volume do banco de dados já existe...${NC}"
    if docker volume inspect career-tracker-data &> /dev/null; then
        echo -e "${GREEN}✅ Volume career-tracker-data já existe. Nenhuma ação necessária.${NC}"
    else
        echo -e "${YELLOW}🏗️ Criando volume externo para o banco de dados...${NC}"
        docker volume create career-tracker-data
        echo -e "${GREEN}✅ Volume career-tracker-data criado com sucesso!${NC}"
    fi
    
    echo -e "${BLUE}📝 Configuração concluída!${NC}"
    echo -e "${YELLOW}Execute agora:${NC} $0 start"
}

# Função para iniciar a aplicação
start() {
    echo -e "${BLUE}🚀 Iniciando a aplicação...${NC}"
    docker compose up -d
    echo -e "${GREEN}✅ Aplicação iniciada! Acesse http://localhost:8098${NC}"
    echo -e "${YELLOW}Para ver os logs:${NC} $0 logs"
}

# Função para parar a aplicação
stop() {
    echo -e "${BLUE}🛑 Parando a aplicação...${NC}"
    docker compose stop
    echo -e "${GREEN}✅ Aplicação parada.${NC}"
}

# Função para atualizar a aplicação
update() {
    echo -e "${BLUE}🔄 Atualizando para a versão mais recente...${NC}"

    # Verificar se estamos em um repositório git
    if [ -d ".git" ]; then
        echo -e "${YELLOW}Repositório Git detectado. Atualizando código...${NC}"
        # Salvar alterações locais se houver
        git stash -u

        # Mudar para a branch main e atualizar
        git checkout main
        git pull origin main

        # Aplicar alterações locais de volta se houver
        git stash pop || echo -e "${YELLOW}Sem alterações locais para aplicar${NC}"
    else
        echo -e "${YELLOW}⚠️ Diretório não é um repositório git. Usando arquivos locais.${NC}"
    fi

    # Parar apenas o container da aplicação, mantendo o banco de dados em execução
    echo -e "${BLUE}🛑 Parando apenas o container da aplicação...${NC}"
    docker compose stop app
    docker compose rm -f app

    # Remover apenas a imagem da aplicação
    echo -e "${BLUE}🧹 Removendo apenas a imagem da aplicação...${NC}"
    docker rmi api-work-profile:latest || true

    # Construir e iniciar apenas o container da aplicação
    echo -e "${BLUE}🏗️ Construindo e iniciando apenas a aplicação...${NC}"
    docker compose build --no-cache app
    docker compose up -d app

    echo -e "${GREEN}✅ Aplicação atualizada! Acesse http://localhost:8098${NC}"
}

# Função para visualizar logs
logs() {
    echo -e "${BLUE}📊 Exibindo logs da aplicação...${NC}"
    docker compose logs -f app
}

# Função para verificar status
status() {
    echo -e "${BLUE}📊 Status dos containers:${NC}"
    docker compose ps
}

# Função para limpar o banco de dados
clean() {
    echo -e "${RED}⚠️  ATENÇÃO: Esta operação irá apagar TODOS os dados do banco!${NC}"
    echo -e "${RED}⚠️  Todos os dados cadastrados serão perdidos permanentemente!${NC}"
    read -p "Tem certeza que deseja continuar? (digite 'SIM' para confirmar): " confirmation
    
    if [ "$confirmation" != "SIM" ]; then
        echo -e "${YELLOW}Operação cancelada.${NC}"
        return
    fi
    
    echo -e "${BLUE}🧹 Limpando banco de dados...${NC}"

    # Parar containers
    docker compose down

    # Remover volumes (dados do banco)
    docker compose down -v

    # Remover tabelas
    docker compose up -d db
    sleep 5

    docker exec -i postgres-career psql -U postgres -d careertracker << 'EOF' 2>/dev/null || true
DROP TABLE IF EXISTS databasechangelog CASCADE;
DROP TABLE IF EXISTS databasechangeloglock CASCADE;
DROP TABLE IF EXISTS daily_reports CASCADE;
DROP TABLE IF EXISTS tech_debt_tags CASCADE;
DROP TABLE IF EXISTS tech_debt_tipos CASCADE;
DROP TABLE IF EXISTS tech_debts CASCADE;
DROP TABLE IF EXISTS achievements CASCADE;
DROP TABLE IF EXISTS goals CASCADE;
DROP TABLE IF EXISTS activities CASCADE;
DROP TABLE IF EXISTS users CASCADE;
EOF

    echo -e "${GREEN}✅ Banco limpo! Agora o Liquibase criará as tabelas do zero.${NC}"
    echo -e "${YELLOW}Execute:${NC} $0 start"
}

# Verificar se foi passado um comando
if [ $# -eq 0 ]; then
    show_header
    show_help
    exit 0
fi

# Executar o comando apropriado
show_header
case "$1" in
    setup)
        setup
        ;;
    start)
        start
        ;;
    stop)
        stop
        ;;
    update)
        update
        ;;
    logs)
        logs
        ;;
    status)
        status
        ;;
    clean)
        clean
        ;;
    help)
        show_help
        ;;
    *)
        echo -e "${RED}Comando desconhecido: $1${NC}"
        show_help
        exit 1
        ;;
esac

exit 0