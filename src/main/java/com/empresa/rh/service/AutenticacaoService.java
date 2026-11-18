package com.empresa.rh.service;

import com.empresa.rh.dao.AdministradorDao;
import com.empresa.rh.exception.NegocioException;
import com.empresa.rh.model.Administrador;
import com.empresa.rh.util.SenhaUtil;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.Stateless;
import javax.inject.Inject;

@Stateless
public class AutenticacaoService {

    // Usado quando o login não existe: gastamos o mesmo tempo de uma conferência real,
    // para que ninguém descubra quais logins existem medindo a demora da resposta.
    private static final String HASH_FALSO = SenhaUtil.gerarHash("senha-falsa");

    private static final int TAMANHO_MINIMO = 8;
    private static final int TAMANHO_MAXIMO = 72; // limite do BCrypt

    @Inject
    private AdministradorDao administradorDao;

    public Administrador autenticar(String login, String senha) {
        String loginLimpo = login == null ? "" : login.trim();
        Administrador admin = loginLimpo.isEmpty() ? null : administradorDao.buscarPorLogin(loginLimpo);

        String hash = admin == null ? HASH_FALSO : admin.getSenhaHash();
        boolean senhaCorreta = SenhaUtil.confere(senha, hash);

        // Mensagem única: não revela se o erro foi no login ou na senha
        if (admin == null || !senhaCorreta) {
            throw new NegocioException("Login ou senha inválidos.");
        }
        return admin;
    }

    public void trocarSenha(Long adminId, String senhaAtual, String novaSenha, String confirmacao) {
        Administrador admin = adminId == null ? null : administradorDao.buscarPorId(adminId);
        if (admin == null) {
            throw new NegocioException("Administrador não encontrado.");
        }

        List<String> erros = new ArrayList<>();

        if (!SenhaUtil.confere(senhaAtual, admin.getSenhaHash())) {
            erros.add("A senha atual está incorreta.");
        }
        if (novaSenha == null || novaSenha.length() < TAMANHO_MINIMO) {
            erros.add("A nova senha deve ter pelo menos " + TAMANHO_MINIMO + " caracteres.");
        } else if (novaSenha.length() > TAMANHO_MAXIMO) {
            erros.add("A nova senha deve ter no máximo " + TAMANHO_MAXIMO + " caracteres.");
        } else {
            if (!novaSenha.equals(confirmacao)) {
                erros.add("A confirmação não confere com a nova senha.");
            }
            if (novaSenha.equals(senhaAtual)) {
                erros.add("A nova senha deve ser diferente da atual.");
            }
        }
        if (!erros.isEmpty()) {
            throw new NegocioException(erros);
        }

        // Objeto gerenciado na transação: o Hibernate faz o UPDATE sozinho no commit
        admin.setSenhaHash(SenhaUtil.gerarHash(novaSenha));
    }
}