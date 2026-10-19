# Roteiro da apresentação — Mesa DJ (~10 min)

> Abram antes: o **README** no GitHub, a aba **Actions**, o **site no Render** e a pasta `docs/evidencias`.
> Pergunta central do professor: *"se um colega clonar o repositório amanhã, ele consegue rodar, testar e publicar sem pedir ajuda?"* → a resposta é **sim**, e cada um mostra uma parte disso.

---

## 1. Lucas Mateus — Abertura e projeto (≈ 1 min 30)

**Mostrar:** o site no ar (https://desafio-pi.onrender.com)

- "Somos o grupo da **Mesa DJ**. Na entrega anterior era um simulador de console em Java com threads."
- "Agora virou uma **API REST com Spring Boot + PostgreSQL**, com uma interface web que toca o som pelo navegador."
- Demonstrar rapidinho: adicionar um instrumento, dar play, mudar o BPM.
- "Hoje vamos mostrar as três entregas: **container**, **TDD** e **deploy automático**."

---

## 2. Gabriel Farias — E1 · Container automatizado (≈ 2 min)

**Mostrar:** `Dockerfile`, `compose.yaml`, prints `01` e `02`

- "O projeto sobe do zero com **um comando**: `docker compose up -d --build`."
- **Dockerfile multi-stage:**
  - 1º estágio (Maven + JDK 21): compila e **roda os testes unitários**. Se falhar, a imagem nem é criada.
  - 2º estágio (só JRE alpine): leva só o `.jar`. Imagem de **~180 MB em vez de ~600 MB**, e roda sem root.
- **compose.yaml:** API + Postgres, **volume nomeado** para os dados sobreviverem ao `down`, **healthcheck** e `depends_on` (a API só sobe quando o banco está saudável).
- **Segredos:** nada de senha no código. Fica no `.env` (fora do Git), e o `.env.example` serve de modelo.

---

## 3. Gabriel Nogueira — E2 · TDD: do story map aos testes (≈ 2 min)

**Mostrar:** seção **Story map** do README

- "Começamos pela **jornada do DJ**: montar a mesa → ajustar o ritmo → mixar ao vivo."
- "A Release 1 tem **3 histórias**: H1 adicionar instrumento, H2 controlar BPM, H3 tocar/pausar/volume."
- "Cada história virou **tarefas**, e cada tarefa tem o **teste que nasceu antes** do código (tabela no README)."
- **Testes por nível:**
  - **Unitário:** `InstrumentoTest`, `MesaServiceTest` (regras puras, rápidos)
  - **Integração:** `InstrumentoRepositoryIT`, `InstrumentoApiIT` com **Postgres real** (Testcontainers)
  - **Aceitação (BDD):** um `.feature` em Gherkin por história
- "No total são **48 testes**, e todos rodam na esteira a cada push e PR."

---

## 4. Mirella Emily — E2 · Jornada TDD e lições (≈ 2 min)

**Mostrar:** histórico de commits no GitHub, print `03` (esteira vermelha)

- "O histórico mostra o ciclo **red → green**: sempre um commit `test:` antes do `feat:`."
- **Quais regras testamos primeiro e por quê (risco):**
  1. **BPM entre 40 e 300**: um BPM 0 travaria a batida, e o maior risco era esse.
  2. **Nome repetido**: deixaria os comandos do DJ ambíguos.
  3. **Limite de 8 instrumentos**: protege o áudio do navegador.
  4. **Volume 0–100**: menor risco, porque é corrigido por *clamping*.
- **Prova do gate:** "quebramos o BPM de propósito (commit `58005c1`). A esteira ficou **vermelha** e o **deploy foi pulado**."
- **Lições:**
  - "Escrever o teste primeiro obrigou a gente a **decidir a regra antes** de programar."
  - "Cada nível de teste pega um tipo diferente de erro."
  - "Ficou sem teste automático: a interface e o som, que validamos na mão."

---

## 5. Myllena Cristiane — E3 · Deploy automatizado e fechamento (≈ 2 min 30)

**Mostrar:** aba **Actions**, prints `04` a `08`, Settings → Branches

- **Fluxo GitHub Flow:** "branch → Pull Request → `main`. A `main` é **protegida**: ninguém faz push direto, e o PR só entra com o check **build e testes** verde." (print `05` e `06`)
- **Esteira (GitHub Actions):** **testes → imagem Docker → deploy no Render**, mais um *smoke test* em `/actuator/health`. (print `04`)
- **Segredos:** a senha do banco fica nas variáveis do **Render** e o deploy hook nos **Secrets do GitHub**.
- **Rollback:** "pelo painel do Render, em 1 clique, voltamos para a versão anterior." (print `08`)
- **Lição de produção:** "o 1º deploy falhou por **senha errada do banco**. Os logs mostraram o erro, e resolvemos **sem mexer no código**, porque a configuração está fora dele."
- **Uso de IA:** "usamos o Antigravity, declarado no README. A IA rascunhou, e **nós revisamos e validamos**."
- **Fechamento:** "Então, respondendo a pergunta do professor: **sim**. Qualquer colega clona, roda com um comando, os testes protegem as regras, e cada merge na `main` vai sozinho para o ar. Obrigado!"

---

### Dicas
- Abram o site uns **2 minutos antes**: no plano grátis ele "dorme" e demora ~50 s para acordar.
- Se perguntarem **por que Render?** → grátis, faz o build do mesmo Dockerfile e tem Postgres gerenciado.
- Se perguntarem **por que GitHub Flow e não Git Flow?** → grupo pequeno, uma versão no ar, sem necessidade de `develop`/`release`.
