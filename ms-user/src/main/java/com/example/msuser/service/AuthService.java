package com.example.msuser.service;

import com.example.msuser.dto.EmailDto;
import com.example.msuser.entity.Role;
import com.example.msuser.entity.RoleName;
import com.example.msuser.entity.User;
import com.example.msuser.repository.RoleRepository;
import com.example.msuser.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CodigoCacheService codigoCacheService;

    @Autowired
    private UserProducer userProducer;

    /**
     * Fluxo do POST /auth/request-code:
     * 1. Busca o usuário pelo e-mail. Se não existir, cria um temporário.
     * 2. Gera código OTP de 6 dígitos e armazena no cache (5 min).
     * 3. Monta o EmailDto e publica na fila RabbitMQ.
     * O código NÃO é retornado na resposta (segurança).
     */
    public void solicitarCodigo(String email) {
        // 1. Buscar ou criar usuário
        User user = userRepository.findByUsername(email)
                .orElseGet(() -> criarUsuarioTemporario(email));

        // 2. Gerar código e armazenar no cache
        String codigo = codigoCacheService.gerarEArmazenar(email);

        // 3. Montar e publicar mensagem na fila
        EmailDto emailDto = EmailDto.builder()
                .userId(UUID.fromString(user.getPublicId()))
                .emailTo(email)
                .subject("Seu código de acesso")
                .text("Olá! Seu código de acesso é: " + codigo
                        + "\n\nEste código expira em 5 minutos. Não compartilhe com ninguém.")
                .build();

        userProducer.publicarEmailDeCodigoAcesso(emailDto);
        log.info("[Auth] Código solicitado para '{}'. Mensagem enviada à fila.", email);
    }

    /**
     * Fluxo do POST /auth/verify-code:
     * Valida o código OTP contra o cache.
     * Retorna true se válido (e remove do cache para uso único).
     */
    public boolean verificarCodigo(String email, String codigo) {
        return codigoCacheService.validar(email, codigo);
    }

    // ─── Helpers ────────────────────────────────────────────────────────────────

    private User criarUsuarioTemporario(String email) {
        log.info("[Auth] Usuário '{}' não encontrado — criando registro temporário.", email);

        Role role = roleRepository.findByName(RoleName.ROLE_CUSTOMER)
                .orElseGet(() -> roleRepository.save(new Role(null, RoleName.ROLE_CUSTOMER)));

        // Senha aleatória — o usuário nunca usará diretamente (acesso via OTP)
        String senhaAleatoria = passwordEncoder.encode(UUID.randomUUID().toString());

        User novoUsuario = User.builder()
                .username(email)
                .password(senhaAleatoria)
                .roles(List.of(role))
                .build();

        return userRepository.save(novoUsuario);
    }
}
