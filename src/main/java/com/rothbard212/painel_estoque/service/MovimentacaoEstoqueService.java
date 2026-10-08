package com.rothbard212.painel_estoque.service;

import com.rothbard212.painel_estoque.exception.SaldoInsuficienteException;
import com.rothbard212.painel_estoque.model.MovimentacaoEstoque;
import com.rothbard212.painel_estoque.model.PedidoCompra;
import com.rothbard212.painel_estoque.model.Produto;
import com.rothbard212.painel_estoque.repository.MovimentacaoEstoqueRepository;
import com.rothbard212.painel_estoque.repository.ProdutoRepository;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class MovimentacaoEstoqueService {

    private static final int MAX_TENTATIVAS = 3;

    private final ProdutoRepository produtoRepository;
    private final MovimentacaoEstoqueRepository movimentacaoRepository;

    public MovimentacaoEstoqueService(ProdutoRepository produtoRepository,
                                      MovimentacaoEstoqueRepository movimentacaoRepository) {
        this.produtoRepository = produtoRepository;
        this.movimentacaoRepository = movimentacaoRepository;
    }

    public void movimentar(Long produtoId, MovimentacaoEstoque.Tipo tipo, Integer quantidade, PedidoCompra referencia) {
        int tentativa = 0;

        while (tentativa < MAX_TENTATIVAS) {
            try {
                executarMovimentacao(produtoId, tipo, quantidade, referencia);
                return;
            } catch (OptimisticLockingFailureException erro) {
                tentativa++;
                if (tentativa >= MAX_TENTATIVAS) {
                    throw erro;
                }
            }
        }
    }

    @Transactional
    protected void executarMovimentacao(Long produtoId, MovimentacaoEstoque.Tipo tipo, Integer quantidade, PedidoCompra referencia) {
        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        if (tipo == MovimentacaoEstoque.Tipo.SAIDA) {
            if (produto.getQuantidade() < quantidade) {
                throw new SaldoInsuficienteException(
                        "Saldo insuficiente para o produto " + produto.getNome()
                );
            }
            produto.setQuantidade(produto.getQuantidade() - quantidade);
        } else {
            produto.setQuantidade(produto.getQuantidade() + quantidade);
        }

        produtoRepository.save(produto);

        MovimentacaoEstoque movimentacao = new MovimentacaoEstoque();
        movimentacao.setProduto(produto);
        movimentacao.setTipo(tipo);
        movimentacao.setQuantidade(quantidade);
        movimentacao.setCriadoEm(LocalDateTime.now());
        movimentacao.setReferencia(referencia);

        movimentacaoRepository.save(movimentacao);
    }

    public List<MovimentacaoEstoque> listarHistoricoPorProduto(Long produtoId) {
        return movimentacaoRepository.findByProdutoIdOrderByIdDesc(produtoId);
    }
}