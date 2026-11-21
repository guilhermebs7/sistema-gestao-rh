package com.empresa.rh.dao;

import com.empresa.rh.model.Cargo;
import com.empresa.rh.model.Departamento;
import com.empresa.rh.model.Funcionario;
import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 * Base dos testes de DAO. Usa um banco H2 em memória (unidade "rhTestPU").
 * Cada teste roda dentro de uma transação que é DESFEITA no final,
 * então um teste nunca enxerga os dados de outro.
 */
public abstract class DaoTestBase {

    private static EntityManagerFactory emf;

    protected EntityManager em;

    @BeforeClass
    public static void abrirBanco() {
        emf = Persistence.createEntityManagerFactory("rhTestPU");
    }

    @AfterClass
    public static void fecharBanco() {
        if (emf != null) {
            emf.close();
        }
    }

    @Before
    public void iniciarTransacao() {
        em = emf.createEntityManager();
        em.getTransaction().begin();
    }

    @After
    public void desfazerTransacao() {
        if (em.getTransaction().isActive()) {
            em.getTransaction().rollback();
        }
        em.close();
    }

    /** No WildFly o servidor injeta o EntityManager; no teste, nós o colocamos à mão. */
    protected <D extends GenericDao<?>> D com(D dao) {
        dao.em = em; // "em" é protected e este teste está no mesmo pacote do DAO
        return dao;
    }

    // ------------------------------------------------------------ dados de apoio

    protected Departamento novoDepartamento(String nome) {
        Departamento d = new Departamento();
        d.setNome(nome);
        em.persist(d);
        return d;
    }

    protected Cargo novoCargo(String nome, String salarioBase) {
        Cargo c = new Cargo();
        c.setNome(nome);
        c.setSalarioBase(new BigDecimal(salarioBase));
        em.persist(c);
        return c;
    }

    /** Monta um funcionário SEM gravar no banco. */
    protected Funcionario criarFuncionario(String matricula, String nome, String cpf,
                                           Departamento departamento, Cargo cargo) {
        Funcionario f = new Funcionario();
        f.setMatricula(matricula);
        f.setNome(nome);
        f.setCpf(cpf);
        f.setDataAdmissao(new Date());
        f.setSalarioAtual(cargo.getSalarioBase());
        f.setDepartamento(departamento);
        f.setCargo(cargo);
        return f;
    }

    /** Monta e GRAVA o funcionário. */
    protected Funcionario novoFuncionario(String matricula, String nome, String cpf,
                                          Departamento departamento, Cargo cargo) {
        Funcionario f = criarFuncionario(matricula, nome, cpf, departamento, cargo);
        em.persist(f);
        return f;
    }
}
