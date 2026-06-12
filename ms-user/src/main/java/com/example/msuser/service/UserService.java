package com.example.msuser.service;

import com.example.msuser.dto.CreateUserDto;
import com.example.msuser.dto.LoginUserDto;
import com.example.msuser.dto.RecoveryJwtTokenDto;
import com.example.msuser.entity.Role;
import com.example.msuser.entity.RoleName;
import com.example.msuser.entity.User;
import com.example.msuser.repository.RoleRepository;
import com.example.msuser.repository.UserRepository;
import com.example.msuser.security.services.JwtTokenService;
import com.example.msuser.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenService jwtTokenService;

    public User createUser(CreateUserDto dto) {
        Role role = roleRepository.findByName(RoleName.ROLE_CUSTOMER)
                .orElseGet(() -> roleRepository.save(new Role(null, RoleName.ROLE_CUSTOMER)));

        User user = User.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .roles(List.of(role))
                .build();

        return userRepository.save(user);
    }

    public RecoveryJwtTokenDto authenticateUser(LoginUserDto dto) {
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword());

        Authentication authentication = authenticationManager.authenticate(authToken);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        return jwtTokenService.generateToken(userDetails);
    }
}
