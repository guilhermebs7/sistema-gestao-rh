package com.empresa.rh.service;

import static com.empresa.rh.testutil.Excecoes.esperaNegocio;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.empresa.rh.dao.AdministradorDao;
import com.empresa.rh.exception.NegocioException;
import com.empresa.rh.model.Administrador;
import com.empresa.rh.util.SenhaUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AutenticacaoServiceTest {

    private static final String MSG_LOGIN_INVALIDO = "Login ou senha inválidos.";

    @Mock
    private AdministradorDao administradorDao;

    @InjectMocks
    private AutenticacaoService service;

    private Administrador admin(String senha) {
        Administrador a = new Administrador();
        a.setId(1L);
        a.setLogin("admin");
        a.setSenhaHash(SenhaUtil.gerarHash(senha));
        return a;
    }

    private static String repetir(char c, int vezes) {
        return new String(new char[vezes]).replace('\0', c);
    }

    // -------------------------------------------------------------- autenticar

    @Test
    public void loginESenhaCorretosDevolvemOAdministrador() {
        Administrador a = admin("senhaSegura1");
        when(administradorDao.buscarPorLogin("admin")).thenReturn(a);

        assertSame(a, service.autenticar("admin", "senhaSegura1"));
    }

    @Test
    public void espacosNasPontasDoLoginSaoIgnorados() {
        Administrador a = admin("senhaSegura1");
        when(administradorDao.buscarPorLogin("admin")).thenReturn(a);

        assertSame(a, service.autenticar("  admin  ", "senhaSegura1"));
    }

    @Test
    public void senhaErradaLancaMensagemGenerica() {
        when(administradorDao.buscarPorLogin("admin")).thenReturn(admin("senhaSegura1"));

        NegocioException e = esperaNegocio(() -> service.autenticar("admin", "outraSenha99"));

        assertEquals(MSG_LOGIN_INVALIDO, e.getMessage());
    }

    @Test
    public void loginInexistenteLancaAMesmaMensagemDeSenhaErrada() {
        // Quem erra o login não pode descobrir que o erro foi no login
        when(administradorDao.buscarPorLogin("fantasma")).thenReturn(null);

        NegocioException e = esperaNegocio(() -> service.autenticar("fantasma", "qualquerSenha1"));

        assertEquals(MSG_LOGIN_INVALIDO, e.getMessage());
    }

    @Test
    public void loginNuloOuVazioNemConsultaOBanco() {
        assertEquals(MSG_LOGIN_INVALIDO,
                esperaNegocio(() -> service.autenticar(null, "senha12345")).getMessage());
        assertEquals(MSG_LOGIN_INVALIDO,
                esperaNegocio(() -> service.autenticar("", "senha12345")).getMessage());
        assertEquals(MSG_LOGIN_INVALIDO,
                esperaNegocio(() -> service.autenticar("   ", "senha12345")).getMessage());
        verify(administradorDao, never()).buscarPorLogin(anyString());
    }

    @Test
    public void senhaNulaLancaMensagemGenerica() {
        when(administradorDao.buscarPorLogin("admin")).thenReturn(admin("senhaSegura1"));

        NegocioException e = esperaNegocio(() -> service.autenticar("admin", null));

        assertEquals(MSG_LOGIN_INVALIDO, e.getMessage());
    }

    // ------------------------------------------------------------ trocarSenha

    @Test
    public void trocaDeSenhaGravaONovoHash() {
        Administrador a = admin("senhaAtual1");
        String hashAntigo = a.getSenhaHash();
        when(administradorDao.buscarPorId(1L)).thenReturn(a);

        service.trocarSenha(1L, "senhaAtual1", "novaSenha99", "novaSenha99");

        assertNotEquals(hashAntigo, a.getSenhaHash());
        assertTrue(SenhaUtil.confere("novaSenha99", a.getSenhaHash()));
        assertTrue(!SenhaUtil.confere("senhaAtual1", a.getSenhaHash()));
        assertTrue(!a.getSenhaHash().contains("novaSenha99")); // nunca em texto puro
    }

    @Test
    public void administradorNuloOuInexistenteLancaErro() {
        assertEquals("Administrador não encontrado.", esperaNegocio(
                () -> service.trocarSenha(null, "a", "b", "b")).getMessage());
        verify(administradorDao, never()).buscarPorId(org.mockito.ArgumentMatchers.any(Long.class));

        assertEquals("Administrador não encontrado.", esperaNegocio(
                () -> service.trocarSenha(2L, "a", "b", "b")).getMessage());
    }

    @Test
    public void senhaAtualIncorretaEhRejeitadaESenhaNaoMuda() {
        Administrador a = admin("senhaAtual1");
        String hashAntigo = a.getSenhaHash();
        when(administradorDao.buscarPorId(1L)).thenReturn(a);

        NegocioException e = esperaNegocio(
                () -> service.trocarSenha(1L, "erradaErrada1", "novaSenha99", "novaSenha99"));

        assertEquals("A senha atual está incorreta.", e.getMessage());
        assertEquals(hashAntigo, a.getSenhaHash());
    }

    @Test
    public void novaSenhaCurtaDemaisEhRejeitada() {
        when(administradorDao.buscarPorId(1L)).thenReturn(admin("senhaAtual1"));

        NegocioException e = esperaNegocio(
                () -> service.trocarSenha(1L, "senhaAtual1", "curta12", "curta12")); // 7 caracteres

        assertEquals("A nova senha deve ter pelo menos 8 caracteres.", e.getMessage());
    }

    @Test
    public void novaSenhaComExatamente8CaracteresEhAceita() {
        Administrador a = admin("senhaAtual1");
        when(administradorDao.buscarPorId(1L)).thenReturn(a);

        service.trocarSenha(1L, "senhaAtual1", "oito1234", "oito1234");

        assertTrue(SenhaUtil.confere("oito1234", a.getSenhaHash()));
    }

    @Test
    public void novaSenhaNulaEhRejeitada() {
        when(administradorDao.buscarPorId(1L)).thenReturn(admin("senhaAtual1"));

        NegocioException e = esperaNegocio(
                () -> service.trocarSenha(1L, "senhaAtual1", null, null));

        assertEquals("A nova senha deve ter pelo menos 8 caracteres.", e.getMessage());
    }

    @Test
    public void novaSenhaAcimaDoLimiteDoBcryptEhRejeitada() {
        when(administradorDao.buscarPorId(1L)).thenReturn(admin("senhaAtual1"));
        String longa = repetir('a', 73);

        NegocioException e = esperaNegocio(
                () -> service.trocarSenha(1L, "senhaAtual1", longa, longa));

        assertEquals("A nova senha deve ter no máximo 72 caracteres.", e.getMessage());
    }

    @Test
    public void novaSenhaComExatamente72CaracteresEhAceita() {
        Administrador a = admin("senhaAtual1");
        when(administradorDao.buscarPorId(1L)).thenReturn(a);
        String limite = repetir('b', 72);

        service.trocarSenha(1L, "senhaAtual1", limite, limite);

        assertTrue(SenhaUtil.confere(limite, a.getSenhaHash()));
    }

    @Test
    public void confirmacaoDiferenteEhRejeitada() {
        when(administradorDao.buscarPorId(1L)).thenReturn(admin("senhaAtual1"));

        NegocioException e = esperaNegocio(
                () -> service.trocarSenha(1L, "senhaAtual1", "novaSenha99", "novaSenha98"));

        assertEquals("A confirmação não confere com a nova senha.", e.getMessage());
    }

    @Test
    public void novaSenhaIgualAAtualEhRejeitada() {
        when(administradorDao.buscarPorId(1L)).thenReturn(admin("senhaAtual1"));

        NegocioException e = esperaNegocio(
                () -> service.trocarSenha(1L, "senhaAtual1", "senhaAtual1", "senhaAtual1"));

        assertEquals("A nova senha deve ser diferente da atual.", e.getMessage());
    }

    @Test
    public void acumulaVariosErrosDeUmaVez() {
        Administrador a = admin("senhaAtual1");
        String hashAntigo = a.getSenhaHash();
        when(administradorDao.buscarPorId(1L)).thenReturn(a);

        NegocioException e = esperaNegocio(
                () -> service.trocarSenha(1L, "erradaErrada1", "novaSenha99", "diferente123"));

        assertEquals(2, e.getMensagens().size());
        assertTrue(e.getMensagens().contains("A senha atual está incorreta."));
        assertTrue(e.getMensagens().contains("A confirmação não confere com a nova senha."));
        assertEquals(hashAntigo, a.getSenhaHash());
    }
}
