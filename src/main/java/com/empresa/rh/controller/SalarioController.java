package com.empresa.rh.controller;

import com.empresa.rh.exception.NegocioException;
import com.empresa.rh.model.Funcionario;
import com.empresa.rh.service.FuncionarioService;
import com.empresa.rh.service.SalarioService;
import com.empresa.rh.util.JsfUtil;
import java.io.Serializable;
import java.math.BigDecimal;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

@Named
@ViewScoped
public class SalarioController implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private SalarioService salarioService;

    @Inject
    private FuncionarioService funcionarioService;

    private Funcionario funcionario;
    private BigDecimal novoSalario;
    private String motivo;

    @PostConstruct
    public void init() {
        Long id = JsfUtil.parametroId("id");
        if (id != null) {
            funcionario = funcionarioService.buscarPorId(id);
        }
        if (funcionario == null) {
            JsfUtil.erro("Funcionário não encontrado.");
        }
    }

    public String salvar() {
        if (funcionario == null) {
            JsfUtil.erro("Funcionário não encontrado.");
            return null;
        }
        try {
            salarioService.atualizar(funcionario.getId(), novoSalario, motivo);
            JsfUtil.info("Salário atualizado com sucesso.");
            JsfUtil.manterMensagens();
            return "detalhe?faces-redirect=true&id=" + funcionario.getId();
        } catch (NegocioException e) {
            JsfUtil.erro(e);
            return null;
        }
    }

    public Funcionario getFuncionario() { return funcionario; }

    public BigDecimal getNovoSalario() { return novoSalario; }
    public void setNovoSalario(BigDecimal novoSalario) { this.novoSalario = novoSalario; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
}