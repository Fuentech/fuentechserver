package co.com.fuentech.fuentechserver.security.services;

import co.com.fuentech.fuentechserver.email.entities.ConfirmationToken;
import co.com.fuentech.fuentechserver.email.interfaces.EmailSender;
import co.com.fuentech.fuentechserver.email.services.ConfirmationTokenService;
import co.com.fuentech.fuentechserver.security.config.JwtUtils;
import co.com.fuentech.fuentechserver.security.entities.Role;
import co.com.fuentech.fuentechserver.security.entities.User;
import co.com.fuentech.fuentechserver.security.payload.requests.UserRequest;
import co.com.fuentech.fuentechserver.security.payload.response.JwtResponse;
import co.com.fuentech.fuentechserver.security.payload.response.MessageResponse;
import co.com.fuentech.fuentechserver.security.repositories.RoleRepository;
import co.com.fuentech.fuentechserver.security.repositories.UserRepository;
import co.com.fuentech.fuentechserver.security.payload.requests.LoginRequest;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import co.com.fuentech.fuentechserver.security.roles.Roles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UserService {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder encoder;
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private ConfirmationTokenService confirmationTokenService;
    @Autowired
    private EmailSender emailSender;

    public ResponseEntity<?> authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());
        JwtResponse jwtResponse = new JwtResponse();
        jwtResponse.setToken(jwt);
        jwtResponse.setId(userDetails.getId());
        jwtResponse.setUsername(userDetails.getUsername());
        jwtResponse.setEmail(userDetails.getEmail());
        jwtResponse.setRoles(roles);
        return ResponseEntity.ok(jwtResponse);
    }

    public ResponseEntity<?> createUser(UserRequest userRequest) {
        MessageResponse messageResponse = new MessageResponse();
        if (userRepository.existsByUsername(userRequest.getUsername())) {
            messageResponse.setMessage("Error: Username is already taken!");
            return ResponseEntity
                    .badRequest()
                    .body(messageResponse);
        } else if (userRepository.existsByEmail(userRequest.getEmail())) {
            messageResponse.setMessage("Error: Email is already in use!");
            return ResponseEntity
                    .badRequest()
                    .body(messageResponse);
        } else {
            // Create new user's account
            User user = new User(userRequest.getUsername(),
                    userRequest.getEmail(),
                    encoder.encode(userRequest.getPassword()));
            Set<String> strRoles = userRequest.getRole();
            Set<Role> roles = new HashSet<>();
            if (strRoles == null) {
                Role userRole = roleRepository.findByName(Roles.ROLE_USER)
                        .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                roles.add(userRole);
            } else {
                strRoles.forEach(role -> {
                    switch (role) {
                        case "admin":
                            Role adminRole = roleRepository.findByName(Roles.ROLE_ADMIN)
                                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                            roles.add(adminRole);
                            break;
                        case "mod":
                            Role modRole = roleRepository.findByName(Roles.ROLE_MODERATOR)
                                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                            roles.add(modRole);
                            break;
                        default:
                            Role userRole = roleRepository.findByName(Roles.ROLE_USER)
                                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                            roles.add(userRole);
                    }
                });
            }
            user.setRoles(roles);
            userRepository.save(user);
            String token = UUID.randomUUID().toString();
            ConfirmationToken confirmationToken = new ConfirmationToken();
            confirmationToken.setToken(token);
            confirmationToken.setCreatedAt(LocalDateTime.now());
            confirmationToken.setExpiresAt(LocalDateTime.now().plusMinutes(15));
            confirmationToken.setUser(user);
            confirmationTokenService.saveConfirmationToken(confirmationToken);
            String link = "http://localhost:1406/auth/confirm?token=" + token;
            emailSender.send(userRequest.getEmail(), buildEmail(userRequest.getUsername(), link));
            return ResponseEntity.ok(user);
        }
    }

    @Transactional
    public ResponseEntity<?> confirmToken(String token) {
        ConfirmationToken confirmationToken = confirmationTokenService
                .getToken(token).orElseThrow(() -> new IllegalStateException("token not found"));
        if (confirmationToken.getConfirmedAt() != null) {
            throw new IllegalStateException("email already confirmed");
        }
        LocalDateTime expiredAt = confirmationToken.getExpiresAt();
        if (expiredAt.isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("token expired");
        }
        confirmationTokenService.setConfirmedAt(token);
        enableUser(confirmationToken.getUser().getEmail());
        return ResponseEntity.ok(confirmationToken);
    }

    public int enableUser(String email) {
        return userRepository.enableAppUser(email);
    }

    private String buildEmail(String name, String link) {
        return "<div style=\"\n" +
                "    display: grid;\n" +
                "    justify-content: center;\n" +
                "    margin: 20px;\n" +
                "    font-family: Roboto, 'Helvetica Neue', sans-serif;\n" +
                "    color: #0000008a;\">\n" +
                "        <div style=\"\n" +
                "        max-width: 780px; \n" +
                "        padding: 20px; \n" +
                "        border-radius: 20px; \n" +
                "        text-align: center;\n" +
                "        box-shadow: 0px 2px 1px -1px rgb(0 0 0 / 20%), \n" +
                "                    0px 1px 1px 0px rgb(0 0 0 / 14%), \n" +
                "                    0px 1px 3px 0px rgb(0 0 0 / 12%);\">\n" +
                "            <div style=\"text-align: center;\"> \n" +
                "                <img src=\"https://i.imgur.com/qgfTh5z.png\" alt=\"logo\" width=\"600px\">\n" +
                "            </div>\n" +
                "            <h2 style=\"text-align: center; font-size: 24px;\">Hola, " + name + "</h2>\n" +
                "            <h1 style=\"\n" +
                "            text-align: center; \n" +
                "            color: #0000008a;\n" +
                "            font-size: 36px;\n" +
                "            font-weight: bold;\n" +
                "            line-height: normal;\">Bienvenido a <strong style=\"color: #3f71ff;\">FUEN</strong><strong style=\"color: #3f71ff;\">TECH</strong></h1>\n" +
                "            <h2 style=\"text-align: center; font-size: 24px;\">¡Te damos la bienvenida!</h2>\n" +
                "            <div style=\"padding: 20px 0px; font-size: 18px; text-align: justify;\">\n" +
                "                <div>\n" +
                "                     Gracias por registrarte. Por favor haz clic en el botón de abajo para activar tu cuenta y acceder a todos los privilegios de nuestra plataforma. " +
                "                </div>\n" +
                "            </div>\n" +
                "            <div style=\"padding-top: 40px; text-align: center;\">\n" +
                "                <a href=\"" + link + "\" target=\"_blank\" style=\"\n" +
                "                color: #3f71ff;\n" +
                "                font-weight: bold;\n" +
                "                font-size: 18px;\n" +
                "                text-decoration: none; \n" +
                "                border-radius: 5px; \n" +
                "                border: 1px solid rgba(0, 0, 0, 0.12);\n" +
                "                padding: 10px 20px;\">Activar Cuenta</a>\n" +
                "            </div>\n" +
                "            <div>" +
                "               <strong style=\"color:#000000;font-size:18px\"> " +
                "                       El enlace de confirmación caducará en 15 minutos.\n" +
                "               </strong>" +
                "            </div>\n" +
                "        </div>\n" +
                "    </div>";
    }
}
