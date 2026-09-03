package com.rothbard212.painel_estoque.repository;

import com.rothbard212.painel_estoque.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    List<Produto> findByFornecedorId(Long fornecedorId);

    List<Produto> findByQuantidadeLessThan(Integer quantidade);
}
