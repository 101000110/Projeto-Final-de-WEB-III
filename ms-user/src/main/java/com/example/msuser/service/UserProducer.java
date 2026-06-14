package com.example.msuser.service;

import com.example.msuser.dto.EmailDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${broker.queue.email.name}")
    private String emailQueueName;

    /**
     * Publica um EmailDto na fila default.email do RabbitMQ.
     * A mensagem é serializada automaticamente para JSON pelo Jackson2JsonMessageConverter.
     */
    public void publicarEmailDeCodigoAcesso(EmailDto emailDto) {
        log.info("[Producer] Publicando mensagem na fila '{}' para: {}", emailQueueName, emailDto.getEmailTo());
        rabbitTemplate.convertAndSend(emailQueueName, emailDto);
        log.info("[Producer] Mensagem publicada com sucesso.");
    }
}
