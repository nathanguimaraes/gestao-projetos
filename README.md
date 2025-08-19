# Sistema de Gestão de Projetos

Sistema de gestão de projetos desenvolvido em Java Spring Boot para controle e acompanhamento de projetos empresariais.

## 🚀 Tecnologias Utilizadas

### Backend
- **Java 21** - Linguagem de programação
- **Spring Boot 3.5.4** - Framework principal
- **Spring Data JPA** - Persistência de dados
- **Spring Security** - Segurança da aplicação
- **Spring Validation** - Validação de dados
- **PostgreSQL 16** - Banco de dados
- **Flyway** - Migração de banco de dados
- **Lombok** - Redução de código boilerplate
- **Maven** - Gerenciamento de dependências

### Documentação e Testes
- **SpringDoc OpenAPI (Swagger)** - Documentação da API
- **JUnit 5** - Framework de testes
- **Mockito** - Framework de mock para testes

### Containerização
- **Docker** - Containerização da aplicação
- **Docker Compose** - Orquestração de containers

## 📋 Regras de Negócio

### Entidades Principais

#### Projeto (Project)
- **Atributos:**
  - ID (auto-gerado)
  - Nome (obrigatório)
  - Data de Início (obrigatório)
  - Previsão de Término (obrigatório)
  - Data Real de Término (opcional)
  - Orçamento Total (obrigatório, positivo)
  - Descrição (obrigatório)
  - Gerente (obrigatório, referência a Member)
  - Status (enum)
  - Lista de Membros (obrigatório, mínimo 1)

- **Cálculo de Risco:**
  - **Alto Risco:** Orçamento > R$ 500.000 OU duração > 6 meses
  - **Médio Risco:** Orçamento > R$ 100.000 OU duração entre 3-6 meses
  - **Baixo Risco:** Orçamento ≤ R$ 100.000 E duração ≤ 3 meses

#### Membro (Member)
- **Atributos:**
  - ID (auto-gerado)
  - Nome (obrigatório)
  - Cargo (obrigatório)

#### Status do Projeto (StatusProjeto)
- **Estados possíveis:**
  - `EM_ANALISE` - Projeto em análise inicial
  - `ANALISE_REALIZADA` - Análise concluída
  - `ANALISE_APROVADA` - Análise aprovada
  - `INICIADO` - Projeto iniciado
  - `PLANEJADO` - Projeto planejado
  - `EM_ANDAMENTO` - Projeto em execução
  - `ENCERRADO` - Projeto finalizado
  - `CANCELADO` - Projeto cancelado

### Regras de Validação

1. **Criação de Projeto:**
   - Deve ter pelo menos 1 membro
   - Gerente deve existir no sistema
   - Todos os membros devem existir no sistema
   - Orçamento deve ser positivo
   - Datas devem ser válidas

2. **Alteração de Status:**
   - Seguência lógica de status deve ser respeitada
   - Projetos encerrados/cancelados não podem ser alterados

3. **Gestão de Membros:**
   - Membros podem ser adicionados/removidos de projetos ativos
   - Gerente não pode ser removido do projeto

## 🏗️ Arquitetura

### Estrutura do Projeto
```
src/main/java/com/planejao/gestao_projetos/
├── controller/          # Controladores REST
├── service/            # Lógica de negócio
├── repository/         # Acesso a dados
├── domain/            # Entidades JPA
├── dto/               # Objetos de transferência
├── config/            # Configurações
└── exception/         # Tratamento de exceções
```

### Padrões Utilizados
- **MVC (Model-View-Controller)**
- **Repository Pattern**
- **DTO Pattern**
- **Service Layer Pattern**

## 🚀 Como Executar

### Pré-requisitos
- Java 21
- Maven 3.9+
- Docker e Docker Compose
- PostgreSQL 16 (opcional, se não usar Docker)

### Execução com Docker (Recomendado)

1. **Clone o repositório:**
```bash
git clone <url-do-repositorio>
cd gestao-projetos
```

2. **Execute com Docker Compose:**
```bash
docker-compose up -d
```

3. **Acesse a aplicação:**
- **API:** http://localhost:8081
- **Swagger UI:** http://localhost:8081/swagger-ui.html

### Execução Local

1. **Configure o banco de dados:**
```bash
# Execute o PostgreSQL localmente ou via Docker
docker run -d --name postgres_db -e POSTGRES_PASSWORD=root -p 5432:5432 postgres:16
```

2. **Configure as variáveis de ambiente:**
```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/postgres
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=root
```

3. **Execute a aplicação:**
```bash
mvn spring-boot:run
```

## 📚 API Endpoints

### Projetos (`/projects`)

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/projects` | Lista projetos com paginação e filtro por status |
| GET | `/projects/{id}` | Obtém projeto por ID |
| POST | `/projects` | Cria novo projeto |
| PUT | `/projects/{id}` | Atualiza projeto |
| DELETE | `/projects/{id}` | Exclui projeto |
| PATCH | `/projects/{id}/status` | Altera status do projeto |
| POST | `/projects/{id}/members/{memberId}` | Adiciona membro ao projeto |
| DELETE | `/projects/{id}/members/{memberId}` | Remove membro do projeto |
| GET | `/projects/report` | Gera relatório do portfólio |

### Membros (`/members`)

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/members` | Lista membros com paginação |
| GET | `/members/{id}` | Obtém membro por ID |
| POST | `/members` | Cria novo membro |
| PUT | `/members/{id}` | Atualiza membro |
| DELETE | `/members/{id}` | Exclui membro |

## 📊 Exemplos de Uso

### Criar um Projeto
```bash
curl -X POST http://localhost:8081/projects \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "Sistema de E-commerce",
    "dataInicio": "2024-01-15",
    "previsaoTermino": "2024-06-15",
    "orcamentoTotal": 150000.00,
    "descricao": "Desenvolvimento de sistema de e-commerce completo",
    "gerenteId": 1,
    "membros": [1, 2, 3]
  }'
```

### Alterar Status do Projeto
```bash
curl -X PATCH http://localhost:8081/projects/1/status \
  -H "Content-Type: application/json" \
  -d '{"status": "EM_ANDAMENTO"}'
```

### Gerar Relatório
```bash
curl -X GET http://localhost:8081/projects/report
```

## 🔧 Configurações

### Variáveis de Ambiente
```properties
# Banco de Dados
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/postgres
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=root

# JPA/Hibernate
SPRING_JPA_HIBERNATE_DDL_AUTO=none
SPRING_JPA_SHOW_SQL=true

# Flyway
SPRING_FLYWAY_ENABLED=true
SPRING_FLYWAY_CLEAN_ON_STARTUP=true
```

### Portas Utilizadas
- **Aplicação:** 8081 (Docker) / 8080 (Local)
- **PostgreSQL:** 5432

## 🧪 Testes

### Executar Testes
```bash
mvn test
```

### Executar Testes com Cobertura
```bash
mvn clean test jacoco:report
```

## 📝 Migrações de Banco

As migrações são gerenciadas pelo Flyway e estão localizadas em:
```
src/main/resources/db/migration/
├── V1__create_tables.sql
└── V2__insert_example_data.sql
```

### Diagrama do Banco de Dados

![Diagrama do Banco de Dados](img-4.png)

O diagrama acima representa a estrutura do banco de dados com as seguintes tabelas:
- **members**: Armazena informações dos membros da equipe
- **projects**: Armazena dados dos projetos
- **project_membros**: Tabela de relacionamento entre projetos e membros

## 🔍 Monitoramento e Logs

- **Logs:** Configurados com SLF4J
- **Swagger UI:** Documentação interativa da API
- **Health Check:** Endpoint de saúde da aplicação

