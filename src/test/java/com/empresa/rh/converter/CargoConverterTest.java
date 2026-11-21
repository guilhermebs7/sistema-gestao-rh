package com.empresa.rh.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.empresa.rh.model.Cargo;
import com.empresa.rh.model.Departamento;
import com.empresa.rh.service.CargoService;
import javax.faces.convert.ConverterException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CargoConverterTest {

    @Mock
    private CargoService cargoService;

    @InjectMocks
    private CargoConverter conversor;

    @Test
    public void idValidoBuscaOCargoNoService() {
        Cargo cargo = new Cargo();
        cargo.setId(5L);
        when(cargoService.buscarPorId(5L)).thenReturn(cargo);

        assertSame(cargo, conversor.getAsObject(null, null, "5"));
    }

    @Test
    public void vazioOuNuloViraNuloSemConsultarOBanco() {
        assertNull(conversor.getAsObject(null, null, null));
        assertNull(conversor.getAsObject(null, null, ""));
        assertNull(conversor.getAsObject(null, null, "   "));
        verifyNoMoreInteractions(cargoService);
    }

    @Test
    public void valorNaoNumericoLancaConverterException() {
        try {
            conversor.getAsObject(null, null, "abc");
            fail("Deveria lançar ConverterException");
        } catch (ConverterException e) {
            assertEquals("Cargo inválido.", e.getFacesMessage().getSummary());
        }
        verifyNoMoreInteractions(cargoService);
    }

    @Test
    public void getAsStringDevolveOIdDoCargo() {
        Cargo cargo = new Cargo();
        cargo.setId(7L);

        assertEquals("7", conversor.getAsString(null, null, cargo));
    }

    @Test
    public void getAsStringDevolveVazioParaCargoNovoOuOutroTipo() {
        assertEquals("", conversor.getAsString(null, null, new Cargo())); // sem id
        assertEquals("", conversor.getAsString(null, null, null));
        assertEquals("", conversor.getAsString(null, null, new Departamento()));
        assertEquals("", conversor.getAsString(null, null, "texto"));
    }
}
