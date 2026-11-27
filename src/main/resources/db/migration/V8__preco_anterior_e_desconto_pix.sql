ALTER TABLE produto ADD COLUMN preco_anterior NUMERIC(10, 2);

ALTER TABLE produto
    ADD CONSTRAINT ck_produto_preco_anterior CHECK (preco_anterior IS NULL OR preco_anterior > preco);

ALTER TABLE pedido ADD COLUMN desconto NUMERIC(10, 2) NOT NULL DEFAULT 0 CHECK (desconto >= 0);
