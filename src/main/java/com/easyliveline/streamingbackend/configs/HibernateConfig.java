package com.easyliveline.streamingbackend.configs;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.type.format.jackson.JacksonJsonFormatMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Configuration
public class HibernateConfig {

    @Bean
    public HibernatePropertiesCustomizer jsonFormatMapperCustomizer(ObjectMapper objectMapper) {
        // Create custom serializer and deserializer for LocalDateTime
        SimpleModule module = new SimpleModule();

        module.addSerializer(LocalDateTime.class, new JsonSerializer<>() {
            @Override
            public void serialize(LocalDateTime value, com.fasterxml.jackson.core.JsonGenerator gen, SerializerProvider serializers) throws IOException {
                try {
                    gen.writeString(value.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)); // Serialize in ISO format
                } catch (Exception e) {
                    throw new IOException("Error serializing LocalDateTime: " + value, e);
                }
            }
        });

        module.addDeserializer(LocalDateTime.class, new JsonDeserializer<>() {
            @Override
            public LocalDateTime deserialize(JsonParser p, com.fasterxml.jackson.databind.DeserializationContext ctxt) throws IOException {
                String text = p.getText();
                try {
                    return LocalDateTime.parse(text, DateTimeFormatter.ISO_LOCAL_DATE_TIME); // Deserialize in ISO format
                } catch (DateTimeParseException e) {
                    throw new IOException("Error parsing LocalDateTime: " + text, e); // Handle parsing errors
                } catch (Exception e) {
                    throw new IOException("Unexpected error during LocalDateTime deserialization: " + text, e); // Handle unexpected errors
                }
            }
        });

        // Register the module with the ObjectMapper
        objectMapper.registerModule(module);

        return (properties) -> properties.put(AvailableSettings.JSON_FORMAT_MAPPER,
                new JacksonJsonFormatMapper(objectMapper));
    }
}
