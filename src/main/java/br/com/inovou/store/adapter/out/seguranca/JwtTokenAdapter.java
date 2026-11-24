package br.com.inovou.store.adapter.out.seguranca;

import br.com.inovou.store.application.autenticacao.TokenEmitido;
import br.com.inovou.store.application.autenticacao.UsuarioAutenticado;
import br.com.inovou.store.application.port.out.TokenPort;
import br.com.inovou.store.domain.usuario.Role;
import br.com.inovou.store.domain.usuario.Usuario;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

@Component
public class JwtTokenAdapter implements TokenPort {

    static final String EMISSOR = "inovou-api";
    private static final int TAMANHO_MINIMO_SEGREDO = 32;

    private final byte[] segredo;
    private final Duration expiracao;
    private final Clock relogio;

    @Autowired
    public JwtTokenAdapter(@Value("${inovou.jwt.segredo}") String segredo,
                           @Value("${inovou.jwt.expiracao}") Duration expiracao) {
        this(segredo, expiracao, Clock.systemUTC());
    }

    JwtTokenAdapter(String segredo, Duration expiracao, Clock relogio) {
        byte[] bytes = segredo == null ? new byte[0] : segredo.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < TAMANHO_MINIMO_SEGREDO) {
            throw new IllegalStateException(
                    "inovou.jwt.segredo precisa ter pelo menos %d bytes".formatted(TAMANHO_MINIMO_SEGREDO));
        }
        this.segredo = bytes;
        this.expiracao = expiracao;
        this.relogio = relogio;
    }

    @Override
    public TokenEmitido emitir(Usuario usuario) {
        Instant agora = relogio.instant();
        Instant expiraEm = agora.plus(expiracao);
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .issuer(EMISSOR)
                .subject(String.valueOf(usuario.getId()))
                .issueTime(Date.from(agora))
                .expirationTime(Date.from(expiraEm))
                .claim("nome", usuario.getNome())
                .claim("email", usuario.getEmail())
                .claim("role", usuario.getRole().name())
                .build();
        try {
            SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
            jwt.sign(new MACSigner(segredo));
            return new TokenEmitido(jwt.serialize(), expiraEm);
        } catch (JOSEException excecao) {
            throw new IllegalStateException("Não foi possível gerar o token", excecao);
        }
    }

    @Override
    public Optional<UsuarioAutenticado> validar(String token) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);
            if (!JWSAlgorithm.HS256.equals(jwt.getHeader().getAlgorithm()) || !jwt.verify(new MACVerifier(segredo))) {
                return Optional.empty();
            }
            JWTClaimsSet claims = jwt.getJWTClaimsSet();
            Date expiracaoDoToken = claims.getExpirationTime();
            if (!EMISSOR.equals(claims.getIssuer()) || expiracaoDoToken == null
                    || !expiracaoDoToken.toInstant().isAfter(relogio.instant())) {
                return Optional.empty();
            }
            return Optional.of(new UsuarioAutenticado(
                    Long.valueOf(claims.getSubject()),
                    claims.getStringClaim("nome"),
                    claims.getStringClaim("email"),
                    Role.valueOf(claims.getStringClaim("role"))));
        } catch (ParseException | JOSEException | RuntimeException excecao) {
            return Optional.empty();
        }
    }
}
