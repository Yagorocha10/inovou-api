package br.com.inovou.store.domain.usuario;

import br.com.inovou.store.domain.exception.DomainException;

import java.util.Locale;
import java.util.Set;

public record Endereco(
        Long id,
        Long usuarioId,
        String cep,
        String logradouro,
        String numero,
        String complemento,
        String bairro,
        String cidade,
        String uf) {

    private static final Set<String> UFS = Set.of(
            "AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO", "MA", "MT", "MS", "MG", "PA",
            "PB", "PR", "PE", "PI", "RJ", "RN", "RS", "RO", "RR", "SC", "SP", "SE", "TO");

    public Endereco {
        if (usuarioId == null) {
            throw new DomainException("O endereço precisa pertencer a um usuário");
        }
        cep = normalizarCep(cep);
        logradouro = obrigatorio(logradouro, "logradouro", 150);
        numero = obrigatorio(numero, "número", 20);
        complemento = complemento == null || complemento.isBlank() ? null : limitar(complemento.strip(), "complemento", 80);
        bairro = obrigatorio(bairro, "bairro", 80);
        cidade = obrigatorio(cidade, "cidade", 80);
        uf = normalizarUf(uf);
    }

    public Endereco comId(Long novoId) {
        return new Endereco(novoId, usuarioId, cep, logradouro, numero, complemento, bairro, cidade, uf);
    }

    public boolean pertenceA(Long idUsuario) {
        return usuarioId.equals(idUsuario);
    }

    private static String normalizarCep(String cep) {
        String digitos = cep == null ? "" : cep.replaceAll("\\D", "");
        if (digitos.length() != 8) {
            throw new DomainException("O CEP deve ter 8 dígitos");
        }
        return digitos.substring(0, 5) + "-" + digitos.substring(5);
    }

    private static String normalizarUf(String uf) {
        String sigla = uf == null ? "" : uf.strip().toUpperCase(Locale.ROOT);
        if (!UFS.contains(sigla)) {
            throw new DomainException("UF inválida");
        }
        return sigla;
    }

    private static String obrigatorio(String valor, String campo, int tamanhoMaximo) {
        if (valor == null || valor.isBlank()) {
            throw new DomainException("O campo %s é obrigatório".formatted(campo));
        }
        return limitar(valor.strip(), campo, tamanhoMaximo);
    }

    private static String limitar(String valor, String campo, int tamanhoMaximo) {
        if (valor.length() > tamanhoMaximo) {
            throw new DomainException("O campo %s pode ter no máximo %d caracteres".formatted(campo, tamanhoMaximo));
        }
        return valor;
    }
}
