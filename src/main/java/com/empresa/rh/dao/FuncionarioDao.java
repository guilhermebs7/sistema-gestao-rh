package com.empresa.rh.dao;

import com.empresa.rh.model.Funcionario;
import java.util.List;
import javax.ejb.Stateless;

@Stateless
public class FuncionarioDao extends GenericDao<Funcionario> {

    public FuncionarioDao() {
        super(Funcionario.class);
    }

    @Override
    public List<Funcionario> listarTodos() {
        return em.createQuery("select f from Funcionario f order by f.nome", Funcionario.class)
                .getResultList();
    }

    public Funcionario buscarPorCpf(String cpf) {
        return primeiroResultado(
                em.createQuery("select f from Funcionario f where f.cpf = :cpf", Funcionario.class)
                        .setParameter("cpf", cpf));
    }

    public Funcionario buscarPorMatricula(String matricula) {
        return primeiroResultado(
                em.createQuery("select f from Funcionario f where lower(f.matricula) = lower(:matricula)",
                                Funcionario.class)
                        .setParameter("matricula", matricula));
    }

    // Pesquisa por parte do nome, da matrícula ou do CPF
    public List<Funcionario> pesquisar(String termo) {
        if (termo == null || termo.trim().isEmpty()) {
            return listarTodos();
        }
        String padrao = "%" + termo.trim().toLowerCase() + "%";
        return em.createQuery(
                        "select f from Funcionario f "
                                + "where lower(f.nome) like :termo "
                                + "   or lower(f.matricula) like :termo "
                                + "   or f.cpf like :termo "
                                + "order by f.nome", Funcionario.class)
                .setParameter("termo", padrao)
                .getResultList();
    }

    // Usados para impedir excluir departamento/cargo que ainda têm funcionários
    public long contarPorDepartamento(Long departamentoId) {
        return em.createQuery(
                        "select count(f) from Funcionario f where f.departamento.id = :id", Long.class)
                .setParameter("id", departamentoId)
                .getSingleResult();
    }

    public long contarPorCargo(Long cargoId) {
        return em.createQuery(
                        "select count(f) from Funcionario f where f.cargo.id = :id", Long.class)
                .setParameter("id", cargoId)
                .getSingleResult();
    }
}