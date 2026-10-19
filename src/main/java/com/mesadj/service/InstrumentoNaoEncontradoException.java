package com.mesadj.service;

/** A API devolve 404. */
public class InstrumentoNaoEncontradoException extends RuntimeException {

    public InstrumentoNaoEncontradoException(String nome) {
        super("Instrumento '" + nome + "' não está na mesa");
    }
}
