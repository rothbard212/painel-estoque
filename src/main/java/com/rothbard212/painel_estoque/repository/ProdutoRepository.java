package com.rothbard212.painel_estoque.repository;

import com.rothbard212.painel_estoque.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    List<Produto> findByFornecedorId(Long fornecedorId);

    List<Produto> findByQuantidadeLessThan(Integer quantidade);

    @Query("SELECT p FROM Produto p WHERE p.quantidade < p.estoqueMinimo")
    List<Produto> findComEstoqueBaixo();
}
