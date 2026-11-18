package com.empresa.rh.security;

import java.io.IOException;
import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebFilter(urlPatterns = "/*")
public class AuthFilter implements Filter {

    @Inject
    private SessaoAdmin sessaoAdmin;

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain chain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String caminho = request.getRequestURI().substring(request.getContextPath().length());

        if (ehPublico(caminho)) {
            chain.doFilter(request, response);
            return;
        }

        if (sessaoAdmin.isLogado()) {
            // Impede que o botão "Voltar" do navegador mostre páginas guardadas após o logout
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            response.setHeader("Pragma", "no-cache");
            response.setDateHeader("Expires", 0);
            chain.doFilter(request, response);
            return;
        }

        redirecionarParaLogin(request, response);
    }

    // Só o login e os arquivos estáticos (CSS etc.) são liberados sem autenticação
    private boolean ehPublico(String caminho) {
        return caminho.equals("/login.xhtml")
                || caminho.startsWith("/javax.faces.resource/")
                || caminho.startsWith("/resources/");
    }

    private void redirecionarParaLogin(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String destino = request.getContextPath() + "/login.xhtml";

        // Requisição AJAX com sessão expirada: o JSF espera uma resposta XML especial
        if ("partial/ajax".equals(request.getHeader("Faces-Request"))) {
            response.setContentType("text/xml");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().printf(
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                            + "<partial-response><redirect url=\"%s\"/></partial-response>", destino);
        } else {
            response.sendRedirect(destino);
        }
    }

    @Override
    public void destroy() {
    }
}