//package fa25.group.evtrainticket.controller;
//
//import fa25.group.evtrainticket.service.UserService;
//import fa25.group.evtrainticket.dto.UserLoginDto;
//import fa25.group.evtrainticket.dto.UserRegistrationDto;
//import fa25.group.evtrainticket.entity.User;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//
//import jakarta.servlet.http.HttpSession;
//
//@Controller
//public class UserController {
//
//    @Autowired
//    private UserService userService;
//
//    @GetMapping("/profile")
//    public String profilePage(HttpSession session, Model model) {
//        User user = (User) session.getAttribute("user");
//        if (user == null) {
//            return "redirect:/login";
//        }
//        model.addAttribute("user", user);
//        return "profile";
//    }
//
//
//    @GetMapping("/api/user/current")
//    @ResponseBody
//    public ResponseEntity<?> getCurrentUser(HttpSession session) {
//        User user = (User) session.getAttribute("user");
//        if (user == null) {
//            return ResponseEntity.badRequest().body("Not logged in");
//        }
//        return ResponseEntity.ok(user);
//    }
//}
