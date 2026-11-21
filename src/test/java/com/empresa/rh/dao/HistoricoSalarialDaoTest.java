package com.empresa.rh.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.empresa.rh.model.Cargo;
import com.empresa.rh.model.Departamento;
import com.empresa.rh.model.Funcionario;
import com.empresa.rh.model.HistoricoSalarial;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class HistoricoSalarialDaoTest extends DaoTestBase {

    private HistoricoSalarialDao dao;
    private Funcionario maria;
    private Funcionario joao;

    @Before
    public void preparar() {
        dao = com(new HistoricoSalarialDao());
        Departamento rh = novoDepartamento("Recursos Humanos");
        Cargo analista = novoCargo("Analista", "4500.00");
        maria = novoFuncionario("F001", "Maria Silva", "52998224725", rh, analista);
        joao = novoFuncionario("F002", "João Pereira", "39053344705", rh, analista);
    }

    private HistoricoSalarial registro(Funcionario f, String anterior, String novo, long dataEmMillis) {
        HistoricoSalarial h = new HistoricoSalarial();
        h.setFuncionario(f);
        h.setSalarioAnterior(anterior == null ? null : new BigDecimal(anterior));
        h.setSalarioNovo(new BigDecimal(novo));
        h.setMotivo("teste");
        h.setDataAlteracao(new Date(dataEmMillis));
        em.persist(h);
        return h;
    }

    @Test
    public void listarPorFuncionarioDevolveMaisRecentePrimeiro() {
        registro(maria, null, "4500.00", 1_000_000_000_000L);
        registro(maria, "4500.00", "5200.00", 1_700_000_000_000L);

        List<HistoricoSalarial> lista = dao.listarPorFuncionario(maria.getId());

        assertEquals(2, lista.size());
        assertEquals(0, new BigDecimal("5200.00").compareTo(lista.get(0).getSalarioNovo()));
        assertEquals(0, new BigDecimal("4500.00").compareTo(lista.get(1).getSalarioNovo()));
    }

    @Test
    public void listarPorFuncionarioNaoMisturaOutrosFuncionarios() {
        registro(maria, null, "4500.00", 1_000_000_000_000L);
        registro(joao, null, "4500.00", 1_000_000_000_000L);

        assertEquals(1, dao.listarPorFuncionario(maria.getId()).size());
    }

    @Test
    public void funcionarioSemHistoricoDevolveListaVazia() {
        assertTrue(dao.listarPorFuncionario(maria.getId()).isEmpty());
    }

    @Test
    public void dataDaAlteracaoEPreenchidaAutomaticamente() {
        HistoricoSalarial h = new HistoricoSalarial();
        h.setFuncionario(maria);
        h.setSalarioNovo(new BigDecimal("5000.00"));

        HistoricoSalarial salvo = dao.salvar(h); // dispara o @PrePersist
        em.flush();

        assertNotNull(salvo.getDataAlteracao());
    }
}
