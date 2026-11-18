package com.empresa.rh.controller;

import javax.annotation.PostConstruct;
import com.empresa.rh.exception.NegocioException;
import com.empresa.rh.model.Departamento;
import com.empresa.rh.service.DepartamentoService;
import com.empresa.rh.util.JsfUtil;
import java.io.Serializable;
import java.util.List;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

@Named
@ViewScoped    //define que o objeto da classe permanece vivo na  memória do servidor enquanto o usuário estiver interagindo com a mesma tela
public class DepartamentoController implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private DepartamentoService departamentoService;

    private Long id;                                   // vem da URL: form.xhtml?id=3
    private Departamento departamento = new Departamento();
    private List<Departamento> departamentos;          // carregada sob demanda

    @PostConstruct
    public void init() {
        id = JsfUtil.parametroId("id");
        if (id != null) {
            Departamento encontrado = departamentoService.buscarPorId(id);
            if (encontrado == null) {
                JsfUtil.erro("Departamento não encontrado.");
                id = null;
            } else {
                departamento = encontrado;
            }
        }
    }

    public String salvar() {
        try {
            departamentoService.salvar(departamento);
            JsfUtil.info("Departamento salvo com sucesso.");
            JsfUtil.manterMensagens();
            return "lista?faces-redirect=true";
        } catch (NegocioException e) {
            JsfUtil.erro(e);
            return null;
        }
    }

    public void excluir(Departamento d) {
        try {
            departamentoService.excluir(d.getId());
            JsfUtil.info("Departamento \"" + d.getNome() + "\" excluído com sucesso.");
        } catch (NegocioException e) {
            JsfUtil.erro(e);
        }
        departamentos = null; // força recarregar a lista
    }

    public List<Departamento> getDepartamentos() {
        if (departamentos == null) {
            departamentos = departamentoService.listarTodos();
        }
        return departamentos;
    }

    public Long getId() { return id; }


    public Departamento getDepartamento() { return departamento; }
    public void setDepartamento(Departamento departamento) { this.departamento = departamento; }
}