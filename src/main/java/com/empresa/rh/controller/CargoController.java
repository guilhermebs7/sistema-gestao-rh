package com.empresa.rh.controller;

import com.empresa.rh.exception.NegocioException;
import com.empresa.rh.model.Cargo;
import com.empresa.rh.service.CargoService;
import com.empresa.rh.util.JsfUtil;
import java.io.Serializable;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

@Named
@ViewScoped
public class CargoController implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private CargoService cargoService;

    private Long id;
    private Cargo cargo = new Cargo();
    private List<Cargo> cargos;

    @PostConstruct
    public void init() {
        id = JsfUtil.parametroId("id");
        if (id != null) {
            Cargo encontrado = cargoService.buscarPorId(id);
            if (encontrado == null) {
                JsfUtil.erro("Cargo não encontrado.");
                id = null;
            } else {
                cargo = encontrado;
            }
        }
    }

    public String salvar() {
        try {
            cargoService.salvar(cargo);
            JsfUtil.info("Cargo salvo com sucesso.");
            JsfUtil.manterMensagens();
            return "lista?faces-redirect=true";
        } catch (NegocioException e) {
            JsfUtil.erro(e);
            return null;
        }
    }

    public void excluir(Cargo c) {
        try {
            cargoService.excluir(c.getId());
            JsfUtil.info("Cargo \"" + c.getNome() + "\" excluído com sucesso.");
        } catch (NegocioException e) {
            JsfUtil.erro(e);
        }
        cargos = null;
    }

    public List<Cargo> getCargos() {
        if (cargos == null) {
            cargos = cargoService.listarTodos();
        }
        return cargos;
    }

    public Long getId() { return id; }


    public Cargo getCargo() { return cargo; }
    public void setCargo(Cargo cargo) { this.cargo = cargo; }
}