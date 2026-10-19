package com.mesadj.service;

import com.mesadj.domain.Instrumento;
import com.mesadj.domain.InstrumentoRepository;
import com.mesadj.domain.RegraDeNegocioException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MesaService {

    private final InstrumentoRepository repository;
    private final int limiteInstrumentos;

    public MesaService(InstrumentoRepository repository,
                       @Value("${mesa.limite-instrumentos:8}") int limiteInstrumentos) {
        this.repository = repository;
        this.limiteInstrumentos = limiteInstrumentos;
    }

    @Transactional(readOnly = true)
    public List<Instrumento> listar() {
        return repository.findAllByOrderByIdAsc();
    }

    @Transactional(readOnly = true)
    public Instrumento buscar(String nome) {
        String chave = Instrumento.normalizarNome(nome);
        return repository.findByNome(chave)
                .orElseThrow(() -> new InstrumentoNaoEncontradoException(chave));
    }

    @Transactional
    public Instrumento adicionar(String nome, String arquivoAudio) {
        Instrumento novo = new Instrumento(nome, arquivoAudio);
        if (repository.existsByNome(novo.getNome())) {
            throw new InstrumentoDuplicadoException(novo.getNome());
        }
        if (repository.count() >= limiteInstrumentos) {
            throw new RegraDeNegocioException(
                    "A mesa já está cheia (máximo de " + limiteInstrumentos + " instrumentos)");
        }
        return repository.save(novo);
    }

    @Transactional
    public Instrumento tocar(String nome) {
        Instrumento i = buscar(nome);
        i.tocar();
        return repository.save(i);
    }

    @Transactional
    public Instrumento pausar(String nome) {
        Instrumento i = buscar(nome);
        i.pausar();
        return repository.save(i);
    }

    @Transactional
    public Instrumento alterarBpm(String nome, int bpm) {
        Instrumento i = buscar(nome);
        i.alterarBpm(bpm);
        return repository.save(i);
    }

    @Transactional
    public Instrumento ajustarVolume(String nome, int volume) {
        Instrumento i = buscar(nome);
        i.ajustarVolume(volume);
        return repository.save(i);
    }

    @Transactional
    public void remover(String nome) {
        repository.delete(buscar(nome));
    }
}
