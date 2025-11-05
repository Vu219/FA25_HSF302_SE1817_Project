package fa25.group.evtrainticket.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        // Get error status code
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());
            model.addAttribute("status", statusCode);

            // Get error details
            String errorMessage = (String) request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
            Exception exception = (Exception) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
            String path = (String) request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);

            model.addAttribute("message", errorMessage);
            model.addAttribute("path", path);

            // Add specific error information based on status code
            if (statusCode == HttpStatus.FORBIDDEN.value()) {
                model.addAttribute("error", "Forbidden");
            } else if (statusCode == HttpStatus.NOT_FOUND.value()) {
                model.addAttribute("error", "Not Found");
            } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                model.addAttribute("error", "Internal Server Error");
                if (exception != null) {
                    model.addAttribute("message", exception.getMessage());
                }
            }
        }

        return "error";
    }
}

