package com.empresa.rh.controller;

import com.empresa.rh.exception.NegocioException;
import com.empresa.rh.security.SessaoAdmin;
import com.empresa.rh.service.AutenticacaoService;
import com.empresa.rh.util.JsfUtil;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

@Named
@RequestScoped
public class SenhaController {

    @Inject
    private AutenticacaoService autenticacaoService;

    @Inject
    private SessaoAdmin sessaoAdmin;

    private String senhaAtual;
    private String novaSenha;
    private String confirmacao;

    public String alterar() {
        try {
            // O id vem da SESSÃO, nunca do formulário: ninguém troca a senha de outra conta
            autenticacaoService.trocarSenha(sessaoAdmin.getId(), senhaAtual, novaSenha, confirmacao);
            JsfUtil.info("Senha alterada com sucesso.");
            JsfUtil.manterMensagens();
            return "/index?faces-redirect=true";
        } catch (NegocioException e) {
            JsfUtil.erro(e);
            return null;
        }
    }

    public String getSenhaAtual() { return senhaAtual; }
    public void setSenhaAtual(String senhaAtual) { this.senhaAtual = senhaAtual; }

    public String getNovaSenha() { return novaSenha; }
    public void setNovaSenha(String novaSenha) { this.novaSenha = novaSenha; }

    public String getConfirmacao() { return confirmacao; }
    public void setConfirmacao(String confirmacao) { this.confirmacao = confirmacao; }
}