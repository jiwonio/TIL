package com.example.til.web;

import com.example.til.dto.RegistrationRequest;
import com.example.til.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("registrationRequest", new RegistrationRequest());
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registrationRequest") RegistrationRequest request,
                           BindingResult bindingResult,
                           Model model) {
        if (bindingResult.hasErrors()) {
            return "register";
        }
        try {
            userService.register(request.getEmail(), request.getPassword(), request.getNickname(), request.getProfileImage());
            model.addAttribute("message", "가입이 완료되었습니다. 이메일을 확인해 인증을 완료해주세요.");
            return "login";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }

    @GetMapping("/verify")
    public String verify(@RequestParam("token") String token, Model model) {
        boolean ok = userService.verifyEmail(token);
        model.addAttribute("message", ok ? "이메일 인증이 완료되었습니다. 로그인 해주세요." : "토큰이 유효하지 않거나 만료되었습니다.");
        return "login";
    }
}
