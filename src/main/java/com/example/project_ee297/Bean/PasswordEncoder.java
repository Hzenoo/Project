package com.example.project_ee297.Bean;

import org.springframework.context.annotation.Bean;

public class PasswordEncoder {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new PasswordEncoder();
    }

}
