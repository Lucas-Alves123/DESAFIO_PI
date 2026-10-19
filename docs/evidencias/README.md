# Evidências

Salve os prints nesta pasta com exatamente estes nomes:

| Arquivo | O que mostrar |
|---------|---------------|
| `01-compose-ps.png` | Saída de `docker compose ps` com `db` e `api` *healthy* |
| `02-api-local.png` | `curl localhost:8080/actuator/health` e `/api/instrumentos` respondendo |
| `03-esteira-vermelha.png` | Actions: execução do commit `58005c1` com o job de testes vermelho e deploy *skipped* |
| `04-esteira-verde.png` | Actions: execução verde com testes → imagem → deploy |
| `05-pull-request.png` | Pull Request com os checks da esteira passando antes do merge |
| `06-branch-protegida.png` | Settings → Branches: regra de proteção da `main` |
| `07-url-publica.png` | Aplicação aberta na URL do Render |
| `08-rollback.png` | Render: deploy com *"Rolled back by you via Dashboard"* e status Live |
