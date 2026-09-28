package com.rothbard212.painel_estoque.service;

import com.rothbard212.painel_estoque.model.Fornecedor;
import com.rothbard212.painel_estoque.repository.FornecedorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FornecedorServiceTest {

    @Mock
    private FornecedorRepository fornecedorRepository;

    @InjectMocks
    private FornecedorService service;

    private Fornecedor fornecedor;

    @BeforeEach
    void setUp() {
        fornecedor = new Fornecedor();
        fornecedor.setId(1L);
        fornecedor.setNome("Distribuidora Teste");
        fornecedor.setCnpj("11.111.111/0001-11");
        fornecedor.setContato("contato@teste.com");
    }

    @Test
    void deveCriarFornecedorComSucesso() {
        when(fornecedorRepository.save(fornecedor)).thenReturn(fornecedor);

        Fornecedor resultado = service.criar(fornecedor);

        assertEquals("Distribuidora Teste", resultado.getNome());
        verify(fornecedorRepository).save(fornecedor);
    }

    @Test
    void deveListarTodosOsFornecedores() {
        when(fornecedorRepository.findAll()).thenReturn(List.of(fornecedor));

        List<Fornecedor> resultado = service.listarTodos();

        assertEquals(1, resultado.size());
    }

    @Test
    void deveLancarExcecaoQuandoFornecedorNaoEncontrado() {
        when(fornecedorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.buscarPorId(99L));
    }

    @Test
    void deveAtualizarCamposDoFornecedor() {
        Fornecedor dadosNovos = new Fornecedor();
        dadosNovos.setNome("Nome Atualizado");
        dadosNovos.setCnpj("22.222.222/0001-22");
        dadosNovos.setContato("novo@teste.com");

        when(fornecedorRepository.findById(1L)).thenReturn(Optional.of(fornecedor));
        when(fornecedorRepository.save(any(Fornecedor.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Fornecedor resultado = service.atualizar(1L, dadosNovos);

        assertEquals("Nome Atualizado", resultado.getNome());
        assertEquals("22.222.222/0001-22", resultado.getCnpj());
        assertEquals("novo@teste.com", resultado.getContato());
    }

    @Test
    void deveDeletarFornecedorPorId() {
        service.deletar(1L);

        verify(fornecedorRepository).deleteById(1L);
    }
}