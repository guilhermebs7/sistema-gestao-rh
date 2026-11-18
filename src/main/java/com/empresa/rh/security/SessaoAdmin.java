package com.empresa.rh.security;

import com.empresa.rh.model.Administrador;
import java.io.Serializable;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;


@Named
@SessionScoped
public class SessaoAdmin implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String login;

    public void entrar(Administrador admin) {
        this.id = admin.getId();
        this.login = admin.getLogin();
    }

    public boolean isLogado() {
        return id != null;
    }

    public Long getId() { return id; }

    public String getLogin() { return login; }
}
//classe cuja a finalidade é controlar a sessão do adminstrador enquanto ele estiver utilizando o sistema de rh
// ela permite que o sistema saiba qual adminstrador está logado e se existe alguem autenticado no momento