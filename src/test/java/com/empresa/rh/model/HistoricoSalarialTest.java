package com.empresa.rh.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import org.junit.Test;

public class HistoricoSalarialTest {

    @Test
    public void antesDeGravarPreencheADataQuandoEstaVazia() {
        HistoricoSalarial h = new HistoricoSalarial();
        long antes = System.currentTimeMillis();

        h.antesDeGravar(); // callback @PrePersist (visível aqui por estar no mesmo pacote)

        assertNotNull(h.getDataAlteracao());
        assertTrue(h.getDataAlteracao().getTime() >= antes);
    }

    @Test
    public void antesDeGravarNaoSobrescreveUmaDataJaDefinida() {
        HistoricoSalarial h = new HistoricoSalarial();
        Date definida = new Date(1_000_000L);
        h.setDataAlteracao(definida);

        h.antesDeGravar();

        assertEquals(definida, h.getDataAlteracao());
    }
}
