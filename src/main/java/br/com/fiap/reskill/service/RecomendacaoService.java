package br.com.fiap.reskill.service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
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
    private ChatModel chatModel;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CursoRepository cursoRepository;

    @Transactional
    @RabbitListener(queues = RabbitMQConfig.QUEUE_RECOMENDACOES)
    public void gerarRecomendacoes(Long usuarioId) {
        System.out.println("--- Iniciando processamento via Groq/IA para usuário: " + usuarioId + " ---");

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

        Message userMessage = new UserMessage(textoPrompt);

        Prompt chatPrompt = new Prompt(userMessage,
                OpenAiChatOptions.builder()
                        .withModel("llama-3.3-70b-versatile")
                        .withTemperature(0.7)
                        .build());

        try {
            ChatResponse response = chatModel.call(chatPrompt);

            String respostaTexto = response.getResult().getOutput().getContent();

            System.out.println("Resposta da Groq recebida: \n" + respostaTexto);

            Set<Curso> cursosSugeridos = parseRespostaIA(respostaTexto);

            Set<Curso> cursosSalvos = new HashSet<>();
            for (Curso curso : cursosSugeridos) {
                cursosSalvos.add(cursoRepository.save(curso));
            }

            if (!cursosSalvos.isEmpty()) {
                usuario.getCursosRecomendados().clear();
                usuario.getCursosRecomendados().addAll(cursosSalvos);
                usuarioRepository.save(usuario);
                System.out.println("Sucesso: " + cursosSalvos.size() + " cursos recomendados salvos.");
            } else {
                System.out.println("Aviso: A IA respondeu, mas não foi possível extrair cursos válidos.");
            }

        } catch (Exception e) {
            System.err.println("Erro crítico ao chamar a IA: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String construirPrompt(Set<AreaInteresse> areas) {
        String interesses = areas.stream()
                .map(AreaInteresse::getNome)
                .collect(Collectors.joining(", "));

        return "Você é um assistente de carreira. " +
                "O usuário tem interesse em: " + interesses + ". " +
                "Liste 5 cursos online gratuitos reais. " +
                "Responda APENAS com os dados, neste formato exato por linha: " +
                "NOME DO CURSO | URL (use https://www.udemy.com ou similar) | PLATAFORMA";
    }

    private Set<Curso> parseRespostaIA(String resposta) {
        Set<Curso> cursos = new HashSet<>();
        String[] linhas = resposta.split("\\n");

        for (String linha : linhas) {
            linha = linha.trim();
            if (linha.isEmpty())
                continue;

            String[] partes = linha.split("\\|");

            if (partes.length >= 3) {
                Curso curso = new Curso();
                curso.setNome(partes[0].trim());
                curso.setLink(partes[1].trim());
                curso.setPlataforma(partes[2].trim());
                cursos.add(curso);
            }
        }
        return cursos;
    }
}