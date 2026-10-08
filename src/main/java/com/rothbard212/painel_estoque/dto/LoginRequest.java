package com.rothbard212.painel_estoque.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(@NotBlank String email, @NotBlank String senha) {
}
