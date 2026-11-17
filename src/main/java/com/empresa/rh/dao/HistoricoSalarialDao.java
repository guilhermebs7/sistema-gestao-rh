package com.empresa.rh.dao;

import com.empresa.rh.model.HistoricoSalarial;
import java.util.List;
import javax.ejb.Stateless;

@Stateless
public class HistoricoSalarialDao extends GenericDao<HistoricoSalarial> {

    public HistoricoSalarialDao() {
        super(HistoricoSalarial.class);
    }

    // Mais recentes primeiro
    public List<HistoricoSalarial> listarPorFuncionario(Long funcionarioId) {
        return em.createQuery(
                        "select h from HistoricoSalarial h "
                                + "where h.funcionario.id = :id order by h.dataAlteracao desc",
                        HistoricoSalarial.class)
                .setParameter("id", funcionarioId)
                .getResultList();
    }
}