package com.empresa.rh.service;

import com.empresa.rh.dao.CargoDao;
import com.empresa.rh.dao.FuncionarioDao;
import com.empresa.rh.exception.NegocioException;
import com.empresa.rh.model.Cargo;
import java.math.RoundingMode;
import java.util.List;
import javax.ejb.Stateless;
import javax.inject.Inject;

@Stateless
public class CargoService extends BaseService {

    @Inject
    private CargoDao cargoDao;

    @Inject
    private FuncionarioDao funcionarioDao;

    public Cargo salvar(Cargo cargo) {
        if (cargo.getNome() != null) {
            cargo.setNome(cargo.getNome().trim());
        }

        validar(cargo); // garante que salarioBase não é nulo nem negativo

        // Dinheiro sempre com exatamente 2 casas decimais
        cargo.setSalarioBase(cargo.getSalarioBase().setScale(2, RoundingMode.HALF_UP));

        Cargo existente = cargoDao.buscarPorNome(cargo.getNome());
        if (existente != null && !existente.getId().equals(cargo.getId())) {
            throw new NegocioException("Já existe um cargo chamado \"" + cargo.getNome() + "\".");
        }

        return cargoDao.salvar(cargo);
    }

    public void excluir(Long id) {
        Cargo cargo = cargoDao.buscarPorId(id);
        if (cargo == null) {
            throw new NegocioException("O cargo não existe mais.");
        }

        long quantidade = funcionarioDao.contarPorCargo(id);
        if (quantidade > 0) {
            throw new NegocioException("Não é possível excluir \"" + cargo.getNome()
                    + "\": existem " + quantidade + " funcionário(s) neste cargo.");
        }

        cargoDao.remover(id);
    }

    public Cargo buscarPorId(Long id) {
        return cargoDao.buscarPorId(id);
    }

    public List<Cargo> listarTodos() {
        return cargoDao.listarTodos();
    }
}