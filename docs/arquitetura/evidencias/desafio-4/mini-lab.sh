#!/usr/bin/env bash
# Mini-lab do Desafio 4: conclusao de curso com interacao sincrona (XP) e
# assincrona (evento -> notificacao), simulando o consumidor indisponivel.
#
# Uso (na raiz do repositorio, com o jar ja gerado por ./mvnw package):
#   PORTA_CORE=8081 bash docs/arquitetura/evidencias/desafio-4/mini-lab.sh > docs/arquitetura/evidencias/desafio-4/mini-lab.log
set -u
RAIZ="$(cd "$(dirname "$0")/../../../.." && pwd)"
PORTA_CORE=${PORTA_CORE:-8080}
CORE=http://localhost:$PORTA_CORE
JSON='Content-Type: application/json'

passo() { echo; echo "== $(date +%H:%M:%S) $*"; }
chamar() { curl -s -w '\n   -> HTTP %{http_code} em %{time_total}s\n' "$@"; }
id_de() { sed 's/.*"id":\([0-9]*\).*/\1/'; }

passo "Subindo o Core (h2) na porta $PORTA_CORE: lab habilitado, reprocessamento a cada 5 s (eventos com 10 s ou mais)"
JAR=$(ls "$RAIZ"/backend/target/*.jar | grep -v original | head -1)
LAB_HABILITADO=true NOTIFICACAO_REPROCESSAR_MS=5000 java -jar "$JAR" --server.port=$PORTA_CORE --spring.jpa.show-sql=false > /tmp/core-d4.log 2>&1 &
echo $! > /tmp/core-d4.pid
for _ in $(seq 1 120); do curl -sf $CORE/actuator/health > /dev/null && break; sleep 1; done
chamar $CORE/actuator/health

ALUNO=$(curl -s -X POST $CORE/api/usuarios -H "$JSON" -d '{"nome":"Aluno Eventos","email":"eventos@teste.com"}' | id_de)

passo "1) Consumidor no ar: concluir curso 1"
M1=$(curl -s -X POST $CORE/api/matriculas -H "$JSON" -d "{\"usuarioId\":$ALUNO,\"cursoId\":1}" | id_de)
chamar -X POST $CORE/api/matriculas/$M1/concluir
echo "   XP logo apos a resposta (sincrono):"; chamar $CORE/api/usuarios/$ALUNO
sleep 1
echo "   notificacoes (assincrono):"; chamar "$CORE/api/notificacoes?usuarioId=$ALUNO"
chamar $CORE/api/notificacoes/eventos-pendentes

passo "2) Derrubando o provedor de notificacao (consumidor indisponivel)"
chamar -X POST "$CORE/api/lab/notificacao?indisponivel=true"

passo "3) Concluir curso 2 com o consumidor fora: a conclusao responde normalmente"
M2=$(curl -s -X POST $CORE/api/matriculas -H "$JSON" -d "{\"usuarioId\":$ALUNO,\"cursoId\":2}" | id_de)
chamar -X POST $CORE/api/matriculas/$M2/concluir
echo "   XP ja atualizado (sincrono):"; chamar $CORE/api/usuarios/$ALUNO
sleep 1
echo "   notificacoes: continua so a do curso 1"; chamar "$CORE/api/notificacoes?usuarioId=$ALUNO"
echo "   evento da conclusao 2 guardado como pendente:"; chamar $CORE/api/notificacoes/eventos-pendentes

passo "4) Erro do consumidor no log do Core (a conclusao nao foi afetada)"
grep -m2 "provedor de notificacao indisponivel" /tmp/core-d4.log | sed 's/^\(.\{200\}\).*/\1.../'

passo "5) Religando o provedor e aguardando o reprocessamento automatico"
chamar -X POST "$CORE/api/lab/notificacao?indisponivel=false"
for i in $(seq 1 30); do
  P=$(curl -s $CORE/api/notificacoes/eventos-pendentes | sed 's/.*"pendentes":\([0-9]*\).*/\1/')
  [ "$P" = "0" ] && echo "   pendentes=0 apos ~$((i * 2)) s" && break
  sleep 2
done
chamar "$CORE/api/notificacoes?usuarioId=$ALUNO"
chamar $CORE/api/notificacoes/eventos-pendentes

kill "$(cat /tmp/core-d4.pid)" 2> /dev/null
passo "Fim do mini-lab"
