package com.empresa.rh.converter;

import com.empresa.rh.model.Cargo;
import com.empresa.rh.service.CargoService;
import javax.enterprise.context.ApplicationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.inject.Inject;
import javax.inject.Named;

@Named
@ApplicationScoped
public class CargoConverter implements Converter {

    @Inject
    private CargoService cargoService;

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return cargoService.buscarPorId(Long.valueOf(value));
        } catch (NumberFormatException e) {
            throw new ConverterException(
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Cargo inválido.", null));
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value instanceof Cargo) {
            Long id = ((Cargo) value).getId();
            return id == null ? "" : id.toString();
        }
        return "";
    }
}