package br.com.inovou.store.adapter.in.web.seguranca;

import br.com.inovou.store.application.port.in.ValidarTokenUseCase;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String PREFIXO = "Bearer ";

    private final ValidarTokenUseCase validarToken;

    public JwtAuthFilter(ValidarTokenUseCase validarToken) {
        this.validarToken = validarToken;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String cabecalho = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (cabecalho != null && cabecalho.startsWith(PREFIXO)) {
            validarToken.validar(cabecalho.substring(PREFIXO.length()).strip()).ifPresent(usuario -> {
                var autoridades = List.of(new SimpleGrantedAuthority("ROLE_" + usuario.role().name()));
                var autenticacao = UsernamePasswordAuthenticationToken.authenticated(usuario, null, autoridades);
                SecurityContextHolder.getContext().setAuthentication(autenticacao);
            });
        }
        chain.doFilter(request, response);
    }
}
