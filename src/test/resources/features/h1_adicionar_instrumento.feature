# language: pt
Funcionalidade: Adicionar instrumento na mesa
  Como DJ
  Quero colocar novos instrumentos na mesa
  Para montar a minha mixagem

  Contexto:
    Dado que a mesa começa só com "bateria" e "baixo"

  Cenário: Adicionar um instrumento novo
    Quando eu adiciono o instrumento "Guitarra"
    Então a resposta tem status 201
    E a mesa passa a ter 3 instrumentos
    E o instrumento "guitarra" está pausado

  Cenário: Não pode ter dois instrumentos com o mesmo nome
    Quando eu adiciono o instrumento "BATERIA"
    Então a resposta tem status 409
    E a mesa passa a ter 2 instrumentos

  Cenário: A mesa tem um limite de instrumentos
    Dado que a mesa já está com 8 instrumentos
    Quando eu adiciono o instrumento "sobrando"
    Então a resposta tem status 422
    E a mensagem de erro fala "máximo de 8"
