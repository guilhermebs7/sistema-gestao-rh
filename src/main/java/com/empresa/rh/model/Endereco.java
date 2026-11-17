package com.empresa.rh.model;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Entity
@Table(name = "endereco")
public class Endereco implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Lado DONO do relacionamento: aqui fica a coluna funcionario_id
    @OneToOne(optional = false)
    @JoinColumn(name = "funcionario_id", nullable = false)
    private Funcionario funcionario;

    @Size(max = 150)
    @Column(length = 150)
    private String logradouro;

    @Size(max = 10)
    @Column(length = 10)
    private String numero;

    @Size(max = 100)
    @Column(length = 100)
    private String complemento;

    @Size(max = 100)
    @Column(length = 100)
    private String bairro;

    @Size(max = 100)
    @Column(length = 100)
    private String cidade;

    @Size(min = 2, max = 2, message = "A UF deve ter 2 letras")
    @Column(length = 2)
    private String uf;

    // Só os 8 dígitos, sem hífen
    @Pattern(regexp = "\\d{8}", message = "O CEP deve ter 8 dígitos numéricos")
    @Column(length = 8)
    private String cep;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Funcionario getFuncionario() { return funcionario; }
    public void setFuncionario(Funcionario funcionario) { this.funcionario = funcionario; }

    public String getLogradouro() { return logradouro; }
    public void setLogradouro(String logradouro) { this.logradouro = logradouro; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public String getComplemento() { return complemento; }
    public void setComplemento(String complemento) { this.complemento = complemento; }

    public String getBairro() { return bairro; }
    public void setBairro(String bairro) { this.bairro = bairro; }

    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }

    public String getUf() { return uf; }
    public void setUf(String uf) { this.uf = uf; }

    public String getCep() { return cep; }
    public void setCep(String cep) { this.cep = cep; }
}