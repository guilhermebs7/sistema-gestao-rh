package com.empresa.rh.dao;

import java.io.Serializable;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

//classe base genérica responsável por centralizar as operações de acesso e persistência de dados(CRUD) , permitindo que diferentes entidades reutilizem os mesmos métodos sem a necessidade de implementá-los individualmente em cada DAO.

public abstract class GenericDao<T extends Serializable> {     //o <T permite que a mesma classe trabalhe com diferentes entidades

    // O WildFly injeta o EntityManager da unidade "rhPU" (persistence.xml)
    @PersistenceContext(unitName = "rhPU")   // essa anotacao informa ao servidor de aplicação que ele deve fornecer um EntityMananger(interface do JPA responsável por fazer a ponte entre os seus objetos Java e o banco de dados ) para essa classe
    //rhPU(identifica a unidade de persistência )
    protected EntityManager em;

    private final Class<T> classe;    //guarda qual é a classe da entidade que o DAO está manipulando. ex: se estivermos trabalhando com departamentos : Departamento.class

    protected GenericDao(Class<T> classe) {
        this.classe = classe;
    }

    /**
     * Insere (id nulo) ou atualiza (id preenchido).
     * ATENÇÃO: use SEMPRE o objeto retornado, pois é ele que tem o id gerado.
     * cria os métodos que utilizamos no repository do Spring boot . que salva os dados , deleta e atualiza
     */
    public T salvar(T entidade) {
        return em.merge(entidade);
    }

    public T buscarPorId(Long id) {
        return em.find(classe, id);
    }

    public List<T> listarTodos() {
        // classe.getSimpleName() vem do nosso código, nunca do usuário: seguro
        return em.createQuery("select e from " + classe.getSimpleName() + " e", classe)
                .getResultList();
    }

    public void remover(Long id) {
        T entidade = em.find(classe, id);
        if (entidade != null) {
            em.remove(entidade);
        }
    }

    // Devolve o primeiro resultado da consulta ou null (evita NoResultException)
    protected T primeiroResultado(TypedQuery<T> query) {
        List<T> lista = query.setMaxResults(1).getResultList();
        return lista.isEmpty() ? null : lista.get(0);
    }
}

//essa classe é o repository do Spring boot