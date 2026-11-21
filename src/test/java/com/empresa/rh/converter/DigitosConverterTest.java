package com.empresa.rh.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class DigitosConverterTest {

    private final DigitosConverter conversor = new DigitosConverter();

    // ---------------------------------------------------------- tela -> Java

    @Test
    public void removeTudoQueNaoForNumero() {
        assertEquals("52998224725", conversor.getAsObject(null, null, "529.982.247-25"));
        assertEquals("50000000", conversor.getAsObject(null, null, "50.000-000"));
        assertEquals("123", conversor.getAsObject(null, null, " 1a2b3 "));
    }

    @Test
    public void textoSemNenhumDigitoViraNulo() {
        assertNull(conversor.getAsObject(null, null, "abc"));
        assertNull(conversor.getAsObject(null, null, "   "));
        assertNull(conversor.getAsObject(null, null, ""));
        assertNull(conversor.getAsObject(null, null, null));
    }

    // ---------------------------------------------------------- Java -> tela

    @Test
    public void formataCpfComOnzeDigitos() {
        assertEquals("529.982.247-25", conversor.getAsString(null, null, "52998224725"));
    }

    @Test
    public void formataCepComOitoDigitos() {
        assertEquals("50000-000", conversor.getAsString(null, null, "50000000"));
    }

    @Test
    public void outrosTamanhosSaemSemFormatacao() {
        assertEquals("12345", conversor.getAsString(null, null, "12345"));
        assertEquals("abc", conversor.getAsString(null, null, "abc"));
    }

    @Test
    public void nuloViraTextoVazio() {
        assertEquals("", conversor.getAsString(null, null, null));
    }
}
