package com.empresa.rh.service;

import com.empresa.rh.dao.FuncionarioDao;
import com.empresa.rh.dao.HistoricoSalarialDao;
import com.empresa.rh.exception.NegocioException;
import com.empresa.rh.model.Funcionario;
import com.empresa.rh.model.HistoricoSalarial;
import com.empresa.rh.model.enums.SituacaoFuncionario;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.Stateless;
import javax.inject.Inject;

@Stateless
public class SalarioService {

    @Inject
    private FuncionarioDao funcionarioDao;

    @Inject
    private HistoricoSalarialDao historicoSalarialDao;

    public void atualizar(Long funcionarioId, BigDecimal novoSalario, String motivo) {
        Funcionario f = funcionarioDao.buscarPorId(funcionarioId);
        if (f == null) {
            throw new NegocioException("O funcionário não existe mais.");
        }
        if (f.getSituacao() != SituacaoFuncionario.ATIVO) {
            throw new NegocioException("Funcionário inativo não pode receber reajuste salarial.");
        }

        String motivoLimpo = motivo == null ? "" : motivo.trim();
        List<String> erros = new ArrayList<>();

        if (novoSalario == null || novoSalario.compareTo(BigDecimal.ZERO) <= 0) {
            erros.add("O novo salário deve ser maior que zero.");
        } else {
            novoSalario = novoSalario.setScale(2, RoundingMode.HALF_UP);
            if (novoSalario.compareTo(f.getSalarioAtual()) == 0) {
                erros.add("O novo salário deve ser diferente do salário atual.");
            }
        }
        if (motivoLimpo.isEmpty()) {
            erros.add("O motivo da alteração é obrigatório.");
        } else if (motivoLimpo.length() > 255) {
            erros.add("O motivo deve ter no máximo 255 caracteres.");
        }
        if (!erros.isEmpty()) {
            throw new NegocioException(erros);
        }

        // 1. Registra o histórico com o valor ANTIGO e o NOVO
        HistoricoSalarial h = new HistoricoSalarial();
        h.setFuncionario(f);
        h.setSalarioAnterior(f.getSalarioAtual());
        h.setSalarioNovo(novoSalario);
        h.setMotivo(motivoLimpo);
        historicoSalarialDao.salvar(h);

        // 2. Atualiza o salário atual. O objeto "f" é gerenciado na transação,
        //    então o Hibernate envia o UPDATE sozinho no commit (dirty checking).
        f.setSalarioAtual(novoSalario);
    }

    // Mais recentes primeiro
    public List<HistoricoSalarial> listarHistorico(Long funcionarioId) {
        return historicoSalarialDao.listarPorFuncionario(funcionarioId);
    }
}