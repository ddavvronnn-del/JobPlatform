package uz.imaan.jobplatform.authModuli.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.imaan.jobplatform.authModuli.dto.AuthResponse;
import uz.imaan.jobplatform.authModuli.dto.LoginRequest;
import uz.imaan.jobplatform.authModuli.dto.RegisterRequest;
import uz.imaan.jobplatform.authModuli.entity.Role;
import uz.imaan.jobplatform.authModuli.entity.User;
import uz.imaan.jobplatform.authModuli.repo.RoleRepository;
import uz.imaan.jobplatform.authModuli.repo.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new RuntimeException("Ushbu telefon raqam allaqachon ro'yxatdan o'tgan!");
        }

        String roleName = "ROLE_" + (request.getRole() != null ? request.getRole().toUpperCase() : "WORKER");
        Role role = roleRepository.findByName(roleName)
                .orElseGet(() -> roleRepository.save(Role.builder().name(roleName).build()));

        User user = User.builder()
                .phone(request.getPhone())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .language(request.getLanguage() != null ? request.getLanguage() : "UZ")
                .active(true)
                .roles(Set.of(role))
                .build();

        userRepository.save(user);

        String token = jwtService.generateToken(user.getPhone(), roleName);

        return AuthResponse.builder()
                .accessToken(token)
                .phone(user.getPhone())
                .role(roleName)
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByPhone(request.getPhone())
                .orElseThrow(() -> new RuntimeException("Foydalanuvchi topilmadi!"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Parol noto'g'ri!");
        }

        String roleName = user.getRoles().stream()
                .findFirst()
                .map(Role::getName)
                .orElse("ROLE_WORKER");

        String token = jwtService.generateToken(user.getPhone(), roleName);

        return AuthResponse.builder()
                .accessToken(token)
                .phone(user.getPhone())
                .role(roleName)
                .build();
    }

}
