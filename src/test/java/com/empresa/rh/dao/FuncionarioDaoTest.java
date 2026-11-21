package com.empresa.rh.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.empresa.rh.model.Cargo;
import com.empresa.rh.model.Departamento;
import com.empresa.rh.model.Endereco;
import com.empresa.rh.model.Funcionario;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class FuncionarioDaoTest extends DaoTestBase {

    // CPFs válidos que NÃO contêm a sequência "11" (evita coincidir com buscas por dígitos)
    private static final String CPF_MARIA = "52998224725";
    private static final String CPF_JOAO = "39053344705";

    private FuncionarioDao dao;
    private Departamento rh;
    private Departamento ti;
    private Cargo analista;

    @Before
    public void preparar() {
        dao = com(new FuncionarioDao());
        rh = novoDepartamento("Recursos Humanos");
        ti = novoDepartamento("Tecnologia");
        analista = novoCargo("Analista", "4500.00");
    }

    // ------------------------------------------------------------ buscas exatas

    @Test
    public void buscarPorCpfEncontraFuncionario() {
        novoFuncionario("F001", "Maria Silva", CPF_MARIA, rh, analista);

        assertEquals("Maria Silva", dao.buscarPorCpf(CPF_MARIA).getNome());
    }

    @Test
    public void buscarPorCpfInexistenteDevolveNull() {
        assertNull(dao.buscarPorCpf("00000000000"));
    }

    @Test
    public void buscarPorMatriculaIgnoraMaiusculasEMinusculas() {
        novoFuncionario("F001", "Maria Silva", CPF_MARIA, rh, analista);

        assertNotNull(dao.buscarPorMatricula("f001"));
    }

    // ---------------------------------------------------------------- pesquisar

    @Test
    public void pesquisarSemTermoDevolveTodosOrdenadosPorNome() {
        novoFuncionario("F002", "Zeca Souza", CPF_JOAO, rh, analista);
        novoFuncionario("F001", "Ana Lima", CPF_MARIA, rh, analista);

        List<Funcionario> lista = dao.pesquisar(null);

        assertEquals(2, lista.size());
        assertEquals("Ana Lima", lista.get(0).getNome());
        assertEquals("Zeca Souza", lista.get(1).getNome());
    }

    @Test
    public void pesquisarTermoEmBrancoDevolveTodos() {
        novoFuncionario("F001", "Ana Lima", CPF_MARIA, rh, analista);

        assertEquals(1, dao.pesquisar("   ").size());
    }

    @Test
    public void pesquisarPorParteDoNome() {
        novoFuncionario("F001", "Maria Silva", CPF_MARIA, rh, analista);
        novoFuncionario("F002", "João Pereira", CPF_JOAO, rh, analista);

        List<Funcionario> lista = dao.pesquisar("MAR");

        assertEquals("Só a Maria deve aparecer (e termo só com letras não pode casar com todos)",
                1, lista.size());
        assertEquals("Maria Silva", lista.get(0).getNome());
    }

    @Test
    public void pesquisarPorMatricula() {
        novoFuncionario("F001", "Maria Silva", CPF_MARIA, rh, analista);
        novoFuncionario("F002", "João Pereira", CPF_JOAO, rh, analista);

        List<Funcionario> lista = dao.pesquisar("f002");

        assertEquals(1, lista.size());
        assertEquals("João Pereira", lista.get(0).getNome());
    }

    @Test
    public void pesquisarPorCpfComPontuacao() {
        novoFuncionario("F001", "Maria Silva", CPF_MARIA, rh, analista);
        novoFuncionario("F002", "João Pereira", CPF_JOAO, rh, analista);

        List<Funcionario> lista = dao.pesquisar("529.982");

        assertEquals(1, lista.size());
        assertEquals("Maria Silva", lista.get(0).getNome());
    }

    @Test
    public void pesquisarSemResultadoDevolveListaVazia() {
        novoFuncionario("F001", "Maria Silva", CPF_MARIA, rh, analista);

        assertTrue(dao.pesquisar("zzz").isEmpty());
    }

    @Test
    public void pesquisarComTentativaDeSqlInjectionNaoQuebraNemTrazTudo() {
        novoFuncionario("F001", "Maria Silva", CPF_MARIA, rh, analista);

        // O texto entra como PARÂMETRO, então é tratado como um nome qualquer
        List<Funcionario> lista = dao.pesquisar("' OR 'x'='x");

        assertTrue(lista.isEmpty());
    }

    @Test
    public void pesquisarTrazEnderecoQuandoExiste() {
        Funcionario f = criarFuncionario("F001", "Maria Silva", CPF_MARIA, rh, analista);
        Endereco e = new Endereco();
        e.setCidade("Recife");
        e.setUf("PE");
        f.setEndereco(e); // sincroniza os dois lados do relacionamento
        dao.salvar(f);    // o cascade grava o endereço junto
        em.flush();
        em.clear();

        List<Funcionario> lista = dao.pesquisar("maria");

        assertEquals("Recife", lista.get(0).getEndereco().getCidade());
    }

    @Test
    public void pesquisarTrazFuncionarioSemEndereco() {
        novoFuncionario("F001", "Maria Silva", CPF_MARIA, rh, analista);
        em.flush();
        em.clear();

        List<Funcionario> lista = dao.pesquisar("maria");

        assertEquals("O 'left join fetch' não pode descartar quem não tem endereço", 1, lista.size());
        assertNull(lista.get(0).getEndereco());
    }

    // ----------------------------------------------------------------- contagens

    @Test
    public void contarPorDepartamento() {
        novoFuncionario("F001", "Maria Silva", CPF_MARIA, rh, analista);
        novoFuncionario("F002", "João Pereira", CPF_JOAO, rh, analista);

        assertEquals(2, dao.contarPorDepartamento(rh.getId()));
        assertEquals(0, dao.contarPorDepartamento(ti.getId()));
    }

    @Test
    public void contarPorCargo() {
        Cargo gerente = novoCargo("Gerente", "9500.00");
        novoFuncionario("F001", "Maria Silva", CPF_MARIA, rh, analista);

        assertEquals(1, dao.contarPorCargo(analista.getId()));
        assertEquals(0, dao.contarPorCargo(gerente.getId()));
    }
}
