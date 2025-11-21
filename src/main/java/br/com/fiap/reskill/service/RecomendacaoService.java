package br.com.fiap.reskill.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.fiap.reskill.config.RabbitMQConfig;
import br.com.fiap.reskill.model.AreaInteresse;
import br.com.fiap.reskill.model.Curso;
import br.com.fiap.reskill.model.Usuario;
import br.com.fiap.reskill.repository.CursoRepository;
import br.com.fiap.reskill.repository.UsuarioRepository;

@Service
public class RecomendacaoService {

    @Autowired
    private GroqAiService groqAiService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CursoRepository cursoRepository;

    @Transactional
    @RabbitListener(queues = RabbitMQConfig.QUEUE_RECOMENDACOES)
    public void gerarRecomendacoes(Long usuarioId) {
        System.out.println("--- Iniciando processamento de recomendação para usuário: " + usuarioId + " ---");

        Optional<Usuario> userOpt = usuarioRepository.findById(usuarioId);
        if (userOpt.isEmpty()) {
            System.err.println("Erro: Usuário não encontrado no banco.");
            return;
        }

        Usuario usuario = userOpt.get();
        if (usuario.getAreasInteresse().isEmpty()) {
            System.out.println("Aviso: Usuário sem áreas de interesse. Nenhuma recomendação gerada.");
            return;
        }

        String textoPrompt = construirPrompt(usuario.getAreasInteresse());

        try {

            String respostaTexto = groqAiService.solicitarRecomendacao(textoPrompt);

            System.out.println("Resposta recebida: \n" + respostaTexto);

            Set<Curso> cursosSugeridos = parseRespostaIA(respostaTexto);

            Set<Curso> cursosSalvos = new HashSet<>();
            for (Curso curso : cursosSugeridos) {
                cursosSalvos.add(cursoRepository.save(curso));
            }

            if (!cursosSalvos.isEmpty()) {
                usuario.getCursosRecomendados().clear();
                usuario.getCursosRecomendados().addAll(cursosSalvos);
                usuarioRepository.save(usuario);

                System.out.println("Sucesso: " + cursosSalvos.size() + " cursos recomendados salvos para o usuário.");
            } else {
                System.out.println("Aviso: A resposta da IA não continha cursos válidos ou formatados corretamente.");
            }

        } catch (Exception e) {
            System.err.println("Erro crítico durante o processamento: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String construirPrompt(Set<AreaInteresse> areas) {
        String interesses = areas.stream()
                .map(AreaInteresse::getNome)
                .collect(Collectors.joining(", "));

        // MUDANÇA: Não pedimos mais a URL para a IA, pois ela alucina.
        // Pedimos apenas NOME e PLATAFORMA.
        return "Você é um assistente de carreira. " +
                "O usuário gosta de: " + interesses + ". " +
                "Indique 5 cursos online ESPECÍFICOS, FAMOSOS e REAIS. " +
                "Responda APENAS com os dados, neste formato exato por linha: " +
                "NOME DO CURSO | NOME DA PLATAFORMA (Ex: Udemy, Coursera, Alura, YouTube)";
    }

    private Set<Curso> parseRespostaIA(String resposta) {
        Set<Curso> cursos = new HashSet<>();
        String[] linhas = resposta.split("\\n");

        for (String linha : linhas) {
            linha = linha.trim();
            if (linha.isEmpty())
                continue;

            String[] partes = linha.split("\\|");

            // Agora esperamos apenas 2 partes: NOME e PLATAFORMA
            if (partes.length >= 2) {
                Curso curso = new Curso();
                String nomeCurso = partes[0].trim();
                String plataforma = partes[1].trim();

                curso.setNome(nomeCurso);
                curso.setPlataforma(plataforma);

                // MUDANÇA: Sempre geramos o link de busca. É 100% seguro.
                curso.setLink(gerarLinkDeBusca(nomeCurso, plataforma));

                cursos.add(curso);
            }
        }
        return cursos;
    }

    private String gerarLinkDeBusca(String nomeCurso, String plataforma) {
        try {
            // Codifica o nome para URL (espaços viram +, acentos convertidos)
            String termoBusca = URLEncoder.encode(nomeCurso, StandardCharsets.UTF_8);
            String platLower = plataforma.toLowerCase();

            if (platLower.contains("udemy")) {
                return "https://www.udemy.com/courses/search/?q=" + termoBusca;
            } else if (platLower.contains("coursera")) {
                return "https://www.coursera.org/search?query=" + termoBusca;
            } else if (platLower.contains("alura")) {
                return "https://www.alura.com.br/busca?query=" + termoBusca;
            } else if (platLower.contains("youtube")) {
                return "https://www.youtube.com/results?search_query=" + termoBusca;
            } else if (platLower.contains("edx")) {
                return "https://www.edx.org/search?q=" + termoBusca;
            } else if (platLower.contains("pluralsight")) {
                return "https://www.pluralsight.com/search?q=" + termoBusca;
            }

            // Fallback: Google
            return "https://www.google.com/search?q=curso+" + termoBusca + "+" + plataforma;

        } catch (Exception e) {
            return "https://www.google.com/search?q=" + nomeCurso;
        }
    }
}