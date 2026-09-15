const BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080'

async function request(path, options = {}) {
  const response = await fetch(`${BASE_URL}${path}`, {
    headers: { 'Content-Type': 'application/json' },
    ...options,
  })

  const texto = await response.text()
  const corpo = texto ? JSON.parse(texto) : null

  if (!response.ok) {
    const mensagem = corpo?.mensagem || `erro ${response.status}`
    throw new Error(mensagem)
  }

  return corpo
}

export const api = {
  listarAlunos: () => request('/api/usuarios'),
  criarAluno: (nome, email) =>
    request('/api/usuarios', { method: 'POST', body: JSON.stringify({ nome, email }) }),
  ranking: () => request('/api/usuarios/ranking'),

  listarCursos: () => request('/api/cursos'),
  criarCurso: (titulo, descricao, cargaHoraria, xpRecompensa) =>
    request('/api/cursos', {
      method: 'POST',
      body: JSON.stringify({ titulo, descricao, cargaHoraria, xpRecompensa }),
    }),

  matricular: (usuarioId, cursoId) =>
    request('/api/matriculas', { method: 'POST', body: JSON.stringify({ usuarioId, cursoId }) }),
  concluirMatricula: (matriculaId) =>
    request(`/api/matriculas/${matriculaId}/concluir`, { method: 'POST' }),
  matriculasDoAluno: (usuarioId) => request(`/api/matriculas?usuarioId=${usuarioId}`),
}
