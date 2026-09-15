# language: pt
# Autor da User Story e dos cenarios BDD: Emanuel Ronaldo Gomes de Souza (github.com/ManuJoestar)
#
# US: Como aluno da plataforma de Educacao Continuada Gamificada,
#     quero ganhar pontos de experiencia (XP) ao concluir um curso,
#     para subir de nivel e acompanhar minha evolucao.

Funcionalidade: Ganho de XP e evolucao de nivel do aluno
  Como aluno da plataforma
  Eu quero ganhar XP ao concluir um curso
  Para subir de nivel e acompanhar minha evolucao

  Cenario: Aluno ganha XP ao concluir um curso
    Dado que existe um aluno cadastrado chamado "Carla Nunes" com 0 XP
    E o aluno esta matriculado no curso "Fundamentos de Spring Boot" que vale 100 XP
    Quando o aluno conclui a matricula no curso "Fundamentos de Spring Boot"
    Entao o aluno deve possuir 100 XP
    E o nivel do aluno deve ser 2

  Cenario: Aluno acumula XP de varios cursos concluidos
    Dado que existe um aluno cadastrado chamado "Diego Alves" com 0 XP
    E o aluno esta matriculado no curso "TDD na Pratica" que vale 150 XP
    E o aluno conclui a matricula no curso "TDD na Pratica"
    E o aluno esta matriculado no curso "BDD e ATDD com Cucumber" que vale 200 XP
    Quando o aluno conclui a matricula no curso "BDD e ATDD com Cucumber"
    Entao o aluno deve possuir 350 XP
    E o nivel do aluno deve ser 4
