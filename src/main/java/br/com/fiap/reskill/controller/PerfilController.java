package br.com.fiap.reskill.controller;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.fiap.reskill.config.RabbitMQConfig;
import br.com.fiap.reskill.model.AreaInteresse;
import br.com.fiap.reskill.model.Curso;
import br.com.fiap.reskill.model.Usuario;
import br.com.fiap.reskill.repository.AreaInteresseRepository;
import br.com.fiap.reskill.repository.CursoRepository;
import br.com.fiap.reskill.repository.UsuarioRepository;
import br.com.fiap.reskill.service.UsuarioService;

@Controller
public class PerfilController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AreaInteresseRepository areaInteresseRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private CursoRepository cursoRepository;

    @Autowired
    private MessageSource messageSource;

    @GetMapping("/meu-perfil")
    public String meuPerfil(Model model,
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page) {

        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        Usuario usuario = usuarioService.processarLoginOAuth(oauth2User);

        Pageable pageable = PageRequest.of(page, 5);

        Page<Curso> cursosPage = cursoRepository.findCursosRecomendadosPorUsuario(usuario.getId(), pageable);

        List<AreaInteresse> todasAreas = areaInteresseRepository.findAll();

        String idsCursos = usuario.getCursosRecomendados().stream()
                .map(Curso::getId)
                .sorted()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        model.addAttribute("idsCursos", idsCursos);

        model.addAttribute("usuario", usuario);
        model.addAttribute("todasAreas", todasAreas);
        model.addAttribute("cursosPage", cursosPage);

        model.addAttribute("nomeUsuario", usuario.getNome());
        model.addAttribute("emailUsuario", usuario.getEmail());
        model.addAttribute("fotoUsuario", oauth2User.getAttribute("picture"));

        return "perfil";
    }

    /**
     * Salva as áreas de interesse selecionadas pelo usuário
     */
    @PostMapping("/meu-perfil/interesses")
    public String salvarInteresses(
            @RequestParam(required = false) List<Long> interesseIds,
            Authentication authentication) {

        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        Usuario usuario = usuarioService.processarLoginOAuth(oauth2User);

        List<AreaInteresse> areasSelecionadas = (interesseIds != null)
                ? areaInteresseRepository.findAllById(interesseIds)
                : Collections.emptyList();

        usuario.setAreasInteresse(new HashSet<>(areasSelecionadas));
        usuarioRepository.save(usuario);

        Locale locale = LocaleContextHolder.getLocale();
        String msg = messageSource.getMessage("flash.salvo.interesses", null, locale);

        return "redirect:/meu-perfil";
    }

    /**
     * Envia o pedido de recomendação para a fila (RabbitMQ)
     */
    @PostMapping("/meu-perfil/recomendar")
    public String solicitarRecomendacoes(Authentication authentication, RedirectAttributes redirectAttributes) {

        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        Usuario usuario = usuarioService.processarLoginOAuth(oauth2User);

        Locale locale = LocaleContextHolder.getLocale();

        if (usuario.getAreasInteresse().isEmpty()) {
            String msg = messageSource.getMessage("flash.erro.interesses", null, locale);
            redirectAttributes.addFlashAttribute("error_message", msg);
            return "redirect:/meu-perfil";
        }

        rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE_RECOMENDACOES, usuario.getId());

        String msg = messageSource.getMessage("flash.sucesso.solicitacao", null, locale);
        redirectAttributes.addFlashAttribute("success_message", msg);

        return "redirect:/meu-perfil";
    }

    /**
     * Endpoint API usado pelo JavaScript para verificar se chegaram novos cursos.
     * Retorna a quantidade atual de cursos recomendados.
     */
    @GetMapping("/api/meu-perfil/status-recomendacoes")
    @ResponseBody
    public ResponseEntity<List<Long>> verificarStatusRecomendacoes(Authentication authentication) {
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        Usuario usuario = usuarioService.processarLoginOAuth(oauth2User);

        List<Long> ids = usuario.getCursosRecomendados().stream()
                .map(Curso::getId)
                .sorted()
                .collect(Collectors.toList());

        return ResponseEntity.ok(ids);
    }
}