CREATE TABLE usuario (
    id         BIGSERIAL PRIMARY KEY,
    nome       VARCHAR(120) NOT NULL,
    email      VARCHAR(160) NOT NULL UNIQUE,
    senha_hash VARCHAR(100) NOT NULL,
    role       VARCHAR(20)  NOT NULL CHECK (role IN ('CLIENTE', 'ADMIN')),
    criado_em  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE endereco (
    id          BIGSERIAL PRIMARY KEY,
    usuario_id  BIGINT       NOT NULL REFERENCES usuario (id) ON DELETE CASCADE,
    cep         VARCHAR(9)   NOT NULL,
    logradouro  VARCHAR(150) NOT NULL,
    numero      VARCHAR(20)  NOT NULL,
    complemento VARCHAR(80),
    bairro      VARCHAR(80)  NOT NULL,
    cidade      VARCHAR(80)  NOT NULL,
    uf          VARCHAR(2)   NOT NULL,
    criado_em   TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_endereco_usuario ON endereco (usuario_id);
