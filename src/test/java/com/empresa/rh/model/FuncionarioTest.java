package com.empresa.rh.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.empresa.rh.model.enums.SituacaoFuncionario;
import org.junit.Test;

public class FuncionarioTest {

    @Test
    public void funcionarioNovoComecaAtivo() {
        assertEquals(SituacaoFuncionario.ATIVO, new Funcionario().getSituacao());
    }

    @Test
    public void setEnderecoMantemOsDoisLadosDoRelacionamentoSincronizados() {
        Funcionario f = new Funcionario();
        Endereco e = new Endereco();

        f.setEndereco(e);

        assertSame(e, f.getEndereco());
        assertSame(f, e.getFuncionario()); // a chave estrangeira fica no lado do Endereco
    }

    @Test
    public void setEnderecoNuloRemoveOEnderecoSemErro() {
        Funcionario f = new Funcionario();
        f.setEndereco(new Endereco());

        f.setEndereco(null);

        assertNull(f.getEndereco());
    }

    @Test
    public void funcionariosSemIdNuncaSaoIguaisEntreSi() {
        Funcionario a = new Funcionario();
        Funcionario b = new Funcionario();

        assertTrue(a.equals(a));
        assertFalse(a.equals(b));
    }

    @Test
    public void funcionariosComMesmoIdSaoIguais() {
        Funcionario a = new Funcionario();
        a.setId(1L);
        Funcionario b = new Funcionario();
        b.setId(1L);
        Funcionario c = new Funcionario();
        c.setId(2L);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertFalse(a.equals(c));
    }

    @Test
    public void naoEhIgualANuloNemAOutroTipo() {
        Funcionario a = new Funcionario();
        a.setId(1L);

        assertFalse(a.equals(null));
        assertFalse(a.equals("texto"));
    }

    @Test
    public void hashCodeNaoMudaQuandoOIdEhGeradoPeloBanco() {
        // Importante para HashSet/HashMap: o objeto não "se perde" depois do persist
        Funcionario f = new Funcionario();
        int antes = f.hashCode();

        f.setId(10L);

        assertEquals(antes, f.hashCode());
    }
}
