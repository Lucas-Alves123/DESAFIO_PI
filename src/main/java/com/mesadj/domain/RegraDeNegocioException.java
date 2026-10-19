package com.mesadj.domain;

/** Violação de uma regra da mesa. A API devolve 422. */
public class RegraDeNegocioException extends RuntimeException {

    public RegraDeNegocioException(String mensagem) {
        super(mensagem);
    }
}
