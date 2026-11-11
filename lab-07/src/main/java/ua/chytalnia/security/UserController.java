package ua.chytalnia.security;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final SessionRegistry registry;

    @GetMapping("/")
    String index() {
        return "index";
    }

    @GetMapping("/login")
    String login() {
        return "login";
    }

    @GetMapping("/registration")
    String registration() {
        return "registration";
    }

    @PostMapping("/registration")
    ModelAndView register(@RequestParam(defaultValue = "") String username,
                          @RequestParam(defaultValue = "") String password) {
        try {
            userService.register(username, password);
            return new ModelAndView("redirect:/login?registered");
        } catch (IllegalArgumentException exception) {
            return registrationError(username, exception.getMessage());
        } catch (DataIntegrityViolationException exception) {
            return registrationError(username, "Цей логін уже зайнятий.");
        }
    }

    @GetMapping("/user")
    String user() {
        return "user-page";
    }

    @GetMapping("/manager")
    String manager() {
        return "manager-page";
    }

    @GetMapping("/admin")
    String admin(Model model) {
        long activeUsers = registry.getAllPrincipals().stream()
            .filter(principal -> !registry.getAllSessions(principal, false).isEmpty())
            .count();
        model.addAttribute("activeUsers", activeUsers);
        return "admin-page";
    }

    private ModelAndView registrationError(String username, String message) {
        return new ModelAndView("registration", HttpStatus.BAD_REQUEST)
            .addObject("username", username)
            .addObject("error", message);
    }
}
