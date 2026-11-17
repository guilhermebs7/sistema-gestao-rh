package com.empresa.rh.dao;

import com.empresa.rh.model.Funcionario;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.TypedQuery;

@Stateless
public class FuncionarioDao extends GenericDao<Funcionario> {

    public FuncionarioDao() {
        super(Funcionario.class);
    }

    @Override
    public List<Funcionario> listarTodos() {
        return pesquisar(null);
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

    /**
     * Pesquisa por parte do nome, da matrícula ou do CPF (com ou sem pontuação).
     * Termo vazio devolve todos.
     */
    public List<Funcionario> pesquisar(String termo) {
        String texto = termo == null ? "" : termo.trim();
        String digitos = texto.replaceAll("\\D", "");
        boolean filtrar = !texto.isEmpty();

        // Só montamos o TEXTO da consulta com pedaços fixos (escritos por nós).
        // O que o usuário digitou entra SEMPRE por setParameter: seguro contra SQL Injection.
        StringBuilder jpql = new StringBuilder(
                "select f from Funcionario f "
                        + "join fetch f.departamento join fetch f.cargo left join fetch f.endereco ");
        if (filtrar) {
            jpql.append("where (lower(f.nome) like :termo or lower(f.matricula) like :termo ");
            if (!digitos.isEmpty()) {
                jpql.append("or f.cpf like :cpf ");
            }
            jpql.append(") ");
        }
        jpql.append("order by f.nome");

        TypedQuery<Funcionario> query = em.createQuery(jpql.toString(), Funcionario.class);
        if (filtrar) {
            query.setParameter("termo", "%" + texto.toLowerCase() + "%");
            if (!digitos.isEmpty()) {
                query.setParameter("cpf", "%" + digitos + "%");
            }
        }
        return query.getResultList();
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