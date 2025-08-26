package com.example.til.service;

import com.example.til.domain.Role;
import com.example.til.domain.User;
import com.example.til.domain.VerificationToken;
import com.example.til.repository.UserRepository;
import com.example.til.repository.VerificationTokenRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final VerificationTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final ImageService imageService;
    private final MailService mailService;

    public UserService(UserRepository userRepository,
                       VerificationTokenRepository tokenRepository,
                       PasswordEncoder passwordEncoder,
                       ImageService imageService,
                       MailService mailService) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.imageService = imageService;
        this.mailService = mailService;
    }

    @Transactional
    public User register(String email, String rawPassword, String nickname, MultipartFile profileImage) throws Exception {
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
        if (userRepository.existsByNicknameIgnoreCase(nickname)) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }
        String imagePath = imageService.saveProfileImage(profileImage);
        User user = User.builder()
            .email(email)
            .passwordHash(passwordEncoder.encode(rawPassword))
            .nickname(nickname)
            .role(Role.USER)
            .emailVerified(false)
            .profileImagePath(imagePath)
            .build();
        user = userRepository.save(user);

        // Invalidate previous tokens and create a new one
        tokenRepository.deleteByUserOrExpired(user, LocalDateTime.now());
        String tokenStr = UUID.randomUUID().toString().replaceAll("-", "");
        VerificationToken token = VerificationToken.builder()
            .token(tokenStr)
            .user(user)
            .expiresAt(LocalDateTime.now().plusHours(24))
            .build();
        tokenRepository.save(token);
        mailService.sendVerificationEmail(user.getEmail(), tokenStr);
        return user;
    }

    @Transactional
    public boolean verifyEmail(String tokenStr) {
        Optional<VerificationToken> opt = tokenRepository.findByToken(tokenStr);
        if (opt.isEmpty()) return false;
        VerificationToken token = opt.get();
        if (token.isExpired() || token.isUsed()) return false;
        User user = token.getUser();
        user.setEmailVerified(true);
        token.setUsedAt(LocalDateTime.now());
        // save via JPA dirty checking
        return true;
    }

    public boolean isEligibleToPostOrComment(User user) {
        if (user == null || !user.isEmailVerified()) return false;
        return user.getCreatedAt() != null && user.getCreatedAt().isBefore(LocalDateTime.now().minusDays(1));
    }
}
