CREATE TABLE carrinho (
    id            BIGSERIAL PRIMARY KEY,
    usuario_id    BIGINT    NOT NULL UNIQUE REFERENCES usuario (id) ON DELETE CASCADE,
    atualizado_em TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE item_carrinho (
    id          BIGSERIAL PRIMARY KEY,
    carrinho_id BIGINT  NOT NULL REFERENCES carrinho (id) ON DELETE CASCADE,
    produto_id  BIGINT  NOT NULL REFERENCES produto (id),
    quantidade  INTEGER NOT NULL CHECK (quantidade > 0),
    CONSTRAINT uk_item_carrinho_produto UNIQUE (carrinho_id, produto_id)
);

CREATE INDEX idx_item_carrinho_produto ON item_carrinho (produto_id);
