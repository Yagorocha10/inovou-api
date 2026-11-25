CREATE TABLE pedido (
    id                   BIGSERIAL PRIMARY KEY,
    codigo               VARCHAR(20)   UNIQUE,
    usuario_id           BIGINT        NOT NULL REFERENCES usuario (id),
    status               VARCHAR(30)   NOT NULL CHECK (status IN ('AGUARDANDO_PAGAMENTO', 'PAGO', 'EM_SEPARACAO',
                                                                  'SAIU_PARA_ENTREGA', 'ENTREGUE', 'CANCELADO')),
    forma_pagamento      VARCHAR(20)   NOT NULL CHECK (forma_pagamento IN ('PIX', 'CARTAO', 'NA_ENTREGA')),
    pagamento_aprovado   BOOLEAN       NOT NULL DEFAULT FALSE,
    instrucoes_pagamento VARCHAR(300)  NOT NULL DEFAULT '',
    codigo_pix           VARCHAR(300),
    frete                NUMERIC(10, 2) NOT NULL CHECK (frete >= 0),
    entrega_cep          VARCHAR(9)    NOT NULL,
    entrega_logradouro   VARCHAR(150)  NOT NULL,
    entrega_numero       VARCHAR(20)   NOT NULL,
    entrega_complemento  VARCHAR(80),
    entrega_bairro       VARCHAR(80)   NOT NULL,
    entrega_cidade       VARCHAR(80)   NOT NULL,
    entrega_uf           VARCHAR(2)    NOT NULL,
    criado_em            TIMESTAMP     NOT NULL,
    atualizado_em        TIMESTAMP     NOT NULL
);

CREATE INDEX idx_pedido_usuario_criado ON pedido (usuario_id, criado_em DESC);

CREATE TABLE item_pedido (
    id             BIGSERIAL PRIMARY KEY,
    pedido_id      BIGINT         NOT NULL REFERENCES pedido (id) ON DELETE CASCADE,
    produto_id     BIGINT         NOT NULL REFERENCES produto (id),
    nome           VARCHAR(150)   NOT NULL,
    slug           VARCHAR(180)   NOT NULL,
    imagem         VARCHAR(500),
    preco_unitario NUMERIC(10, 2) NOT NULL CHECK (preco_unitario > 0),
    quantidade     INTEGER        NOT NULL CHECK (quantidade > 0)
);

CREATE INDEX idx_item_pedido_pedido ON item_pedido (pedido_id);
