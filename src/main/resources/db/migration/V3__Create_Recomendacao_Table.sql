-- Tabela de junção para cursos recomendados
CREATE TABLE TB_USUARIO_CURSO_RECOMENDADO (
    usuario_id BIGINT NOT NULL,
    curso_id BIGINT NOT NULL,
    PRIMARY KEY (usuario_id, curso_id),
    FOREIGN KEY (usuario_id) REFERENCES TB_RESKILL_USUARIO(id),
    FOREIGN KEY (curso_id) REFERENCES TB_RESKILL_CURSO(id)
);