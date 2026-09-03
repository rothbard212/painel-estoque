package com.rothbard212.painel_estoque.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "produtos")
@Getter
@Setter
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String sku;

    @ManyToOne
    @JoinColumn(name = "fornecedor_id", nullable = false)
    private Fornecedor fornecedor;

    @Column(name = "preco_custo", nullable = false)
    private Double precoCusto;

    @Column(nullable = false)
    private Integer quantidade;

    @Column(name = "estoque_minimo")
    private Integer estoqueMinimo;

    @Version
    private Long versao;
}