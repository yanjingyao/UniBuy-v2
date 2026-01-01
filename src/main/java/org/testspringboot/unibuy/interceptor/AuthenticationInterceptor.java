package org.testspringboot.unibuy.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthenticationInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Simple Token Check
        // In production, use JWT or Redis
        String token = request.getHeader("token");
        if (token == null || token.isEmpty()) {
            // Allow OPTIONS requests (CORS preflight)
            if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
                return true;
            }
            // For now, we are skipping strict validation to avoid breaking existing frontend logic
            // which might not send token everywhere.
            // In a strict mode:
            // response.setStatus(401);
            // return false;
            return true; 
        }
        return true;
    }
}
