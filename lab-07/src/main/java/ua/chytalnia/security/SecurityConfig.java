package ua.chytalnia.security;

import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.web.filter.OncePerRequestFilter;

@Configuration
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, SessionRegistry registry) throws Exception {
        return http
            .authorizeHttpRequests(access -> access
                .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                .requestMatchers("/", "/app.css").permitAll()
                .requestMatchers("/login", "/registration").anonymous()
                .requestMatchers("/user", "/user/**").hasAnyAuthority(Role.USER, Role.ADMIN, Role.MANAGER)
                .requestMatchers("/manager", "/manager/**").hasAuthority(Role.MANAGER)
                .requestMatchers("/admin", "/admin/**").hasAuthority(Role.ADMIN)
                .anyRequest().denyAll()
            )
            .addFilterBefore(new LoginRequestFilter(), UsernamePasswordAuthenticationFilter.class)
            .formLogin(login -> login.loginPage("/login").defaultSuccessUrl("/user"))
            .logout(logout -> logout.logoutSuccessUrl("/"))
            .sessionManagement(sessions -> sessions
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
                .expiredUrl("/login?expired")
                .sessionRegistry(registry)
            )
            .build();
    }

    @Bean
    BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    private static class LoginRequestFilter extends OncePerRequestFilter {
        private final ReentrantLock[] locks = IntStream.range(0, 32)
            .mapToObj(index -> new ReentrantLock())
            .toArray(ReentrantLock[]::new);

        @Override
        protected boolean shouldNotFilter(HttpServletRequest request) {
            return !request.getMethod().equals("POST") || !request.getServletPath().equals("/login");
        }

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            String username = request.getParameter("username");
            String login = username == null ? "" : UserService.normalize(username);
            // Serialize authentication and session registration for the same username.
            ReentrantLock lock = locks[Math.floorMod(login.hashCode(), locks.length)];
            lock.lock();
            try {
                chain.doFilter(request, response);
            } finally {
                lock.unlock();
            }
        }
    }
}
