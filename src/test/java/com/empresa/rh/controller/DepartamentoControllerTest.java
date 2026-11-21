package com.empresa.rh.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.empresa.rh.exception.NegocioException;
import com.empresa.rh.model.Departamento;
import com.empresa.rh.service.DepartamentoService;
import java.util.Arrays;
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

/** O CargoController tem exatamente o mesmo molde; este teste serve de modelo para ele. */
@RunWith(MockitoJUnitRunner.Silent.class)    //informa ao Junit que os testes utilizarão o mockito
public class DepartamentoControllerTest {

    @Mock       //cria um objeto simulado do DepartamentoService
    private DepartamentoService service;

    @InjectMocks         //cria o controller e injeta nele os objetos simulados necessarios
    private DepartamentoController controller;

    private final Map<String, String> parametros = new HashMap<>();      //o parametros representa os parãmetros recebidos pela página JSF . ex: quando acessamos uma tela : departamento.xhtml?id=3 o parametro id recebe o valor 3
    private FacesContext ctx;      //representa o contexto da aplicação JSF , utilizado para trabalhar com mensagens, requisições e outras informações da interface

    @Before
    public void iniciar() {
        ctx = FacesTestSupport.instalar(parametros);    //prepara o ambiente JSF para que o controller consiga trabalhar normalmente durante os testes
    }

    @After
    public void limpar() {
        FacesTestSupport.remover();    // remove o contexto JSF simulado. Importante para evitar que um teste interfira no outro
    }

    private Departamento departamento(Long id, String nome) {
        Departamento d = new Departamento();
        d.setId(id);
        d.setNome(nome);
        return d;
    }

    // --------------------------------------------------------------------- init

    @Test
    public void initSemIdMantemDepartamentoNovo() {
        controller.init();

        assertNull(controller.getId());   //verifica se o ID do controller continua nulo
        assertNull(controller.getDepartamento().getId());   //verifica se o departamento criado não possui ID
        verifyNoInteractions(service);   //verifica se o serviço não foi chamado
    }

    @Test
    public void initComIdCarregaDepartamentoParaEdicao() {
        parametros.put("id", "3");   //simula que a pagina recebeu o parâmetro ID igual a 3.
        Departamento rh = departamento(3L, "RH");     //cria um departamento com ID 3 e nome RH.
        when(service.buscarPorId(3L)).thenReturn(rh);    //"quando o controller procurar o departamento de ID 3, devolva o departamento RH

        controller.init();

        assertEquals(Long.valueOf(3L), controller.getId());     //verifica se o controller recebeu o ID 3
        assertSame(rh, controller.getDepartamento());   // assertSame = verifica se duas refer~encias apontam para o mesmo objeto
    }

    @Test
    public void initComIdInexistenteMostraErro() {
        parametros.put("id", "99");
        when(service.buscarPorId(99L)).thenReturn(null);

        controller.init();

        assertNull(controller.getId());  //verifica se o objeto é nulo
        assertEquals(Arrays.asList("Departamento não encontrado."),
                FacesTestSupport.textos(ctx, FacesMessage.SEVERITY_ERROR));  // recupera as mensagens de erro que foram adicionadas ao contexto JSF
    }

    @Test
    public void initComIdInvalidoNaTelaEIgnorado() {
        parametros.put("id", "abc");

        controller.init();

        assertNull(controller.getId());
        verifyNoInteractions(service);
    }

    // ------------------------------------------------------------------- salvar

    @Test
    public void salvarComSucessoRedirecionaParaALista() {
        String destino = controller.salvar();

        assertEquals("lista?faces-redirect=true", destino);
        verify(service).salvar(controller.getDepartamento());
        assertEquals(Arrays.asList("Departamento salvo com sucesso."),
                FacesTestSupport.textos(ctx, FacesMessage.SEVERITY_INFO));
        // sem isso, a mensagem de sucesso se perderia no redirect
        verify(ctx.getExternalContext().getFlash()).setKeepMessages(true);
    }

    @Test
    public void salvarComErroDeNegocioPermaneceNaPagina() {
        when(service.salvar(any(Departamento.class)))
                .thenThrow(new NegocioException("Já existe um departamento chamado \"RH\"."));

        String destino = controller.salvar();

        assertNull("null = permanece na mesma página", destino);
        assertEquals(Arrays.asList("Já existe um departamento chamado \"RH\"."),
                FacesTestSupport.textos(ctx, FacesMessage.SEVERITY_ERROR));
    }

    // ------------------------------------------------------------------ excluir

    @Test
    public void excluirComSucessoMostraMensagemERecarregaALista() {
        Departamento d = departamento(5L, "Vendas");
        controller.getDepartamentos(); // 1ª consulta da lista

        controller.excluir(d);
        controller.getDepartamentos(); // a lista foi invalidada: consulta de novo

        verify(service).excluir(5L);
        verify(service, times(2)).listarTodos();
        assertEquals(Arrays.asList("Departamento \"Vendas\" excluído com sucesso."),
                FacesTestSupport.textos(ctx, FacesMessage.SEVERITY_INFO));
    }

    @Test
    public void excluirComVinculoMostraErro() {
        Departamento d = departamento(5L, "Vendas");
        doThrow(new NegocioException("Não é possível excluir \"Vendas\": existem 1 funcionário(s) vinculado(s)."))
                .when(service).excluir(5L);

        controller.excluir(d);

        List<String> erros = FacesTestSupport.textos(ctx, FacesMessage.SEVERITY_ERROR);
        assertEquals(1, erros.size());
        assertTrue(erros.get(0).startsWith("Não é possível excluir"));    //verifica se uma condicao é verdadeira
        assertTrue(FacesTestSupport.textos(ctx, FacesMessage.SEVERITY_INFO).isEmpty());
    }

    // ------------------------------------------------------------------- lista

    @Test
    public void listaECarregadaSoUmaVez() {
        controller.getDepartamentos();
        controller.getDepartamentos();

        verify(service, times(1)).listarTodos();
    }
}
