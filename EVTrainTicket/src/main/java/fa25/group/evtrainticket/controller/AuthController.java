package fa25.group.evtrainticket.controller;

import fa25.group.evtrainticket.entity.User;
import fa25.group.evtrainticket.service.EmailService;
import fa25.group.evtrainticket.service.OTPService;
import fa25.group.evtrainticket.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final OTPService otpService;
    private final EmailService emailService;

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute("user") @Valid User user, BindingResult bindingResult, @RequestParam("confirmPassword") String confirmPassword,
                           Model model, RedirectAttributes redirectAttributes) {

        if (!user.getPassword().equals(confirmPassword)) {
            model.addAttribute("errorMessage", "Mật khẩu xác nhận không khớp");
        }

        if (bindingResult.hasErrors() || model.containsAttribute("errorMessage")) {
            return "register";
        }

        try {
            user.setRole("USER");
            user.setCreatedAt(LocalDateTime.now());
            userService.addUserAccount(user);
            redirectAttributes.addFlashAttribute("successMessage", "Đăng ký thành công! Vui lòng đăng nhập.");
            return "redirect:/login";
        } catch (Exception e) {
            // Lỗi email đã tồn tại (từ service)
            model.addAttribute("errorMessage", e.getMessage());
            return "register";
        }
    }

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String sendOTP(@RequestParam("email") String email, Model model, HttpSession session) {

        try {
            User user = userService.findByEmail(email);

            session.setAttribute("resetEmail", email);
            session.setAttribute("resetUser", user);

            otpService.sendOTPToEmail(email);
            return "redirect:/verify-otp";

        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "forgot-password";
        }
    }

    @GetMapping("/verify-otp")
    public String showVerifyOTPForm(HttpSession session, Model model) {
        if (session.getAttribute("resetEmail") == null) {
            return "redirect:/forgot-password";
        }
        return "verify-otp";
    }

    @PostMapping("/verify-otp")
    public String verifyOTP(@RequestParam("otp") String otp, HttpSession session, Model model) {

        String email = (String) session.getAttribute("resetEmail");
        if (email == null) {
            return "redirect:/forgot-password";
        }

        try {
            otpService.verifyOTP(email, otp);
            session.setAttribute("otpVerified", true);
            return "redirect:/reset-password";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "verify-otp";
        }
    }

    @GetMapping("/reset-password")
    public String showResetPasswordForm(HttpSession session, Model model) {
        if (session.getAttribute("resetEmail") == null || session.getAttribute("otpVerified") == null) {
            return "redirect:/forgot-password";
        }
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam("newPassword") String newPassword, @RequestParam("confirmPassword") String confirmPassword,
                                HttpSession session, Model model, RedirectAttributes redirectAttributes) {

        String email = (String) session.getAttribute("resetEmail");
        User user = (User) session.getAttribute("resetUser");

        if (email == null || user == null) {
            return "redirect:/forgot-password";
        }

        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("errorMessage", "Mật khẩu xác nhận không khớp");
            return "reset-password";
        }

        try {
            userService.updatePassword(email, newPassword);

            emailService.sendPasswordResetSuccessEmail(user.getEmail(), user.getFullName());

            // Xóa session
            session.removeAttribute("resetEmail");
            session.removeAttribute("resetUser");
            session.removeAttribute("otpVerified");

            // Dùng RedirectAttributes để gửi thông báo thành công
            redirectAttributes.addFlashAttribute("successMessage", "Đặt lại mật khẩu thành công! Vui lòng đăng nhập.");
            return "redirect:/login"; // Redirect về trang login

        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "reset-password";
        }
    }
}