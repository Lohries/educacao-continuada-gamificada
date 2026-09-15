# language: pt
# Autor da User Story e dos cenarios BDD: Mathues de Luzia Souza (github.com/mathzk)
#
# US: Como aluno da plataforma de Educacao Continuada Gamificada,
#     quero me matricular em um curso disponivel,
#     para iniciar minha trilha de aprendizado.

Funcionalidade: Matricula do aluno em um curso
  Como aluno da plataforma
  Eu quero me matricular em um curso disponivel
  Para iniciar minha trilha de aprendizado

  Cenario: Aluno se matricula com sucesso em um curso disponivel
    Dado que existe um aluno cadastrado chamado "Ana Souza"
    E existe o curso "Fundamentos de Spring Boot" disponivel na plataforma
    Quando o aluno se matricula no curso "Fundamentos de Spring Boot"
    Entao a matricula deve ser criada com sucesso
    E a matricula deve estar com o status "nao concluida"

  Cenario: Aluno nao pode se matricular duas vezes no mesmo curso
    Dado que existe um aluno cadastrado chamado "Bruno Lima"
    E existe o curso "TDD na Pratica" disponivel na plataforma
    E o aluno ja esta matriculado no curso "TDD na Pratica"
    Quando o aluno tenta se matricular novamente no curso "TDD na Pratica"
    Entao a plataforma deve recusar a nova matricula
