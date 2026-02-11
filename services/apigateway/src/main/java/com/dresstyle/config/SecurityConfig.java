package com.dresstyle.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {


    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) 
                .csrf(csrf -> csrf.disable()) // Desactivar CSRF para permitir POST
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                        .pathMatchers("/auth/**").permitAll() // Rutas reescritas permitidas
                        .pathMatchers("/actuator/**").permitAll() // Visualizar rutas en el navegador
                        .anyExchange().authenticated()
                );
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Define EXACTAMENTE qué orígenes pueden acceder
        configuration.setAllowedOrigins(List.of("http://localhost:5173","http://localhost")); 
        
        // Define los métodos permitidos
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        
        // Define qué cabeceras se permiten (puedes usar "*" para desarrollo, pero estas son las básicas)
        configuration.setAllowedHeaders(Arrays.asList("*"));
        
        // Permite que el navegador envíe cookies o cabeceras de autenticación
        configuration.setAllowCredentials(true);
        
        // Aplica esta configuración a todas las rutas del servidor
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}
