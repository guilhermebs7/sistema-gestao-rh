package com.empresa.rh.model;

import com.empresa.rh.model.enums.SituacaoFuncionario;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.Valid;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Entity
@Table(name = "funcionario")
public class Funcionario implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "A matrícula é obrigatória")
    @Size(max = 20, message = "A matrícula deve ter no máximo 20 caracteres")
    @Column(nullable = false, length = 20)
    private String matricula;

    @NotNull(message = "O nome é obrigatório")
    @Size(min = 3, max = 150, message = "O nome deve ter entre 3 e 150 caracteres")
    @Column(nullable = false, length = 150)
    private String nome;

    // Guardamos só os 11 dígitos, sem pontos e traço
    @NotNull(message = "O CPF é obrigatório")
    @Pattern(regexp = "\\d{11}", message = "O CPF deve ter 11 dígitos numéricos")
    @Column(nullable = false, length = 11)
    private String cpf;

    @Size(max = 20)
    @Column(length = 20)
    private String rg;

    @Temporal(TemporalType.DATE)
    @Column(name = "data_nascimento")
    private Date dataNascimento;

    @Size(max = 150)
    @Column(length = 150)
    private String email;

    @Size(max = 20)
    @Column(length = 20)
    private String telefone;

    @NotNull(message = "A data de admissão é obrigatória")
    @Temporal(TemporalType.DATE)
    @Column(name = "data_admissao", nullable = false)
    private Date dataAdmissao;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private SituacaoFuncionario situacao = SituacaoFuncionario.ATIVO;

    @NotNull(message = "O salário é obrigatório")
    @DecimalMin(value = "0.00", message = "O salário não pode ser negativo")
    @Column(name = "salario_atual", nullable = false, precision = 12, scale = 2)
    private BigDecimal salarioAtual;

    @NotNull(message = "Selecione o departamento")
    @ManyToOne
    @JoinColumn(name = "departamento_id", nullable = false)
    private Departamento departamento;

    @NotNull(message = "Selecione o cargo")
    @ManyToOne
    @JoinColumn(name = "cargo_id", nullable = false)
    private Cargo cargo;

    // Lado "inverso" do 1:1: a chave estrangeira fica na tabela endereco
    @Valid
    @OneToOne(mappedBy = "funcionario", cascade = CascadeType.ALL, orphanRemoval = true)
    private Endereco endereco;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public String getRg() { return rg; }
    public void setRg(String rg) { this.rg = rg; }

    public Date getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(Date dataNascimento) { this.dataNascimento = dataNascimento; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public Date getDataAdmissao() { return dataAdmissao; }
    public void setDataAdmissao(Date dataAdmissao) { this.dataAdmissao = dataAdmissao; }

    public SituacaoFuncionario getSituacao() { return situacao; }
    public void setSituacao(SituacaoFuncionario situacao) { this.situacao = situacao; }

    public BigDecimal getSalarioAtual() { return salarioAtual; }
    public void setSalarioAtual(BigDecimal salarioAtual) { this.salarioAtual = salarioAtual; }

    public Departamento getDepartamento() { return departamento; }
    public void setDepartamento(Departamento departamento) { this.departamento = departamento; }

    public Cargo getCargo() { return cargo; }
    public void setCargo(Cargo cargo) { this.cargo = cargo; }

    public Endereco getEndereco() { return endereco; }

    // Mantém os DOIS lados do relacionamento sincronizados
    public void setEndereco(Endereco endereco) {
        this.endereco = endereco;
        if (endereco != null) {
            endereco.setFuncionario(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Funcionario)) return false;
        Funcionario outro = (Funcionario) o;
        return id != null && id.equals(outro.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}