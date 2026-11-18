package com.empresa.rh.util;

import org.mindrot.jbcrypt.BCrypt;


//Classe cuja finalidade é trabalhar com a segurança de senhas de usuarios utilizando o algoritmo BCrypt
// possui duas funções principais : 1. Gerar um hash da senha para armazenar no bd. 2.verificar se a senha digitada corresponde a senha cadastrada

public final class SenhaUtil {

    private SenhaUtil() {
    }

    /** Gera o hash BCrypt (com "salt" aleatório embutido) para gravar no banco. */
    public static String gerarHash(String senha) {
        return BCrypt.hashpw(senha, BCrypt.gensalt(10));
    }

    /** Confere a senha digitada contra o hash do banco. */
    public static boolean confere(String senha, String hash) {
        if (senha == null || hash == null) {
            return false;
        }
        try {
            return BCrypt.checkpw(senha, hash);
        } catch (IllegalArgumentException e) {
            return false; // hash em formato inválido
        }
    }
}

//hash é uma transformaçao de uma informação (como uma senha) em uma sequencia de caracteres que representa aquela informacao.
// o hash é utilizado para proteger as senhas dos usuarios , impedindo que elas fiquem armazenadas em texto puro  no banco de dados