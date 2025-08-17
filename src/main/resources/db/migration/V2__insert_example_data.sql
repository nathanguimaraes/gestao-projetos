-- Inserir membros
INSERT INTO members (nome, cargo) VALUES
('Maria Silva', 'gerente'),
('Nathan', 'funcionario'),
('John Doe', 'gerente'),
('Ana Pereira', 'funcionario');

-- Inserir projetos
INSERT INTO projects (data_inicio, previsao_termino, orcamento_total, descricao, gerente_id, nome, status) VALUES
('2025-08-01', '2025-10-01', 100000.00, 'Desenvolvimento de software', 1, 'Projeto Software', 'EM_ANALISE'),
('2025-08-16', '2025-09-16', 75000.00, 'Projeto de exemplo gerenciado por John Doe', 3, 'Projeto Exemplo', 'EM_ANALISE'),
('2025-07-15', '2025-12-15', 200000.00, 'Infraestrutura de TI', 1, 'Projeto Infra', 'ANALISE_REALIZADA');

-- Relacionar membros aos projetos
INSERT INTO project_membros (project_id, membros) VALUES
(1, 2),
(1, 4),
(2, 2),
(3, 4);