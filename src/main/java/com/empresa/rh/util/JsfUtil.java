package com.empresa.rh.util;

import com.empresa.rh.exception.NegocioException;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

/**
 * Isola o código do JSF para mostrar mensagens na tela. a classe JsfUtil centraliza e facilita a exibição de mensagens para o user na interface do sistema , utilizando o JSF
 */
public final class JsfUtil {

    private JsfUtil() {
    }

    public static void info(String mensagem) {     //Exibe mensagens informativas
        adicionar(FacesMessage.SEVERITY_INFO, mensagem);
    }

    public static void erro(String mensagem) {
        adicionar(FacesMessage.SEVERITY_ERROR, mensagem);
    }

    public static void erro(NegocioException e) {
        for (String m : e.getMensagens()) {
            erro(m);
        }
    }

    /**
     * Use ANTES de redirecionar para outra página, senão a mensagem se perde.
     */
    public static void manterMensagens() {
        FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
    }

    private static void adicionar(FacesMessage.Severity severidade, String mensagem) {
        // clientId null = mensagem "global" (aparece no topo da página)
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severidade, mensagem, null));
    }
}