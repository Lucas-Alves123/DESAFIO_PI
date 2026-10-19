package com.mesadj.web;

import com.mesadj.domain.Instrumento;
import com.mesadj.service.MesaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/instrumentos")
public class InstrumentoController {

    public record NovoInstrumento(@NotBlank String nome, String arquivoAudio) {}

    public record Valor(@NotNull Integer valor) {}

    public record InstrumentoDto(String nome, String arquivoAudio, int bpm, int volume,
                                 boolean tocando, long intervaloBatidaMs) {
        static InstrumentoDto de(Instrumento i) {
            return new InstrumentoDto(i.getNome(), i.getArquivoAudio(), i.getBpm(), i.getVolume(),
                    i.isTocando(), i.intervaloBatidaMs());
        }
    }

    private final MesaService mesa;

    public InstrumentoController(MesaService mesa) {
        this.mesa = mesa;
    }

    @GetMapping
    public List<InstrumentoDto> listar() {
        return mesa.listar().stream().map(InstrumentoDto::de).toList();
    }

    @GetMapping("/{nome}")
    public InstrumentoDto buscar(@PathVariable String nome) {
        return InstrumentoDto.de(mesa.buscar(nome));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InstrumentoDto adicionar(@RequestBody @Valid NovoInstrumento body) {
        return InstrumentoDto.de(mesa.adicionar(body.nome(), body.arquivoAudio()));
    }

    @PostMapping("/{nome}/tocar")
    public InstrumentoDto tocar(@PathVariable String nome) {
        return InstrumentoDto.de(mesa.tocar(nome));
    }

    @PostMapping("/{nome}/pausar")
    public InstrumentoDto pausar(@PathVariable String nome) {
        return InstrumentoDto.de(mesa.pausar(nome));
    }

    @PutMapping("/{nome}/bpm")
    public InstrumentoDto bpm(@PathVariable String nome, @RequestBody @Valid Valor body) {
        return InstrumentoDto.de(mesa.alterarBpm(nome, body.valor()));
    }

    @PutMapping("/{nome}/volume")
    public InstrumentoDto volume(@PathVariable String nome, @RequestBody @Valid Valor body) {
        return InstrumentoDto.de(mesa.ajustarVolume(nome, body.valor()));
    }

    @DeleteMapping("/{nome}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(@PathVariable String nome) {
        mesa.remover(nome);
    }
}
