package com.rothbard212.painel_estoque.controller;

import com.rothbard212.painel_estoque.dto.LoginRequest;
import com.rothbard212.painel_estoque.dto.LoginResponse;
import com.rothbard212.painel_estoque.service.AutenticacaoService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AutenticacaoController {

    private final AutenticacaoService autenticacaoService;

    public AutenticacaoController(AutenticacaoService autenticacaoService) {
        this.autenticacaoService = autenticacaoService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return autenticacaoService.login(request);
    }
}