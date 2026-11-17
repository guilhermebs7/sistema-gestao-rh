package com.empresa.rh.util;

public final class CpfUtil {

    private CpfUtil() {
    }

    /** Recebe só dígitos (11). Devolve true se os dígitos verificadores conferem. */
    public static boolean isValido(String cpf) {
        if (cpf == null || !cpf.matches("\\d{11}")) {
            return false;
        }
        // 111.111.111-11, 222.222.222-22... passam na conta, mas são inválidos
        if (cpf.matches("(\\d)\\1{10}")) {
            return false;
        }
        String base = cpf.substring(0, 9);
        int digito1 = calcularDigito(base, 10);
        int digito2 = calcularDigito(base + digito1, 11);
        return cpf.equals(base + digito1 + digito2);
    }

    // Multiplica cada dígito por um peso decrescente, soma e aplica o resto da divisão por 11
    private static int calcularDigito(String base, int pesoInicial) {
        int soma = 0;
        for (int i = 0; i < base.length(); i++) {
            soma += Character.getNumericValue(base.charAt(i)) * (pesoInicial - i);
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }
}