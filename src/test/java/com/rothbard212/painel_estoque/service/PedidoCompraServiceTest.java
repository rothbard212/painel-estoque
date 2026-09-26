package com.rothbard212.painel_estoque.service;

import com.rothbard212.painel_estoque.dto.ItemPedidoRequest;
import com.rothbard212.painel_estoque.dto.PedidoCompraRequest;
import com.rothbard212.painel_estoque.model.*;
import com.rothbard212.painel_estoque.repository.FornecedorRepository;
import com.rothbard212.painel_estoque.repository.ItemPedidoRepository;
import com.rothbard212.painel_estoque.repository.PedidoCompraRepository;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoCompraServiceTest {

    @Mock
    private PedidoCompraRepository pedidoCompraRepository;

    @Mock
    private FornecedorRepository fornecedorRepository;

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private ItemPedidoRepository itemPedidoRepository;

    @Mock
    private MovimentacaoEstoqueService movimentacaoEstoqueService;

    @InjectMocks
    private PedidoCompraService service;

    private Fornecedor fornecedor;
    private Produto produto;
    private PedidoCompra pedido;

    @BeforeEach
    void setUp() {
        fornecedor = new Fornecedor();
        fornecedor.setId(1L);
        fornecedor.setNome("Fornecedor Teste");

        produto = new Produto();
        produto.setId(1L);
        produto.setNome("Produto Teste");
        produto.setQuantidade(50);

        pedido = new PedidoCompra();
        pedido.setId(1L);
        pedido.setFornecedor(fornecedor);
        pedido.setStatus(PedidoCompra.Status.PENDENTE);
    }

    @Test
    void deveCriarPedidoComItensQuandoFornecedorEProdutoExistem() {
        when(fornecedorRepository.findById(1L)).thenReturn(Optional.of(fornecedor));
        when(pedidoCompraRepository.save(any(PedidoCompra.class))).thenReturn(pedido);
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));

        PedidoCompraRequest request = new PedidoCompraRequest(
                1L,
                List.of(new ItemPedidoRequest(1L, 10, 45.00))
        );

        PedidoCompra resultado = service.criar(request);

        assertEquals(PedidoCompra.Status.PENDENTE, resultado.getStatus());
        verify(itemPedidoRepository).save(any(ItemPedido.class));
    }

    @Test
    void deveLancarExcecaoQuandoFornecedorNaoExiste() {
        when(fornecedorRepository.findById(99L)).thenReturn(Optional.empty());

        PedidoCompraRequest request = new PedidoCompraRequest(99L, List.of());

        assertThrows(RuntimeException.class, () -> service.criar(request));
        verify(pedidoCompraRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoQuandoProdutoDoItemNaoExiste() {
        when(fornecedorRepository.findById(1L)).thenReturn(Optional.of(fornecedor));
        when(pedidoCompraRepository.save(any(PedidoCompra.class))).thenReturn(pedido);
        when(produtoRepository.findById(999L)).thenReturn(Optional.empty());

        PedidoCompraRequest request = new PedidoCompraRequest(
                1L,
                List.of(new ItemPedidoRequest(999L, 10, 45.00))
        );

        assertThrows(RuntimeException.class, () -> service.criar(request));
    }

    @Test
    void deveChamarMovimentarParaCadaItemAoReceberPedido() {
        ItemPedido item1 = new ItemPedido();
        item1.setProduto(produto);
        item1.setQuantidade(10);

        Produto produto2 = new Produto();
        produto2.setId(2L);
        ItemPedido item2 = new ItemPedido();
        item2.setProduto(produto2);
        item2.setQuantidade(5);

        when(pedidoCompraRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(itemPedidoRepository.findByPedidoId(1L)).thenReturn(List.of(item1, item2));
        when(pedidoCompraRepository.save(any(PedidoCompra.class))).thenReturn(pedido);

        service.receber(1L);

        verify(movimentacaoEstoqueService, times(2))
                .movimentar(anyLong(), eq(MovimentacaoEstoque.Tipo.ENTRADA), anyInt(), eq(pedido));
    }

    @Test
    void deveLancarExcecaoAoTentarReceberPedidoJaRecebido() {
        pedido.setStatus(PedidoCompra.Status.RECEBIDO);
        when(pedidoCompraRepository.findById(1L)).thenReturn(Optional.of(pedido));

        assertThrows(RuntimeException.class, () -> service.receber(1L));
        verify(movimentacaoEstoqueService, never()).movimentar(anyLong(), any(), anyInt(), any());
    }

}
