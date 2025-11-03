package fa25.group.evtrainticket.controller;

import fa25.group.evtrainticket.service.UserService;
import fa25.group.evtrainticket.dto.UserLoginDto;
import fa25.group.evtrainticket.dto.UserRegistrationDto;
import fa25.group.evtrainticket.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    // Web pages
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @GetMapping("/profile")
    public String profilePage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        return "profile";
    }

    // API endpoints
    @PostMapping("/api/user/register")
    @ResponseBody
    public ResponseEntity<?> register(@RequestBody UserRegistrationDto registrationDto) {
        try {
            User user = userService.registerUser(registrationDto);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Registration failed: " + e.getMessage());
        }
    }

    @PostMapping("/api/user/login")
    @ResponseBody
    public ResponseEntity<?> login(@RequestBody UserLoginDto loginDto, HttpSession session) {
        try {
            User user = userService.loginUser(loginDto.getEmail(), loginDto.getPassword());
            session.setAttribute("user", user);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Login failed: " + e.getMessage());
        }
    }

    @PostMapping("/api/user/logout")
    @ResponseBody
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok("Logged out successfully");
    }

    @GetMapping("/api/user/current")
    @ResponseBody
    public ResponseEntity<?> getCurrentUser(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.badRequest().body("Not logged in");
        }
        return ResponseEntity.ok(user);
    }
}
