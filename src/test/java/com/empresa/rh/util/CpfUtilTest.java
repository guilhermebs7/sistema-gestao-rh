package com.empresa.rh.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class CpfUtilTest {

    @Test
    public void aceitaCpfsValidos() {
        assertTrue(CpfUtil.isValido("52998224725"));
        assertTrue(CpfUtil.isValido("11144477735"));
        assertTrue(CpfUtil.isValido("12345678909")); // primeiro dígito verificador é 0
    }

    @Test
    public void rejeitaDigitoVerificadorErrado() {
        assertFalse(CpfUtil.isValido("52998224724")); // último dígito errado
        assertFalse(CpfUtil.isValido("52998224715")); // penúltimo dígito errado
    }

    @Test
    public void rejeitaSequenciasDeDigitosRepetidos() {
        for (char d = '0'; d <= '9'; d++) {
            String cpf = new String(new char[11]).replace('\0', d);
            assertFalse("CPF " + cpf + " deveria ser inválido", CpfUtil.isValido(cpf));
        }
    }

    @Test
    public void rejeitaTamanhoErrado() {
        assertFalse(CpfUtil.isValido("5299822472"));    // 10 dígitos
        assertFalse(CpfUtil.isValido("529982247250"));  // 12 dígitos
    }

    @Test
    public void exigeSomenteDigitos() {
        assertFalse(CpfUtil.isValido("529.982.247-25")); // com máscara: quem chama deve limpar antes
        assertFalse(CpfUtil.isValido("5299822472a"));
        assertFalse(CpfUtil.isValido("           "));
    }

    @Test
    public void rejeitaNuloEVazio() {
        assertFalse(CpfUtil.isValido(null));
        assertFalse(CpfUtil.isValido(""));
    }
}
