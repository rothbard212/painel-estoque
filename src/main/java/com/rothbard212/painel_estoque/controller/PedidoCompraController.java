package com.rothbard212.painel_estoque.controller;

import com.rothbard212.painel_estoque.dto.PedidoCompraRequest;
import com.rothbard212.painel_estoque.model.PedidoCompra;
import com.rothbard212.painel_estoque.service.PedidoCompraService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pedidos-compra")
public class PedidoCompraController {

    private final PedidoCompraService pedidoCompraService;

    public PedidoCompraController(PedidoCompraService pedidoCompraService) {
        this.pedidoCompraService = pedidoCompraService;
    }

    @PostMapping
    public PedidoCompra criar(@Valid @RequestBody PedidoCompraRequest request) {
        return pedidoCompraService.criar(request);
    }

    @GetMapping
    public List<PedidoCompra> listarTodos() {
        return pedidoCompraService.listarTodos();
    }

    @GetMapping("/{id}")
    public PedidoCompra buscarPorId(@PathVariable Long id) {
        return pedidoCompraService.buscarPorId(id);
    }

    @PostMapping("/{id}/receber")
    public PedidoCompra receber(@PathVariable Long id) {
        return pedidoCompraService.receber(id);
    }
}
