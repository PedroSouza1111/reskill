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

    public Usuario processarLoginOAuth(OAuth2User oauth2User) {
        String email = oauth2User.getAttribute("email");

        return usuarioRepository.findByEmail(email)
                .orElseGet(() -> criarNovoUsuario(oauth2User));
    }

    private Usuario criarNovoUsuario(OAuth2User oauth2User) {
        Usuario novoUsuario = new Usuario();
        novoUsuario.setNome(oauth2User.getAttribute("name"));
        novoUsuario.setEmail(oauth2User.getAttribute("email"));

        return usuarioRepository.save(novoUsuario);
    }
}