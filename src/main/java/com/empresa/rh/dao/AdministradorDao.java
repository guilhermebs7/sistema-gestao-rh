package com.empresa.rh.dao;

import com.empresa.rh.model.Administrador;
import javax.ejb.Stateless;

@Stateless
public class AdministradorDao extends GenericDao<Administrador> {

    public AdministradorDao() {
        super(Administrador.class);
    }

    public Administrador buscarPorLogin(String login) {
        return primeiroResultado(
                em.createQuery("select a from Administrador a where a.login = :login",
                                Administrador.class)
                        .setParameter("login", login));
    }
}