package com.empresa.rh.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.empresa.rh.model.enums.SituacaoFuncionario;
import org.junit.Test;

public class CargoEDepartamentoTest {

    @Test
    public void cargosComMesmoIdSaoIguaisEOsSemIdNao() {
        Cargo a = new Cargo();
        a.setId(1L);
        Cargo b = new Cargo();
        b.setId(1L);
        Cargo semId1 = new Cargo();
        Cargo semId2 = new Cargo();

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertTrue(semId1.equals(semId1));
        assertFalse(semId1.equals(semId2));
        assertFalse(a.equals(null));
        assertFalse(a.equals(new Departamento()));
    }

    @Test
    public void departamentosComMesmoIdSaoIguaisEOsSemIdNao() {
        Departamento a = new Departamento();
        a.setId(1L);
        Departamento b = new Departamento();
        b.setId(1L);
        Departamento outro = new Departamento();
        outro.setId(2L);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertFalse(a.equals(outro));
        assertFalse(new Departamento().equals(new Departamento()));
        assertFalse(a.equals(new Cargo()));
    }

    @Test
    public void toStringMostraONomeParaUsoNosSelectsDaTela() {
        Cargo c = new Cargo();
        c.setNome("Analista");
        Departamento d = new Departamento();
        d.setNome("TI");

        assertEquals("Analista", c.toString());
        assertEquals("TI", d.toString());
    }

    @Test
    public void situacaoTemDescricaoAmigavel() {
        assertEquals("Ativo", SituacaoFuncionario.ATIVO.getDescricao());
        assertEquals("Inativo", SituacaoFuncionario.INATIVO.getDescricao());
    }
}
