# language: pt
# Autor da User Story e dos cenarios BDD: Luis Henrique Telo Ladeira Mota (github.com/Lohries)
#
# US: Como aluno da plataforma de Educacao Continuada Gamificada,
#     quero visualizar o ranking de pontuacao dos alunos,
#     para me comparar com outros alunos e me sentir motivado a evoluir.

Funcionalidade: Ranking de alunos por pontuacao (XP)
  Como aluno da plataforma
  Eu quero visualizar o ranking de alunos ordenado por XP
  Para me comparar com outros alunos

  Cenario: Ranking ordena alunos do maior para o menor XP
    Dado que existe um aluno cadastrado chamado "Elisa Prado" com 300 XP
    E existe um aluno cadastrado chamado "Fabio Rocha" com 900 XP
    Quando eu consulto o ranking de alunos
    Entao "Fabio Rocha" deve aparecer no ranking em posicao melhor que "Elisa Prado"

  Cenario: Aluno sem nenhum XP aparece no final do ranking em relacao aos demais
    Dado que existe um aluno cadastrado chamado "Gabriel Dias" com 0 XP
    E existe um aluno cadastrado chamado "Helena Melo" com 50 XP
    Quando eu consulto o ranking de alunos
    Entao "Helena Melo" deve aparecer no ranking em posicao melhor que "Gabriel Dias"
