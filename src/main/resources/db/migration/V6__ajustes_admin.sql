CREATE UNIQUE INDEX uk_produto_nome ON produto (lower(nome));

CREATE INDEX idx_pedido_status_criado ON pedido (status, criado_em DESC);
