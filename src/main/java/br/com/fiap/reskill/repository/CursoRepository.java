package br.com.fiap.reskill.repository;

import br.com.fiap.reskill.model.Curso;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CursoRepository extends JpaRepository<Curso, Long> {

    @Query("SELECT c FROM Usuario u JOIN u.cursosRecomendados c WHERE u.id = :usuarioId")
    Page<Curso> findCursosRecomendadosPorUsuario(@Param("usuarioId") Long usuarioId, Pageable pageable);

}