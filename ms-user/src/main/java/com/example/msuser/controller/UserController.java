package com.example.msuser.controller;

import com.example.msuser.dto.CreateUserDto;
import com.example.msuser.dto.LoginUserDto;
import com.example.msuser.dto.RecoveryJwtTokenDto;
import com.example.msuser.entity.User;
import com.example.msuser.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody @Valid CreateUserDto dto) {
        User user = userService.createUser(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PostMapping("/login")
    public ResponseEntity<RecoveryJwtTokenDto> login(@RequestBody @Valid LoginUserDto dto) {
        RecoveryJwtTokenDto token = userService.authenticateUser(dto);
        return ResponseEntity.ok(token);
    }

    @GetMapping("/test/customer")
    public ResponseEntity<String> testCustomer() {
        return ResponseEntity.ok("Acesso liberado para CUSTOMER!");
    }

    @GetMapping("/test/administrator")
    public ResponseEntity<String> testAdministrator() {
        return ResponseEntity.ok("Acesso liberado para ADMINISTRATOR!");
    }
}
