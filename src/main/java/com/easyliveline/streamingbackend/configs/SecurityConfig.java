package com.easyliveline.streamingbackend.configs;

import com.easyliveline.streamingbackend.authFilters.CustomAuthenticationFilter;
import com.easyliveline.streamingbackend.authFilters.JwtAuthorizationFilter;
import com.easyliveline.streamingbackend.authFilters.TenantFilter;
import com.easyliveline.streamingbackend.services.RoleService;
import com.easyliveline.streamingbackend.services.UserAuthService;
import com.easyliveline.streamingbackend.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableMethodSecurity // Enables @PreAuthorize and @PostAuthorize annotations
public class SecurityConfig {

    private final UserAuthService userAuthService;
    private final RoleService roleService;
    private final AuthenticationFailureHandler customAuthenticationFailureHandler;
    private final UserService userService;
    private final TenantFilter tenantFilter;

//    @Value("${app.base-url}")
//    private String FRONTEND_URL;

    @Autowired
    public SecurityConfig(UserAuthService userAuthService,RoleService roleService,AuthenticationFailureHandler customAuthenticationFailureHandler, UserService userService, TenantFilter tenantFilter) {
        this.userAuthService = userAuthService;
        this.roleService = roleService;
        this.customAuthenticationFailureHandler = customAuthenticationFailureHandler;
        this.userService = userService;
        this.tenantFilter = tenantFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
//        config.addAllowedOriginPattern(FRONTEND_URL); // Allow requests from frontend origin
//        config.addAllowedOriginPattern("http://*:3000");
        config.addAllowedOriginPattern("*");
        config.addAllowedOriginPattern("https://websocketking.com");
        config.addAllowedMethod("*"); // Allow all HTTP methods
        config.addAllowedHeader("*"); // Allow all headers
        config.setAllowCredentials(true); // Allow credentials (cookies, authorization headers)

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config); // Apply to all endpoints
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationManager authenticationManager) throws Exception {
        CustomAuthenticationFilter authenticationFilter = new CustomAuthenticationFilter(authenticationManager, userService);
        authenticationFilter.setFilterProcessesUrl("/auth/login"); // Set custom login endpoint
        authenticationFilter.setAuthenticationFailureHandler(this.customAuthenticationFailureHandler);

        http
//                .csrf(csrf -> csrf
//                        .requireCsrfProtectionMatcher(request -> {
//                            String method = request.getMethod();
//                            return "POST".equals(method) || "PUT".equals(method) || "DELETE".equals(method);
//                        })
//                        .ignoringRequestMatchers(
//                                request -> ("/auth/login".equals(request.getRequestURI()) && "POST".equals(request.getMethod()))
////                                        || request.getRequestURI().startsWith("/api/users")
//                                        || request.getRequestURI().startsWith("/api/no-csrf/")
//                        )
//                )
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Apply CORS configuration
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Stateless session
                .authorizeHttpRequests(auth -> auth
                                .requestMatchers(EndpointRequest.to("health", "metrics", "refresh","logfile")).hasRole("ADMIN")
                                .requestMatchers("/actuator/**").hasRole("ADMIN") // Restrict actuator endpoints to ADMIN
                                .requestMatchers("/api/refunds/**").hasAnyRole("OWNER", "ADMIN", "MANAGER") // Restrict /api/refunds/** to OWNER and ADMIN roles
                                .requestMatchers("/api/admins/**").hasRole("ADMIN") // Restrict /api/admin/** to ADMIN role
                                .requestMatchers("/api/owners/**").hasAnyRole("OWNER", "ADMIN")
                                .requestMatchers("/api/users/**").authenticated()
                                .requestMatchers("/api/zoom/**").authenticated()
                                .requestMatchers("/api/**").authenticated()
                                .requestMatchers("/api/users/sse/subscribe/**").permitAll()
                                .requestMatchers("/ws/**").permitAll()
                                .anyRequest().permitAll()
                )
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(LogoutConfigurer::disable) // Disable logout as we use JWT tokens
                .addFilterBefore(tenantFilter, UsernamePasswordAuthenticationFilter.class) // Add TenantFilter first
                .addFilter(authenticationFilter)
                .addFilterBefore(new JwtAuthorizationFilter(roleService), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userAuthService);
        provider.setPasswordEncoder(passwordEncoder());
        provider.setHideUserNotFoundExceptions(false);
        System.out.println("✅ Registered authentication providers: " + provider.getClass().getName());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
