package DACK.config;

import DACK.model.RoleName;
import DACK.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String ROLE_ADMIN_AUTHORITY = "ROLE_" + RoleName.ADMIN.name();

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Admin → /admin/dashboard; khách hàng → URL đã lưu (nếu có) hoặc /home.
     */
    @Bean
    AuthenticationSuccessHandler loginSuccessHandler() {
        SavedRequestAwareAuthenticationSuccessHandler customerHandler = new SavedRequestAwareAuthenticationSuccessHandler();
        customerHandler.setDefaultTargetUrl("/home");
        customerHandler.setAlwaysUseDefaultTargetUrl(true);
        return (request, response, authentication) -> {
            if (isAdmin(authentication)) {
                response.sendRedirect(request.getContextPath() + "/admin/dashboard");
                return;
            }
            customerHandler.onAuthenticationSuccess(request, response, authentication);
        };
    }

    private static boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> ROLE_ADMIN_AUTHORITY.equals(a.getAuthority()));
    }

    @Bean
    UserDetailsService userDetailsService(UserRepository userRepository) {
        return username -> userRepository.findByUsername(username)
                .map(u -> {
                    var authorities = u.getRoles().stream()
                            .map(r -> new SimpleGrantedAuthority("ROLE_" + r.getName().name()))
                            .toList();
                    UserDetails ud = org.springframework.security.core.userdetails.User
                            .withUsername(u.getUsername())
                            .password(u.getPassword())
                            .authorities(authorities)
                            .build();
                    return ud;
                })
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationSuccessHandler loginSuccessHandler) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/home", "/about", "/news", "/books/**", "/contact", "/register", "/login",
                                "/forgot-password", "/reset-password", "/css/**", "/images/**", "/h2-console/**").permitAll()
                        .requestMatchers("/admin/**").hasRole(RoleName.ADMIN.name())
                        .requestMatchers("/cart/**", "/checkout/**", "/orders/**").hasRole(RoleName.CUSTOMER.name())
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler(loginSuccessHandler)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/home")
                        .permitAll()
                )
                .httpBasic(Customizer.withDefaults());

        // H2 console support (dev only)
        http.csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"));
        http.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        return http.build();
    }
}

