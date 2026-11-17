package com.empresa.rh.converter;

import com.empresa.rh.model.Departamento;
import com.empresa.rh.service.DepartamentoService;
import javax.enterprise.context.ApplicationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.inject.Inject;
import javax.inject.Named;
//precisa consultar o banco (para devolver o objeto), então precisa de um Service injetado. No JSF, o @facesConnverter não aceita injeção, por isso fazemos o converter como bean CDI e o usamdos na pagina por expressao :
@Named
@ApplicationScoped
public class DepartamentoConverter implements Converter {

    @Inject
    private DepartamentoService departamentoService;

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.trim().isEmpty()) {
            return null; // "Selecione..." escolhido
        }
        try {
            return departamentoService.buscarPorId(Long.valueOf(value));
        } catch (NumberFormatException e) {
            throw new ConverterException(
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Departamento inválido.", null));
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value instanceof Departamento) {
            Long id = ((Departamento) value).getId();
            return id == null ? "" : id.toString();
        }
        return "";
    }
}