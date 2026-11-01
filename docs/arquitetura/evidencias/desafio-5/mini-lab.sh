#!/usr/bin/env bash
# Mini-lab do Desafio 5: dispositivos -> Mosquitto (MQTT) -> gateway -> Core (REST).
# Cenarios: duplicatas, queda de conexao do dispositivo e gateway fora do ar.
#
# Uso (na raiz do repositorio, com o jar gerado e o venv iot/.venv criado):
#   PORTA_CORE=8081 bash docs/arquitetura/evidencias/desafio-5/mini-lab.sh > docs/arquitetura/evidencias/desafio-5/mini-lab.log
# Requer: Docker (para o eclipse-mosquitto:2.0.20), Java 21, curl.
set -u
RAIZ="$(cd "$(dirname "$0")/../../../.." && pwd)"
PORTA_CORE=${PORTA_CORE:-8080}
CORE=http://localhost:$PORTA_CORE
JSON='Content-Type: application/json'
PY="$RAIZ/iot/.venv/Scripts/python"
[ -x "$PY" ] || PY="$RAIZ/iot/.venv/bin/python"

passo() { echo; echo "== $(date +%H:%M:%S) $*"; }
id_de() { sed 's/.*"id":\([0-9]*\).*/\1/'; }
total_no_core() {
  local t=0
  for a in $ALUNOS; do t=$((t + $(curl -s "$CORE/api/eventos-aprendizagem?alunoId=$a" | grep -o '"eventId"' | wc -l))); done
  echo $t
}
subir_gateway() {
  (cd "$RAIZ/iot/gateway" && CORE_URL=$CORE "$PY" -u gateway.py >> /tmp/gateway.log 2>&1 & echo $! > /tmp/gateway.pid)
  sleep 3
}
parar_gateway() {
  kill "$(cat /tmp/gateway.pid)" 2> /dev/null
  command -v taskkill > /dev/null && for p in $(netstat -ano | grep ':9101 ' | grep LISTENING | awk '{print $5}' | sort -u); do taskkill //F //PID "$p" > /dev/null 2>&1; done
  sleep 1
}

passo "Subindo o broker Mosquitto 2.0.20 (Docker) com iot/mosquitto/mosquitto.conf"
docker rm -f lab-mosquitto > /dev/null 2>&1
MSYS_NO_PATHCONV=1 docker run -d --name lab-mosquitto -p 1883:1883 \
  -v "$RAIZ/iot/mosquitto/mosquitto.conf:/mosquitto/config/mosquitto.conf:ro" eclipse-mosquitto:2.0.20 > /dev/null
sleep 2
docker logs lab-mosquitto 2>&1 | grep "running"

passo "Subindo o Core (h2) na porta $PORTA_CORE"
JAR=$(ls "$RAIZ"/backend/target/*.jar | grep -v original | head -1)
java -jar "$JAR" --server.port=$PORTA_CORE --spring.jpa.show-sql=false > /tmp/core-d5.log 2>&1 &
echo $! > /tmp/core-d5.pid
for _ in $(seq 1 120); do curl -sf $CORE/actuator/health > /dev/null && break; sleep 1; done
ALUNOS=""
for n in 1 2 3; do
  ALUNOS="$ALUNOS $(curl -s -X POST $CORE/api/usuarios -H "$JSON" -d "{\"nome\":\"Aluno Lab $n\",\"email\":\"lab$n@teste.com\"}" | id_de)"
done
echo "alunos cadastrados:$ALUNOS"

passo "Subindo o gateway IoT (sessao persistente, QoS 1, ack manual)"
: > /tmp/gateway.log
subir_gateway
grep "conectado" /tmp/gateway.log

passo "1) Rajada: 2 dispositivos x 4 eventos, queda de conexao do sensor1 no meio e 2 duplicatas"
(cd "$RAIZ/iot/simulador" && "$PY" simulador.py --alunos $ALUNOS --dispositivos 2 --eventos 4 --duplicar 2 --queda)
sleep 4
echo "eventos no Core: $(total_no_core) (esperado: 8 unicos)"

passo "2) Gateway fora do ar; dispositivos continuam publicando (2 x 3 eventos)"
parar_gateway
(cd "$RAIZ/iot/simulador" && "$PY" simulador.py --alunos $ALUNOS --dispositivos 2 --eventos 3 --semente 7)
echo "eventos no Core com o gateway fora: $(total_no_core) (continua 8)"

passo "3) Gateway volta: o broker entrega as mensagens guardadas na sessao persistente"
subir_gateway
sleep 4
echo "eventos no Core: $(total_no_core) (esperado: 14 unicos)"

passo "4) Metricas do gateway (Prometheus)"
curl -s localhost:9101/metrics | grep -E "^iot_[a-z_]+_total "

passo "5) Trechos do log do gateway"
grep -E "duplicado|conectado" /tmp/gateway.log | sed 's/^\(.\{170\}\).*/\1.../'
grep -c "encaminhado:" /tmp/gateway.log | sed 's/^/encaminhados ao Core: /'

passo "6) Exemplo de evento registrado no Core"
curl -s "$CORE/api/eventos-aprendizagem?alunoId=$(echo $ALUNOS | cut -d' ' -f1)" | sed 's/},{/},\n{/g' | head -2

parar_gateway
kill "$(cat /tmp/core-d5.pid)" 2> /dev/null
docker rm -f lab-mosquitto > /dev/null
passo "Fim do mini-lab"
