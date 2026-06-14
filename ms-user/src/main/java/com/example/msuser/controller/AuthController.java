package com.example.msuser.controller;

import com.example.msuser.dto.RequestCodeDto;
import com.example.msuser.service.AuthService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * POST /auth/request-code
     * Body: { "email": "usuario@email.com" }
     *
     * Gera código OTP, guarda no cache e publica na fila RabbitMQ.
     * Retorna 200 OK sempre (não revela se o e-mail existe ou não — segurança).
     * O código NÃO é retornado na resposta.
     */
    @PostMapping("/request-code")
    public ResponseEntity<Map<String, String>> requestCode(@RequestBody @Valid RequestCodeDto dto) {
        authService.solicitarCodigo(dto.getEmail());
        return ResponseEntity.ok(Map.of(
                "message", "Código enviado para o e-mail informado. Verifique sua caixa de entrada."
        ));
    }

    /**
     * POST /auth/verify-code
     * Body: { "email": "usuario@email.com", "codigo": "123456" }
     *
     * Valida o código OTP.
     * Retorna 200 OK se válido, 400 Bad Request se inválido/expirado.
     */
    @PostMapping("/verify-code")
    public ResponseEntity<Map<String, String>> verifyCode(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String codigo = body.get("codigo");

        if (email == null || codigo == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Os campos 'email' e 'codigo' são obrigatórios."));
        }

        boolean valido = authService.verificarCodigo(email, codigo);

        if (valido) {
            return ResponseEntity.ok(Map.of("message", "Código validado com sucesso!"));
        } else {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Código inválido ou expirado."));
        }
    }
}
