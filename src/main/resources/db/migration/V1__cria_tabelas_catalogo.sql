CREATE TABLE categoria (
    id   BIGSERIAL PRIMARY KEY,
    nome VARCHAR(80)  NOT NULL,
    slug VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE produto (
    id            BIGSERIAL PRIMARY KEY,
    categoria_id  BIGINT         NOT NULL REFERENCES categoria (id),
    nome          VARCHAR(150)   NOT NULL,
    slug          VARCHAR(180)   NOT NULL UNIQUE,
    descricao     VARCHAR(2000)  NOT NULL DEFAULT '',
    preco         NUMERIC(10, 2) NOT NULL CHECK (preco > 0),
    estoque       INTEGER        NOT NULL DEFAULT 0 CHECK (estoque >= 0),
    ativo         BOOLEAN        NOT NULL DEFAULT TRUE,
    criado_em     TIMESTAMP      NOT NULL DEFAULT NOW(),
    atualizado_em TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_produto_categoria ON produto (categoria_id);
CREATE INDEX idx_produto_ativo_criado_em ON produto (ativo, criado_em DESC);

CREATE TABLE imagem_produto (
    id         BIGSERIAL PRIMARY KEY,
    produto_id BIGINT       NOT NULL REFERENCES produto (id) ON DELETE CASCADE,
    url        VARCHAR(500) NOT NULL,
    ordem      INTEGER      NOT NULL DEFAULT 0
);

CREATE INDEX idx_imagem_produto_produto ON imagem_produto (produto_id);
