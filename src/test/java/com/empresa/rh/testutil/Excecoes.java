package com.empresa.rh.testutil;

import com.empresa.rh.exception.NegocioException;

/**
 * Ajuda os testes a verificar NegocioException sem depender do assertThrows
 * (que só existe a partir do JUnit 4.13; o projeto usa o 4.12).
 */
public final class Excecoes {

    @FunctionalInterface
    public interface Acao {
        void executar() throws Exception;
    }

    private Excecoes() {
    }

    /** Executa a ação e devolve a NegocioException lançada; falha o teste se nada for lançado. */
    public static NegocioException esperaNegocio(Acao acao) {
        try {
            acao.executar();
        } catch (NegocioException e) {
            return e;
        } catch (Exception e) {
            throw new AssertionError("Esperava NegocioException, mas veio: " + e, e);
        }
        throw new AssertionError("Esperava NegocioException, mas nenhuma exceção foi lançada");
    }
}
