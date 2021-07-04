package co.com.fuentech.fuentechserver.security.controllers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.Valid;

import co.com.fuentech.fuentechserver.security.config.JwtUtils;
import co.com.fuentech.fuentechserver.security.models.UserLoginModel;
import co.com.fuentech.fuentechserver.security.payload.requests.LoginRequest;
import co.com.fuentech.fuentechserver.security.payload.requests.UserRequest;
import co.com.fuentech.fuentechserver.security.repositories.RoleRepository;
import co.com.fuentech.fuentechserver.security.repositories.UserRepository;
import co.com.fuentech.fuentechserver.security.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        return userService.authenticateUser(loginRequest);
    }

    @PostMapping("/signUp")
    public ResponseEntity<?> createUser(@Valid @RequestBody UserRequest userRequest) {
        return userService.createUser(userRequest);
    }

    @GetMapping("/confirm")
    public ResponseEntity<?> confirm(@RequestParam("token") String token) {
        return userService.confirmToken(token);
    }
}
