package br.com.inovou.store.adapter.in.web.pedido;

import br.com.inovou.store.adapter.in.web.comum.PaginaResponse;
import br.com.inovou.store.application.autenticacao.UsuarioAutenticado;
import br.com.inovou.store.application.consulta.Paginacao;
import br.com.inovou.store.application.port.in.CancelarPedidoUseCase;
import br.com.inovou.store.application.port.in.ConsultarPedidosUseCase;
import br.com.inovou.store.application.port.in.CriarPedidoUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private static final int TAMANHO_PADRAO = 10;

    private final CriarPedidoUseCase criarPedido;
    private final ConsultarPedidosUseCase consultarPedidos;
    private final CancelarPedidoUseCase cancelarPedido;

    public PedidoController(CriarPedidoUseCase criarPedido, ConsultarPedidosUseCase consultarPedidos,
                            CancelarPedidoUseCase cancelarPedido) {
        this.criarPedido = criarPedido;
        this.consultarPedidos = consultarPedidos;
        this.cancelarPedido = cancelarPedido;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PedidoResponse criar(@AuthenticationPrincipal UsuarioAutenticado usuario,
                                @Valid @RequestBody NovoPedidoRequest request) {
        return PedidoResponse.de(criarPedido.criar(usuario.id(), request.enderecoId(), request.formaPagamento()));
    }

    @GetMapping
    public PaginaResponse<PedidoResumoResponse> listar(@AuthenticationPrincipal UsuarioAutenticado usuario,
                                                       @RequestParam(name = "pagina", required = false) Integer pagina,
                                                       @RequestParam(name = "tamanho", required = false) Integer tamanho) {
        Paginacao paginacao = Paginacao.de(pagina, tamanho == null ? TAMANHO_PADRAO : tamanho);
        return PaginaResponse.de(consultarPedidos.listar(usuario.id(), paginacao), PedidoResumoResponse::de);
    }

    @GetMapping("/{id}")
    public PedidoResponse buscar(@AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable("id") Long id) {
        return PedidoResponse.de(consultarPedidos.buscar(usuario.id(), id));
    }

    @PostMapping("/{id}/cancelar")
    public PedidoResponse cancelar(@AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable("id") Long id) {
        return PedidoResponse.de(cancelarPedido.cancelar(usuario.id(), id));
    }
}
