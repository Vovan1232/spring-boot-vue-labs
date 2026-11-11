package ua.chytalnia.security;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.regex.Pattern;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private static final Pattern USERNAME = Pattern.compile("[a-zA-Z0-9_.-]{3,32}");

    private final UserRepository users;
    private final RoleRepository roles;
    private final BCryptPasswordEncoder encoder;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) {
        User user = users.findByUsername(normalize(username))
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return org.springframework.security.core.userdetails.User.withUsername(user.getUsername())
            .password(user.getPassword())
            .authorities(user.getRoles().stream().map(Role::getName).map(SimpleGrantedAuthority::new).toList())
            .build();
    }

    @Transactional
    public void register(String username, String password) {
        String login = normalize(username);
        if (!USERNAME.matcher(login).matches()) {
            throw new IllegalArgumentException("Логін має містити 3–32 латинські літери, цифри або символи _ . -");
        }
        if (password.isBlank() || password.length() < 8 || password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("Пароль має містити щонайменше 8 символів і не більше 72 байтів.");
        }
        if (users.existsByUsername(login)) {
            throw new IllegalArgumentException("Цей логін уже зайнятий.");
        }
        save(login, password, roles.findByName(Role.USER).orElseThrow());
    }

    void save(String username, String password, Role role) {
        users.saveAndFlush(new User(username, encoder.encode(password), role));
    }

    static String normalize(String username) {
        return username.strip().toLowerCase(Locale.ROOT);
    }
}
