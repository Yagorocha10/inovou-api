package br.com.inovou.store.adapter.in.web.comum;

import br.com.inovou.store.domain.exception.CredenciaisInvalidasException;
import br.com.inovou.store.domain.exception.DomainException;
import br.com.inovou.store.domain.exception.EmailJaCadastradoException;
import br.com.inovou.store.domain.exception.EstoqueInsuficienteException;
import br.com.inovou.store.domain.exception.TransicaoInvalidaException;
import br.com.inovou.store.domain.exception.RecursoNaoEncontradoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    ResponseEntity<ErroResponse> naoEncontrado(RecursoNaoEncontradoException excecao) {
        return responder(HttpStatus.NOT_FOUND, excecao.getMessage());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    ResponseEntity<ErroResponse> rotaInexistente(NoResourceFoundException excecao) {
        return responder(HttpStatus.NOT_FOUND, "Recurso não encontrado: /" + excecao.getResourcePath());
    }

    @ExceptionHandler(CredenciaisInvalidasException.class)
    ResponseEntity<ErroResponse> credenciaisInvalidas(CredenciaisInvalidasException excecao) {
        return responder(HttpStatus.UNAUTHORIZED, excecao.getMessage());
    }

    @ExceptionHandler(EmailJaCadastradoException.class)
    ResponseEntity<ErroResponse> emailJaCadastrado(EmailJaCadastradoException excecao) {
        return responder(HttpStatus.CONFLICT, excecao.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ErroResponse> corpoInvalido(HttpMessageNotReadableException excecao) {
        return responder(HttpStatus.BAD_REQUEST, "O corpo da requisição está ausente ou mal formatado");
    }

    @ExceptionHandler(EstoqueInsuficienteException.class)
    ResponseEntity<ErroResponse> estoqueInsuficiente(EstoqueInsuficienteException excecao) {
        return responder(HttpStatus.CONFLICT, excecao.getMessage());
    }

    @ExceptionHandler(TransicaoInvalidaException.class)
    ResponseEntity<ErroResponse> transicaoInvalida(TransicaoInvalidaException excecao) {
        return responder(HttpStatus.CONFLICT, excecao.getMessage());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    ResponseEntity<ErroResponse> parametroAusente(MissingServletRequestParameterException excecao) {
        return responder(HttpStatus.BAD_REQUEST, "O parâmetro '%s' é obrigatório".formatted(excecao.getParameterName()));
    }

    @ExceptionHandler(DomainException.class)
    ResponseEntity<ErroResponse> regraDeNegocio(DomainException excecao) {
        return responder(HttpStatus.BAD_REQUEST, excecao.getMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<ErroResponse> parametroInvalido(MethodArgumentTypeMismatchException excecao) {
        return responder(HttpStatus.BAD_REQUEST,
                "Valor inválido para o parâmetro '%s': %s".formatted(excecao.getName(), excecao.getValue()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErroResponse> camposInvalidos(MethodArgumentNotValidException excecao) {
        Map<String, String> campos = new LinkedHashMap<>();
        for (FieldError erro : excecao.getBindingResult().getFieldErrors()) {
            campos.putIfAbsent(erro.getField(), erro.getDefaultMessage());
        }
        ErroResponse corpo = ErroResponse.de(HttpStatus.BAD_REQUEST.value(), "Existem campos inválidos", campos);
        return ResponseEntity.badRequest().body(corpo);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    ResponseEntity<ErroResponse> metodoNaoSuportado(HttpRequestMethodNotSupportedException excecao) {
        return responder(HttpStatus.METHOD_NOT_ALLOWED, "Método " + excecao.getMethod() + " não é suportado nesta rota");
    }

    private ResponseEntity<ErroResponse> responder(HttpStatus status, String mensagem) {
        return ResponseEntity.status(status).body(ErroResponse.de(status.value(), mensagem));
    }
}
