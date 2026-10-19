package com.mesadj.service;

import com.mesadj.domain.RegraDeNegocioException;

/** Já existe um instrumento com esse nome na mesa. A API devolve 409. */
public class InstrumentoDuplicadoException extends RegraDeNegocioException {

    public InstrumentoDuplicadoException(String nome) {
        super("Já existe um instrumento chamado '" + nome + "' na mesa");
    }
}
