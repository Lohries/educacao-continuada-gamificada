#!/usr/bin/env bash
# Mini-lab do Desafio 3: Spring Boot consumindo o ia-service (FastAPI),
# interrupcao do servico Python e observacao do comportamento degradado.
#
# Uso (na raiz do repositorio, com o jar ja gerado por ./mvnw package):
#   bash docs/arquitetura/evidencias/desafio-3/mini-lab.sh > docs/arquitetura/evidencias/desafio-3/mini-lab.log
#
# Variavel opcional: PORTA_CORE (padrao 8080).
# Requer: Java 21, o venv do ia-service (ia-service/.venv) e curl.
set -u
RAIZ="$(cd "$(dirname "$0")/../../../.." && pwd)"
PORTA_CORE=${PORTA_CORE:-8080}
CORE=http://localhost:$PORTA_CORE
PY="$RAIZ/ia-service/.venv/Scripts/python"
[ -x "$PY" ] || PY="$RAIZ/ia-service/.venv/bin/python"

passo() { echo; echo "== $(date +%H:%M:%S) $*"; }
chamar() { curl -s -w '\n   -> HTTP %{http_code} em %{time_total}s\n' "$@"; }

subir_ia() {
  (cd "$RAIZ/ia-service" && "$PY" -m uvicorn app.main:app --port 8000 > /tmp/ia-service.log 2>&1 & echo $! > /tmp/ia-service.pid)
  for _ in $(seq 1 40); do curl -sf localhost:8000/health > /dev/null && return; sleep 0.5; done
}

parar_ia() {
  kill "$(cat /tmp/ia-service.pid)" 2> /dev/null
  # no Windows o uvicorn pode deixar o processo filho; garante a porta livre
  command -v taskkill > /dev/null && for p in $(netstat -ano | grep ':8000 ' | grep LISTENING | awk '{print $5}' | sort -u); do taskkill //F //PID "$p" > /dev/null 2>&1; done
  sleep 1
}

passo "Subindo o ia-service (FastAPI) na porta 8000"
subir_ia
chamar localhost:8000/health

passo "Subindo o Core (Spring Boot, perfil h2) na porta $PORTA_CORE com IA_URL=http://localhost:8000"
JAR=$(ls "$RAIZ"/backend/target/*.jar | grep -v original | head -1)
IA_URL=http://localhost:8000 java -jar "$JAR" --server.port=$PORTA_CORE --spring.jpa.show-sql=false > /tmp/core.log 2>&1 &
echo $! > /tmp/core.pid
for _ in $(seq 1 120); do curl -sf $CORE/actuator/health > /dev/null && break; sleep 1; done
chamar $CORE/actuator/health

passo "Cadastrando aluno e concluindo o curso 1 (para a IA ter historico)"
ALUNO=$(curl -s -X POST $CORE/api/usuarios -H 'Content-Type: application/json' -d '{"nome":"Aluna Mini-lab","email":"minilab@teste.com"}' | sed 's/.*"id":\([0-9]*\).*/\1/')
MAT=$(curl -s -X POST $CORE/api/matriculas -H 'Content-Type: application/json' -d "{\"usuarioId\":$ALUNO,\"cursoId\":1}" | sed 's/.*"id":\([0-9]*\).*/\1/')
chamar -X POST $CORE/api/matriculas/$MAT/concluir

passo "1) IA no ar: recomendacao e assistente vem do ia-service (origem=ia)"
chamar "$CORE/api/recomendacoes?usuarioId=$ALUNO"
chamar -X POST $CORE/api/assistente/perguntas -H 'Content-Type: application/json' -d '{"pergunta":"Quantos XP preciso para a badge Intermediario?"}'

passo "2) Interrompendo o ia-service (kill do processo Python)"
parar_ia
curl -s -m 2 localhost:8000/health || echo "   ia-service nao responde (conexao recusada)"

passo "3) IA fora: o Core responde com fallback (origem=fallback, degradado=true), sem erro 5xx"
for i in 1 2 3; do chamar "$CORE/api/recomendacoes?usuarioId=$ALUNO"; done
chamar -X POST $CORE/api/assistente/perguntas -H 'Content-Type: application/json' -d '{"pergunta":"Como ganho XP?"}'

passo "4) Estado do circuit breaker depois das falhas"
chamar $CORE/actuator/circuitbreakers

passo "5) Fluxo essencial com a IA fora: matricula e conclusao continuam funcionando"
MAT2=$(curl -s -X POST $CORE/api/matriculas -H 'Content-Type: application/json' -d "{\"usuarioId\":$ALUNO,\"cursoId\":2}" | sed 's/.*"id":\([0-9]*\).*/\1/')
chamar -X POST $CORE/api/matriculas/$MAT2/concluir
chamar "$CORE/api/usuarios/$ALUNO"

passo "6) Religando o ia-service e esperando o circuito sair de OPEN (wait-duration 15 s)"
subir_ia
sleep 16
chamar "$CORE/api/recomendacoes?usuarioId=$ALUNO"
chamar $CORE/actuator/circuitbreakers

passo "Logs do Core sobre a falha (WARN)"
grep "ia-service indisponivel" /tmp/core.log | sed 's/^\(.\{220\}\).*/\1.../' | head -6

kill "$(cat /tmp/core.pid)" 2> /dev/null
parar_ia
passo "Fim do mini-lab"
