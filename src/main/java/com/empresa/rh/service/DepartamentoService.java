package com.empresa.rh.service;

import com.empresa.rh.dao.DepartamentoDao;
import com.empresa.rh.dao.FuncionarioDao;
import com.empresa.rh.exception.NegocioException;
import com.empresa.rh.model.Departamento;
import java.util.List;
import javax.ejb.Stateless;
import javax.inject.Inject;

@Stateless
public class DepartamentoService extends BaseService {

    @Inject
    private DepartamentoDao departamentoDao;

    @Inject
    private FuncionarioDao funcionarioDao;

    public Departamento salvar(Departamento departamento) {
        // 1. Limpa espaços acidentais ("  RH  " vira "RH")
        if (departamento.getNome() != null) {
            departamento.setNome(departamento.getNome().trim());
        }

        // 2. Validação de formato (anotações da entidade)
        validar(departamento);

        // 3. Regra de negócio: nome único (ignorando o próprio registro na edição)
        Departamento existente = departamentoDao.buscarPorNome(departamento.getNome());
        if (existente != null && !existente.getId().equals(departamento.getId())) {
            throw new NegocioException(
                    "Já existe um departamento chamado \"" + departamento.getNome() + "\".");
        }

        return departamentoDao.salvar(departamento);
    }

    public void excluir(Long id) {
        Departamento departamento = departamentoDao.buscarPorId(id);
        if (departamento == null) {
            throw new NegocioException("O departamento não existe mais.");
        }

        // Regra de negócio: não excluir se houver funcionários vinculados
        long quantidade = funcionarioDao.contarPorDepartamento(id);
        if (quantidade > 0) {
            throw new NegocioException("Não é possível excluir \"" + departamento.getNome()
                    + "\": existem " + quantidade + " funcionário(s) vinculado(s).");
        }

        departamentoDao.remover(id);
    }

    public Departamento buscarPorId(Long id) {
        return departamentoDao.buscarPorId(id);
    }

    public List<Departamento> listarTodos() {
        return departamentoDao.listarTodos();
    }
}