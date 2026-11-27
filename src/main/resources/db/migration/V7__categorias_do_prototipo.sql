ALTER TABLE categoria ADD COLUMN ordem INTEGER NOT NULL DEFAULT 0;

UPDATE categoria SET nome = 'Games', slug = 'games' WHERE slug = 'games-e-controles';
UPDATE categoria SET nome = 'Acessórios', slug = 'acessorios' WHERE slug = 'carregadores-e-cabos';

INSERT INTO categoria (nome, slug)
SELECT novas.nome, novas.slug
FROM (VALUES ('Eletrônicos', 'eletronicos'),
             ('Games', 'games'),
             ('Ferramentas', 'ferramentas'),
             ('Acessórios', 'acessorios'),
             ('Áudio', 'audio'),
             ('Utilidades', 'utilidades')) AS novas (nome, slug)
WHERE NOT EXISTS (SELECT 1 FROM categoria c WHERE c.slug = novas.slug);

UPDATE produto
SET categoria_id = (SELECT id FROM categoria WHERE slug = 'acessorios')
WHERE categoria_id IN (SELECT id FROM categoria WHERE slug = 'relogios');

UPDATE produto
SET categoria_id = (SELECT id FROM categoria WHERE slug = 'utilidades')
WHERE categoria_id IN (SELECT id FROM categoria WHERE slug = 'barbeadores');

DELETE FROM categoria WHERE slug IN ('relogios', 'barbeadores');

UPDATE categoria
SET ordem = CASE slug
                WHEN 'eletronicos' THEN 1
                WHEN 'games' THEN 2
                WHEN 'ferramentas' THEN 3
                WHEN 'acessorios' THEN 4
                WHEN 'audio' THEN 5
                WHEN 'utilidades' THEN 6
                ELSE 99
            END;
