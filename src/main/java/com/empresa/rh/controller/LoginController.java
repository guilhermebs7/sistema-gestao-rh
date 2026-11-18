package com.empresa.rh.controller;

import com.empresa.rh.exception.NegocioException;
import com.empresa.rh.model.Administrador;
import com.empresa.rh.security.SessaoAdmin;
import com.empresa.rh.service.AutenticacaoService;
import com.empresa.rh.util.JsfUtil;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

@Named
@RequestScoped
public class LoginController {

    @Inject
    private AutenticacaoService autenticacaoService;

    @Inject
    private SessaoAdmin sessaoAdmin;

    private String login;
    private String senha;

    public String entrar() {
        try {
            Administrador admin = autenticacaoService.autenticar(login, senha);

            // Grava o admin na sessão primeiro...
            sessaoAdmin.entrar(admin);

            // ...e só depois troca o identificador da sessão (evita "session fixation")
            HttpServletRequest request = (HttpServletRequest)
                    FacesContext.getCurrentInstance().getExternalContext().getRequest();
            request.changeSessionId();

            return "/index?faces-redirect=true";
        } catch (NegocioException e) {
            JsfUtil.erro(e);
            senha = null;
            return null;
        }
    }

    public String sair() {
        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        ec.invalidateSession(); // destrói a sessão inteira, inclusive o SessaoAdmin
        return "/login?faces-redirect=true";
    }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
}