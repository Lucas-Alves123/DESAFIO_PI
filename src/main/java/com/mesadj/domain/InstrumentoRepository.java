package com.mesadj.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InstrumentoRepository extends JpaRepository<Instrumento, Long> {

    Optional<Instrumento> findByNome(String nome);

    boolean existsByNome(String nome);

    List<Instrumento> findAllByOrderByIdAsc();
}
