package br.com.fiap.reskill.service;

import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class GroqAiService {

    @Autowired
    private ChatModel chatModel;

    @Cacheable(value = "recomendacoesIA", key = "#textoPrompt.hashCode()")
    public String solicitarRecomendacao(String textoPrompt) {
        
        Message userMessage = new UserMessage(textoPrompt);

        Prompt chatPrompt = new Prompt(userMessage,
                OpenAiChatOptions.builder()
                        .withModel("llama-3.3-70b-versatile")
                        .withTemperature(0.7)
                        .build()
        );

        ChatResponse response = chatModel.call(chatPrompt);
        return response.getResult().getOutput().getContent();
    }
}