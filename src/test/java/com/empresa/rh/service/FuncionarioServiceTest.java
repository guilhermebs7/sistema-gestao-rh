package com.empresa.rh.service;

import static com.empresa.rh.testutil.Excecoes.esperaNegocio;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.empresa.rh.dao.FuncionarioDao;
import com.empresa.rh.dao.HistoricoSalarialDao;
import com.empresa.rh.exception.NegocioException;
import com.empresa.rh.model.Cargo;
import com.empresa.rh.model.Departamento;
import com.empresa.rh.model.Endereco;
import com.empresa.rh.model.Funcionario;
import com.empresa.rh.model.HistoricoSalarial;
import com.empresa.rh.model.enums.SituacaoFuncionario;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FuncionarioServiceTest {

    private static final String CPF_VALIDO = "52998224725";

    @Mock private FuncionarioDao funcionarioDao;
    @Mock private HistoricoSalarialDao historicoSalarialDao;
    @Mock private Validator validator;

    @InjectMocks
    private FuncionarioService service;

    // ------------------------------------------------------------------ apoio

    private static Date data(int ano, int mes, int dia) {
        return paraDate(LocalDate.of(ano, mes, dia));
    }

    private static Date paraDate(LocalDate d) {
        return Date.from(d.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private Funcionario funcionarioValido() {
        Funcionario f = new Funcionario();
        f.setMatricula("MAT001");
        f.setNome("Maria da Silva");
        f.setCpf(CPF_VALIDO);
        f.setDataNascimento(data(1990, 5, 10));
        f.setDataAdmissao(data(2020, 1, 15));
        f.setEmail("maria@empresa.com");
        f.setSalarioAtual(new BigDecimal("3000"));
        f.setDepartamento(new Departamento());
        f.setCargo(new Cargo());
        return f;
    }

    private Funcionario outroFuncionario(long id) {
        Funcionario o = new Funcionario();
        o.setId(id);
        return o;
    }

    private void daoSalvaDevolvendoOArgumento() {
        when(funcionarioDao.salvar(any(Funcionario.class))).thenAnswer(returnsFirstArg());
    }

    private void esperaErro(Funcionario f, String mensagem) {
        NegocioException e = esperaNegocio(() -> service.salvar(f));
        assertTrue("Mensagens: " + e.getMensagens(), e.getMensagens().contains(mensagem));
        verify(funcionarioDao, never()).salvar(any(Funcionario.class));
    }

    // ------------------------------------------------------- salvar: caso feliz

    @Test
    public void salvaFuncionarioValidoEArredondaOSalarioParaDuasCasas() {
        daoSalvaDevolvendoOArgumento();
        Funcionario f = funcionarioValido();
        f.setSalarioAtual(new BigDecimal("3000.456"));

        Funcionario salvo = service.salvar(f);

        assertSame(f, salvo);
        assertEquals(new BigDecimal("3000.46"), salvo.getSalarioAtual());
    }

    @Test
    public void funcionarioNovoGeraAPrimeiraLinhaDoHistoricoSalarial() {
        daoSalvaDevolvendoOArgumento();

        Funcionario salvo = service.salvar(funcionarioValido());

        ArgumentCaptor<HistoricoSalarial> captor = ArgumentCaptor.forClass(HistoricoSalarial.class);
        verify(historicoSalarialDao).salvar(captor.capture());
        HistoricoSalarial h = captor.getValue();
        assertSame(salvo, h.getFuncionario());
        assertNull(h.getSalarioAnterior());
        assertEquals(new BigDecimal("3000.00"), h.getSalarioNovo());
        assertEquals("Salário inicial (admissão)", h.getMotivo());
    }

    @Test
    public void editarFuncionarioNaoGeraHistoricoInicial() {
        Funcionario f = funcionarioValido();
        f.setId(1L);
        Funcionario noBanco = funcionarioValido();
        noBanco.setId(1L);
        // O próprio funcionário aparece nas buscas por CPF/matrícula: isso NÃO é duplicidade
        when(funcionarioDao.buscarPorCpf(CPF_VALIDO)).thenReturn(noBanco);
        when(funcionarioDao.buscarPorMatricula("MAT001")).thenReturn(noBanco);
        when(funcionarioDao.buscarPorId(1L)).thenReturn(noBanco);
        daoSalvaDevolvendoOArgumento();

        service.salvar(f);

        verify(funcionarioDao).salvar(f);
        verify(historicoSalarialDao, never()).salvar(any(HistoricoSalarial.class));
    }

    // ---------------------------------------------------------- normalização

    @Test
    public void normalizaOsCamposAntesDeSalvar() {
        daoSalvaDevolvendoOArgumento();
        Funcionario f = funcionarioValido();
        f.setNome("  Maria da Silva  ");
        f.setMatricula("  mat-01 ");
        f.setCpf("529.982.247-25");
        f.setRg("   ");
        f.setEmail("  MARIA@Empresa.COM ");
        f.setTelefone(" 81 99999-0000 ");
        Endereco e = new Endereco();
        e.setLogradouro(" Rua A ");
        e.setCidade(" Recife ");
        e.setUf(" pe ");
        e.setCep("50.000-000");
        f.setEndereco(e);

        service.salvar(f);

        assertEquals("Maria da Silva", f.getNome());
        assertEquals("MAT-01", f.getMatricula());
        assertEquals(CPF_VALIDO, f.getCpf());
        assertNull(f.getRg());                       // só espaços vira null
        assertEquals("maria@empresa.com", f.getEmail());
        assertEquals("81 99999-0000", f.getTelefone());
        assertEquals("Rua A", f.getEndereco().getLogradouro());
        assertEquals("Recife", f.getEndereco().getCidade());
        assertEquals("PE", f.getEndereco().getUf());
        assertEquals("50000000", f.getEndereco().getCep());
    }

    @Test
    public void enderecoTotalmenteEmBrancoViraNulo() {
        daoSalvaDevolvendoOArgumento();
        Funcionario f = funcionarioValido();
        Endereco vazio = new Endereco();
        vazio.setLogradouro("   ");
        vazio.setCep("");
        f.setEndereco(vazio);

        service.salvar(f);

        assertNull(f.getEndereco());
    }

    @Test
    public void enderecoPreenchidoParcialmenteEhMantido() {
        daoSalvaDevolvendoOArgumento();
        Funcionario f = funcionarioValido();
        Endereco parcial = new Endereco();
        parcial.setCidade("Olinda");
        f.setEndereco(parcial);

        service.salvar(f);

        assertSame(parcial, f.getEndereco());
    }

    // ------------------------------------------------------------------ CPF

    @Test
    public void cpfInvalidoEhRejeitadoESemConsultarOBanco() {
        Funcionario f = funcionarioValido();
        f.setCpf("11111111111");

        esperaErro(f, "CPF inválido.");
        verify(funcionarioDao, never()).buscarPorCpf(anyString());
    }

    @Test
    public void cpfDeOutroFuncionarioEhRejeitado() {
        when(funcionarioDao.buscarPorCpf(CPF_VALIDO)).thenReturn(outroFuncionario(99L));

        esperaErro(funcionarioValido(), "Já existe um funcionário cadastrado com este CPF.");
    }

    // ------------------------------------------------------------ matrícula

    @Test
    public void matriculaDeOutroFuncionarioEhRejeitada() {
        when(funcionarioDao.buscarPorMatricula("MAT001")).thenReturn(outroFuncionario(99L));

        esperaErro(funcionarioValido(), "Já existe um funcionário com a matrícula \"MAT001\".");
    }

    @Test
    public void matriculaEhComparadaEmMaiusculas() {
        Funcionario f = funcionarioValido();
        f.setMatricula("mat001");
        when(funcionarioDao.buscarPorMatricula("MAT001")).thenReturn(outroFuncionario(99L));

        esperaErro(f, "Já existe um funcionário com a matrícula \"MAT001\".");
    }

    // ---------------------------------------------------------------- datas

    @Test
    public void admissaoNoFuturoEhRejeitada() {
        Funcionario f = funcionarioValido();
        f.setDataAdmissao(paraDate(LocalDate.now().plusDays(1)));

        esperaErro(f, "A data de admissão não pode ser futura.");
    }

    @Test
    public void admissaoHojeEhAceita() {
        daoSalvaDevolvendoOArgumento();
        Funcionario f = funcionarioValido();
        f.setDataAdmissao(paraDate(LocalDate.now()));

        service.salvar(f);

        verify(funcionarioDao).salvar(f);
    }

    @Test
    public void nascimentoNoFuturoOuHojeEhRejeitado() {
        Funcionario amanha = funcionarioValido();
        amanha.setDataNascimento(paraDate(LocalDate.now().plusDays(1)));
        esperaErro(amanha, "A data de nascimento deve estar no passado.");

        Funcionario hoje = funcionarioValido();
        hoje.setDataNascimento(paraDate(LocalDate.now()));
        esperaErro(hoje, "A data de nascimento deve estar no passado.");
    }

    @Test
    public void nascimentoDepoisDaAdmissaoEhRejeitado() {
        Funcionario f = funcionarioValido();
        f.setDataAdmissao(data(2020, 1, 15));
        f.setDataNascimento(data(2021, 1, 1));

        esperaErro(f, "A data de nascimento deve ser anterior à data de admissão.");
    }

    @Test
    public void nascimentoNaMesmaDataDaAdmissaoEhRejeitado() {
        Funcionario f = funcionarioValido();
        f.setDataAdmissao(data(2020, 1, 15));
        f.setDataNascimento(data(2020, 1, 15));

        esperaErro(f, "A data de nascimento deve ser anterior à data de admissão.");
    }

    @Test
    public void nascimentoEhOpcional() {
        daoSalvaDevolvendoOArgumento();
        Funcionario f = funcionarioValido();
        f.setDataNascimento(null);

        service.salvar(f);

        verify(funcionarioDao).salvar(f);
    }

    // --------------------------------------------------------------- e-mail

    @Test
    public void emailSemFormatoValidoEhRejeitado() {
        for (String invalido : new String[] {"maria", "maria@", "@empresa.com", "maria@empresa",
                "ma ria@empresa.com", "maria@@empresa.com"}) {
            Funcionario f = funcionarioValido();
            f.setEmail(invalido);

            esperaErro(f, "E-mail inválido.");
        }
    }

    @Test
    public void emailEhOpcional() {
        daoSalvaDevolvendoOArgumento();
        Funcionario f = funcionarioValido();
        f.setEmail("   ");

        service.salvar(f);

        assertNull(f.getEmail());
    }

    // ------------------------------------------------------------------- UF

    @Test
    public void ufInexistenteEhRejeitada() {
        Funcionario f = funcionarioValido();
        Endereco e = new Endereco();
        e.setUf("xx");
        f.setEndereco(e);

        esperaErro(f, "UF inválida: \"XX\".");
    }

    @Test
    public void todasAs27UfsSaoAceitas() {
        String[] ufs = {"AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO", "MA", "MT", "MS", "MG",
                "PA", "PB", "PR", "PE", "PI", "RJ", "RN", "RS", "RO", "RR", "SC", "SP", "SE", "TO"};
        daoSalvaDevolvendoOArgumento();
        for (String uf : ufs) {
            Funcionario f = funcionarioValido();
            Endereco e = new Endereco();
            e.setUf(uf.toLowerCase());
            f.setEndereco(e);

            service.salvar(f); // não deve lançar

            assertEquals(uf, f.getEndereco().getUf());
        }
    }

    // --------------------------------------------- salário na edição / erros

    @Test
    public void salarioNaoPodeSerAlteradoPelaEdicaoDoCadastro() {
        Funcionario f = funcionarioValido();
        f.setId(1L);
        f.setSalarioAtual(new BigDecimal("4000"));
        Funcionario noBanco = funcionarioValido();
        noBanco.setId(1L);
        noBanco.setSalarioAtual(new BigDecimal("3000.00"));
        when(funcionarioDao.buscarPorId(1L)).thenReturn(noBanco);

        esperaErro(f, "O salário não pode ser alterado aqui. Use a atualização salarial.");
    }

    @Test
    public void editarFuncionarioQueNaoExisteMaisEhRejeitado() {
        Funcionario f = funcionarioValido();
        f.setId(1L);

        esperaErro(f, "O funcionário não existe mais.");
    }

    @Test
    public void acumulaVariosErrosDeNegocioDeUmaVez() {
        Funcionario f = funcionarioValido();
        f.setCpf("11111111111");
        f.setEmail("invalido");
        Endereco e = new Endereco();
        e.setUf("XX");
        f.setEndereco(e);

        NegocioException ex = esperaNegocio(() -> service.salvar(f));

        assertEquals(3, ex.getMensagens().size());
        assertTrue(ex.getMensagens().contains("CPF inválido."));
        assertTrue(ex.getMensagens().contains("E-mail inválido."));
        assertTrue(ex.getMensagens().contains("UF inválida: \"XX\"."));
        verify(funcionarioDao, never()).salvar(any(Funcionario.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void erroDeValidacaoDeFormatoImpedeAsRegrasDeNegocio() {
        ConstraintViolation<Object> v = mock(ConstraintViolation.class);
        when(v.getMessage()).thenReturn("O nome é obrigatório");
        Funcionario f = funcionarioValido();
        Set<ConstraintViolation<Object>> violacoes = new HashSet<>(Collections.singletonList(v));
        when(validator.validate((Object) f)).thenReturn(violacoes);

        NegocioException e = esperaNegocio(() -> service.salvar(f));

        assertEquals(Collections.singletonList("O nome é obrigatório"), e.getMensagens());
        verify(funcionarioDao, never()).buscarPorCpf(anyString());
        verify(funcionarioDao, never()).salvar(any(Funcionario.class));
    }

    // ------------------------------------------------- inativar / reativar

    @Test
    public void inativarMudaASituacaoParaInativo() {
        Funcionario f = funcionarioValido();
        f.setSituacao(SituacaoFuncionario.ATIVO);
        when(funcionarioDao.buscarPorId(1L)).thenReturn(f);

        service.inativar(1L);

        assertEquals(SituacaoFuncionario.INATIVO, f.getSituacao());
    }

    @Test
    public void reativarMudaASituacaoParaAtivo() {
        Funcionario f = funcionarioValido();
        f.setSituacao(SituacaoFuncionario.INATIVO);
        when(funcionarioDao.buscarPorId(1L)).thenReturn(f);

        service.reativar(1L);

        assertEquals(SituacaoFuncionario.ATIVO, f.getSituacao());
    }

    @Test
    public void inativarQuemJaEstaInativoEhRejeitado() {
        Funcionario f = funcionarioValido();
        f.setSituacao(SituacaoFuncionario.INATIVO);
        when(funcionarioDao.buscarPorId(1L)).thenReturn(f);

        NegocioException e = esperaNegocio(() -> service.inativar(1L));

        assertEquals("O funcionário já está inativo.", e.getMessage());
    }

    @Test
    public void reativarQuemJaEstaAtivoEhRejeitado() {
        Funcionario f = funcionarioValido();
        f.setSituacao(SituacaoFuncionario.ATIVO);
        when(funcionarioDao.buscarPorId(1L)).thenReturn(f);

        NegocioException e = esperaNegocio(() -> service.reativar(1L));

        assertEquals("O funcionário já está ativo.", e.getMessage());
    }

    @Test
    public void alterarSituacaoDeFuncionarioInexistenteEhRejeitado() {
        assertEquals("O funcionário não existe mais.",
                esperaNegocio(() -> service.inativar(1L)).getMessage());
        assertEquals("O funcionário não existe mais.",
                esperaNegocio(() -> service.reativar(1L)).getMessage());
    }

    // ------------------------------------------------------------- consultas

    @Test
    public void consultasDelegamParaODao() {
        Funcionario f = funcionarioValido();
        List<Funcionario> lista = Arrays.asList(f);
        when(funcionarioDao.buscarPorId(1L)).thenReturn(f);
        when(funcionarioDao.pesquisar("maria")).thenReturn(lista);

        assertSame(f, service.buscarPorId(1L));
        assertSame(lista, service.pesquisar("maria"));
    }
}
