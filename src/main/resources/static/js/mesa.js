// Mesa DJ - interface web
// O servidor guarda o estado (tocando, BPM, volume). O som é gerado aqui no navegador
// com Web Audio, porque o servidor na nuvem não tem placa de som.

const API = "/api/instrumentos";
const canaisEl = document.getElementById("canais");
const tpl = document.getElementById("tpl-canal");
const toastEl = document.getElementById("toast");
const btnSom = document.getElementById("btn-som");

let audio = null;          // AudioContext, criado só depois de um clique (regra dos navegadores)
const loops = new Map();   // nome -> id do setInterval

// ---------- API ----------
async function chamar(metodo, url, corpo) {
  const resp = await fetch(url, {
    method: metodo,
    headers: corpo ? { "Content-Type": "application/json" } : {},
    body: corpo ? JSON.stringify(corpo) : undefined,
  });
  if (resp.status === 204) return null;
  const dados = await resp.json().catch(() => ({}));
  if (!resp.ok) throw new Error(dados.detail || `Erro ${resp.status}`);
  return dados;
}

function aviso(msg, ok = false) {
  toastEl.textContent = msg;
  toastEl.classList.toggle("ok", ok);
  toastEl.classList.add("visivel");
  clearTimeout(aviso.t);
  aviso.t = setTimeout(() => toastEl.classList.remove("visivel"), 2800);
}

// ---------- som ----------
function batida(inst) {
  if (!audio) return;
  const agora = audio.currentTime;
  const ganho = audio.createGain();
  const osc = audio.createOscillator();
  const vol = (inst.volume / 100) * 0.5;

  // cada instrumento com um timbre simples diferente
  const timbres = {
    bateria: () => { osc.type = "sine"; osc.frequency.setValueAtTime(150, agora); osc.frequency.exponentialRampToValueAtTime(40, agora + 0.15); },
    baixo:   () => { osc.type = "sawtooth"; osc.frequency.setValueAtTime(55, agora); },
  };
  (timbres[inst.nome] || (() => {
    osc.type = "triangle";
    const notas = [262, 330, 392, 523];
    let h = 0; for (const c of inst.nome) h += c.charCodeAt(0);
    osc.frequency.setValueAtTime(notas[h % notas.length], agora);
  }))();

  ganho.gain.setValueAtTime(vol, agora);
  ganho.gain.exponentialRampToValueAtTime(0.001, agora + 0.25);
  osc.connect(ganho).connect(audio.destination);
  osc.start(agora);
  osc.stop(agora + 0.26);
}

function atualizarLoop(inst, card) {
  clearInterval(loops.get(inst.nome));
  loops.delete(inst.nome);
  if (!inst.tocando) return;
  const id = setInterval(() => {
    batida(inst);
    card.classList.add("batida");
    setTimeout(() => card.classList.remove("batida"), 80);
  }, inst.intervaloBatidaMs);
  loops.set(inst.nome, id);
}

btnSom.addEventListener("click", async () => {
  if (!audio) audio = new AudioContext();
  if (audio.state === "running") { await audio.suspend(); }
  else { await audio.resume(); }
  const ligado = audio.state === "running";
  btnSom.textContent = ligado ? "🔊 Som ligado" : "🔇 Ativar som";
  btnSom.classList.toggle("ativo", ligado);
});

// ---------- tela ----------
function desenharCanal(inst) {
  let card = canaisEl.querySelector(`[data-nome="${inst.nome}"]`);
  if (!card) {
    card = tpl.content.firstElementChild.cloneNode(true);
    card.dataset.nome = inst.nome;
    card.id = `canal-${inst.nome}`;
    ligarEventos(card);
    canaisEl.appendChild(card);
  }
  card._inst = inst;
  card.querySelector(".nome").textContent = inst.nome;
  card.querySelector(".bpm").value = inst.bpm;
  card.querySelector(".bpm-valor").textContent = inst.bpm;
  card.querySelector(".volume").value = inst.volume;
  card.querySelector(".vol-valor").textContent = inst.volume;
  card.querySelector(".btn-play").textContent = inst.tocando ? "⏸ Pausar" : "▶ Tocar";
  card.classList.toggle("tocando", inst.tocando);
  card.style.setProperty("--intervalo", `${inst.intervaloBatidaMs}ms`);
  card.style.setProperty("--nivel", Math.max(0.2, inst.volume / 100));
  atualizarLoop(inst, card);
}

function ligarEventos(card) {
  const nome = () => card.dataset.nome;
  const url = (acao) => `${API}/${encodeURIComponent(nome())}${acao}`;

  card.querySelector(".btn-play").addEventListener("click", async () => {
    try { desenharCanal(await chamar("POST", url(card._inst.tocando ? "/pausar" : "/tocar"))); }
    catch (e) { aviso(e.message); }
  });

  const bpm = card.querySelector(".bpm");
  bpm.addEventListener("input", () => card.querySelector(".bpm-valor").textContent = bpm.value);
  bpm.addEventListener("change", async () => {
    try { desenharCanal(await chamar("PUT", url("/bpm"), { valor: Number(bpm.value) })); }
    catch (e) { aviso(e.message); }
  });

  const vol = card.querySelector(".volume");
  vol.addEventListener("input", () => card.querySelector(".vol-valor").textContent = vol.value);
  vol.addEventListener("change", async () => {
    try { desenharCanal(await chamar("PUT", url("/volume"), { valor: Number(vol.value) })); }
    catch (e) { aviso(e.message); }
  });

  card.querySelector(".remover").addEventListener("click", async () => {
    try {
      await chamar("DELETE", url(""));
      clearInterval(loops.get(nome()));
      card.remove();
      atualizarContador();
    } catch (e) { aviso(e.message); }
  });
}

function atualizarContador() {
  const n = canaisEl.children.length;
  document.getElementById("contador").textContent = `${n} ${n === 1 ? "canal" : "canais"}`;
}

document.getElementById("form-add").addEventListener("submit", async (ev) => {
  ev.preventDefault();
  const input = document.getElementById("input-nome");
  try {
    const novo = await chamar("POST", API, { nome: input.value });
    desenharCanal(novo);
    atualizarContador();
    aviso(`"${novo.nome}" entrou na mesa`, true);
    input.value = "";
  } catch (e) { aviso(e.message); }
});

async function carregar() {
  try {
    (await chamar("GET", API)).forEach(desenharCanal);
    atualizarContador();
  } catch (e) { aviso("Não consegui falar com a API: " + e.message); }
}

carregar();
