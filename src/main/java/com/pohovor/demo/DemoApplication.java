package com.pohovor.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.vavr.jackson.datatype.VavrModule;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import xyz.capybara.clamav.ClamavClient;

import javax.validation.constraints.NotBlank;
import java.net.URL;


@SpringBootApplication
@Configuration
@ComponentScan("com.pohovor.demo")
@Log4j2
@EnableTransactionManagement
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    public static final String ANTIVIRUS_QUEUE = "antivirus_queue";
    public static final String ANTIVIRUS_RESULT_QUEUE = "antivirus_result_queue";
    public static final String PROCESS_FILE_QUEUE = "process_file_queue";


    @Bean
    public ClamavClient clamavClient(@NotBlank @Value("${antivirus.url}") URL antivirus ) {
        return new ClamavClient(antivirus.getHost(), antivirus.getPort());
    }

    @Bean
    public Queue antivirusQueue() {
        return new Queue(ANTIVIRUS_QUEUE);
    }

    @Bean
    public Queue antivirusResultQueue() {
        return new Queue(ANTIVIRUS_RESULT_QUEUE);
    }

    @Bean
    public Queue processQueue() {
        return new Queue(PROCESS_FILE_QUEUE);
    }


    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .registerModules(new JavaTimeModule(), new VavrModule())
                .findAndRegisterModules();
    }

    // MessageConverter creates own mapper (without modules) thus pass our mapper, otherwise it fails on DateTime serialization.
    @Bean
    MessageConverter messageConverter(ObjectMapper objectMapper) {
        Jackson2JsonMessageConverter jackson2JsonMessageConverter = new Jackson2JsonMessageConverter(objectMapper);
        return jackson2JsonMessageConverter;
    }

}
