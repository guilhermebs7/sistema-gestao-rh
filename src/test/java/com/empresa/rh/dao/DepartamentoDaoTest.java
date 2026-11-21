package com.empresa.rh.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.empresa.rh.model.Departamento;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

/** Também cobre os métodos herdados do GenericDao (salvar, buscarPorId, listarTodos, remover). */
public class DepartamentoDaoTest extends DaoTestBase {

    private DepartamentoDao dao;

    @Before
    public void preparar() {
        dao = com(new DepartamentoDao());
    }

    @Test
    public void salvarNovoGeraId() {
        Departamento d = new Departamento();
        d.setNome("Financeiro");

        Departamento salvo = dao.salvar(d);

        assertNotNull("O objeto RETORNADO é o que recebe o id", salvo.getId());
    }

    @Test
    public void salvarExistenteAtualiza() {
        Departamento d = novoDepartamento("Financeiro");
        em.flush();
        em.clear();

        Departamento copia = new Departamento(); // objeto "solto", como o que vem da tela
        copia.setId(d.getId());
        copia.setNome("Financeiro e Contábil");
        dao.salvar(copia);
        em.flush();
        em.clear();

        assertEquals("Financeiro e Contábil", dao.buscarPorId(d.getId()).getNome());
    }

    @Test
    public void buscarPorIdEncontraRegistro() {
        Departamento d = novoDepartamento("Jurídico");
        em.flush();
        em.clear();

        assertEquals("Jurídico", dao.buscarPorId(d.getId()).getNome());
    }

    @Test
    public void buscarPorIdInexistenteDevolveNull() {
        assertNull(dao.buscarPorId(999999L));
    }

    @Test
    public void listarTodosOrdenaPorNome() {
        novoDepartamento("Vendas");
        novoDepartamento("Almoxarifado");
        novoDepartamento("Marketing");

        List<Departamento> lista = dao.listarTodos();

        assertEquals(3, lista.size());
        assertEquals("Almoxarifado", lista.get(0).getNome());
        assertEquals("Marketing", lista.get(1).getNome());
        assertEquals("Vendas", lista.get(2).getNome());
    }

    @Test
    public void removerApagaRegistro() {
        Departamento d = novoDepartamento("Temporário");
        em.flush();

        dao.remover(d.getId());
        em.flush();
        em.clear();

        assertNull(dao.buscarPorId(d.getId()));
    }

    @Test
    public void removerIdInexistenteNaoLancaErro() {
        dao.remover(999999L); // não deve lançar exceção
        assertTrue(true);
    }

    @Test
    public void buscarPorNomeIgnoraMaiusculasEMinusculas() {
        novoDepartamento("Recursos Humanos");

        assertNotNull(dao.buscarPorNome("recursos humanos"));
        assertNotNull(dao.buscarPorNome("RECURSOS HUMANOS"));
    }

    @Test
    public void buscarPorNomeInexistenteDevolveNull() {
        assertNull(dao.buscarPorNome("Não existe"));
    }
}
