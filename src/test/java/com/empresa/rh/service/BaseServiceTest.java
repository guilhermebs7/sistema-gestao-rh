package com.empresa.rh.service;

import static com.empresa.rh.testutil.Excecoes.esperaNegocio;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.empresa.rh.exception.NegocioException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BaseServiceTest {

    /** BaseService é abstrata e "validar" é protected: este serviço de teste só expõe o método. */
    static class ServicoDeTeste extends BaseService {
        void validarPublico(Object entidade) {
            validar(entidade);
        }
    }

    @Mock
    private Validator validator;

    @InjectMocks
    private ServicoDeTeste servico;

    @SuppressWarnings("unchecked")
    private ConstraintViolation<Object> violacao(String mensagem) {
        ConstraintViolation<Object> v = mock(ConstraintViolation.class);
        when(v.getMessage()).thenReturn(mensagem);
        return v;
    }

    @Test
    public void semViolacoesNaoLancaNada() {
        Object entidade = new Object();
        when(validator.validate(entidade)).thenReturn(new HashSet<ConstraintViolation<Object>>());

        servico.validarPublico(entidade); // não deve lançar
    }

    @Test
    public void comViolacoesLancaNegocioExceptionComTodasAsMensagensOrdenadas() {
        Object entidade = new Object();
        Set<ConstraintViolation<Object>> violacoes = new HashSet<>(Arrays.asList(
                violacao("O nome é obrigatório"),
                violacao("A matrícula é obrigatória"),
                violacao("Selecione o cargo")));
        when(validator.validate(entidade)).thenReturn(violacoes);

        NegocioException e = esperaNegocio(() -> servico.validarPublico(entidade));

        assertEquals(Arrays.asList(
                "A matrícula é obrigatória",
                "O nome é obrigatório",
                "Selecione o cargo"), e.getMensagens());
    }
}
