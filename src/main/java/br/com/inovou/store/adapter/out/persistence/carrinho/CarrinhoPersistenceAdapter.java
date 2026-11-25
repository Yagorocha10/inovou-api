package br.com.inovou.store.adapter.out.persistence.carrinho;

import br.com.inovou.store.adapter.out.persistence.produto.ProdutoJpaRepository;
import br.com.inovou.store.adapter.out.persistence.produto.ProdutoPersistenceMapper;
import br.com.inovou.store.application.port.out.CarrinhoRepositoryPort;
import br.com.inovou.store.domain.carrinho.Carrinho;
import br.com.inovou.store.domain.carrinho.ItemCarrinho;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Transactional(readOnly = true)
public class CarrinhoPersistenceAdapter implements CarrinhoRepositoryPort {

    private final CarrinhoJpaRepository carrinhoJpaRepository;
    private final ProdutoJpaRepository produtoJpaRepository;

    public CarrinhoPersistenceAdapter(CarrinhoJpaRepository carrinhoJpaRepository,
                                      ProdutoJpaRepository produtoJpaRepository) {
        this.carrinhoJpaRepository = carrinhoJpaRepository;
        this.produtoJpaRepository = produtoJpaRepository;
    }

    @Override
    public Optional<Carrinho> buscarDoUsuario(Long usuarioId) {
        return carrinhoJpaRepository.findByUsuarioId(usuarioId).map(CarrinhoPersistenceAdapter::paraDominio);
    }

    @Override
    @Transactional
    public Carrinho salvar(Carrinho carrinho) {
        CarrinhoJpaEntity entidade = carrinhoJpaRepository.findByUsuarioId(carrinho.getUsuarioId())
                .orElseGet(() -> new CarrinhoJpaEntity(carrinho.getUsuarioId()));
        Map<Long, ItemCarrinho> desejados = carrinho.getItens().stream()
                .collect(Collectors.toMap(ItemCarrinho::produtoId, Function.identity()));

        entidade.getItens().removeIf(item -> !desejados.containsKey(item.getProduto().getId()));

        Set<Long> jaNoCarrinho = entidade.getItens().stream()
                .map(item -> item.getProduto().getId())
                .collect(Collectors.toSet());
        for (ItemCarrinhoJpaEntity item : entidade.getItens()) {
            item.setQuantidade(desejados.get(item.getProduto().getId()).quantidade());
        }
        for (ItemCarrinho item : carrinho.getItens()) {
            if (!jaNoCarrinho.contains(item.produtoId())) {
                entidade.getItens().add(new ItemCarrinhoJpaEntity(entidade,
                        produtoJpaRepository.getReferenceById(item.produtoId()), item.quantidade()));
            }
        }
        entidade.aoSalvar();
        return paraDominio(carrinhoJpaRepository.saveAndFlush(entidade));
    }

    private static Carrinho paraDominio(CarrinhoJpaEntity entidade) {
        return new Carrinho(entidade.getId(), entidade.getUsuarioId(), entidade.getItens().stream()
                .map(item -> new ItemCarrinho(ProdutoPersistenceMapper.paraDominio(item.getProduto()),
                        item.getQuantidade()))
                .toList());
    }
}
