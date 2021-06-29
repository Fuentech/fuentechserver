package co.com.fuentech.fuentechserver.security.models;

import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class UserLoginModel {
    private String token;
    private String username;
    List<String> roles;
}
