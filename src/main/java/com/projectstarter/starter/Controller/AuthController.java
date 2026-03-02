package com.projectstarter.starter.Controller;

import com.projectstarter.starter.Dto.Request.LoginRequest;
import com.projectstarter.starter.Dto.Request.RegisterRequest;
import com.projectstarter.starter.Dto.Response.ApiResponse;
import com.projectstarter.starter.Dto.Response.JwtResponse;
import com.projectstarter.starter.Entity.User;
import com.projectstarter.starter.Repository.UserRepository;
import com.projectstarter.starter.Util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(AuthenticationManager authenticationManager,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Username già in uso"));
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Email già in uso"));
        }

        User user = new User(
                request.getUsername(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword())
        );
        userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse(true, "Registrazione completata"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtUtil.generateToken(userDetails);
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);

        return ResponseEntity.ok(new JwtResponse(token, refreshToken, userDetails.getUsername()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestParam String refreshToken) {
        if (!jwtUtil.validateToken(refreshToken)) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Refresh token non valido"));
        }

        String username = jwtUtil.extractUsername(refreshToken);
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Utente non trovato"));
        }

        String newToken = jwtUtil.generateToken(user);
        return ResponseEntity.ok(new JwtResponse(newToken, refreshToken, username));
    }
}
