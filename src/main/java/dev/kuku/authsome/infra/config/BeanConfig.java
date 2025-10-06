package dev.kuku.authsome.infra.config;

import dev.kuku.vfl.api.annotation.VFLAnnotation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class BeanConfig {
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public VFLAnnotation vflAnnotation() {
        return VFLAnnotation.getInstance();
    }
}
