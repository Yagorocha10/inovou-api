package br.com.inovou.store.domain.produto;

import br.com.inovou.store.domain.exception.DomainException;
import br.com.inovou.store.domain.exception.EstoqueInsuficienteException;
import br.com.inovou.store.domain.exception.RecursoNaoEncontradoException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Produto {

    public static final int TAMANHO_MAXIMO_NOME = 150;
    public static final int TAMANHO_MAXIMO_DESCRICAO = 2000;
    public static final int LIMITE_IMAGENS = 8;

    private final Long id;
    private String nome;
    private String slug;
    private String descricao;
    private BigDecimal preco;
    private BigDecimal precoAnterior;
    private int estoque;
    private boolean ativo;
    private Categoria categoria;
    private final List<ImagemProduto> imagens;

    public Produto(Long id, String nome, String slug, String descricao, BigDecimal preco,
                   int estoque, boolean ativo, Categoria categoria, List<ImagemProduto> imagens) {
        this(id, nome, slug, descricao, preco, null, estoque, ativo, categoria, imagens);
    }

    public Produto(Long id, String nome, String slug, String descricao, BigDecimal preco, BigDecimal precoAnterior,
                   int estoque, boolean ativo, Categoria categoria, List<ImagemProduto> imagens) {
        this.id = id;
        this.nome = validarNome(nome);
        this.slug = (slug == null || slug.isBlank()) ? Slug.de(this.nome) : slug;
        this.descricao = validarDescricao(descricao);
        this.preco = validarPreco(preco);
        this.precoAnterior = validarPrecoAnterior(precoAnterior, this.preco);
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
        BigDecimal precoValidado = validarPreco(novoPreco);
        validarPrecoAnterior(precoAnterior, precoValidado);
        preco = precoValidado;
    }

    public boolean emPromocao() {
        return precoAnterior != null;
    }

    public int percentualDesconto() {
        if (precoAnterior == null) {
            return 0;
        }
        return precoAnterior.subtract(preco)
                .multiply(BigDecimal.valueOf(100))
                .divide(precoAnterior, 0, RoundingMode.DOWN)
                .intValue();
    }

    public void alterarDados(String novoNome, String novaDescricao, Categoria novaCategoria) {
        nome = validarNome(novoNome);
        descricao = validarDescricao(novaDescricao);
        categoria = validarCategoria(novaCategoria);
    }

    public void atualizar(String novoNome, String novaDescricao, BigDecimal novoPreco, int novoEstoque,
                          Categoria novaCategoria, boolean ativoAgora) {
        atualizar(novoNome, novaDescricao, novoPreco, null, novoEstoque, novaCategoria, ativoAgora);
    }

    public void atualizar(String novoNome, String novaDescricao, BigDecimal novoPreco, BigDecimal novoPrecoAnterior,
                          int novoEstoque, Categoria novaCategoria, boolean ativoAgora) {
        String nomeValidado = validarNome(novoNome);
        String descricaoValidada = validarDescricao(novaDescricao);
        BigDecimal precoValidado = validarPreco(novoPreco);
        BigDecimal precoAnteriorValidado = validarPrecoAnterior(novoPrecoAnterior, precoValidado);
        int estoqueValidado = validarEstoque(novoEstoque);
        Categoria categoriaValidada = validarCategoria(novaCategoria);
        nome = nomeValidado;
        descricao = descricaoValidada;
        preco = precoValidado;
        precoAnterior = precoAnteriorValidado;
        estoque = estoqueValidado;
        categoria = categoriaValidada;
        ativo = ativoAgora;
    }

    public void definirEstoque(int novoEstoque) {
        estoque = validarEstoque(novoEstoque);
    }

    public void ativar() {
        ativo = true;
    }

    public void desativar() {
        ativo = false;
    }

    public void adicionarImagem(String url) {
        if (imagens.size() >= LIMITE_IMAGENS) {
            throw new DomainException("Cada produto pode ter no máximo %d fotos".formatted(LIMITE_IMAGENS));
        }
        imagens.add(new ImagemProduto(null, url, imagens.size()));
    }

    public ImagemProduto removerImagem(Long imagemId) {
        ImagemProduto removida = imagens.stream()
                .filter(imagem -> imagem.id() != null && imagem.id().equals(imagemId))
                .findFirst()
                .orElseThrow(() -> new RecursoNaoEncontradoException("Foto não encontrada neste produto"));
        imagens.remove(removida);
        renumerar(new ArrayList<>(imagens));
        return removida;
    }

    public void reordenarImagens(List<Long> idsNaNovaOrdem) {
        List<Long> atuais = imagens.stream().map(ImagemProduto::id).toList();
        boolean mesmaLista = idsNaNovaOrdem != null
                && idsNaNovaOrdem.size() == atuais.size()
                && new HashSet<>(idsNaNovaOrdem).size() == idsNaNovaOrdem.size()
                && new HashSet<>(idsNaNovaOrdem).equals(new HashSet<>(atuais));
        if (!mesmaLista) {
            throw new DomainException("Envie todas as fotos do produto, cada uma uma única vez, na nova ordem");
        }
        Map<Long, ImagemProduto> porId = imagens.stream()
                .collect(Collectors.toMap(ImagemProduto::id, Function.identity()));
        renumerar(idsNaNovaOrdem.stream().map(porId::get).toList());
    }

    private void renumerar(List<ImagemProduto> emOrdem) {
        imagens.clear();
        for (int posicao = 0; posicao < emOrdem.size(); posicao++) {
            ImagemProduto imagem = emOrdem.get(posicao);
            imagens.add(new ImagemProduto(imagem.id(), imagem.url(), posicao));
        }
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

    public BigDecimal getPrecoAnterior() {
        return precoAnterior;
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

    private static String validarDescricao(String descricao) {
        String descricaoLimpa = descricao == null ? "" : descricao.strip();
        if (descricaoLimpa.length() > TAMANHO_MAXIMO_DESCRICAO) {
            throw new DomainException("A descrição do produto pode ter no máximo %d caracteres"
                    .formatted(TAMANHO_MAXIMO_DESCRICAO));
        }
        return descricaoLimpa;
    }

    private static BigDecimal validarPreco(BigDecimal preco) {
        if (preco == null || preco.signum() <= 0) {
            throw new DomainException("O preço do produto deve ser maior que zero");
        }
        if (preco.stripTrailingZeros().scale() > 2) {
            throw new DomainException("O preço do produto pode ter no máximo 2 casas decimais");
        }
        return preco.setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal validarPrecoAnterior(BigDecimal precoAnterior, BigDecimal precoAtual) {
        if (precoAnterior == null) {
            return null;
        }
        if (precoAnterior.stripTrailingZeros().scale() > 2) {
            throw new DomainException("O preço anterior pode ter no máximo 2 casas decimais");
        }
        BigDecimal normalizado = precoAnterior.setScale(2, RoundingMode.HALF_UP);
        if (normalizado.compareTo(precoAtual) <= 0) {
            throw new DomainException("O preço anterior deve ser maior que o preço atual para aparecer como promoção");
        }
        return normalizado;
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
