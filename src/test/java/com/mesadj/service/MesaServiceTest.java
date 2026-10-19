package com.mesadj.service;

import com.mesadj.domain.Instrumento;
import com.mesadj.domain.InstrumentoRepository;
import com.mesadj.domain.RegraDeNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MesaServiceTest {

    private InstrumentoRepository repository;
    private MesaService mesa;

    @BeforeEach
    void setUp() {
        repository = mock(InstrumentoRepository.class);
        when(repository.save(any(Instrumento.class))).thenAnswer(inv -> inv.getArgument(0));
        mesa = new MesaService(repository, 3);
    }

    @Test
    void adicionaInstrumentoNovo() {
        when(repository.existsByNome("guitarra")).thenReturn(false);
        when(repository.count()).thenReturn(2L);

        Instrumento criado = mesa.adicionar("Guitarra", "guitarra.wav");

        assertThat(criado.getNome()).isEqualTo("guitarra");
        verify(repository).save(any(Instrumento.class));
    }

    @Test
    void naoDeixaAdicionarDoisInstrumentosComOMesmoNome() {
        when(repository.existsByNome("bateria")).thenReturn(true);

        assertThatThrownBy(() -> mesa.adicionar("BATERIA", null))
                .isInstanceOf(InstrumentoDuplicadoException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void naoDeixaPassarDoLimiteDeInstrumentos() {
        when(repository.existsByNome("synth")).thenReturn(false);
        when(repository.count()).thenReturn(3L);

        assertThatThrownBy(() -> mesa.adicionar("synth", null))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("3");
        verify(repository, never()).save(any());
    }

    @Test
    void instrumentoInexistenteDaErroDeNaoEncontrado() {
        when(repository.findByNome("piano")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> mesa.tocar("piano"))
                .isInstanceOf(InstrumentoNaoEncontradoException.class);
    }

    @Test
    void tocarSalvaOEstado() {
        Instrumento baixo = new Instrumento("baixo", null);
        when(repository.findByNome("baixo")).thenReturn(Optional.of(baixo));

        Instrumento resultado = mesa.tocar("Baixo");

        assertThat(resultado.isTocando()).isTrue();
        verify(repository).save(baixo);
    }

    @Test
    void bpmInvalidoNaoChegaASerSalvo() {
        Instrumento bateria = new Instrumento("bateria", null);
        when(repository.findByNome("bateria")).thenReturn(Optional.of(bateria));

        assertThatThrownBy(() -> mesa.alterarBpm("bateria", 500))
                .isInstanceOf(RegraDeNegocioException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void volumeEhAjustadoESalvo() {
        Instrumento bateria = new Instrumento("bateria", null);
        when(repository.findByNome("bateria")).thenReturn(Optional.of(bateria));

        assertThat(mesa.ajustarVolume("bateria", 120).getVolume()).isEqualTo(100);
        verify(repository).save(bateria);
    }
}
