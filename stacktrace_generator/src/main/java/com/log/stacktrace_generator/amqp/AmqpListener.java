package com.log.stacktrace_generator.amqp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AmqpListener {

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "input.queue", durable = "true"),
            exchange = @Exchange(value = "service.tx", type = "topic"),
            key = "input-key"))
    public void processMessage(String content) {
        log.info("Received content: {}", content);
    }
}
