package com.empresa.rh.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.empresa.rh.exception.NegocioException;
import com.empresa.rh.model.Cargo;
import com.empresa.rh.model.Funcionario;
import com.empresa.rh.service.CargoService;
import com.empresa.rh.service.DepartamentoService;
import com.empresa.rh.service.FuncionarioService;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.Silent.class)
public class FuncionarioControllerTest {

    @Mock
    private FuncionarioService funcionarioService;

    @Mock
    private DepartamentoService departamentoService;

    @Mock
    private CargoService cargoService;

    @InjectMocks
    private FuncionarioController controller;

    private final Map<String, String> parametros = new HashMap<>();
    private FacesContext ctx;

    @Before
    public void iniciar() {
        ctx = FacesTestSupport.instalar(parametros);
    }

    @After
    public void limpar() {
        FacesTestSupport.remover();
    }

    private Funcionario funcionario(Long id, String nome) {
        Funcionario f = new Funcionario();
        f.setId(id);
        f.setNome(nome);
        return f;
    }

    // --------------------------------------------------------------------- init

    @Test
    public void initSemIdPreparaFuncionarioNovoComEndereco() {
        controller.init();

        assertNull(controller.getId());
        assertNull(controller.getFuncionario().getId());
        assertNotNull("O formulário precisa de um Endereco para ligar os campos",
                controller.getFuncionario().getEndereco());
        verifyNoInteractions(funcionarioService);
    }

    @Test
    public void initComIdCarregaFuncionario() {
        parametros.put("id", "4");
        Funcionario maria = funcionario(4L, "Maria");
        when(funcionarioService.buscarPorId(4L)).thenReturn(maria);

        controller.init();

        assertSame(maria, controller.getFuncionario());
        assertEquals(Long.valueOf(4L), controller.getId());
    }

    @Test
    public void initGaranteEnderecoQuandoOFuncionarioNaoTem() {
        parametros.put("id", "4");
        Funcionario semEndereco = funcionario(4L, "Maria"); // endereco == null
        when(funcionarioService.buscarPorId(4L)).thenReturn(semEndereco);

        controller.init();

        assertNotNull(controller.getFuncionario().getEndereco());
    }

    @Test
    public void initComIdInexistenteMostraErro() {
        parametros.put("id", "99");
        when(funcionarioService.buscarPorId(99L)).thenReturn(null);

        controller.init();

        assertNull(controller.getId());
        assertEquals(Arrays.asList("Funcionário não encontrado."),
                FacesTestSupport.textos(ctx, FacesMessage.SEVERITY_ERROR));
    }

    // ------------------------------------------------------------------- salvar

    @Test
    public void salvarComSucessoRedirecionaParaALista() {
        String destino = controller.salvar();

        assertEquals("lista?faces-redirect=true", destino);
        verify(funcionarioService).salvar(controller.getFuncionario());
        assertEquals(Arrays.asList("Funcionário salvo com sucesso."),
                FacesTestSupport.textos(ctx, FacesMessage.SEVERITY_INFO));
    }

    @Test
    public void salvarComErroPermaneceNaPaginaEDevolveOEndereco() {
        controller.getFuncionario().setEndereco(null); // o Service zera endereço em branco
        when(funcionarioService.salvar(any(Funcionario.class)))
                .thenThrow(new NegocioException(Arrays.asList("CPF inválido.", "E-mail inválido.")));

        String destino = controller.salvar();

        assertNull(destino);
        assertEquals(2, FacesTestSupport.textos(ctx, FacesMessage.SEVERITY_ERROR).size());
        assertNotNull("Sem o endereço de volta, o próximo clique em Salvar falharia",
                controller.getFuncionario().getEndereco());
    }

    // --------------------------------------------------------- inativar/reativar

    @Test
    public void inativarComSucesso() {
        controller.inativar(funcionario(1L, "Maria"));

        verify(funcionarioService).inativar(1L);
        assertEquals(Arrays.asList("Funcionário \"Maria\" inativado."),
                FacesTestSupport.textos(ctx, FacesMessage.SEVERITY_INFO));
    }

    @Test
    public void inativarComErroMostraMensagem() {
        doThrow(new NegocioException("O funcionário já está inativo."))
                .when(funcionarioService).inativar(1L);

        controller.inativar(funcionario(1L, "Maria"));

        assertEquals(Arrays.asList("O funcionário já está inativo."),
                FacesTestSupport.textos(ctx, FacesMessage.SEVERITY_ERROR));
    }

    @Test
    public void reativarComSucesso() {
        controller.reativar(funcionario(1L, "Maria"));

        verify(funcionarioService).reativar(1L);
        assertEquals(Arrays.asList("Funcionário \"Maria\" reativado."),
                FacesTestSupport.textos(ctx, FacesMessage.SEVERITY_INFO));
    }

    @Test
    public void inativarRecarregaAListaMantendoOTermoDeBusca() {
        controller.setTermoBusca("maria");
        controller.getFuncionarios(); // 1ª consulta

        controller.inativar(funcionario(1L, "Maria"));
        controller.getFuncionarios(); // lista invalidada: consulta de novo

        verify(funcionarioService, times(2)).pesquisar("maria");
    }

    // ------------------------------------------------------ salário sugerido (AJAX)

    @Test
    public void cargoSelecionadoSugereSalarioBaseEmCadastroNovo() {
        Cargo cargo = new Cargo();
        cargo.setSalarioBase(new BigDecimal("4500.00"));
        controller.getFuncionario().setCargo(cargo);

        controller.cargoSelecionado();

        assertEquals(new BigDecimal("4500.00"), controller.getFuncionario().getSalarioAtual());
    }

    @Test
    public void cargoSelecionadoNaoAlteraSalarioEmEdicao() {
        Funcionario existente = funcionario(1L, "Maria");
        existente.setSalarioAtual(new BigDecimal("1000.00"));
        Cargo cargo = new Cargo();
        cargo.setSalarioBase(new BigDecimal("4500.00"));
        existente.setCargo(cargo);
        controller.setFuncionario(existente);

        controller.cargoSelecionado();

        assertEquals(new BigDecimal("1000.00"), controller.getFuncionario().getSalarioAtual());
    }

    // ------------------------------------------------------------------ pesquisa

    @Test
    public void pesquisarUsaOTermoDigitado() {
        List<Funcionario> resultado = Collections.singletonList(funcionario(1L, "Maria"));
        when(funcionarioService.pesquisar("mar")).thenReturn(resultado);
        controller.setTermoBusca("mar");

        controller.pesquisar();

        assertSame(resultado, controller.getFuncionarios());
    }

    @Test
    public void listasDeDepartamentoECargoSaoCarregadasUmaVez() {
        controller.getDepartamentos();
        controller.getDepartamentos();
        controller.getCargos();
        controller.getCargos();

        verify(departamentoService, times(1)).listarTodos();
        verify(cargoService, times(1)).listarTodos();
    }
}
