package br.com.inovou.store.domain.produto;

import br.com.inovou.store.domain.exception.DomainException;
import br.com.inovou.store.domain.exception.EstoqueInsuficienteException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class Produto {

    private static final int TAMANHO_MAXIMO_NOME = 150;

    private final Long id;
    private String nome;
    private String slug;
    private String descricao;
    private BigDecimal preco;
    private int estoque;
    private boolean ativo;
    private Categoria categoria;
    private final List<ImagemProduto> imagens;

    public Produto(Long id, String nome, String slug, String descricao, BigDecimal preco,
                   int estoque, boolean ativo, Categoria categoria, List<ImagemProduto> imagens) {
        this.id = id;
        this.nome = validarNome(nome);
        this.slug = (slug == null || slug.isBlank()) ? Slug.de(this.nome) : slug;
        this.descricao = descricao == null ? "" : descricao.strip();
        this.preco = validarPreco(preco);
        this.estoque = validarEstoque(estoque);
        this.ativo = ativo;
        this.categoria = validarCategoria(categoria);
        this.imagens = new ArrayList<>(imagens == null ? List.of() : imagens);
        this.imagens.sort(Comparator.comparingInt(ImagemProduto::ordem));
    }

    public static Produto novo(String nome, String descricao, BigDecimal preco, int estoque,
                               Categoria categoria) {
        return new Produto(null, nome, null, descricao, preco, estoque, true, categoria, List.of());
    }

    public boolean temEstoque(int quantidade) {
        return quantidade > 0 && estoque >= quantidade;
    }

    public boolean disponivelParaVenda() {
        return ativo && estoque > 0;
    }

    public void baixarEstoque(int quantidade) {
        validarQuantidade(quantidade);
        if (estoque < quantidade) {
            throw new EstoqueInsuficienteException(nome, estoque, quantidade);
        }
        estoque -= quantidade;
    }

    public void reporEstoque(int quantidade) {
        validarQuantidade(quantidade);
        estoque += quantidade;
    }

    public void alterarPreco(BigDecimal novoPreco) {
        preco = validarPreco(novoPreco);
    }

    public void alterarDados(String novoNome, String novaDescricao, Categoria novaCategoria) {
        nome = validarNome(novoNome);
        descricao = novaDescricao == null ? "" : novaDescricao.strip();
        categoria = validarCategoria(novaCategoria);
    }

    public void ativar() {
        ativo = true;
    }

    public void desativar() {
        ativo = false;
    }

    public void adicionarImagem(String url) {
        imagens.add(new ImagemProduto(null, url, imagens.size()));
    }

    public Optional<ImagemProduto> imagemPrincipal() {
        return imagens.stream().findFirst();
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getSlug() {
        return slug;
    }

    public String getDescricao() {
        return descricao;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public int getEstoque() {
        return estoque;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public List<ImagemProduto> getImagens() {
        return List.copyOf(imagens);
    }

    private static String validarNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new DomainException("O nome do produto é obrigatório");
        }
        String nomeLimpo = nome.strip();
        if (nomeLimpo.length() > TAMANHO_MAXIMO_NOME) {
            throw new DomainException("O nome do produto pode ter no máximo %d caracteres"
                    .formatted(TAMANHO_MAXIMO_NOME));
        }
        return nomeLimpo;
    }

    private static BigDecimal validarPreco(BigDecimal preco) {
        if (preco == null || preco.signum() <= 0) {
            throw new DomainException("O preço do produto deve ser maior que zero");
        }
        return preco.setScale(2, RoundingMode.HALF_UP);
    }

    private static int validarEstoque(int estoque) {
        if (estoque < 0) {
            throw new DomainException("O estoque do produto não pode ser negativo");
        }
        return estoque;
    }

    private static Categoria validarCategoria(Categoria categoria) {
        if (categoria == null) {
            throw new DomainException("A categoria do produto é obrigatória");
        }
        return categoria;
    }

    private static void validarQuantidade(int quantidade) {
        if (quantidade <= 0) {
            throw new DomainException("A quantidade deve ser maior que zero");
        }
    }
}
