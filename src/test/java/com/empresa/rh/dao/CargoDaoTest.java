package com.empresa.rh.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.empresa.rh.model.Cargo;
import java.math.BigDecimal;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class CargoDaoTest extends DaoTestBase {

    private CargoDao dao;

    @Before
    public void preparar() {
        dao = com(new CargoDao());
    }

    @Test
    public void salvarGuardaSalarioBaseComPrecisao() {
        Cargo c = new Cargo();
        c.setNome("Analista");
        c.setSalarioBase(new BigDecimal("4500.50"));

        Cargo salvo = dao.salvar(c);
        em.flush();
        em.clear();

        Cargo lido = dao.buscarPorId(salvo.getId());
        assertEquals(0, new BigDecimal("4500.50").compareTo(lido.getSalarioBase()));
    }

    @Test
    public void listarTodosOrdenaPorNome() {
        novoCargo("Gerente", "9500.00");
        novoCargo("Analista", "4500.00");

        List<Cargo> lista = dao.listarTodos();

        assertEquals(2, lista.size());
        assertEquals("Analista", lista.get(0).getNome());
        assertEquals("Gerente", lista.get(1).getNome());
    }

    @Test
    public void buscarPorNomeIgnoraMaiusculasEMinusculas() {
        novoCargo("Desenvolvedor Java", "7500.00");

        assertNotNull(dao.buscarPorNome("desenvolvedor JAVA"));
    }

    @Test
    public void buscarPorNomeInexistenteDevolveNull() {
        assertNull(dao.buscarPorNome("Astronauta"));
    }
}
