# Educação Continuada Gamificada

Projeto acadêmico (atividade em grupo com avaliação individual) desenvolvido em
**Spring Boot**, com **TDD** (RED → GREEN → BLUE) e **BDD/ATDD** executável via
**Cucumber**, camadas completas (Entity, Repository, Service, DTO, Controller),
documentação de API via **Swagger/OpenAPI**, front-end em **Vue 3** e
orquestração via **Docker** (Postgres + PgAdmin + backend + front-end).

## Equipe

| Integrante | GitHub |
|---|---|
| Mathues de Luzia Souza | [github.com/mathzk](https://github.com/mathzk) |
| Emanuel Ronaldo Gomes de Souza | [github.com/ManuJoestar](https://github.com/ManuJoestar) |
| Luis Henrique Telo Ladeira Mota | [github.com/Lohries](https://github.com/Lohries) |

## Descrição do estudo de caso

**Educação Continuada Gamificada**: uma plataforma onde alunos se cadastram,
se matriculam em cursos/trilhas de capacitação, e ao concluir um curso ganham
**XP (pontos de experiência)**. O acúmulo de XP define o **nível** do aluno e
desbloqueia **badges** (Iniciante, Intermediário, Avançado). Os alunos podem
comparar sua evolução com a de outros através de um **ranking** ordenado por
XP, o que estimula o engajamento com a educação continuada através de
mecânicas de jogo (gamificação).

## User Stories (1 por integrante)

| # | User Story | Responsável |
|---|---|---|
| US1 | Como aluno, quero **me matricular em um curso disponível**, para iniciar minha trilha de aprendizado. | Mathues de Luzia Souza |
| US2 | Como aluno, quero **ganhar XP ao concluir um curso**, para subir de nível e acompanhar minha evolução. | Emanuel Ronaldo Gomes de Souza |
| US3 | Como aluno, quero **visualizar o ranking de pontuação** dos alunos, para me comparar com outros e me sentir motivado a evoluir. | Luis Henrique Telo Ladeira Mota |

## BDD — cenários Gherkin por integrante (ATDD)

Cada User Story tem seu próprio arquivo `.feature`, escrito em Gherkin
(pt-BR) pelo integrante responsável pela US, e executado automaticamente
contra a API REST real via Cucumber (JUnit 5 + Spring Boot Test), o que
caracteriza **ATDD** (Acceptance Test Driven Development) — os critérios de
aceite viram testes automatizados que validam o sistema de ponta a ponta.

| Feature | Autor(a) | Cenários |
|---|---|---|
| [`matricula.feature`](backend/src/test/resources/features/matricula.feature) | Mathues de Luzia Souza | Matrícula com sucesso / recusa de matrícula duplicada |
| [`xp_e_nivel.feature`](backend/src/test/resources/features/xp_e_nivel.feature) | Emanuel Ronaldo Gomes de Souza | Ganho de XP ao concluir curso / acúmulo de XP de vários cursos |
| [`ranking.feature`](backend/src/test/resources/features/ranking.feature) | Luis Henrique Telo Ladeira Mota | Ordenação do ranking por XP (dois cenários) |

**Resultado da execução**: 6 cenários, 30 passos, todos `passed`.
Evidência (relatório HTML/JSON gerado pelo Cucumber e resumo legível) em
[`docs/evidencias/bdd-atdd/`](docs/evidencias/bdd-atdd/).

Os step definitions ficam em
[`backend/src/test/java/br/edu/gamificacao/bdd/`](backend/src/test/java/br/edu/gamificacao/bdd/).

## TDD — RED → GREEN → BLUE

Seguindo o mesmo formato do exercício da calculadora, foi criado o pacote
`domain` com uma classe de domínio pura (sem anotações de framework) e o
pacote de teste correspondente:

- Classe de domínio: [`ProgressoGamificado`](backend/src/main/java/br/edu/gamificacao/domain/ProgressoGamificado.java)
  (XP, nível, badges e comparação para ranking)
- Testes: [`ProgressoGamificadoTest`](backend/src/test/java/br/edu/gamificacao/domain/ProgressoGamificadoTest.java)
  (16 casos de teste, identificados por autor no cabeçalho de cada bloco)

### RED

Testes escritos primeiro; a classe existe apenas como *stub* (todo método
lança `UnsupportedOperationException`). Resultado: **14 testes, 14 falhas**.
Evidência: [`docs/evidencias/tdd/01-RED-mvn-test-output.txt`](docs/evidencias/tdd/01-RED-mvn-test-output.txt)

### GREEN

Lógica de negócio implementada para fazer os testes passarem. Resultado:
**14/14 testes passando**, porém cobertura ainda **não é 100%**
(97% instruções / 90% branches — método `getNomeAluno()` não exercitado por
nenhum teste ainda), evidenciando o "amarelo/vermelho" esperado nesta etapa.
Evidência: [`docs/evidencias/tdd/02-GREEN-mvn-test-output.txt`](docs/evidencias/tdd/02-GREEN-mvn-test-output.txt)
e relatório JaCoCo em [`docs/evidencias/tdd/coverage-green/`](docs/evidencias/tdd/coverage-green/index.html)

### BLUE

Casos de teste adicionados para fechar os gaps de cobertura (nome nulo e
getter não exercitado), sem alterar a lógica de negócio. Resultado:
**16/16 testes passando, 100% de cobertura** (instruções, branches, linhas e
métodos) — sem vermelho ou amarelo.
Evidência: [`docs/evidencias/tdd/03-BLUE-mvn-test-output.txt`](docs/evidencias/tdd/03-BLUE-mvn-test-output.txt)
e relatório JaCoCo em [`docs/evidencias/tdd/coverage-blue/`](docs/evidencias/tdd/coverage-blue/index.html)
(abra o `index.html` no navegador para ver o detalhe linha a linha, incluindo
o realce verde/amarelo/vermelho por linha de código).

> **Nota sobre evidência via IntelliJ IDEA Ultimate**: este projeto foi
> desenvolvido e validado via linha de comando (Maven + JUnit 5 + JaCoCo), já
> que o ambiente de execução usado aqui não tem o IntelliJ instalado. As
> evidências acima (saída real do `mvn test` e relatórios JaCoCo reais, que
> mostram a mesma informação de cobertura verde/amarelo/vermelho que o
> runner de cobertura do IntelliJ) são igualmente válidas como evidência de
> TDD. Ainda assim, **recomenda-se fortemente** abrir o projeto no IntelliJ
> IDEA Ultimate e rodar os testes por lá (botão direito > Run with Coverage)
> para também capturar as capturas de tela pedidas no enunciado com a UI do
> IntelliJ.

### Suíte completa

Rodando `mvn test` sem filtro (domínio + smoke test do Spring Boot +
Cucumber): **23/23 testes passando**.
Evidência: [`docs/evidencias/tdd/04-suite-completa-mvn-test.txt`](docs/evidencias/tdd/04-suite-completa-mvn-test.txt)

## Arquitetura / camadas

```
backend/src/main/java/br/edu/gamificacao/
├── domain/       ProgressoGamificado (regra de XP/nivel/badges, TDD)
├── entity/       Usuario, Curso, Matricula, Badge (JPA)
├── repository/   UsuarioRepository, CursoRepository, MatriculaRepository, BadgeRepository
├── service/      UsuarioService, CursoService, MatriculaService (+ exceptions)
├── dto/          *DTO (records) de entrada e saída da API
├── controller/   UsuarioController, CursoController, MatriculaController (+ exception handler)
├── config/       OpenApiConfig (Swagger), CorsConfig
└── seed/         DataSeeder (dados de demonstração)
```

## Dependências (Spring Boot)

Spring Web, Spring Data JPA, H2, PostgreSQL (driver), Lombok, Bean
Validation, Spring Boot Actuator, springdoc-openapi (Swagger UI) e Cucumber
(BDD/ATDD).

## Como rodar

### Opção 1 — Localmente (perfil H2, sem Docker)

```bash
cd backend
./mvnw spring-boot:run
```

A aplicação sobe em `http://localhost:8080` com banco **H2 em memória**
(perfil padrão). Console H2 disponível em `http://localhost:8080/h2-console`
(JDBC URL: `jdbc:h2:mem:gamificacao`, usuário `sa`, senha em branco).

### Opção 2 — Docker Compose (Postgres + PgAdmin + backend + front-end)

Requer Docker Desktop rodando.

```bash
docker compose up --build
```

Serviços disponíveis:

| Serviço | URL |
|---|---|
| Backend (API) | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui/index.html |
| Front-end (Vue) | http://localhost:5173 |
| PgAdmin | http://localhost:5050 (login: `admin@gamificacao.com` / `admin`) |
| PostgreSQL | `localhost:5432` (db `gamificacao`, user/senha `gamificacao`) |

Evidência de todos os serviços rodando (containers, `\dt` no Postgres,
API respondendo com dados vindos do Postgres, etc.) em
[`docs/evidencias/docker/evidencia-docker-compose.txt`](docs/evidencias/docker/evidencia-docker-compose.txt).

### Rodando os testes

```bash
cd backend
./mvnw test
```

> Se o JDK instalado na máquina for muito recente (ex.: JDK 26), o JaCoCo
> pode falhar ao instrumentar as classes por incompatibilidade de bytecode.
> Use um JDK 17 ou 21 (LTS) para rodar os testes com cobertura.

## Swagger / OpenAPI

Com o backend rodando (local ou Docker), acesse:

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- Especificação OpenAPI (JSON): `http://localhost:8080/v3/api-docs`

Especificação capturada como evidência em
[`docs/evidencias/openapi.json`](docs/evidencias/openapi.json).

## Endpoints principais

| Método | Caminho | Descrição |
|---|---|---|
| POST | `/api/usuarios` | Cadastra um aluno |
| GET | `/api/usuarios` | Lista alunos |
| GET | `/api/usuarios/{id}` | Busca aluno por id |
| GET | `/api/usuarios/ranking` | Ranking de alunos por XP |
| POST | `/api/cursos` | Cadastra um curso |
| GET | `/api/cursos` | Lista cursos |
| POST | `/api/matriculas` | Matricula um aluno em um curso |
| POST | `/api/matriculas/{id}/concluir` | Conclui a matrícula e concede XP |
| GET | `/api/matriculas?usuarioId=` | Lista matrículas de um aluno |

Evidência de uma chamada real de ponta a ponta (cadastro → matrícula →
conclusão → XP → ranking) em
[`docs/evidencias/api/evidencia-endpoints-h2.txt`](docs/evidencias/api/evidencia-endpoints-h2.txt)
e log de subida da aplicação em
[`docs/evidencias/app-boot-h2.log`](docs/evidencias/app-boot-h2.log).

## Front-end (Vue 3 + Vite)

Interface simples cobrindo as três User Stories em uma única tela: cadastro
e seleção de aluno, catálogo de cursos com matrícula/conclusão, e ranking.

```bash
cd frontend
npm install
npm run dev
```

Acesse `http://localhost:5173`. A URL da API é configurada via variável de
ambiente `VITE_API_URL` (ver [`frontend/.env`](frontend/.env)).

## Banco de dados: evidências H2 e PostgreSQL

- **H2**: ao rodar localmente (`./mvnw spring-boot:run`, perfil padrão),
  acesse o console em `http://localhost:8080/h2-console`. Evidência de
  endpoints funcionando sobre H2 em
  [`docs/evidencias/api/evidencia-endpoints-h2.txt`](docs/evidencias/api/evidencia-endpoints-h2.txt).
- **PostgreSQL**: ao rodar via `docker compose up`, o backend usa o perfil
  `postgres` ([`application-postgres.yml`](backend/src/main/resources/application-postgres.yml))
  e se conecta ao container `postgres`. Evidência das tabelas criadas e
  dados persistidos (via `psql`) em
  [`docs/evidencias/docker/evidencia-docker-compose.txt`](docs/evidencias/docker/evidencia-docker-compose.txt).
  O **PgAdmin** fica disponível em `http://localhost:5050` para
  administração visual do banco (adicionar um novo servidor apontando para
  host `postgres`, porta `5432`, banco `gamificacao`, usuário
  `gamificacao`).

## Planilha

Não foi utilizada planilha nesta entrega — todo o rastreamento de User
Stories, BDD e evidências de TDD está documentado neste README e nos
arquivos de código/`.feature` referenciados acima.

## Estrutura de evidências

```
docs/evidencias/
├── tdd/                     RED, GREEN, BLUE, suite completa + relatorios JaCoCo
├── bdd-atdd/                Relatorio Cucumber (HTML/JSON) + resumo legivel
├── api/                     Chamada de ponta a ponta da API (perfil H2)
├── docker/                  Evidencia do docker compose (Postgres, PgAdmin, etc.)
├── frontend/                Log de subida do front-end (Vite dev server)
├── app-boot-h2.log          Log de subida da aplicacao (perfil H2)
└── openapi.json             Especificacao OpenAPI capturada do Swagger
```
