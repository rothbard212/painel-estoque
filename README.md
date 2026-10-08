# 📦 Painel de Estoque

API REST de controle de estoque B2B para uma distribuidora/atacado: fornecedores, produtos, pedidos de compra com itens, entrada de estoque no recebimento do pedido, saída manual e histórico de movimentações, tudo protegido por autenticação JWT.

Quarto projeto de um portfólio de cinco aplicações, com foco em **concorrência**, **transações** e **segurança** em Spring Boot.

📄 Requisitos, regras de negócio e decisões de design: [Requisitos.md](./Requisitos.md)

---

## 📋 O que o sistema faz

- Autenticação por email e senha, com token JWT (validade de 10 horas)
- CRUD de fornecedores e produtos
- Alerta de estoque baixo, comparando a quantidade de cada produto com o **seu próprio** estoque mínimo
- Pedidos de compra com múltiplos itens, criados de forma transacional
- Recebimento de pedido: só nesse momento o estoque é incrementado (RN02)
- Saída manual de estoque, rejeitada se o saldo ficaria negativo (RN01)
- Histórico completo de movimentações por produto

---

## 🏗️ Arquitetura

```
controller/   → endpoints REST (sem regra de negócio)
service/      → regras de negócio (RN01-RN05), transações e retry
repository/   → Spring Data JPA
model/        → entidades JPA
dto/          → objetos de entrada/saída da API (records, com validação)
config/       → Spring Security + JWT (SecurityConfig, JwtUtil, JwtAuthFilter)
exception/    → exceções de domínio + tratamento centralizado (@RestControllerAdvice)
```

**Stack:** Java, Spring Boot, Spring Security, Spring Data JPA/Hibernate, PostgreSQL, JUnit 5, Mockito.

---

## 🧠 Decisões de arquitetura

- **Lock otimista (`@Version`) com retry automático** para movimentação de estoque. O padrão de uso de uma distribuidora tem baixa contenção, então não vale bloquear linhas. Em caso de conflito, o service busca o dado atualizado e tenta de novo, sem expor isso ao usuário.
- **Histórico separado do saldo**: cada entrada ou saída gera um registro em `movimentacoes_estoque`, permitindo auditar e reconstruir o saldo em qualquer ponto do tempo.
- **Preço congelado no item do pedido**: `ItemPedido` guarda o preço da compra, que pode divergir do `precoCusto` atual do produto.
- **Relacionamentos recebidos por ID**: a API recebe só `fornecedorId`/`produtoId` e o service busca a entidade real, evitando objetos parciais montados pelo Jackson.
- **Mensagem de login única** para "email inexistente" e "senha errada", para não revelar quais emails estão cadastrados (coberto por teste).
- **Segredos fora do Git**: chave JWT e senha do banco em `application-local.properties`, ignorado pelo Git.

---

## 🧪 Testes

```bash
./mvnw test
```

**23 testes**, incluindo:

- Testes unitários com Mockito para todos os services (CRUD, RN01, RN02, proteção contra receber pedido duas vezes, login).
- **Teste de concorrência real**: duas threads dão baixa no mesmo produto ao mesmo tempo contra um PostgreSQL de verdade, e o teste confirma que nenhuma atualização é perdida (100 → 40, não 70).
- Rollback transacional validado manualmente: criar pedido com produto inexistente não deixa pedido órfão no banco.

---

## 🔌 Endpoints

| Método | Rota | Descrição |
|---|---|---|
| POST | `/auth/login` | Gera o token JWT (único endpoint público) |
| GET/POST/PUT/DELETE | `/fornecedores` | CRUD de fornecedores |
| GET/POST/PUT/DELETE | `/produtos` | CRUD de produtos |
| GET | `/produtos/fornecedor/{id}` | Produtos de um fornecedor |
| GET | `/produtos/estoque-baixo` | Produtos abaixo do estoque mínimo |
| POST/GET | `/pedidos-compra` | Criar (com itens) e listar pedidos |
| POST | `/pedidos-compra/{id}/receber` | Recebe o pedido e dá entrada no estoque |
| POST | `/movimentacoes/saida` | Saída manual de estoque |
| GET | `/movimentacoes/produto/{id}` | Histórico de movimentações |

---

## ⚙️ Como rodar

```bash
# 1. Banco
CREATE DATABASE painel_estoque;
CREATE DATABASE painel_estoque_test;   -- usado pelos testes

# 2. Segredos (arquivo ignorado pelo Git)
# src/main/resources/application-local.properties
jwt.secret=<chave Base64 de 32 bytes>
spring.datasource.password=<senha do postgres>

# 3. Rodar
./mvnw spring-boot:run
```

---

## 🐛 Aprendizados de debugging

Problemas reais encontrados e resolvidos durante o desenvolvimento:

- **Referência circular no JSON** (`Produto` ↔ `Fornecedor`): a resposta chegou a 22 KB de recursão. Resolvido com `@JsonIgnore` no lado "muitos".
- **`@Version` + objetos parciais**: `"fornecedor": {"id": 1}` vindo do JSON gerava erro de entidade desacoplada. Resolvido buscando a entidade real no repository.
- **Chave JWT gerada a cada start**: invalidava todos os tokens a cada reinício. Resolvido com chave fixa por configuração.
- **Pedido órfão sem itens**: falha no meio da criação deixava dado parcial. Resolvido com `@Transactional` e confirmado no banco.

---

## 🚧 Limitações conhecidas

- Exportação de relatórios em PDF/Excel (RF09) ainda não implementada.
- Frontend em React ainda não implementado.
- Esquema do banco gerenciado por `ddl-auto=update`; em produção seria substituído por migrações (Flyway).
- Requisição sem token retorna `403` (padrão do Spring Security) em vez de `401`.
- Sem refresh de token: ao expirar, é preciso fazer login de novo.
