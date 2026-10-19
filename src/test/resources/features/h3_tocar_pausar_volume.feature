# language: pt
Funcionalidade: Tocar, pausar e ajustar volume
  Como DJ
  Quero ligar, desligar e mudar o volume de cada instrumento
  Para controlar a mixagem ao vivo

  Contexto:
    Dado que a mesa começa só com "bateria" e "baixo"

  Cenário: Tocar e depois pausar o baixo
    Quando eu mando tocar o "baixo"
    Então o instrumento "baixo" está tocando
    Quando eu mando pausar o "baixo"
    Então o instrumento "baixo" está pausado

  Cenário: Volume acima de 100 fica no máximo
    Quando eu ajusto o volume da "bateria" para 150
    Então a resposta tem status 200
    E o volume da "bateria" é 100

  Cenário: Tocar um instrumento que não existe
    Quando eu mando tocar o "piano"
    Então a resposta tem status 404
