package br.com.inovou.store.domain.usuario;

import br.com.inovou.store.domain.exception.DomainException;

import java.util.Locale;
import java.util.regex.Pattern;

public class Usuario {

    private static final Pattern FORMATO_EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final int TAMANHO_MAXIMO_NOME = 120;
    private static final int TAMANHO_MAXIMO_EMAIL = 160;

    private final Long id;
    private final String nome;
    private final String email;
    private final String senhaHash;
    private final Role role;

    public Usuario(Long id, String nome, String email, String senhaHash, Role role) {
        this.id = id;
        this.nome = validarNome(nome);
        this.email = normalizarEmail(email);
        if (senhaHash == null || senhaHash.isBlank()) {
            throw new DomainException("A senha do usuário é obrigatória");
        }
        this.senhaHash = senhaHash;
        if (role == null) {
            throw new DomainException("O perfil do usuário é obrigatório");
        }
        this.role = role;
    }

    public static Usuario novoCliente(String nome, String email, String senhaHash) {
        return new Usuario(null, nome, email, senhaHash, Role.CLIENTE);
    }

    public static Usuario novoAdmin(String nome, String email, String senhaHash) {
        return new Usuario(null, nome, email, senhaHash, Role.ADMIN);
    }

    public static String normalizarEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new DomainException("O e-mail é obrigatório");
        }
        String normalizado = email.strip().toLowerCase(Locale.ROOT);
        if (normalizado.length() > TAMANHO_MAXIMO_EMAIL || !FORMATO_EMAIL.matcher(normalizado).matches()) {
            throw new DomainException("E-mail inválido");
        }
        return normalizado;
    }

    public boolean ehAdmin() {
        return role == Role.ADMIN;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public Role getRole() {
        return role;
    }

    private static String validarNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new DomainException("O nome é obrigatório");
        }
        String limpo = nome.strip().replaceAll("\\s+", " ");
        if (limpo.length() < 2 || limpo.length() > TAMANHO_MAXIMO_NOME) {
            throw new DomainException("O nome deve ter entre 2 e %d caracteres".formatted(TAMANHO_MAXIMO_NOME));
        }
        return limpo;
    }
}
