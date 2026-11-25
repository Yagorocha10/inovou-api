package br.com.inovou.store.adapter.out.seguranca;

import br.com.inovou.store.application.autenticacao.TokenEmitido;
import br.com.inovou.store.application.autenticacao.UsuarioAutenticado;
import br.com.inovou.store.domain.usuario.Role;
import br.com.inovou.store.domain.usuario.Usuario;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenAdapterTest {

    private static final String SEGREDO = "segredo-de-teste-com-mais-de-32-caracteres";
    private static final Instant AGORA = Instant.parse("2026-11-24T12:00:00Z");

    private final Usuario admin = new Usuario(1L, "Administrador", "admin@inovou.com", "hash", Role.ADMIN);

    private JwtTokenAdapter adapterEm(Instant instante) {
        return new JwtTokenAdapter(SEGREDO, Duration.ofHours(8), Clock.fixed(instante, ZoneOffset.UTC));
    }

    @Test
    void deveEmitirEValidarToken() {
        TokenEmitido token = adapterEm(AGORA).emitir(admin);

        Optional<UsuarioAutenticado> resultado = adapterEm(AGORA.plusSeconds(60)).validar(token.valor());

        assertThat(token.expiraEm()).isEqualTo(AGORA.plus(Duration.ofHours(8)));
        assertThat(resultado).contains(new UsuarioAutenticado(1L, "Administrador", "admin@inovou.com", Role.ADMIN));
    }

    @Test
    void deveRecusarTokenExpirado() {
        TokenEmitido token = adapterEm(AGORA).emitir(admin);

        assertThat(adapterEm(AGORA.plus(Duration.ofHours(9))).validar(token.valor())).isEmpty();
    }

    @Test
    void deveRecusarTokenAdulteradoOuAssinadoComOutroSegredo() {
        String token = adapterEm(AGORA).emitir(admin).valor();
        String adulterado = token.substring(0, token.length() - 3) + "abc";
        JwtTokenAdapter outroSegredo = new JwtTokenAdapter("outro-segredo-tambem-com-mais-de-32-caracteres",
                Duration.ofHours(8), Clock.fixed(AGORA, ZoneOffset.UTC));

        assertThat(adapterEm(AGORA).validar(adulterado)).isEmpty();
        assertThat(outroSegredo.validar(token)).isEmpty();
        assertThat(adapterEm(AGORA).validar("isto-nao-e-um-jwt")).isEmpty();
    }

    @Test
    void deveExigirSegredoComPeloMenos32Bytes() {
        assertThatThrownBy(() -> new JwtTokenAdapter("curto", Duration.ofHours(1), Clock.systemUTC()))
                .isInstanceOf(IllegalStateException.class);
    }
}
