package br.com.fiap.reskill.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import br.com.fiap.reskill.model.Usuario;
import br.com.fiap.reskill.repository.UsuarioRepository;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Encontra um usuário pelo email (do Google).
     * Se não encontrar, cria um novo usuário no banco de dados.
     */
    public Usuario findOrCreateUsuario(OAuth2User oauth2User) {
        String email = oauth2User.getAttribute("email");
        Optional<Usuario> userOpt = usuarioRepository.findByEmail(email);

        // Se o usuário já existe, retorna-o
        if (userOpt.isPresent()) {
            return userOpt.get();
        }

        // Se não existe, cria um novo
        Usuario novoUsuario = new Usuario();
        novoUsuario.setNome(oauth2User.getAttribute("name"));
        novoUsuario.setEmail(email);
        // A senha fica nula, pois estamos usando OAuth2

        return usuarioRepository.save(novoUsuario);
    }
}