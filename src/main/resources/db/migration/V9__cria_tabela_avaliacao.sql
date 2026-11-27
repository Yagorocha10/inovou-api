CREATE TABLE avaliacao (
    id         BIGSERIAL PRIMARY KEY,
    produto_id BIGINT       NOT NULL REFERENCES produto (id) ON DELETE CASCADE,
    usuario_id BIGINT       NOT NULL REFERENCES usuario (id) ON DELETE CASCADE,
    autor      VARCHAR(80)  NOT NULL,
    nota       INTEGER      NOT NULL CHECK (nota BETWEEN 1 AND 5),
    comentario VARCHAR(1000),
    criado_em  TIMESTAMP    NOT NULL,
    CONSTRAINT uk_avaliacao_produto_usuario UNIQUE (produto_id, usuario_id)
);

CREATE INDEX idx_avaliacao_produto_criado ON avaliacao (produto_id, criado_em DESC);
CREATE INDEX idx_avaliacao_criado ON avaliacao (criado_em DESC);
