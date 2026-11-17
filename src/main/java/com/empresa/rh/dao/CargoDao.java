package com.empresa.rh.dao;

import com.empresa.rh.model.Cargo;
import java.util.List;
import javax.ejb.Stateless;

@Stateless
public class CargoDao extends GenericDao<Cargo> {

    public CargoDao() {
        super(Cargo.class);
    }

    @Override
    public List<Cargo> listarTodos() {
        return em.createQuery("select c from Cargo c order by c.nome", Cargo.class)
                .getResultList();
    }

    public Cargo buscarPorNome(String nome) {
        return primeiroResultado(
                em.createQuery("select c from Cargo c where lower(c.nome) = lower(:nome)",
                                Cargo.class)
                        .setParameter("nome", nome));
    }
}