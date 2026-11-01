## Antes — Monolito em Camadas (branch main, AC1)

Classes: 29; pacotes de topo: 9

| Capacidade | Pacotes de topo com codigo da capacidade (classes ou metodos) | Qtd |
|---|---|---|
| Usuario | controller, dto, entity, repository, service | 5 |
| Curso | controller, dto, entity, repository, service | 5 |
| Matricula | controller, dto, entity, repository, service | 5 |
| Conquista | domain, entity, repository, service | 4 |
| Ranking | controller, dto, service | 3 |

| De | Para | Imports |
|---|---|---|
| controller | dto | 7 |
| controller | entity | 3 |
| controller | service | 5 |
| dto | entity | 3 |
| repository | entity | 4 |
| seed | entity | 2 |
| seed | repository | 2 |
| service | domain | 1 |
| service | dto | 4 |
| service | entity | 6 |
| service | repository | 4 |

Dependencias entre pacotes de topo: 11 arestas, 41 imports

Classes que misturam 3 ou mais capacidades:

- `Matricula` (entity): Curso, Matricula, Usuario
- `MatriculaService` (service): Conquista, Curso, Matricula, Usuario
- `UsuarioService` (service): Conquista, Ranking, Usuario

## Depois — Monolito Modular (B1)

Classes: 33; pacotes de topo: 7

| Capacidade | Pacotes de topo com codigo da capacidade (classes ou metodos) | Qtd |
|---|---|---|
| Usuario | usuario | 1 |
| Curso | curso | 1 |
| Matricula | matricula | 1 |
| Conquista | conquista | 1 |
| Ranking | ranking | 1 |

| De | Para | Imports |
|---|---|---|
| conquista | usuario | 2 |
| curso | shared | 1 |
| matricula | conquista | 1 |
| matricula | curso | 3 |
| matricula | shared | 2 |
| matricula | usuario | 3 |
| ranking | usuario | 2 |
| usuario | shared | 2 |

Dependencias entre pacotes de topo: 8 arestas, 16 imports

Classes que misturam 3 ou mais capacidades:

- `Matricula` (matricula): Curso, Matricula, Usuario
- `MatriculaService` (matricula): Conquista, Curso, Matricula, Usuario
