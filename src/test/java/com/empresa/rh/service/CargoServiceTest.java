package com.empresa.rh.service;

import static com.empresa.rh.testutil.Excecoes.esperaNegocio;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.empresa.rh.dao.CargoDao;
import com.empresa.rh.dao.FuncionarioDao;
import com.empresa.rh.exception.NegocioException;
import com.empresa.rh.model.Cargo;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CargoServiceTest {

    @Mock private CargoDao cargoDao;
    @Mock private FuncionarioDao funcionarioDao;
    @Mock private Validator validator; // vai para o campo privado de BaseService

    @InjectMocks
    private CargoService service;

    private Cargo cargo(Long id, String nome, String salarioBase) {
        Cargo c = new Cargo();
        c.setId(id);
        c.setNome(nome);
        c.setSalarioBase(new BigDecimal(salarioBase));
        return c;
    }

    // ------------------------------------------------------------------ salvar

    @Test
    public void salvaRemovendoEspacosDoNomeEArredondandoOSalarioParaDuasCasas() {
        Cargo c = cargo(null, "  Analista de Sistemas  ", "1234.565");
        when(cargoDao.salvar(any(Cargo.class))).thenAnswer(returnsFirstArg());

        Cargo salvo = service.salvar(c);

        assertEquals("Analista de Sistemas", salvo.getNome());
        assertEquals(new BigDecimal("1234.57"), salvo.getSalarioBase()); // HALF_UP
    }

    @Test
    public void salarioInteiroGanhaCasasDecimais() {
        Cargo c = cargo(null, "Dev", "1000");
        when(cargoDao.salvar(any(Cargo.class))).thenAnswer(returnsFirstArg());

        Cargo salvo = service.salvar(c);

        assertEquals("1000.00", salvo.getSalarioBase().toPlainString());
    }

    @Test
    public void devolveOQueODaoDevolveu() {
        Cargo entrada = cargo(null, "Dev", "100");
        Cargo doBanco = cargo(10L, "Dev", "100.00");
        when(cargoDao.salvar(entrada)).thenReturn(doBanco);

        assertSame(doBanco, service.salvar(entrada));
    }

    @Test
    public void naoPermiteNomeDuplicadoEmCargoNovo() {
        when(cargoDao.buscarPorNome("Analista")).thenReturn(cargo(5L, "Analista", "100"));

        NegocioException e = esperaNegocio(() -> service.salvar(cargo(null, "Analista", "200")));

        assertEquals("Já existe um cargo chamado \"Analista\".", e.getMessage());
        verify(cargoDao, never()).salvar(any(Cargo.class));
    }

    @Test
    public void naoPermiteRenomearParaONomeDeOutroCargo() {
        when(cargoDao.buscarPorNome("Analista")).thenReturn(cargo(5L, "Analista", "100"));

        NegocioException e = esperaNegocio(() -> service.salvar(cargo(6L, "Analista", "200")));

        assertEquals("Já existe um cargo chamado \"Analista\".", e.getMessage());
    }

    @Test
    public void editarOMesmoCargoSemMudarONomeNaoContaComoDuplicado() {
        Cargo existente = cargo(5L, "Analista", "100");
        when(cargoDao.buscarPorNome("Analista")).thenReturn(existente);
        when(cargoDao.salvar(any(Cargo.class))).thenAnswer(returnsFirstArg());

        Cargo salvo = service.salvar(cargo(5L, "Analista", "300"));

        assertEquals("300.00", salvo.getSalarioBase().toPlainString());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void erroDeValidacaoImpedeABuscaPorNomeEOSalvamento() {
        ConstraintViolation<Object> v = mock(ConstraintViolation.class);
        when(v.getMessage()).thenReturn("O nome é obrigatório");
        Cargo c = cargo(null, "Dev", "100");
        Set<ConstraintViolation<Object>> violacoes = new HashSet<>(Collections.singletonList(v));
        when(validator.validate((Object) c)).thenReturn(violacoes);

        NegocioException e = esperaNegocio(() -> service.salvar(c));

        assertEquals(Collections.singletonList("O nome é obrigatório"), e.getMensagens());
        verify(cargoDao, never()).buscarPorNome(any(String.class));
        verify(cargoDao, never()).salvar(any(Cargo.class));
    }

    // ----------------------------------------------------------------- excluir

    @Test
    public void excluirCargoInexistenteLancaErro() {
        NegocioException e = esperaNegocio(() -> service.excluir(1L));

        assertEquals("O cargo não existe mais.", e.getMessage());
        verify(cargoDao, never()).remover(any(Long.class));
    }

    @Test
    public void naoExcluiCargoComFuncionariosVinculados() {
        when(cargoDao.buscarPorId(1L)).thenReturn(cargo(1L, "Dev", "100"));
        when(funcionarioDao.contarPorCargo(1L)).thenReturn(3L);

        NegocioException e = esperaNegocio(() -> service.excluir(1L));

        assertEquals("Não é possível excluir \"Dev\": existem 3 funcionário(s) neste cargo.",
                e.getMessage());
        verify(cargoDao, never()).remover(any(Long.class));
    }

    @Test
    public void excluiCargoSemFuncionarios() {
        when(cargoDao.buscarPorId(1L)).thenReturn(cargo(1L, "Dev", "100"));
        when(funcionarioDao.contarPorCargo(1L)).thenReturn(0L);

        service.excluir(1L);

        verify(cargoDao).remover(1L);
    }

    // ---------------------------------------------------------------- consultas

    @Test
    public void consultasDelegamParaODao() {
        Cargo c = cargo(1L, "Dev", "100");
        List<Cargo> todos = Arrays.asList(c);
        when(cargoDao.buscarPorId(1L)).thenReturn(c);
        when(cargoDao.listarTodos()).thenReturn(todos);

        assertSame(c, service.buscarPorId(1L));
        assertSame(todos, service.listarTodos());
    }
}
