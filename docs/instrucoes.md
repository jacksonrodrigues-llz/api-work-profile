Funcionalidades Principais:
Cadastro de Débito Técnico
Desenvolver um sistema de cadastro com três métodos de entrada:
1. Formulário manual com campos:
    - Problema (texto)
    - Descrição (textarea)
    - Prioridade: 1 – Crítico 2 – Alto 3 – Médio 4 – Baixo
    - Tipos (multi-select: Backend, Frontend, UI/UX, INFRA, Negócio)
    - Tags (input com sugestões)
    - Status (default: todo, in_progress, pause, cancelled, test, deploy, done)
    - Data de Criação (auto-preenchido)
    - Por Criado por (usuário logado ou extraído do JSON)
    - Aba para inserir o debito técnico no formato JSON:
    - Webhook de entrada (Slack ou ferramenta externa)
      Endpoint exposto como /api/techdebt/import Aceita payload como:
    ```json
   {
    "problema": "Classe com responsabilidade duplicada",
    "descricao": "A classe XYZ está tratando lógica de negócio e acesso a dados",
    "prioridade": 2,
    "tipos": ["Backend", "Negócio"],
    "tags": ["#refatoracao"],
    "status": "todo",
    "dataCriacao": "2025-07-10",
    "criadoPor": "jackson.r"
    }

2. Upload via JSON:
    - Criar endpoint POST /api/tech-debts/upload/json
    - Validar schema conforme exemplo fornecido
    - Processar em lote

3. Upload via Planilha (CSV/Excel):
    - Criar endpoint POST /api/tech-debts/upload/sheet
    - Implementar pré-visualização com mapeamento de colunas
    - Suportar CSV e Excel

Dashboard de Acompanhamento
Implementar um dashboard com:
- Tabela dinâmica com:
    * Paginação
    * Filtros combináveis (tipo, status, prioridade, responsável, tags)
    * Ordenação por campos relevantes
    * Exportação para CSV

- Visualizações gráficas:
    * Gráfico de pizza: débitos por status
    * Gráfico de barras: tempo médio em aberto por prioridade
    * Gráfico de dispersão: distribuição por tipo
    * Gráfico de linha: tendência de resolução (30 dias)

- Campos a exibir:
    * Problema (link para detalhes)
    * Criado por
    * Data criação/resolução
    * Dias em aberto (calculado)
    * Status (com badge colorido)
    * Tipo
    * Tags
    * Prioridade (com ícone indicativo)

Regras de Negócio
Implementar as seguintes regras:
- Prioridades:
    * 1 (Crítico): estilo vermelho, ícone de emergência
    * 2 (Alto): estilo laranja, ícone de alerta
    * 3 (Médio): estilo amarelo, ícone de atenção
    * 4 (Baixo): estilo azul, ícone de informação

- Fluxo de status:
    * Validações de transição (ex: todo → in_progress → test → deploy → done)
    * Notificações para mudanças de status críticos

- Cálculos automáticos:
    * Tempo em aberto (dias)
    * Velocidade de resolução (média móvel)

