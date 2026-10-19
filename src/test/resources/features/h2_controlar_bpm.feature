# language: pt
Funcionalidade: Controlar o ritmo (BPM)
  Como DJ
  Quero mudar o BPM de cada instrumento
  Para acompanhar o ritmo da pista sem estragar o som

  Contexto:
    Dado que a mesa começa só com "bateria" e "baixo"

  Esquema do Cenário: BPM dentro da faixa é aceito
    Quando eu mudo o BPM da "bateria" para <bpm>
    Então a resposta tem status 200
    E o BPM da "bateria" é <bpm>

    Exemplos:
      | bpm |
      | 40  |
      | 128 |
      | 300 |

  Esquema do Cenário: BPM fora da faixa é recusado e nada muda
    Quando eu mudo o BPM da "bateria" para <bpm>
    Então a resposta tem status 422
    E o BPM da "bateria" é 120

    Exemplos:
      | bpm |
      | 0   |
      | 39  |
      | 301 |
