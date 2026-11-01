# ADR-002 — Ports & Adapters no módulo Conquista

- **Status:** aceito
- **Data:** 2026-11-01
- **Desafio:** B1 / Desafio 2 — Organização interna do Core

## Contexto

As regras de ranking, XP, moedas, badges e recompensas mudam com frequência. No Desafio 1, o `ConquistaService` dependia diretamente de três coisas:
- da entidade JPA `Usuario`;
- do `UsuarioService`;
- do `BadgeRepository` (Spring Data).

Ou seja, a regra de gamificação só podia ser testada subindo Spring e banco. Além disso, qualquer mudança no modelo de persistência atingia a regra.

## ASRs

| ASR | Como altera a arquitetura |
|---|---|
| **ASR-04 Modificabilidade das regras de gamificação** | As regras mudam com frequência, então precisam ficar num núcleo que não muda quando banco, framework ou integração mudam (estrutura: direção das dependências) |
| **ASR-05 Testabilidade do domínio** | As regras devem ser verificáveis sem Spring e sem banco, em milissegundos (estrutura: porta substituível por fake) |
| **ASR-01 Manutenibilidade** (vem do ADR-001) | Mantém a fronteira do módulo: o Hexagonal fica **dentro** de `conquista` e não se espalha pelo Core |

## RPCs

- O Core continua em Spring Boot.
- As regras existentes são reaproveitadas: `ProgressoGamificado` e seus 16 testes da AC1 continuam iguais.
- É proibido reescrever o produto inteiro só para aplicar um padrão.

## Alternativas

| Alternativa | Benefícios | Custos / riscos |
|---|---|---|
| A — Manter o serviço acoplado à persistência (estado do D1) | Zero custo | Regra testável só com Spring/banco; mudança de persistência atinge a regra |
| **B — Ports & Adapters só em `conquista`** | Inverte a única dependência que dói (regra → persistência); adapters trocáveis; custo localizado | Uma interface e um adapter a mais; mapeamento domínio ↔ persistência |
| C — Clean/Hexagonal em todos os módulos | Uniformidade | Desproporcional: `curso`, `usuario` e `ranking` são CRUD/leitura sem regra volátil, e violaria a RPC "não reescrever tudo" |

**DDD, Hexagonal, Clean e Onion não são concorrentes:**
- O **DDD** dá o vocabulário e as fronteiras: o módulo `conquista` funciona como um *bounded context*, e `ProgressoGamificado` é o modelo de domínio.
- O **Hexagonal** dá a mecânica da inversão (porta + adapter).
- Clean e Onion são a mesma ideia de "dependências apontando para dentro", descrita em camadas concêntricas.

Usamos DDD (tático, leve) + Hexagonal, e só onde há regra volátil.

## Decisão

Aplicar **Ports & Adapters no módulo `conquista`**:

| Papel | Classe | Depende de |
|---|---|---|
| Domínio | `ProgressoGamificado` | nada (Java puro) |
| Caso de uso / porta de entrada | `ConquistaService` | domínio + `ProgressoAlunoPort` |
| Porta de saída | `porta.ProgressoAlunoPort` | domínio |
| Adapter real | `adapter.ProgressoAlunoJpaAdapter` | `UsuarioService` (JPA, tabela `usuarios` da AC1) |
| Adapter fake (testes) | `ProgressoAlunoEmMemoria` | domínio |

As regras são protegidas por ArchUnit (`HexagonalConquistaTest`):
- o domínio e a porta não podem importar Spring, JPA ou o módulo `usuario`;
- o caso de uso não pode importar o adapter.

## Consequências

**Positivas:**
- A regra de gamificação roda sem Spring (`ConquistaServiceTest`).
- O mesmo contrato passa com os dois adapters.
- Trocar a persistência (por exemplo, para uma tabela própria de progresso ou um cache) passa a exigir só um novo adapter.

**Trade-offs aceitos:**
- Há uma camada de tradução (`ProgressoGamificado.restaurar`) a cada leitura.
- O XP ainda fica na tabela `usuarios` (legado), porque mover o dado era desnecessário agora.
- O catálogo de badges existe em dois lugares: nos limiares do domínio e na tabela `badges` da AC1. O domínio é a fonte de verdade. Unificar os dois por uma porta `CatalogoDeBadges` fica registrado como dívida.

**Gatilho de revisão:** se as regras passarem a ser configuráveis por instituição (multi-tenant), criar uma porta de leitura das regras.

## Evidência

- Antes: o `HexagonalConquistaTest` rodado contra o código do D1 falha, e mostra cada dependência proibida ([log](../arquitetura/evidencias/desafio-2/hexagonal-antes.log)).
- Depois: o contrato `ProgressoAlunoPortContrato` passa com o adapter em memória **e** com o JPA, e o caso de uso é testado sem Spring ([log](../arquitetura/evidencias/desafio-2/testes-desafio-2.log)).
