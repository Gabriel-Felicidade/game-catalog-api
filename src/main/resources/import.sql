-- Insere Perfil para a CD Projekt Red (id 1)
INSERT INTO PerfilDesenvolvedora (id, historia, principaisFranquias) VALUES (1, 'Fundada em 1994, a CD Projekt Red é um estúdio polonês famoso por sua série de jogos The Witcher e pelo ambicioso Cyberpunk 2077.', 'The Witcher, Cyberpunk');
-- Insere CD Projekt Red e associa ao perfil 1
INSERT INTO Desenvolvedora (id, nome, fundacao, nacionalidade, perfil_id) VALUES (1, 'CD Projekt Red', '1994-05-01', 'Polonesa', 1);

-- Insere Perfil para a Naughty Dog (id 2)
INSERT INTO PerfilDesenvolvedora (id, historia, principaisFranquias) VALUES (2, 'Conhecida por suas narrativas cinematográficas, a Naughty Dog é um estúdio americano parte da PlayStation Studios. Criou franquias icônicas como Crash Bandicoot, Uncharted e The Last of Us.', 'Uncharted, The Last of Us');
-- Insere Naughty Dog e associa ao perfil 2
INSERT INTO Desenvolvedora (id, nome, fundacao, nacionalidade, perfil_id) VALUES (2, 'Naughty Dog', '1984-09-01', 'Americana', 2);

-- Insere Gêneros
INSERT INTO Genero (id, nome, descricao) VALUES (1, 'RPG de Ação', 'Combina elementos de RPG com combate em tempo real.');
INSERT INTO Genero (id, nome, descricao) VALUES (2, 'Mundo Aberto', 'Apresenta um vasto mundo para exploração livre.');
INSERT INTO Genero (id, nome, descricao) VALUES (3, 'Ação-Aventura', 'Foco em exploração, quebra-cabeças e combate.');

-- Insere Jogos
INSERT INTO Jogo (id, titulo, sinopse, anoLancamento, notaCritica, desenvolvedora_id) VALUES (1, 'The Witcher 3: Wild Hunt', 'Geralt de Rívia, um caçador de monstros, procura por sua filha adotiva.', 2015, 9.3, 1);
INSERT INTO Jogo (id, titulo, sinopse, anoLancamento, notaCritica, desenvolvedora_id) VALUES (2, 'Cyberpunk 2077', 'Um mercenário busca um implante único que é a chave para a imortalidade.', 2020, 7.2, 1);
INSERT INTO Jogo (id, titulo, sinopse, anoLancamento, notaCritica, desenvolvedora_id) VALUES (3, 'Uncharted 4: A Thief''s End', 'O caçador de tesouros Nathan Drake é forçado a voltar ao mundo dos ladrões.', 2016, 9.3, 2);

-- Tabela de junção Jogo <-> Gênero
INSERT INTO jogo_genero (jogo_id, genero_id) VALUES (1, 1); -- The Witcher 3 -> RPG de Ação
INSERT INTO jogo_genero (jogo_id, genero_id) VALUES (1, 2); -- The Witcher 3 -> Mundo Aberto
INSERT INTO jogo_genero (jogo_id, genero_id) VALUES (2, 1); -- Cyberpunk -> RPG de Ação
INSERT INTO jogo_genero (jogo_id, genero_id) VALUES (2, 2); -- Cyberpunk -> Mundo Aberto
INSERT INTO jogo_genero (jogo_id, genero_id) VALUES (3, 3); -- Uncharted 4 -> Ação-Aventura

