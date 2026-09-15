<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { api } from './api'

const alunos = ref([])
const cursos = ref([])
const ranking = ref([])
const matriculas = ref([])
const alunoSelecionadoId = ref(null)
const mensagemErro = ref('')

const novoAluno = reactive({ nome: '', email: '' })
const novoCurso = reactive({ titulo: '', descricao: '', cargaHoraria: 10, xpRecompensa: 100 })

const alunoSelecionado = computed(() =>
  alunos.value.find((a) => a.id === alunoSelecionadoId.value) || null,
)

async function carregarTudo() {
  try {
    mensagemErro.value = ''
    ;[alunos.value, cursos.value, ranking.value] = await Promise.all([
      api.listarAlunos(),
      api.listarCursos(),
      api.ranking(),
    ])

    if (!alunoSelecionadoId.value && alunos.value.length > 0) {
      alunoSelecionadoId.value = alunos.value[0].id
    }
    await carregarMatriculas()
  } catch (erro) {
    mensagemErro.value = erro.message
  }
}

async function carregarMatriculas() {
  if (!alunoSelecionadoId.value) {
    matriculas.value = []
    return
  }
  matriculas.value = await api.matriculasDoAluno(alunoSelecionadoId.value)
}

async function cadastrarAluno() {
  try {
    mensagemErro.value = ''
    const aluno = await api.criarAluno(novoAluno.nome, novoAluno.email)
    novoAluno.nome = ''
    novoAluno.email = ''
    alunoSelecionadoId.value = aluno.id
    await carregarTudo()
  } catch (erro) {
    mensagemErro.value = erro.message
  }
}

async function cadastrarCurso() {
  try {
    mensagemErro.value = ''
    await api.criarCurso(
      novoCurso.titulo,
      novoCurso.descricao,
      Number(novoCurso.cargaHoraria),
      Number(novoCurso.xpRecompensa),
    )
    novoCurso.titulo = ''
    novoCurso.descricao = ''
    novoCurso.cargaHoraria = 10
    novoCurso.xpRecompensa = 100
    await carregarTudo()
  } catch (erro) {
    mensagemErro.value = erro.message
  }
}

async function matricular(cursoId) {
  try {
    mensagemErro.value = ''
    await api.matricular(alunoSelecionadoId.value, cursoId)
    await carregarMatriculas()
  } catch (erro) {
    mensagemErro.value = erro.message
  }
}

async function concluir(matriculaId) {
  try {
    mensagemErro.value = ''
    await api.concluirMatricula(matriculaId)
    await carregarTudo()
  } catch (erro) {
    mensagemErro.value = erro.message
  }
}

onMounted(carregarTudo)
</script>

<template>
  <main class="pagina">
    <header class="cabecalho">
      <h1>🎮 Educação Continuada Gamificada</h1>
      <p>Plataforma de cursos com XP, níveis e ranking</p>
    </header>

    <p v-if="mensagemErro" class="erro">{{ mensagemErro }}</p>

    <section class="grid">
      <div class="cartao">
        <h2>Alunos</h2>
        <form class="formulario" @submit.prevent="cadastrarAluno">
          <input v-model="novoAluno.nome" placeholder="Nome" required />
          <input v-model="novoAluno.email" type="email" placeholder="Email" required />
          <button type="submit">Cadastrar aluno</button>
        </form>

        <label class="rotulo" for="select-aluno">Aluno atual</label>
        <select id="select-aluno" v-model="alunoSelecionadoId" @change="carregarMatriculas">
          <option v-for="aluno in alunos" :key="aluno.id" :value="aluno.id">
            {{ aluno.nome }} — {{ aluno.xp }} XP (nível {{ aluno.nivel }})
          </option>
        </select>

        <div v-if="alunoSelecionado" class="resumo-aluno">
          <strong>{{ alunoSelecionado.nome }}</strong>
          <span>XP: {{ alunoSelecionado.xp }}</span>
          <span>Nível: {{ alunoSelecionado.nivel }}</span>
        </div>
      </div>

      <div class="cartao">
        <h2>Cursos</h2>
        <form class="formulario" @submit.prevent="cadastrarCurso">
          <input v-model="novoCurso.titulo" placeholder="Título do curso" required />
          <input v-model="novoCurso.descricao" placeholder="Descrição" />
          <input v-model.number="novoCurso.cargaHoraria" type="number" placeholder="Carga horária" min="1" />
          <input v-model.number="novoCurso.xpRecompensa" type="number" placeholder="XP de recompensa" min="1" />
          <button type="submit">Cadastrar curso</button>
        </form>

        <ul class="lista">
          <li v-for="curso in cursos" :key="curso.id">
            <div>
              <strong>{{ curso.titulo }}</strong>
              <small>{{ curso.cargaHoraria }}h · {{ curso.xpRecompensa }} XP</small>
            </div>
            <button :disabled="!alunoSelecionadoId" @click="matricular(curso.id)">Matricular</button>
          </li>
        </ul>
      </div>

      <div class="cartao">
        <h2>Minhas matrículas</h2>
        <ul class="lista">
          <li v-for="matricula in matriculas" :key="matricula.id">
            <div>
              <strong>{{ matricula.tituloCurso }}</strong>
              <small>{{ matricula.concluida ? 'Concluída ✅' : 'Em andamento' }}</small>
            </div>
            <button v-if="!matricula.concluida" @click="concluir(matricula.id)">Concluir</button>
          </li>
          <li v-if="matriculas.length === 0" class="vazio">Nenhuma matrícula ainda</li>
        </ul>
      </div>

      <div class="cartao">
        <h2>🏆 Ranking</h2>
        <table class="tabela-ranking">
          <thead>
            <tr>
              <th>#</th>
              <th>Aluno</th>
              <th>XP</th>
              <th>Nível</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in ranking" :key="item.usuarioId">
              <td>{{ item.posicao }}</td>
              <td>{{ item.nome }}</td>
              <td>{{ item.xp }}</td>
              <td>{{ item.nivel }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  </main>
</template>

<style scoped>
.pagina {
  max-width: 1100px;
  margin: 0 auto;
  padding: 1.5rem;
}

.cabecalho {
  text-align: center;
  margin-bottom: 1.5rem;
}

.cabecalho h1 {
  margin-bottom: 0.25rem;
}

.erro {
  background: #fee2e2;
  color: #991b1b;
  padding: 0.75rem 1rem;
  border-radius: 8px;
  margin-bottom: 1rem;
}

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 1rem;
}

.cartao {
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 1rem;
  background: #fff;
}

.formulario {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.formulario input {
  padding: 0.5rem;
  border: 1px solid #d1d5db;
  border-radius: 6px;
}

button {
  padding: 0.5rem 0.75rem;
  border: none;
  border-radius: 6px;
  background: #4f46e5;
  color: #fff;
  cursor: pointer;
}

button:disabled {
  background: #9ca3af;
  cursor: not-allowed;
}

.rotulo {
  display: block;
  font-size: 0.85rem;
  margin-bottom: 0.25rem;
  color: #4b5563;
}

select {
  width: 100%;
  padding: 0.5rem;
  border-radius: 6px;
  border: 1px solid #d1d5db;
  margin-bottom: 0.75rem;
}

.resumo-aluno {
  display: flex;
  flex-direction: column;
  gap: 0.15rem;
  background: #f3f4f6;
  padding: 0.6rem 0.8rem;
  border-radius: 8px;
}

.lista {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.lista li {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.5rem;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
}

.lista li div {
  display: flex;
  flex-direction: column;
}

.vazio {
  color: #6b7280;
  justify-content: center;
}

.tabela-ranking {
  width: 100%;
  border-collapse: collapse;
}

.tabela-ranking th,
.tabela-ranking td {
  text-align: left;
  padding: 0.4rem 0.5rem;
  border-bottom: 1px solid #f0f0f0;
}
</style>
