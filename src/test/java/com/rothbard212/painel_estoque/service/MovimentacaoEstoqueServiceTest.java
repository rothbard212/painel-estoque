package com.rothbard212.painel_estoque.service;

import com.rothbard212.painel_estoque.exception.SaldoInsuficienteException;
import com.rothbard212.painel_estoque.model.MovimentacaoEstoque;
import com.rothbard212.painel_estoque.model.Produto;
import com.rothbard212.painel_estoque.repository.MovimentacaoEstoqueRepository;
import com.rothbard212.painel_estoque.repository.ProdutoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovimentacaoEstoqueServiceTest {

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private MovimentacaoEstoqueRepository movimentacaoRepository;

    @InjectMocks
    private MovimentacaoEstoqueService service;

    private Produto produto;

    @BeforeEach
    void setUp() {
        produto = new Produto();
        produto.setId(1L);
        produto.setNome("Produto Teste");
        produto.setQuantidade(10);
    }

    @Test
    void deveRealizarSaidaComSaldoSuficiente() {
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));

        service.movimentar(1L, MovimentacaoEstoque.Tipo.SAIDA, 5, null);

        assertEquals(5, produto.getQuantidade());
        verify(produtoRepository).save(produto);
        verify(movimentacaoRepository).save(any(MovimentacaoEstoque.class));
    }

    @Test
    void deveLancarExcecaoQuandoSaldoInsuficiente() {
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));

        assertThrows(SaldoInsuficienteException.class, () ->
                service.movimentar(1L, MovimentacaoEstoque.Tipo.SAIDA, 15, null));

        verify(produtoRepository, never()).save(any());
    }

    @Test
    void deveRealizarEntradaSomandoQuantidade() {
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));

        service.movimentar(1L, MovimentacaoEstoque.Tipo.ENTRADA, 20, null);

        assertEquals(30, produto.getQuantidade());
    }
}