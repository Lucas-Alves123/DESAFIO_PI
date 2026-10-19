package com.mesadj.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.regex.Pattern;

@Entity
@Table(name = "instrumento")
public class Instrumento {

    public static final int BPM_MIN = 40;
    public static final int BPM_MAX = 300;
    public static final int BPM_PADRAO = 120;
    public static final int VOLUME_PADRAO = 80;

    private static final Pattern NOME_VALIDO = Pattern.compile("[a-z0-9_-]{1,40}");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String nome;

    @Column(name = "arquivo_audio")
    private String arquivoAudio;

    @Column(nullable = false)
    private int bpm = BPM_PADRAO;

    @Column(nullable = false)
    private int volume = VOLUME_PADRAO;

    @Column(nullable = false)
    private boolean tocando = false;

    protected Instrumento() {
        // JPA
    }

    public Instrumento(String nome, String arquivoAudio) {
        this.nome = normalizarNome(nome);
        this.arquivoAudio = arquivoAudio;
    }

    /**
     * Na versão de console o comando era "play bateria", então o nome não pode ter espaço.
     * Guardamos sempre em minúsculo pra "Bateria" e "bateria" serem o mesmo instrumento.
     */
    public static String normalizarNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new RegraDeNegocioException("Nome do instrumento é obrigatório");
        }
        String normalizado = nome.trim().toLowerCase();
        if (!NOME_VALIDO.matcher(normalizado).matches()) {
            throw new RegraDeNegocioException(
                    "Nome inválido: use só letras, números, - ou _ (sem espaço, até 40 caracteres)");
        }
        return normalizado;
    }

    public void tocar() {
        this.tocando = true;
    }

    public void pausar() {
        this.tocando = false;
    }

    public void alterarBpm(int novoBpm) {
        if (novoBpm < BPM_MIN || novoBpm > BPM_MAX) {
            throw new RegraDeNegocioException(
                    "BPM deve estar entre " + BPM_MIN + " e " + BPM_MAX + " (recebido: " + novoBpm + ")");
        }
        this.bpm = novoBpm;
    }

    /** Igual à versão antiga: valor fora da faixa é ajustado pro limite, não dá erro. */
    public void ajustarVolume(int novoVolume) {
        this.volume = Math.max(0, Math.min(100, novoVolume));
    }

    public long intervaloBatidaMs() {
        return 60_000L / bpm;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getArquivoAudio() {
        return arquivoAudio;
    }

    public int getBpm() {
        return bpm;
    }

    public int getVolume() {
        return volume;
    }

    public boolean isTocando() {
        return tocando;
    }
}
