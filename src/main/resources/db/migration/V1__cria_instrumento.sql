CREATE TABLE instrumento (
    id            BIGSERIAL PRIMARY KEY,
    nome          VARCHAR(40)  NOT NULL UNIQUE,
    arquivo_audio VARCHAR(255),
    bpm           INTEGER      NOT NULL DEFAULT 120 CHECK (bpm BETWEEN 40 AND 300),
    volume        INTEGER      NOT NULL DEFAULT 80  CHECK (volume BETWEEN 0 AND 100),
    tocando       BOOLEAN      NOT NULL DEFAULT FALSE
);

-- os dois instrumentos que a mesa antiga já carregava ao iniciar
INSERT INTO instrumento (nome, arquivo_audio) VALUES ('bateria', 'bateria.wav');
INSERT INTO instrumento (nome, arquivo_audio) VALUES ('baixo', 'baixo.wav');
