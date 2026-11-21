package com.empresa.rh.service;

import static com.empresa.rh.testutil.Excecoes.esperaNegocio;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.empresa.rh.dao.FuncionarioDao;
import com.empresa.rh.dao.HistoricoSalarialDao;
import com.empresa.rh.exception.NegocioException;
import com.empresa.rh.model.Funcionario;
import com.empresa.rh.model.HistoricoSalarial;
import com.empresa.rh.model.enums.SituacaoFuncionario;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SalarioServiceTest {

    @Mock private FuncionarioDao funcionarioDao;
    @Mock private HistoricoSalarialDao historicoSalarialDao;

    @InjectMocks
    private SalarioService service;

    private Funcionario ativoComSalario(String salario) {
        Funcionario f = new Funcionario();
        f.setId(1L);
        f.setSituacao(SituacaoFuncionario.ATIVO);
        f.setSalarioAtual(new BigDecimal(salario));
        return f;
    }

    private void naoGravouNada(Funcionario f, String salarioOriginal) {
        verify(historicoSalarialDao, never()).salvar(any(HistoricoSalarial.class));
        assertEquals(0, new BigDecimal(salarioOriginal).compareTo(f.getSalarioAtual()));
    }

    // -------------------------------------------------------------- caso feliz

    @Test
    public void reajusteGravaHistoricoComValorAntigoENovoEAtualizaOSalario() {
        Funcionario f = ativoComSalario("3000.00");
        when(funcionarioDao.buscarPorId(1L)).thenReturn(f);

        service.atualizar(1L, new BigDecimal("3500.456"), "  Promoção por mérito  ");

        ArgumentCaptor<HistoricoSalarial> captor = ArgumentCaptor.forClass(HistoricoSalarial.class);
        verify(historicoSalarialDao).salvar(captor.capture());
        HistoricoSalarial h = captor.getValue();

        assertSame(f, h.getFuncionario());
        assertEquals(new BigDecimal("3000.00"), h.getSalarioAnterior());
        assertEquals(new BigDecimal("3500.46"), h.getSalarioNovo());   // HALF_UP, 2 casas
        assertEquals("Promoção por mérito", h.getMotivo());            // sem espaços nas pontas
        assertEquals(new BigDecimal("3500.46"), f.getSalarioAtual());  // dirty checking fará o UPDATE
    }

    @Test
    public void permiteReduzirOSalario() {
        Funcionario f = ativoComSalario("3000.00");
        when(funcionarioDao.buscarPorId(1L)).thenReturn(f);

        service.atualizar(1L, new BigDecimal("2500"), "Ajuste de carga horária");

        assertEquals(new BigDecimal("2500.00"), f.getSalarioAtual());
    }

    // ------------------------------------------------------ funcionário/estado

    @Test
    public void funcionarioInexistenteLancaErro() {
        NegocioException e = esperaNegocio(
                () -> service.atualizar(1L, new BigDecimal("100"), "Motivo"));

        assertEquals("O funcionário não existe mais.", e.getMessage());
        verify(historicoSalarialDao, never()).salvar(any(HistoricoSalarial.class));
    }

    @Test
    public void funcionarioInativoNaoRecebeReajuste() {
        Funcionario f = ativoComSalario("3000.00");
        f.setSituacao(SituacaoFuncionario.INATIVO);
        when(funcionarioDao.buscarPorId(1L)).thenReturn(f);

        NegocioException e = esperaNegocio(
                () -> service.atualizar(1L, new BigDecimal("4000"), "Motivo"));

        assertEquals("Funcionário inativo não pode receber reajuste salarial.", e.getMessage());
        naoGravouNada(f, "3000.00");
    }

    // --------------------------------------------------------------- validações

    @Test
    public void salarioNuloZeroOuNegativoEhRejeitado() {
        for (BigDecimal invalido : new BigDecimal[] {null, BigDecimal.ZERO, new BigDecimal("-10")}) {
            Funcionario f = ativoComSalario("3000.00");
            when(funcionarioDao.buscarPorId(1L)).thenReturn(f);

            NegocioException e = esperaNegocio(() -> service.atualizar(1L, invalido, "Motivo"));

            assertEquals(1, e.getMensagens().size());
            assertEquals("O novo salário deve ser maior que zero.", e.getMensagens().get(0));
            naoGravouNada(f, "3000.00");
        }
    }

    @Test
    public void salarioIgualAoAtualEhRejeitadoMesmoComCasasDecimaisDiferentes() {
        Funcionario f = ativoComSalario("3000.00");
        when(funcionarioDao.buscarPorId(1L)).thenReturn(f);

        NegocioException e = esperaNegocio(
                () -> service.atualizar(1L, new BigDecimal("3000"), "Motivo"));

        assertEquals("O novo salário deve ser diferente do salário atual.", e.getMessage());
        naoGravouNada(f, "3000.00");
    }

    @Test
    public void salarioQueArredondaParaOValorAtualTambemEhRejeitado() {
        Funcionario f = ativoComSalario("3000.00");
        when(funcionarioDao.buscarPorId(1L)).thenReturn(f);

        NegocioException e = esperaNegocio(
                () -> service.atualizar(1L, new BigDecimal("3000.004"), "Motivo"));

        assertEquals("O novo salário deve ser diferente do salário atual.", e.getMessage());
    }

    @Test
    public void motivoEhObrigatorio() {
        for (String motivo : new String[] {null, "", "    "}) {
            Funcionario f = ativoComSalario("3000.00");
            when(funcionarioDao.buscarPorId(1L)).thenReturn(f);

            NegocioException e = esperaNegocio(
                    () -> service.atualizar(1L, new BigDecimal("3500"), motivo));

            assertEquals("O motivo da alteração é obrigatório.", e.getMessage());
            naoGravouNada(f, "3000.00");
        }
    }

    @Test
    public void motivoTemNoMaximo255Caracteres() {
        Funcionario f = ativoComSalario("3000.00");
        when(funcionarioDao.buscarPorId(1L)).thenReturn(f);
        String motivo256 = new String(new char[256]).replace('\0', 'a');

        NegocioException e = esperaNegocio(
                () -> service.atualizar(1L, new BigDecimal("3500"), motivo256));

        assertEquals("O motivo deve ter no máximo 255 caracteres.", e.getMessage());
    }

    @Test
    public void motivoComExatamente255CaracteresEhAceito() {
        Funcionario f = ativoComSalario("3000.00");
        when(funcionarioDao.buscarPorId(1L)).thenReturn(f);
        String motivo255 = new String(new char[255]).replace('\0', 'a');

        service.atualizar(1L, new BigDecimal("3500"), motivo255);

        verify(historicoSalarialDao).salvar(any(HistoricoSalarial.class));
    }

    @Test
    public void acumulaTodosOsErrosDeUmaVez() {
        Funcionario f = ativoComSalario("3000.00");
        when(funcionarioDao.buscarPorId(1L)).thenReturn(f);

        NegocioException e = esperaNegocio(() -> service.atualizar(1L, BigDecimal.ZERO, "   "));

        assertEquals(2, e.getMensagens().size());
        assertTrue(e.getMensagens().contains("O novo salário deve ser maior que zero."));
        assertTrue(e.getMensagens().contains("O motivo da alteração é obrigatório."));
        naoGravouNada(f, "3000.00");
    }

    // ---------------------------------------------------------------- histórico

    @Test
    public void listarHistoricoDelegaParaODao() {
        List<HistoricoSalarial> historico = Arrays.asList(new HistoricoSalarial());
        when(historicoSalarialDao.listarPorFuncionario(1L)).thenReturn(historico);

        assertSame(historico, service.listarHistorico(1L));
    }
}
