package com.empresa.rh.service;

import com.empresa.rh.dao.FuncionarioDao;
import com.empresa.rh.dao.HistoricoSalarialDao;
import com.empresa.rh.exception.NegocioException;
import com.empresa.rh.model.Endereco;
import com.empresa.rh.model.Funcionario;
import com.empresa.rh.model.HistoricoSalarial;
import com.empresa.rh.model.enums.SituacaoFuncionario;
import com.empresa.rh.util.CpfUtil;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;
import javax.ejb.Stateless;
import javax.inject.Inject;

@Stateless
public class FuncionarioService extends BaseService {

    private static final List<String> UFS = Arrays.asList(
            "AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO", "MA", "MT", "MS", "MG", "PA",
            "PB", "PR", "PE", "PI", "RJ", "RN", "RS", "RO", "RR", "SC", "SP", "SE", "TO");

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    @Inject
    private FuncionarioDao funcionarioDao;

    @Inject
    private HistoricoSalarialDao historicoSalarialDao;

    // ------------------------------------------------------------------ salvar

    public Funcionario salvar(Funcionario f) {
        normalizar(f);                 // 1. limpa os dados
        validar(f);                    // 2. formato (anotações da entidade)
        validarRegrasDeNegocio(f);     // 3. regras que dependem do banco e de datas

        boolean novo = f.getId() == null;
        f.setSalarioAtual(f.getSalarioAtual().setScale(2, RoundingMode.HALF_UP));

        Funcionario salvo = funcionarioDao.salvar(f);

        if (novo) {
            registrarSalarioInicial(salvo);
        }
        return salvo;
    }

    private void validarRegrasDeNegocio(Funcionario f) {
        List<String> erros = new ArrayList<>();

        // CPF: dígitos verificadores e unicidade
        if (!CpfUtil.isValido(f.getCpf())) {
            erros.add("CPF inválido.");
        } else {
            Funcionario porCpf = funcionarioDao.buscarPorCpf(f.getCpf());
            if (porCpf != null && !porCpf.getId().equals(f.getId())) {
                erros.add("Já existe um funcionário cadastrado com este CPF.");
            }
        }

        // Matrícula única
        Funcionario porMatricula = funcionarioDao.buscarPorMatricula(f.getMatricula());
        if (porMatricula != null && !porMatricula.getId().equals(f.getId())) {
            erros.add("Já existe um funcionário com a matrícula \"" + f.getMatricula() + "\".");
        }

        // Datas
        LocalDate hoje = LocalDate.now();
        LocalDate admissao = paraLocalDate(f.getDataAdmissao());
        if (admissao.isAfter(hoje)) {
            erros.add("A data de admissão não pode ser futura.");
        }
        if (f.getDataNascimento() != null) {
            LocalDate nascimento = paraLocalDate(f.getDataNascimento());
            if (!nascimento.isBefore(hoje)) {
                erros.add("A data de nascimento deve estar no passado.");
            } else if (!nascimento.isBefore(admissao)) {
                erros.add("A data de nascimento deve ser anterior à data de admissão.");
            }
        }

        // E-mail (opcional, mas se vier precisa ter formato válido)
        if (f.getEmail() != null && !EMAIL.matcher(f.getEmail()).matches()) {
            erros.add("E-mail inválido.");
        }

        // UF (opcional, mas se vier precisa existir)
        Endereco e = f.getEndereco();
        if (e != null && e.getUf() != null && !UFS.contains(e.getUf())) {
            erros.add("UF inválida: \"" + e.getUf() + "\".");
        }

        // Salário só muda pela atualização salarial (etapa 8), que grava o histórico
        if (f.getId() != null) {
            Funcionario atual = funcionarioDao.buscarPorId(f.getId());
            if (atual == null) {
                erros.add("O funcionário não existe mais.");
            } else if (atual.getSalarioAtual().compareTo(f.getSalarioAtual()) != 0) {
                erros.add("O salário não pode ser alterado aqui. Use a atualização salarial.");
            }
        }

        if (!erros.isEmpty()) {
            throw new NegocioException(erros);
        }
    }

    // Primeira linha do histórico: salário anterior vazio, salário novo = o da admissão
    private void registrarSalarioInicial(Funcionario salvo) {
        HistoricoSalarial h = new HistoricoSalarial();
        h.setFuncionario(salvo);
        h.setSalarioAnterior(null);
        h.setSalarioNovo(salvo.getSalarioAtual());
        h.setMotivo("Salário inicial (admissão)");
        historicoSalarialDao.salvar(h);
    }

    // ------------------------------------------------------- inativar/reativar

    public void inativar(Long id) {
        alterarSituacao(id, SituacaoFuncionario.INATIVO);
    }

    public void reativar(Long id) {
        alterarSituacao(id, SituacaoFuncionario.ATIVO);
    }

    private void alterarSituacao(Long id, SituacaoFuncionario nova) {
        Funcionario f = funcionarioDao.buscarPorId(id);
        if (f == null) {
            throw new NegocioException("O funcionário não existe mais.");
        }
        if (f.getSituacao() == nova) {
            throw new NegocioException(
                    "O funcionário já está " + nova.getDescricao().toLowerCase() + ".");
        }
        // Não chamamos salvar(): o objeto é "gerenciado" dentro da transação, então o Hibernate
        // percebe a mudança sozinho (dirty checking) e faz o UPDATE no commit.
        f.setSituacao(nova);
    }

    // ---------------------------------------------------------------- consultas

    public Funcionario buscarPorId(Long id) {
        return funcionarioDao.buscarPorId(id);
    }

    public List<Funcionario> pesquisar(String termo) {
        return funcionarioDao.pesquisar(termo);
    }

    // ------------------------------------------------------------- utilitários

    private void normalizar(Funcionario f) {
        f.setNome(limpar(f.getNome()));
        f.setMatricula(upper(limpar(f.getMatricula())));
        f.setCpf(digitos(f.getCpf()));
        f.setRg(limpar(f.getRg()));
        f.setEmail(lower(limpar(f.getEmail())));
        f.setTelefone(limpar(f.getTelefone()));

        Endereco e = f.getEndereco();
        if (e != null) {
            e.setLogradouro(limpar(e.getLogradouro()));
            e.setNumero(limpar(e.getNumero()));
            e.setComplemento(limpar(e.getComplemento()));
            e.setBairro(limpar(e.getBairro()));
            e.setCidade(limpar(e.getCidade()));
            e.setUf(upper(limpar(e.getUf())));
            e.setCep(digitos(e.getCep()));

            // Endereço totalmente em branco = funcionário sem endereço (não grava linha vazia)
            if (enderecoVazio(e)) {
                f.setEndereco(null);
            }
        }
    }

    private boolean enderecoVazio(Endereco e) {
        return e.getLogradouro() == null && e.getNumero() == null && e.getComplemento() == null
                && e.getBairro() == null && e.getCidade() == null && e.getUf() == null
                && e.getCep() == null;
    }

    private String limpar(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private String upper(String s) {
        return s == null ? null : s.toUpperCase();
    }

    private String lower(String s) {
        return s == null ? null : s.toLowerCase();
    }

    private String digitos(String s) {
        if (s == null) {
            return null;
        }
        String d = s.replaceAll("\\D", "");
        return d.isEmpty() ? null : d;
    }

    // O Hibernate devolve java.sql.Date, cujo toInstant() lança exceção; por isso getTime()
    private LocalDate paraLocalDate(Date data) {
        return Instant.ofEpochMilli(data.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
    }
}