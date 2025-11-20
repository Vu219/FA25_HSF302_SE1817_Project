package fa25.group.evtrainticket.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// ... các import
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                // 1. CHẶN TẤT CẢ
                .addPathPatterns("/**")

                // 2. DANH SÁCH NGOẠI LỆ (SỬA Ở ĐÂY)
                .excludePathPatterns(
                        "/",
                        "/home",
                        "/login",
                        "/register",
                        "/forgot-password",
                        "/verify-otp",
                        "/reset-password",
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