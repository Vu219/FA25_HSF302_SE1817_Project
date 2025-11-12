package fa25.group.evtrainticket.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();

        // Kiểm tra: Nếu chưa đăng nhập
        if (session.getAttribute("user") == null) {

            // Lưu lại trang họ đang định vào (để login xong trả về đúng chỗ đó)
            String currentUrl = request.getRequestURI();
            String queryString = request.getQueryString(); // Lưu cả tham số ?scheduleId=...
            if (queryString != null) {
                currentUrl += "?" + queryString;
            }
            session.setAttribute("redirectUrl", currentUrl);

            // Đuổi về trang login
            response.sendRedirect("/login?error=auth_required");
            return false; // Chặn không cho đi tiếp
        }

        return true; // Đã đăng nhập -> Cho đi qua
    }
}