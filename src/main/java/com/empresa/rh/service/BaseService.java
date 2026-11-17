package com.empresa.rh.service;

import com.empresa.rh.exception.NegocioException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

/**
 * Funcionalidades comuns a todos os Services.
 */
public abstract class BaseService {

    // O servidor entrega o validador do Bean Validation pronto
    @Inject
    private Validator validator;

    /**
     * Executa as anotações da entidade (@NotNull, @Size, @Pattern...).
     * Se algo falhar, lança NegocioException com todas as mensagens.
     */
    protected void validar(Object entidade) {
        Set<ConstraintViolation<Object>> violacoes = validator.validate(entidade);
        if (!violacoes.isEmpty()) {
            List<String> mensagens = new ArrayList<>();
            for (ConstraintViolation<Object> v : violacoes) {
                mensagens.add(v.getMessage());
            }
            Collections.sort(mensagens);
            throw new NegocioException(mensagens);
        }
    }
}