package com.rothbard212.painel_estoque.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "fornecedores")
@Getter
@Setter
public class Fornecedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String cnpj;

    @OneToMany(mappedBy = "fornecedor")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<Produto> produtos;

    @Column(nullable = false)
    private String contato;

    @Version
    private Long versao;
}
