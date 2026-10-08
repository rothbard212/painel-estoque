package com.rothbard212.painel_estoque.service;

import com.rothbard212.painel_estoque.model.Fornecedor;
import com.rothbard212.painel_estoque.model.MovimentacaoEstoque;
import com.rothbard212.painel_estoque.model.Produto;
import com.rothbard212.painel_estoque.repository.FornecedorRepository;
import com.rothbard212.painel_estoque.repository.ProdutoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
class MovimentacaoEstoqueConcorrenciaTest {

    @Autowired
    private MovimentacaoEstoqueService service;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private FornecedorRepository fornecedorRepository;

    @Test
    void duasThreadsRetirandoEstoqueSimultaneamenteNaoDevemPerderAtualizacao() throws InterruptedException {

        String sufixoUnico = String.valueOf(System.currentTimeMillis());

        Fornecedor fornecedor = new Fornecedor();
        fornecedor.setNome("Fornecedor Teste");
        fornecedor.setCnpj("CNPJ-TESTE-" + sufixoUnico);
        fornecedor.setContato("contato@teste.com");
        fornecedor = fornecedorRepository.save(fornecedor);

        Produto produto = new Produto();
        produto.setNome("Produto Concorrencia");
        produto.setSku("SKU-CONCORRENCIA-" + sufixoUnico);
        produto.setPrecoCusto(10.0);
        produto.setQuantidade(100);
        produto.setFornecedor(fornecedor);
        produto = produtoRepository.save(produto);

        Long produtoId = produto.getId();
        CyclicBarrier barreira = new CyclicBarrier(2);
        AtomicInteger falhas = new AtomicInteger(0);

        Runnable tarefa = () -> {
            try {
                barreira.await(); // sincroniza as duas threads para disparar juntas
                service.movimentar(produtoId, MovimentacaoEstoque.Tipo.SAIDA, 30, null);
            } catch (Exception erro) {
                falhas.incrementAndGet();
            }
        };

        Thread thread1 = new Thread(tarefa);
        Thread thread2 = new Thread(tarefa);

        thread1.start();
        thread2.start();
        thread1.join();
        thread2.join();

        Produto produtoFinal = produtoRepository.findById(produtoId).orElseThrow();

        // 100 inicial - 30 - 30 = 40, SE as duas movimentações foram aplicadas corretamente
        assertEquals(40, produtoFinal.getQuantidade());
    }
}