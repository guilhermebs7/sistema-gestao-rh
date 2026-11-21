package com.empresa.rh.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SenhaUtilTest {

    @Test
    public void hashNaoGuardaASenhaEmTextoPuro() {
        String hash = SenhaUtil.gerarHash("minhaSenha123");

        assertNotEquals("minhaSenha123", hash);
        assertFalse(hash.contains("minhaSenha123"));
        assertTrue(hash.startsWith("$2"));   // formato BCrypt
        assertEquals(60, hash.length());     // cabe na coluna senha_hash (length = 60)
    }

    @Test
    public void conferePassaComSenhaCorretaEFalhaComIncorreta() {
        String hash = SenhaUtil.gerarHash("minhaSenha123");

        assertTrue(SenhaUtil.confere("minhaSenha123", hash));
        assertFalse(SenhaUtil.confere("minhaSenha124", hash));
        assertFalse(SenhaUtil.confere("MINHASENHA123", hash)); // diferencia maiúsculas
        assertFalse(SenhaUtil.confere("", hash));
    }

    @Test
    public void mesmaSenhaGeraHashesDiferentesPorCausaDoSalt() {
        String h1 = SenhaUtil.gerarHash("igual");
        String h2 = SenhaUtil.gerarHash("igual");

        assertNotEquals(h1, h2);
        assertTrue(SenhaUtil.confere("igual", h1));
        assertTrue(SenhaUtil.confere("igual", h2));
    }

    @Test
    public void nuloNuncaConfere() {
        String hash = SenhaUtil.gerarHash("x1234567");

        assertFalse(SenhaUtil.confere(null, hash));
        assertFalse(SenhaUtil.confere("x1234567", null));
        assertFalse(SenhaUtil.confere(null, null));
    }

    @Test
    public void hashEmFormatoInvalidoNaoLancaExcecao() {
        assertFalse(SenhaUtil.confere("qualquer", "isso-nao-e-um-hash-bcrypt"));
        assertFalse(SenhaUtil.confere("qualquer", "abc"));
        assertFalse(SenhaUtil.confere("qualquer", " "));
    }

    /**
     * ATENÇÃO: este teste FALHA com o código atual e aponta um bug real.
     * O jBCrypt lança StringIndexOutOfBoundsException (e não IllegalArgumentException) quando o hash
     * vem vazio ou truncado, e o SenhaUtil só captura IllegalArgumentException. Na prática, um
     * senha_hash vazio/cortado no banco derrubaria o login com erro 500 em vez de "Login ou senha
     * inválidos". Correção: em SenhaUtil.confere, trocar "catch (IllegalArgumentException e)" por
     * "catch (RuntimeException e)".
     */

}
