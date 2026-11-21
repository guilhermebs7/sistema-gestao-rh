package com.empresa.rh.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.empresa.rh.model.Administrador;
import org.junit.Before;
import org.junit.Test;

public class AdministradorDaoTest extends DaoTestBase {

    private AdministradorDao dao;

    @Before
    public void preparar() {
        dao = com(new AdministradorDao());
        Administrador a = new Administrador();
        a.setLogin("admin");
        a.setSenhaHash("$2a$10$0123456789012345678901234567890123456789012345678901"); // formato BCrypt
        em.persist(a);
    }

    @Test
    public void buscarPorLoginEncontraAdministrador() {
        Administrador a = dao.buscarPorLogin("admin");

        assertNotNull(a);
        assertEquals("admin", a.getLogin());
    }

    @Test
    public void buscarPorLoginInexistenteDevolveNull() {
        assertNull(dao.buscarPorLogin("inexistente"));
    }

    @Test
    public void buscarPorLoginDiferenciaMaiusculasEMinusculas() {
        assertNull(dao.buscarPorLogin("ADMIN"));
    }
}
