package com.rothbard212.painel_estoque.service;

import com.rothbard212.painel_estoque.model.Fornecedor;
import com.rothbard212.painel_estoque.repository.FornecedorRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FornecedorService {

    private final FornecedorRepository fornecedorRepository;

    public FornecedorService(FornecedorRepository fornecedorRepository) {
        this.fornecedorRepository = fornecedorRepository;
    }

    public Fornecedor criar(Fornecedor fornecedor) {
        return fornecedorRepository.save(fornecedor);
    }

    public List<Fornecedor> listarTodos() {
        return fornecedorRepository.findAll();
    }

    public Fornecedor buscarPorId(Long id) {
        return fornecedorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fornecedor não encontrado"));
    }

    public Fornecedor atualizar(Long id, Fornecedor dadosAtualizados) {
        Fornecedor fornecedor = buscarPorId(id);
        fornecedor.setNome(dadosAtualizados.getNome());
        fornecedor.setCnpj(dadosAtualizados.getCnpj());
        fornecedor.setContato(dadosAtualizados.getContato());
        return fornecedorRepository.save(fornecedor);
    }

    public void deletar(Long id) {
        fornecedorRepository.deleteById(id);
    }
}