package com.empresa.rh.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.empresa.rh.model.Administrador;
import org.junit.Test;

public class SessaoAdminTest {

    @Test
    public void sessaoNovaNaoTemNinguemLogado() {
        SessaoAdmin sessao = new SessaoAdmin();

        assertFalse(sessao.isLogado());
        assertNull(sessao.getId());
        assertNull(sessao.getLogin());
    }

    @Test
    public void entrarGuardaIdELoginEMarcaComoLogado() {
        Administrador admin = new Administrador();
        admin.setId(7L);
        admin.setLogin("admin");
        SessaoAdmin sessao = new SessaoAdmin();

        sessao.entrar(admin);

        assertTrue(sessao.isLogado());
        assertEquals(Long.valueOf(7L), sessao.getId());
        assertEquals("admin", sessao.getLogin());
    }

    @Test
    public void naoGuardaOHashDaSenhaNaSessao() {
        // Só id e login ficam na sessão (que pode ser serializada/replicada)
        for (java.lang.reflect.Field campo : SessaoAdmin.class.getDeclaredFields()) {
            assertFalse("Campo suspeito: " + campo.getName(),
                    campo.getName().toLowerCase().contains("senha")
                            || campo.getName().toLowerCase().contains("hash"));
        }
    }
}
