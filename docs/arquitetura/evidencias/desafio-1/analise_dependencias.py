"""Compara a organizacao do Core antes (camadas) e depois (modulos).

Uso: python analise_dependencias.py <src/main/java da versao A> <src/main/java da versao B> [rotulo A] [rotulo B]

Para cada versao, mostra:
- em quantos pacotes de topo ficam as classes de cada capacidade de negocio;
- as dependencias entre pacotes de topo (imports), com contagem;
- as classes que misturam mais de uma capacidade.
"""
import os
import re
import sys
from collections import defaultdict

BASE = 'br.edu.gamificacao'

# Palavras que identificam a capacidade de negocio pelo nome da classe ou pelo uso
CAPACIDADES = {
    'Usuario': re.compile(r'\bUsuario(?!Service\.adicionarXp)'),
    'Curso': re.compile(r'\bCurso\w*\b'),
    'Matricula': re.compile(r'\bMatricula\w*\b'),
    'Conquista': re.compile(r'\b(Badge\w*|ProgressoGamificado|adicionarXp|concederXp|calcularNivel)\b'),
    'Ranking': re.compile(r'\b(Ranking\w*|ranking\(|findAllByOrderByXpDesc|listarPorXpDecrescente)\b'),
}


def capacidade_da_classe(nome):
    for cap in ('Ranking', 'Conquista', 'Matricula', 'Curso', 'Usuario'):
        if CAPACIDADES[cap].match(nome) or nome.startswith(cap):
            return cap
    if nome in ('Badge', 'BadgeRepository', 'BadgeSeeder', 'ProgressoGamificado', 'ConquistaService'):
        return 'Conquista'
    return None


# Metodos que implementam a capacidade, mesmo dentro de classes de outra capacidade
METODOS = {
    'Conquista': re.compile(r'^\s*public [\w<>]+ (adicionarXp|concederXp|badgesConquistadas)\(', re.M),
    'Ranking': re.compile(r'^\s*public [\w<>]+ ranking\(', re.M),
}


def analisar(raiz):
    classes = {}
    for d, _, fs in os.walk(raiz):
        for f in fs:
            if f.endswith('.java'):
                caminho = os.path.join(d, f)
                texto = open(caminho, encoding='utf8').read()
                pkg = re.search(r'^package ([\w.]+);', texto, re.M).group(1)
                topo = pkg[len(BASE) + 1:].split('.')[0] if pkg != BASE else '(raiz)'
                classes[f[:-5]] = {'pkg': pkg, 'topo': topo, 'texto': texto}

    espalhamento = defaultdict(set)
    misturas = []
    for nome, c in classes.items():
        cap = capacidade_da_classe(nome)
        if cap:
            espalhamento[cap].add(c['topo'])
        for k, rx in METODOS.items():
            if rx.search(c['texto']):
                espalhamento[k].add(c['topo'])
        corpo = re.sub(r'^(package|import) .*$', '', c['texto'], flags=re.M)
        usadas = {k for k, rx in CAPACIDADES.items() if rx.search(corpo)}
        donas = {cap} if cap else set()
        extras = usadas - donas
        if cap and len(extras) >= 2:
            misturas.append((nome, c['topo'], sorted(usadas)))

    arestas = defaultdict(int)
    for nome, c in classes.items():
        for imp in re.findall(r'^import ' + re.escape(BASE) + r'\.([\w.]+);', c['texto'], re.M):
            destino = imp.split('.')[0]
            if destino != c['topo']:
                arestas[(c['topo'], destino)] += 1
    return classes, espalhamento, arestas, misturas


def relatorio(rotulo, raiz):
    classes, esp, arestas, misturas = analisar(raiz)
    linhas = [f'## {rotulo}', '', f'Classes: {len(classes)}; pacotes de topo: {len({c["topo"] for c in classes.values()})}', '']
    linhas += ['| Capacidade | Pacotes de topo com codigo da capacidade (classes ou metodos) | Qtd |', '|---|---|---|']
    for cap in ('Usuario', 'Curso', 'Matricula', 'Conquista', 'Ranking'):
        linhas.append(f'| {cap} | {", ".join(sorted(esp[cap]))} | {len(esp[cap])} |')
    linhas += ['', '| De | Para | Imports |', '|---|---|---|']
    for (a, b), n in sorted(arestas.items()):
        linhas.append(f'| {a} | {b} | {n} |')
    linhas += ['', f'Dependencias entre pacotes de topo: {len(arestas)} arestas, {sum(arestas.values())} imports', '']
    linhas += ['Classes que misturam 3 ou mais capacidades:', '']
    linhas += [f'- `{n}` ({t}): {", ".join(u)}' for n, t, u in sorted(misturas)] or ['- nenhuma']
    return '\n'.join(linhas) + '\n'


if __name__ == '__main__':
    a, b = sys.argv[1], sys.argv[2]
    ra = sys.argv[3] if len(sys.argv) > 3 else 'Antes'
    rb = sys.argv[4] if len(sys.argv) > 4 else 'Depois'
    sys.stdout.buffer.write((relatorio(ra, a) + '\n' + relatorio(rb, b)).encode('utf8'))
