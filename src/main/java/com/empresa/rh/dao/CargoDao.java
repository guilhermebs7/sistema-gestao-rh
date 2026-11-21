package com.empresa.rh.dao;

import com.empresa.rh.model.Cargo;
import java.util.List;
import javax.ejb.Stateless;

@Stateless // ativa automaticamente o gerenciamento de transações e injeção do EntityManager pelo servidor (WildFly), recursos essenciais para acessar o banco
//ele sinaliza uma classe de lógica de negócio que não guarda dados do usuário entre uma requisição e outra
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