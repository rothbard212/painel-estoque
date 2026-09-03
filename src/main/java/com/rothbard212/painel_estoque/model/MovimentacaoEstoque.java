package com.rothbard212.painel_estoque.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "movimentacoes_estoque")
@Getter
@Setter
public class MovimentacaoEstoque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    public enum Tipo{
        ENTRADA,SAIDA
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Tipo tipo;

    @Column(nullable = false)
    private Integer quantidade;

    @Column(nullable = false)
    private LocalDateTime criadoEm;

    @ManyToOne
    @JoinColumn(name = "pedido_compra_id")
    private PedidoCompra referencia;

}
