INSERT INTO categoria (nome, slug) VALUES
    ('Eletrônicos', 'eletronicos'),
    ('Carregadores e cabos', 'carregadores-e-cabos'),
    ('Games e controles', 'games-e-controles'),
    ('Relógios', 'relogios'),
    ('Barbeadores', 'barbeadores'),
    ('Ferramentas', 'ferramentas');

INSERT INTO produto (categoria_id, nome, slug, descricao, preco, estoque, criado_em) VALUES
    ((SELECT id FROM categoria WHERE slug = 'games-e-controles'),
     'Mouse Gamer Attack Shark X11 Wireless', 'mouse-gamer-attack-shark-x11-wireless',
     'Mouse gamer sem fio Attack Shark X11.', 10.00, 10, NOW() - INTERVAL '4 minutes'),
    ((SELECT id FROM categoria WHERE slug = 'games-e-controles'),
     'Controle PS4 sem fio Bluetooth', 'controle-ps4-sem-fio-bluetooth',
     'Controle sem fio Bluetooth com vibração dupla.', 20.00, 10, NOW() - INTERVAL '3 minutes'),
    ((SELECT id FROM categoria WHERE slug = 'carregadores-e-cabos'),
     'Cabo USB-C para Lightning 20W 1m', 'cabo-usb-c-para-lightning-20w-1m',
     'Cabo USB-C para Lightning de 1 metro, compatível com carregamento de 20W.', 30.00, 10, NOW() - INTERVAL '2 minutes'),
    ((SELECT id FROM categoria WHERE slug = 'games-e-controles'),
     'Console portátil R36', 'console-portatil-r36',
     'Videogame portátil R36.', 40.00, 5, NOW() - INTERVAL '1 minute'),
    ((SELECT id FROM categoria WHERE slug = 'games-e-controles'),
     'Game Stick', 'game-stick',
     'Videogame Game Stick para ligar na TV.', 50.00, 0, NOW());
