package br.com.inovou.store.application.service;

import br.com.inovou.store.application.consulta.FiltroPedidosAdmin;
import br.com.inovou.store.application.consulta.Paginacao;
import br.com.inovou.store.application.consulta.ResumoAdmin;
import br.com.inovou.store.application.port.in.ConsultarResumoAdminUseCase;
import br.com.inovou.store.application.port.in.GerenciarPedidosUseCase;
import br.com.inovou.store.application.port.out.PedidoRepositoryPort;
import br.com.inovou.store.application.port.out.ProdutoRepositoryPort;
import br.com.inovou.store.domain.pedido.StatusPedido;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Set;

public class PainelAdminService implements ConsultarResumoAdminUseCase {

    static final Set<StatusPedido> PARA_PREPARAR = EnumSet.of(StatusPedido.PAGO, StatusPedido.EM_SEPARACAO);
    static final int QUANTIDADE_ULTIMOS = 5;

    private final PedidoRepositoryPort pedidoRepository;
    private final ProdutoRepositoryPort produtoRepository;
    private final GerenciarPedidosUseCase pedidos;
    private final Clock relogio;

    public PainelAdminService(PedidoRepositoryPort pedidoRepository, ProdutoRepositoryPort produtoRepository,
                              GerenciarPedidosUseCase pedidos, Clock relogio) {
        this.pedidoRepository = pedidoRepository;
        this.produtoRepository = produtoRepository;
        this.pedidos = pedidos;
        this.relogio = relogio;
    }

    @Override
    public ResumoAdmin resumo() {
        LocalDate hoje = LocalDate.now(relogio);
        LocalDateTime inicio = hoje.atStartOfDay();
        LocalDateTime fim = hoje.plusDays(1).atStartOfDay();
        return new ResumoAdmin(
                pedidoRepository.contarCriadosEntre(inicio, fim),
                pedidoRepository.somarTotalNaoCanceladoEntre(inicio, fim),
                pedidoRepository.contarPorStatus(PARA_PREPARAR),
                produtoRepository.contarAtivosSemEstoque(),
                pedidos.listar(new FiltroPedidosAdmin(null, null), Paginacao.de(0, QUANTIDADE_ULTIMOS)).itens());
    }
}
