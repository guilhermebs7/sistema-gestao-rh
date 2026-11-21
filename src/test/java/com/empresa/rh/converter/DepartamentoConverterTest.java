package com.empresa.rh.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.empresa.rh.model.Cargo;
import com.empresa.rh.model.Departamento;
import com.empresa.rh.service.DepartamentoService;
import javax.faces.convert.ConverterException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DepartamentoConverterTest {

    @Mock
    private DepartamentoService departamentoService;

    @InjectMocks
    private DepartamentoConverter conversor;

    @Test
    public void idValidoBuscaODepartamentoNoService() {
        Departamento d = new Departamento();
        d.setId(3L);
        when(departamentoService.buscarPorId(3L)).thenReturn(d);

        assertSame(d, conversor.getAsObject(null, null, "3"));
    }

    @Test
    public void opcaoSelecioneViraNuloSemConsultarOBanco() {
        assertNull(conversor.getAsObject(null, null, null));
        assertNull(conversor.getAsObject(null, null, ""));
        assertNull(conversor.getAsObject(null, null, "  "));
        verifyNoMoreInteractions(departamentoService);
    }

    @Test
    public void valorNaoNumericoLancaConverterException() {
        try {
            conversor.getAsObject(null, null, "x1");
            fail("Deveria lançar ConverterException");
        } catch (ConverterException e) {
            assertEquals("Departamento inválido.", e.getFacesMessage().getSummary());
        }
    }

    @Test
    public void getAsStringDevolveOIdDoDepartamento() {
        Departamento d = new Departamento();
        d.setId(9L);

        assertEquals("9", conversor.getAsString(null, null, d));
    }

    @Test
    public void getAsStringDevolveVazioParaDepartamentoNovoOuOutroTipo() {
        assertEquals("", conversor.getAsString(null, null, new Departamento()));
        assertEquals("", conversor.getAsString(null, null, null));
        assertEquals("", conversor.getAsString(null, null, new Cargo()));
    }
}
