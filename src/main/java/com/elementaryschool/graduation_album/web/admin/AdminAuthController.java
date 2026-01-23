package com.elementaryschool.graduation_album.web.admin;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AdminAuthController {

    public static final String SESSION_KEY = "isAdmin";

    @GetMapping("/admin")
    public String adminHome(HttpSession session) {
        Boolean isAdmin = (Boolean) session.getAttribute(SESSION_KEY);
        if (Boolean.TRUE.equals(isAdmin)) {
            return "admin/index";
        }
        return "redirect:/admin/login";
    }

    @GetMapping("/admin/login")
    public String loginForm() {
        return "admin/login";
    }

    @PostMapping("/admin/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session,
                        Model model) {
        // 간단 구현(노션 요구: 로그인만 있으면 됨)
        // 실제 운영이라면 Spring Security + 암호화된 저장소로 교체하세요.
        if ("admin".equals(username) && "admin".equals(password)) {
            session.setAttribute(SESSION_KEY, true);
            return "redirect:/admin";
        }
        model.addAttribute("error", "아이디 또는 비밀번호가 올바르지 않습니다.");
        return "admin/login";
    }

    @PostMapping("/admin/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}


