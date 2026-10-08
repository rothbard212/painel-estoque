# REQUISITOS.md — Painel de Administração (Controle de Estoque B2B)

---

## 1. Visão Geral

Sistema de controle de estoque B2B para uma distribuidora/atacado — gerenciando fornecedores, produtos, pedidos em lote e movimentação de estoque. Quarto projeto do portfólio, com foco em autenticação JWT, CRUD completo, filtros em tempo real e exportação de relatórios (PDF/Excel). Backend em Java/Spring Boot, frontend em React.

---

## 2. Requisitos Funcionais

| ID | Requisito |
|----|-----------|
| RF01 | O sistema deve permitir login com autenticação JWT, restringindo acesso às demais funcionalidades a usuários autenticados |
| RF02 | O sistema deve permitir CRUD completo de fornecedores (nome, CNPJ, contato) |
| RF03 | O sistema deve permitir CRUD completo de produtos (nome, SKU, fornecedor, preço de custo, quantidade em estoque, estoque mínimo) |
| RF04 | O sistema deve permitir registrar pedidos de compra (a um fornecedor), com múltiplos itens e quantidades |
| RF05 | O sistema deve permitir registrar a entrada de estoque quando um pedido de compra é recebido, atualizando a quantidade de cada produto |
| RF06 | O sistema deve permitir registrar saída de estoque (venda ou baixa manual), com validação de quantidade disponível |
| RF07 | O sistema deve exibir alerta visual para produtos com quantidade abaixo do estoque mínimo configurado |
| RF08 | O sistema deve permitir filtrar produtos e pedidos em tempo real (por fornecedor, por status, por faixa de quantidade) |
| RF09 | O sistema deve permitir exportar relatórios de estoque e movimentação em PDF e Excel |
| RF10 | O sistema deve registrar o histórico de movimentações de estoque (entrada/saída), não apenas o saldo atual |

---

## 3. Requisitos Não-Funcionais

| ID | Requisito |
|----|-----------|
| RNF01 | Toda rota da API, exceto login, deve exigir token JWT válido |
| RNF02 | Senhas de usuário nunca devem ser armazenadas em texto puro — apenas hash |
| RNF03 | Consultas de listagem (produtos, pedidos) devem suportar paginação, evitando carregar toda a tabela de uma vez conforme o volume de dados cresce |
| RNF04 | Toda alteração de quantidade em estoque deve ser atômica — duas movimentações concorrentes no mesmo produto não podem gerar saldo inconsistente |

---

## 4. Regras de Negócio

| ID | Regra |
|----|-------|
| RN01 | A quantidade em estoque de um produto nunca pode ficar negativa — uma saída que excederia o saldo disponível deve ser rejeitada |
| RN02 | Um pedido de compra só gera entrada de estoque quando marcado como "recebido" — criar o pedido, por si só, não altera o saldo |
| RN03 | O estoque mínimo é configurável por produto, não um valor global — cada produto pode ter seu próprio limiar de alerta (RF07) |
| RN04 | Toda movimentação de estoque (entrada ou saída) gera um registro no histórico (RF10), permitindo reconstruir o saldo em qualquer ponto do tempo, não apenas consultar o valor atual |
| RN05 | Concorrência em movimentação de estoque (RNF04) é tratada via lock otimista (`@Version` do JPA), não lock pessimista — ao detectar `OptimisticLockException`, o sistema busca o dado atualizado e tenta novamente automaticamente, de forma transparente para o usuário |

---

## 5. Casos de Borda

| Cenário | Comportamento Esperado | Status |
|---|---|---|
| Duas requisições tentam dar baixa no mesmo produto simultaneamente | ⬜ decidir: mesma estratégia de transação atômica do projeto anterior (Agendador), ou abordagem diferente já que agora é Spring Boot/JPA, não SQLite puro? |
| Tentativa de saída de estoque maior que o saldo disponível | Rejeitada (RN01) — retorna erro claro ao invés de permitir saldo negativo | ✅ decidido |
| Pedido de compra criado mas nunca marcado como recebido | Fica pendente indefinidamente, sem afetar o estoque (RN02) — sem expiração automática por ora | ✅ decidido |
| Produto sem estoque mínimo configurado (campo vazio) | ⬜ decidir: usa um valor padrão global, ou trata como "sem alerta configurado" e nunca dispara RF07 para esse produto? |
| Token JWT expirado durante o uso da aplicação | ⬜ decidir: como o frontend deve reagir — redirecionar para login, tentar refresh automático? |
| Exportação de relatório com volume muito grande de dados | ⬜ decidir: processamento síncrono (usuário espera) ou assíncrono (gera em background, notifica quando pronto)? |

---

## 6. Modelagem de Dados (rascunho inicial)

| Tabela | Colunas principais | Observações |
|---|---|---|
| `usuarios` | id, email, senha_hash, nome | Autenticação (RF01) |
| `fornecedores` | id, nome, cnpj, contato | |
| `produtos` | id, nome, sku, fornecedor_id, preco_custo, quantidade, estoque_minimo | |
| `pedidos_compra` | id, fornecedor_id, status, criado_em, recebido_em | `status`: pendente / recebido / cancelado |
| `itens_pedido` | id, pedido_id, produto_id, quantidade, preco_unitario | |
| `movimentacoes_estoque` | id, produto_id, tipo, quantidade, criado_em, referencia | `tipo`: entrada / saída. `referencia`: liga a um pedido, quando aplicável (RN04) |

---

## 7. Estratégia de Testes (rascunho inicial)

- Testes unitários com JUnit para as regras de negócio (RN01-RN04), especialmente a validação de saldo insuficiente e a atomicidade da movimentação de estoque
- Testes de integração para os endpoints protegidos por JWT (RNF01) — acesso sem token, com token inválido, com token expirado
- Teste de concorrência para movimentação de estoque simultânea (RNF04), reaproveitando o aprendizado do teste de threads já validado no projeto anterior (Agendador)

---

## 8. Decisões Tomadas (registro para o README/portfólio)

| Decisão | Justificativa |
|---|---|
| Domínio específico (distribuidora/atacado), não genérico | Modelar um caso de uso real e específico força decisões de negócio mais interessantes (pedido de compra, recebimento, fornecedor) do que um CRUD genérico de "produtos" |
| Histórico de movimentações separado do saldo atual (RN04) | Permite auditoria e reconstrução de saldo em qualquer ponto do tempo, prática comum em sistemas de estoque reais, em vez de apenas um contador que pode ser adulterado sem rastro |
| Estoque mínimo configurável por produto, não global (RN03) | Reflete a realidade de um catálogo diverso — um produto de giro rápido precisa de um limiar diferente de um item de baixa rotatividade |
| Autenticação (RF01) usa o email como identificador de login, sem campo de username separado | Simplifica o modelo de `Usuario` — a entidade implementa `UserDetails` do Spring Security diretamente, com `getUsername()` retornando o email cadastrado |
| `@JsonIgnore` na lista `Fornecedor.produtos`, evitando referência circular na serialização | Relacionamento bidirecional `Produto` ↔ `Fornecedor` gerava recursão infinita ao serializar em JSON (cada produto trazia o fornecedor, que trazia a lista de produtos, que trazia o fornecedor de cada um...). Ignorar o lado "muitos" na serialização resolve sem remover a navegação bidirecional em Java — consultar produtos de um fornecedor continua disponível via endpoint dedicado (`GET /produtos/fornecedor/{id}`) |
| Banco de dados: PostgreSQL, não SQLite | Mais alinhado à stack real de backend do restante do portfólio (Spring Boot/JPA), com suporte nativo mais robusto a controle de concorrência do que o `BEGIN IMMEDIATE` usado como solução alternativa no projeto anterior (Agendador, SQLite) |
| Lock otimista (`@Version`) com retry automático, em vez de lock pessimista (RN05) | Padrão de uso da distribuidora tem baixa contenção real (conflitos raros no mesmo produto), tornando o lock otimista mais performático — não bloqueia leitura/escrita de outras transações enquanto uma está em andamento. Retry automático mantém a experiência transparente para o usuário, sem expor a complexidade de concorrência na interface |
| `@JsonIgnore` na lista `produtos` de `Fornecedor`, não no `fornecedor` de `Produto` | O relacionamento bidirecional `@OneToMany`/`@ManyToOne` entre `Fornecedor` e `Produto` causava recursão infinita na serialização JSON (fornecedor → produtos → fornecedor → ...). Ignorar o lado "muitos" preserva a navegação útil (produto mostra seu fornecedor) sem quebrar a serialização; a lista de produtos de um fornecedor é consultada via endpoint dedicado (`GET /produtos/fornecedor/{id}`), não aninhada na resposta |
| `@JsonIgnore` na lista `produtos` de `Fornecedor`, não no lado `Produto.fornecedor` | Relacionamento bidirecional (`Produto.fornecedor` + `Fornecedor.produtos`) causava recursão infinita na serialização JSON (cada produto trazia o fornecedor, que trazia a lista de produtos, que trazia o fornecedor de novo...). Ignorar o lado "muitos" preserva a informação mais útil no JSON de resposta (de qual fornecedor um produto é) sem quebrar a serialização; a lista de produtos de um fornecedor é consultada separadamente via endpoint próprio |
| `@JsonIgnore` no lado `@OneToMany` de `Fornecedor.produtos`, não no `@ManyToOne` de `Produto.fornecedor` | Relacionamento bidirecional entre `Produto` e `Fornecedor` causava recursão infinita na serialização JSON (cada produto incluía seu fornecedor, que incluía a lista de produtos, que incluía os fornecedores, indefinidamente). Ignorar o lado "muitos" preserva a navegação útil (`produto.fornecedor` no JSON) e remove o ciclo, sem afetar o uso em código Java |

---

## 9. Notas de Configuração do Ambiente

- **Credencial do PostgreSQL via variável de ambiente**: `spring.datasource.password=${DB_PASSWORD}` no `application.properties`, nunca a senha em texto puro. Após `setx DB_PASSWORD "..."` no Windows, é necessário fechar e reabrir o IntelliJ por completo para que o novo valor seja reconhecido — reiniciar apenas o projeto não é suficiente
- **Sintaxe de variável de ambiente no properties**: `${DB_PASSWORD}` referencia a variável pelo nome; a senha nunca deve aparecer entre aspas dentro do `${}` (isso faz o Spring tentar interpretar o valor literal como nome de variável, causando falha de autenticação)
- **Porta 8080 ocupada**: execuções anteriores da aplicação podem deixar a porta presa mesmo após erro de inicialização. Resolvido com `netstat -ano | findstr :8080` para identificar o PID e `taskkill /PID <pid> /F` (executado como Administrador, quando o usuário comum não tem permissão) para liberar

---

## 10. Referências e Aprendizados de Projetos Similares

_A preencher conforme projetos de referência forem analisados._

---

## 11. Perguntas em Aberto

- [x] Estratégia de concorrência para movimentação de estoque (RNF04) — decidido: lock otimista (`@Version`) com retry automático em caso de `OptimisticLockException` (RN05)
- [ ] Comportamento quando estoque mínimo não está configurado
- [ ] Tratamento de expiração de token JWT no frontend
- [ ] Exportação de relatório grande — síncrona ou assíncrona
- [x] Banco de dados: decidido PostgreSQL, mais alinhado à stack real de backend (Spring Boot/JPA) e com suporte nativo mais robusto a concorrência do que o SQLite usado nos projetos anteriores

---

## 12. Atualizações de Implementação

### Decisões e regras consolidadas durante o desenvolvimento

| Decisão | Justificativa |
|---|---|
| Chave JWT fixa, lida de `application-local.properties` (fora do Git) | A chave gerada a cada start invalidava todos os tokens a cada reinício da aplicação |
| Relacionamentos recebidos por ID (`fornecedorId`, `produtoId`) e entidade real buscada no repository | Entidades com `@Version` montadas parcialmente pelo Jackson causavam erro de entidade desacoplada |
| `@JsonIgnore` na lista `produtos` de `Fornecedor` | Evita referência circular na serialização JSON (resposta de 22 KB de recursão) |
| `@Transactional` em `PedidoCompraService.criar()` | Falha no meio da criação deixava pedido órfão sem itens; rollback confirmado no banco |
| Recebimento de pedido bloqueado se status ≠ PENDENTE | Impede entrada de estoque em duplicidade por chamada repetida |
| Mensagem única no login para email inexistente e senha errada | Não revela quais emails existem; coberto por teste automatizado |
| Validação de entrada nos DTOs (`@NotNull`, `@Positive`, `@NotEmpty`, `@Valid`) | Rejeita dado inválido com 400 antes de chegar ao service |
| Teste de concorrência em banco separado (`painel_estoque_test`, perfil `test`) | Evita sujar o banco de desenvolvimento com dados de teste |

### Endpoints implementados

`POST /auth/login` · CRUD `/fornecedores` · CRUD `/produtos` · `GET /produtos/fornecedor/{id}` · `GET /produtos/estoque-baixo` · `POST|GET /pedidos-compra` · `POST /pedidos-compra/{id}/receber` · `POST /movimentacoes/saida` · `GET /movimentacoes/produto/{id}`

### Status dos requisitos

RF01-RF08 e RF10 implementados e testados. **RF09 (exportação PDF/Excel) e o frontend React permanecem pendentes.**

### Casos de borda resolvidos

- Saída maior que o saldo: rejeitada com 400 (RN01), validada pela API.
- Pedido criado com produto inexistente: nada é persistido (rollback transacional).

### Limitações conhecidas

Esquema via `ddl-auto=update` (em produção, Flyway); sem refresh de token; requisição sem token retorna 403 em vez de 401.

