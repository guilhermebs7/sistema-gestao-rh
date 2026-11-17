package com.empresa.rh.exception;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.ejb.ApplicationException;

/**
 * Lançada pelos Services quando uma regra de negócio é violada.
 * A mensagem é feita para ser mostrada ao usuário.
 */
@ApplicationException(rollback = true)
public class NegocioException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final List<String> mensagens;

    public NegocioException(String mensagem) {
        super(mensagem);
        this.mensagens = Collections.singletonList(mensagem);
    }

    // Usada quando várias validações falham ao mesmo tempo
    public NegocioException(List<String> mensagens) {
        super(String.join(" | ", mensagens));
        this.mensagens = new ArrayList<>(mensagens);
    }

    public List<String> getMensagens() {
        return mensagens;
    }
}