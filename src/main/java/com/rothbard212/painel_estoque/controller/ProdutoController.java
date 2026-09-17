package com.rothbard212.painel_estoque.controller;

import com.rothbard212.painel_estoque.model.Produto;
import com.rothbard212.painel_estoque.service.ProdutoService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/produtos")
public class ProdutoController {

    private final ProdutoService produtoService;

    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @PostMapping
    public Produto criar(@RequestBody Produto produto) {
        return produtoService.criar(produto);
    }

    @GetMapping
    public List<Produto> listarTodos() {
        return produtoService.listarTodos();
    }

    @GetMapping("/{id}")
    public Produto buscarPorId(@PathVariable Long id) {
        return produtoService.buscarPorId(id);
    }

    @GetMapping("/fornecedor/{fornecedorId}")
    public List<Produto> listarPorFornecedor(@PathVariable Long fornecedorId) {
        return produtoService.listarPorFornecedor(fornecedorId);
    }

    @GetMapping("/estoque-baixo")
    public List<Produto> listarComEstoqueBaixo() {
        return produtoService.listarComEstoqueBaixo();
    }

    @PutMapping("/{id}")
    public Produto atualizar(@PathVariable Long id, @RequestBody Produto produto) {
        return produtoService.atualizar(id, produto);
    }

    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Long id) {
        produtoService.deletar(id);
    }
}