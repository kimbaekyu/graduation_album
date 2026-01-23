package com.elementaryschool.graduation_album.web.admin;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public final class AdminGuard {

    private AdminGuard() {}

    public static void requireAdmin(HttpSession session) {
        Boolean isAdmin = (Boolean) session.getAttribute(AdminAuthController.SESSION_KEY);
        if (!Boolean.TRUE.equals(isAdmin)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }
}


