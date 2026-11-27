package br.com.inovou.store.adapter.in.web.admin;

import br.com.inovou.store.adapter.in.web.comum.PaginaResponse;
import br.com.inovou.store.application.consulta.FiltroPedidosAdmin;
import br.com.inovou.store.application.consulta.Paginacao;
import br.com.inovou.store.application.port.in.GerenciarPedidosUseCase;
import br.com.inovou.store.domain.pedido.StatusPedido;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/pedidos")
public class AdminPedidoController {

    private static final int TAMANHO_PADRAO = 20;

    private final GerenciarPedidosUseCase pedidos;

    public AdminPedidoController(GerenciarPedidosUseCase pedidos) {
        this.pedidos = pedidos;
    }

    @GetMapping
    public PaginaResponse<PedidoAdminResumoResponse> listar(
            @RequestParam(name = "status", required = false) StatusPedido status,
            @RequestParam(name = "busca", required = false) String busca,
            @RequestParam(name = "pagina", required = false) Integer pagina,
            @RequestParam(name = "tamanho", required = false) Integer tamanho) {
        Paginacao paginacao = Paginacao.de(pagina, tamanho == null ? TAMANHO_PADRAO : tamanho);
        return PaginaResponse.de(pedidos.listar(new FiltroPedidosAdmin(status, busca), paginacao),
                PedidoAdminResumoResponse::de);
    }

    @GetMapping("/{id}")
    public PedidoAdminResponse buscar(@PathVariable("id") Long id) {
        return PedidoAdminResponse.de(pedidos.buscar(id));
    }

    @PatchMapping("/{id}/status")
    public PedidoAdminResponse alterarStatus(@PathVariable("id") Long id, @Valid @RequestBody StatusRequest request) {
        return PedidoAdminResponse.de(pedidos.alterarStatus(id, request.status()));
    }
}
