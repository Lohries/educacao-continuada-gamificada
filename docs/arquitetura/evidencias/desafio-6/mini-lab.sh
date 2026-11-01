#!/usr/bin/env bash
# Mini-lab do Desafio 6: Core + ia-service instrumentados, Prometheus e Grafana.
# Gera trafego, provoca a queda do ia-service e registra o que as metricas mostram.
#
# Uso (na raiz do repositorio, com o jar gerado e o venv ia-service/.venv):
#   PORTA_CORE=8081 bash docs/arquitetura/evidencias/desafio-6/mini-lab.sh > docs/arquitetura/evidencias/desafio-6/mini-lab.log
# Requer: Docker, Java 21, Node 22 + Chrome (captura do Grafana), curl.
set -u
RAIZ="$(cd "$(dirname "$0")/../../../.." && pwd)"
EV="$RAIZ/docs/arquitetura/evidencias/desafio-6"
PORTA_CORE=${PORTA_CORE:-8080}
CORE=http://localhost:$PORTA_CORE
JSON='Content-Type: application/json'
PY="$RAIZ/ia-service/.venv/Scripts/python"
[ -x "$PY" ] || PY="$RAIZ/ia-service/.venv/bin/python"

passo() { echo; echo "== $(date +%H:%M:%S) $*"; }
consulta() { # consulta instantanea no Prometheus, imprime "rotulos = valor"
  curl -s --get localhost:9090/api/v1/query --data-urlencode "query=$1" | python -c "
import sys, json
for r in json.load(sys.stdin)['data']['result']:
    rot = ','.join(f'{k}={v}' for k, v in r['metric'].items() if k not in ('__name__', 'instance', 'job', 'application'))
    print('   ', rot or '(total)', '=', round(float(r['value'][1]), 4))"
}
subir_ia() {
  (cd "$RAIZ/ia-service" && "$PY" -m uvicorn app.main:app --host 0.0.0.0 --port 8000 > /tmp/ia-d6.log 2>&1 & echo $! > /tmp/ia-d6.pid)
  for _ in $(seq 1 40); do curl -sf localhost:8000/health > /dev/null && return; sleep 0.5; done
}
parar_ia() {
  kill "$(cat /tmp/ia-d6.pid)" 2> /dev/null
  command -v taskkill > /dev/null && for p in $(netstat -ano | grep ':8000 ' | grep LISTENING | awk '{print $5}' | sort -u); do taskkill //F //PID "$p" > /dev/null 2>&1; done
  sleep 1
}
trafego() { # $1 = segundos
  local fim=$(( $(date +%s) + $1 ))
  while [ "$(date +%s)" -lt "$fim" ]; do
    curl -s -o /dev/null "$CORE/api/ranking"
    curl -s -o /dev/null "$CORE/api/recomendacoes?usuarioId=$ALUNO"
    sleep 0.3
  done
}

passo "Subindo ia-service (8000) e Core ($PORTA_CORE)"
subir_ia
JAR=$(ls "$RAIZ"/backend/target/*.jar | grep -v original | head -1)
IA_URL=http://localhost:8000 java -jar "$JAR" --server.port=$PORTA_CORE --spring.jpa.show-sql=false > /tmp/core-d6.log 2>&1 &
echo $! > /tmp/core-d6.pid
for _ in $(seq 1 120); do curl -sf $CORE/actuator/health > /dev/null && break; sleep 1; done
ALUNO=$(curl -s -X POST $CORE/api/usuarios -H "$JSON" -d '{"nome":"Aluno Obs","email":"obs@teste.com"}' | sed 's/.*"id":\([0-9]*\).*/\1/')
M=$(curl -s -X POST $CORE/api/matriculas -H "$JSON" -d "{\"usuarioId\":$ALUNO,\"cursoId\":1}" | sed 's/.*"id":\([0-9]*\).*/\1/')
curl -s -o /dev/null -X POST $CORE/api/matriculas/$M/concluir

passo "Subindo Prometheus v2.55.1 e Grafana 11.3.1 (rede lab-obs) com a configuracao versionada"
docker rm -f prometheus grafana > /dev/null 2>&1
docker network create lab-obs > /dev/null 2>&1
MSYS_NO_PATHCONV=1 docker run -d --name prometheus --network lab-obs -p 9090:9090 \
  -v "$EV/prometheus-lab.yml:/etc/prometheus/prometheus.yml:ro" \
  -v "$RAIZ/observabilidade/prometheus/alertas.yml:/etc/prometheus/alertas.yml:ro" prom/prometheus:v2.55.1 > /dev/null
MSYS_NO_PATHCONV=1 docker run -d --name grafana --network lab-obs -p 3000:3000 \
  -e GF_AUTH_ANONYMOUS_ENABLED=true -e GF_AUTH_ANONYMOUS_ORG_ROLE=Viewer \
  -v "$RAIZ/observabilidade/grafana/provisioning:/etc/grafana/provisioning:ro" \
  -v "$RAIZ/observabilidade/grafana/dashboards:/var/lib/grafana/dashboards:ro" grafana/grafana:11.3.1 > /dev/null
for _ in $(seq 1 60); do curl -sf localhost:9090/-/ready > /dev/null && curl -sf localhost:3000/api/health > /dev/null && break; sleep 2; done
sleep 6
echo "alvos do Prometheus:"; curl -s localhost:9090/api/v1/targets | python -c "
import sys, json
for t in json.load(sys.stdin)['data']['activeTargets']: print('   ', t['labels']['job'], t['health'])"

passo "Fase 1 (90 s): trafego normal com a IA no ar"
trafego 90
echo "recomendacoes/s por origem:"; consulta 'sum by (origem) (rate(ia_respostas_total{recurso="recomendacao"}[1m]))'
echo "p95 do ranking (s):"; consulta 'histogram_quantile(0.95, sum by (le) (rate(http_server_requests_seconds_bucket{uri=~"/api(/usuarios)?/ranking"}[1m])))'
echo "circuito aberto?"; consulta 'resilience4j_circuitbreaker_state{name="ia-service",state="open"}'

passo "Fase 2 (90 s): ia-service derrubado (condicao provocada)"
parar_ia
trafego 90
echo "recomendacoes/s por origem:"; consulta 'sum by (origem) (rate(ia_respostas_total{recurso="recomendacao"}[1m]))'
echo "circuito aberto?"; consulta 'resilience4j_circuitbreaker_state{name="ia-service",state="open"}'
echo "ia-service no ar (up)?"; consulta 'up{job="ia-service"}'
echo "respostas 5xx do Core:"; consulta 'sum(increase(http_server_requests_seconds_count{status=~"5..",uri!~"/actuator.*"}[3m]))'
echo "alertas disparados:"; curl -s localhost:9090/api/v1/alerts | python -c "
import sys, json
for a in json.load(sys.stdin)['data']['alerts']: print('   ', a['labels']['alertname'], a['state'], '-', a['annotations'].get('resumo', ''))"
node "$EV/capturar.mjs" "http://localhost:3000/d/b1-evidencias/b1-evidencias-arquiteturais?orgId=1&from=now-5m&to=now&kiosk" "$EV/grafana-ia-fora.png" 12000

passo "Fase 3 (60 s): ia-service de volta"
subir_ia
trafego 60
echo "recomendacoes/s por origem:"; consulta 'sum by (origem) (rate(ia_respostas_total{recurso="recomendacao"}[30s]))'
echo "circuito aberto?"; consulta 'resilience4j_circuitbreaker_state{name="ia-service",state="open"}'
node "$EV/capturar.mjs" "http://localhost:3000/d/b1-evidencias/b1-evidencias-arquiteturais?orgId=1&from=now-5m&to=now&kiosk" "$EV/grafana-recuperacao.png" 12000

kill "$(cat /tmp/core-d6.pid)" 2> /dev/null
parar_ia
docker rm -f prometheus grafana > /dev/null
docker network rm lab-obs > /dev/null
passo "Fim do mini-lab"
