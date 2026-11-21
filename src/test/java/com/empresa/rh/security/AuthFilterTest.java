package com.empresa.rh.security;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;
import java.io.StringWriter;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AuthFilterTest {

    private static final String CONTEXTO = "/rh";

    @Mock private SessaoAdmin sessaoAdmin;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private FilterChain chain;

    @InjectMocks
    private AuthFilter filtro;

    @Before
    public void configurarContexto() {
        // lenient por existirem testes que não chegam a ler o contexto
        org.mockito.Mockito.lenient().when(request.getContextPath()).thenReturn(CONTEXTO);
    }

    private void acessa(String caminho) {
        when(request.getRequestURI()).thenReturn(CONTEXTO + caminho);
    }

    // ---------------------------------------------------- recursos públicos

    @Test
    public void paginaDeLoginEstaLiberadaSemAutenticar() throws Exception {
        acessa("/login.xhtml");

        filtro.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(sessaoAdmin, never()).isLogado();
        verify(response, never()).sendRedirect(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    public void cssImagensEResourcesDoJsfEstaoLiberados() throws Exception {
        String[] publicos = {
                "/resources/css/login.css",
                "/resources/css/estilo.css",
                "/resources/img/fundo-login.jpg",   // foto de fundo da tela de login
                "/javax.faces.resource/jsf.js.xhtml"
        };
        for (String caminho : publicos) {
            acessa(caminho);
            filtro.doFilter(request, response, chain);
        }

        verify(chain, times(publicos.length)).doFilter(request, response);
        verify(sessaoAdmin, never()).isLogado();
    }

    // ------------------------------------------------------- sem autenticação

    @Test
    public void paginaProtegidaSemLoginRedirecionaParaOLogin() throws Exception {
        acessa("/funcionario/lista.xhtml");
        when(sessaoAdmin.isLogado()).thenReturn(false);

        filtro.doFilter(request, response, chain);

        verify(response).sendRedirect("/rh/login.xhtml");
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    public void paginaInicialTambemExigeLogin() throws Exception {
        acessa("/index.xhtml");
        when(sessaoAdmin.isLogado()).thenReturn(false);

        filtro.doFilter(request, response, chain);

        verify(response).sendRedirect("/rh/login.xhtml");
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    public void caminhoParecidoComOLoginNaoEhLiberado() throws Exception {
        acessa("/login.xhtml.bak");
        when(sessaoAdmin.isLogado()).thenReturn(false);

        filtro.doFilter(request, response, chain);

        verify(response).sendRedirect("/rh/login.xhtml");
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    public void requisicaoAjaxSemSessaoRecebeRespostaXmlDeRedirecionamento() throws Exception {
        acessa("/funcionario/lista.xhtml");
        when(sessaoAdmin.isLogado()).thenReturn(false);
        when(request.getHeader("Faces-Request")).thenReturn("partial/ajax");
        StringWriter saida = new StringWriter();
        PrintWriter escritor = new PrintWriter(saida);
        when(response.getWriter()).thenReturn(escritor);

        filtro.doFilter(request, response, chain);
        escritor.flush();

        verify(response).setContentType("text/xml");
        assertTrue(saida.toString().contains("<partial-response><redirect url=\"/rh/login.xhtml\"/>"));
        verify(response, never()).sendRedirect(org.mockito.ArgumentMatchers.anyString());
        verify(chain, never()).doFilter(request, response);
    }

    // ------------------------------------------------------- com autenticação

    @Test
    public void usuarioLogadoPassaEOsHeadersImpedemCacheDaPagina() throws Exception {
        acessa("/funcionario/lista.xhtml");
        when(sessaoAdmin.isLogado()).thenReturn(true);

        filtro.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(response).setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        verify(response).setHeader("Pragma", "no-cache");
        verify(response).setDateHeader("Expires", 0);
        verify(response, never()).sendRedirect(org.mockito.ArgumentMatchers.anyString());
    }
}
