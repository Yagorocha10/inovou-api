package br.com.inovou.store.adapter.in.web.seguranca;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;

public class RespostaErroSeguranca implements AuthenticationEntryPoint, AccessDeniedHandler {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException excecao) throws IOException {
        escrever(response, HttpStatus.UNAUTHORIZED, "Faça login para continuar");
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException excecao) throws IOException {
        escrever(response, HttpStatus.FORBIDDEN, "Você não tem permissão para acessar este recurso");
    }

    private static void escrever(HttpServletResponse response, HttpStatus status, String mensagem) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("{\"status\":%d,\"mensagem\":\"%s\",\"campos\":{},\"instante\":\"%s\"}"
                .formatted(status.value(), mensagem, OffsetDateTime.now()));
    }
}
