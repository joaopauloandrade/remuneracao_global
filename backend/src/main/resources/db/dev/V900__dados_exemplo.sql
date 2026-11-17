-- Dados de exemplo apenas para o perfil "dev" (não executado em produção).

INSERT INTO empresa (codigo, sigla, setor, porte, controle, controladora) VALUES
    ('1001', 'EMPRESA-A', 'Financeiro', 'Grande', 'Estatal', TRUE),
    ('1002', 'EMPRESA-B', 'Energia', 'Médio', 'Estatal', FALSE);

INSERT INTO instancia_gestao (codigo, nome, descricao, ativo) VALUES
    ('DIR', 'Diretoria', 'Diretoria executiva', TRUE),
    ('CA', 'Conselho de Administração', 'Conselho de administração', TRUE),
    ('CF', 'Conselho Fiscal', 'Conselho fiscal', TRUE),
    ('CAU', 'Comitê de Auditoria', 'Comitê de auditoria', TRUE),
    ('COM', 'Comitê de Pessoas', 'Demais comitês', TRUE);

INSERT INTO cargo (codigo, nome, ativo) VALUES
    ('PRE', 'Presidente', TRUE),
    ('VPR', 'Vice-Presidente', TRUE),
    ('DIR', 'Diretor', TRUE),
    ('LIQ', 'Liquidante', TRUE),
    ('MEM', 'Membro', TRUE);
