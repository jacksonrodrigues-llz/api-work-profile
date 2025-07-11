# Portal de Progresso Profissional

Um sistema completo para gerenciar e acompanhar seu crescimento profissional dentro da empresa, funcionando como um "currículo vivo" com métricas de produtividade e integração com GitHub.

## 🚀 Funcionalidades

### ✅ Implementadas
- **Autenticação OAuth2 com GitHub**
- **Dashboard com métricas em tempo real**
- **Gestão completa de atividades** (CRUD)
- **Sistema de prioridades e status**
- **Interface responsiva com Bootstrap 5**
- **Integração básica com GitHub API**
- **Gestão de metas SMART** (CRUD completo)
- **Sistema de progresso visual** (barras de progresso)
- **Registro de conquistas** (timeline de marcos)
- **Exportação para PDF** (relatórios profissionais)
- **Gráficos avançados** (Chart.js analytics)
- **Relatórios mensais** (métricas automatizadas)

### 🔄 Em Desenvolvimento
- Comparativos de performance
- Sincronização automática GitHub
- Notificações por email

## 🛠 Stack Tecnológica

- **Backend**: Java 24, Spring Boot 3.5.3, Spring Security, Spring Data JPA
- **Frontend**: Thymeleaf, Bootstrap 5, Font Awesome, Chart.js
- **Banco**: PostgreSQL (produção), H2 (testes)
- **Integração**: GitHub API via OpenFeign
- **Build**: Maven

## 🏃‍♂️ Como Executar

### Pré-requisitos
- Java 21+
- Maven 3.6+
- Docker e Docker Compose
- Conta GitHub (para OAuth)

### Configuração GitHub OAuth

1. Acesse [GitHub Developer Settings](https://github.com/settings/developers)
2. Crie uma nova OAuth App:
   - **Application name**: Portal Profissional
   - **Homepage URL**: `http://localhost:8098`
   - **Authorization callback URL**: `http://localhost:8098/login/oauth2/code/github`
3. Copie o Client ID e Client Secret

### Executando com Docker

```bash
# Clone o repositório
git clone <seu-repositorio>
cd api-work-profile

# Configure as variáveis de ambiente
cp .env .env
# Edite o arquivo .env com suas credenciais GitHub

# Execute com Docker Compose
docker compose up -d

# Build da aplicação
mvn clean package -DskipTests

# Execute a aplicação
docker-compose up --build
```

### Executando Localmente

```bash
# Inicie apenas o PostgreSQL (porta 54322)
docker compose up db -d

# Configure as variáveis de ambiente
export GITHUB_CLIENT_ID=seu_client_id
export GITHUB_CLIENT_SECRET=seu_client_secret

# Execute a aplicação
mvn spring-boot:run
```

A aplicação estará disponível em: `http://localhost:8098`

## 📊 Funcionalidades Detalhadas

### Dashboard
- Métricas de atividades concluídas
- Contagem de Pull Requests do GitHub
- Metas ativas e concluídas
- Conquistas registradas
- Ações rápidas para criar novos itens

### Gestão de Atividades
- **CRUD completo** de atividades
- **Sistema de prioridades**: Low, Medium, High, Urgent
- **Status de acompanhamento**: TODO, IN_PROGRESS, DONE, CANCELLED
- **Controle de tempo**: horas estimadas vs reais
- **Categorização por projeto**
- **Tags de habilidades utilizadas**

### Integração GitHub
- Login via OAuth2
- Sincronização automática de dados do usuário
- Contagem de Pull Requests
- Preparado para métricas avançadas

## 🎨 Design System

### Bootstrap 5
- **Componentes**: Cards, Forms, Navigation, Buttons
- **Grid System**: Layout responsivo
- **Utilities**: Spacing, Colors, Typography

### Paleta de Cores
- **Primary**: #667eea (Azul)
- **Success**: #28a745 (Verde)
- **Warning**: #ffc107 (Amarelo)
- **Danger**: #dc3545 (Vermelho)
- **Info**: #17a2b8 (Ciano)

### Ícones
- **Font Awesome 6**: Ícones consistentes em toda aplicação
- **GitHub**: Integração visual com branding

## 📁 Estrutura do Projeto

```
src/main/java/api/work/profile/
├── entity/          # Entidades JPA
├── repository/      # Repositórios Spring Data
├── service/         # Lógica de negócio
├── controller/      # Controllers MVC
├── config/          # Configurações
└── dto/             # Data Transfer Objects

src/main/resources/
├── templates/       # Templates Thymeleaf
│   ├── activities/  # Templates de atividades
│   └── layout.html  # Layout base
├── static/          # Recursos estáticos
└── application.yml  # Configurações
```

## 🔐 Segurança

- **OAuth2 com GitHub**: Autenticação segura
- **Spring Security**: Proteção de rotas
- **CSRF Protection**: Proteção contra ataques
- **Session Management**: Gerenciamento de sessões

## 📈 Próximos Passos

### Fase 2 - Metas e Conquistas ✅
- [x] CRUD de metas SMART
- [x] Sistema de progresso visual
- [x] Registro de conquistas
- [x] Timeline de marcos

### Fase 3 - Relatórios e Analytics ✅
- [x] Exportação PDF com Flying Saucer
- [x] Gráficos avançados com Chart.js
- [x] Relatórios mensais automatizados
- [ ] Comparativos de performance

### Fase 4 - Integrações Avançadas
- [ ] Sync completo GitHub (commits, issues)
- [ ] Integração Jira/Azure Boards
- [ ] Notificações por email
- [ ] API REST para mobile

## 🤝 Contribuição

1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

## 📝 Licença

Este projeto está sob a licença MIT. Veja o arquivo `LICENSE` para mais detalhes.

---

**Desenvolvido com ❤️ para profissionais que querem acompanhar seu crescimento**