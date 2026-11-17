package com.empresa.rh.controller;

import com.empresa.rh.exception.NegocioException;
import com.empresa.rh.model.Cargo;
import com.empresa.rh.model.Departamento;
import com.empresa.rh.model.Endereco;
import com.empresa.rh.model.Funcionario;
import com.empresa.rh.service.CargoService;
import com.empresa.rh.service.DepartamentoService;
import com.empresa.rh.service.FuncionarioService;
import com.empresa.rh.util.JsfUtil;
import java.io.Serializable;
import java.util.List;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

@Named
@ViewScoped
public class FuncionarioController implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private FuncionarioService funcionarioService;

    // Só para preencher as listas de seleção (departamento e cargo) do formulário
    @Inject
    private DepartamentoService departamentoService;

    @Inject
    private CargoService cargoService;

    private Long id;
    private Funcionario funcionario = novoFuncionario();
    private String termoBusca;

    private List<Funcionario> funcionarios;
    private List<Departamento> departamentos;
    private List<Cargo> cargos;

    private Funcionario novoFuncionario() {
        Funcionario f = new Funcionario();
        f.setEndereco(new Endereco());
        return f;
    }

    // O formulário precisa de um Endereco (mesmo vazio) para ligar os campos
    private void garantirEndereco() {
        if (funcionario.getEndereco() == null) {
            funcionario.setEndereco(new Endereco());
        }
    }

    /** Chamado ao abrir form.xhtml ou detalhe.xhtml com ?id=... */
    public String carregar() {
        if (id != null) {
            Funcionario encontrado = funcionarioService.buscarPorId(id);
            if (encontrado == null) {
                JsfUtil.erro("Funcionário não encontrado.");
                JsfUtil.manterMensagens();
                return "lista?faces-redirect=true";
            }
            funcionario = encontrado;
            garantirEndereco();
        }
        return null;
    }

    public String salvar() {
        try {
            funcionarioService.salvar(funcionario);
            JsfUtil.info("Funcionário salvo com sucesso.");
            JsfUtil.manterMensagens();
            return "lista?faces-redirect=true";
        } catch (NegocioException e) {
            JsfUtil.erro(e);
            // O Service pode ter zerado o endereço em branco; o formulário precisa dele de volta
            garantirEndereco();
            return null;
        }
    }

    public void pesquisar() {
        funcionarios = funcionarioService.pesquisar(termoBusca);
    }

    public void inativar(Funcionario f) {
        try {
            funcionarioService.inativar(f.getId());
            JsfUtil.info("Funcionário \"" + f.getNome() + "\" inativado.");
        } catch (NegocioException e) {
            JsfUtil.erro(e);
        }
        funcionarios = null; // recarrega mantendo o termo de busca
    }

    public void reativar(Funcionario f) {
        try {
            funcionarioService.reativar(f.getId());
            JsfUtil.info("Funcionário \"" + f.getNome() + "\" reativado.");
        } catch (NegocioException e) {
            JsfUtil.erro(e);
        }
        funcionarios = null;
    }

    /** Ao escolher o cargo num cadastro NOVO, sugere o salário-base dele. */
    public void cargoSelecionado() {
        if (funcionario.getId() == null && funcionario.getCargo() != null) {
            funcionario.setSalarioAtual(funcionario.getCargo().getSalarioBase());
        }
    }

    public List<Funcionario> getFuncionarios() {
        if (funcionarios == null) {
            funcionarios = funcionarioService.pesquisar(termoBusca);
        }
        return funcionarios;
    }

    public List<Departamento> getDepartamentos() {
        if (departamentos == null) {
            departamentos = departamentoService.listarTodos();
        }
        return departamentos;
    }

    public List<Cargo> getCargos() {
        if (cargos == null) {
            cargos = cargoService.listarTodos();
        }
        return cargos;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Funcionario getFuncionario() { return funcionario; }
    public void setFuncionario(Funcionario funcionario) { this.funcionario = funcionario; }

    public String getTermoBusca() { return termoBusca; }
    public void setTermoBusca(String termoBusca) { this.termoBusca = termoBusca; }
}