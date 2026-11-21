package com.empresa.rh.service;

import static com.empresa.rh.testutil.Excecoes.esperaNegocio;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.empresa.rh.dao.DepartamentoDao;
import com.empresa.rh.dao.FuncionarioDao;
import com.empresa.rh.exception.NegocioException;
import com.empresa.rh.model.Departamento;
import java.util.Arrays;
import java.util.List;
import javax.validation.Validator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DepartamentoServiceTest {

    @Mock private DepartamentoDao departamentoDao;
    @Mock private FuncionarioDao funcionarioDao;
    @Mock private Validator validator;

    @InjectMocks
    private DepartamentoService service;

    private Departamento departamento(Long id, String nome) {
        Departamento d = new Departamento();
        d.setId(id);
        d.setNome(nome);
        return d;
    }

    // ------------------------------------------------------------------ salvar

    @Test
    public void salvaRemovendoEspacosDoNome() {
        when(departamentoDao.salvar(any(Departamento.class))).thenAnswer(returnsFirstArg());

        Departamento salvo = service.salvar(departamento(null, "   RH   "));

        assertEquals("RH", salvo.getNome());
    }

    @Test
    public void devolveOQueODaoDevolveu() {
        Departamento entrada = departamento(null, "TI");
        Departamento doBanco = departamento(1L, "TI");
        when(departamentoDao.salvar(entrada)).thenReturn(doBanco);

        assertSame(doBanco, service.salvar(entrada));
    }

    @Test
    public void naoPermiteNomeDuplicadoEmDepartamentoNovo() {
        when(departamentoDao.buscarPorNome("TI")).thenReturn(departamento(2L, "TI"));

        NegocioException e = esperaNegocio(() -> service.salvar(departamento(null, "TI")));

        assertEquals("Já existe um departamento chamado \"TI\".", e.getMessage());
        verify(departamentoDao, never()).salvar(any(Departamento.class));
    }

    @Test
    public void duplicidadeConsideraONomeJaSemEspacos() {
        when(departamentoDao.buscarPorNome("TI")).thenReturn(departamento(2L, "TI"));

        NegocioException e = esperaNegocio(() -> service.salvar(departamento(null, "  TI ")));

        assertEquals("Já existe um departamento chamado \"TI\".", e.getMessage());
    }

    @Test
    public void editarOProprioDepartamentoNaoContaComoDuplicado() {
        when(departamentoDao.buscarPorNome("TI")).thenReturn(departamento(2L, "TI"));
        when(departamentoDao.salvar(any(Departamento.class))).thenAnswer(returnsFirstArg());

        Departamento salvo = service.salvar(departamento(2L, "TI"));

        assertEquals(Long.valueOf(2L), salvo.getId());
    }

    // ----------------------------------------------------------------- excluir

    @Test
    public void excluirDepartamentoInexistenteLancaErro() {
        NegocioException e = esperaNegocio(() -> service.excluir(1L));

        assertEquals("O departamento não existe mais.", e.getMessage());
        verify(departamentoDao, never()).remover(any(Long.class));
    }

    @Test
    public void naoExcluiDepartamentoComFuncionariosVinculados() {
        when(departamentoDao.buscarPorId(1L)).thenReturn(departamento(1L, "TI"));
        when(funcionarioDao.contarPorDepartamento(1L)).thenReturn(2L);

        NegocioException e = esperaNegocio(() -> service.excluir(1L));

        assertEquals("Não é possível excluir \"TI\": existem 2 funcionário(s) vinculado(s).",
                e.getMessage());
        verify(departamentoDao, never()).remover(any(Long.class));
    }

    @Test
    public void excluiDepartamentoSemFuncionarios() {
        when(departamentoDao.buscarPorId(1L)).thenReturn(departamento(1L, "TI"));
        when(funcionarioDao.contarPorDepartamento(1L)).thenReturn(0L);

        service.excluir(1L);

        verify(departamentoDao).remover(1L);
    }

    // ---------------------------------------------------------------- consultas

    @Test
    public void consultasDelegamParaODao() {
        Departamento d = departamento(1L, "TI");
        List<Departamento> todos = Arrays.asList(d);
        when(departamentoDao.buscarPorId(1L)).thenReturn(d);
        when(departamentoDao.listarTodos()).thenReturn(todos);

        assertSame(d, service.buscarPorId(1L));
        assertSame(todos, service.listarTodos());
    }
}
