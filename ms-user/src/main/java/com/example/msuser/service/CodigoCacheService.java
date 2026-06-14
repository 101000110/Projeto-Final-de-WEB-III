package com.example.msuser.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class CodigoCacheService {

    private static final long EXPIRACAO_MS = 5 * 60 * 1000L; // 5 minutos

    /**
     * Representa uma entrada no cache: o código OTP e o momento de criação.
     */
    private record EntradaCache(String codigo, long criadoEm) {
        boolean expirou() {
            return System.currentTimeMillis() - criadoEm > EXPIRACAO_MS;
        }
    }

    private final Map<String, EntradaCache> cache = new ConcurrentHashMap<>();
    private final Random random = new Random();

    /**
     * Gera um código OTP de 6 dígitos, armazena no cache e retorna o código.
     */
    public String gerarEArmazenar(String email) {
        String codigo = String.format("%06d", random.nextInt(1_000_000));
        cache.put(email.toLowerCase(), new EntradaCache(codigo, System.currentTimeMillis()));
        log.info("[Cache] Código gerado para '{}' — expira em 5 minutos.", email);
        return codigo;
    }

    /**
     * Valida o código informado para o e-mail.
     * Retorna true se válido (e remove do cache).
     * Retorna false se não encontrado ou expirado.
     */
    public boolean validar(String email, String codigo) {
        EntradaCache entrada = cache.get(email.toLowerCase());

        if (entrada == null) {
            log.warn("[Cache] Código não encontrado para '{}'.", email);
            return false;
        }

        if (entrada.expirou()) {
            cache.remove(email.toLowerCase());
            log.warn("[Cache] Código expirado para '{}'.", email);
            return false;
        }

        if (!entrada.codigo().equals(codigo)) {
            log.warn("[Cache] Código incorreto para '{}'.", email);
            return false;
        }

        // Código válido: remove do cache (uso único)
        cache.remove(email.toLowerCase());
        log.info("[Cache] Código validado com sucesso para '{}'.", email);
        return true;
    }

    /**
     * Verifica se existe código ativo (não expirado) para o e-mail.
     */
    public boolean existeCodigoAtivo(String email) {
        EntradaCache entrada = cache.get(email.toLowerCase());
        return entrada != null && !entrada.expirou();
    }

    /**
     * Limpeza automática de entradas expiradas a cada 2 minutos.
     */
    @Scheduled(fixedRate = 120_000)
    public void limparExpirados() {
        int antes = cache.size();
        cache.entrySet().removeIf(e -> e.getValue().expirou());
        int removidos = antes - cache.size();
        if (removidos > 0) {
            log.info("[Cache] Limpeza automática: {} código(s) expirado(s) removido(s).", removidos);
        }
    }

    /**
     * Retorna o tamanho atual do cache (útil para testes/debug).
     */
    public int tamanho() {
        return cache.size();
    }
}
