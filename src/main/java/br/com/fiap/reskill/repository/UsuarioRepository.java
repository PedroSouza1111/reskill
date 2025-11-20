package br.com.fiap.reskill.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.fiap.reskill.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Método essencial para o Spring Security encontrar o usuário pelo email
    Optional<Usuario> findByEmail(String email);
}