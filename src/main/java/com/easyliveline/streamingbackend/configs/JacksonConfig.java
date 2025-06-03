package com.easyliveline.streamingbackend.configs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        // Create an instance of Hibernate6Module
        Hibernate6Module hibernate6Module = new Hibernate6Module();

        // Configure Hibernate6Module to avoid issues with lazy loading
        hibernate6Module.configure(Hibernate6Module.Feature.FORCE_LAZY_LOADING, false);
        hibernate6Module.configure(Hibernate6Module.Feature.SERIALIZE_IDENTIFIER_FOR_LAZY_NOT_LOADED_OBJECTS, true);

        // Create and configure the ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(hibernate6Module);

        // Disable default inclusion of properties without explicit annotations
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

        return objectMapper;
    }
}
