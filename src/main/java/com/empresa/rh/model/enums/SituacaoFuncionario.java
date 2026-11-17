package com.empresa.rh.model.enums;

public enum SituacaoFuncionario {

    ATIVO("Ativo"),
    INATIVO("Inativo");

    private final String descricao;

    SituacaoFuncionario(String descricao) {
        this.descricao = descricao;
    }

    // Texto amigável para mostrar nas telas
    public String getDescricao() {
        return descricao;
    }
}