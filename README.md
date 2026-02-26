# Sales Insight System

API REST para análise inteligente de calls de vendas. Faz upload de áudios, transcreve automaticamente e gera insights estratégicos com IA.

## 🚀 Funcionalidades

- Upload de áudios de reuniões de vendas
- Transcrição automática via **AssemblyAI**
- Geração de insights com **Google Gemini**: resumo, sentimento, pontos de ação e próximos passos
- Notificação por email quando os insights estão prontos
- Cache de insights com **Redis**
- Pipeline assíncrono com **RabbitMQ**
- Autenticação via **JWT**

## 🏗️ Arquitetura
```
Upload → RabbitMQ → Transcrição (AssemblyAI) → RabbitMQ → Insights (Gemini) → RabbitMQ → Email
```

Pipeline 100% assíncrono. Cada etapa é independente com Dead Letter Queue para tratamento de falhas.

## 🧠 Decisões de Arquitetura

### Arquitetura modular em camadas, não Clean Architecture
O projeto é organizado em módulos por domínio (`meeting`, `user`, `notification`) com camadas internas (controller → service → repository). Clean Architecture foi considerada mas descartada: o domínio não tem complexidade suficiente para justificar 5 camadas com mapeamentos entre DTOs em cada fronteira. A estrutura modular permite que cada módulo vire um microsserviço no futuro sem grandes refatorações.

### Consumers separados para transcrição e insights
`TranscriptionConsumer` e `InsightConsumer` são classes independentes que escutam filas diferentes. Isso porque transcrição (AssemblyAI) e geração de insights (Gemini) têm latências, falhas e políticas de retry completamente distintas. Se fossem no mesmo consumer, uma falha na IA bloquearia a transcrição. Cada etapa tem sua própria DLQ.

### FileStorageService como interface
O armazenamento de arquivos é abstraído por uma interface com implementação local (`LocalFileStorageService`). Hoje salva em disco, amanhã pode ser trocado por S3 via `@Profile("prod")` sem alterar nenhuma regra de negócio. Nenhum service conhece detalhes de onde o arquivo é salvo.

### Pipeline assíncrono com RabbitMQ
A transcrição de um áudio pode levar de 5 a 30 segundos. Retornar isso de forma síncrona no HTTP travaria a thread e degradaria a experiência. Com o pipeline assíncrono, o endpoint retorna `202 Accepted` imediatamente e o processamento acontece em background. O usuário é notificado por email quando tudo estiver pronto.

### Domínio rico com invariantes
A entidade `Meeting` protege seu próprio estado. Métodos como `markAsProcessing()`, `markAsProcessed()` e `addInsight()` lançam `IllegalStateException` para transições inválidas. Isso garante que nenhuma parte do sistema consiga colocar uma `Meeting` em um estado inconsistente — a regra vive no domínio, não espalhada pelos services.

### Insight não referencia User diretamente
`Insight` pertence a `Meeting`, que pertence a `User`. A navegação `insight → meeting → user` é suficiente para todas as operações necessárias. Referenciar `User` diretamente no `Insight` criaria acoplamento desnecessário entre módulos.

## 🛠️ Tecnologias

| Tecnologia | Uso |
|---|---|
| Java 21 + Spring Boot 3 | Core da aplicação |
| PostgreSQL 16 | Persistência |
| Redis 7 | Cache de insights |
| RabbitMQ 3 | Mensageria assíncrona |
| AssemblyAI | Transcrição de áudio |
| Google Gemini | Geração de insights com IA |
| Flyway | Migrations de banco |
| Testcontainers | Testes de integração |
| Docker | Infraestrutura local |

## ▶️ Como rodar

### Pré-requisitos

- Java 21
- Docker Desktop
- Maven

### 1. Clone o repositório
```bash
git clone https://github.com/ceglauskis/sales-insight-system.git
cd sales-insight
```

### 2. Configure as variáveis de ambiente

Crie um arquivo `.env` na raiz do projeto:
```env
JWT_SECRET=seu-secret-com-pelo-menos-32-caracteres
ASSEMBLYAI_API_KEY=sua-chave-assemblyai
GEMINI_API_KEY=sua-chave-gemini
MAIL_USERNAME=seu-email@gmail.com
MAIL_PASSWORD=sua-app-password-gmail
```

### 3. Suba a infraestrutura
```bash
docker compose up -d
```

### 4. Configure as variáveis no IntelliJ

Em **Run Configurations → Environment Variables**, aponte para o arquivo `.env`.

### 5. Rode a aplicação
```bash
./mvnw spring-boot:run
```

A API estará disponível em `http://localhost:8080`.

## 📡 Endpoints

### Autenticação
```
POST /auth/register   # Criar conta
POST /auth/login      # Login
```

### Meetings
```
POST   /meetings                    # Upload de áudio (multipart/form-data)
GET    /meetings/{id}               # Buscar meeting
GET    /meetings/{id}/insights      # Buscar insights gerados
```

## 🔄 Fluxo completo

1. Usuário faz `POST /meetings` com título e arquivo de áudio
2. Meeting é criada com status `CREATED` → evento publicado no RabbitMQ
3. `TranscriptionConsumer` recebe o evento → envia áudio para o AssemblyAI
4. Após transcrição → status `PROCESSING` → evento publicado
5. `InsightConsumer` recebe o evento → envia transcrição para o Gemini
6. Gemini retorna summary, sentiment, action points e next steps
7. Meeting atualizada para `PROCESSED` → evento publicado
8. `NotificationConsumer` envia email para o dono da meeting
9. Usuário consulta `GET /meetings/{id}/insights` → retorna do cache Redis

## 🧪 Testes
```bash
./mvnw test
```

- **Testes unitários**: invariantes de domínio da entidade `Meeting` — transições de status, regras de negócio e proteção contra estados inválidos
- **Testes de integração**: fluxo completo de autenticação com banco real via Testcontainers — register, login, email duplicado e credenciais inválidas

## 📁 Estrutura do projeto
```
src/main/java/com/salesinsight/
├── meeting/          # Módulo de meetings e insights
├── user/             # Módulo de usuários e autenticação
├── notification/     # Módulo de notificações por email
├── infra/            # Infraestrutura (security, messaging, AI, storage, cache)
└── shared/           # Componentes compartilhados (exception handler, MDC)
```
