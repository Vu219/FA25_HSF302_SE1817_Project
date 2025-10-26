package fa25.group.evtrainticket.controller;

import fa25.group.evtrainticket.entity.User;
import fa25.group.evtrainticket.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LoginController {
    @Autowired
    UserService userService;

    @GetMapping("/login")
    public String showLoginForm(HttpSession session, @RequestParam(value = "error", required = false) String error, Model model) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            return "redirect:/home";
        }

        if (error != null) {
            model.addAttribute("errorMessage", "Email hoặc mật khẩu không đúng");
        }
        return "login";
    }

    @PostMapping("/login")
    public String login(HttpSession session, @RequestParam("email") String email, @RequestParam("password") String password, Model model) {
        try {
            User user = userService.getUserAccount(email, password);
            session.setAttribute("user", user);
            return "redirect:/home";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.removeAttribute("user");
        session.invalidate();
        return "redirect:/home";
    }
}
