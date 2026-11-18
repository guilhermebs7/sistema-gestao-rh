package com.empresa.rh.util;

import com.empresa.rh.exception.NegocioException;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

public final class JsfUtil {

    private JsfUtil() {
    }

    public static void info(String mensagem) {
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

    public static void manterMensagens() {
        FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
    }

    /** Lê um parâmetro numérico da URL (ex.: ?id=3). Devolve null se ausente ou inválido. */
    public static Long parametroId(String nome) {
        String valor = FacesContext.getCurrentInstance().getExternalContext()
                .getRequestParameterMap().get(nome);
        if (valor == null || !valor.matches("\\d{1,18}")) {
            return null;
        }
        return Long.valueOf(valor);
    }

    private static void adicionar(FacesMessage.Severity severidade, String mensagem) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severidade, mensagem, null));
    }
}