package br.com.fiap.reskill.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Define o nome da fila
    public static final String QUEUE_RECOMENDACOES = "reskill.recomendacoes";

    @Bean
    public Queue recomendacoesQueue() {
        return new Queue(QUEUE_RECOMENDACOES, true);
    }
}
