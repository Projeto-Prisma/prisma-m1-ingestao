package br.ufrpe.prisma.m1.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    // Nomes das constantes
    public static final String EXCHANGE_DENUNCIAS = "denuncias.exchange";
    public static final String QUEUE_DENUNCIAS = "denuncias.queue";
    public static final String ROUTING_KEY_RECEBIDA = "denuncia.recebida";

    // 1. Configura o conversor para JSON 
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // 2. Define o Exchange
    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange(EXCHANGE_DENUNCIAS);
    }

    // 3. Define a Fila
    @Bean
    public Queue queue() {
        return new Queue(QUEUE_DENUNCIAS);
    }

    // 4. Faz o binding (conecta a fila ao exchange com a routing key)
    @Bean
    public Binding binding(Queue queue, DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY_RECEBIDA);
    }
}