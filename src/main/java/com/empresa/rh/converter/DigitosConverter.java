package com.empresa.rh.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

@FacesConverter("digitosConverter")
public class DigitosConverter implements Converter {

    // Tela -> Java: tira tudo que não é número
    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null) {
            return null;
        }
        String digitos = value.replaceAll("\\D", "");
        return digitos.isEmpty() ? null : digitos;
    }

    // Java -> tela: formata CPF (11 dígitos) ou CEP (8 dígitos)
    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value == null) {
            return "";
        }
        String d = value.toString();
        if (d.matches("\\d{11}")) {
            return d.substring(0, 3) + "." + d.substring(3, 6) + "."
                    + d.substring(6, 9) + "-" + d.substring(9);
        }
        if (d.matches("\\d{8}")) {
            return d.substring(0, 5) + "-" + d.substring(5);
        }
        return d;
    }
}

//faz a conversão de texto para objeto .ex: um "select" HTML envia "3" , mas o funcionario precisa de um objeto Departamento . O converter traduz nos dois sentidos.