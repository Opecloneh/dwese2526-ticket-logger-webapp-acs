package org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.config;

import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configura la seguridad de la aplicación, definiendo autenticación y autorización
 * para diferentes roles de usuario, y gestionando la política de sesiones.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)  // Activa la seguridad basada en métodos
public class SecurityConfig {

    @Autowired
    private UserDetailsService customUserDetailService;


    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    /**
     * Configura el filtro de seguridad para las solicitudes HTTP, especificando las
     * rutas permitidas y los roles necesarios para acceder a diferentes endpoints.
     *
     * @param http instancia de {@link HttpSecurity} para configurar la seguridad.
     * @return una instancia de {@link SecurityFilterChain} que contiene la configuración de seguridad.
     * @throws Exception si ocurre un error en la configuración de seguridad.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        logger.info("Entrando en el método securityFilterChain");


        // Configuración de seguridad
        http
                .authorizeHttpRequests(auth -> {
                    logger.debug("Configurando autorización de solicitudes HTTP");
                    auth
                            .requestMatchers("/", "/js/**", "/css/**", "/images/**", "/login", "/register").permitAll()        // Acceso anónimo
                            .requestMatchers("/users**").hasRole("ADMIN")         // Solo ADMIN
                            // REGIONS: ADMIN o MANAGER (para algunas pruebas de permisos)
                            .requestMatchers("/regions**").hasAnyRole("ADMIN", "MANAGER")
                            .requestMatchers("/provinces**").hasRole("MANAGER")   // Solo MANAGER
                            .requestMatchers("/profile**").hasRole("USER")                    // Solo USER
                            .anyRequest().authenticated();           // Cualquier otra solicitud requiere autenticación
                })
                .formLogin(form -> {
                    logger.debug("Configurando formulario de inicio de sesión");
                    form
                            .loginPage("/login")    //Pagina personalizada de login
                            .defaultSuccessUrl("/") //Redirige al inicio despues del login
                            .permitAll();           //Permite acceso a la pagina de login a todos los usuarios
                })
                .sessionManagement(session -> {
                    logger.debug("Configurando política de gestión de sesiones");
                    // Usa sesiones cuando sea necesario
                    session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED);
                });


        logger.info("Saliendo del método securityFilterChain");
        return http.build();
    }


    /**
     * Configura los detalles de usuario en memoria para pruebas y desarrollo, asignando
     * roles específicos a cada usuario.
     *
     * @return una instancia de {@link UserDetailsService} que proporciona autenticación en memoria.
     */
    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        logger.info("Entrando en el método daoAuthenticationProvider");

        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailService);
        provider.setPasswordEncoder(passwordEncoder());

        logger.info("Saliendo del método daoAuthenticationProvider");
        return provider;
    }


    /**
     * Configura el codificador de contraseñas para cifrar las contraseñas de los usuarios
     * utilizando BCrypt.
     *
     * @return una instancia de {@link PasswordEncoder} que utiliza BCrypt para cifrar contraseñas.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        logger.info("Entrando en el método passwordEncoder");
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        logger.info("Saliendo del método passwordEncoder");
        return encoder;
    }
}


