package com.rothbard212.painel_estoque.dto;

import java.util.List;

public record PedidoCompraRequest(Long fornecedorId, List<ItemPedidoRequest> itens) {
}