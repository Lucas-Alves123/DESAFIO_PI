# Mesa DJ — API de controle de instrumentos

Projeto Integrador — Container, TDD e Deploy automatizados  
CESAR School · Infraestrutura de Software · 2026.2 · ADS

---

## Integrantes

| Nome | GitHub |
|------|--------|
| Gabriel Farias | — |
| Gabriel Nogueira | — |
| Lucas Mateus | [@Lucas-Alves123](https://github.com/Lucas-Alves123) |
| Mirella Emily | — |
| Myllena Cristiane | — |

---

## O que é o projeto

A Mesa DJ nasceu na entrega anterior como um simulador de console multithread em Java puro, onde cada instrumento rodava em uma thread separada e o DJ controlava tudo por linha de comando (`play bateria`, `bpm baixo 128`, etc.).

Nesta entrega transformamos o núcleo de regras em uma **API REST com Spring Boot + PostgreSQL**, mantendo a mesma lógica de negócio (nome do instrumento, faixa de BPM, limite de canais na mesa) e adicionando uma interface web que roda no navegador. O som continua sendo sintetizado — agora pela Web Audio API do próprio browser.

---

## Como rodar

### Pré-requisitos

- Docker e Docker Compose instalados e com o daemon rodando
- Git

### Do zero em um único comando

```bash
git clone https://github.com/Lucas-Alves123/DESAFIO_PI.git
cd DESAFIO_PI
cp .env.example .env          # edite a senha se quiser
docker compose up -d --build
```

Após o build (primeira vez leva ~2 min por conta do download das imagens), acesse:

- **Interface web:** <http://localhost:8080>
- **API:** <http://localhost:8080/api/instrumentos>
- **Health:** <http://localhost:8080/actuator/health>

### Verificar que está no ar

```bash
# status dos containers
docker compose ps

# log da API
docker compose logs -f api

# testar a API direto
curl http://localhost:8080/actuator/health
curl http://localhost:8080/api/instrumentos
```

### Parar

```bash
docker compose down          # para sem apagar os dados
docker compose down -v       # para e apaga o volume do banco
```

---

## Story map

### Backbone (jornada do DJ)

```
 Montar a mesa  ──►  Ajustar o ritmo  ──►  Mixar ao vivo  ──►  Ver a mesa no navegador
```

| Backbone | **Release 1 (entregue)** | Release 2 (futuro) |
|----------|--------------------------|--------------------|
| Montar a mesa | **H1 · Adicionar instrumento** (+ remover) | Salvar e carregar "sets" |
| Ajustar o ritmo | **H2 · Controlar BPM** | Sincronizar BPM de todos (sync) |
| Mixar ao vivo | **H3 · Tocar, pausar e volume** | Efeitos (eco, filtro), crossfader |
| Ver no navegador | Interface web com Web Audio | Login por DJ, várias mesas |

### Release 1 quebrada em tarefas

**H1 · Adicionar instrumento** — *como DJ, quero colocar instrumentos na mesa para montar a mixagem*

| # | Tarefa | Teste que nasceu antes |
|---|--------|------------------------|
| 1.1 | Normalizar o nome (minúsculo, sem espaços nas pontas, não vazio) | `InstrumentoTest` |
| 1.2 | Recusar nome repetido (409) | `MesaServiceTest` |
| 1.3 | Limitar a mesa a 8 instrumentos (422) | `MesaServiceTest` |
| 1.4 | Persistir no Postgres com constraint de unicidade | `InstrumentoRepositoryIT` |
| 1.5 | Endpoint `POST /api/instrumentos` | `InstrumentoApiIT` + `h1_adicionar_instrumento.feature` |

**H2 · Controlar BPM** — *como DJ, quero mudar o BPM para acompanhar a pista sem estragar o som*

| # | Tarefa | Teste que nasceu antes |
|---|--------|------------------------|
| 2.1 | Aceitar BPM só entre 40 e 300 | `InstrumentoTest` |
| 2.2 | Calcular o intervalo da batida em ms | `InstrumentoTest` |
| 2.3 | BPM inválido não altera o estado (422) | `MesaServiceTest` |
| 2.4 | Endpoint `PUT /api/instrumentos/{nome}/bpm` | `InstrumentoApiIT` + `h2_controlar_bpm.feature` |

**H3 · Tocar, pausar e volume** — *como DJ, quero ligar, desligar e mudar o volume para controlar a mixagem ao vivo*

| # | Tarefa | Teste que nasceu antes |
|---|--------|------------------------|
| 3.1 | Tocar / pausar | `InstrumentoTest` |
| 3.2 | Volume limitado entre 0 e 100 (clamping) | `InstrumentoTest` |
| 3.3 | Instrumento inexistente → 404 | `MesaServiceTest` |
| 3.4 | Endpoints de tocar, pausar e volume | `InstrumentoApiIT` + `h3_tocar_pausar_volume.feature` |

---

## Testes

### Estrutura de testes por nível

| Nível | Arquivo(s) | O que valida |
|-------|-----------|--------------|
| **Unitário** | `InstrumentoTest` | Regras puras do domínio: normalização do nome, faixa de BPM (40–300), clamping de volume (0–100) |
| **Unitário** | `MesaServiceTest` | Regras da mesa: nome duplicado → 409, limite de canais → 422, instrumento inexistente → 404. Usa Mockito, sem banco. |
| **Integração** | `InstrumentoRepositoryIT` | Repositório JPA contra PostgreSQL real (Testcontainers). Valida a migração Flyway e a constraint de unicidade do banco. |
| **Integração** | `InstrumentoApiIT` | Stack completa via MockMvc + PostgreSQL. Cobre todos os endpoints e os códigos HTTP esperados. |
| **Aceitação** | `h1_adicionar_instrumento.feature` | Cenários BDD da história H1: adicionar, nome repetido, mesa cheia. |
| **Aceitação** | `h2_controlar_bpm.feature` | Cenários BDD da história H2: BPM dentro e fora da faixa. |
| **Aceitação** | `h3_tocar_pausar_volume.feature` | Cenários BDD da história H3: tocar, pausar, volume, instrumento inexistente. |

### Rodar localmente

```bash
# só os unitários (rápido, sem Docker)
./mvnw test

# suite completa: unitários + integração + aceitação (precisa do Docker)
./mvnw verify
```

Os testes de integração e aceitação sobem um PostgreSQL descartável via **Testcontainers**. Não é necessário ter o banco instalado.

### Quais regras ganharam teste primeiro (e por quê)

1. **Faixa de BPM (40–300)** — é o maior risco: um BPM 0 travaria a thread/batida e um BPM absurdo estraga o som ao vivo.
2. **Nome repetido** — dois canais com o mesmo nome deixariam os comandos do DJ ambíguos.
3. **Limite de 8 instrumentos** — protege a mesa (e o áudio do navegador) de sobrecarga.
4. **Volume 0–100** — menor risco, porque é corrigido por clamping em vez de erro.

### O que ficou sem teste automatizado

- **Interface web (`mesa.js` / `mesa.css`) e o som da Web Audio API** — validados manualmente no navegador.
- **Deploy no Render** — coberto apenas pelo smoke test `/actuator/health` na esteira.
- **Rollback** — validado por execução manual do workflow (ver evidências).

### Histórico red → green

O histórico de commits mostra o ciclo TDD:

```
test: regras do instrumento (nome, BPM entre 40 e 300, volume 0-100)
feat: entidade Instrumento com validação de nome, BPM e volume
test: regras da mesa (nome repetido, limite de instrumentos, não encontrado)
feat: MesaService com limite de instrumentos e bloqueio de nome repetido
test: integração com Postgres e cenários BDD das três histórias
feat: controller REST, tratamento de erros e interface web
```

---

## Pipeline e deploy

### Fluxo (GitHub Flow)

```
feature/* ──PR──► main ──► deploy automático no Render
```

- `main` é protegida: push direto bloqueado, PR exige a esteira verde.
- Branches de feature seguem o padrão `feat/`, `fix/`, `test/`.

### Esteira (`.github/workflows/pipeline.yml`)

```
push / PR na main
       │
       ▼
   ┌─────────┐
   │ testes  │  mvn verify (unitários + integração + aceitação)
   └────┬────┘
        │ verde
        ▼
   ┌─────────┐
   │ imagem  │  docker build → publica no GHCR (só em push na main)
   └────┬────┘
        │
        ▼
   ┌─────────┐
   │  deploy │  dispara o deploy hook do Render + smoke test /actuator/health
   └─────────┘
```

### Rollback

O serviço no Render é ligado ao repositório Git e guarda o histórico de deploys. Para voltar uma versão:

1. Render → **DESAFIO_PI → Events**
2. No deploy bom anterior, clicar em **Rollback → Rollback to this deploy**
3. O Render recoloca no ar exatamente aquele build (sem rebuild) e desliga o Auto-Deploy, para que só a esteira volte a publicar.

Para voltar à versão mais nova: **Manual Deploy → Deploy latest commit**, ou um novo merge na `main`.

### URL pública

> **<https://desafio-pi.onrender.com>**
>
> Plano gratuito: após ~15 min sem acesso o serviço hiberna e o primeiro acesso pode levar ~50 s.

---

## Evidências

Todas as capturas ficam em [`docs/evidencias/`](docs/evidencias/README.md).

| Evidência | Arquivo |
|-----------|---------|
| `docker compose ps` com os dois serviços *healthy* | `docs/evidencias/01-compose-ps.png` |
| API respondendo (`/actuator/health` e `/api/instrumentos`) | `docs/evidencias/02-api-local.png` |
| Esteira **vermelha** barrando o deploy (commit `58005c1`) | `docs/evidencias/03-esteira-vermelha.png` |
| Esteira **verde** publicando no Render | `docs/evidencias/04-esteira-verde.png` |
| Pull Request com checks obrigatórios | `docs/evidencias/05-pull-request.png` |
| Proteção da branch `main` | `docs/evidencias/06-branch-protegida.png` |
| Aplicação no ar (URL pública) | `docs/evidencias/07-url-publica.png` |
| Rollback executado | `docs/evidencias/08-rollback.png` |

---

## Decisões e justificativas

| Decisão | Alternativas consideradas | Escolha e motivo |
|---------|--------------------------|-----------------|
| **Imagem base (build)** | `maven:3.9-eclipse-temurin-21`, `openjdk:21` puro | `maven:3.9-eclipse-temurin-21` no 1º estágio: já tem Maven e JDK, o cache de dependências fica numa camada separada. No 2º estágio, `eclipse-temurin:21-jre-alpine`: só tem JRE, ~180 MB vs ~600 MB do JDK completo. |
| **Multi-stage build** | Build fora do Docker (CI monta o jar antes) | Rodar o build dentro do Docker garante que qualquer máquina que tenha Docker reproduz exatamente o mesmo resultado. Os testes unitários são executados no 1º estágio; se quebrarem, a imagem não é gerada. |
| **Dados (volume)** | Bind mount (`./data:/var/lib/postgresql/data`) | Volume nomeado (`dados-mesadj`): sobrevive a `docker compose down`, independe do diretório de trabalho e é gerenciado pelo Docker. Bind mount facilita acesso direto ao sistema de arquivos, mas cria acoplamento com o SO do host. |
| **Banco** | MySQL, MongoDB, H2 | PostgreSQL 16: mesmo banco que o Render oferece gratuitamente, elimina diferença entre desenvolvimento e produção. H2 foi descartado porque queria testar as constraints reais (unicidade, CHECK de BPM). |
| **Testes de domínio primeiro** | Começar pela API e testar tudo integrado | Escrevemos os testes de `Instrumento` antes da entidade. Isso forçou a definir o contrato do domínio (como normalizar o nome, qual faixa de BPM) sem pensar em banco ou HTTP. Menor risco de lógica de negócio vazando para o controller. |
| **Testcontainers** | H2 nos testes de integração, banco local na porta fixa | Testcontainers sobe um PostgreSQL real isolado por execução. Pega problemas que o H2 não pegaria (dialeto SQL diferente, constraints de banco). Não exige que o desenvolvedor tenha o banco instalado. |
| **CI/CD** | GitLab CI, Jenkins, CircleCI | GitHub Actions: já é onde o repositório fica, sem conta extra. Os runners do Ubuntu têm Docker pré-instalado, o que simplifica o Testcontainers na esteira. |
| **Deploy** | Railway, Fly.io | Render: plano gratuito suficiente para a entrega, build direto do `Dockerfile` do repositório (mesma imagem do E1), banco PostgreSQL gerenciado na mesma região e rollback de 1 clique pelo histórico de deploys. O deploy é disparado pela esteira via deploy hook, só depois dos testes verdes. |
| **Fluxo de branch** | Git Flow (develop/release/hotfix) | GitHub Flow: grupo pequeno, uma versão no ar, ciclo de entrega curto. Uma branch por feature, PR → main → deploy. Git Flow teria cerimônia desnecessária. |
| **Segredos** | Segredos no código, `.env` commitado | `.env` no `.gitignore`, `.env.example` como modelo. Segredos de produção ficam no cofre de cada ferramenta: a senha do banco nas **Environment Variables do Render** (`DB_URL`, `DB_USER`, `DB_PASSWORD`) e o deploy hook nos **Secrets do GitHub** (`RENDER_DEPLOY_HOOK_URL`). O compose lê de variáveis de ambiente, sem valor padrão obrigatório para `DB_PASSWORD` (falha ruidosamente se não for definida). |

---

## Uso de IA

Usamos o **Antigravity** (assistente de programação com IA do Google) durante o desenvolvimento desta entrega. Abaixo declaramos onde e como:

| Etapa | O que a IA gerou | O que revisamos / alteramos |
|-------|------------------|-----------------------------|
| Estrutura do projeto | Esqueleto do `pom.xml`, `application.yml`, `MesaDjApplication.java` | Ajustamos as versões das dependências, removemos dependências desnecessárias |
| Domínio | `Instrumento.java`, `RegraDeNegocioException.java` | Validamos que as regras (nome sem espaço, BPM 40–300) batiam com o que a versão de console original fazia |
| Testes unitários | `InstrumentoTest`, `MesaServiceTest` | Revisamos os casos de borda; adicionamos o teste de `intervaloBatidaMs` que fazia referência à lógica original de threads |
| Testes de integração | `InstrumentoRepositoryIT`, `InstrumentoApiIT`, steps do Cucumber | Ajustamos o `@Transactional` nos steps para isolar cenários; corrigimos o cenário de "mesa cheia" |
| Dockerfile e compose | Gerado pela IA | Revisamos o `HEALTHCHECK`, o usuário sem root e o `depends_on` com `condition: service_healthy` |
| Pipeline | `.github/workflows/pipeline.yml` | Ajustamos o encoding da tag da imagem e o smoke test pós-deploy |
| Deploy no Render | Passo a passo de configuração (banco, variáveis, deploy hook) e diagnóstico de erros | Corrigimos a senha do banco após erro de autenticação nos logs; trocamos o deploy por imagem pelo deploy do repositório; rollback feito e validado pelo painel |
| README | Gerado com base nas decisões tomadas pelo grupo | Revisado e complementado pelo grupo antes de commitar |

Os prompts principais foram: *"monte a estrutura de um projeto Spring Boot com TDD para a Mesa DJ"*, *"escreva os testes unitários das regras de BPM e nome do instrumento antes da implementação"*, *"crie um Dockerfile multi-stage para Java 21 com PostgreSQL via compose"* e *"me guie passo a passo no deploy no Render"*.

---

## Jornada TDD e lições aprendidas

- **Teste primeiro obriga a decidir a regra.** Antes de escrever `Instrumento`, tivemos que responder "qual é o BPM mínimo?" e "nome com espaço vale?". O teste virou a especificação.
- **Cada nível pega um tipo de erro.** O unitário pega regra errada em milissegundos; o de integração (Testcontainers) pega erro de banco que o H2 esconderia; o BDD garante que a história do DJ funciona de ponta a ponta.
- **A esteira vermelha é a prova de que o gate funciona.** Quebramos o BPM de propósito (`58005c1`): os testes falharam e o deploy foi pulado. O código quebrado nunca chegou ao ar.
- **"Funciona na minha máquina" sumiu.** O mesmo `Dockerfile` roda no PC, na esteira e no Render.
- **Produção ensina o que o teste não ensina.** O primeiro deploy falhou por senha errada do banco. O erro estava claro nos logs, e foi resolvido sem mexer no código, porque a configuração estava fora dele.
- **IA acelera, mas a gente valida.** A IA rascunhou código e testes; revisamos os casos de borda e as regras contra a versão original de console.