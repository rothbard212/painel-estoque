package com.rothbard212.painel_estoque.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record PedidoCompraRequest(@NotNull Long fornecedorId,@NotEmpty @Valid List<ItemPedidoRequest> itens) {
}