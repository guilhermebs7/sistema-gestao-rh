package com.empresa.rh.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.empresa.rh.exception.NegocioException;
import com.empresa.rh.model.Funcionario;
import com.empresa.rh.service.FuncionarioService;
import com.empresa.rh.service.SalarioService;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
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
public class SalarioControllerTest {

    @Mock
    private SalarioService salarioService;

    @Mock
    private FuncionarioService funcionarioService;

    @InjectMocks
    private SalarioController controller;

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

    private Funcionario funcionario(Long id) {
        Funcionario f = new Funcionario();
        f.setId(id);
        f.setNome("Maria Silva");
        return f;
    }

    @Test
    public void initComIdValidoCarregaFuncionario() {
        parametros.put("id", "7");
        Funcionario maria = funcionario(7L);
        when(funcionarioService.buscarPorId(7L)).thenReturn(maria);

        controller.init();

        assertSame(maria, controller.getFuncionario());
        assertTrue(FacesTestSupport.textos(ctx, FacesMessage.SEVERITY_ERROR).isEmpty());
    }

    @Test
    public void initSemIdMostraErro() {
        controller.init();

        assertNull(controller.getFuncionario());
        assertEquals(Arrays.asList("Funcionário não encontrado."),
                FacesTestSupport.textos(ctx, FacesMessage.SEVERITY_ERROR));
    }

    @Test
    public void initComIdInexistenteMostraErro() {
        parametros.put("id", "99");
        when(funcionarioService.buscarPorId(99L)).thenReturn(null);

        controller.init();

        assertNull(controller.getFuncionario());
        assertEquals(1, FacesTestSupport.textos(ctx, FacesMessage.SEVERITY_ERROR).size());
    }

    @Test
    public void salvarSemFuncionarioCarregadoNaoChamaOService() {
        controller.init(); // sem id: funcionario fica null

        String destino = controller.salvar();

        assertNull(destino);
        verifyNoInteractions(salarioService);
    }

    @Test
    public void salvarComSucessoVaiParaODetalheDoFuncionario() {
        parametros.put("id", "7");
        when(funcionarioService.buscarPorId(7L)).thenReturn(funcionario(7L));
        controller.init();
        controller.setNovoSalario(new BigDecimal("5200.00"));
        controller.setMotivo("Promoção");

        String destino = controller.salvar();

        assertEquals("detalhe?faces-redirect=true&id=7", destino);
        verify(salarioService).atualizar(7L, new BigDecimal("5200.00"), "Promoção");
        assertEquals(Arrays.asList("Salário atualizado com sucesso."),
                FacesTestSupport.textos(ctx, FacesMessage.SEVERITY_INFO));
    }

    @Test
    public void salvarMostraTodasAsMensagensDeErroDoService() {
        parametros.put("id", "7");
        when(funcionarioService.buscarPorId(7L)).thenReturn(funcionario(7L));
        controller.init();
        doThrow(new NegocioException(Arrays.asList(
                "O novo salário deve ser maior que zero.",
                "O motivo da alteração é obrigatório.")))
                .when(salarioService).atualizar(any(), any(), any());

        String destino = controller.salvar();

        assertNull(destino);
        assertEquals(2, FacesTestSupport.textos(ctx, FacesMessage.SEVERITY_ERROR).size());
    }
}
