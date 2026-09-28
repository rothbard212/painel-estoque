package com.rothbard212.painel_estoque.service;

import com.rothbard212.painel_estoque.model.Fornecedor;
import com.rothbard212.painel_estoque.model.Produto;
import com.rothbard212.painel_estoque.repository.ProdutoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.rothbard212.painel_estoque.repository.FornecedorRepository;

@ExtendWith(MockitoExtension.class)
class ProdutoServiceTest {

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private FornecedorRepository fornecedorRepository;

    @InjectMocks
    private ProdutoService service;

    private Produto produto;
    private Fornecedor fornecedor;

    @BeforeEach
    void setUp() {
        fornecedor = new Fornecedor();
        fornecedor.setId(1L);

        produto = new Produto();
        produto.setId(1L);
        produto.setNome("Produto Teste");
        produto.setSku("SKU-001");
        produto.setPrecoCusto(50.0);
        produto.setQuantidade(100);
        produto.setEstoqueMinimo(10);
        produto.setFornecedor(fornecedor);
    }

    @Test
    void deveCriarProdutoComSucesso() {
        when(fornecedorRepository.findById(1L)).thenReturn(Optional.of(fornecedor));
        when(produtoRepository.save(any(Produto.class))).thenReturn(produto);

        Produto resultado = service.criar(produto);

        assertEquals("Produto Teste", resultado.getNome());
        verify(produtoRepository).save(any(Produto.class));
    }

    @Test
    void deveLancarExcecaoQuandoProdutoNaoEncontrado() {
        when(produtoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.buscarPorId(99L));
    }

    @Test
    void deveListarProdutosComEstoqueBaixo() {
        when(produtoRepository.findComEstoqueBaixo()).thenReturn(List.of(produto));

        List<Produto> resultado = service.listarComEstoqueBaixo();

        assertEquals(1, resultado.size());
        verify(produtoRepository).findComEstoqueBaixo();
    }

    @Test
    void deveAtualizarCamposDoProduto() {
        Produto dadosNovos = new Produto();
        dadosNovos.setNome("Produto Atualizado");
        dadosNovos.setPrecoCusto(75.0);
        dadosNovos.setEstoqueMinimo(20);

        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(produtoRepository.save(any(Produto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Produto resultado = service.atualizar(1L, dadosNovos);

        assertEquals("Produto Atualizado", resultado.getNome());
        assertEquals(75.0, resultado.getPrecoCusto());
        assertEquals(20, resultado.getEstoqueMinimo());
    }

    @Test
    void deveDeletarProdutoPorId() {
        service.deletar(1L);

        verify(produtoRepository).deleteById(1L);
    }

}
