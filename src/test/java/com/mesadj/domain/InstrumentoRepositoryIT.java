package com.mesadj.domain;

import com.mesadj.PostgresTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(PostgresTestConfig.class)
class InstrumentoRepositoryIT {

    @Autowired
    InstrumentoRepository repository;

    @Test
    void migracaoJaCriaBateriaEBaixo() {
        assertThat(repository.findAllByOrderByIdAsc())
                .extracting(Instrumento::getNome)
                .containsExactly("bateria", "baixo");
    }

    @Test
    void salvaEBuscaPeloNome() {
        Instrumento salvo = repository.saveAndFlush(new Instrumento("Guitarra", "guitarra.wav"));
        salvo.alterarBpm(90);
        salvo.tocar();
        repository.saveAndFlush(salvo);

        Instrumento lido = repository.findByNome("guitarra").orElseThrow();
        assertThat(lido.getBpm()).isEqualTo(90);
        assertThat(lido.isTocando()).isTrue();
        assertThat(repository.existsByNome("guitarra")).isTrue();
    }

    @Test
    void bancoTambemBarraNomeRepetido() {
        assertThatThrownBy(() -> repository.saveAndFlush(new Instrumento("bateria", null)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
