package com.rothbard212.painel_estoque.service;

import com.rothbard212.painel_estoque.dto.ItemPedidoRequest;
import com.rothbard212.painel_estoque.dto.PedidoCompraRequest;
import com.rothbard212.painel_estoque.model.*;
import com.rothbard212.painel_estoque.repository.FornecedorRepository;
import com.rothbard212.painel_estoque.repository.ItemPedidoRepository;
import com.rothbard212.painel_estoque.repository.PedidoCompraRepository;
import com.rothbard212.painel_estoque.repository.ProdutoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PedidoCompraService {

    private final PedidoCompraRepository pedidoCompraRepository;
    private final FornecedorRepository fornecedorRepository;
    private final ProdutoRepository produtoRepository;
    private final ItemPedidoRepository itemPedidoRepository;
    private final MovimentacaoEstoqueService movimentacaoEstoqueService;

    public PedidoCompraService(PedidoCompraRepository pedidoCompraRepository,
                               FornecedorRepository fornecedorRepository,
                               ProdutoRepository produtoRepository,
                               ItemPedidoRepository itemPedidoRepository,
                               MovimentacaoEstoqueService movimentacaoEstoqueService) {
        this.pedidoCompraRepository = pedidoCompraRepository;
        this.fornecedorRepository = fornecedorRepository;
        this.produtoRepository = produtoRepository;
        this.itemPedidoRepository = itemPedidoRepository;
        this.movimentacaoEstoqueService = movimentacaoEstoqueService;
    }

    @Transactional
    public PedidoCompra criar(PedidoCompraRequest request) {
        Fornecedor fornecedor = fornecedorRepository.findById(request.fornecedorId())
                .orElseThrow(() -> new RuntimeException("Fornecedor não encontrado"));

        PedidoCompra pedido = new PedidoCompra();
        pedido.setFornecedor(fornecedor);
        pedido.setStatus(PedidoCompra.Status.PENDENTE);
        pedido.setCriadoEm(LocalDateTime.now());
        pedido = pedidoCompraRepository.save(pedido);

        for (ItemPedidoRequest itemRequest : request.itens()) {
            Produto produto = produtoRepository.findById(itemRequest.produtoId())
                    .orElseThrow(() -> new RuntimeException("Produto não encontrado: " + itemRequest.produtoId()));

            ItemPedido item = new ItemPedido();
            item.setPedido(pedido);
            item.setProduto(produto);
            item.setQuantidade(itemRequest.quantidade());
            item.setPrecoUnitario(itemRequest.precoUnitario());

            itemPedidoRepository.save(item);
        }

        return pedido;
    }

    public List<PedidoCompra> listarTodos() {
        return pedidoCompraRepository.findAll();
    }

    public PedidoCompra buscarPorId(Long id) {
        return pedidoCompraRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));
    }

    public PedidoCompra receber(Long pedidoId) {
        PedidoCompra pedido = buscarPorId(pedidoId);

        if (pedido.getStatus() != PedidoCompra.Status.PENDENTE) {
            throw new RuntimeException("Apenas pedidos pendentes podem ser recebidos");
        }

        List<ItemPedido> itens = itemPedidoRepository.findByPedidoId(pedidoId);

        for (ItemPedido item : itens) {
            movimentacaoEstoqueService.movimentar(
                    item.getProduto().getId(),
                    MovimentacaoEstoque.Tipo.ENTRADA,
                    item.getQuantidade(),
                    pedido
            );
        }

        pedido.setStatus(PedidoCompra.Status.RECEBIDO);
        pedido.setRecebidoEm(LocalDateTime.now());

        return pedidoCompraRepository.save(pedido);
    }
}
