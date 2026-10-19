package com.mesadj.web;

import com.mesadj.domain.RegraDeNegocioException;
import com.mesadj.service.InstrumentoDuplicadoException;
import com.mesadj.service.InstrumentoNaoEncontradoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class TratadorDeErros {

    @ExceptionHandler(InstrumentoNaoEncontradoException.class)
    public ProblemDetail naoEncontrado(InstrumentoNaoEncontradoException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(InstrumentoDuplicadoException.class)
    public ProblemDetail duplicado(InstrumentoDuplicadoException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(RegraDeNegocioException.class)
    public ProblemDetail regra(RegraDeNegocioException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage());
    }
}
