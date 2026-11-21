package com.empresa.rh.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.empresa.rh.exception.NegocioException;
import com.empresa.rh.model.Administrador;
import com.empresa.rh.security.SessaoAdmin;
import com.empresa.rh.service.AutenticacaoService;
import java.util.Arrays;
import java.util.HashMap;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.Silent.class)
public class LoginControllerTest {

    @Mock
    private AutenticacaoService autenticacaoService;

    @Mock
    private SessaoAdmin sessaoAdmin;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private LoginController controller;

    private FacesContext ctx;

    @Before
    public void iniciar() {
        ctx = FacesTestSupport.instalar(new HashMap<String, String>());
        when(ctx.getExternalContext().getRequest()).thenReturn(request);
    }

    @After
    public void limpar() {
        FacesTestSupport.remover();
    }

    @Test
    public void entrarComSucessoGravaNaSessaoETrocaOIdDaSessao() {
        Administrador admin = new Administrador();
        admin.setLogin("admin");
        when(autenticacaoService.autenticar("admin", "admin123")).thenReturn(admin);
        controller.setLogin("admin");
        controller.setSenha("admin123");

        String destino = controller.entrar();

        assertEquals("/index?faces-redirect=true", destino);
        // A ordem importa: primeiro grava o admin, depois troca o id da sessão
        InOrder ordem = inOrder(sessaoAdmin, request);
        ordem.verify(sessaoAdmin).entrar(admin);
        ordem.verify(request).changeSessionId();
    }

    @Test
    public void entrarComCredenciaisInvalidasMostraErroELimpaASenha() {
        when(autenticacaoService.autenticar("admin", "errada"))
                .thenThrow(new NegocioException("Login ou senha inválidos."));
        controller.setLogin("admin");
        controller.setSenha("errada");

        String destino = controller.entrar();

        assertNull(destino);
        assertNull("A senha digitada não deve voltar para a tela", controller.getSenha());
        assertEquals(Arrays.asList("Login ou senha inválidos."),
                FacesTestSupport.textos(ctx, FacesMessage.SEVERITY_ERROR));
        verifyNoInteractions(sessaoAdmin);
    }

    @Test
    public void sairInvalidaASessaoEVoltaParaOLogin() {
        String destino = controller.sair();

        assertEquals("/login?faces-redirect=true", destino);
        verify(ctx.getExternalContext()).invalidateSession();
    }
}
