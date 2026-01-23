package com.elementaryschool.graduation_album.config;

import com.elementaryschool.graduation_album.web.admin.AdminAuthController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * /admin/** 페이지 접근 시 세션에 isAdmin 이 없으면 /admin/login 으로 리다이렉트한다.
 * - API(/api/**)는 적용하지 않음
 */
public class AdminLoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        HttpSession session = request.getSession(false);
        Boolean isAdmin = session == null ? null : (Boolean) session.getAttribute(AdminAuthController.SESSION_KEY);

        if (Boolean.TRUE.equals(isAdmin)) {
            return true;
        }

        response.sendRedirect("/admin/login");
        return false;
    }
}

