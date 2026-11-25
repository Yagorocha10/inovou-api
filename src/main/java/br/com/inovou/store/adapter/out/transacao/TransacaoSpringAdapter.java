package br.com.inovou.store.adapter.out.transacao;

import br.com.inovou.store.application.port.out.TransacaoPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.function.Supplier;

@Component
public class TransacaoSpringAdapter implements TransacaoPort {

    private final TransactionTemplate template;

    public TransacaoSpringAdapter(PlatformTransactionManager gerenciador) {
        this.template = new TransactionTemplate(gerenciador);
    }

    @Override
    public <T> T executar(Supplier<T> operacao) {
        return template.execute(status -> operacao.get());
    }
}
