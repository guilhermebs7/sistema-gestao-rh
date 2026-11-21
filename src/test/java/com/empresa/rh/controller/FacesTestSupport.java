package com.empresa.rh.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

/**
 * Apoio aos testes de Controller.
 *
 * Os Controllers chamam JsfUtil, que usa FacesContext.getCurrentInstance(). Fora do servidor
 * esse valor é null, então instalamos um FacesContext FALSO (mock).
 *
 * Esta classe estende FacesContext só para conseguir chamar o método protegido
 * setCurrentInstance(). Ela nunca é instanciada.
 */
abstract class FacesTestSupport extends FacesContext {

    /** Instala o contexto falso. "parametros" simula os parâmetros da URL (?id=3). */
    static FacesContext instalar(Map<String, String> parametros) {
        FacesContext ctx = Mockito.mock(FacesContext.class, Mockito.RETURNS_DEEP_STUBS);
        Mockito.when(ctx.getExternalContext().getRequestParameterMap()).thenReturn(parametros);
        setCurrentInstance(ctx);
        return ctx;
    }

    static void remover() {
        setCurrentInstance(null);
    }

    /** Todas as mensagens que o Controller colocou na tela. */
    static List<FacesMessage> mensagens(FacesContext ctx) {
        ArgumentCaptor<FacesMessage> captor = ArgumentCaptor.forClass(FacesMessage.class);
        Mockito.verify(ctx, Mockito.atLeast(0)).addMessage(Mockito.isNull(String.class), captor.capture());
        return captor.getAllValues();
    }

    /** Só os textos de uma severidade (INFO ou ERROR). */
    static List<String> textos(FacesContext ctx, FacesMessage.Severity severidade) {
        List<String> textos = new ArrayList<>();
        for (FacesMessage m : mensagens(ctx)) {
            if (m.getSeverity() == severidade) {
                textos.add(m.getSummary());
            }
        }
        return textos;
    }
}
