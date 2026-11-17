package com.empresa.rh.dao;

import com.empresa.rh.model.Departamento;
import java.util.List;
import javax.ejb.Stateless;

@Stateless //transforma uma classe Java comum em um componente de negocio gerenciado pelo servidor (não guarda memória do user : ele n armazena dados específicos de quem a chamou em seus atributos
public class DepartamentoDao extends GenericDao<Departamento> {

    public DepartamentoDao() {
        super(Departamento.class);
    }

    @Override
    public List<Departamento> listarTodos() {
        return em.createQuery("select d from Departamento d order by d.nome", Departamento.class)
                .getResultList();
    }

    // Busca ignorando maiúsculas/minúsculas (usado para evitar nomes duplicados)
    public Departamento buscarPorNome(String nome) {
        return primeiroResultado(
                em.createQuery("select d from Departamento d where lower(d.nome) = lower(:nome)",
                                Departamento.class)
                        .setParameter("nome", nome));
    }
}