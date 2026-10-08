package com.rothbard212.painel_estoque.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record MovimentacaoSaidaRequest(@NotNull Long produtoId, @NotNull @Positive Integer quantidade) {
}
