package com.empresa.rh.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "historico_salarial")
public class HistoricoSalarial implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "funcionario_id", nullable = false)
    private Funcionario funcionario;

    @Column(name = "salario_anterior", precision = 12, scale = 2)
    private BigDecimal salarioAnterior;

    @Column(name = "salario_novo", nullable = false, precision = 12, scale = 2)
    private BigDecimal salarioNovo;

    @Temporal(TemporalType.TIMESTAMP)  //salva tanto a data como a hora
    @Column(name = "data_alteracao", nullable = false)
    private Date dataAlteracao;

    @Column(length = 255)
    private String motivo;

    // Executado automaticamente pelo JPA antes do INSERT
    @PrePersist
    void antesDeGravar() {
        if (dataAlteracao == null) {
            dataAlteracao = new Date();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Funcionario getFuncionario() { return funcionario; }
    public void setFuncionario(Funcionario funcionario) { this.funcionario = funcionario; }

    public BigDecimal getSalarioAnterior() { return salarioAnterior; }
    public void setSalarioAnterior(BigDecimal salarioAnterior) { this.salarioAnterior = salarioAnterior; }

    public BigDecimal getSalarioNovo() { return salarioNovo; }
    public void setSalarioNovo(BigDecimal salarioNovo) { this.salarioNovo = salarioNovo; }

    public Date getDataAlteracao() { return dataAlteracao; }
    public void setDataAlteracao(Date dataAlteracao) { this.dataAlteracao = dataAlteracao; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
}