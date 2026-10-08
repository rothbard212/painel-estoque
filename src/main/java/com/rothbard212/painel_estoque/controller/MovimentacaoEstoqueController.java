package com.rothbard212.painel_estoque.controller;

import com.rothbard212.painel_estoque.dto.MovimentacaoSaidaRequest;
import com.rothbard212.painel_estoque.model.MovimentacaoEstoque;
import com.rothbard212.painel_estoque.service.MovimentacaoEstoqueService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/movimentacoes")
public class MovimentacaoEstoqueController {

    private final MovimentacaoEstoqueService movimentacaoEstoqueService;

    public MovimentacaoEstoqueController(MovimentacaoEstoqueService movimentacaoEstoqueService) {
        this.movimentacaoEstoqueService = movimentacaoEstoqueService;
    }

    @PostMapping("/saida")
    public void registrarSaida(@Valid @RequestBody MovimentacaoSaidaRequest request) {
        movimentacaoEstoqueService.movimentar(
                request.produtoId(),
                MovimentacaoEstoque.Tipo.SAIDA,
                request.quantidade(),
                null
        );
    }

    @GetMapping("/produto/{produtoId}")
    public List<MovimentacaoEstoque> listarHistorico(@PathVariable Long produtoId) {
        return movimentacaoEstoqueService.listarHistoricoPorProduto(produtoId);
    }
}