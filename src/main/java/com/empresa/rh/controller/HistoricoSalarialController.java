package com.empresa.rh.controller;

import com.empresa.rh.model.HistoricoSalarial;
import com.empresa.rh.service.SalarioService;
import com.empresa.rh.util.JsfUtil;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

@Named
@ViewScoped
public class HistoricoSalarialController implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private SalarioService salarioService;

    private List<HistoricoSalarial> historico;

    public List<HistoricoSalarial> getHistorico() {
        if (historico == null) {
            Long id = JsfUtil.parametroId("id");
            historico = id == null
                    ? Collections.<HistoricoSalarial>emptyList()
                    : salarioService.listarHistorico(id);
        }
        return historico;
    }
}