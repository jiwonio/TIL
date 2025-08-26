package com.example.til.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

public class RegistrationRequest {
    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 8, max = 100)
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "비밀번호는 영문과 숫자를 포함해야 합니다.")
    private String password;

    @NotBlank
    @Size(min = 2, max = 50)
    private String nickname;

    private MultipartFile profileImage;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public MultipartFile getProfileImage() { return profileImage; }
    public void setProfileImage(MultipartFile profileImage) { this.profileImage = profileImage; }
}
