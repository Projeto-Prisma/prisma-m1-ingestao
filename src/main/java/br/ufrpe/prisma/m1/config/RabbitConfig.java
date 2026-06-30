package br.ufrpe.prisma.m1.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE_DENUNCIAS = "denuncias";
    public static final String ROUTING_KEY_RECEBIDA = "denuncia.recebida";

    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    // M1 é produtor puro: declara o topic exchange compartilhado, sem fila própria.
    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(EXCHANGE_DENUNCIAS, true, false);
    }
}