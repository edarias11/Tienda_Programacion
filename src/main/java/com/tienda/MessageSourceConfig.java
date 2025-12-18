package com.tienda;

//Declara una configuración de Spring.
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;


///Crea un bean MessageSource.
///Carga el archivo messages.properties.

//Permite usar mensajes internacionalizados (i18n) en la app.

//Define que los mensajes se lean en UTF-8.

@Configuration
public class MessageSourceConfig {
  
    @Bean("messageSource")
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }
   
}

