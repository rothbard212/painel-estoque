package com.rothbard212.painel_estoque.service;

import com.rothbard212.painel_estoque.model.Fornecedor;
import com.rothbard212.painel_estoque.model.Produto;
import com.rothbard212.painel_estoque.repository.FornecedorRepository;
import com.rothbard212.painel_estoque.repository.ProdutoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final FornecedorRepository fornecedorRepository;

    public ProdutoService(ProdutoRepository produtoRepository, FornecedorRepository fornecedorRepository) {
        this.produtoRepository = produtoRepository;
        this.fornecedorRepository = fornecedorRepository;
    }

    public Produto criar(Produto produto) {
        Long fornecedorId = produto.getFornecedor().getId();
        Fornecedor fornecedor = fornecedorRepository.findById(fornecedorId)
                .orElseThrow(() -> new RuntimeException("Fornecedor não encontrado"));
        produto.setFornecedor(fornecedor);

        return produtoRepository.save(produto);
    }

    public List<Produto> listarTodos() {
        return produtoRepository.findAll();
    }

    public Produto buscarPorId(Long id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));
    }

    public List<Produto> listarPorFornecedor(Long fornecedorId) {
        return produtoRepository.findByFornecedorId(fornecedorId);
    }

    public List<Produto> listarComEstoqueBaixo() {
        return produtoRepository.findComEstoqueBaixo();
    }

    public Produto atualizar(Long id, Produto dadosAtualizados) {
        Produto produto = buscarPorId(id);
        produto.setNome(dadosAtualizados.getNome());
        produto.setPrecoCusto(dadosAtualizados.getPrecoCusto());
        produto.setEstoqueMinimo(dadosAtualizados.getEstoqueMinimo());
        return produtoRepository.save(produto);
    }

    public void deletar(Long id) {
        produtoRepository.deleteById(id);
    }
}