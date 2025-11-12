package fa25.group.evtrainticket.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                // 1. CHẶN TẤT CẢ MỌI NGÓC NGÁCH
                .addPathPatterns("/**")

                // 2. DANH SÁCH NGOẠI LỆ (Được phép vào không cần login)
                .excludePathPatterns(
                        "/",                // Root
                        "/home",            // Trang chủ
                        "/login",           // Trang đăng nhập (QUAN TRỌNG - Không chặn trang này sẽ bị lặp vô tận)
                        "/register",        // Trang đăng ký
                        "/forgot-password", // Quên mật khẩu
                        "/search/**",
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/fonts/**",
                        "/webjars/**",

                        "/api/schedules/**"
                );
    }
}